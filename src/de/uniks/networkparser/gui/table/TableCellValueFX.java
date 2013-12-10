package de.uniks.networkparser.gui.table;

/*
 NetworkParser
 Copyright (c) 2011 - 2013, Stefan Lindel
 All rights reserved.
 
 Licensed under the EUPL, Version 1.1 or � as soon they
 will be approved by the European Commission - subsequent
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
import javafx.beans.property.SimpleObjectProperty;
import de.uniks.networkparser.interfaces.SendableEntityCreator;

public class TableCellValueFX extends SimpleObjectProperty<TableCellValue> implements TableCellValue{
	private Column column;
	private SendableEntityCreator creator;
	private Object item;
	
	public TableCellValueFX withItem(Object item) {
		this.item = item;
		this.set(this);
		return this;
	}
	
	public TableCellValueFX withColumn(Column column) {
		this.column = column;
		return this;
	}
	
	
	public TableCellValueFX withCreator(
			SendableEntityCreator creator) {
		this.creator = creator;
		return this;
	}
	
	public Object getItem(){
		return item;
	}
	public Column getColumn() {
		return column;
	}

	public SendableEntityCreator getCreator() {
		return creator;
	}
	public String toString(){
		if(creator==null){
			return "";
		}
		return "" + this.column.getListener().getValue(item, creator);
	}

	@Override
	public Object getSimpleValue() {
//		Object value = getCreator().getValue(item, getColumn().getAttrName());
//		if(value instanceof String){
//			return new ModelListenerStringProperty(getCreator(), item, getColumn().getAttrName());
//		}else if(value instanceof Number){
//			return new ModelListenerNumberProperty(getCreator(), item, getColumn().getAttrName());
//		}
		return getCreator().getValue(item, getColumn().getAttrName());
	}
}
