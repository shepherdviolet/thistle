package sviolet.thistle.util.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>加解密基本逻辑</p>
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * @author S.Violet
 *
 */
class BaseCipher {

    /**
     * 加密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static byte[] encrypt(byte[] data, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * 加密(byte[]数据, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, 例如:"1234567812345678".getBytes("UTF-8")
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static byte[] encryptCBC(byte[] data, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        IvParameterSpec iv = new IvParameterSpec(ivSeed);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        return cipher.doFinal(data);
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void encrypt(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, 例如:"1234567812345678".getBytes("UTF-8")
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void encryptCBC(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            IvParameterSpec iv = new IvParameterSpec(ivSeed);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * 解密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static byte[] decrypt(byte[] data, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * 解密(byte[]数据, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, "1234567812345678".getBytes("UTF-8")
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static byte[] decryptCBC(byte[] data, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        IvParameterSpec iv = new IvParameterSpec(ivSeed);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        return cipher.doFinal(data);
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void decrypt(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            out = new CipherOutputStream(out, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, "1234567812345678".getBytes("UTF-8")
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void decryptCBC(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            IvParameterSpec iv = new IvParameterSpec(ivSeed);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

            out = new CipherOutputStream(out, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }
    }

}
