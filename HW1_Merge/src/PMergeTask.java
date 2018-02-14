import java.util.Arrays;

//UT-EID=
//JTK764
//RM48763

public class PMergeTask implements Runnable{
	private int start; //start location in source1
	private int end; //length of the section this task is responsible for
	private static int[] source1; //1st source
	private static int[] source2; //2nd source
	private static int[] destination; //destination
	
	public PMergeTask(int start, int end, int[] s1, int[] s2, int[] dest ){
		this.start=start;
		this.end = end;
		this.source1 = s1;
		this.source2 = s2;
		this.destination=dest;
	}
	
	public PMergeTask(int start, int end){
		this.start=start;
		this.end = end;
	}
	

	@Override
	public void run() {
		for (int i = start; i < end; i++){
			if(i >= source1.length) insert(source2, source1, i-source1.length);
			else insert(source1, source2, i);
		}
		}
	
	private void insert(int[] key_holder, int[] arr, int key_index){
		int tmp=Arrays.binarySearch(arr, key_holder[key_index]);
		if (tmp >= 0) {
			destination[Math.abs(tmp)+key_index]=key_holder[key_index];
			destination[Math.abs(tmp)+key_index+1]=key_holder[key_index];
		}
		else destination[(Math.abs(tmp))+key_index-1]=key_holder[key_index];    	
		
	}

}
