//package androidapp;

import java.net.Socket;
import java.net.SocketException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.JPanel;

//import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
//import java.awt.GridBagLayout;
import java.awt.GridLayout;



public class Main {

    private static final int TCP_SERVER_PORT = 38300;
    private static boolean connected = false;
    private static boolean recording = false;
    private Socket socket;
    private PrintWriter out;
	private Scanner sc;
    private BufferedReader in;
	
    private JLabel statusLabel;
    private JLabel cameraLabel;
    private JLabel photoLabel;
    private JLabel batteryLabel;
    private JLabel memoryLabel;
	private JButton startButton;
    private JButton stopButton;

    private final JLabel statusText;
    private final JLabel cameraText;
    private final JLabel photoText;
    private final JLabel memoryText;



    public static final Color DARK_GREEN = new Color(0,153,0);
	
	private ActionListener startAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			System.out.println("c: START ACTION");
			sendStartCommand();
		}
	};

	private ActionListener stopAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			System.out.println("c: STOP ACTION");
			sendStopCommand();
		}
	};
	
    public static void main(String[] args) {
   	
		Main main = new Main();
		
		main.startAdb();
        main.createConnection();
        //System.out.println(main.socket.isConnected());
    }

	public Main() {
        JFrame guiFrame = new JFrame();
        
        JPanel guiPanel = new JPanel();
        JPanel textPanel = new JPanel(new GridLayout(0, 4));
		
		//make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("OnSite Recording Controller");
        guiFrame.setSize(400, 200);
		
		//This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
    
        statusText = new JLabel("Status:");
        statusText.setPreferredSize(new Dimension(20, 20));
        cameraText = new JLabel("Camera:");
        cameraText.setPreferredSize(new Dimension(20, 20));
        photoText = new JLabel("Photo:");
        photoText.setPreferredSize(new Dimension(20, 20));
        memoryText = new JLabel("Memory:");
        memoryText.setPreferredSize(new Dimension(20, 20));

        statusLabel = new JLabel("Not Connected");
        statusLabel.setForeground(Color.red);
        statusLabel.setPreferredSize(new Dimension(80, 20));
        cameraLabel = new JLabel("Not Recording");
        cameraLabel.setForeground(Color.red);
        cameraLabel.setPreferredSize(new Dimension(80, 20));
        photoLabel = new JLabel("no photo");
        photoLabel.setPreferredSize(new Dimension(120, 20));
        memoryLabel = new JLabel("");
        memoryLabel.setPreferredSize(new Dimension(20, 20));
        //photoLabel.setHorizontalAlignment(JLabel.LEFT);

		startButton = new JButton("Start Recording");
        startButton.setPreferredSize(new Dimension(150, 55));
        stopButton = new JButton("Stop Recording");
        stopButton.setPreferredSize(new Dimension(150, 55));
		
        startButton.addActionListener(startAction);
        stopButton.addActionListener(stopAction);
        
        textPanel.add(statusText);
        textPanel.add(statusLabel);
        textPanel.add(cameraText);
        textPanel.add(cameraLabel);
        textPanel.add(photoText);
        textPanel.add(photoLabel);
        textPanel.add(memoryText);
        //textPanel.add(memoryLabel);
		guiPanel.add(startButton);
        guiPanel.add(stopButton);
		     
        guiFrame.add(textPanel, BorderLayout.NORTH);
        guiFrame.add(guiPanel, BorderLayout.SOUTH);
        guiFrame.pack();
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
    
    private synchronized void isConnected(boolean state) {
        connected = state;
        if (connected == true) {
            statusLabel.setText("Connected");
            statusLabel.setForeground(DARK_GREEN);
        } else {
            statusLabel.setText("Not Connected");
            statusLabel.setForeground(Color.red);
        }
    }

    private synchronized void isRecording(boolean state) {
        recording = state;
        if (recording == true) {
            cameraLabel.setText("Recording");
            cameraLabel.setForeground(DARK_GREEN);
        } else {
            cameraLabel.setText("Not Recording");
            cameraLabel.setForeground(Color.red);
        }
    }
	
    private void createConnection() {
        //Create socket connection
        try{
            socket = new Socket("localhost",TCP_SERVER_PORT);  
            //socket = new Socket("192.168.1.3",TCP_SERVER_PORT);         
            System.out.println("Socket Created");
            out = new PrintWriter(socket.getOutputStream(), true);     
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(readFromServer).start();
            
            Thread closeSocketOnShutdown = new Thread() {
                public void run() {
                    try {
                        socket.close();
                        System.out.println("Socket Shutdown");      
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(closeSocketOnShutdown);
        } 
        catch (UnknownHostException e) {
            System.out.println("Socket connection problem (Unknown host)"+ e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not initialize I/O on socket "+ e.getMessage());
        }
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
                    System.out.println(buffer.toString());
                    if (buffer.toString().contains("NOTRECORDING")) {
                        isRecording(false);
                    } else if (buffer.toString().contains("RECORDING")) {
                        isRecording(true);
                    } else if (buffer.toString().contains("CONNECTED")) {
                        isConnected(true);
                    } else if (buffer.toString().contains("PAUSED")) {
                        //isConnected(false);
                    } else if (buffer.toString().contains("RESUMED")) {
                        //isConnected(true);
                    } else if (buffer.toString().contains("MEMORY")) {
                        memoryLabel.setText(buffer.toString());
                    } else {
                        photoLabel.setText(buffer.toString());
                    }
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
            //Process process=Runtime.getRuntime().exec("C:\\and_tools\\platform-tools-latest-windows\\platform-tools\\adb.exe forward tcp:38300 tcp:38300");
            Process process=Runtime.getRuntime().exec("C:\\and\\platform-tools\\adb.exe forward tcp:38300 tcp:38300");
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

}