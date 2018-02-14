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
	



    int partition(int a[], int s, int e)
    {
        int p = a[e]; 
        int i = (s-1); // index of smaller element
        for (int j=s; j<e; j++)
        {
            // If current element is smaller than or
            // equal to pivot
            if (a[j] <= p)
            {
                i++;
 
                // swap arr[i] and arr[j]
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
            }
        }
 
        // swap arr[i+1] and arr[high] (or pivot)
        int temp = a[i+1];
        a[i+1] = a[e];
        a[e] = temp;
 
        return i+1;
    }

	

	@Override
	protected void compute() {
		if (end-start >= 16); 
		else if(start < end){
			splitTasks();
		}
		
	}
}
