package de.uniks.networkparser.gui.form;

/*
 Json Id Serialisierung Map
 Copyright (c) 2011 - 2013, Stefan Lindel
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 3. All advertising materials mentioning features or use of this software
 must display the following acknowledgement:
 This product includes software developed by Stefan Lindel.
 4. Neither the name of contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THE SOFTWARE 'AS IS' IS PROVIDED BY STEFAN LINDEL ''AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEFAN LINDEL BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.TextItems;
import de.uniks.networkparser.gui.controls.EditControl;
import de.uniks.networkparser.gui.controls.EditFieldMap;
import de.uniks.networkparser.gui.table.CellEditorElement;
import de.uniks.networkparser.gui.table.Column;
import de.uniks.networkparser.gui.table.FieldTyp;
import de.uniks.networkparser.interfaces.GUIPosition;
import de.uniks.networkparser.interfaces.SendableEntity;
import de.uniks.networkparser.interfaces.SendableEntityCreator;


public class PropertyComposite extends HBox implements PropertyChangeListener, CellEditorElement{
	private Label westLabel;
	private Node centerComposite;
	private Label eastLabel;
	private GUIPosition labelOrientation=GUIPosition.WEST;
	private String labelPostText=": ";
	private Object item;
	private Column column;
	private SendableEntityCreator creator;
	private EditFieldMap field=new EditFieldMap();
	private EditControl<?> editControl;

	public String getLabelText() {
		if(getColumn().getLabel()!= null){
			return getColumn().getLabel();
		}
		return getColumn().getAttrName();
	}

	public PropertyComposite withLabelText(String value) {
		getColumn().withLabel(value);
		withDataBinding();
		return this;
	}

	public PropertyComposite withLabel(String value) {
		if(value != null){
			if(field.getMap()!=null){
				TextItems textClazz = (TextItems) field.getMap().getCreator(TextItems.class.getName(), true);
				if(textClazz !=null){
					getColumn().withLabel(textClazz.getText(value, item, this));
				}
			}else{
				getColumn().withLabel(value);
			}
		}
		withDataBinding();
		return this;
	}
	
	

	public PropertyComposite withFieldTyp(FieldTyp value) {
		getColumn().withFieldTyp(value);
		editControl = this.field.getControl(null, column, getItemValue(), this);
		 editControl.withValue(getItemValue());
		 if(this.centerComposite != null) {
			 this.getChildren().remove(this.centerComposite);
		 }
		 this.centerComposite = editControl.getControl(); 
		this.getChildren().add(1, this.centerComposite);
		return this;
	}
	
	public PropertyComposite withFieldType(FieldTyp type){
		getColumn().withFieldTyp(type);
		return this;
	}

	 private PropertyComposite withDataBinding() {
		 initLabel();
		 editControl.withValue(getItemValue() );
		 if(item instanceof SendableEntity) {
			 ((SendableEntity)item).addPropertyChangeListener(getColumn().getAttrName(), this);
		 }
		 return this;
	 }
	
	 public PropertyComposite withDataBinding(IdMap map, Object item, Column column) {
		 this.item = item;
		 this.column =  column;
		 this.field.withMap(map);
		 if(map!=null){
			 this.creator = map.getCreatorClass(item);
		 }
		 return withDataBinding();
	 }
	 
	 public void initLabel() {
		 if(westLabel==null){
			westLabel = new Label();
			westLabel.setPadding(new Insets(3, 0, 0, 0));
			this.getChildren().add(westLabel);
		}
		 if(this.centerComposite==null){
			 editControl = this.field.getControl(null, column, getItemValue(), this);
			 editControl.withValue(getItemValue());
			 this.centerComposite = editControl.getControl(); 
			this.getChildren().add(1, centerComposite);
		 }
		if(GUIPosition.WEST.equals(labelOrientation)){
			
			westLabel.setVisible(true);
			if(eastLabel!=null){
				eastLabel.setVisible(false);
			}
			String labelText = getLabelText();
			if(labelText!=null){
				westLabel.setText(labelText+labelPostText);
			}
		}else if(GUIPosition.EAST.equals(labelOrientation)){
			if(eastLabel==null){
				eastLabel = new Label();
				this.getChildren().add(2, eastLabel);
			}
			if(westLabel!=null){
				westLabel.setVisible(false);
			}
			String labelText = getLabelText();
			if(labelText != null){
				eastLabel.setText(labelText);
			}
		}else{
			if(westLabel!=null){
				westLabel.setVisible(false);
			}
			if(eastLabel!=null){
				eastLabel.setVisible(false);
			}
		}
	}
	 
	 public Object getItemValue(){
		 if(creator!=null && getColumn().getAttrName() != null){
			 return creator.getValue(item, getColumn().getAttrName());
		 }
		 return null;
	}
	 
	public void reload() {
		editControl.withValue( getItemValue() );
	}

	public void save() {
		creator.setValue(item, getColumn().getAttrName(), editControl.getValue(true), IdMap.UPDATE);
	}
	
	public Label getLabelControl(){
		if(labelOrientation==null){
		}else if(labelOrientation.equals( GUIPosition.WEST)){
			return westLabel;
		}else if(labelOrientation.equals(GUIPosition.EAST)){
			return eastLabel;
		}
		return null;
	}
	
	public double getLabelWidth(){
		Text text = new Text(getLabelControl().getText() );
		text.applyCss(); 

	    return text.getLayoutBounds().getWidth();
	}
	
	public void setLabelLength(double width){
		Label control = getLabelControl();
		if(control!=null){
			control.setMinWidth(width);
		}
	}
	
	@Override
	public void dispose() {
		if(item instanceof SendableEntity) {
			((SendableEntity) item).removePropertyChangeListener(this);
		}
	}
	
	@Override
	public boolean setFocus(boolean value) {
		if(field!=null){
			return field.setFocus(value);
		}
		return false;
	}
	
	@Override
	public void relocate(double x, double y) {
		super.relocate(x, y);
		withDataBinding();
	}
	
	@Override
	public PropertyComposite withColumn(Column value) {
		this.column = value;
		return this;
	}
	
	public Column getColumn() {
		if(this.column==null){
			this.column = new Column();
		}
		return column;
	}
	
	public PropertyComposite withLabelOrientation(GUIPosition position) {
		this.labelOrientation = position;
		return this;
	}
	
	@Override
	public CellEditorElement withValue(Object value) {
		editControl.withValue( value );	
		return this;
	}
	
	public EditControl<?> getEditControl(){
		return editControl;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onActive(boolean value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean nextFocus() {
//FIXME		this.owner.focusnext();
		return false;
	}

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName()!=null){
			if(evt.getPropertyName().equals(getColumn().getAttrName())){
				// Test Thread and restarten
	//			field.setValue(evt.getNewValue(), false);
			}
		}
	}

	@Override
	public Object getValue(boolean convert) {
		return editControl.getValue(convert);
	}


	//FIXME
//	public String getLabelPostText() {
//		return labelPostText;
//	}
//
//
//	public void setLabelPostText(String value) {
//		this.labelPostText = value;
//	}
//
//	public void handleDefaultSelection(SelectionEvent event) {
////		field.dispose();
//	}
//
//	@Override
//	public Object getEditorValue(boolean convert) throws ParseException {
//		return field.getEditorValue(convert); 
//	}
//	
//	public Control getEditorField(){
//		return field.getControl();
//	}
//	
//	public void setFormatLayout(String value){
//		field.setNumberFormat(value);
//	}
//	public String getFormatLayout(){
//		return field.getNumberFormat();
//	}
//	
//	public void addChoiceList(Object value){
//		field.addChoiceList(value);
//	}
//	
//	public String toString(){
//		if(column!=null){
//			return field.getNumberFormat()+" "+column.getLabel()+":"+column.getAttrName();
//		}
//		return super.toString();
//	}
//
//	@Override
//	public void focusGained(FocusEvent e) {
//		for(EventListener listener : listeners){
//			if(listener instanceof FocusListener){
//				((FocusListener)listener).focusGained(e);
//			}
//		}
//		if (this.getParent() instanceof ModelForm) {
//			ModelForm parent = (ModelForm) this.getParent();
//			parent.onFocus(this);
//		}
//	}
//
//	@Override
//	public void focusLost(FocusEvent e) {
//		for(EventListener listener : listeners){
//			if(listener instanceof FocusListener){
//				((FocusListener)listener).focusLost(e);
//			}
//		}
//	}
//
//	@Override
//	public void keyPressed(KeyEvent e) {
//		for(EventListener listener : listeners){
//			if(listener instanceof KeyListener){
//				((KeyListener)listener).keyPressed(e);
//			}
//		}
//		if (this.getParent() instanceof ModelForm) {
//			ModelForm parent = (ModelForm) this.getParent();
//			parent.onKeyPressed(e);
//		}
//	}
//
//	@Override
//	public void keyReleased(KeyEvent e) {
//		for(EventListener listener : listeners){
//			if(listener instanceof KeyListener){
//				((KeyListener)listener).keyReleased(e);
//			}
//		}
//		if (this.getParent() instanceof ModelForm) {
//			ModelForm parent = (ModelForm) this.getParent();
//			parent.onKeyReleased(e);
//		}
//	}
//	
//	public void addFieldListener(EventListener listener) {
//		this.listeners.add(listener);
//	}
//
//	@Override
//	public void keyTraversed(TraverseEvent e) {
//		if (this.getParent() instanceof ModelForm) {
//			ModelForm parent = (ModelForm) this.getParent();
//			parent.onKeyTraversed(e);
//		}
//	}
//
}
