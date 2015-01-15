package tutorial;

public class Converter {

	Converter(){}
	
	/**
	 * @param day must be in {'M','m','T','t','W','w','H','h','F','f','S','s','U','u'}
	 * @param time must be formatted "HH:MM"
	 * @return the number of minutes elapsed from Monday at 00:00 until the time represented by parameters
	 */
	private int convertDayTimeToMinutes(char day, String time){
		int minsPerDay=1440, minsPerHour=60, dayMultiplier;
		
		switch(Character.toUpperCase(day)){
			case 'M': dayMultiplier = 0; break;
			case 'T': dayMultiplier = 1; break;
			case 'W': dayMultiplier = 2; break;
			case 'H': dayMultiplier = 3; break;
			case 'F': dayMultiplier = 4; break;
			case 'S': dayMultiplier = 5; break;
			case 'U': dayMultiplier = 6; break;
			default : dayMultiplier = -1; /* something is wrong */
		}
		
		String[] hoursAndMins = time.split(":");
		return (minsPerDay*dayMultiplier) + (minsPerHour*Integer.parseInt(hoursAndMins[0])) + Integer.parseInt(hoursAndMins[1]);
	}
	
	
	public int[] convertDayTimeToMinutes(String[] times){
		int i;
		
		int minutes[] = new int[times.length];
		for(i=0; i<times.length; ++i)
			minutes[i] = convertDayTimeToMinutes(times[i].charAt(0), times[i].substring(1));
		return minutes;
	}
}
