package org.fp;

public class DecryptVIC {

    /**
     * Decrypts the given encrypted password using the provided VIC data.
     *
     * @param encryptedPassword The encrypted password to be decrypted.
     * @param vicData           The VIC data used for decryption.
     * @return The decrypted password.
     */
    public static String decrypt(String encryptedPassword, VICData vicData) {
        // VIC data validation
        String agentID = VICOperations.extractID(encryptedPassword, vicData.date);
        String encodedMessage = VICOperations.extractEncodedMessage(encryptedPassword, vicData.date);

        // VIC decoding
        String step1 = VICOperations.noCarryAddition(agentID, vicData.date.substring(0, 5));
        String step2 = VICOperations.chainAddition(step1, 10);
        String step3 = VICOperations.digitPermutation(vicData.phrase);
        String step4 = VICOperations.noCarryAddition(step2, step3);
        String step5 = VICOperations.digitPermutation(step4);
        String decodedMessage = VICOperations.checkerboardDecode(step5, vicData.anagram, encodedMessage);

        return decodedMessage;
    }
}
