package com.aes.main;

import java.util.Scanner;

import javax.crypto.SecretKey;

import com.aes.cryptographyEngine.AESGCMCipher;
import com.aes.file_io.SecureFileStream;
import com.aes.key.KeyGeneratorUtil;


public class Main {

	public static void main(String[] args) {
		
		KeyGeneratorUtil kgu =new KeyGeneratorUtil();
		byte[] iv= kgu.createIV();
		SecretKey key =kgu.generateKey();
		AESGCMCipher aes= new AESGCMCipher();
		SecureFileStream sfs= new SecureFileStream();
	
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Welcome to the the AES-GCM.");
		
		System.out.println("----------------------------------------------------------------------------------------------------------------");
		System.out.println("This application is developed by Harsh Raj.\n");
		System.out.println("Here you can encrypt any type of file with the help of AES-GCM Algorithm.\nIts one of the widely used Algorithm\n");
		System.out.println("Only files encrypted with this applications can be decrypted via this application.");
		System.out.println("----------------------------------------------------------------------------------------------------------------");
		while(true) {
			System.out.println("What feature you want:\nPress 1 for Encryption\nPress 2 for Decryption\n Press 3 for Exit.");
			int choice =sc.nextInt();
		
//			String inputFile ="input.txt";
//			String encryptedFile="encrypted.encrypted";
//			String decryptedFile ="output.txt";
			
			switch (choice) {
			case 1: 
				System.out.print("Enter the exact address of the file you want to encrypt:-- ");
				String inputFile=sc.next();
				System.out.println("Enter the name of encrypted file");
				String encryptedFile= sc.next();
				sfs.encryptFile(inputFile, encryptedFile, key, iv, aes);
				break;
		
			case 2:
				System.out.println("Enter the name of encrypted file");
				encryptedFile= sc.next();
				System.out.println("Enter the name of decrypted file");
				String decryptedFile= sc.next();
				sfs.decryptFile(encryptedFile, decryptedFile, key, aes);
				break;
		
			case 3 :
				System.out.println("----------------------------------------------------------------------------------------------------------------");
				System.out.println("Exiting the App Thank You for Using It....");
				System.out.println("----------------------------------------------------------------------------------------------------------------");
				break;
		}
		
			sc.close();
		}
		
		
		
	}

}
