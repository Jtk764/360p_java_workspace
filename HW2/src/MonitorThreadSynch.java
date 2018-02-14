public class MonitorThreadSynch {
	int threads;
	int waiting;
	
	public MonitorThreadSynch(int parties) {
		threads = parties;
		waiting = 0;
	}
	
	public synchronized int await() throws InterruptedException {
           if(waiting == threads-1) {
        	   waiting = 0;
        	   notifyAll();
        	   return threads;
           }
           else {
        	   waiting++;
        	   wait();
           }
		
          // you need to write this code
	    return waiting;
	}
}