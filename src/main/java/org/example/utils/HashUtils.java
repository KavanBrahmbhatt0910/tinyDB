package org.example.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class HashUtils
{
    private HashUtils() {
        throw new AssertionError("HashUtils class cannot be instantiated.");
    }
    public static String hashMD5(String password) {
        return DigestUtils.md5Hex(password);
    }

}
