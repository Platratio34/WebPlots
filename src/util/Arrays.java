package util;

public class Arrays {
	
	public static String printArray(Object[] arr) {
		return printArray(arr,",",0,arr.length-1);
	}
	public static String printArray(Object[] arr, String seperator) {
		return printArray(arr,seperator,0,arr.length-1);
	}
	public static String printArray(Object[] arr, int stop) {
		return printArray(arr,",",0,stop);
	}
	public static String printArray(Object[] arr, String seperator, int stop) {
		return printArray(arr,seperator,0,stop);
	}
	public static String printArray(Object[] arr, int start, int stop) {
		return printArray(arr,",",start,stop);
	}
	public static String printArray(Object[] arr, String seperator, int start, int stop) {
		String str = "";
		for(int i = start; i <= stop; i++) {
			if(i>start) {
				str += seperator;
			}
			str += arr[i];
		}
		return str;
	}
}
