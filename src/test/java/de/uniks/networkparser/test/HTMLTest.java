package de.uniks.networkparser.test;

import org.junit.Assert;
import org.junit.Test;
import de.uniks.networkparser.EntityUtil;
import de.uniks.networkparser.GUILine;
import de.uniks.networkparser.Style;
import de.uniks.networkparser.interfaces.GUIPosition;
import de.uniks.networkparser.xml.HTMLEntity;

public class HTMLTest {

	@Test
	public void testHTML(){
		EntityUtil html = new EntityUtil();
		String txt = "Stefan <Test>";
		String encode = html.encode(txt);
		Assert.assertEquals("Stefan &lt;Test&gt;", encode);

		Assert.assertEquals(txt, html.decode(encode));
	}
	@Test
	public void testSimpleHTMLFile(){
		HTMLEntity file=new HTMLEntity();
		file.withText("Hallo Welt");
		Style style = new Style().withBorder(GUIPosition.ALL, new GUILine().withColor("black").withWidth("1px"));
		style.toString();
		file.addStyle("Table", ".Table{border:1px solid black}");
		file.addStyle("Table", ".div{border:1px solid black}");
		file.withNewLine();
		file.withText("Second Line");
		Assert.assertNotNull(file.toString());
//		System.out.println(file.toString());
	}

}
