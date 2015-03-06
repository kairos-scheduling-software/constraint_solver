package util;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class IO {
	
	public static String readInput(Scanner sc, boolean readAll) {
//		Scanner sc = new Scanner(is);
		
		String line = "";
		StringBuilder sb = new StringBuilder();
		try {
			do {
				sb.append(line);
				line = sc.nextLine();
			} while (readAll || line.length() > 0);
		} catch (NoSuchElementException e) {}
//		sc.close();
		
		return sb.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
