package org.fp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VICTest {

    @Test
    void testEncryptDecryptPureNumericOutput() {
        // 1) 完全唯一的字母短语（10 个不同字母）
        String phrase  = "ABCDEFGHIJ";
        // 2) 8 个字母 + 2 个空格 的 anagram
        String anagram = "ABCD EFGH ";

        VICData vic = new VICData(
                "85721",       // agentID
                "470918",      // date (YYMMDD)
                phrase,        // → 10 个唯一字母
                anagram,       // → 8 字母 + 2 空格
                null           // message 在 encrypt 里会被赋为 password
        );

        String original = "HELLOWORLD"; // 10 字符长度

        // 加密
        String encrypted = EncryptVIC.encrypt(original, vic);
        // 断言得到纯数字密文
        assertEquals("0973737485721677476733", encrypted);

        // 解密回原文
        VICData vic2 = new VICData("85721", "470918", phrase, anagram, null);
        String decrypted = DecryptVIC.decrypt(encrypted, vic2);
        assertEquals(original, decrypted);
    }
}
