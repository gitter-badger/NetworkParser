package de.uniks.networkparser.test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.graph.Cardinality;
import de.uniks.networkparser.graph.Clazz;
import de.uniks.networkparser.graph.DataType;
import de.uniks.networkparser.graph.GraphList;
import de.uniks.networkparser.xml.EMFTokener;
import de.uniks.networkparser.xml.XMLEntity;

public class EMFTest extends IOClasses{

	@Test
	public void testEMF() {
//		StringBuffer value = readFile("testcase4-in.petrinet");
//		EMFIdMap map=new EMFIdMap();
//		Object decode = map.decode(value.toString());
//		out.println(decode);
	}

	@Test
	public void testEMFDecode() {
		StringBuffer value = readFile("railway.ecore");
		Object model = new IdMap().decodeEMF(value.toString());
		GraphList list = (GraphList) model;
		Assert.assertEquals(9, list.getClazzes().size());
		Assert.assertEquals("[Segment|length:int]-^[TrackElement],[TrackElement]-^[RailwayElement|id:int],[TrackElement]^-[Switch|currentPosition:Position],[TrackElement]->[Sensor],[TrackElement]<-[TrackElement],[Switch]->[SwitchPosition|position:Position],[Route]-^[RailwayElement],[Route]->[Semaphore|signal:Signal],[Route]->[SwitchPosition],[Route]->[Semaphore],[Route]->[Sensor],[Route]<-[RailwayContainer],[Semaphore]-^[RailwayElement],[Semaphore]<-[RailwayContainer],[SwitchPosition]-^[RailwayElement],[RailwayElement]^-[Sensor],[RailwayElement]<-[RailwayContainer]", model.toString());
	}

	@Test
	public void testEMFTTC2014() throws FileNotFoundException {
		IdMap map=new IdMap();
		StringBuffer value = readFile("imdb.movies");
		ArrayList<?> decode = (ArrayList<?>) map.decodeEMF(value.toString());
		Assert.assertEquals(0, decode.size());
	}

	@Test
	public void testXMITOEMF() throws FileNotFoundException {
		IdMap map=new IdMap();
		StringBuffer value = readFile("imdb.movies");
		GraphList decode = new GraphList();
		map.decodeEMF(value.toString(), decode);
		Assert.assertEquals(3, decode.getClazzes().size());
	}

	@Test
	public void testWriteEMF() {
		IdMap map=new IdMap();
		GraphList list=new GraphList();
		Clazz uni = list.createClazz("University");
		Clazz student = list.createClazz("Student");
		student.withAttribute("semester", DataType.INT);
		student.withAttribute("name", DataType.STRING);
		uni.withBidirectional(student, "student", Cardinality.MANY, "university", Cardinality.ONE);
		XMLEntity item = (XMLEntity) map.encode(list, new EMFTokener());

		XMLEntity root =(XMLEntity) item.getChildren().first();
		StringBuilder sb=new StringBuilder();
		sb.append("<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" name=\"model\" nsURI=\"http:///model.ecore\" nsPrefix=\"model\">"+
				"<eClassifiers xsi:type=\"ecore:EClass\" name=\"University\">"+
				"<eReferences name=\"student\" eType=\"#//Student\" eOpposite=\"#//Student/university\" upperBound=\"1\"/>"+
				"</eClassifiers>"+
	    		"<eClassifiers xsi:type=\"ecore:EClass\" name=\"Student\">"+
	    		"<eAttributes name=\"semester\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>"+
	    		"<eAttributes name=\"name\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"+
	    		"<eReferences name=\"university\" eType=\"#//University\" eOpposite=\"#//University/student\" upperBound=\"-1\"/>"+
	    		"</eClassifiers>"+
	    		"</ecore:EPackage>");

		Assert.assertEquals(sb.toString(), root.toString());
	}

}
