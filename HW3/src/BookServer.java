import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServer {
	
	final static int Port = 8000;
	public static AtomicInteger  requestId = new AtomicInteger(0);
	private static ArrayList<Student> students;
	public static ArrayList<BookCollection> inventory;
	private static DatagramSocket listener;
	
	
  public static void main (String[] args) {
	  try {
		listener=new DatagramSocket(Port);
	} catch (SocketException e) {
		e.printStackTrace();
	}
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    parseInventory(fileName);
    while (true){
    	
    }

  }
  
  
  public synchronized static void addStudent(Student s){
	  students.add(s);
  }
  
  public synchronized static void removeStudent(Student s){
	  students.remove(s);
  }
  
  public synchronized static Student getStudent(String s){
	  for ( Student i : students){
		  if (i.name.equals(s)){
			  return i;
		  }
	  }
	  return null;
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
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	  
  }
  
  
}