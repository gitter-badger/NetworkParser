NetworkParser
=============

Framework for serialization from Java objects to Json, XML and Byte.


NetworkParser is a simple framework for serializing complex model structures. 
To do that it transforms a given model to an intermediate model which can be serialized. It also offers lots of filters.

For serialization you are three formats available: Json, XML and Byte. 
For deserialization you can use following formats: Json, XML, Byte and EMF.

The Framework have many other features like:
- Calculator
- Date with holidays
- UML-Layouting with Javascript or Webservice like YUML
- JavaFX Container Classes:
  - for DataBinding
  - Table with Searchfield
  - Form
  - PopupDialog
  - Basic Shell-Class with Writing Errorfiles
- Logicstructure
- SimpleList as universal solution for datamodels


## Current Status ##
![Build Status](https://se.cs.uni-kassel.de/jenkins/buildStatus/icon?job=NetworkParser)

# Getting Started

## Installation
$ git clone https://github.com/fujaba/NetworkParser.git

#Maven artifacts
Maven artifacts are available at:
- http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22NetworkParser%22 - release repository
- https://oss.sonatype.org/content/repositories/snapshots/com/github/fujaba/NetworkParser/ - snaphots repository

#Usage
Here are a simple Usage of JsonIdMap for serialization and deserialization and get UpdateMessages
```java
	House house=new House();
	house.setFloor(4);
	house.setName("University");
	JsonIdMap map=new JsonIdMap().withCreator(new House());
	map.withUpdateListenerSend(new UpdateListener() {
		@Override
		public boolean update(String typ, BaseItem source, Object target, String property, Object oldValue,
				Object newValue) {
			System.out.println(source);
			return false;
		}
	});
	
	JsonObject json = map.encode(house);
	String string=json.toString();
	
	JsonIdMap decodeMap=new JsonIdMap().withCreator(new House());
	House newHouse = (House) decodeMap.decode(string);

	house.setFloor(42);
```
- [SimpleJsonTest](src/test/java/de/uniks/networkparser/test/SimpleJsonTest.java "Sourcecode SimpleJsonTest.java")
- [House](src/test/java/de/uniks/networkparser/test/model/House.java "Sourcecode House.java")
## Links
- The issue list: Head straight to https://github.com/fujaba/NetworkParser/issues for a list of all issues or click `Issues` in the navigation bar on the right.
- See also on Openhub https://www.openhub.net/p/NetworkParser

# License
NetworkParser is released under an [EuPL 1.1 license](Licence/EUPL v.1.1 - Lizenz.pdf).