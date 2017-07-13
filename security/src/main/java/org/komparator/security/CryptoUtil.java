package org.komparator.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;

import pt.ulisboa.tecnico.sdis.cert.*;


public class CryptoUtil {
	/** Asymmetric cryptography algorithm. */
	private static final String ASYM_ALGO = "RSA";
	/** Asymmetric cryptography key size. */
	private static final int ASYM_KEY_SIZE = 2048;
	/**
	 * Asymmetric cipher: combination of algorithm, block processing, and
	 * padding.
	 */
	private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding";
    
    /** Digital signature algorithm. */
	private static final String SIGNATURE_ALGO = "SHA256withRSA";

	/** Digest algorithm. */
	private static final String DIGEST_ALGO = "SHA-256";

    final static String CERTIFICATE = KeystoreAliasManager.getInstance().getCertificateName();

    final static String KEYSTORE = KeystoreAliasManager.getInstance().getKeyStorePath();
    final static String KEYSTORE_PASSWORD = "OKlGguzw";

    final static String KEY_ALIAS = KeystoreAliasManager.getInstance().getKeyAlias();
    final static String KEY_PASSWORD = "OKlGguzw";

    public byte[] asymCipher(byte[] data) throws Exception {
        try{
            // get an RSA cipher object
            Cipher cipher = Cipher.getInstance(ASYM_CIPHER);
            // encrypt the plain text using the public key
            PublicKey publicKey = CertUtil.getX509CertificateFromResource(CERTIFICATE).getPublicKey();
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);

        }catch(NoSuchAlgorithmException ex){
            System.err.println("Caught exception NoSuchAlgorithmException\n" + ex.getMessage());
        }
        catch(InvalidKeyException ex){
            System.err.println("Caught exception InvalidKeyException\n" + ex.getMessage());
        }
        catch(IllegalBlockSizeException ex){
            System.err.println("Caught exception IllegalBlockSizeException\n" + ex.getMessage());
        }
        catch(NoSuchPaddingException ex){
            System.err.println("Caught exception NoSuchPaddingException\n" + ex.getMessage());
        }
        catch(BadPaddingException ex){
            System.err.println("Caught exception BadPaddingException\n" + ex.getMessage());
        }

        throw new Exception("Not possible to complete cipher!");
    }
    
    /*
        
        //System.out.println("Ciphering  with public key...");
        // encrypt the plain text using the public key
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        byte[] cipherBytes = cipher.doFinal(plainBytes);

        //System.out.println("Ciphered bytes:");
        //System.out.println(printHexBinary(cipherBytes));

       // System.out.println("Deciphering  with private key...");
        // decipher the ciphered digest using the private key
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decipheredBytes = cipher.doFinal(cipherBytes);
       // System.out.println("Deciphered bytes:");
       // System.out.println(printHexBinary(decipheredBytes));

      //  System.out.print("Text: ");
        String newPlainText = new String(decipheredBytes);
      //  System.out.println(newPlainText);
    */

    public byte[] asymDecipher(byte[] data) throws Exception{
    
        try{
            PrivateKey privateKey =  getPrivateKey();
            // get an RSA cipher object
            Cipher cipher = Cipher.getInstance(ASYM_CIPHER);
            // decipher the ciphered digest using the private key
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
		}catch(NoSuchAlgorithmException ex){
            System.err.println("Caught exception NoSuchAlgorithmException\n" + ex.getMessage());
        }
        catch(InvalidKeyException ex){
            System.err.println("Caught exception InvalidKeyException\n" + ex.getMessage());
        }
        catch(IllegalBlockSizeException ex){
            System.err.println("Caught exception IllegalBlockSizeException\n" + ex.getMessage());
        }
        catch(NoSuchPaddingException ex){
            System.err.println("Caught exception NoSuchPaddingException\n" + ex.getMessage());
        }
        catch(BadPaddingException ex){
            System.err.println("Caught exception BadPaddingException\n" + ex.getMessage());
        }

        throw new Exception("Not possible to complete decipher!");
    }
    
    public byte[] makeDigitalSignature(byte[] data) throws Exception {
        try{

            //System.out.println("Signing ...");
            PrivateKey privateKey =  getPrivateKey();

            byte[] digest = digestThis(data);

            return CertUtil.makeDigitalSignature(SIGNATURE_ALGO, privateKey, digest);

        }catch(FileNotFoundException ex){
            System.err.println("Caught exception FileNotFoundException\n" + ex.getMessage());
        }catch(KeyStoreException ex){
            System.err.println("Caught exception KeyStoreException\n" + ex.getMessage());
        }catch(UnrecoverableKeyException ex){
            System.err.println("Caught exception UnrecoverableKeyException\n" + ex.getMessage());
        }catch(NoSuchAlgorithmException ex){
            System.err.println("Caught exception NoSuchAlgorithmException\n" + ex.getMessage());
        }

        throw new Exception("Not possible to sign!");
    }


    public boolean verifyDigitalSignature(byte[] data, byte[] digitalSignature) throws Exception {
    
        try{
            PublicKey publicKey = CertUtil.getX509CertificateFromResource(CERTIFICATE).getPublicKey();

            byte[] decSignature = asymDecipher(digitalSignature);

            byte[] digest = digestThis(data);

            return CertUtil.verifyDigitalSignature(SIGNATURE_ALGO, publicKey, digest, decSignature);

        }catch(FileNotFoundException ex){
            System.err.println("Caught exception FileNotFoundException\n" + ex.getMessage());
        }catch(NoSuchAlgorithmException ex){
            System.err.println("Caught exception NoSuchAlgorithmException\n" + ex.getMessage());
        }

        throw new Exception("Not possible to verify signature!");
    }

    public PrivateKey getPrivateKey() throws KeyStoreException, UnrecoverableKeyException, FileNotFoundException{
        System.out.println("KEYSTORE + " +  " + keyAlias");
        System.out.println(KEYSTORE + " " + KEY_ALIAS);
       
        KeyStore ks = CertUtil.readKeystoreFromFile(KEYSTORE, KEYSTORE_PASSWORD.toCharArray());
        return CertUtil.getPrivateKeyFromKeyStore(KEY_ALIAS, KEY_PASSWORD.toCharArray(), ks);
    }

    private byte[] digestThis(byte[] data) throws NoSuchAlgorithmException{
            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            messageDigest.update(data);
            return messageDigest.digest();
    }    
}
