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
import java.io.UnsupportedEncodingException;
import java.util.Date;

import de.uniks.networkparser.EntityUtil;
import de.uniks.networkparser.buffer.ByteBuffer;
import de.uniks.networkparser.converter.ByteConverter;
import de.uniks.networkparser.converter.ByteConverterHTTP;
import de.uniks.networkparser.converter.ByteConverterHex;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.ByteItem;
import de.uniks.networkparser.interfaces.Converter;
/**
 * The Class ByteEntity.
 */

public class ByteEntity implements ByteItem {
	/** The Constant BIT OF A BYTE. */
	public final static int BITOFBYTE = 8;
	public final static int TYPBYTE = 1;

	public final static String TYP="TYP";
	public final static String VALUE="VALUE";

	/** The Byte Typ. */
	protected byte typ;

	/** The values. */
	protected byte[] values;

	
	
	public String toBinaryString() {
		if(values == null || values.length<1) {
			return "";
		}
		byte[] result = new byte[values.length*9+9];
		for (int z=0; z<Byte.SIZE; z++) {
			result[7-z] = (byte) (typ >> z & 0x1);
		}
		result[8] = ' ';
		for(int i=0;i<values.length;i++) {
			for (int z=0; z<Byte.SIZE; z++) {
				result[i*9+7-z+9] = (byte) (values[i] >> z & 0x1);
			}
			result[i*9+8+9] = ' ';
		}
		return new String(result);
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public byte[] getValue() {
		if(values==null)
			return null;
		return EntityUtil.clone(this.values);
	}

	/**
	 * Sets the value.
	 *
	 * @param type		the new type
	 * @param value		the new value
	 * @return 			Itself
	 */
	public ByteEntity withValue(byte type, byte[] value) {
		this.typ = type;
		if(value != null){
			this.values = EntityUtil.clone(value);
		}
		return this;
	}

	/**
	 * Sets the value.
	 *
	 * @param type		the type of Element
	 * @param value		the new value
	 * @return 			Itself
	 */
	public ByteEntity withValue(byte type, byte value) {
		this.typ = type;
		this.values = new byte[] {value };
		return this;
	}

	public ByteEntity withValue(byte typ, int value) {
		this.typ = typ;
		ByteBuffer msgValue = new ByteBuffer().withBufferLength(4);
		msgValue.put(value);
		this.values = msgValue.flip(true).array();
		return this;
	}

	/**
	 * Byte to unsigned byte.
	 *
	 * @param n		the Byte
	 * @return 		the Byte
	 */
	public byte byteToUnsignedByte(int n) {
		if (n < 128)
			return (byte) n;
		return (byte) (n - 256);
	}

	/*
	 * @see de.uni.kassel.peermessage.Entity#toString()
	 */
	@Override
	public String toString() {
		return toString(null);
	}

	/**
	 * Convert the bytes to a String
	 *
	 * @param converter
	 *			Grammar
	 * @return converted bytes as String
	 */
	@Override
	public String toString(Converter converter) {
		if(converter instanceof ByteConverter) {
			return toString((ByteConverter)converter, false);
		}
		return toString(new ByteConverterHex(), false);
	}

	/**
	 * Convert the bytes to a String
	 *
	 * @param converter
	 *			Grammar
	 * @param dynamic
	 *			if byte is dynamic
	 * @return converted bytes as String
	 */
	@Override
	public String toString(ByteConverter converter, boolean dynamic) {
		if (converter == null) {
			converter = new ByteConverterHTTP();
		}
		return converter.toString(this.getBytes(dynamic));
	}

	/**
	 * Gets the bytes.
	 *
	 * @param buffer
	 *			The Buffer to write
	 * @param isDynamic
	 *			is short the Stream for message
	 * @param isLast
	 *			is the Element is the last of Group
	 * @param isPrimitive
	 *			is the Element is the StreamClazz
	 */
	@Override
	public void writeBytes(ByteBuffer buffer, boolean isDynamic,
			boolean isLast, boolean isPrimitive) {
		byte[] value = this.values;

		byte typ = getTyp();
		if (value == null) {
			typ = EntityUtil.getTyp(typ, 0, isLast);
			EntityUtil.writeByteHeader(buffer, typ, 0);
			return;
		}
		if (isDynamic) {
			if (typ == ByteTokener.DATATYPE_SHORT) {
				short bufferValue = new ByteBuffer().with(value).flip(true).getShort();
				if (bufferValue >= Byte.MIN_VALUE
						&& bufferValue <= Byte.MAX_VALUE) {
					typ = ByteTokener.DATATYPE_BYTE;
					value = new byte[] {(byte) bufferValue };
				}
			} else if (typ == ByteTokener.DATATYPE_INTEGER
					|| typ == ByteTokener.DATATYPE_LONG) {
				int bufferValue = new ByteBuffer().with(value).flip(true).getInt();
				if (bufferValue >= Byte.MIN_VALUE
						&& bufferValue <= Byte.MAX_VALUE) {
					typ = ByteTokener.DATATYPE_BYTE;
					value = new byte[] {(byte) bufferValue };
				} else if (bufferValue >= Short.MIN_VALUE
						&& bufferValue <= Short.MAX_VALUE) {
					typ = ByteTokener.DATATYPE_BYTE;
					ByteBuffer bbShort = ByteBuffer.allocate(Short.SIZE
							/ BITOFBYTE);
					bbShort.put((short) bufferValue);
					bbShort.flip(true);
					value = bbShort.array();
				}
			}
		}
		if (!isPrimitive || typ == ByteTokener.DATATYPE_CLAZZTYP
				|| typ == ByteTokener.DATATYPE_CLAZZTYPLONG) {
			typ = EntityUtil.getTyp(typ, value.length, isLast);
			EntityUtil.writeByteHeader(buffer, typ, value.length);
		}
		// SAVE Length
		buffer.put(value);
	}

	@Override
	public ByteBuffer getBytes(boolean isDynamic) {
		int len = calcLength(isDynamic, true);
		ByteBuffer buffer = EntityUtil.getBuffer(len);
		writeBytes(buffer, isDynamic, true, false);
		buffer.flip(true);
		return buffer;
	}

	public boolean setValues(Object value) {
		byte typ = 0;
		ByteBuffer msgValue = new ByteBuffer();
		if (value == null) {
			typ = ByteTokener.DATATYPE_NULL;
		}
		if (value instanceof Short) {
			typ = ByteTokener.DATATYPE_SHORT;
			msgValue.withBufferLength(Short.SIZE / BITOFBYTE);
			msgValue.put((Short) value);
		} else if (value instanceof Integer) {
			typ = ByteTokener.DATATYPE_INTEGER;
			msgValue.withBufferLength(Integer.SIZE / BITOFBYTE);
			msgValue.put((Integer) value);
		} else if (value instanceof Long) {
			typ = ByteTokener.DATATYPE_LONG;
			msgValue.withBufferLength(Long.SIZE / BITOFBYTE);
			msgValue.put((Long) value);
		} else if (value instanceof Float) {
			typ = ByteTokener.DATATYPE_FLOAT;
			msgValue.withBufferLength(Float.SIZE / BITOFBYTE);
			msgValue.put((Float) value);
		} else if (value instanceof Double) {
			typ = ByteTokener.DATATYPE_DOUBLE;
			msgValue.withBufferLength(Double.SIZE / BITOFBYTE);
			msgValue.put((Double) value);
		} else if (value instanceof Byte) {
			typ = ByteTokener.DATATYPE_BYTE;
			msgValue.withBufferLength(Byte.SIZE / BITOFBYTE);
			msgValue.put((Byte) value);
		} else if (value instanceof Character) {
			typ = ByteTokener.DATATYPE_CHAR;
			msgValue.withBufferLength(Character.SIZE / BITOFBYTE);
			msgValue.put((Character) value);
		} else if (value instanceof String) {
			typ = ByteTokener.DATATYPE_STRING;
			String newValue = (String) value;
			msgValue.withBufferLength(newValue.length());
			try {
				msgValue.put(newValue.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
		} else if (value instanceof Date) {
			typ = ByteTokener.DATATYPE_DATE;
			msgValue.withBufferLength(Integer.SIZE / BITOFBYTE);
			Date newValue = (Date) value;
			msgValue.put((int) newValue.getTime());
		} else if (value instanceof Byte[] || value instanceof byte[]) {
			typ = ByteTokener.DATATYPE_BYTEARRAY;
			if (value != null) {
				byte[] newValue = (byte[]) value;
				msgValue.withBufferLength(newValue.length);
				msgValue.put(newValue);
			}
		}
		if (typ != 0) {
			this.typ = typ;
			// Check for group
			msgValue.flip(true);
			this.values = msgValue.array();
			return true;
		}
		return false;
	}

	/**
	 * Gets the typ.
	 *
	 * @return the typ
	 */
	@Override
	public byte getTyp() {
		return this.typ;
	}

	/**
	 * calculate the length of value
	 *
	 * @return the length
	 */
	@Override
	public int calcLength(boolean isDynamic, boolean isLast) {
		// Length calculate Sonderfaelle ermitteln
		if (this.values == null) {
			return TYPBYTE;
		}
		if (isDynamic) {
			if (typ == ByteTokener.DATATYPE_SHORT) {
				Short bufferValue = new ByteBuffer().with(values).flip(true).getShort();
				if (bufferValue >= Byte.MIN_VALUE
						&& bufferValue <= Byte.MAX_VALUE) {
					return TYPBYTE + Byte.SIZE / BITOFBYTE;
				}
			} else if (typ == ByteTokener.DATATYPE_INTEGER
					|| typ == ByteTokener.DATATYPE_LONG) {
				Integer bufferValue = new ByteBuffer().with(values).flip(true).getInt();
				if (bufferValue >= Byte.MIN_VALUE
						&& bufferValue <= Byte.MAX_VALUE) {
					return TYPBYTE + Byte.SIZE / BITOFBYTE;
				} else if (bufferValue >= Short.MIN_VALUE
						&& bufferValue <= Short.MAX_VALUE) {
					return TYPBYTE + Short.SIZE / BITOFBYTE;
				}
			}
		}
		return TYPBYTE + EntityUtil.getTypLen(typ, values.length, isLast)
				+ this.values.length;
	}

	@Override
	public BaseItem getNewList(boolean keyValue) {
		if(keyValue) {
			return new ByteEntity();
		}
		return new ByteList();
	}

	@Override
	public boolean isEmpty() {
		return getTyp() == ByteTokener.DATATYPE_NULL;
	}

	@Override
	public int size() {
		if (values == null) {
			return 0;
		}
		return values.length;
	}

	public static ByteEntity create(Object value) {
		ByteEntity item = new ByteEntity();
		item.setValues(value);
		return item;
	}

	@Override
	public ByteEntity with(Object... values) {
		if(values==null){
			return this;
		}
		if(values.length>1) {
			byte[] value = new byte[values.length-1];
			for(int i=1;i<values.length;i++) {
				value[i-1] = (Byte) values[i];
			}
			withValue((Byte)values[0], value);
		}
		return this;
	}

	public ByteEntity withValue(byte[] values) {
		if(values==null){
			return this;
		}
		if(values.length>1) {
			byte[] value = new byte[values.length-1];
			for(int i=1;i<values.length;i++) {
				value[i-1] = (Byte) values[i];
			}
			this.typ = (Byte)values[0];
			this.values = value;
		}
		return this;
	}

	public Object getValue(Object key) {
		if(TYP.equals(key)) {
			return typ;
		}
		if(VALUE.equals(key)) {
			return values;
		}
		return null;
	}
}
