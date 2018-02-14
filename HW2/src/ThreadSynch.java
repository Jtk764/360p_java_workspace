/*
 * EID's of group members
 * 
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSynch {
	final Semaphore latch;
	int threads;
	final Semaphore mutex;
	final Semaphore cond;
	static AtomicInteger index;
	
	public ThreadSynch(int parties) {
		 latch= new Semaphore(0);
		threads=parties;
		mutex=new Semaphore(1);
		cond=new Semaphore(0);
		index=new AtomicInteger();
	}
	
	public int await() throws InterruptedException {
		mutex.acquire();
		int index2;
           index2=index.getAndIncrement();
           if (index.get()== threads){
        	   latch.release(threads-1);
        	   cond.acquire();
        	   index.getAndDecrement();
        	   int tmp=latch.availablePermits();
               mutex.release();
           }
           else {
               mutex.release();
        	   latch.acquireUninterruptibly();
        	   if (index.getAndDecrement()==2) {
        		   cond.release();}
           }
	    return index2;
	}
}
