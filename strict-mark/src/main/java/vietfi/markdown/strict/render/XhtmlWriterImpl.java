/*
 * Copyright 2024, Thach-Anh Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vietfi.markdown.strict.render;

import java.nio.CharBuffer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.SMDXhtmlWriter;

public class XhtmlWriterImpl implements SMDXhtmlWriter {
	
	public static final String XHTML_BLOCKQUOTE_TAG = "blockquote";
	public static final String XHTML_PARA_TAG = "p";
	public static final String XHTML_NEW_LINE = "br";
	public static final String XHTML_PRE_TAG_NAME = "pre";	
	public static final String XHTML_PRE_LANGUAGUE_PREFIX = "language-";
	
	public static final String XHTML_UL_TAG = "ul";
	public static final String XHTML_OL_TAG = "ol";
	public static final String XHTML_LI_TAG = "li";
	
	public static final String[] XHTML_HEADINGS_TAG = {"h1","h2","h3","h4","h5","h6",};
	
	private final static String XHTML_HR = "hr";
	private final static String XHTML_UNDERSCORE_HR = "underscore-line";
	private final static String XHTML_DOUBLE_HR = "double-line";
	private static final String XHTML_STRIKETHROUGH = "s";
	private static final String XHTML_BOLD = "b";
	private static final String XHTML_ITALIC = "i";
	private static final String XHTML_UNDERLINE = "u";
	private static final String XHTML_CODE = "code";
	private static final String XHTML_A_TAG = "a";
	private static final String XHTML_IMG_TAG = "img";

	private final static String XHTML_CLASS_ATTR = "class";
	
	protected String pClass;
	protected String linkClass;
	protected String imgClass;
	protected String codeClass;
	protected String preCodeClass;
	protected String blockquoteClass;
	protected String ulClass;
	protected String olClass;
	protected String liClass;

	@Override
	public void setClassNameForTag(String className, int classForTag) {
		if(className.isBlank())
			className = null;
		
		switch(classForTag) {
		case CLASS_FOR_PARAGRAPH:
			this.pClass = className;
			break;
		case CLASS_FOR_LINK:
			this.linkClass = className;
			break;
		case CLASS_FOR_IMG:
			this.imgClass = className;
			break;
		case CLASS_FOR_INLINE_CODE:
			this.codeClass = className;
			break;
		case CLASS_FOR_PRE_CODE:
			this.preCodeClass = className;
			break;
		case CLASS_FOR_BLOCKQUOTE:
			this.blockquoteClass = className;
			break;
		case CLASS_FOR_UL:
			this.ulClass = className;
			break;
		case CLASS_FOR_OL:
			this.olClass = className;
			break;
		case CLASS_FOR_LI:
			this.liClass = className;
			break;
		};
	}
	
	private char[] myArray = null;
	StringBuilder sb = new StringBuilder(1024);
	StringBuilder lastText = new StringBuilder(128);
    StringBuilder lastUrl = new StringBuilder(128);
    
	@Override
	public void writeXhtml(SMDMarkers markers, CharBuffer buffer, XMLStreamWriter xmlWriter) throws XMLStreamException {

		if(markers.isEmpty() && buffer.position() > 0) {
			//whole buffer is the line of paragraph
			if(buffer.hasArray())
				xmlWriter.writeCharacters(buffer.array(), 0, buffer.position());
			else {
				if(myArray == null || myArray.length < buffer.position())
					myArray = new char[buffer.capacity()];
				for(int j = 0; j < buffer.position(); j++)
					myArray[j] = buffer.get(j);
				xmlWriter.writeCharacters(myArray, 0, buffer.position());
			}
			return;
		}
		
		//reset buffer.
		sb.setLength(0);
		lastText.setLength(0);
		lastUrl.setLength(0);
		
        boolean isInLinkText = false;
    	boolean isInUrl = false;
    	
        while(markers.cursorIsAvailable()) {
        	boolean isMarkerStop = markers.cursorIsMarkerStop();
        	int contentBegin = markers.cursorPosition1();
			if(!isMarkerStop && contentBegin >= buffer.position()) //exceed the output point
				break;
			
        	int state = markers.cursorState(); //extract state
        	
        	isInLinkText = false;
        	isInUrl = false;
        	
        	if(markers.cursorIsMarkerStart()) {
        		if(sb.length() > 0) {
        			xmlWriter.writeCharacters(sb.toString());
        			sb.setLength(0);
        		}
        		//start marker
        		switch(state) {
	        		case SMDParser.STATE_STRIKETHROUGH:
	        			xmlWriter.writeStartElement(XHTML_STRIKETHROUGH);
	        			break;
	        		case SMDParser.STATE_BOLD:
	        			xmlWriter.writeStartElement(XHTML_BOLD);
	        			break;
	        		case SMDParser.STATE_ITALIC:
	        			xmlWriter.writeStartElement(XHTML_ITALIC);
	        			break;
	        		case SMDParser.STATE_UNDERLINE:
	        			xmlWriter.writeStartElement(XHTML_UNDERLINE);
	        			break;
	        		case SMDParser.STATE_INLINE_CODE:
	        			xmlWriter.writeStartElement(XHTML_CODE);
	        			if(codeClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, codeClass);
	        			break;
	        		case SMDParser.STATE_LINK:
	        			xmlWriter.writeStartElement(XHTML_A_TAG);
	        			if(linkClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, linkClass);
	        			break;
	        		case SMDParser.STATE_IMAGE:
	        			xmlWriter.writeStartElement(XHTML_IMG_TAG);
	        			if(imgClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, imgClass);
	        			break;
	        		case SMDParser.STATE_CODE_BLOCK:
	        			xmlWriter.writeStartElement(XHTML_PRE_TAG_NAME);
	        			if(preCodeClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, preCodeClass);
	        			break;
	        		case SMDParser.STATE_QUOTE_BLOCK:
	        			xmlWriter.writeStartElement(XHTML_BLOCKQUOTE_TAG);
	        			if(blockquoteClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, blockquoteClass);
	        			break;
	        		
	        		case SMDParser.STATE_PARAGRAPH:
	        			xmlWriter.writeStartElement(XHTML_PARA_TAG);
	        			if(pClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, pClass);
	        			break;
	        			
	        		case SMDParser.STATE_NEW_LINE:
	        			xmlWriter.writeEmptyElement(XHTML_NEW_LINE);
	        			break;
	        		case SMDParser.STATE_ORDERED_LIST:
	        			xmlWriter.writeStartElement(XHTML_OL_TAG);
	        			if(olClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, olClass);
	        			break;
	        		case SMDParser.STATE_UNORDERED_LIST:
	        			xmlWriter.writeStartElement(XHTML_UL_TAG);
	        			if(ulClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, ulClass);
	        			break;
	        		case SMDParser.STATE_LIST_ITEM:
	        			xmlWriter.writeStartElement(XHTML_LI_TAG);
	        			if(liClass != null)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, liClass);
	        			break;
	        			
	        		case SMDParser.STATE_HEADING_1:
	        		case SMDParser.STATE_HEADING_2:
	        		case SMDParser.STATE_HEADING_3:
	        		case SMDParser.STATE_HEADING_4:
	        		case SMDParser.STATE_HEADING_5:
	        		case SMDParser.STATE_HEADING_6:
	        			xmlWriter.writeStartElement(XHTML_HEADINGS_TAG[state - SMDParser.STATE_HEADING_1]);
	        			break;
	        			
	        		case SMDParser.STATE_HORIZONTAL:
	        		case SMDParser.STATE_HORIZONTAL_D:
	        		case SMDParser.STATE_HORIZONTAL_U:
	        			xmlWriter.writeEmptyElement(XHTML_HR);
	        		
	        			if(state == SMDParser.STATE_HORIZONTAL_D)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, XHTML_DOUBLE_HR);
	        			else if(state == SMDParser.STATE_HORIZONTAL_U)
	        				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, XHTML_UNDERSCORE_HR);
	        			break;
        		}
        	}
        	//stop
        	else if(isMarkerStop) { //MARKER_STOP
        		//stop marker
        		switch(state) {
        		case SMDParser.STATE_STRIKETHROUGH:
        		case SMDParser.STATE_BOLD:
        		case SMDParser.STATE_ITALIC:
        		case SMDParser.STATE_UNDERLINE:
        		case SMDParser.STATE_INLINE_CODE:
        		case SMDParser.STATE_IMAGE:
        		case SMDParser.STATE_CODE_BLOCK:
        		case SMDParser.STATE_QUOTE_BLOCK:
        		case SMDParser.STATE_PARAGRAPH:
        		case SMDParser.STATE_ORDERED_LIST:
        		case SMDParser.STATE_UNORDERED_LIST:
        		case SMDParser.STATE_LIST_ITEM:
        		case SMDParser.STATE_HEADING_1:
        		case SMDParser.STATE_HEADING_2:
        		case SMDParser.STATE_HEADING_3:
        		case SMDParser.STATE_HEADING_4:
        		case SMDParser.STATE_HEADING_5:
        		case SMDParser.STATE_HEADING_6:
        			xmlWriter.writeEndElement();
        			break;
        		case SMDParser.STATE_LINK:
        			if(lastText.length() > 0) {
        				xmlWriter.writeCharacters(lastText.toString());
        				lastText.setLength(0);
        			}
        			else {
        				xmlWriter.writeCharacters(lastUrl.toString());
        			}
        			xmlWriter.writeEndElement();
        			break;
        			
	    		}
        	}
        	
        	if(markers.cursorIsContentStart()) {
        		switch(state) {
        		case SMDParser.STATE_URL: //state URL content is differ
        		case SMDParser.STATE_IMAGE_SRC:
        			isInUrl = true;
        			/*if(lastUrl == null)
        				lastUrl = new StringBuilder();
        			else*/
        				lastUrl.setLength(0);
        			break;
        		case SMDParser.STATE_IMAGE:
        		case SMDParser.STATE_LINK:
        			isInLinkText = true;
        			/*if(lastText == null)
        				lastText = new StringBuilder();
        			else*/
        				lastText.setLength(0);
        			break;
        		}
        	}
        	else if(markers.cursorIsContentStop()) { //End of content
        		switch(state) {
        		case SMDParser.STATE_NONE:
        			break;
        		case SMDParser.STATE_URL: //state URL content is differ
        			xmlWriter.writeAttribute("href", lastUrl.toString());
        			break;
        		case SMDParser.STATE_IMAGE:
        			xmlWriter.writeAttribute("alt", lastText.toString());
        			break;
        		case SMDParser.STATE_IMAGE_SRC:
        			xmlWriter.writeAttribute("src", lastUrl.toString());
        			break;
        		case SMDParser.STATE_CODE_LANGUAGE:
    				xmlWriter.writeAttribute(XHTML_CLASS_ATTR, 
    					XHTML_PRE_LANGUAGUE_PREFIX+sb.toString());
    				sb.setLength(0);
    				break;
    			default:
					if(sb.length() > 0) {
        				xmlWriter.writeCharacters(sb.toString());
        				sb.setLength(0);
        			}
        			break;
        		}
        	}
        	
        	int contentEnd = Math.min(markers.cursorPosition2(), buffer.position());
			
			if (contentBegin >= 0 && contentBegin < contentEnd) {
				if(buffer.hasArray()) {
					if(isInLinkText) {
						lastText.append(buffer.array(), contentBegin, contentEnd - contentBegin);
					}
					else if(isInUrl) {//copy to URL
						lastUrl.append(buffer.array(), contentBegin, contentEnd - contentBegin);
	    			}
					else {
						sb.append(buffer.array(), contentBegin, contentEnd - contentBegin);
					}
				}
				else {
					//copy char by char
					if(isInLinkText) {
						for(int j = contentBegin; j < contentEnd; j++)
							lastText.append(buffer.get(j));
					}
					else if(isInUrl) {//copy to URL
						for(int j = contentBegin; j < contentEnd; j++)
							lastUrl.append(buffer.get(j));
	    			}
					else {
						for(int j = contentBegin; j < contentEnd; j++)
							sb.append(buffer.get(j));
					}
				}

			}
			
        	markers.cursorGoNext();
        }
        if(sb.length() > 0)
        	xmlWriter.writeCharacters(sb.toString());
	}

}
