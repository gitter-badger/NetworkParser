package de.uniks.networkparser.bytes;

/*
 NetworkParser
 Copyright (c) 2011 - 2013, Stefan Lindel
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
import de.uniks.networkparser.Filter;

public class ByteFilter extends Filter {
	private boolean isDynamic;
	private boolean isLenCheck;

	public boolean isLenCheck() {
		return isLenCheck;
	}

	public ByteFilter withLenCheck(boolean value) {
		this.isLenCheck = value;
		return this;
	}

	public boolean isDynamic() {
		return isDynamic;
	}

	public ByteFilter withDynamic(boolean value) {
		this.isDynamic = value;
		return this;
	}
	
	@Override
	public Filter cloneObj() {
	   return clone(new ByteFilter());
	}
	
	@Override
	public Filter clone(Filter newInstance)
	{
	   ByteFilter result = (ByteFilter) super.clone(newInstance);
	   result.withDynamic(this.isDynamic).withLenCheck(this.isLenCheck);
	   return result;
	}

	public String getCharset() {
		return "UTF-8";
	}
	
  public int getIndexOfClazz(String clazzName){
     int pos=0;
     for(Object item : visitedObjects){
        if(clazzName.equalsIgnoreCase(item.getClass().getName())){
           return pos;
        }
        pos++;
     }
     return -1;
  }
}
