package com.yoocent.mtp.common;

public class StringUtil {

	public static boolean isBlankOrNull(String value){
		
		return value == null || value.length() == 0;
	}
}