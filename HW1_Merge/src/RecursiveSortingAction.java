//UT-EID=
//JTK764
//RM48763

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class RecursiveSortingAction extends RecursiveAction {
	
	static private int[] arr;
	int start;
	int end;
	private final int thresh = 16;
	private ForkJoinPool pool; 
	
	public RecursiveSortingAction(int[] a, int s, int e, ForkJoinPool p){
		arr = a;
		start = s;
		end = e;
		pool = p;
	}
	
	
	private void splitTasks() {
		int pivot = partition(arr,start,end);
		RecursiveSortingAction t1=new RecursiveSortingAction(arr, start, pivot-1, pool);
		RecursiveSortingAction t2=new RecursiveSortingAction(arr, pivot+1, end, pool);
		pool.submit(t1);
		pool.submit(t2);
		t1.join();
		t2.join();
	}
	



    int partition(int arr[], int low, int high)
    {
        int pivot = arr[high]; 
        int i = (low-1); // index of smaller element
        for (int j=low; j<high; j++)
        {
            // If current element is smaller than or
            // equal to pivot
            if (arr[j] <= pivot)
            {
                i++;
 
                // swap arr[i] and arr[j]
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
 
        // swap arr[i+1] and arr[high] (or pivot)
        int temp = arr[i+1];
        arr[i+1] = arr[high];
        arr[high] = temp;
 
        return i+1;
    }

	

	@Override
	protected void compute() {
		if (end-start >= 16)Arrays.sort(arr);
		else if(start < end){
			splitTasks();
		}
		
	}
}
