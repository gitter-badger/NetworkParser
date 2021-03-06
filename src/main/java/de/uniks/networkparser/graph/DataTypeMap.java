package de.uniks.networkparser.graph;

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
import de.uniks.networkparser.list.SimpleKeyValueList;

public class DataTypeMap extends DataType {
	private DataType genericKey;

	private DataType genericValue;

	DataTypeMap() {
		super(SimpleKeyValueList.class.getName());
		this.value.withExternal(true);
	}

	public static DataTypeMap create(Clazz key, Clazz value) {
		DataTypeMap result = new DataTypeMap().withGenericKey(DataType.create(key)).withGenericValue(DataType.create(value));
		return result;
	}
	public static DataTypeMap create(String key, String value) {
		DataTypeMap result = new DataTypeMap().withGenericKey(DataType.create(key)).withGenericValue(DataType.create(value));
		return result;
	}

	public static DataTypeMap create(DataType key, DataType value) {
		DataTypeMap result = new DataTypeMap().withGenericKey(key).withGenericValue(value);
		return result;
	}

	private DataTypeMap withGenericKey(DataType key) {
		this.genericKey = key;
		return this;
	}

	private DataTypeMap withGenericValue(DataType value) {
		this.genericValue = value;
		return this;
	}

	public DataType getGenericKey() {
		return genericKey;
	}

	public DataType getGenericValue() {
		return genericValue;
	}

	@Override
	public String getName(boolean shortName) {
		if (this.value == null) {
			return null;
		}
		return this.value.getName(shortName) + "<" + genericKey.getInternName(shortName, false) + "," + genericValue.getInternName(shortName, false) + ">";
	}
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj) == false) {
			return false;
		}
		if(obj instanceof DataTypeMap == false) {
			return false;
		}
		if(obj.hashCode() == this.hashCode()) {
			return true;
		}
		DataTypeMap otherDTM = (DataTypeMap) obj;
		if(this.genericKey == null) {
			if(otherDTM.getGenericKey() != null) {
				return false;
			}
		} else {
			if(otherDTM.getGenericKey().equals(this.genericKey) == false) {
				return false;
			}
		}
		if(this.genericValue == null) {
			return otherDTM.getGenericValue() == null;
		}
		return otherDTM.getGenericValue().equals(this.genericValue);
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
