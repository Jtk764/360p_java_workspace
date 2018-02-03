

public class PMergeTask implements Runnable{
	private int start1; //start location in source1
	private int length1; //length of the section this task is responsible for
	private int length2;
	private int start2; //start location in destination
	private int[] source1; //1st source
	private int[] source2; //2nd source
	private int[] destination; //destination
	
	public PMergeTask(int start, int l, int[] s1, int[] s2, int[] dest ){
		start2=start;
		destination=dest;
		if (start >= s1.length ){
			source1=s2;
			start1=start%s1.length;
			length1=l;
			length2=0;
		} else if (start < s1.length && start+l >= s1.length ){
			source1=s1;
			source2=s2;
			start1=start;
			length1=s1.length-start;
			length2=l-length1;
		}
		else {
			source1=s1;
			start1=start;
			length1=l;
			
		}
	}
	

	@Override
	public void run() {
		if (length2 == 0){
			for (int i=0; i<length1; i++){
				destination[start2+i]=source1[start1+i];
			}	
		}
		else{
			for (int i=0; i<length1; i++){
				destination[start2]=source1[start1+i];
				start2++;
			}
			for (int i=0; i<length2; i++){
				destination[start2]=source2[i];
				start2++;
			}
		}
		}
	

}
