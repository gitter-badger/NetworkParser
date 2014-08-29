package de.uniks.networkparser.test;

import org.junit.Test;

import de.uniks.networkparser.json.JsonIdMap;
import de.uniks.networkparser.json.JsonObject;
import de.uniks.networkparser.test.model.ListEntity;

public class JsonRekursivTest {
	 @Test
	 public void testSimple(){
		 ListEntity root= new ListEntity();
		 ListEntity test = new ListEntity();
		 ListEntity child = new ListEntity().withOwner(new ListEntity().withOwner(test)); 
		 root.withChildren(new ListEntity(), child);
		 root.withChildren(child);
		 child.withChildren(root);
		 JsonIdMap map= new JsonIdMap();
		 map.withCreator(new ListEntity());
		 JsonObject json =map.encode(root);
		 System.out.println(json.toString(2));
		 
		 JsonIdMap mapDecode= new JsonIdMap();
		 mapDecode.withCreator(new ListEntity());
		 Object rootDecode = mapDecode.decode(json);
		 System.out.println(rootDecode);
		 
	 }
}
