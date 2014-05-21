package de.uniks.networkparser.gui.controls;

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
import java.time.LocalDate;
import java.util.GregorianCalendar;

import javafx.scene.control.DatePicker;
import de.uniks.networkparser.gui.table.Column;
import de.uniks.networkparser.gui.table.FieldTyp;

public class DateTimeEditControl extends EditControl<DatePicker>{
	@Override
	public Object getValue(boolean convert) {
		LocalDate date = this.control.getValue();
		GregorianCalendar calendar=new GregorianCalendar();
		calendar.set(GregorianCalendar.YEAR, date.getYear());
		calendar.set(GregorianCalendar.MONTH, date.getMonth().getValue());
		calendar.set(GregorianCalendar.DAY_OF_MONTH, date.getDayOfMonth());
		return calendar.getTime();
//		return date;
	}

	@Override
	public FieldTyp getControllForTyp(Object value) {
		return FieldTyp.DATE;
	}

	@Override
	public DateTimeEditControl withValue(Object value) {
		if(value instanceof LocalDate){
			getControl().setValue((LocalDate)value);
		}
		return this;
	}

	@Override
	public DatePicker createControl(Column column) {
		return new DatePicker();
	}
}
