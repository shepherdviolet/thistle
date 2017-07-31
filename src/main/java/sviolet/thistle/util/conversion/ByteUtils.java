/*
 * Copyright (C) 2015 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Byte处理工具
 * 
 * @author S.Violet
 */

public class ByteUtils {

	private static final String HEX_STRING_MAPPING = "0123456789abcdef0123456789ABCDEF";
	/**
	 * 把两个byte[]前后拼接成一个byte[]
	 * 
	 * @param left left bytes
	 * @param right right bytes
	 * @return jointed bytes
	 */
	public static byte[] joint(byte[] left, byte[] right){
		byte[] result = new byte[left.length + right.length];
		System.arraycopy(left, 0, result, 0, left.length);
		System.arraycopy(right, 0, result, left.length, right.length);
		return result;
	}

	/**
	 * bytes转为hexString
	 * @param bytes bytes
	 * @return lower case hex string
	 */
	public static String bytesToUpperCaseHex(byte bytes) {
		return bytesToHex(bytes).toUpperCase();
	}

	/**
	 * bytes转为hexString
	 * @param bytes bytes
	 * @return hex string
	 */
	public static String bytesToHex(byte bytes) {
		int unitInt = bytes & 0xFF;
		String unitHex = Integer.toHexString(unitInt);
		if (unitHex.length() < 2) {
			return "0" + unitHex;
		}
		return unitHex;
	}

    /**
     * bytes转为hexString
     * @param bytes bytes
     * @return lower case hex string
     */
    public static String bytesToUpperCaseHex(byte[] bytes){
        return bytesToHex(bytes).toUpperCase();
    }

	/**
	 * bytes转为hexString
	 * @param bytes bytes
	 * @return hex string
	 */
	public static String bytesToHex(byte[] bytes){
		if (bytes == null) {
			return null;
		}
        if (bytes.length <= 0){
            return "";
        }
		StringBuilder stringBuilder = new StringBuilder();
        for (byte unit : bytes) {
            int unitInt = unit & 0xFF;
            String unitHex = Integer.toHexString(unitInt);
            if (unitHex.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(unitHex);
        }
		return stringBuilder.toString();
	}

	/**
	 * hexString转为bytes
	 * @param hexString hexString
	 * @return bytes
	 */
	public static byte[] hexToBytes(String hexString) {
		if (hexString == null) {
			return null;
		}
        if (hexString.length() <= 0){
            return new byte[0];
        }
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			int step = i * 2;
			result[i] = (byte) (charToByte(hexChars[step], hexString) << 4 | charToByte(hexChars[step + 1], hexString));
		}
		return result;
	}

	private static byte charToByte(char c, String hexString) {
		int index = HEX_STRING_MAPPING.indexOf(c);
		if (index < 0){
			throw new IllegalArgumentException("[ByteUtils]hexToBytes: illegal char:" + c + ", hex string:" + hexString);
		}
		return (byte) (index % 16);
	}

	/**
	 * 对象转数组
	 * @param obj object
	 * @return bytes
	 */
	public static byte[] objectToByte(Object obj) throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        bytes = bos.toByteArray();
        oos.close();
        bos.close();
        return bytes;
	}

	/**
	 * 数组转对象
	 * @param bytes bytes
	 * @return object
	 */
	public static Object byteToObject(byte[] bytes) throws IOException, ClassNotFoundException {
		Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        obj = ois.readObject();
        ois.close();
        bis.close();
        return obj;
	}

	/**
	 * char[] 转 byte[]
	 * @param chars char[]
	 * @param charset charset
	 */
	public static byte[] charsToBytes(char[] chars, String charset) {
		CharBuffer charBuffer = CharBuffer.allocate(chars.length);
		charBuffer.put(chars);
		charBuffer.flip();
		return Charset.forName(charset).encode(charBuffer).array();
	}

	/**
	 * byte[] 转 char[]
	 * @param bytes byte[]
	 * @param charset charset
	 */
	public static char[] bytesToChars(byte[] bytes, String charset) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
		byteBuffer.put(bytes);
		byteBuffer.flip();
		return Charset.forName(charset).decode(byteBuffer).array();
	}

	/**
	 * 循环左移
	 * @param b byte
	 * @param n 左移位数[0, 8]
	 */
	public static byte cyclicLeftShift(byte b, int n){
		n = n % 8;
		if (n < 0){
			return cyclicRightShift(b, -n);
		}
		//byte做移位时, 会先转为int, 必须与0xff, 否则会出问题
		int bi = b & 0xff;
		return (byte)(bi << n | bi >>> 8 - n);
	}

	/**
	 * 循环右移
	 * @param b byte
	 * @param n 右移位数[0, 8]
	 */
	public static byte cyclicRightShift(byte b, int n){
		n = n % 8;
		if (n < 0){
			return cyclicLeftShift(b, -n);
		}
		//byte做移位时, 会先转为int, 必须与0xff, 否则会出问题
		int bi = b & 0xff;
		return (byte)(bi >>> n | bi << 8 - n);
	}

	private static final String[] BIN_MAPPING = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};

	/**
	 * byte转二进制字符串
	 * @param b byte
	 */
	public static String byteToBin(byte b){
		//byte做移位时, 会先转为int, 必须与0xff, 否则会出问题
		return BIN_MAPPING[(b & 0xff) >>> 4] + BIN_MAPPING[b & 0x0F];
	}

}
