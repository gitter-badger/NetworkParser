package de.uniks.networkparser;

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

import de.uniks.networkparser.interfaces.BaseItem;

public abstract class IdMap extends IdMapEncoder{
	public abstract Object decode(BaseItem value);
	public abstract Object decode(String value);
	protected NetworkParserLog logger = new NetworkParserLog();
	
	/**
	 * @return the CurrentLogger
	 */
	public NetworkParserLog getLogger() {
		return logger;
	}

	/**
	 * Set the Current Logger for Infos
	 * @param logger the new Logger
	 * @return Itself
	 */
	public IdMap withLogger(NetworkParserLog logger) {
		this.logger = logger;
		return this;
	}
}
