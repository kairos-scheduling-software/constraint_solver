package scheduleSolver;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Utils {
	
	public static String readInput(InputStream is) {
		Scanner sc = new Scanner(is);
		
		String line = "";
		StringBuilder sb = new StringBuilder();
		try {
			do {
				sb.append(line);
			} while ((line = sc.nextLine()).length() > 0);
		} catch (NoSuchElementException e) {}
		sc.close();
		
		return sb.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
