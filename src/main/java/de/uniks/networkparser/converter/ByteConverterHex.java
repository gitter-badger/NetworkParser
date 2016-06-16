package de.uniks.networkparser.converter;

/*
 NetworkParser
 Copyright (c) 2011 - 2015, Stefan Lindel
 All rights reserved.

 Licensed under the EUPL, Version 1.1 or (as soon they
 will be approved by the European Commission) subsequent
 versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl5

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.
*/
import de.uniks.networkparser.EntityUtil;
import de.uniks.networkparser.buffer.BufferedBuffer;
import de.uniks.networkparser.buffer.CharacterBuffer;

public class ByteConverterHex extends ByteConverter {
	/**
	 * To hex string.
	 *
	 * @param values
	 *			the bytes
	 * @return the string
	 */
	@Override
	public String toString(BufferedBuffer values) {
		return toString(values, 0);
	}
	public String toString(BufferedBuffer values, int space) {
		if(values == null) {
			return null;
		}
		String hexVal = "0123456789ABCDEF";

		CharacterBuffer returnValue = new CharacterBuffer().withBufferLength(values.length() << 1 + values.length() * space);
		String step = EntityUtil.repeat(' ', space);
		for (int i = 0; i < values.length(); i++) {
			int value = values.byteAt(i);
			if (value < 0) {
				value += 256;
			}
			returnValue.with(hexVal.charAt(value / 16));
			returnValue.with(hexVal.charAt(value % 16));
			returnValue.with(step);
		}
		return returnValue.toString();
	}

	/**
	 * To byte string.
	 *
	 * @param value
	 *			the hex string
	 * @return the byte[]
	 */
	@Override
	public byte[] decode(String value) {
		String hexVal = "0123456789ABCDEF";
		byte[] out = new byte[value.length() / 2];

		int n = value.length();

		for (int i = 0; i < n; i += 2) {
			// make a bit representation in an int of the hex value
			int hn = hexVal.indexOf(value.charAt(i));
			int ln = hexVal.indexOf(value.charAt(i + 1));

			// now just shift the high order nibble and add them together
			out[i / 2] = (byte) ((hn << 4) | ln);
		}
		return out;
	}

}
