package de.uniks.networkparser.graph;

/*
 NetworkParser
 Copyright (c) 2011 - 2014, Stefan Lindel
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
import java.util.Iterator;

import de.uniks.networkparser.event.SimpleMapEntry;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.Converter;
import de.uniks.networkparser.list.SimpleKeyValueList;
import de.uniks.networkparser.list.SimpleList;

public class GraphList extends GraphNode{
	private ArrayList<GraphEdge> edges = new ArrayList<GraphEdge>();
	private String typ=GraphIdMap.CLASS;
	private String style;
	private GraphOptions options;

	public boolean add(GraphMember value) {
		with(value);
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		int index = super.indexOf(o);
		if(index>=0) {
			return super.remove(index) != null;
		}
		return false;
	}

	@Override
	public String toString() {
		return toString(new YUMLConverter());
	}

	public String toString(Converter converter) {
		return converter.convert(this, false);
	}

	public String getTyp() {
		return typ;
	}

	public GraphList withTyp(String typ) {
		this.typ = typ;
		return this;
	}

	public GraphList withEdge(GraphEdge edge) {
		add(edge);
		return this;
	}

	public GraphList withEdge(String sourceName, String targetName) {
		GraphEdge edge = new GraphEdge().with(sourceName).with(
				new GraphEdge().with(targetName));
		add(edge);
		return this;
	}

	public boolean add(GraphEdge edge) {
		for (Iterator<GraphEdge> i = this.edges.iterator(); i.hasNext();) {
			GraphEdge item = i.next();
			if (edge.getOther()!= null && item.containsAll(edge.getOther())
					&& item.getOther().containsAll(edge)) {
				// Back again
				if(edge.getOther() != null ) {	
					item.with(edge.getOther().getCardinality());
					item.with(edge.getOther().getProperty());
				}
				return false;
			}
		}
		return this.edges.add(edge);
	}

	public ArrayList<GraphEdge> getEdges() {
		return edges;
	}

	public SimpleKeyValueList<String, Object> getLinks() {
		SimpleKeyValueList<String, Object> links = new SimpleKeyValueList<String, Object>();
		for (GraphEdge element : edges) {
			for (GraphNode node : element) {
				String key = node.getTyp(typ, false);
				SimpleList<?> value = (SimpleList<?>)links
						.getValueItem(key);
				if (value != null) {
					value.withAll(element);
				} else {
					SimpleList<GraphEdge> simpleList = new SimpleList<GraphEdge>();
					simpleList.add(element);
					links.put(key, simpleList);
				}
			}
		}
		return links;
	}

	@Override
	public BaseItem getNewList(boolean keyValue) {
		if(keyValue) {
			return new SimpleMapEntry<String, GraphNode>();
		}
		return new GraphList();
	}

	@Override
	public GraphList withAll(Object... values) {
		super.withAll(values);
		return this;
	}
	
	public GraphClazz with(GraphClazz value) {
		if (value != null) {
			if(value.getId()==null){
				value.withId(value.getClassName());
			}
			add(value);
		}
		return value;
	}

	public GraphPattern with(GraphPattern value) {
		add(value);
		return value;
	}

	public GraphEdge with(GraphEdge value) {
		add(value);
		return value;
	}
	
	public GraphOptions getOptions() {
		return options;
	}

	public GraphList withOptions(GraphOptions options) {
		this.options = options;
		return this;
	}

	public String getStyle() {
		return style;
	}

	public GraphList withStyle(String style) {
		this.style = style;
		return this;
	}

	public GraphList withMain(GraphNode parse) {
		return this;
	}

	public GraphNode getNode(String id) {
		if(id==null){
			return null;
		}
		for(GraphMember item : this) {
			if(item instanceof GraphNode && id.equalsIgnoreCase(item.getId())){
				return (GraphNode)item;
			}
		}
		return null;
	}

	public GraphEdge getEdge(GraphNode node, String property) {
		for(GraphEdge edge : getEdges()) {
			if(edge.getNode()==node && property.equals(edge.getProperty())) {
				return edge;
			}else if(edge.getOther() != null) {
				GraphEdge oEdge = edge.getOther();
				if(oEdge.getNode()==node && property.equals(oEdge.getProperty())) {
					return oEdge;
				}
			}
		}
		return null;
	}
}
