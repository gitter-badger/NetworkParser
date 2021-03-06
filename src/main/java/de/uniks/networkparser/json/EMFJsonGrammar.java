package de.uniks.networkparser.json;

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
import java.util.Iterator;
import de.uniks.networkparser.Filter;
import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.MapEntity;
import de.uniks.networkparser.SimpleGrammar;
import de.uniks.networkparser.buffer.CharacterBuffer;
import de.uniks.networkparser.buffer.Tokener;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.Entity;
import de.uniks.networkparser.interfaces.Grammar;
import de.uniks.networkparser.interfaces.IdMapCounter;
import de.uniks.networkparser.interfaces.SendableEntityCreator;

public class EMFJsonGrammar extends SimpleGrammar {
	public static final String SRC="@src";
	public static final String PROP="@prop";
	public static final String NV ="@nv";

	@Override
	public BaseItem getProperties(Entity item, IdMap map, Filter filter, boolean isId, String type) {
		JsonObject props= new JsonObject();
		if(item.has(PROP)){
			String key = item.getString(PROP);
			String value = item.getString(NV);
			SendableEntityCreator result = getCreator(Grammar.READ, null, map, false, value);
			if(result!=null){
				props.put(key, new JsonObject().withValue(SRC, value));
			} else {
				props.put(key, value);
			}
			return props;
		}
		return item;
	}

	@Override
	public SendableEntityCreator getCreator(String type, Object item, IdMap map, boolean searchForSuperCreator,
			String className) {
		if(Grammar.READ.equals(type) && item instanceof Entity) {
			SendableEntityCreator result = getCreator(type, null, map, false, ((Entity)item).getString(SRC));
			if(result!=null){
				return result;
			}
			return super.getCreator(type, item, map, searchForSuperCreator, className);
		}
		if(className == null) {
			return null;
		}
		int pos=className.indexOf("@");
		String clazz=null;
		if(pos>0){
			clazz=className.substring(0, pos);
		}else {
			pos = className.lastIndexOf(".");
			if(pos>0) {
				clazz=className.substring(0, pos);
			}
		}
		if(clazz != null) {
			for (Iterator<SendableEntityCreator> i = map.iterator();i.hasNext();){
				SendableEntityCreator creator = i.next();
				Object sendableInstance = creator.getSendableInstance(true);
				String refClazzName = sendableInstance.getClass().getName();
				if(refClazzName.endsWith("." +clazz)){
					return creator;
				}
			}
		}
		return super.getCreator(type, item, map, searchForSuperCreator, className);
	}

	@Override
	public String getId(Object obj, IdMapCounter counter) {
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
	public String getValue(Entity item, String property) {
		if (IdMap.ID.equals(property)) {
			return item.getString(SRC);
		}
		return item.getString(property);
	}

	@Override
	public boolean hasValue(Entity json, String property) {
		if(property.equals(IdMap.ID)){
			property = SRC;
		}
		return super.hasValue(json, property);
	}

	@Override
	public CharacterBuffer getPrefixProperties(SendableEntityCreator creator, Tokener format, boolean isId) {
		return new CharacterBuffer();
	}

	@Override
	public Entity writeBasicValue(Entity entity, BaseItem parent, String className, String id, MapEntity map) {
		if(id != null) {
			entity.put(SRC, id);
		}
		return entity;
	}
}
