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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface SMDXhtmlWriter {
	
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

}
