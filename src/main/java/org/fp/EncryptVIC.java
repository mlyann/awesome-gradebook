package org.fp;

public class EncryptVIC {

	/**
	 * 对给定的明文密码进行VIC加密
	 * @param password 明文密码
	 * @param vicData 其他VIC加密数据 (agentID, date, phrase, anagram)
	 * @return 加密后的密码字符串
	 */
	public static String encrypt(String password, VICData vicData) {
		// 设置VICData对象中的message为密码
		vicData.message = password;

		// VIC加密步骤
		String step1 = VICOperations.noCarryAddition(vicData.agentID, vicData.date.substring(0, 5));
		String step2 = VICOperations.chainAddition(step1, 10);
		String step3 = VICOperations.digitPermutation(vicData.phrase);
		String step4 = VICOperations.noCarryAddition(step2, step3);
		String step5 = VICOperations.digitPermutation(step4);
		String step6and7 = VICOperations.checkerboardEncode(step5, vicData.anagram, vicData.message);

		return VICOperations.insertID(step6and7, vicData.agentID, vicData.date);
	}
}