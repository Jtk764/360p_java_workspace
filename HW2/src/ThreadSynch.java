/*
 * EID's of group members
 * 
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class ThreadSynch {
	final Semaphore latch;
	int threads;
	final Semaphore mutex;
	final Semaphore cond;
	
	public ThreadSynch(int parties) {
		 latch= new Semaphore(0);
		threads=parties;
		mutex=new Semaphore(1);
		cond=new Semaphore(0);
	}
	
	public int await() throws InterruptedException {
		mutex.acquire();
		int index;
           index=latch.getQueueLength();
           if (index== threads-1){
        	   latch.release(threads-1);
        	   cond.acquire();
               mutex.release();
           }
           else {
               mutex.release();
        	   latch.acquireUninterruptibly();
        	   if (!latch.hasQueuedThreads()) {
        		   cond.release();}
           }
	    return index;
	}
}