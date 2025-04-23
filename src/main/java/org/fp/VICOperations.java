package org.fp;
/*=============================================================================
 |   Assignment:  Program #1: The VIC (VIC InComplete) Cipher
 |       Author:  Haocheng Cao (Cao8@arizona.edu)
 |
 |       Course:  CSC 345, Fall 2024
 |   Instructor:  L. McCann
 | Sect. Leader:  Haocheng Cao
 |     Due Date:  September 19th, 2024, at the beginning of class
 |
 |     Language:  Java(JDK 1.3)
 |     Packages:  Java.src
 |  Compile/Run:  [How to Compile and Run this program]
 |                JKD: Compile: EncryptVIC.java & DecryptVIC.java
                  Run: EncryptVIC.java & DecryptVIC.java
 +-----------------------------------------------------------------------------
 |
 |  Description:  This program provide 4 basic VICOperations for EncryptVIC.java
 |                and DecryptVIC.java. Including
 |				  (a) No-Carry Addition
 |				  (b) Chain Addition
 |                (c) Digit Permutation
 |				  (d) The Straddling Checkerboard
 |
 |        Input:  None
 |       Output:  None
 |
 |   Techniques:  StringBuilder()
 |				  ArrayList<>
 |				  HashSet<>
 |				  LinkedHashMap<>
 |				  TreeMap<>
 |
 |   Required Features Not Included: All required features are included.
 |
 |   Known Bugs:  None; the program operates correctly.
 |
 *===========================================================================*/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/**
 * Class Name: VICOperations
 * Author: Haocheng Cao
 * External Packages: None
 * Package: Java.src
 * Inheritance: None
 *
 * Purpose: This class serves as a utility class for performing
 * various cryptographic operations used in the VIC cipher system.
 * It includes methods for non-carry addition, chain addition,
 * digit permutation, and creation of a straddling checkerboard,
 * each essential for the VIC cipher encoding process.
 */
public class VICOperations {
	/**
	 * Performs non-carry addition of two strings representing numbers.
	 * @param num1 The first number as a string.
	 * @param num2 The second number as a string.
	 * @return A string representing the sum of the two numbers,
	 * digit by digit without carry over.
	 */
	public static String noCarryAddition (String num1, String num2) {
		int maxLen = Math.max(num1.length(), num2.length());
		// Reverse to simplify addition from least significant digit
		StringBuilder sb1 = new StringBuilder(num1).reverse();
		StringBuilder sb2 = new StringBuilder(num2).reverse();
		// Pad shorter string with zeros
		while (sb1.length() < maxLen) {
			sb1.append('0');
		}
		while (sb2.length() < maxLen) {
			sb2.append('0');
		}

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < maxLen; i++) {
			int digit1 = sb1.charAt(i) - '0';  // Convert char to integer digit
			int digit2 = sb2.charAt(i) - '0';
			int sum = (digit1 + digit2) % 10;  // Non-carry addition
			result.append(sum);
		}

		result.reverse();  // Reverse back to normal order
		// Strip leading zeros
		while (result.length() > 1 && result.charAt(0) == '0') {
			result.deleteCharAt(0);
		}

		return result.toString();
	}

	/**
	 * Performs chain addition based on a starting number and extends
	 * it to a specified digit length.
	 * @param num The initial number as a string.
	 * @param digit The length to which the number should be extended.
	 * @return A string of numbers extended to the specified length by chain addition.
	 */
	public static String chainAddition (String num, int digit) {
		int numLength = num.length();
		if (numLength >= digit) {
			// If initial number is longer or equal, trim to desired length
			return num.substring(0, digit);
		}
		StringBuilder result = new StringBuilder();
		result.append(num);

		boolean insert;  // Flag to manage initial padding if needed
		if (result.length() < 2) {
			insert = true;
			// Pad with zero if length is less than 2
			result.insert(0, '0');
		} else {
			insert = false;
		}
		while (result.length() <= digit) {
			int firstPos;
			if (insert) {
				// Calculate position for chain addition
				firstPos = result.length() - 1 - numLength;
			} else {
				firstPos = result.length() - numLength;
			}
			int firstDigit = result.charAt(firstPos) - '0';
			int secondDigit = result.charAt(firstPos + 1) - '0';
			int newDigit = (firstDigit + secondDigit) % 10;
			result.append(newDigit);
		}
		if (insert) {
			result.deleteCharAt(0);  // Remove padding if added
		} else {
			// Correct length by removing last digit
			result.deleteCharAt(result.length() - 1);
		}
		return result.toString();
	}

	/**
	 * Creates a digit permutation based on a given string of at least 10 characters.
	 * @param s The input string to permute.
	 * @return A string representing a permutation of the digits 0-9.
	 */
	public static String digitPermutation(String s) {
		if (s == null || s.length() < 10)
			return null;  // Validate input length
		// Use LinkedHashMap to maintain the insertion order
		StringBuilder word = new StringBuilder(s);

		LinkedHashMap<Integer, int[]> charToDigit = new LinkedHashMap<>();

		// Create a HashMap for the position of each character
		for (int i = 0; i < s.length(); i++) {
			char w = Character.toLowerCase(word.charAt(i));
			int place = w - 'a';
			if (!charToDigit.containsKey(place)) {
				charToDigit.put(place, new int[] { i });
			} else {
				int[] oldArray = charToDigit.get(place);
				int[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
				newArray[oldArray.length] = i; // Append the new value
				charToDigit.put(place, newArray);
			}
		}

		// Use array of string to analysis hashMap
		String[] result = new String[10];
		int value = 0;
		TreeMap<Integer, int[]> sortedMap = new TreeMap<>(charToDigit);
		for (Map.Entry<Integer, int[]> entry : sortedMap.entrySet()) {
			int[] list = entry.getValue();
			for (int n : list) {
				if (value < 10) {
					result[n] = String.valueOf(value);
					value++;
				} else {
					break;
				}
			}
		}

		// Output result as string
		StringBuilder output = new StringBuilder();
		for (String c : result) {
			output.append(c);
		}
		return output.toString();
	}

	/**
	 * Generates a list of values from a straddling checkerboard constructed
	 * for the VIC cipher.
	 *
	 * @param number A string representing a permutation of digits 0-9
	 *               used to generate the checkerboard.
	 * @param letter A string representing an anagram, which includes spaces,
	 *               used to create the straddling pattern.
	 * @return An ArrayList containing the mapped values of each character
	 * 			in the sorted checkerboard.
	 */
	public static ArrayList<String> straddlingCheckerboard (String number, String letter){
		// create checkerboard
		LinkedHashMap<Character, String> board = get2DTable(number, letter);
		// Sorts the checkerboard by characters for consistent output
		TreeMap<Character, String> sortedBoard = new TreeMap<>(board);
		ArrayList<String> result = new ArrayList<>();
		for (Map.Entry<Character, String> entry : sortedBoard.entrySet()) {
			result.add(entry.getValue());  // Collect all values in sorted order
		}
        return result;
	}

	/**
	 * Generates a 2-dimensional table used for encoding messages in the VIC cipher.
	 * This table is structured based on the provided digit permutation and anagram.
	 *
	 * @param number A digit permutation
	 * @param letter An anagram
	 * @return A LinkedHashMap representing the checkerboard with character keys
	 * 		and their corresponding coded values.
	 */
	public static LinkedHashMap<Character, String> get2DTable(String number, String letter) {
		letter = letter.toUpperCase();
		Character[] checkerboard = createStraddlingCheckerboard(number, letter);
		if (checkerboard == null){
			return null;
		}
		String label = getLabel(number, letter);
		LinkedHashMap<Character, String> board = new LinkedHashMap<>();
		// Set mappings for non-space characters from the anagram directly using the number permutation
		for (int i = 0; i < 10; i++) {
			if (letter.charAt(i) != ' ') {
				board.put(letter.charAt(i), String.valueOf(number.charAt(i)));
			}
		}

		// Initialize counters for rows and columns
		int row = 0;
		int column = 0;
		for (char c = 'A'; c <= 'Z'; c++) {
			String charAssString = String.valueOf(c);
			if (!letter.contains(charAssString)){
				if (column >= 10) {
					row++;  // Increment row index after filling a row
					column = 0;  // Reset column index
				}
				String digitLabel = String.valueOf(label.charAt(row)) + String.valueOf(number.charAt(column));
				board.put(c, digitLabel);  // Map character to its new coded value
				column++;
			}
		}
		return board;
	}

	/**
	 * Creates a straddling checkerboard from a given digit permutation and an anagram.
	 * Both must be valid for the checkerboard to be created.
	 *
	 * @param digitPermutation A string of 10 unique digits.
	 * @param anagram A string of 10 characters
	 * @return An array of Characters representing the checkerboard or null if validation fails.
	 */
	public static Character[] createStraddlingCheckerboard(String digitPermutation, String anagram) {
		// Validate digit permutation
		if (!isValidDigitPermutation(digitPermutation)) {
			return null;
		}

		// Validate anagram
		if (!isValidAnagram(anagram)) {
			return null;
		}

		// Create checkerboard
		Character[] checkerboard = new Character[10];
		for (int i = 0; i < 10; i++) {
			checkerboard[i] = anagram.charAt(i);
		}

		return checkerboard;
	}

	/**
	 * Validates that a string represents a valid digit permutation for the VIC cipher.
	 * A valid permutation must contain exactly 10 unique digits.
	 *
	 * @param digits The string to validate.
	 * @return true if the string is a valid digit permutation, false otherwise.
	 */
	private static boolean isValidDigitPermutation(String digits) {
		if (digits == null) {
			return false;
		}

		if (digits.length() != 10) {
			return false;
		}

		Set<Character> seenDigits = new HashSet<>();
		for (char c : digits.toCharArray()) {
			if (!Character.isDigit(c) || seenDigits.contains(c)) {
				return false;  // Invalid if character is not a digit or repeats
			}
			seenDigits.add(c);
		}

		return seenDigits.size() == 10;
	}

	/**
	 * Validates that a string is a valid anagram for use in a straddling checkerboard.
	 * It must be exactly 10 characters long, including exactly two spaces and eight
	 * unique letters.
	 *
	 * @param anagram The anagram to validate.
	 * @return true if the anagram meets the conditions, false otherwise.
	 */
	private static boolean isValidAnagram(String anagram) {
        if (anagram.length() != 10) {
            return false;
        }

        int spaceCount = 0;
        Set<Character> letters = new HashSet<>();
        for (char c : anagram.toCharArray()) {
            if (c == ' ') {
                spaceCount++;
            } else if (!Character.isLetter(c) || letters.contains(c)) {
                return false;
            } else {
                letters.add(c);
            }
        }

        return spaceCount == 2 && letters.size() == 8;
    }


	/**
	 * Extracts labels for the straddling checkerboard rows based
	 * on the positions of spaces in the anagram.
	 *
	 * @param number A digit permutation
	 * @param letter The anagram
	 * @return A string of labels derived from the number string
	 * 		at the positions of spaces in the letter string.
	 */
	public static String getLabel(String number, String letter) {

		StringBuilder labels = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			if (letter.charAt(i) == ' ') {
				// Append number associated with space in anagram
				labels.append(number.charAt(i));
			}
		}
		return labels.toString();
	}

	public static String checkerboardEncode(String numberPermutation, String anagram, String message) {
		LinkedHashMap<Character, String> board = get2DTable(numberPermutation, anagram);
		if (board == null) return null;
		StringBuilder encoded = new StringBuilder();
		for (char c : message.toCharArray()) {
			encoded.append(board.get(c));
		}
		return encoded.toString();
	}

	// 新增解码函数
	public static String checkerboardDecode(String numberPermutation, String anagram, String encodedMessage) {
		LinkedHashMap<Character, String> board = get2DTable(numberPermutation, anagram);
		String label = getLabel(numberPermutation, anagram);
		if (board == null) return null;

		ArrayList<String> messageList = new ArrayList<>();
		for (int i = 0; i < encodedMessage.length(); i++) {
			if (label.indexOf(encodedMessage.charAt(i)) != -1) {
				messageList.add(encodedMessage.substring(i, i + 2));
				i++;
			} else {
				messageList.add(encodedMessage.substring(i, i + 1));
			}
		}

		StringBuilder decoded = new StringBuilder();
		for (String code : messageList) {
			for (Entry<Character, String> entry : board.entrySet()) {
				if (entry.getValue().equals(code)) {
					decoded.append(entry.getKey());
					break;
				}
			}
		}
		return decoded.toString();
	}

	// 插入agentID到加密消息中
	public static String insertID(String input, String agentID, String date) {
		int index = Integer.parseInt(date.substring(5));
		return input.substring(0, index) + agentID + input.substring(index);
	}

	// 从加密消息中提取agentID
	public static String extractID(String encodedMessage, String date) {
		int index = Integer.parseInt(date.substring(5));
		return encodedMessage.substring(index, index + 5);
	}

	// 提取除去agentID后的真实加密信息
	public static String extractEncodedMessage(String encodedMessage, String date) {
		int index = Integer.parseInt(date.substring(5));
		return encodedMessage.substring(0, index) + encodedMessage.substring(index + 5);
	}

}
