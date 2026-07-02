package Services;
import javax.crypto.*;
import java.util.Base64;



public class CryptUtil {
    public static SecretKey sessionKey; //this is used to 

    public static SecretKey stringToKey(String keyString) {
        return new javax.crypto.spec.SecretKeySpec(Base64.getDecoder().decode(keyString), "AES");
    }

    public static String encrypt(String data, SecretKey key){
        try{

            Cipher cipher = Cipher.getInstance("AES"); // create a cipher object for AES, cipher is used to perform encryption and decryption
            cipher.init(Cipher.ENCRYPT_MODE, key); // initialize the cipher in encryption mode with the secret key
            byte[] encrypted = cipher.doFinal(data.getBytes()); // encrypt the data and return the encrypted bytes, dofinal is used to perform the encryption operation
            return Base64.getEncoder().encodeToString(encrypted); // return the encrypted data as a base64 encoded string
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String data, SecretKey key){
        try{

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key); // initialize the cipher in decryption mode
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data)); // decrypt the data and return the decrypted bytes
            return new String(decrypted); // return the decrypted data as a string
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

