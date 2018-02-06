//UT-EID=
//JTK764
//RM48763

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class RecursiveSortingAction extends RecursiveAction{
	
	private int[] arr;
	int start;
	int end;
	private final int thresh = 16;
	
	public RecursiveSortingAction(int[] a, int s, int e){
		arr = a;
		start = s;
		end = e;
	}
	
	@Override
	protected void compute() {
		if(arr.length > thresh){
			ForkJoinTask.invokeAll(splitTasks());
		}
		else{
			sort();
		}
	}
	
	private List<RecursiveSortingAction> splitTasks() {
		List<RecursiveSortingAction> subs = new ArrayList<>();
		int half = start + (start-end)/2;
		subs.add(new RecursiveSortingAction(arr, start, half));
		subs.add(new RecursiveSortingAction(arr, half, end));
		return subs;
	}
	
	private void sort(){
		for(int ind = start; ind < end; ind++){
			int j = ind;
			if(j == start){
				continue;
			}
			while(j > start){
				if(arr[j] < arr[j-1]){
					int temp = arr[j];
					arr[j] = arr[j-1];
					arr[j-1] = temp;
					j--;
				}
				else break;
			}
		}
	}
}
