package util;

import java.math.BigInteger;
import java.security.MessageDigest;  

import javax.crypto.KeyGenerator;  
import javax.crypto.Mac;  
import javax.crypto.SecretKey;  
import javax.crypto.spec.SecretKeySpec;
  
import sun.misc.BASE64Decoder;  
import sun.misc.BASE64Encoder;  
  
/** 
 * 基础加密组件 
 *  
 * @author 梁栋 
 * @version 1.0 
 * @since 1.0 
 */  
public abstract class Coder {  
    public static final String KEY_SHA = "SHA";  
    public static final String KEY_MD5 = "MD5";  
  
    /** 
     * MAC算法可选以下多种算法 
     *  
     * <pre> 
     * HmacMD5  
     * HmacSHA1  
     * HmacSHA256  
     * HmacSHA384  
     * HmacSHA512 
     * </pre> 
     */  
    public static final String KEY_MAC = "HmacMD5";  
  
    /** 
     * BASE64解密 
     *  
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decryptBASE64(String key) throws Exception {  
        return (new BASE64Decoder()).decodeBuffer(key);  
    }  
  
    /** 
     * BASE64加密 
     *  
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static String encryptBASE64(byte[] key) throws Exception {  
        return (new BASE64Encoder()).encodeBuffer(key);  
    }  
  
    /** 
     * MD5加密 
     *  
     * @param data 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptMD5(byte[] data) throws Exception {  
  
        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);  
        md5.update(data);  
  
        return md5.digest();  
  
    }  
  
    /** 
     * SHA加密 
     *  
     * @param data 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptSHA(byte[] data) throws Exception {  
  
        MessageDigest sha = MessageDigest.getInstance(KEY_SHA);  
        sha.update(data);  
  
        return sha.digest();  
  
    }  
  
    /** 
     * 初始化HMAC密钥 
     *  
     * @return 
     * @throws Exception 
     */  
    public static String initMacKey() throws Exception {  
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);  
  
        SecretKey secretKey = keyGenerator.generateKey();  
        return encryptBASE64(secretKey.getEncoded());  
    }  
  
    /** 
     * HMAC加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {  
  
        SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), KEY_MAC);  
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());  
        mac.init(secretKey);  
  
        return mac.doFinal(data);  
  
    }
    public static byte[] hex2Bytes(String srcStr) {
		int byte_len = srcStr.length() / 2;
		byte[] result = new byte[byte_len];
		for (int i = 0; i < byte_len; i++) {
			String bytestr = srcStr.substring(i * 2, i * 2 + 2);
			result[i] = Integer.valueOf(bytestr, 16).byteValue();
		}
		return result;
	}
    public static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}
    public static void main(String[] args) throws Exception {
    	String inputStr = "1234";  
        System.err.println("原文:\n" + inputStr);  
        //byte[] inputData = inputStr.getBytes();
        byte[] inputData=Coder.hex2Bytes(inputStr);
        BigInteger md5 = new BigInteger(Coder.encryptMD5(inputData));  
        System.err.println("MD5:\n" + md5.toString(16));  
        BigInteger sha = new BigInteger(Coder.encryptSHA(inputData));  
        System.err.println("SHA:\n" + sha.toString(16));
        String strDes = Coder.bytes2Hex(Coder.encryptSHA(inputData));
        System.out.println(strDes);
        BigInteger mac = new BigInteger(Coder.encryptHMAC(inputData, inputStr));  
        System.err.println("HMAC:\n" + mac.toString(16));
        
  
	}
} 