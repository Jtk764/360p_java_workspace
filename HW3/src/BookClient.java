import java.util.Scanner;

import javax.print.attribute.standard.RequestingUserName;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
public class BookClient {
	

    static String hostAddress="localhost";
    static int port = BookServer.Port;
    static int clientId;
    static int mode=0;
    static int len = 1024;
    static byte[] rbuffer = new byte[len];
    static DatagramPacket rPacket;
    static DatagramSocket udpSocket;
    static Socket tcpSocket=null;
    static InetAddress ia;
    static File file;
	
  public static void main (String[] args) {
    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    //TO-DO: get a client socket from server first
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
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
        udpSocket = new DatagramSocket();
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
        	  endClient(cmd); 
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (Exception e) {
	e.printStackTrace();
    }
  }
  
  
  private static void setmode(String s){
	  if ( s.equals("U") && mode == 0 ){
		  DatagramPacket rPacket;

	  }
	  else if (s.equals("T") && mode == 1){
		  
	  }
	  else if (s.equals("T") && mode != 1){
		  
	  }
	  else if (s.equals("U") && mode != 0){
		  
	  }  
  }
  
  private static void borrow(String s){
	  if (mode == 0 ){

	  }
	  else {
		  
	  }

  }
  
  
  private static void list(String s){
	  if (mode == 0 ){

	  }
	  else {
		  
	  }

  }
  
  private static void unborrow(String s){
	  if (mode == 0 ){

	  }
	  else {
		  
	  }
  }
  
  private static void inventory (String s){
	  if (mode == 0 ){

	  }
	  else {
		  
	  }

  }


  
  
  
	  
	  private static void endClient (String s){
		  if (mode == 0 ){

		  }
		  else {
			  
		  }

	  }

  }
  
  
  
