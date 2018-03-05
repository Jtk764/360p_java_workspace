import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class serverThread implements Runnable{
	
	private int mode;
	private ServerSocket tcpServer;
	private Socket tcpClient;
	private DatagramSocket udpClient;
	public int port;
	int len = 1024;
    byte[] rbuffer = new byte[len];
    DatagramPacket rPacket;
    String hostAddress="localhost";
    InetAddress ia;
    static Scanner din;
    static PrintStream pout;
	
	 public serverThread() {
		try {
			udpClient = new DatagramSocket();
			port = udpClient.getLocalPort();
			rPacket= new DatagramPacket(rbuffer, len);
			ia = InetAddress.getByName(hostAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mode=0;
		
	}

	@Override
	public void run() {
		String cmd;
		while (true) {
			try {
				if (mode==0){
				udpClient.receive(rPacket);
				cmd=new String(rPacket.getData(), 0,rPacket.getLength());
				String[] tokens = cmd.split(" ");
		          if (tokens[0].equals("setmode")) {
		        	  mode=1;
		        	  tcpServer=new ServerSocket(0);
		        	  String s = Integer.toString(tcpServer.getLocalPort());
		        	  byte[] buffer = new byte[s.length()];
		        	  buffer = s.getBytes();
		        	  udpClient.send(new DatagramPacket(buffer, buffer.length, ia, rPacket.getPort()));
		        	  tcpClient=tcpServer.accept();
		        	  din = new Scanner(tcpClient.getInputStream());
		    		  pout = new PrintStream(tcpClient.getOutputStream());
		          }
		          else if (tokens[0].equals("borrow")) {
		        	  String s = BookServer.checkout(cmd);
		        	  byte[] buffer = new byte[s.length()];
		        	  buffer = s.getBytes();
		        	  udpClient.send(new DatagramPacket(buffer, buffer.length, ia, rPacket.getPort()));
		          }
		          else if (tokens[0].equals("return")) {
		        	  String s = BookServer.returnCheckout(cmd);
		        	  byte[] buffer = new byte[s.length()];
		        	  buffer = s.getBytes();
		        	  udpClient.send(new DatagramPacket(buffer, buffer.length, ia, rPacket.getPort()));  

		          } 
		          else if (tokens[0].equals("inventory")) {
		        	  ArrayList<String> arr = BookServer.listInventory();
		        	  for (String s : arr){
		        		  byte[] buffer = new byte[s.length()];
			        	  buffer = s.getBytes();
			        	  udpClient.send(new DatagramPacket(buffer, buffer.length, ia, rPacket.getPort()));
		        	  }

		          } 
		          else if (tokens[0].equals("list")) {
		        	  ArrayList<String> arr = BookServer.listStudent(cmd);
		        	  if ( arr == null){
		        		  String s = String.format("No record found for "+tokens[1]);
		        		  byte[] buffer = new byte[s.length()];
			        	  buffer = s.getBytes();
			        	  udpClient.send(new DatagramPacket(buffer, buffer.length, ia, rPacket.getPort()));
			        	  continue;
		        	  }
		        	  arr.add("done");
		        	  for (String s : arr){
		        		  byte[] buffer = new byte[s.length()];
			        	  buffer = s.getBytes();
			        	  udpClient.send(new DatagramPacket(buffer, buffer.length, ia, rPacket.getPort()));
		        	  }
		          } 
		          else if (tokens[0].equals("exit")) {
		        	  if (tcpServer != null){ tcpServer.close(); tcpClient.close();}
		        	  udpClient.close();
		        	  BookServer.printFile();
		        	  break;
		          }
		         }
				else {
					mode=0;
					 cmd=din.nextLine();
					 String[] tokens = cmd.split(" ");
			          if (tokens[0].equals("setmode")) {
			        	  mode=0;
			        	  pout.flush();
			          }
			          else if (tokens[0].equals("borrow")) {
			        	  String s = BookServer.checkout(cmd);
			        	  pout.println(s);
			        	  pout.flush();
			          }
			          else if (tokens[0].equals("return")) {
			        	  String s = BookServer.returnCheckout(cmd);
			        	  pout.println(s);
			        	  pout.flush(); 

			          } 
			          else if (tokens[0].equals("inventory")) {
			        	  ArrayList<String> arr = BookServer.listInventory();
			        	  for (String s : arr){
			        		  pout.println(s);
			        	  }
			        	  pout.flush();
			          } 
			          else if (tokens[0].equals("list")) {
			        	  ArrayList<String> arr = BookServer.listStudent(cmd);
			        	  if ( arr == null){
			        		  String s = String.format("No record found for "+tokens[1]);
				        	  pout.println(s);
				        	  pout.flush(); 
				        	  continue;
			        	  }
			        	  arr.add("done");
			        	  for (String s : arr){
				        	  pout.println(s);
				        	  
			        	  }
			        	  pout.flush(); 
			          } 
			          else if (tokens[0].equals("exit")) {
			        	  if (tcpServer != null){ tcpServer.close(); tcpClient.close();}
			        	  udpClient.close();
			        	  BookServer.printFile();
			        	  break;
			          }
			         
		        	  
		        }
				
			} catch (IOException e) {

			e.printStackTrace();
			}
		
		}
	}

}
