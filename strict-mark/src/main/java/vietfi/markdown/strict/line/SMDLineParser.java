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
	
	/**
	 * Cascade the call to endLine.
	 * 
	 * @param position the ending position (exclusive).
	 */
	@Override
	public void endBlock(int position) {
		endLine(position);
	}

	/**
	 * detect whether is it a blank line?
	 * 
	 * @param buffer
	 * @return
	 */
	public static boolean detectBlankLine(CharBuffer buffer) {
		if(!buffer.hasRemaining())
			throw new IllegalArgumentException();
		
		int pos = buffer.position();
		
		for(int i = 0; buffer.remaining() > i; i++) {
			char ch = buffer.get(pos + i);
			
			if(!Character.isWhitespace(ch))
				break;
			
			if(ch == '\n' || ch == '\u001C') {
				//got a new line, stop of the lookup.
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Consume the buffer which position starts of a blank line until it gets the new line. 
	 * 
	 * @param buffer the input buffer.
	 */
	public static void consumeBlankLine(CharBuffer buffer) {
		char ch = buffer.get(); //consume until the new line.
		
		while(buffer.hasRemaining() 
				&& !(ch == '\n' || ch == '\u001C')) {
			if(!Character.isWhitespace(ch))
				throw new IllegalArgumentException("Not a blank line of char '"+ch+"' at "+buffer.position());
			ch = buffer.get(); //consume until the new line.
			
		}
		assert ch == '\n' || ch == '\u001C';
	}

	/**
	 * Consume the buffer until it gets a space or tab. 
	 * 
	 * @param buffer the input buffer.
	 */
	public static void consumeUtilCatchSpace(CharBuffer buffer) {
		char ch = buffer.get(); //consume until the space
		while(buffer.hasRemaining() 
				&& !(ch == ' ' || ch == '\t')) {
			ch = buffer.get(); //consume until the new line.
		}
		assert ch == ' ' || ch == '\t';
	}

	/**
	 * Consume the buffer until it gets a new line character or ending
	 * 
	 * @param buffer the input buffer.
	 */
	public static void consumeUtilCatchNewLine(CharBuffer buffer) {
		char ch = buffer.get(); //consume until the new line/ending
		while(buffer.hasRemaining() 
				&& !(ch == '\n' || ch == '\u001C')) {
			ch = buffer.get(); //consume until the new line.
		}
		assert ch == '\n' || ch == '\u001C';
	}
	
	/**
	 * 
	 * Look forward for a new line or ending.
	 * 
	 * @param buffer
	 * @return how far from current position, -1 if not found
	 */
	public static int lookForwardNewLine(CharBuffer buffer) {
		int pos = buffer.position();
		
		for(int i = 0; buffer.remaining() > i; i++) {
			char ch = buffer.get(pos + i);
						
			if(ch == '\n' || ch == '\u001C') {
				//got a new line
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Consume the buffer until it counts enough spaces or tab. 
	 * 
	 * @param buffer the input buffer.
	 * @param space number of spaces to consume (maximum).
	 * @param tab number of tabs to consume (break condition)
	 * @return number of chars consumed
	 */
	public static int consumeSpaceOrTab(CharBuffer buffer, int space, int tab) {
		if(space == 0 && tab == 0)
			return 0;
		int count = 0;
		
		while(buffer.hasRemaining() && space > 0) {
			char ch = buffer.get(); //consume until the space
			count++;
			if(ch == ' ')
				space --;
			else if(ch == '\t') {
				tab --;
				space -= 4;
				if(tab <= 0) {
					break;
				}
			}
		}
		return count;
	}
	
	/**
	 * 
	 * count forward of spaces, one tab = 4 spaces.
	 * 
	 * @param buffer
	 * @param spaceStop number of space exceeds to stop (inclusive)
	 * @param tabStop number of tab exceeds to stop (inclusive)
	 * @return number of space + tab x 4.
	 */
	public static int lookForwardTabOrSpaces(CharBuffer buffer, int spaceStop, int tabStop) {
		if(!buffer.hasRemaining())
			throw new IllegalArgumentException();
		
		int sp = 0; //spaces
		int tab = 0; //tabs
		
		int pos = buffer.position();
		
		for(int i = 0; sp < spaceStop && tab < tabStop && buffer.remaining() > i; i++) {//read multiple of spaces
			char ch = buffer.get(pos + i);
			
			if(!Character.isWhitespace(ch))
				break;
			if(ch == '\t')
				tab++;
			else if(ch == ' ')
				sp++;
			
			if(ch == '\n' || ch == '\u001C') {
				//got a new line, invalid of the lookup.
				break;
			}
		}
		
		return sp + tab * 4;
	}
	
}
