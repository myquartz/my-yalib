package vietfi.markdown.strict;

import java.nio.CharBuffer;

public interface SMDHtmlRender {

	/**
	 * Produce the output from the current buffer to outputBuilder.
	 * Must call parseNextBlock/parseLine first to parse and setup the buffer.
	 * 
	 * @param markers the markers
	 * @param buffer (source data to copy)
	 * @param outputBuilder the output buffer.
	 * @return number of written characters to outputBuilder.
	 */
	public void produceHtml(SMDMarkers markers, CharBuffer buffer, StringBuilder outputBuilder);
	
}
