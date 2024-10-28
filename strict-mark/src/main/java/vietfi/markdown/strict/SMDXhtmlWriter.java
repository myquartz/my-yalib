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
