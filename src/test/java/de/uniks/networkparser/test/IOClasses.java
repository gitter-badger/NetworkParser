package de.uniks.networkparser.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class IOClasses {
public static String CRLF="\r\n";
	public String getAbsolutePath(String file){
		file = "test/"+file;
		String path = IOClasses.class.getResource("IOClasses.class").getPath();
		
		int pos = path.lastIndexOf("bin/");
		if(pos>0){
			path = path.substring(0, pos)+"src/test/resources/" ;
		}else{
			pos = path.lastIndexOf("build/classes");
			if(pos>0){
				path = path.substring(0, pos + 6)+"resources/test/";
			}
		}
		return path+file;
	}
	public StringBuffer readFile(String file){
		BufferedReader bufferedReader;
		String fullPath = getAbsolutePath(file);
		try {
			bufferedReader = new BufferedReader(new FileReader(fullPath));
			StringBuffer indexText = new StringBuffer();
			String line = bufferedReader.readLine();
			while (line != null)
			{
				indexText.append(line).append(CRLF);
				line = bufferedReader.readLine();
			}
			
			bufferedReader.close();
			return indexText;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return null;
	}
}
