

import java.util.Scanner;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
public class BookClient {
	

    static String hostAddress="localhost";
    static int port = 8000;
    static int clientId;
    static int mode=0;
    static int len = 1024;
    static byte[] rbuffer = new byte[len];
    static DatagramPacket rPacket;
    static DatagramSocket udpSocket;
    static Socket tcpSocket=null;
    static InetAddress ia;
    static File file;
    static Scanner din;
    static PrintStream pout;
    static LinkedList<String> print = new LinkedList<String>();
	
  public static void main (String[] args) {
    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    try {
    	ia = InetAddress.getByName(hostAddress);
		DatagramSocket datasocket = new DatagramSocket();
		byte[] buffer = new byte["first".length()];
		buffer = "first".getBytes();
		datasocket.send(new DatagramPacket(buffer, buffer.length, ia, port));
		rPacket = new DatagramPacket(rbuffer, rbuffer.length);
		datasocket.receive(rPacket);
		port = Integer.parseInt(new String(rPacket.getData(), 0,
		rPacket.getLength()));
		udpSocket=datasocket;
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          if (tokens[0].equals("setmode")) {
             setmode(cmd);
          }
          else if (tokens[0].equals("borrow")) {
        	  borrow(cmd);
          } else if (tokens[0].equals("return")) {
        	  unborrow(cmd);
          } else if (tokens[0].equals("inventory")) {
        	  inventory(cmd);
          } else if (tokens[0].equals("list")) {
        	  list(cmd);
          } else if (tokens[0].equals("exit")) {
        	  sc.close();
        	  endClient(cmd); 
        	  break;
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (Exception e) {
	e.printStackTrace();
    }
  }
  
  
  private static void setmode(String s) throws IOException{
	String[] tmp=s.split(" ");
	 if (tmp[tmp.length-1].equals("U") && mode != 0){
		 pout.println(s);
		 mode=0;
	  }  
	  else if (tmp[tmp.length-1].equals("T") && mode != 1){
		 
		  byte[] buffer = new byte[s.length()];
		  buffer = s.getBytes(); 
		  udpSocket.send(new DatagramPacket(buffer, buffer.length, ia, port));
		  udpSocket.receive(rPacket);
		  InetSocketAddress endpoint= new InetSocketAddress(hostAddress,Integer.parseInt(new String(rPacket.getData(), 0,
		rPacket.getLength())));
		  if (tcpSocket == null){
		  tcpSocket = new Socket();
		  tcpSocket.connect(endpoint);
		  din = new Scanner(tcpSocket.getInputStream());
		  pout = new PrintStream(tcpSocket.getOutputStream()); 
		  }
		  mode=1;
	  }
  }
  
  private static void borrow(String s) throws IOException{
	  if (mode == 0 ){
		  byte[] buffer = new byte[s.length()];
		  buffer = s.getBytes();
		  udpSocket.send(new DatagramPacket(buffer, buffer.length, ia, port));
		  udpSocket.receive(rPacket);
		  print.add(new String(rPacket.getData(), 0,
					rPacket.getLength()));
	  }
	  else {
		  pout.println(s);
		  pout.flush();
		  print.add(din.nextLine());
	  }

  }
  
  
  private static void list(String s) throws IOException{
	  if (mode == 0 ){
		  byte[] buffer = new byte[s.length()];
		  buffer = s.getBytes();
		  udpSocket.send(new DatagramPacket(buffer, buffer.length, ia, port));
		  while (true){
		  udpSocket.receive(rPacket);
		  String tmp=new String(rPacket.getData(), 0, rPacket.getLength());
		  if ( tmp.equals("done")) break;
		  print.add(tmp);
		  }
	  }
	  else {
		  pout.println(s);
		  pout.flush();
		  while (din.hasNextLine()){
		  String tmp=din.nextLine();
		  if ( tmp.equals("done")) break;
		  print.add(tmp);
		  }
	  }

  }
  
  private static void unborrow(String s) throws IOException{
	  if (mode == 0 ){
		  byte[] buffer = new byte[s.length()];
		  buffer = s.getBytes();
		  udpSocket.send(new DatagramPacket(buffer, buffer.length, ia, port));
		  udpSocket.receive(rPacket);
		  print.add(new String(rPacket.getData(), 0,
					rPacket.getLength()));
	  }
	  else {
		  pout.println(s);
		  pout.flush();
		  print.add(din.nextLine());
	  }

  }
  
  private static void inventory (String s) throws IOException{
	  if (mode == 0 ){
		  byte[] buffer = new byte[s.length()];
		  buffer = s.getBytes();
		  udpSocket.send(new DatagramPacket(buffer, buffer.length, ia, port));
		  while (true){
		  udpSocket.receive(rPacket);
		  String tmp=new String(rPacket.getData(), 0, rPacket.getLength());
		  if ( tmp.equals("done")) break;
		  print.add(tmp);
		  }
	  }
	  else {
		  pout.println(s);
		  pout.flush();
		  while (true){
		  String tmp=din.nextLine();
		  if ( tmp.equals("done")) break;
		  print.add(tmp);
		  }
	  }

  }


  
  
  
	  
	  private static void endClient (String s) throws IOException{
		  if (mode == 0 ){
			  byte[] buffer = new byte[s.length()];
			  buffer = s.getBytes();
			  udpSocket.send(new DatagramPacket(buffer, buffer.length, ia, port));
		  }
		  else {
			  pout.println(s);
			  pout.flush();
		  }
		  if (tcpSocket != null) tcpSocket.close();
		  udpSocket.close();
		  file = new File(String.format("out_"+clientId+".txt"));
		  if (file.exists()){ file.delete();}
		  file.createNewFile();
		  file.setWritable(true);
		  PrintStream fout = new PrintStream(file);
		  for (String out : print){
			  fout.println(out);
		  }
		  fout.close();
	}
	  
}