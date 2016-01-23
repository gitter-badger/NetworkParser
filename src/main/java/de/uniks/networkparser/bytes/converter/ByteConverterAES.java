package de.uniks.networkparser.bytes.converter;

import de.uniks.networkparser.buffer.CharacterBuffer;
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
import de.uniks.networkparser.bytes.checksum.AES;
import de.uniks.networkparser.interfaces.ByteConverter;

public class ByteConverterAES extends ByteConverter {
	private AES aes;

	public void setKey(String value) {
		if (this.aes == null) {
			this.aes = new AES();
		}
		this.aes.setKey(value);
	}

	public CharacterBuffer toString(String values) {
		return aes.encode(values);
	}

	@Override
	public String toString(byte[] values, int size) {
		return this.aes.encode(values).toString();
	}

	@Override
	public byte[] decode(String value) {
		return aes.decodeString(value);
	}
}
