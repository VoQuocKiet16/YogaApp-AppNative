package com.example.newyogaapplication.utils;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static String hashPassword(String password) {
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");


            byte[] hashedPassword = md.digest(password.getBytes());


            return Base64.encodeToString(hashedPassword, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
