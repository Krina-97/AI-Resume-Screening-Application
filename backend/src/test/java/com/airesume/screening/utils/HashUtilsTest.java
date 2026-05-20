package com.airesume.screening.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HashUtilsTest {

    @Test
    void sha256IsStable() {
        String a = HashUtils.sha256Hex(HashUtils.normalizeResumeText("Hello   World"));
        String b = HashUtils.sha256Hex(HashUtils.normalizeResumeText("hello world"));
        Assertions.assertEquals(a, b);
    }
}
