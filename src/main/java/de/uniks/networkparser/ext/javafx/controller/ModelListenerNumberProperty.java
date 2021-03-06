package de.uniks.networkparser.ext.javafx.controller;

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
import javafx.beans.Observable;
import de.uniks.networkparser.interfaces.SendableEntityCreator;

public class ModelListenerNumberProperty extends ModelListenerProperty<Number> {
	public ModelListenerNumberProperty(SendableEntityCreator creator, Object item, String property) {
		super(creator, item, property);
	}

	@Override
	public void invalidated(Observable arg0) {
	}

	@Override
	public Number getValue() {
		Object value = getItemValue();
		if(value instanceof Number){
			return (Number)value;
		}
		return Double.valueOf(""+value);
	}
}
