//UT-EID=
//JTK764
//RM48763


import java.util.*;
import java.util.concurrent.*;

public class PSort{
  public static void parallelSort(int[] A, int begin, int end){
	  ForkJoinPool pool = new ForkJoinPool();
	  RecursiveSortingAction sorter = new RecursiveSortingAction(A, begin, end-1, pool);
	  pool.execute(sorter);
	  sorter.join();
  }
}