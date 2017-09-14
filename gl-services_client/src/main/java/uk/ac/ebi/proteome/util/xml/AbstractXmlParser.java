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

/**
 * CVS:  $Id$
 * File: AbstractXmlParser.java
 * Created by: mhaimel
 * Created on: 30 Mar 2007
 */
package uk.ac.ebi.proteome.util.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;

/**
 * @author mhaimel
 *
 */
public abstract class AbstractXmlParser<T> {
	
	private Map<String, AbstractXmlParser<?>> queue;
	private XMLEventReader reader;
	private XMLEvent event;
	protected T resultValue;
	protected final String element;
	
	public AbstractXmlParser(String element) {
		super();
		queue = new HashMap<String,AbstractXmlParser<?>>();	
		this.element = element;
	}
	
	public void addEvent(String key, AbstractXmlParser<?> parser){
		this.queue.put(key, parser);
	}
	
	public void addEvent(String key){
		this.addEvent(key,null);
	}
	
	protected XMLEvent nextEvent() throws XMLStreamException {
		this.event = this.reader.nextEvent();
		return this.getEvent();
	}
	
	
	public T parse(XMLEvent event, XMLEventReader reader) throws XMLStreamException{
		this.reset();
		this.reader = reader;
		this.event = event;
		if(this.getEvent().isStartElement() 
				&& isName(this.getEvent().asStartElement(), this.element)){
			this.myStartElementEvent(this.getEvent(), this.getReader());
		}
		boolean endElement = false;
		while(!endElement && this.getReader().hasNext()){
			this.nextEvent();
			switch (this.getEvent().getEventType()) {
	            case XMLStreamConstants.START_ELEMENT:
	            	for(Entry<String, AbstractXmlParser<?>> entry : this.queue.entrySet()){
	            		if(this.getEvent().isStartElement() 
	            				&& isName(this.getEvent().asStartElement(),entry.getKey())){
	            			this.handleStartElementEvent(entry.getKey(), entry.getValue());
	            		}
	            	}
	            	if(this.getEvent().isStartElement() 
	            			&& isName(this.getEvent().asStartElement(), element)){
	            		this.myStartElementEvent(this.getEvent(), this.getReader());
	            	}
	        		break;
	            case XMLStreamConstants.END_ELEMENT:
	            	for(Entry<String, AbstractXmlParser<?>> entry : this.queue.entrySet()){
	            		if(this.getEvent().isEndElement() 
	            				&& isName(this.getEvent().asEndElement(),entry.getKey())){
	            			this.handleEndElementEvent(entry.getKey(), entry.getValue());
	            		}
	            	}
	            	if(this.getEvent().isEndElement() 
            				&& isName(this.getEvent().asEndElement(), this.element)){
	            		endElement = true;
	            	}
	        		break;
            }
		}	
		return resultValue;
	}
	

	protected String getExpectedCharacters(XMLEvent event) {
		if(event.isCharacters()){
			return event.asCharacters().getData();
		} else{
			return StringUtils.EMPTY;
		}
	}
	
	protected void myStartElementEvent(XMLEvent e, XMLEventReader r) {
//		 do nothing
	}

	protected abstract void reset();

	
	protected void handleStartElementEvent(String key, AbstractXmlParser<?> value)  throws XMLStreamException{
//		 do nothing
	}
	
	protected void handleEndElementEvent(String key, AbstractXmlParser<?> value)  throws XMLStreamException{
//		 do nothing
	}

	private boolean isName(EndElement e, String name) {
		return e.getName().getLocalPart().equals(name);
	}

	private boolean isName(StartElement e, String name) {
		return e.getName().getLocalPart().equals(name);
	}
	
	protected void setReturnValue(T value){
		this.resultValue = value;
	}
	
	protected T getReturnValue(){
		return this.resultValue;
	}
	
	public XMLEvent getEvent() {
		return event;
	}

	public XMLEventReader getReader() {
		return reader;
	}
}
