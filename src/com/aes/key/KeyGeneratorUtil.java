package com.aes.key;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyGeneratorUtil {
	public SecretKey generateKey()  {
		SecretKey sec;
		int size=256;
		KeyGenerator k;
		try {
		//Using of Reflection API to create an instance of that Class.
		k =KeyGenerator.getInstance("AES");
		//Initiating a key of size 256 bit
		k.init(size);
				
		sec=k.generateKey();//Storing the secret Key into the the variable "sec"
		return sec;
		}
		catch(NoSuchAlgorithmException n) {
			System.out.println("This Algorithm does not exist :"+n.getMessage());
			return null;
		}
		
	}
	
	
	
	public byte[] createIV() {
		//Initializing the byte array of size 12 as suggested by NIST.
		byte[] ivArray= new byte[12];
		
		// Making the sr as final so there is no need of creating the object again and again.
		final SecureRandom sr=new SecureRandom();
		
		sr.nextBytes(ivArray);//Storing random bytes into the array.
		return ivArray;
	}
}
