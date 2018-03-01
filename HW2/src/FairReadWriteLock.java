import java.util.LinkedList;



public class FairReadWriteLock {
	LinkedList<Integer> writers= new LinkedList<Integer>();
	LinkedList<Integer> readers= new LinkedList<Integer>();
	private ThreadLocal<Integer> myThreadLocal = new ThreadLocal<Integer>();
	static int index=0;
                        
	public synchronized void beginRead() {
		index++;
		myThreadLocal.set(index);
		readers.addLast(myThreadLocal.get());
		while( writers.peekLast() != null && 
				myThreadLocal.get() >  writers.peekFirst()){
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
		index++;
		myThreadLocal.set(index);
		writers.addLast(myThreadLocal.get());
		while( ( readers.peekFirst() != null && 
				myThreadLocal.get() > (readers.peekFirst()) ) || 
				!((writers.peekFirst())==myThreadLocal.get())){
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
