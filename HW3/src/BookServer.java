import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServer {
	
	final static int Port = 8000;
	public static AtomicInteger  requestId = new AtomicInteger(1);
	private static ArrayList<Student> students;
	public static ArrayList<BookCollection> inventory;
	private static DatagramSocket listener;
	private static ForkJoinPool pool;
    static int len = 1024;
    static byte[] rbuffer = new byte[len];
    static DatagramPacket rPacket;
    static String hostAddress="localhost";
    static InetAddress ia;
	
  public static void main (String[] args) {
	  try {
		listener=new DatagramSocket(Port);
		ia = InetAddress.getByName(hostAddress);
	} catch (Exception e) {
		e.printStackTrace();
	}
	  pool = new ForkJoinPool();
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    parseInventory(fileName);
    while (true){
    	rPacket=rPacket = new DatagramPacket(rbuffer, rbuffer.length);
    	try {
			listener.receive(rPacket);
			String s = new String(rPacket.getData(), 0, rPacket.getLength());
			if (s.equals("first")){
				serverThread thread = new serverThread();
				byte[] buffer = new byte[Integer.toString(thread.port).length()];
				buffer = Integer.toString(thread.port).getBytes();
				listener.send(new DatagramPacket(buffer, buffer.length, ia, rPacket.getPort()));
				pool.submit(thread);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

  }
  
  
  
  
  public static synchronized String checkout (String s){
	  String[] tmp=s.split(" ");
	  Book b=null;
	  Pattern MY_PATTERN = Pattern.compile("*");
	  Matcher m;
	  m= MY_PATTERN.matcher(s);
	  for (BookCollection bc : inventory){
		  if (bc.name.equals(m.group(1))) { 
			 b=bc.checkoutBook(requestId.get());
			 if(b==null) return "Request Failed - Book not available";
			 break;
		  }
	  }
	  if (b == null) return "Request Failed - We do not have this book";
	  requestId.incrementAndGet();
	  Student student=null;
	  for (Student s2 : students){
		  if (s2.name.equals(tmp[tmp.length-1])) {student=s2; break; }
	  }
	  if (student == null){ student = new Student(tmp[tmp.length-1]);}
	  student.addBook(b);
	  return String.format("Your request has been approved, "+b.getId()+" "+student.name+" "+b.getName());
  }
  
  public static synchronized String returnCheckout (String s){
	  String[] tmp= s.split(" ");
	  int id = Integer.parseInt(tmp[tmp.length-1]);
	  Book b=null;
	  for (Student student : students){
		  b=student.getBook(id);
		  if (b != null) {
			  student.removeBook(b);
			  if (student.isEmpty()) students.remove(student);
			  break;
		  }
	  }
	  if (b == null) return String.format(id+" not found, no such borrow record");
	  for (BookCollection bc : inventory){
		  if (bc.name.equals(b.getName())){ bc.returnBook(id); break;}
	  }
	  return String.format(id+" is returned");
  }
  
  public static synchronized ArrayList<String> listStudent (String s){
	  String[] tmp= s.split(" ");
	  ArrayList<String> r = null;
	  for(Student student : students){
		  if (student.name.equals(tmp[1])){
			r = student.list();
			break;
		  }
	  }
	  return r;
  }
  
  public static synchronized ArrayList<String> listInventory (){
	  ArrayList<String> r = new ArrayList<String>();
	  for(BookCollection bc : inventory){
		  r.add(bc.toString());
	  }
	  r.add("done");
	  return r;
  }
  
  public static synchronized void printFile () throws IOException{
	  File file = new File(String.format("inventory.txt"));
	  if (file.exists()){ file.delete();}
	  file.createNewFile();
	  file.setWritable(true);
	  PrintStream fout = new PrintStream(file);
	  for (BookCollection bc: inventory){
		  fout.println(bc.toString());
	  }
	  fout.close();
  }
  
  private static void parseInventory(String fileName){
	  File  file = new File(fileName);
	  Pattern MY_PATTERN = Pattern.compile("*");
	  Matcher m;
	  try {
		Scanner sc = new Scanner(file);
		 while (sc.hasNextLine()) {
			 String tmp = sc.nextLine();
			 m= MY_PATTERN.matcher(tmp);
			 String [] split = tmp.split(" ");
			 inventory.add(new BookCollection(m.group(1), Integer.parseInt(split[split.length-1])));
		 }
		 sc.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	  
  }
  
  
  
  
}