//UT-EID=
//JTK764
//RM48763


import java.util.*;
import java.util.concurrent.*;

public class PSort{
  public static void parallelSort(int[] A, int begin, int end){
	  RecursiveSortingAction sorter = new RecursiveSortingAction(A, begin, end);
	  ForkJoinPool pool = new ForkJoinPool();
	  pool.invoke(sorter);
  }
}