package com.vicmns.bluetoothglass.client.tools;

public class FileExtensionFilter {
	
	private static String[] imageExtensions = {".png", ".jpeg", ".gif", ".jpg"};
	private static String[] videoExtensions = {".mp4", ".avi", ".mpeg", ".3GP"};
	
	public static enum MediaTypes {
		 IMAGE, VIDEO
	}
	
	public static boolean isFileImage(String fileName) {
		for(int i = 0; i < imageExtensions.length ; i++) {
			if(fileName.toLowerCase().endsWith(imageExtensions[i]))
				return true;
		}
		return false;
	}
	
	public static boolean isFileVideo(String fileName) {
		for(int i = 0; i < videoExtensions.length ; i++) {
			if(fileName.toLowerCase().endsWith(videoExtensions[i]))
				return true;
		}
		return false;
	}
	
	public static boolean filerFilesByType(String fileName, MediaTypes mediaType) {
		switch(mediaType) {
			case IMAGE:
				return isFileImage(fileName);
			case VIDEO:
				return isFileVideo(fileName);
			default:
				return false;
		}
	}
}
