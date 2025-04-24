package org.fp;

public class EncryptVIC {

	/**
	 * Encrypts the given password using the provided VIC data.
	 * 
	 * @param password The password to be encrypted.
	 * @param vicData  The VIC data used for encryption.
	 * @return The encrypted password.
	 */
	public static String encrypt(String password, VICData vicData) {
		// VIC data validation
		vicData.message = password;

		// VIC encoding
		String step1 = VICOperations.noCarryAddition(vicData.agentID, vicData.date.substring(0, 5));
		String step2 = VICOperations.chainAddition(step1, 10);
		String step3 = VICOperations.digitPermutation(vicData.phrase);
		String step4 = VICOperations.noCarryAddition(step2, step3);
		String step5 = VICOperations.digitPermutation(step4);
		String step6and7 = VICOperations.checkerboardEncode(step5, vicData.anagram, vicData.message);

		return VICOperations.insertID(step6and7, vicData.agentID, vicData.date);
	}
}