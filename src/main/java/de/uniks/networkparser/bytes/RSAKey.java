package de.uniks.networkparser.bytes;
/*
NetworkParser
The MIT License
Copyright (c) 2010-2016 Stefan Lindel https://github.com/fujaba/NetworkParser/

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
import java.math.BigInteger;
import java.util.Random;
import de.uniks.networkparser.buffer.CharacterBuffer;
import de.uniks.networkparser.buffer.DERBuffer;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.Entity;

public class RSAKey {
	public static final String BEGINPUBLICKEY = "-----BEGIN PUBLIC RSA KEY-----\n";
	public static final String ENDPUBLICKEY = "-----END PUBLIC RSA KEY-----";
	public static final String BEGINPRIVATEKEY = "-----BEGIN PRIVATE RSA KEY-----\n";
	public static final String ENDPRIVATEKEY = "-----END PRIVATE RSA KEY-----";

	public static final Byte RSABYTE=48;
	public static final int SAFESIZE = 1024;
	public static final String TAG="RSA";
	// public
	private BigInteger e;
	// private
	private BigInteger d;
	// RSA-Modul
	private BigInteger N;

	public RSAKey(BigInteger N) {
		this.N = N;
	}

	public RSAKey withPublicKey(BigInteger value) {
		this.e = value;
		return this;
	}

	public BigInteger getPublicKey() {
		return e;
	}

	public RSAKey withPrivateKey(BigInteger value) {
		this.d = value;
		return this;
	}

	public BigInteger getPrivateKey() {
		return d;
	}

	public BigInteger getModulus() {
		return N;
	}

	/** 
	 * Sets the public exponent.
	 * @param value The the Public Exponent
	 * @return ThisComponent
	*/
	
	public RSAKey withPubExp(BigInteger value) {
		e = weedOut(value);
		return this;
	}

	/** 
	 * Sets the public exponent.
	 * @param value The the Public Exponent
	 * @return ThisComponent
	 * */
	public RSAKey withPubExp(int value) {
		BigInteger newValue = BigInteger.valueOf(value);
		e = weedOut(newValue);
		return this;
	}

	/**
	 * Performs the classical RSA computation.
	 * @param message Encrypt a Message
	 * @return Encoded Message
	 */
	public BigInteger encrypt(BigInteger message) {
		if (message.divide(getModulus()).intValue() > 0) {
			System.out.println("WARNUNG MODULUS MUST BIGGER (HASH-VALUE)");
		}
		return message.modPow(getPublicKey(), getModulus());
	}

	/**
	 *  Performs the classical RSA computation.
	 *  @param value Enscript the Value
	 *  @return the enscripted Message
	 * */
	public StringBuilder encrypt(String value) {
		return encrypt(value, value.length());
	}

	public StringBuilder decrypt(String message) {
		return decrypt(new BigInteger(message));
	}
	
    /** 
     * Performs the classical RSA computation. 
     * @param message Message to descrypt
     * @return the descrypted Message 
     * */
	public StringBuilder decrypt(BigInteger message) {
		BigInteger text = message.modPow(getPrivateKey(), getModulus());
		BigInteger divider = BigInteger.valueOf(1000);
		int bitCount = text.bitCount();
		StringBuilder sb=new StringBuilder(bitCount);
		while(bitCount>=0) {
			BigInteger character = text.remainder(divider);
			sb.setCharAt(bitCount, (char)character.intValue());
			text = text.divide(divider);
			bitCount--;
		}
        return sb;
    }

	public Entity sign(Entity value) {
		String string = value.toString();
		StringBuilder hashCode = encrypt(string, string.length());
		//CHECK FOR HASHCODE ONLY
		value.put(TAG, hashCode);
		return null;
	}
	
	public StringBuilder encrypt(String value, int group) {
		StringBuilder sb = new StringBuilder();
		StringBuilder item = new StringBuilder();

		int c = 0;
		for (int i = 0; i < value.length(); i++) {
			if (c == 0) {
				item = new StringBuilder();
			}
			char character = value.charAt(i);
			if (character < 10) {
				item.append("00" + (int) character);
			} else if (character < 100) {
				item.append("0" + (int) character);
			} else {
				item.append((int) character);
			}
			c++;
			if (c == group) {
				sb.append(encoding(item.toString()));
				item = new StringBuilder();
				c = 0;
			}
		}
		if (c > 0) {
			sb.append(encoding(item.toString()));
		}
		return sb;
	}

	private String encoding(String value) {
		BigInteger encrypt = encrypt(new BigInteger(value));
		String string = encrypt.toString();
		int rest = string.length() % 3;
		if (rest == 1) {
			return "0" + string;
		} else if (rest == 2) {
			return "00" + string;
		}
		return string;
	}

	/** 
	 * Weeds out bad inputs.
	 * @param value The Value for Check
	 * @return the checked Value
	 * */
	private final BigInteger weedOut(BigInteger value) {
		if (!isNull(value) && isPositive(value)) {
			return value;
		} else {
			return null;
		}
	}

	/** 
	 * Returns true when the argument is greater than zero.
	 * @param number Number for Check
	 * @return if number is Positive
	 * */
	private final boolean isPositive(BigInteger number) {
		return (number.compareTo(BigInteger.ZERO) > 0);
	}

	/** 
	 * Returns true when the argument is null.
	 * @param value Value for Check
	 * @return if Value is Null
	 * */
	private final boolean isNull(Object value) {
		return (value == null);
	}
	public static RSAKey generateKey(int p, int q, int max){
		return generateKey(BigInteger.valueOf(p), BigInteger.valueOf(q), max);
	}
	public static RSAKey generateKey() {
		return generateKey(SAFESIZE);
	}
	public static RSAKey generateKey(int max) {
		return generateKey(BigInteger.ZERO, BigInteger.ZERO, max);
	}

	public static RSAKey generateKey(BigInteger p, BigInteger q, int max) {
		Random rand = new Random();
		if (p.longValue() < 1) {
			p = BigInteger.probablePrime(75 * max / 100, rand);
			q = BigInteger.probablePrime(25 * max / 100, rand);
		}
		RSAKey key = new RSAKey(p.multiply(q));
		// n is the modulus for the public key and the private keys

		BigInteger i;
		BigInteger phi = computePhi(p, q);

		for (i = BigInteger.probablePrime((max / 10), rand); i.compareTo(key.getModulus()) < 0; i = i
				.nextProbablePrime()) {
			if (i.gcd(phi).equals(BigInteger.ONE)) {
				break;
			}
		}
		key.withPubExp(i);
		return key;
	}

	/**
	 * Computes the LCM of the primes.
	 * @param p first prime
	 * @param q second prime
	 * @return Phi
	 */
	private static BigInteger computePhi(BigInteger p, BigInteger q) {
		return lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
	}

	/** 
	 * Computes the least common multiple.
	 * @param a first value
	 * @param b second value
	 * @return the multiply of a,b
	 * */
	private static BigInteger lcm(BigInteger a, BigInteger b) {
		return (a.multiply(b).divide(a.gcd(b)));
	}

	public static RSAKey getDecryptKey(BigInteger n, BigInteger privateKey) {
		RSAKey key = new RSAKey(n);
		key.withPrivateKey(privateKey);
		return key;
	}
	
	@Override
	public String toString() {
		CharacterBuffer sb=new CharacterBuffer();
		if(e != null) {
			sb.with(BEGINPUBLICKEY+BaseItem.CRLF);
			sb.with(getPublicStream().toString()+BaseItem.CRLF);
			sb.with(ENDPUBLICKEY+BaseItem.CRLF);
		}
		if(d != null) {
			sb.with(BEGINPRIVATEKEY+BaseItem.CRLF);
			sb.with(getPrivateStream().toString()+BaseItem.CRLF);
			sb.with(ENDPRIVATEKEY+BaseItem.CRLF);
		}
		return sb.toString();
	}

	public DERBuffer getPublicStream() {
		return getStream(e);
	}
	public DERBuffer getPrivateStream() {
		return getStream(d);
	}

	public DERBuffer getStream(BigInteger key) {
		DERBuffer bitString = new DERBuffer();
		
		bitString.addGroup(RSABYTE, new Object[]{N, key});
		DERBuffer derBuffer = new DERBuffer();
		derBuffer = new DERBuffer();
		derBuffer.addGroup(RSABYTE, new Object[]{ 
				RSABYTE, new Object[]{DERBuffer.OBJECTID, new Byte[]{42, -122, 72, -122, -9, 13, 1, 1, 1}, DERBuffer.NULL}, 
				DERBuffer.BITSTRING, bitString.getBytes()});
		// 48 l:92[48 l:13 [ 6 l:9 [42, -122, 72, -122, -9, 13, 1, 1, 1],5 l:0] 3 l:75[n,e]]
		return derBuffer;
	}
}
