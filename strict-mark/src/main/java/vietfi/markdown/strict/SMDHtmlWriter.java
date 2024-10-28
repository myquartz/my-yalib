package vietfi.markdown.strict;

import java.nio.CharBuffer;

public interface SMDHtmlWriter {
	
	/**
	 * Produce the output from the current buffer to outputBuffer by HTML 5.0 Standard
	 * Must call parseNextBlock/parseLine first to the buffer.
	 * 
	 * Exclusive to call writeXhtml (not mixing both) 
	 * 
	 * @param markers the markers
	 * @param buffer (source data to copy)
	 * @param outputBuilder the output buffer.
	 * @return true if written completed, false if there is no space left (remaining not enough), needing another call to appending out (after the buffer clean).
	 */
	public boolean appendHtml(SMDMarkers markers, CharBuffer buffer, CharBuffer outputBuffer);
	
	/**
	 * compacting the buffer position to zero.
	 * 
	 * @param position current position.
	 */
	public void compact(int position);
}
