import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {
	
	final static int udpPort = 8000;
	final static int tcpPort = 7000;
	public static AtomicInteger  requestId = new AtomicInteger(0);
	public static ArrayList<Student> students;
	public static ArrayList<BookCollection> inventory;
	
	
  public static void main (String[] args) {
	  
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];

    // parse the inventory file

    // TODO: handle request from clients
  }
  
  
  public synchronized static void addStudent(Student s){}
  
  public synchronized static void removeStudent(Student s){}
  
  public synchronized static void getStudent(Student s){}
  
  
  
}