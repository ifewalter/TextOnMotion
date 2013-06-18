package com.ifewalter.android.textonmotion.textmanipulation;

import java.util.ArrayList;

public class ShortHand {

	private static String converted;
	// private static ArrayList<String> wordsInput = new ArrayList<String>();
	private static String[] dictionary = new String[]{"get", "gt", "become",
			"bcm", "hi"};

	private static ArrayList<String> testArray = new ArrayList<String>();

	public static String convertToShortHand(String longHandString) {
		initDictionary();

		String g;

		String[] wordsInput = longHandString.split(" ");
		for (String b : wordsInput) {
			g = mainConversion(b);
			converted = longHandString.replace(b, g);
		}
		return converted;
	}

	private static void initDictionary() {
		for (String a : dictionary) {
			testArray.add(a);
		}
	}

	private static String mainConversion(String wrongWordInput) {
		String converted = null;
		int wrongWordLocation = testArray.lastIndexOf(wrongWordInput);
		++wrongWordLocation;

		converted = testArray.get(wrongWordLocation);

		return converted;
	}
}
