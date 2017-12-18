package sviolet.thistle.util.crypto;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AESCipherTest {

    @Test
    public void streamEncryptDecrypt() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        String data = "我是中国人呢";
        byte[] dataBytes = data.getBytes("UTF-8");
        byte[] key = AESKeyGenerator.generateShaKey128("key".getBytes());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));

        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes("UTF-8"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AESCipher.encrypt(in, out, key, AESCipher.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        AESCipher.decrypt(in, out, key, AESCipher.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(data, new String(decrypted, "UTF-8"));

    }

}
