package org.kosa.shoppingmaillmanager.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

	// AES : Advanced Encryption Standard. 대표적인 대칭키 암호화 방식
	//		-> 암호화 & 복호화에 같은 키 사용
	
	// Cipher : 암호화/복호화 기능을 제공하는 Java 클래스
	
	// SecretKeySpec	: 비밀키를 byte[] 형태로 지정해주는 객체
	
	// Base64 인코딩	: 암호화된 바이트 데이터를 문자열로 바꾸는 방식 (URL-safe하게 보낼 수 있도록)
	
	// UTF-8	: 문자열 <-> 바이트 변환 시 사용한 문자 인코딩 방식
	
	
    private static final String ALGORITHM = "AES"; // 사용할 암호화 알고리즘 이름 (AES)
    private static final String SECRET_KEY = "1234567890123456"; // 16바이트(128bit) 고정 키

    // 문자열을 암호화하는 메서드
    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM); // AES 알고리즘 객체 생성
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM); // 비밀키 설정
        cipher.init(Cipher.ENCRYPT_MODE, key); // 암호화 모드로 초기화

        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8")); // 평문을 바이트로 암호화
        return Base64.getEncoder().encodeToString(encrypted); // 암호문을 Base64로 인코딩해 문자열로 반환
    }

    // 암호화된 문자열을 복호화하는 메서드
    public static String decrypt(String cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM); // AES 알고리즘 객체 생성
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM); // 비밀키 설정
        cipher.init(Cipher.DECRYPT_MODE, key); // 복호화 모드로 초기화

        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText)); // Base64 디코딩 후 복호화
        return new String(decrypted, "UTF-8"); // 바이트 → 문자열로 변환 후 반환
    }
}