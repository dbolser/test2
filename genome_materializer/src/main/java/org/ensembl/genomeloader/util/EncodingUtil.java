/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
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
 */

/**
 * File: EncodingUtil.java
 * Created by: dstaines
 * Created on: Sep 26, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;

/**
 * Utility class for encoding and decoding various objects
 * 
 * @author dstaines
 * 
 */
public class EncodingUtil {

	/**
	 * Utility method for encoding and decoding strings using user name as key
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
		if (args.length != 2 && args.length != 3) {
			System.err
					.println("Usage: EncodingUtil encrypt|decrypt string url");
			System.exit(1);
		}
		String in = args[1];
		String out = null;
		if ("encrypt".equals(args[0])) {
			out = encode(in, System.getProperty("user.name"));
			if (args.length == 3 && "url".equals(args[2])) {
				out = new URLCodec().encode(out);
			}
		} else {
			if (args.length == 3 && "url".equals(args[2])) {
				in = new URLCodec().decode(in).replace(" ", "+");
			}
			out = decode(in, System.getProperty("user.name"));
		}
		System.out.println(out);
	}

	/**
	 * Current CODEC used
	 */
	private static final String CODEC = "ARCFOUR";

	/**
	 * Encode the string using the supplied key and return as base 64 string.
	 * Note that the key is encoded alongside the string, allowing a checksum
	 * 
	 * @param input
	 * @param key
	 * @return
	 */
	public static String encode(String input, String key) {
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), CODEC);
		Cipher cipher;
		input = key + ":" + input;
		try {
			cipher = Cipher.getInstance(CODEC);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] out = cipher.doFinal(input.getBytes());
			return new String(Base64.encodeBase64(out));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Decode a base 64 encrypted string with the supplied key. The key is
	 * expected to be found prepending the decoded string - if not, the original
	 * string is returned
	 * 
	 * @param input
	 * @param key
	 * @return
	 */
	public static String decode(String input, String key) {
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), CODEC);
			Cipher cipher = Cipher.getInstance(CODEC);
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] out = cipher.doFinal(Base64.decodeBase64(input.getBytes()));
			String output = new String(out);
			if (output.startsWith(key + ":")) {
				return output.replaceFirst(key + ":", "");
			} else {
				return input;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Deserialize an object using base 64
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserialiseObject(String s) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bytesS = new ByteArrayInputStream(Base64
				.decodeBase64(s.getBytes()));
		ObjectInputStream is = new ObjectInputStream(bytesS);
		return (Serializable) (is.readObject());
	}

	/**
	 * Serialize an object using base 64
	 * 
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static String serialiseObject(Object o) throws IOException {
		ByteArrayOutputStream bytesS = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bytesS);
		out.writeObject(o);
		out.close();
		return new String(Base64.encodeBase64Chunked(bytesS.toByteArray()));
	}
}
