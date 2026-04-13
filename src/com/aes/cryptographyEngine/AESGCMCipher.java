package com.aes.cryptographyEngine;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AESGCMCipher {
	private static final int GCM_Tag_Length =128;//Suggested by NIST
	private Cipher cipher;
	
	
	//Method to initialize the Cipher Engine
	public void initialize(int operationMode,SecretKey key, byte[]iv){
		
		try {
			GCMParameterSpec gcmParamSpec = new GCMParameterSpec(GCM_Tag_Length,iv);
			cipher=Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(operationMode,key,gcmParamSpec);
		}
		
		
		catch(NoSuchPaddingException e) {
			System.out.println("Padding not Available:" +e.getMessage());
		}catch(NoSuchAlgorithmException e) {
			System.out.println("Algorithm does not Exist"+e.getMessage());
		}
		catch(InvalidAlgorithmParameterException ie) {
			System.out.println("Please check the parameters :"+ie.getMessage());
		}
		catch(InvalidKeyException ike) {
			System.out.println("Please check the Keys :"+ike.getMessage());
		}
		
	}
	
	
	//Method for processing 4KB data Chunk so that the JVM won't run out of memory.
	//This method remains the mathematical state open so that nest byte can also be processed.
	public byte[] updateByte(byte[] inputChunk) {
		
		return (cipher.update(inputChunk));
	}
	
	
	//This method process the very last chunk of the data and completely closes the mathematical state .
	public byte[] finalizeByte(byte[] lastChunk)  throws BadPaddingException{
		try {
				return (cipher.doFinal(lastChunk));

		}

		catch(IllegalBlockSizeException e) {
			System.out.println("Invalid Block Size during finalization:" +e.getMessage());
			return null;
		}
	}
}
