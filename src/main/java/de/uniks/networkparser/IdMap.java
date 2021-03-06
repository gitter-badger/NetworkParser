package de.uniks.networkparser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
/*
NetworkParser
Copyright (c) 2011 - 2016, Stefan Lindel
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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import de.uniks.networkparser.buffer.Buffer;
import de.uniks.networkparser.buffer.ByteBuffer;
import de.uniks.networkparser.buffer.CharacterBuffer;
import de.uniks.networkparser.buffer.Tokener;
import de.uniks.networkparser.bytes.ByteEntity;
import de.uniks.networkparser.bytes.ByteTokener;
import de.uniks.networkparser.converter.ByteConverter;
import de.uniks.networkparser.graph.GraphList;
import de.uniks.networkparser.graph.GraphPatternMatch;
import de.uniks.networkparser.graph.GraphTokener;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.ByteItem;
import de.uniks.networkparser.interfaces.Converter;
import de.uniks.networkparser.interfaces.DateCreator;
import de.uniks.networkparser.interfaces.Entity;
import de.uniks.networkparser.interfaces.EntityList;
import de.uniks.networkparser.interfaces.Grammar;
import de.uniks.networkparser.interfaces.IdMapCounter;
import de.uniks.networkparser.interfaces.SendableEntity;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
import de.uniks.networkparser.interfaces.SendableEntityCreatorNoIndex;
import de.uniks.networkparser.interfaces.SendableEntityCreatorTag;
import de.uniks.networkparser.interfaces.UpdateListener;
import de.uniks.networkparser.json.JsonArray;
import de.uniks.networkparser.json.JsonObject;
import de.uniks.networkparser.json.JsonTokener;
import de.uniks.networkparser.json.UpdateJson;
import de.uniks.networkparser.json.util.JsonArrayCreator;
import de.uniks.networkparser.json.util.JsonObjectCreator;
import de.uniks.networkparser.list.EntityComparator;
import de.uniks.networkparser.list.ObjectMapEntry;
import de.uniks.networkparser.list.SimpleIterator;
import de.uniks.networkparser.list.SimpleKeyValueList;
import de.uniks.networkparser.list.SimpleList;
import de.uniks.networkparser.xml.EMFTokener;
import de.uniks.networkparser.xml.MapEntityStack;
import de.uniks.networkparser.xml.XMLEntity;
import de.uniks.networkparser.xml.XMLEntityCreator;
import de.uniks.networkparser.xml.XMLTokener;
/**
 * The Class IdMap.
 * @author Stefan Lindel
 */
public class IdMap implements BaseItem, Iterable<SendableEntityCreator> {
	/** The Constant CLASS. */
	public static final String CLASS = "class";

	/** The Constant VALUE. */
	public static final String VALUE = "value";

	/** The Constant ID. */
	public static final String ID = "id";

	/** The Constant REMOVE. */
	public static final String REMOVE = SendableEntityCreator.REMOVE;

	/** The Constant UPDATE. */
	public static final String UPDATE = SendableEntityCreator.UPDATE;

	/** The Constant NEW. */
	public static final String NEW = SendableEntityCreator.NEW;

	/** The Constant SPACE. */
	public static final char SPACE = ' ';
	/** The Constant EQUALS. */
	public static final char EQUALS = '=';

	/** The Constant MAINITEM. */
	public static final String MAINITEM = "main";

	public static final char DOUBLEQUOTIONMARK = '"';

	public static final String REMOVE_YOU = "REMOVE_YOU";

	public static final byte FLAG_NONE = 0x00;
	public static final byte FLAG_ID = 0x01;
	public static final byte FLAG_TYPESAVE = 0x02;
	public static final byte FLAG_SEARCHFORSUPERCLASS = 0x04;
//	public static final byte FLAG_NOID = 0x02;
//	public static final byte FLAG_ALLOWQUOTE = 0x04;
	private byte flag = FLAG_ID;

	private Grammar grammar = new SimpleGrammar();

	/** The counter. */
	private IdMapCounter counter;

	protected SimpleKeyValueList<String, Object> keyValue = new SimpleKeyValueList<String, Object>().withFlag(SimpleKeyValueList.BIDI);

	protected Filter filter = new Filter();

	private JsonTokener jsonTokener = new JsonTokener().withMap(this);

	private XMLTokener xmlTokener = new XMLTokener().withMap(this);

	private ByteTokener byteTokener = new ByteTokener().withMap(this);

	protected NetworkParserLog logger = new NetworkParserLog();

	/** The update listener. */
	protected UpdateListener updateListener;

	/** The updatelistener for Notification changes. */
	protected PropertyChangeListener listener = new UpdateJson(this);

	protected SimpleKeyValueList<SendableEntityCreator, Object> referenceList = new SimpleKeyValueList<SendableEntityCreator, Object>(); 

	/** The Constant ENTITYSPLITTER. */
	public static final char ENTITYSPLITTER = '.';

	/** The creators. */
	protected SimpleKeyValueList<String, SendableEntityCreator> creators = new SimpleKeyValueList<String, SendableEntityCreator>()
			.withAllowDuplicate(false);
	/**
	 * Instantiates a new id map.
	 */
	public IdMap() {
		this.with(new TextItems());
		this.with(new DateCreator());
		this.with(new JsonObjectCreator());
		this.with(new JsonArrayCreator());
		this.with(new ObjectMapEntry());
		this.with(new XMLEntityCreator());
	}

	public UpdateListener getListener() {
		return updateListener;
	}

	/**
	 * Gets the creator class.
	 *
	 * @param reference		the reference
	 * @return 				the creator class
	 */
	public SendableEntityCreator getCreatorClass(Object reference) {
		if (reference == null) {
			return null;
		}
		return getCreator(reference.getClass().getName(), true);
	}

	/**
	 * Gets the creator classes.
	 *
	 * @param clazz		Clazzname for search
	 * @param fullName	if the clazzName is the Fullname for search
	 * @return 			return a Creator class for a clazz name
	 */
	public SendableEntityCreator getCreator(String clazz, boolean fullName) {
		Object creator = this.creators.getValue(clazz);
		if (creator != null || fullName ) {
			return (SendableEntityCreator) creator;
		}
		String endTag;
		if(clazz.lastIndexOf(ENTITYSPLITTER)>=0) {
			endTag = ENTITYSPLITTER + clazz.substring(clazz.lastIndexOf(ENTITYSPLITTER)+1);
		} else {
			endTag = ENTITYSPLITTER + clazz;
		}
		for(int i=0;i<this.creators.size();i++) {
			String key = this.creators.getKeyByIndex(i);
			SendableEntityCreator value = this.creators.getValueByIndex(i);
			if (key.endsWith(endTag)) {
				return value;
			}
		}

		// Search for Child Node
		return null;
	}

	/**
	 * Adds the creator.
	 *
	 * @param iterator	the creater classes
	 * @return 			return a Creator class for a clazz name
	 */
	public IdMap withCreator(Iterable<SendableEntityCreator> iterator) {
		if(iterator == null) {
			return null;
		}
		for (Iterator<SendableEntityCreator> i = iterator.iterator(); i.hasNext();) {
			with(i.next());
		}
		return this;
	}

	/**
	 * Adds the creator.
	 *
	 * @param createrClass	the creater class
	 * @return AbstractIdMap to interlink arguments
	 */
	public IdMap withCreator(SendableEntityCreator... createrClass) {
		if(createrClass == null) {
			return this;
		}
		for (SendableEntityCreator creator : createrClass) {
			if(creator == null)
				continue;
			try{
				Object reference = creator.getSendableInstance(true);
				if (reference != null) {
					if (reference instanceof Class<?>) {
						this.flag = FLAG_SEARCHFORSUPERCLASS;
						with(((Class<?>)reference).getName(), creator);
					} else {
						with(reference.getClass().getName(), creator);
					}
				}
			}catch(Exception e){}
		}
		return this;
	}

	/**
	 * add a Creator to list of all creators.
	 *
	 * @param className	the class name
	 * @param creator	the creator
	 * @return AbstractIdMap to interlink arguments
	 */
	public IdMap with(String className,
			SendableEntityCreator creator) {
		if (creator instanceof SendableEntityCreatorTag) {
			SendableEntityCreatorTag creatorTag = (SendableEntityCreatorTag) creator;
			this.creators.add(creatorTag.getTag(), creator);
		}
		this.creators.add(className, creator);
		return this;
	}

	/**
	 * remove the creator.
	 *
	 * @param className	the creater class
	 * @return 			true, if successful
	 */
	public boolean removeCreator(String className) {
		return this.creators.remove(className) != null;
	}

	/**
	 * Sets the counter.
	 *
	 * @param counter	the new counter
	 * @return 			Itself
	 */
	public IdMap withCounter(IdMapCounter counter) {
		this.counter = counter;
		return this;
	}

	/**
	 * Gets the counter.
	 *
	 * @return the counter
	 */
	public IdMapCounter getCounter() {
		if (this.counter == null) {
			this.counter = new SimpleIdCounter();
		}
		return this.counter;
	}

	/**
	 * Gets the Id. Do not generate a Id
	 *
	 * @param obj	the obj
	 * @return 		the key
	 */
	public String getKey(Object obj) {
		String result = null;
		try {
			int pos = this.keyValue.indexOfValue(obj);
			if(pos>=0) {
				return this.keyValue.getKeyByIndex(pos);
			}
		} catch (ConcurrentModificationException e) {
		}
		return result;
	}

	/**
	 * Gets the object.
	 *
	 * @param key	the key
	 * @return the object
	 */
	public Object getObject(String key) {
		Object result = null;
		try {
			result = this.keyValue.getValue(key);
		} catch (ConcurrentModificationException e) {
		}
		return result;
	}

	/**
	 * Gets or Create the id.
	 *
	 * @param obj	the obj
	 * @return the id
	 */
	public String getId(Object obj) {
		String key = getKey(obj);
		if (key != null) {
			return key;
		}
		key = grammar.getId(obj, getCounter());
		if (key != null) {
			put(key, obj);
			return key;
		}
		key = getCounter().getId(obj);
		put(key, obj);
		return key;
	}

	/**
	 * Put a Object to List
	 *
	 * @param jsonId		the json id
	 * @param object		the object
	 * @return the newObject
	 */
	public Object put(String jsonId, Object object) {
		boolean changed = this.keyValue.add(jsonId, object);
		if (changed)
		{
		   addListener(object);
		}
		return object;
	}

	protected boolean addListener(Object object) {
		if (object instanceof SendableEntity) {
			((SendableEntity) object).addPropertyChangeListener(this.listener);
		} else {
			SendableEntityCreator creator = getCreatorClass(object);
			if(creator != null && creator instanceof SendableEntity) {
				((SendableEntity) creator).addPropertyChangeListener(this.listener);
			}
		}
		return false;
	}

	/**
	 * Removes the Entity from List or Destroy them
	 *
	 * @param oldValue		the old Value
	 * @param destroy		destroy the missed Element
	 * @return boolean if success
	 */
	public boolean removeObj(Object oldValue, boolean destroy) {
		String key = getKey(oldValue);
		if (destroy) {
			SendableEntityCreator creator = getCreatorClass(oldValue);
			if (creator != null) {
				String[] props = creator.getProperties();
				for (String prop : props) {
					Object reference = creator.getValue(oldValue, prop);
					if (reference instanceof Collection<?>) {
						Collection<?> continee = (Collection<?>) reference;
						Iterator<?> i = continee.iterator();
						while (i.hasNext()) {
							creator.setValue(oldValue, prop, i.next(),
									IdMap.REMOVE);
						}
					} else {
						creator.setValue(oldValue, prop, reference,
								IdMap.REMOVE);
					}
				}
			}
		}
		if (key != null) {
			this.keyValue.without(key);
			return true;
		}
		return false;
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return this.keyValue.size();
	}

	/**
	 * Clone object.
	 *
	 * @param reference		the reference
	 * @param filter		the filter
	 * @return the object
	 */
	public Object cloneObject(Object reference, Filter filter) {
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		return cloning(reference, map);
	}
	private Object cloning(Object reference, MapEntity map) {
		SendableEntityCreator creatorClass = getCreatorClass(reference);
		if(map.contains(reference)) {
			return null;
		}
		map.with(reference);
		Object newObject = null;
		if (creatorClass != null) {
			newObject = creatorClass.getSendableInstance(false);
			String[] properties = creatorClass.getProperties();

			for (String property : properties) {
				Object value = creatorClass.getValue(reference, property);
				if (value instanceof Collection<?>) {
					if (map.isFullSeriation() || map.isPropertyRegard(reference, this, property, value)) {
						Collection<?> list = (Collection<?>) value;
						map.add();
						for (Object item : list) {
							Object refValue = map.getRefByEntity(item);
							if (refValue != null) {
								creatorClass.setValue(newObject, property,
										refValue, IdMap.NEW);
							} else {
								SendableEntityCreator childCreatorClass = getCreatorClass(item);
								if (childCreatorClass != null) {
									if (!map.isConvertable(reference, this, property, item)) {
										creatorClass.setValue(newObject,
												property, item,
												IdMap.NEW);
									} else {
										Object clonedChild = cloning(item, map);
										if(clonedChild!=null) {
											creatorClass.setValue(newObject,
													property, clonedChild,
													IdMap.NEW);
										}
									}
								} else {
									creatorClass.setValue(newObject, property,
											item, IdMap.NEW);
								}
							}
						}
						map.minus();
					}
				} else if(map.isPropertyRegard(reference, this, property, value)){
					Object refValue = map.getRefByEntity(value);
					if (refValue != null) {
						creatorClass.setValue(newObject, property, refValue,
								IdMap.NEW);
					} else {
						SendableEntityCreator childCreatorClass = getCreatorClass(value);
						if (childCreatorClass != null) {
							map.add();
							if (!map.isConvertable(reference, this, property, value)) {
								creatorClass.setValue(newObject, property,
										value, IdMap.NEW);
							} else if(map.isPropertyRegard(reference, this, property, value)) {
								Object clonedChild = cloning(value, map);
								if(clonedChild!=null) {
									creatorClass.setValue(newObject,
											property, clonedChild,
											IdMap.NEW);
								}
							}
							map.minus();
						} else {
							creatorClass.setValue(newObject, property, value,
									IdMap.NEW);
						}
					}
				}
			}
		}
		return newObject;
	}

	public SimpleList<Object> getTypList(SendableEntityCreator creator) {
		if (creator == null) {
			return null;
		}
		SimpleList<Object> result = new SimpleList<Object>();
		String clazzName = creator.getSendableInstance(true).getClass()
				.getName();
		for (Object item : this.keyValue.valuesArrayIntern()) {
			if (item != null) {
				if (item.getClass().getName().equals(clazzName)) {
					result.add(item);
				}
			}
		}
		return result;
	}

	public boolean replaceObject(Object newObject) {
		String key = getKey(newObject);
		if (key != null) {
			return false;
		}
		if (!(newObject instanceof Comparable<?>)) {
			return false;
		}
		SendableEntityCreator creator = getCreatorClass(newObject);
		if (creator == null) {
			return false;
		}
		boolean result = false;
		SimpleList<Object> oldValues = getTypList(creator);
		for (Object obj : oldValues) {
			if (obj instanceof Comparable<?>) {
				@SuppressWarnings("unchecked")
				Comparable<Object> oldValue = (Comparable<Object>) obj;
				if (oldValue.compareTo(newObject) == 0) {
					String oldKey = getKey(oldValue);
					if (oldKey != null) {
						this.keyValue.remove(oldValue);
						put(oldKey, newObject);
					}
				}
			}
		}
		return result;
	}

	public void clear() {
		this.keyValue.clear();
	}

	public IdMap withFilter(Filter filter) {
		this.filter = filter;
		return this;
	}

	/**
	 * @return the CaseSensitive Option
	 */
	public boolean isCaseSensitive() {
		return keyValue.isCaseSensitive();
	}

	/**
	 * For setting the Option of checking the CaseSensitive of the Properties
	 *
	 * @param value		the new Value of CaseSensitive
	 * @return 			XMLGrammar Instance
	 */
	public IdMap withCaseSensitive(boolean value) {
		keyValue.withCaseSensitive(value);
		return this;
	}

	@Override
	public Iterator<SendableEntityCreator> iterator() {
		return this.creators.values().iterator();
	}

	public boolean notify(PropertyChangeEvent event) {
    	if (this.listener != null ) {
    		this.listener.propertyChange(event);
    		if (this.listener != null && this.listener instanceof UpdateListener) {
    			return ((UpdateListener)this.listener).update(event);
    		}
    		if(this.updateListener != null) {
    			this.updateListener.update(event);
    		}
    	}
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " (" + this.size() + ")";
	}

	/**
	 * Set the new Listener
	 *
	 * @param updateListener the new Listener
	 * @return This Component
	 *
	 * @see IdMap#withListener(PropertyChangeListener)
	 * @see de.uniks.networkparser.ChainListener
	 */
	public IdMap withListener(UpdateListener updateListener) {
		this.updateListener = updateListener;
		return this;
	}

	/**
	 * Garbage collection.
	 *
	 * @param root	the root Element for garbage Colleciton
	 */
	public void garbageCollection(Object root) {
		if(this.listener instanceof UpdateJson) {
			((UpdateJson)this.listener).garbageCollection(root);
		}
	}

	/**
	 * @param value		Gammar value
	 * @return Itself
	 */
	public IdMap withGrammar(Grammar value) {
		this.grammar = value;
		return this;
	}

	public IdMap withListener(PropertyChangeListener listener) {
		this.listener = listener;
		return this;
	}

	public IdMap withSessionId(String value) {
		getCounter().withPrefixId(value);
		return this;
	}

	public boolean hasKey(Object element) {
		return this.keyValue.contains(element);
	}

	/**
	 * Read Json Automatic create JsonArray or JsonObject
	 * @param value value to decode
	 * @return the object
	 */
	public Object decode(Buffer value) {
		char firstChar = value.nextClean(true);
		if (firstChar == JsonTokener.STARTARRAY) {
			return decode(jsonTokener.newInstanceList().withValue(value));
		}
		if (firstChar == JsonTokener.STARTENTITY) {
			return decode(jsonTokener.newInstance().withValue(value));
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		if(firstChar == XMLTokener.ITEMSTART) {
			XMLTokener tokener = new XMLTokener().withMap(this);
			tokener.withBuffer(value);
			tokener.skipHeader();
			return decodingXMLEntity(tokener, map);
		}
		// MUST BE BYTE
		return byteTokener.decodeValue((byte)firstChar, value, map);
	}

	public Object decode(Tokener tokener) {
		char firstChar = tokener.nextClean(true);
		if (firstChar == '[') {
			return decode(new JsonArray().withValue(tokener));
		}
		if(firstChar == XMLTokener.ITEMSTART) {
			MapEntity map = new MapEntity(filter, grammar);
			map.withFlag(flag);
			if(tokener instanceof XMLTokener) {
				XMLTokener xmlTokener = (XMLTokener) tokener;
				xmlTokener.skipHeader();
				return decodingXMLEntity(xmlTokener, map);
			} else if(tokener instanceof EMFTokener) {
				EMFTokener xmlTokener = (EMFTokener) tokener;
				xmlTokener.withMap(this);
				return ((EMFTokener) xmlTokener).decode(map, null);
			}
			return null;
		}
		Entity item = tokener.newInstance();
		if(item == null) {
			return null;
		}
		tokener.parseToEntity(item);
		return decode(item);
	}
	/**
	 * Read Json Automatic create JsonArray or JsonObject
	 * @param value for Decoding
	 * @return the object
	 */
	public Object decode(String value) {
		if(value == null) {
			return null;
		}
		return decode(new CharacterBuffer().with(value.intern()));
	}

	/**
	 * Special Case for EMF
	 * @param value EMF-Value as String
	 * @return the object
	 */
	public Object decodeEMF(String value) {
		return decodeEMF(value, null);
	}
	/**
	 * Special Case for EMF
	 * @param value EMF-Value as String
	 * @param root The Root Element for Result of ClassModel
	 * @return the object
	 */
	public Object decodeEMF(String value, Object root) {
		if(value ==null ) {
			return null;
		}
		EMFTokener tokener = new EMFTokener();
		MapEntity map = new MapEntity(filter, grammar);
		tokener.withMap(this);
		map.withFlag(flag);
		tokener.withBuffer(value);
		return tokener.decode(map, root);
	}
	/**
	 * Decode.
	 *
	 * @param value			the value
	 * @param converter		the Converter for bytes to String
	 * @return the object
	 */
	public Object decode(String value, ByteConverter converter) {
		if(converter == null) {
			return null;
		}
		byte[] decodeBytes = converter.decode(value);
		if(decodeBytes == null) {
			return null;
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		ByteBuffer buffer = new ByteBuffer().with(decodeBytes);
		return byteTokener.decodeValue((byte)buffer.getCurrentChar(), buffer, map);
	}

	/**
	 * Read Json Automatic create JsonArray or JsonObject
	 * @param value Value for decoding as SubClasss from BaseItem
	 * @return the object
	 */
	public Object decode(BaseItem value) {
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		return decoding(value, map);
	}

	/**
	 * Read json.
	 *
	 * @param value		the value for decoding
	 * @param target	the target
	 * @param filter	the filter for decoding
	 * @return the object
	 */
	public Object decode(BaseItem value, Object target, Filter filter) {
		if(filter == null) {
			filter = this.filter;
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		map.withTarget(target);
		return decoding(value, map);
	}

	/**
	 * Decoding Element to model
	 * @param value	The Baseitem for decoding
	 * @param map	the Runtime information
	 * @return the decoded Values
	 */
	private Object decoding(BaseItem value, MapEntity map) {
		if (value instanceof JsonArray) {
			return decodingJsonArray((JsonArray) value, map);
		}
		if(value instanceof XMLEntity) {
			return decodingXMLEntity(new XMLTokener().withMap(this).withBuffer(value.toString()), map);
		}
		if(value instanceof ByteEntity) {
			ByteEntity entity = (ByteEntity) value;
			return byteTokener.decodeValue(entity, map);
		}

		return decodingJsonObject((JsonObject) value, map);
	}

	/**
	 * Decoding the XMLTokener with XMLGrammar.
	 *
	 * @param tokener
	 *			The XMLTokener
	 * @return the Model-Instance
	 */

	private Object decodingXMLEntity(XMLTokener tokener, MapEntity map) {
		if (tokener.skipTo(XMLTokener.ITEMSTART, false)) {
			map.withStack(new MapEntityStack());
			//FIRST TAG
			tokener.parseEntity(tokener, map);
			return tokener.parse(tokener, map);
		}
		return null;
	}

	// Methods for decoding SubElements
	/**
	 * Read json.
	 *
	 * @param jsonArray	the json array
	 * @param map		the ruintime information
	 * @return the object
	 */
	private Object decodingJsonArray(JsonArray jsonArray, MapEntity map) {
		Object result = null;
		int len = jsonArray.size() - 1;
		// Add all Objects
		for (int i = 0; i <= len; i++) {
			JsonObject kidObject = jsonArray.getJSONObject(i);
			Object tmp = decodingJsonObject(kidObject, map);
			if (kidObject.has(MAINITEM)) {
				result = tmp;
			} else if (i == 0) {
				result = tmp;
			}
		}
		return result;
	}

	private Object decodingJsonObject(JsonObject jsonObject, MapEntity map) {
		if(this.listener instanceof UpdateJson) {
			UpdateJson listener = (UpdateJson) this.listener;
			Object result = listener.execute(jsonObject, filter);
			if (result != null) {
				return result;
			}
		}
		return jsonTokener.decoding(jsonObject, map);
	}

	/**
	 * To json array by ids.
	 *
	 * @param ids		the suspend id list
	 * @return success all Items to baseItem
	 */
	public JsonArray getJsonByIds(List<String> ids) {
		if (ids == null) {
			return null;
		}
		JsonArray items = new JsonArray();
		for (String childId : ids) {
			JsonObject jsonObject = toJsonObject(getObject(childId), Filter.convertable(Depth.create(0)));
			if (jsonObject != null) {
				items.add(jsonObject);
			}
		}
		return items;
	}

	/**
	 * To XMLEntity
	 * @param entity the object
	 * @return the XMLEntity
	 */
	public XMLEntity toXMLEntity(Object entity) {
		if (entity == null) {
			return null;
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.setFlag(flag);
		return (XMLEntity) this.encode(entity, map, xmlTokener);
	}

	/**
	 * To XMLEntity
	 * @param entity the object
	 * @return the XMLEntity
	 */
	public XMLEntity toSimpleXML(Object entity) {
		if (entity == null) {
			return null;
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		map.withoutFlag(FLAG_ID);
		map.withStack(new MapEntityStack());
		return (XMLEntity) this.encode(entity, map, xmlTokener);
	}

	/**
	 * To json object.
	 *
	 * @param entity	the object
	 * @return the json object
	 */
	public JsonObject toJsonObject(Object entity) {
		if (entity == null) {
			return null;
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		return (JsonObject) this.encode(entity, map, jsonTokener);
	}

	/**
	 * To Jsonobject.
	 *
	 * @param entity		the entity
	 * @param filter		the filter
	 * @return the Jsonobject
	 */
	public JsonObject toJsonObject(Object entity, Filter filter) {
		if (entity == null) {
			return null;
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		return (JsonObject) this.encode(entity, map, jsonTokener);
	}

	/**
	 * To json array.
	 *
	 * @param object	the object
	 * @return the json array
	 */
	public JsonArray toJsonArray(Object object) {
		return toJsonArray(object, null);
	}
	public JsonArray toJsonArray(Object object, Filter filter) {
		if(filter == null) {
			filter = this.filter;
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		map.withTarget(jsonTokener.newInstanceList());
		return (JsonArray) encodeList(object, map, jsonTokener);
	}
	public JsonArray toJsonArray(Object object, JsonArray target, Filter filter) {
		if(filter == null) {
			filter = this.filter;
		}
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		if (target.isComparator()
				&& target.comparator() instanceof EntityComparator) {
			((EntityComparator<?>) target.comparator()).withMap(this);
		}
		map.withTarget(target);
		return (JsonArray) encodeList(object, map, jsonTokener);
	}

	public ByteItem toByteItem(Object object) {
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		return byteTokener.encode(object, map);
	}
	public ByteItem toByteItem(Object object, Filter filter) {
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		return byteTokener.encode(object, map);
	}

	public GraphList toObjectDiagram(Object object) {
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		return new GraphTokener().withMap(this).encode(object, map);
	}

	public GraphList toClassDiagram(Object object) {
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		map.withTokenerFlag(GraphTokener.FLAG_CLASS);
		return new GraphTokener().withMap(this).encode(object, map);
	}

	public GraphPatternMatch getDiff(Object source, Object target, boolean ordered) {
		MapEntity map = new MapEntity(filter, grammar);
		if(ordered) {
			map.setFlag(GraphTokener.FLAG_ORDERD);
		} else {
			map.setFlag(GraphTokener.FLAG_UNORDERD);
		}
		return new GraphTokener().withMap(this).diffModel(source, target, map);
	}

	/**
	 * Convert to JsonArray in the resource
	 *
	 * @param object		the object
	 * @param map			the runtime information
	 * @param tokener		the The Tokener for encoding
	 * @return 				the json array
	 */
	private EntityList encodeList(Object object, MapEntity map, Tokener tokener) {
		EntityList target = (EntityList) map.getTarget();
		SimpleList<String> ignoreIds=new SimpleList<String>();
		if (object instanceof Collection<?>) {
			Collection<?> list = (Collection<?>) object;
			for (Iterator<?> i = list.iterator(); i.hasNext();) {
				Object item = i.next();
				//DEEP 0
				Entity ignore = encode(item, map, tokener);
				if(ignore != null) {
					ignoreIds.add(ignore.getString(ID));
				}
			}
//			return target;
		}else if (object.getClass().isArray()) {
			for (Object item : ((Object[]) object)) {
				if(tokener.getKey(item)==null) {
					//DEEP 0
					Entity ignore = encode(item, map, tokener);
					if(ignore != null) {
						ignoreIds.add(ignore.getString(ID));
					}
				}
			}
			return target;
		} else {
			Entity ignore = encode(object, map, tokener);
			if(ignore != null) {
				ignoreIds.add(ignore.getString(ID));
			}
		}
		if(target.isComparator() == false) {
			SimpleIterator<Entity> queueIterator = new SimpleIterator<Entity>(target);
			while(queueIterator.hasNext()) {
				Entity json = queueIterator.next();
				String id = json.getString(ID);
				if(ignoreIds.contains(id) == false) {
					Object item = this.getObject(id);
					if(item!=null) {
						String className = item.getClass().getName();
				 		encode(item, className,  map, tokener, null);
					}
				}
			}
		}
		return target;
	}

	/**
	 * Convert a Model to Tokener
	 * @param model The Model to encode
	 * @param tokener The Tokener For Syntax
	 * @return The Encoded Model
	 */
	public BaseItem encode(Object model, Tokener tokener) {
		return encode(model, tokener, filter);
	}

	/**
	 * Set the current flag
	 * @param flag	the new flag
	 * @return ThisComponent
	 */
	public IdMap withFlag(byte flag) {
		this.flag = flag;
		return this;
	}
	
	/**
	 * Convert a Model to Tokener
	 * @param model The Model to encode
	 * @param tokener The Tokener For Syntax
	 * @param filter The Filter
	 * @return The Encoded Model
	 */
	public BaseItem encode(Object model, Tokener tokener, Filter filter) {
		MapEntity map = new MapEntity(filter, grammar);
		map.withFlag(flag);
		if(tokener == null) {
			tokener = jsonTokener;
		}
		tokener.withMap(this);
		BaseItem item = grammar.encode(model, map, tokener);

		if(item != null) {
			return item;
		}
		return encode(model, map, tokener);
	}

	/**
	 * Encode Model
	 *
	 * @param entity
	 *            the entity to convert
	 * @param map
	 *            encoding runtimevalue
	 * @param tokener
	 *            tokener for Encoding like JsonTokener, XMLTokener
	 * @return the Jsonobject
	 */
	Entity encode(Object entity, MapEntity map, Tokener tokener) {
		if(entity == null) {
			return null;
		}
		String className = entity.getClass().getName();
 		return encode(entity, className,  map, tokener, null);
	}
	public boolean isError(Object owner, String method, String type, Object entity, String className) {
		return logger.error(owner, method, type, entity, className);
	}

	private Entity encode(Object entity, String className, MapEntity map, Tokener tokener, BaseItem parentNode) {
		SendableEntityCreator creator = map.getCreator(Grammar.WRITE, this, entity, className);
		if (creator == null) {
			return null;
		}
		EntityList targetList = (EntityList) map.getTarget();
		String id = null;
		if(creator instanceof SendableEntityCreatorNoIndex == false) {
			id = map.getId(entity, this, className);
		}
		boolean isSimple = targetList != null && targetList.isComparator() == false;

		Entity item = null;
		if(isSimple) {
			// Only add to List
			if(parentNode != null) {
				item = map.writeBasicValue(creator, tokener.newInstance(), parentNode, className, id);
				targetList.with(item);
				return item;
			}
			// May be a Child
			if(targetList instanceof JsonArray) {
				JsonArray list = (JsonArray) targetList;
				item = list.get(id);
			}
		}
		if(isSimple == false || item == null) {
			item = map.writeBasicValue(creator, tokener.newInstance(), parentNode, className, id);
			if (item == null) {
				return null;
			}
			if(isSimple) {
				targetList.with(item);
			}
		}
		String[] properties = map.getProperties(tokener, creator);
		if (properties != null) {
			map.pushStack(className, entity, creator);
			item.setAllowEmptyValue(map.isFullSeriation());
			// Find ReferenceObject in Cache
			Object referenceObject = referenceList.get(creator);
			if(referenceObject == null) {
				referenceObject = map.getNewEntity(creator, className, true);
				this.referenceList.add(creator, referenceObject);
			}
			map.add();
			CharacterBuffer prop = map.getPrefixProperties(creator, tokener, entity, className);
			int pos=prop.length();

			for (String property : properties) {
				Object value = creator.getValue(entity, property);
				if (value != null) {
					if(map.isPropertyRegard(entity, tokener.getMap(), property, value) == false) {
						continue;
					}
					boolean encoding = map.isFullSeriation();
					if (referenceObject instanceof Class<?>) {
						encoding = true;
					}
					if (!encoding) {
						Object refValue = creator.getValue(referenceObject, property);
						encoding = !value.equals(refValue);
					}
					if (encoding) {
						prop.setNextString(property, pos);
						Entity parent = map.convertProperty(prop, item);
						if (parent.has(property)) {
							if (isError(this, "Encode", NetworkParserLog.ERROR_TYP_DUPPLICATE, entity, className)) {
								throw new RuntimeException("Property duplicate:" + property + "(" + className + ")");
							}
						}
						if(value instanceof Entity && parent instanceof XMLEntity) {
							parent.with(value);
							continue;
						}
						className = value.getClass().getName();
						String fullProp = prop.toString();
						SendableEntityCreator valueCreater = map.getCreator(Grammar.WRITE, tokener.getMap(), value, className);

						String childClassName = value.getClass().getName();
						Object key = value;
						if(map.isId(value, tokener.getMap(), className)) {
							key = tokener.getKey(value);
						}
						boolean contains = false;
						if(key != null) {
							contains = map.contains(key);
						}
						if(valueCreater != null && targetList != null) {
							if(map.isConvertable(entity, tokener.getMap(), property, value)  && contains == false ) {
								encode(value, childClassName, map, tokener, item);
							}
							Entity child = tokener.createLink(item, fullProp, childClassName, tokener.getId(value));
							if(child != null) {
								SendableEntityCreator childCreater = map.getCreator(Grammar.WRITE, tokener.getMap(), child, child.getClass().getName());
								parseValue(fullProp, child, null, childCreater, map, tokener, parent);
							}
						} else if (valueCreater != null && (contains || map.isConvertable(entity, tokener.getMap(),  property, value) == false)){
							Entity child = null;
							if(map.isAddOwnerLink(value)) {
								child = tokener.createLink(item, fullProp, childClassName, tokener.getId(value));
							}
							if(child != null) {
								SendableEntityCreator childCreater = map.getCreator(Grammar.WRITE, tokener.getMap(), child, child.getClass().getName());
								parseValue(fullProp, child, null, childCreater, map, tokener, parent);
							}
						} else {
							parseValue(fullProp, value, className, valueCreater, map, tokener, parent);
						}
					}
				}else if(map.isFullSeriation()) {
					if(property.startsWith(".")) {
						pos--;
					}
					prop.setNextString(property, pos);
					Entity parent = map.convertProperty(prop, item);
					parent.put(prop.toString(), value);
				}
			}
			map.minus();
			map.popStack();
		}
		if(targetList != null && targetList.isComparator()) {
			targetList.with(item);
		}
		return item;
	}

	private void parseValue(String property, Object value, String className, SendableEntityCreator valueCreater, MapEntity map, Tokener tokener, BaseItem parent) {
		Object writeValue = null;
		if (value instanceof Collection<?> && valueCreater == null) {
			// Simple List or Assocs
			EntityList subValues;
			boolean isArray=map.getTarget() != null;
			subValues = tokener.newInstanceList();
//			map.add();
			for (Object child : ((Collection<?>) value)) {
				if(child != null) {
					String childClassName = child.getClass().getName();
					SendableEntityCreator childCreater = map.getCreator(Grammar.WRITE, tokener.getMap(), child, childClassName);
					Object key = child;
					if(map.isId(child, tokener.getMap(), className)) {
						key = tokener.getKey(child);
					}
					if(map.contains(key)) {
						child = tokener.createLink((Entity)parent, property, childClassName, tokener.getKey(child));
						childClassName = null;
					} else if(isArray) {
						if(map.isPropertyRegard(value, tokener.getMap(), property, child) == false) {
							continue;
						}
						if(map.isConvertable(value, tokener.getMap(), property, child)) {
							encode(child, childClassName, map, tokener, parent);
						}
						child = tokener.createLink((Entity)parent, property, childClassName, tokener.getId(child));
						childClassName = null;
					} else if(childCreater != null) {
						if(map.isConvertable(value, tokener.getMap(), property, child) == false) {
							child = tokener.createLink((Entity)parent, property, childClassName, tokener.getKey(child));
							childClassName = null;
						}
					}
					parseValue( property, child, childClassName, childCreater, map, tokener, subValues);
				}
			}
//			map.minus();
			writeValue = subValues;
		} else if (value instanceof Map<?, ?> && valueCreater == null) {
			// Maps
			Map<?, ?> list = (Map<?, ?>) value;
			EntityList subValues = tokener.newInstanceList();
			String packageName = ObjectMapEntry.class.getName();
			for (Iterator<?> i = list.entrySet().iterator(); i.hasNext();) {
				Entry<?, ?> mapEntry = (Entry<?, ?>) i.next();
				SendableEntityCreator childCreater = map.getCreator(Grammar.WRITE, tokener.getMap(), mapEntry, packageName);
				parseValue(property, mapEntry, packageName, childCreater, map, tokener, subValues);
			}
			writeValue = subValues;
		} else if(valueCreater != null && className != null){
			writeValue = encode(value, className, map, tokener, parent);

		} else {
			writeValue = value;
		}
		if(map.writeValue(parent, property, writeValue, tokener)) {
		}else if (parent instanceof EntityList && tokener.isChild(writeValue)){
			((EntityList)parent).with(writeValue);
//			((EntityList)parent).with(tokener.transformValue(writeValue, parent));
		} else if (parent instanceof Entity){
			if (property.length() == 1 && property.charAt(0) == ENTITYSPLITTER) {
				// Its ChildValue
				CharacterBuffer buffer = new CharacterBuffer().with(""+ tokener.transformValue(value, parent));
				((Entity)parent).withValue(buffer);
			} else  if (map.isTypSave() ) {
				Entity child = tokener.newInstance();
				if(child != null) {
					child.put(CLASS, className);
					child.put(VALUE, tokener.transformValue(writeValue, parent));
					((Entity)parent).put(property, child);
				}
			} else {
				//FILTER
				((Entity)parent).put(property, tokener.transformValue(writeValue, parent));
			}
		}
	}

	public SimpleKeyValueList<String, Object> getKeyValue() {
		return keyValue;
	}
	/**
	 * set the new List of Items for the Map
	 *
	 * @param parent		the parent-List of Items
	 * @return the Map
	 */
	public IdMap withKeyValue(SimpleKeyValueList<String, Object> parent) {
		if(parent != null)
			this.keyValue = parent;
		return this;
	}

	public SimpleKeyValueList<String, SendableEntityCreator> getCreators() {
		return this.creators;
	}

	@Override
	public BaseItem getNewList(boolean keyValue) {
		return new IdMap();
	}

	@Override
	public String toString(Converter converter) {
		if(converter == null) {
			return null;
		}
		return converter.encode(this);
	}

	@Override
	public IdMap with(Object... values) {
		if (values == null) {
			return this;
		}
		for(Object item : values) {
			if(item instanceof SendableEntityCreator) {
				this.withCreator((SendableEntityCreator)item);
			} else if(item instanceof UpdateListener) {
				this.withListener((UpdateListener)item);
			} else if(item instanceof PropertyChangeListener) {
				this.withListener((PropertyChangeListener)item);
			} else if(item instanceof Filter) {
				this.withFilter((Filter)item);
			} else if(item instanceof Grammar) {
				this.withGrammar((Grammar)item);
			} else if(item instanceof IdMapCounter) {
				this.withCounter((IdMapCounter)item);
			} else if (item instanceof Iterable<?>) {
				Iterator<?> i = (Iterator<?>) ((Iterable<?>)item).iterator();
				while(i.hasNext()) {
					Object value = i.next();
					if(value instanceof SendableEntityCreator) {
						this.withCreator((SendableEntityCreator)value);
					}
				}
			}
		}
		return this;
	}

}