import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

public class Main_HH {

	private Socket socket;
	private PrintWriter out;
	private Scanner sc;
	private BufferedReader in;
	
	private JLabel statusLabel;
	private JButton startButton;
    private JButton stopButton;
	
	private ActionListener startAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			System.out.println("START ACTION");
			sendStartCommand();
		}
	};

	private ActionListener stopAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			System.out.println("STOP ACTION");
			sendStopCommand();
		}
	};
	
    public static void main(String[] args) {
        System.out.println("Hello World!");
		
		Main_HH main = new Main_HH();
		
		main.startAdb();
		main.createConnection();
    }

	public Main_HH() {
		JFrame guiFrame = new JFrame();
		
		//make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("OnSite Recording Controller");
        guiFrame.setSize(300,200);
		
		//This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
		
		statusLabel = new JLabel("Status: Not Connected");
		startButton = new JButton("Start Recording");
        startButton.setPreferredSize(new Dimension(300, 55));
        stopButton = new JButton("Stop Recording");
        stopButton.setPreferredSize(new Dimension(300, 55));
		
        startButton.addActionListener(startAction);
		stopButton.addActionListener(stopAction);
		
		guiFrame.add(statusLabel, BorderLayout.NORTH);
		guiFrame.add(startButton, BorderLayout.CENTER);
		guiFrame.add(stopButton, BorderLayout.SOUTH);
		
		guiFrame.setVisible(true);
	}
	
	private void sendStopCommand() {
		out.println("STOP");
		
		try {
		    Thread.sleep(200);
		} catch(Exception ex) {
			
		}
		sendStatusCommand();
	}
	
	private void sendStartCommand() {
		out.println("START");
		
		try {
		    Thread.sleep(200);
		} catch(Exception ex) {
			
		}		
		sendStatusCommand();
	}

	private void sendStatusCommand() {
		out.println("STATUS");
	}
	
    private void createConnection() {
        //Create socket connection
        try{
            socket = new Socket("localhost",38300);
            System.out.println("Socket Created");
            out = new PrintWriter(socket.getOutputStream(), true);
            //out.println("Hey Server!\n");

            new Thread(readFromServer).start();
            Thread closeSocketOnShutdown = new Thread() {
                public void run() {
                    try {
						System.out.println("Socket Shutdown");
                        socket.close();
                    } 
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(closeSocketOnShutdown);
        } 
        catch (UnknownHostException e) {
            System.out.println("Socket connection problem (Unknown host)"+e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not initialize I/O on socket "+e.getMessage());
        }
    }

    /**
     * Execute a command
     */
    private String executeCommand(String command) {
		StringBuffer sb = new StringBuffer();
        String[] commands = new String[]{"/bin/sh","-c", command};
        try {
            Process proc = new ProcessBuilder(commands).start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String s = null;
            while ((s = stdInput.readLine()) != null) 
            {
                sb.append(s);
                sb.append("\n");
            }
            while ((s = stdError.readLine()) != null) 
            {
                sb.append(s);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		return sb.toString();
    }

    /**
     * Start the adb bridge
     */
    private void startAdb() {
		try {
			Process process=Runtime.getRuntime().exec("C:\\and_tools\\platform-tools-latest-windows\\platform-tools\\adb.exe forward tcp:38300 tcp:38300");
			sc = new Scanner(process.getErrorStream());
			if (sc.hasNext()) 
			{
				while (sc.hasNext()) 
					System.out.print(sc.next()+" ");
				System.out.println("\nCannot start the Android debug bridge");
			}
			sc.close();
		} catch (IOException ioEx) {
			System.out.println("\nCannot start the Android debug bridge, IOException");
		}
    }

    /**
     * Get the device id through adb
     */
    private String getDeviceId() {
        //Extracting Device Id through ADB
        String device = "";
        String[] device_list=executeCommand("adb devices").split("\\r?\\n");
        System.out.println(device_list);
        if(device_list.length>1)
        {
            if(device_list[1].matches(".*\\d.*"))
            {
                String[] device_id=device_list[1].split("\\s+");
                String device_name=""+executeCommand("adb -s "+device_id[0]+" shell getprop ro.product.manufacturer")+executeCommand("adb -s "+device_id[0]+" shell getprop ro.product.model");
                device_name=device_name.replaceAll("\\s+"," ");
                System.out.println("\n"+device_name+" : "+device_id[0]);
                device=device_id[0];
                System.out.println("\n"+device);
            }
            else
            {
                System.out.println("Please attach a device");
            }
        }
        else
        {
            System.out.println("Please attach a device");
        }
        return device;
    }



    /**
     * Runnable that will read from the server on a thread
     */
    private Runnable readFromServer = new Runnable() {
        @Override
        public void run() {
            try {
				String buffer;
                System.out.println("Reading From Server");
                
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				System.out.println("in = " + (in == null ? "null" : "not null"));
				
                while ((buffer=in.readLine())!=null) {
					if (buffer.toString().contains("STOPPED")) {
						statusLabel.setText("Status: Not Recording");
					} else if (buffer.toString().contains("RECORDING")) {
						statusLabel.setText("Status: Recording");
					}
                    System.out.println(buffer);
                }
            } catch (IOException e) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    };

}