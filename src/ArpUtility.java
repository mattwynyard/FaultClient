import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
class ArpUtility {

    String out;  
    BufferedReader in;
    ProcessBuilder pb;
    Process p;
     // Create operating system process from arpe.bat file command



    public ArpUtility() {

        // Create operating system process from arpe.bat file command
        try {
            pb = new ProcessBuilder("arpe.bat"); 
            System.out.println("arp started");
            p = pb.start();	
        } catch (IOException e) {
            e.printStackTrace();
        }			
                
        in = new BufferedReader(new InputStreamReader(p.getInputStream()));

    }   

    public String getIPAddress() {
        try {
            String buffer;
            while ((buffer=in.readLine())!=null) {
                if (buffer.contains("192.168.173") && buffer.contains("dynamic")) {
                    //System.out.println(buffer.toString());
                    String[] arr = buffer.split(" ");
                    for (String i : arr) {
                        if (i.contains("192.168")) {
                            return i;
                        }
                    }
                }   
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}