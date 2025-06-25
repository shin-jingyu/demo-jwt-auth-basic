package com.example.demo.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // 암호화
    public static String encode(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // 일치 확인
    public static boolean matches(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
