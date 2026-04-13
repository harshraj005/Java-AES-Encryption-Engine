package com.aes.file_io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.BadPaddingException;
import javax.crypto.AEADBadTagException;

import com.aes.cryptographyEngine.AESGCMCipher;

public class SecureFileStream {

	
	public void encryptFile(String inputFile,String outputFile, SecretKey key, byte[] iv,AESGCMCipher engine) {
		try {
				FileInputStream fi =new FileInputStream(inputFile);
				FileOutputStream fo= new FileOutputStream(outputFile);
			
			
				engine.initialize(Cipher.ENCRYPT_MODE, key, iv);
				
				fo.write(iv);
			
				byte[] buffer =new byte[4096];
			
				int bytesRead;
				while((bytesRead=fi.read(buffer))!= -1) {
						
						byte[]encrypted = engine.updateByte(Arrays.copyOfRange(buffer, 0, bytesRead));
						fo.write(encrypted);
				}
			
				byte[] finalbyte=engine.finalizeByte(new byte [0]);
				if(finalbyte != null) {
				fo.write(finalbyte);
				}
				fi.close();
				fo.close();
				System.out.println("Encryption successful.");
		}
		catch (AEADBadTagException e) {
	        System.out.println("SECURITY ALERT: File may be tampered — authentication tag mismatch: " + e.getMessage());
	    }
		catch(BadPaddingException e) {
			System.out.println("Authentication tag mismatch - data may be tampered: " + e.getMessage());
		}
		
		catch(IOException e) {
			System.out.println("There is some issue with the file:"+ e.getMessage());
		}
		
		
	}
	
	
	
	
	
	public void decryptFile(String inputFilePath, String outputFilePath, SecretKey key, AESGCMCipher engine) {
		
		try {
			FileInputStream fi =new FileInputStream(inputFilePath);
			FileOutputStream fo= new FileOutputStream(outputFilePath);
			
			byte[] iv = new byte[12];
			fi.read(iv);
			

			engine.initialize(Cipher.DECRYPT_MODE, key, iv);
			
			byte[] buffer =new byte[4096];
			
			
			int byteRead ;
			while((byteRead =fi.read(buffer))!=-1) {
				
				byte[]decrypted = engine.updateByte(Arrays.copyOfRange(buffer, 0, byteRead));
				if(decrypted!= null) {
					fo.write(decrypted);
				}
				
			}
			
			byte[] finalbyte=engine.finalizeByte(new byte [0]);
			if(finalbyte!= null) {
				fo.write(finalbyte);
			}
			fi.close();
			fo.close();
			System.out.println("Decryption successful — file integrity verified.");
			
		}
		
		catch (AEADBadTagException e) {
	        System.out.println("SECURITY ALERT: File may be tampered — authentication tag mismatch: " + e.getMessage());
	    }
		catch(BadPaddingException e) {
			System.out.println("Authentication tag mismatch - data may be tampered: " + e.getMessage());
		}
		catch(IOException e) {
			System.out.println("There is some issue with the file:"+ e.getMessage());
		}
	}
}
