package org.fp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VICTest {

    @Test
    void testEncryptDecryptPureNumericOutput() {
        String phrase  = "ABCDEFGHIJ";
        String anagram = "ABCD EFGH ";

        VICData vic = new VICData(
                "85721",
                "470918",
                phrase,
                anagram,
                null
        );

        String original = "HELLOWORLD";

        String encrypted = EncryptVIC.encrypt(original, vic);
        assertEquals("0973737485721677476733", encrypted);

        VICData vic2 = new VICData("85721", "470918", phrase, anagram, null);
        String decrypted = DecryptVIC.decrypt(encrypted, vic2);
        assertEquals(original, decrypted);
    }
}
