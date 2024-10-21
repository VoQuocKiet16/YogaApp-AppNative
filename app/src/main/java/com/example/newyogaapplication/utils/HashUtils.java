package com.example.newyogaapplication.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static String hashPassword(String password) {
        try {
            // Tạo đối tượng MessageDigest với thuật toán SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Băm mật khẩu thành mảng byte
            byte[] hashedPassword = md.digest(password.getBytes());

            // Chuyển đổi mảng byte thành chuỗi hexa
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedPassword) {
                String hex = Integer.toHexString(0xff & b); // Chuyển đổi từng byte sang chuỗi hexa
                if (hex.length() == 1) {
                    hexString.append('0'); // Thêm số 0 ở phía trước nếu chuỗi hexa chỉ có 1 ký tự
                }
                hexString.append(hex);
            }

            // Trả về chuỗi băm ở dạng hexadecimal (tương tự như C#)
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
