package com.customprogrammingsolutions.utils;

public class DateTimeUtils {
	public static String getDescriptiveTimeString(long duration) {
		return getDescriptiveTimeString(duration, false);
	}
	
	public static String getDescriptiveTimeString(long duration, boolean truncate) {
		String second = truncate ? "Sec" : "Second";
		String minute = truncate ? "Min" : "Minute";
		String hour = truncate ? "H" : "Hour";
		String day = truncate ? "D" : "Day";
		
		//milliseconds / 1000 -> seconds / 60 -> minutes / 60 -> hours / 24 -> days
		long days = ((((duration / 1000) / 60) / 60) / 24); 
						
		//milliseconds / 1000 -> seconds / 60 -> minutes / 60  - (the hours stored in days) -> hours
		long hours = (((duration / 1000) / 60) / 60) - (days * 24); 
						
		//milliseconds / 1000 -> seconds / 60 - ( (the minutes stored in days) + (the minutes stored in hours) ) -> minutes
		long minutes = ((duration / 1000) / 60) - ( ((days * 24) * 60) + (hours * 60) ); 
						
		//milliseconds / 1000 - ( (the seconds stored in days) + (the seconds stored in hours) + (the seconds stored in minutes) ) -> seconds
		long seconds = duration / 1000 - ( (((days * 24) * 60) * 60) + ((hours * 60) * 60) + (minutes * 60) ); 
						
		String secondsString = seconds == 1 ? "1 " + second : seconds < 1 ? "" : seconds + " " + second + "s";
						
		String minutesString = minutes == 1 ? "1 " + minute : minutes < 1 ? "" : minutes + " " + minute + "s";
						
		String hoursString = hours == 1 ? "1 " + hour : hours < 1 ? "" : hours + " " + hour + "s";
						
		String daysString = days == 1 ? "1 " + day : days < 1 ? "" : days + " " + day + "s ";
				
		if(duration < 60)
			return secondsString.trim();
				
		else if(duration < (60 * 60))
			return (minutesString + " " + secondsString).trim();
				
		else if(duration >= (60 * 60))
			return (daysString + hoursString + " " + minutesString + " " + secondsString).trim();		
				
		return "XX:XX:XX";
	}
}
