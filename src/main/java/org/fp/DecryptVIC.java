package org.fp;

public class DecryptVIC {

    /**
     * 对给定的密文密码进行VIC解密
     * @param encryptedPassword 加密后的密码
     * @param vicData VIC解密数据 (date, phrase, anagram)
     * @return 解密后的明文密码
     */
    public static String decrypt(String encryptedPassword, VICData vicData) {
        // 从密文中提取agentID和真正的编码信息
        String agentID = VICOperations.extractID(encryptedPassword, vicData.date);
        String encodedMessage = VICOperations.extractEncodedMessage(encryptedPassword, vicData.date);

        // VIC解密步骤
        String step1 = VICOperations.noCarryAddition(agentID, vicData.date.substring(0, 5));
        String step2 = VICOperations.chainAddition(step1, 10);
        String step3 = VICOperations.digitPermutation(vicData.phrase);
        String step4 = VICOperations.noCarryAddition(step2, step3);
        String step5 = VICOperations.digitPermutation(step4);
        String decodedMessage = VICOperations.checkerboardDecode(step5, vicData.anagram, encodedMessage);

        return decodedMessage;
    }
}
