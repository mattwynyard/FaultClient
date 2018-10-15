import java.net.Socket;
import java.net.SocketException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.net.UnknownHostException;

public class ServerPinger {

    private static final int TCP_SERVER_PORT = 38300;
    private boolean flag;
    private boolean connected = false;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public ServerPinger() {
        //this.socket = socket;
    }

    public Runnable pingServer = new Runnable() {
        @Override
        public void run() {
            try {
                socket = new Socket("localhost",TCP_SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                String buffer;
                System.out.println("Pinging Server"); 
                
                out.println("PING\n");
                buffer=in.readLine();
                System.out.println(buffer.toString());
                try {
                    Thread.sleep(2000);
                    while(flag == false) {

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

    public void stopPing() {
        this.flag = false;
    }

    public boolean isConnected() {
        return this.connected;
    }
}