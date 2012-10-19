package com.customprogrammingsolutions.cpsolutions.utils;

public class DateTimeUtils {
	public static String getDescriptiveTimeString(long duration) {
		//milliseconds / 1000 -> seconds / 60 -> minutes / 60 -> hours / 24 -> days
		long days = ((((duration / 1000) / 60) / 60) / 24); 
				
		//milliseconds / 1000 -> seconds / 60 -> minutes / 60  - (the hours stored in days) -> hours
		long hours = (((duration / 1000) / 60) / 60) - (days * 24); 
				
		//milliseconds / 1000 -> seconds / 60 - ( (the minutes stored in days) + (the minutes stored in hours) ) -> minutes
		long minutes = ((duration / 1000) / 60) - ( ((days * 24) * 60) + (hours * 60) ); 
				
		//milliseconds / 1000 - ( (the seconds stored in days) + (the seconds stored in hours) + (the seconds stored in minutes) ) -> seconds
		long seconds = duration / 1000 - ( (((days * 24) * 60) * 60) + ((hours * 60) * 60) + (minutes * 60) ); 
				
		String secondsString = seconds == 1 ? "1 Second" : seconds < 1 ? "" : seconds + " Seconds";
				
		String minutesString = minutes == 1 ? "1 Minute" : minutes < 1 ? "" : minutes + " Minutes";
				
		String hoursString = hours == 1 ? "1 Hour" : hours < 1 ? "" : hours + " Hours";
				
		String daysString = days == 1 ? "1 Day " : days < 1 ? "" : days + " Days ";
		
		if(duration < 60)
			return secondsString.trim();
		
		else if(duration < (60 * 60))
			return (minutesString + " " + secondsString).trim();
		
		else if(duration >= (60 * 60))
			return (daysString + hoursString + " " + minutesString + " " + secondsString).trim();		
		
		return "XX:XX:XX";
	}
}
