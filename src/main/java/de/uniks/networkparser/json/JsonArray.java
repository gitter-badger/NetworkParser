package de.uniks.networkparser.json;

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
import java.util.Iterator;
import de.uniks.networkparser.EntityUtil;
import de.uniks.networkparser.Tokener;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.StringItem;
import de.uniks.networkparser.list.SortedList;
/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 *

 * <code>{} [ ] / \ : , = ; #</code> and if they do not look like numbers and
	* if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2010-12-28
 */

public class JsonArray extends SortedList<Object> implements
		StringItem {

	/**
	 * Get the JSONArray associated with an index.
	 *
	 * @param index
	 *			The index must be between 0 and length() - 1.
	 * @return A JSONArray value.
	 * @throws RuntimeException
	 *			 If there is no value for the index. or if the value is not a
	 *			 JSONArray
	 */
	public JsonArray getJSONArray(int index) {
		Object object = get(index);
		if (object instanceof JsonArray) {
			return (JsonArray) object;
		} else if(object instanceof String) {
			return new JsonArray().withValue(""+object);
		}
		throw new RuntimeException("JSONArray[" + index
				+ "] is not a JSONArray.");
	}

	/**
	 * Get the JSONObject associated with an index.
	 *
	 * @param index
	 *			subscript
	 * @return A JSONObject value.
	 * @throws RuntimeException
	 *			 If there is no value for the index or if the value is not a
	 *			 JSONObject
	 */
	public JsonObject getJSONObject(int index) {
		Object object = get(index);
		if (object instanceof JsonObject) {
			return (JsonObject) object;
		} else if(object instanceof String) {
			return new JsonObject().withValue(""+object);
		}
		throw new RuntimeException("JSONArray[" + index
				+ "] is not a JSONObject.");
	}

   /**
	* Get the JSONObject associated with an index.
	*
	* @param index
	*			subscript
	* @return A JSONObject value.
	* @throws RuntimeException
	*			 If there is no value for the index or if the value is not a
	*			 JSONObject
	*/
   public String getString(int index) {
	  Object object = get(index);
	  if (object instanceof String) {
		 return (String) object;
	  }
	  throw new RuntimeException("JSONArray[" + index
			+ "] is not a String.");
   }

	/**
	 * Produce a JSONObject by combining a JSONArray of names with the values of
	 * this JSONArray.
	 *
	 * @param names
	 *			A JSONArray containing a list of key strings. These will be
	 *			paired with the values.
	 * @return A JSONObject, or null if there are no names or if this JSONArray
	 *		 has no values.
	 */
	public JsonObject toJSONObject(JsonArray names) {
		if (names == null || names.size() == 0 || size() == 0) {
			return null;
		}
		JsonObject jo = new JsonObject();
		for (int i = 0; i < names.size(); i += 1) {
			jo.put(""+names.getKeyByIndex(i), this.get(i));
		}
		return jo;
	}

	/**
	 * Make a JSON text of this JSONArray. For compactness, no unnecessary
	 * whitespace is added. If it is not possible to produce a syntactically
	 * correct JSON text then null will be returned instead. This could occur if
	 * the array contains an invalid number.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a printable, displayable, transmittable representation of the
	 *		 array.
	 */
	@Override
	public String toString() {
		return toString(0, 0);
	}

	/**
	 * Make a prettyprinted JSON text of this JSONArray. Warning: This method
	 * assumes that the data structure is acyclical.
	 *
	 * @param indentFactor
	 *			The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, transmittable representation of the
	 *		 object, beginning with <code>[</code>&nbsp;<small>(left
	 *		 bracket)</small> and ending with <code>]</code>
	 *		 &nbsp;<small>(right bracket)</small>.
	 */
	@Override
	public String toString(int indentFactor) {
		return toString(indentFactor, 0);
	}

	/**
	 * Make a prettyprinted JSON text of this JSONArray.
	 */
	@Override
	public String toString(int indentFactor, int indent) {
		Iterator<Object> iterator = iterator();
		if (!iterator.hasNext()) {
			return "[]";
		}

		if (!isVisible()) {
			return "[" + size() + " Items]";
		}

		StringBuilder sb;
		String step = EntityUtil.repeat(' ', indentFactor);
		String prefix = "";
		int newindent = 0;
		if (indent > 0) {
			newindent = indent + indentFactor;
		}

		if (newindent > 0) {
			sb = new StringBuilder();
			for (int i = 0; i < indent; i += indentFactor) {
				sb.append(step);
			}
			prefix = CRLF + sb.toString();
		}
		// First Element
		sb = new StringBuilder("[" + prefix);
		Object element = iterator.next();
		sb.append(EntityUtil.valueToString(element, indentFactor, newindent,
				false, this));

		while (iterator.hasNext()) {
			element = iterator.next();
			sb.append("," + prefix + step);
			sb.append(EntityUtil.valueToString(element, indentFactor,
					newindent, false, this));
		}
		sb.append(prefix + ']');
		return sb.toString();
	}

	/**
	 * JSONArray from a source JSON text.
	 *
	 * @param value
	 *			A string that begins with <code>[</code>&nbsp;<small>(left
	 *			bracket)</small> and ends with <code>]</code>
	 *			&nbsp;<small>(right bracket)</small>.
	 * @return Itself
	 */
	public JsonArray withValue(String value) {
		clear();
		new JsonTokener().withBuffer(value).parseToEntity(this);
		return this;
	}

	/**
	 * JSONArray from a JSONTokener.
	 *
	 * @param x
	 *			A JSONTokener
	 * @return Itself
	 */
	public JsonArray withValue(Tokener x) {
		x.parseToEntity(this);
		return this;
	}

	/**
	 * JSONArray from a BaseEntityArray.
	 *
	 * @param values
	 *			of Elements.
	 * @return Itself
	 */
	public JsonArray withValue(BaseItem... values) {
		for (int i = 0; i < values.length; i++) {
			add(EntityUtil.wrap(values[i], this));
		}
		return this;
	}

	public JsonObject get(String id) {
		for (Object item : this) {
			if (item instanceof JsonObject) {
				JsonObject json = (JsonObject) item;
				if (json.has(JsonIdMap.ID)
						&& json.getString(JsonIdMap.ID).equals(id)) {
					return json;
				}
			}
		}
		return null;
	}

	/**
	 * Get a new Instance of a JsonObject
	 */
	@Override
	public BaseItem getNewList(boolean keyValue) {
		if(keyValue) {
			return new JsonObject();
		}
		return new JsonArray();
	}

	@Override
	public boolean remove(Object value) {
		return removeByObject(value) >= 0;
	}
	@Override
	public JsonArray subList(int fromIndex, int toIndex) {
		return (JsonArray) super.subList(fromIndex, toIndex);
	}
}
