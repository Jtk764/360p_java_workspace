//UT-EID=
//JTK764
//RM48763

import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  
	
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		assert A.length+B.length==C.length;
		ExecutorService pool = Executors.newFixedThreadPool(numThreads);
		int threadNum=numThreads;
		int totalsize=A.length+B.length;
		if ( numThreads > totalsize ) threadNum = totalsize ;
		int sizePerTask=(totalsize+threadNum-1)/threadNum;
		int lastsize=totalsize%sizePerTask;
		if (lastsize==0)lastsize=sizePerTask;
		for (int i = 0; i < threadNum-1; i++) {
			pool.execute(new PMergeTask(i*sizePerTask, sizePerTask, A, B, C));
		}
		pool.execute(new PMergeTask((threadNum-1)*sizePerTask, lastsize, A, B, C));
		pool.shutdown();
		try {
			  pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				
			}
		PSort.parallelSort(C,0, C.length);
  }


  

  
  
}
