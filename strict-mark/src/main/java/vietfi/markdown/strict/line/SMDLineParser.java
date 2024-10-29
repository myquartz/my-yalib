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
package vietfi.markdown.strict.line;

import java.nio.CharBuffer;

import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;

public abstract class SMDLineParser implements SMDParser {
	
	public final static int MINIMUM_LINE_BUFFER_SIZE = 128;
	
	/**
	 * Void result, because there is no line ending.
	 * The caller needs to compact the input buffer, fulfill more data, or append an new line / EOF character for closing line.
	 * 
	 *  The buffer position will be reset.
	 */
	public static final int SMD_LINE_VOID = 0;
	
	/**
	 * Empty line result, because it gets a new line immediately.
	 * The consequent action is depended on the caller and the the kind of block. 
	 */
	public static final int SMD_LINE_BLANK_OR_EMPTY = 1;
	
	/**
	 * The invalid line, it can not parse. go ahead to the next parser.
	 */
	public static final int SMD_LINE_INVALID = 2;
	
	/**
	 * The line is parsed successfully, and the position is at the char after the processed new line.
	 */
	public static final int SMD_LINE_PARSED = 3;
	
	/**
	 * The line is parsed successfully, and the position is at the char after the processed new line.
	 * However, in this case, the content of the parsed line is empty.
	 */
	public static final int SMD_LINE_PARSED_END = 4;
	    
    /**
     * Parse a line from buffer. The buffer must be start of a line with its position.
     * 
     * If parse success, buffer will be consumed until the last of new line character (inclusive). 
     * Otherwise the position will be reset back to the original position.
     * 
     * Position (Buffer will be reset to the beginning mark) does not change when the result is:
     * 
     * SMD_LINE_VOID: it can not parse, there is no new line marker when no buffer remaining, no detectable the block. 
     * SMD_LINE_INVALID: got a invalid line.
     * 
     * The marker character is consume (position changed) when the result is:
     * 
     * SMD_LINE_EMPTY: got a empty line. The new line character is consumed.
     * SMD_LINE_PARSED:  if successful parsing one line in the series.
     * SMD_LINE_PARSED_END:  if successful parsing one line but this is the final line of the series.
     * 
     * @param buffer to assign for the parser
     * @return the value of parsing result
     */
	public abstract int parseLine(CharBuffer buffer);
	
	/**
	 * adjust position of parseLine CharBuffer for the call. 
	 * 
	 * @param shiftRemaining
	 * @return number of bytes compacted
	 */
	public abstract int compact(int shiftRemaining);
	
	/**
	 * Markers using in the line parsers;
	 * 
	 * @return
	 */
	public abstract SMDMarkers markers();
	
	/**
	 * Force ending the series of line (if any) without calling parseLine.
	 * The caller know it is ending the child level and call this method to tell the position of ending.
	 * 
	 * Child will create an ending mark if needed. It is similar to SMD_LINE_INVALID but no buffer input.
	 *   
	 * @param position the ending position (exclusive).
	 */
	public abstract void endLine(int position);
	
}
