//UT-EID=


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  
	
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		assert A.length+B.length==C.length;
		ExecutorService pool = Executors.newFixedThreadPool(numThreads);
		int totalsize=A.length+B.length;
		int sizePerTask=totalsize/numThreads;
		int lastsize=totalsize%numThreads;
		if (lastsize==0)lastsize=sizePerTask;
		for (int i = 0; i < numThreads-1; i++) {
			pool.execute(new PMergeTask(i*sizePerTask, sizePerTask, A, B, C));
		}
		pool.execute(new PMergeTask((numThreads-1)*sizePerTask, lastsize, A, B, C));
		pool.shutdown();
		try {
			  pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				
			}
		PSort.parallelSort(C,0, C.length);
  }


  

  
  
}