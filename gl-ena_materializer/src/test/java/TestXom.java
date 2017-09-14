/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;

/**
 * File: TestXom.java
 * Created by: dstaines
 * Created on: Feb 11, 2010
 * CVS:  $$
 */

/**
 * @author dstaines
 * 
 */
public class TestXom {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Builder parser = new Builder();
			Document doc = parser.build(new BufferedReader(new FileReader(
					"/scratch/U00096.xml")));
			Nodes query = doc.query("//feature");
			for (int i = 0; i < query.size(); i++) {
				Element f = (Element) query.get(i);
				for(int j=0; j<f.getAttributeCount(); j++) {
					Attribute attr = f.getAttribute(j);
					System.out.println(f);
				}
			}
		} catch (ParsingException ex) {
			System.err
					.println("Cafe con Leche is malformed today. How embarrassing!");
		} catch (IOException ex) {
			System.err
					.println("Could not connect to Cafe con Leche. The site may be down.");
		}
	}

}
