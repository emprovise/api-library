package com.emprovise.api.security;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class JasyptEncryptor {

    private StandardPBEStringEncryptor stringEncryptor;

    public JasyptEncryptor(String password) {
        stringEncryptor = new StandardPBEStringEncryptor();
        stringEncryptor.setPassword(password);                  // we HAVE TO set a password
        stringEncryptor.setAlgorithm("PBEWithMD5AndDES");             // optionally set the algorithm
//      stringEncryptor.setIvGenerator(new RandomIvGenerator());      // for PBE-AES-based algorithms, the IV generator is MANDATORY
    }

    public String decrypt(String encryptedValue) {
        return stringEncryptor.decrypt(encryptedValue);
    }

    public String encrypt(String toBeEncrypted) {
        return stringEncryptor.encrypt(toBeEncrypted);
    }
}
