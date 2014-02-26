package de.uniks.networkparser.graph;

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
import java.util.ArrayList;

import de.uniks.networkparser.interfaces.BaseEntity;
import de.uniks.networkparser.interfaces.BaseEntityList;

public class GraphNode implements BaseEntity {
	private String className;
	private String id;
	private boolean visible = true;
	private ArrayList<Attribute> values = new ArrayList<Attribute>();

	@Override
	public BaseEntityList getNewArray() {
		return new GraphList();
	}

	@Override
	public BaseEntity getNewObject() {
		return new GraphNode();
	}

	@Override
	public GraphNode withVisible(boolean value) {
		this.visible = value;
		return this;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	// GETTER AND SETTER
	public String getClassName() {
		return className;
	}

	public GraphNode withClassName(String className) {
		this.className = className;
		return this;
	}

	public String getId() {
		return id;
	}
	
	public String getTyp(String typ){
		if(typ.equals(GaphIdMap.OBJECT)){
			return getId();
		}else if(typ.equals(GaphIdMap.CLASS)){
			return getClassName();
		}
		return "";
	}
	
	public ArrayList<Attribute> getAttributes(){
		return values;
	}
	
	public GraphNode withTyp(String typ, String value){
		if(typ.equals(GaphIdMap.OBJECT)){
			withId(value);
		}else if(typ.equals(GaphIdMap.CLASS)){
			withClassName(value);
		}
		return this;
	}

	public GraphNode withId(String id) {
		this.id = id;
		return this;
	}

	public void addValue(String property, String clazz, String value) {
		values.add(new Attribute().withKey(property).withClazz(clazz).withValue(value));
	}

	@Override
	public String toString(int indentFactor) {
		return toString(0,0);
	}

	@Override
	public String toString(int indentFactor, int intent) {
		if(id==null){
			return className;
		}
		return id;
	}
}
