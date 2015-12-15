package de.uniks.networkparser.logic;

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
/**
 * Logicclass for simple Check of Value.
 *
 * @author Stefan Lindel
 */

public class ValuesSimple {
	/** The Variable of value. */
	protected Object value;

	/**
	 * @return The Value of Check
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value Set the new Value.
	 * @return ValuesSimple Instance
	 */
	public ValuesSimple withValue(Object value) {
		this.value = value;
		return this;
	}
}
