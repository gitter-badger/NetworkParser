package de.uniks.networkparser.json;

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
import java.util.Iterator;
import de.uniks.networkparser.Filter;
import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.interfaces.IdMapCounter;
import de.uniks.networkparser.interfaces.SendableEntityCreator;

public class SimpleGrammar extends Grammar {
	public static final String ID = "@ID";

	/**
	 * @param jsonObject
	 *            the new Value
	 * @param map
	 *            the IdMap
	 * @param filter
	 *            the Filter
	 * @param isId
	 *            is Id is reed
	 * @return the props of theJsonObject
	 */
	@Override
	public JsonObject getReadProperties(JsonObject jsonObject,
			IdMap map, Filter filter, boolean isId) {
		jsonObject.remove(ID);
		return jsonObject;
	}

	/**
	 * @param jsonObject
	 *            the new Value
	 * @return the Creator for this JsonObject
	 */
	@Override
	public SendableEntityCreator getReadCreator(JsonObject jsonObject,
			IdMap map) {
		String idString = jsonObject.getString(ID);
		String className = "."
				+ idString.substring(0,
						idString.indexOf(map.getCounter().getSplitter()));

		// Find Item for LastName
		for (Iterator<SendableEntityCreator> iterator = map.iterator(); iterator
				.hasNext();) {
			SendableEntityCreator item = iterator.next();
			if (item.getSendableInstance(true).getClass().getName()
					.endsWith(className)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public JsonObject getWriteObject(IdMap map,
			SendableEntityCreator prototyp, String className, String id,
			JsonObject jsonProp, Filter filter) {
		JsonObject json = new JsonObject();

		json.put(ID, id);

		if (jsonProp.size() > 0) {
			for (int i = 0; i < jsonProp.size(); i++) {
				json.put(jsonProp.getKeyByIndex(i), jsonProp.getValueByIndex(i));
			}
		}
		return json;
	}

	@Override
	public String getWriteId(Object obj, IdMapCounter counter) {
		String name = obj.getClass().getName();
		int pos = name.lastIndexOf(".");
		counter.withPrefixId(null);
		if (pos > 0) {
			return name.substring(pos + 1) + counter.getSplitter()
					+ counter.getId(obj);
		} else {
			return name + counter.getSplitter() + counter.getId(obj);
		}
	}

	@Override
	public String getReadValue(JsonObject json, String property) {
		if (JsonIdMap.ID.equals(property)) {
			return json.getString(ID);
		}
		return json.getString(property);
	}

	@Override
	public boolean hasReadValue(JsonObject json, String property) {
		if (JsonIdMap.ID.equals(property)) {
			return true;
		}
		return json.has(property);
	}

}
