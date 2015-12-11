package de.uniks.networkparser.graph;

public class Association extends GraphMember {
	public static final String PROPERTY_NODE = "node";
	public static final String PROPERTY_CARDINALITY = "cardinality";
	public static final String PROPERTY_PROPERTY = "property";
	private Cardinality cardinality;
	// The Source or Target Edge Info
	private GraphLabel property;
	// The Complete Edge Info
	private GraphLabel info;
	private Association other;
	private GraphEdgeTypes typ = GraphEdgeTypes.EDGE;

	public Association with(Clazz node, Cardinality cardinality, String property) {
		with(node);
		with(cardinality);
		with(property);
		return this;
	}

	public Cardinality getCardinality() {
		if(cardinality != null) {
			return cardinality;
		}
		if(children.size() > 1){
			return Cardinality.MANY;
		}
		return Cardinality.ONE;
	}
	
	public String getCardinalityValue() {
		return cardinality.getValue();
	}
	
	public String getCardinalityText() {
		return property + "<br>0.." + this.cardinality;
	}

	public String getProperty() {
		if(property != null) {
			return property.getName();
		}
		if(children.size() == 1) {
			GraphMember item = children.get(0);
			if(item instanceof Clazz) {
				String className = ((Clazz)item).getName(true);
				if(className != null) {
					return className.toLowerCase();
				}
			}
			
		}
		return null;
	}

	public Association with(String value) {
		this.property = GraphLabel.create(value);
		return this;
	}
	
	public Association withInfo(String value) {
		if(info != null) {
			this.info.with(value);
			return this;
		}
		this.info = GraphLabel.create(value);
		return this;
	}
	
	public Association withInfo(GraphLabel value) {
		this.info = value;
		return this;
	}
	
	public Association withStyle(String value) {
		if(info != null) {
			this.info.withStyle(value);
			return this;
		}
		this.info = new GraphLabel().withStyle(value);
		return this;
	}
	
	public GraphLabel getInfo() {
		return info;
	}

	public Association with(GraphEntity... values) {
		if (values == null) {
			return this;
		}
		for (GraphEntity value : values) {
			if(this.children.add(value) ) {
				value.with(this);
			}
		}
		return this;
	}

	Association withOtherEdge(Association value) {
		this.other = value;
		return this;
	}

	public Association with(Association value) {
		if (this.getOther() == value) {
			return this;
		}
		if(this.other != null) {
			this.other.withOtherEdge(null);
		}
		this.other = value;
		getOther().with(this);
		return this;
	}

	public Association with(Cardinality cardinality) {
		this.cardinality = cardinality;
		return this;
	}
	
	public Association getOther() {
		return other;
	}
	
	public Clazz getOtherClazz() {
		if(other.getNode() instanceof Clazz) {
			return (Clazz) other.getNode();
		}
		return null;
	}

	public static Association create(GraphEntity source, GraphEntity target){
		Association edge = new Association().with(source);
		edge.with(new Association().with(target));
		return edge;
	}

	public GraphEdgeTypes getTyp() {
		return typ;
	}
	
	public String getSeperator() {
		if (getTyp() == GraphEdgeTypes.CHILD) {
			return "-|>";
		}
		if (getOther().getTyp() == GraphEdgeTypes.EDGE) {
			return "->";
		}
		return "-";
	}

	public Association withTyp(GraphEdgeTypes typ) {
		this.typ = typ;
		return this;
	}

	public GraphEntity getNode() {
		if(children.size()>0) {
			GraphMember item = children.get(0);
			if(item instanceof GraphEntity) {
				return (GraphEntity)item;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return getIds()+getSeperator()+getOther().getIds();
	}
	
	GraphSimpleSet<GraphEntity> getNodes() {
		GraphSimpleSet<GraphEntity> values = new GraphSimpleSet<GraphEntity>(); 
		for(GraphMember item : children) {
			if(item instanceof GraphEntity) {
				values.add((GraphEntity)item);
			}
		}
		return values;
	}

	public String getIds() {
		StringBuilder sb=new StringBuilder();
		if(children.size()>1) {
			sb.append("[");
			sb.append(children.get(0).getName());
			for(int i=1;i<children.size();i++) {
				sb.append(","+children.get(1).getName());
			}
			sb.append("]");
		}else if(children.size()>0) {
			sb.append(children.get(0).getName());
		}else{
			sb.append("[]");
		}
		
		return sb.toString();
	}

	public boolean contains(GraphEntity key) {
		return children.contains(key);
	}
	
	public boolean containsOther(GraphEntity key) {
		if(other != null) {
			return other.getChildren().contains(key);
		}
		return false;
	}

	public boolean containsAll(Association others, boolean both) {
		if(! children.containsAll(others.getChildren()) ) {
			return false;
		}
		if(getOther()!= null && both) {
			return getOther().containsAll(others.getOther(), false);
		}
		return true;
	}
}
