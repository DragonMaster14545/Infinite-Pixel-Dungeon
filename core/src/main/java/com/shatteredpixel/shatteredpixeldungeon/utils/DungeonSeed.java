/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.watabou.utils.Random;

import java.util.Locale;

//This class defines the parameters for seeds in ShatteredPD and contains a few convenience methods
public class DungeonSeed {

	public static long TOTAL_SEEDS = 5429503678976L; //larges possible seed has a value of 26^9
	// we extend the total possible seeds to 12 digits, so we have 26^12 seeds total now
	public static final long EXPANSION = 1_000_000_000_000_000L;
	public static final long EXPANSION_SEEDS = TOTAL_SEEDS * 17576L;

	//Seed codes take the form @@@-@@@-@@@ where @ is any letter from A to Z (only uppercase)
	//This is effectively a base-26 number system, therefore 26^9 unique seeds are possible.

	//Seed codes exist to make sharing and inputting seeds easier
	//ZZZ-ZZZ-ZZZ is much easier to enter and share than 5,429,503,678,975

	//generates a random seed, omitting seeds that contain vowels (to minimize real words appearing randomly)
	//This means that there are 21^9 = 794,280,046,581 unique seeds that can be randomly generated
	public static long randomSeed(){
		Long seed;
		String seedText;
		do {
			seed = Random.Long(TOTAL_SEEDS);
			seedText = convertToCode(seed);
		} while (seedText.contains("A") || seedText.contains("E") || seedText.contains("I") || seedText.contains("O") || seedText.contains("U"));
		return seed;
	}

	//Takes a seed code (9 or 12 letters, dashes optional) and converts it to the equivalent long value
	public static long convertFromCode( String code ){
		//if code is formatted properly, force uppercase
		if ((code.length() == 11 && code.charAt(3) == '-' && code.charAt(7) == '-')
				|| (code.length() == 15 && code.charAt(3) == '-' && code.charAt(7) == '-' && code.charAt(11) == '-')){
			code = code.toUpperCase(Locale.ROOT);
		}

		//ignore whitespace characters and dashes
		code = code.replaceAll("[-\\s]", "");

		if (code.length() != 9 && code.length() != 12) {
			throw new IllegalArgumentException("codes must be 9 or 12 A-Z characters.");
		}

		long result = 0;
		for (int i = 0; i < code.length(); i++) {
			char c = code.charAt(i);
			if (c > 'Z' || c < 'A')
				throw new IllegalArgumentException("codes must be A-Z characters.");
			result = result * 26 + (c - 'A');
		}
		return code.length() == 12 ? EXPANSION + result : result;
	}

	//Takes a long value and converts it to the equivalent seed code
	public static String convertToCode( long seed ){
		if (seed >= 0 && seed < TOTAL_SEEDS) {
			return encode(seed, 9);
		} else if (seed >= EXPANSION && seed < EXPANSION + EXPANSION_SEEDS) {
			return encode(seed - EXPANSION, 12);
		}
		throw new IllegalArgumentException("seed is outside every valid seed range");
	}

	private static String encode( long value, int length ){
		char[] letters = new char[length];
		for (int i = length - 1; i >= 0; i--) {
			letters[i] = (char) ('A' + value % 26);
			value /= 26;
		}
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i > 0 && i % 3 == 0) result.append('-');
			result.append(letters[i]);
		}
		return result.toString();
	}

	//same rule as randomSeed(), but picks from the extended range
	public static long randomExtendedSeed(){
		long seed;
		String seedText;
		do {
			seed = EXPANSION + Random.Long(EXPANSION_SEEDS);
			seedText = convertToCode(seed);
		} while (seedText.contains("A") || seedText.contains("E") || seedText.contains("I") || seedText.contains("O") || seedText.contains("U"));
		return seed;
	}

	//Creates a seed from arbitrary user text input
	public static long convertFromText( String inputText ){
		if (inputText.isEmpty()) return -1;

		//First see if input is a seed code, use that format if it is
		try {
			return convertFromCode(inputText);
		} catch (IllegalArgumentException e){

		}

		//Then see if input is a number (ignoring spaces), if so parse as a long seed (with overflow)
		try {
			return Long.parseLong(inputText.replaceAll("\\s", "")) % TOTAL_SEEDS;
		} catch (NumberFormatException e){

		}

		//Finally, if the user has entered unformatted text, convert it to a long seed equivalent
		// This is basically the same as string.hashcode except with long, and overflow
		// this lets the user input 'fun' seeds, like names or places
		long total = 0;
		for (char c : inputText.toCharArray()){
			total = 31 * total + c;
		}
		if (total < 0) total += Long.MAX_VALUE;
		total %= TOTAL_SEEDS;
		return total;
	}


	public static String formatText( String inputText ){
		try {
			//if the seed matches a code, then just convert it to using the code system
			return convertToCode(convertFromCode(inputText));
		} catch (IllegalArgumentException e){
			//otherwise just return the input text
			return inputText;
		}
	}

}
