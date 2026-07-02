//this will be used to generate a key for each user after login. The key will be deleted after user disconnects.

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCryptUtil {
    public static String generateKey(){
        try{
            KeyGenerator keygen = KeyGenerator.getInstance("AES"); // create a key generator for AES (Advanced Encryption Standard)
            keygen.init(128, new SecureRandom()); // set it to generate a random with a key size of 128 bits
            SecretKey sk = keygen.generateKey(); // generate the key 

            return Base64.getEncoder().encodeToString(sk.getEncoded()); // return the key as a base64 encoded string
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //storing the key in a hashmap with the username as the key and the generated key as the value. This will be used to encrypt and decrypt messages between the client and server.
    private static final ConcurrentHashMap<String, SecretKey> userKeys = new ConcurrentHashMap<>();
    
    static void storeUserKey(String username, String base64Key) {
        SecretKey sk = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
        userKeys.put(username, sk);
    }
    
    public static SecretKey getUserKey(String username) {
        return userKeys.get(username);
    }
    
    static void removeUserKey(String username) {
        userKeys.remove(username);
    }

    public static String encrypt(String data, String username){
        SecretKey sk = getUserKey(username);
        if (sk == null) return null;
        try{
            Cipher cipher = Cipher.getInstance("AES"); // create a cipher object for AES, cipher is used to perform encryption and decryption
            cipher.init(Cipher.ENCRYPT_MODE, sk); // initialize the cipher in encryption mode with the secret key
            byte[] encrypted = cipher.doFinal(data.getBytes()); // encrypt the data and return the encrypted bytes, dofinal is used to perform the encryption operation
            return Base64.getEncoder().encodeToString(encrypted); // return the encrypted data as a base64 encoded string
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String data, String username){
        SecretKey sk = getUserKey(username);
        if (sk == null) return null;
        try{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sk); // initialize the cipher in decryption mode
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data)); // decrypt the data and return the decrypted bytes
            return new String(decrypted); // return the decrypted data as a string
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
