import java.net.Socket;
import java.net.SocketException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.net.UnknownHostException;
import java.net.InetAddress;

public class Client {

    private Socket socket;
    private PrintWriter out;
	//private Scanner sc;
    private BufferedReader in;

    private static int TCP_SERVER_PORT;
    private static String IP_ADDRESS;
    private static final int MAX_CONNECT = 10;
    private int reconnects;

    public Client(String ip, int port) {
        TCP_SERVER_PORT = port;
        IP_ADDRESS = ip;
        reconnects = 0;
        createConnection();
    }

    public void sendStopCommand() {
        out.println("STOP");
        
        try {
            Thread.sleep(200);
        } catch(Exception ex) {
            
        }
        //sendStatusCommand();
    }

    public void sendStartCommand() {
        out.println("START");
        
        try {
            Thread.sleep(200);
        } catch(Exception ex) {
            
        }		
        //sendStatusCommand();
    }

    public void sendStatusCommand() {
        out.println("STATUS");
    }

    public void createConnection() {
        //Create socket connection
        try{
            socket = new Socket(IP_ADDRESS, TCP_SERVER_PORT);          
            System.out.println("Socket Created");
            out = new PrintWriter(socket.getOutputStream(), true);

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

                while ((buffer=in.readLine())!=null) {
                    //System.out.println(buffer.toString());
                    if (buffer.toString().contains("NOTRECORDING")) {
                        CameraApp.isRecording(false);
                    } else if (buffer.toString().contains("RECORDING")) {
                        CameraApp.isRecording(true);
                    } else if (buffer.toString().contains("CONNECTED")) {
                        CameraApp.setConnected(true);
                    } else if (buffer.toString().contains("Succesful")) {
                        System.out.println("Server Ready");
                    } else if (buffer.toString().contains("RESUMED")) {
                        //isConnected(true);
                    } else if (buffer.toString().contains("MEMORY")) {
                        CameraApp.setMemoryLabel(buffer.toString());
                    } else {
                        System.out.println(buffer.toString());
                    }   
                }     

            } catch (IOException e) {
                try {
                    //socket.close();
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    };
}