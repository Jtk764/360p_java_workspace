import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;



public class FairReadWriteLock {
	LinkedList<Timestamp> writers= new LinkedList<Timestamp>();
	LinkedList<Timestamp> readers= new LinkedList<Timestamp>();
	private ThreadLocal<Timestamp> myThreadLocal = new ThreadLocal<Timestamp>();
                        
	public synchronized void beginRead() {
		myThreadLocal.set(new Timestamp((new Date()).getTime()));
		readers.addLast(myThreadLocal.get());
		while( writers.peekLast() != null && 
				myThreadLocal.get().after(writers.peekFirst())){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public synchronized void endRead() {
		readers.remove(myThreadLocal.get());
		 notifyAll();
	}
	
	public synchronized void beginWrite() {
		myThreadLocal.set(new Timestamp((new Date()).getTime()));
		writers.addLast(myThreadLocal.get());
		while( ( readers.peekFirst() != null && 
				myThreadLocal.get().after(readers.peekFirst()) ) || 
				!(writers.peekFirst()).equals(myThreadLocal.get())){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public synchronized void endWrite() {
		writers.remove(myThreadLocal.get());
		 notifyAll();
	}
}
