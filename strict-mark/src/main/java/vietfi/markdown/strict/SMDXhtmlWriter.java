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
package vietfi.markdown.strict;

import java.nio.CharBuffer;
import java.util.function.Function;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface SMDXhtmlWriter extends SMDRender {
	
	/**
	 * Produce the output from the current buffer to xmlWriter as XHTML 1.1 Standard.
	 * Must call parseNextBlock/parseLine first the set the current buffer.
	 * 
	 * Exclusive to call appendHtml (not mixing both)
	 * 
	 * @param markers the markers
	 * @param buffer (source data to copy)
	 * @param xmlWriter the writer to write out
	 * @throws XMLStreamException when writing error
	 */
	public void writeXhtml(SMDMarkers markers, CharBuffer buffer, XMLStreamWriter xmlWriter) throws XMLStreamException;
	
	/**
	 * Set the link URL resolver. Because the XML writer is string type for attribute so the resolver is different.
	 * 
	 * The resolver form is: method(inputURL) 
	 * 		it returns the unescaped text to replace the link URL
	 * 		(or null if not modified, the render will continue as is).
	 * 
	 * @param resolver function to call when a link is found.
	 */
	void setLinkHrefResolver(Function<String, String> resolver);
	
	/**
	 * Set the image source URL resolver. Because the XML writer is string type for attribute so the resolver is different.
	 * 
	 * The resolver form is: method(inputURL) 
	 * 		it returns the unescaped text to replace the link URL
	 * 		(or null if not modified, the render will continue as is). 
	 * 
	 * @param resolver function to call when a image (img tag) is found.
	 */
	void setImageSrcResolver(Function<String, String> resolver);

}
