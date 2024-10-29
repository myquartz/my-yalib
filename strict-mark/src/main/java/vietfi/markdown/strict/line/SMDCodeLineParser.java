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

/**
 * This class process in series of code lines which prefix with a space.
 * 
 */
public class SMDCodeLineParser extends SMDLineParser {

	private final SMDMarkers markers;
	private final boolean internalMarkers;
	private int codeLines = 0; //code block lines counts
	private boolean ending = false;
	
	public SMDCodeLineParser() {
		this.markers = new SMDMarkers(8);
		internalMarkers = true;
	}
	
	public SMDCodeLineParser(SMDMarkers markers) {
		this.markers = markers;
		internalMarkers = false;
	}

	public SMDCodeLineParser(int length) {
		this.markers = new SMDMarkers(length);
		internalMarkers = true;
	}
	
	public SMDMarkers markers() {
		return markers;
	}

	private boolean doCopy = false;
    private CharBuffer inputBuffer = null; //char buffer for check reference only.
    private char[] inputChars = null; //the char array extract from buffer (if the buffer backed by an array) or it will be copy from buffer.
    
    
	public CharBuffer getInputBuffer() {
		return inputBuffer;
	}

	public char[] getInputChars() {
		return inputChars;
	}

	@Override
	public int parseLine(CharBuffer buffer) {
		if(!buffer.hasRemaining())
			return SMD_LINE_VOID;
		
		if(ending) {
			codeLines = 0;
			ending = false;
		}
		
		char ch = '\0';
		
		buffer.mark();
		int pos = buffer.position();
		ch = buffer.get();
		int chType = Character.getType(ch);
		int r = 0;
		if(ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {//consumed
			r = SMD_LINE_BLANK_OR_EMPTY;
		}
		
		if(r == 0 && !(ch == ' ' || ch == '\t')) {//not a code by indentation space or tab
			buffer.reset();
			r = SMD_LINE_INVALID;
		}
		
		if(r != 0) {
			//end of block or just invalid?
			if(codeLines > 0) {
				ending = true;
				markers.addStopMarker(STATE_CODE_BLOCK, buffer.position());
			}
			return r;
		}
		//setup the buffer
    	if(this.inputBuffer != buffer) {
    		this.inputBuffer = buffer;
    		if(buffer.hasArray()) {
    			this.inputChars = buffer.array();
    			doCopy = false;
    		}
    		else {//allocate an array with same size to copy characters while processing
    			this.inputChars = new char[buffer.capacity()];
    			doCopy = true;
    		}
    	}
    	
    	if(codeLines == 0) {
			markers.addStartMarker(STATE_CODE_BLOCK, pos);
		}
    	pos++;
		
		//now, sure that is after the first space
    	markers.addStartContent(STATE_CODE_INDENT_BLOCK, pos);
		//read next char bypass the space
		while((ch == ' ' || ch == '\t') && buffer.hasRemaining()) {
			ch = buffer.get();
			chType = Character.getType(ch);
			if(doCopy)
				this.inputChars[pos] = ch;
			pos++;
			if(ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {//new line, meaning empty line code
				markers.addStopContent(STATE_CODE_INDENT_BLOCK, pos);
				codeLines++;
				return SMD_LINE_PARSED;
			}
		}
		
		//parseLine will read until the end of line, if not, then void
		while(buffer.hasRemaining()) {
			ch = buffer.get();
			chType = Character.getType(ch);
			if(doCopy)
				this.inputChars[pos] = ch;
			pos++;
			if(ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {//new line, meaning empty line code
				//include the new line
				markers.addStopContent(STATE_CODE_INDENT_BLOCK, pos);
				codeLines++;
				return SMD_LINE_PARSED;
			}
		}
		
		//can not reach to new line in the loop, rollback for next call
		markers.rollbackLastContentStart(STATE_CODE_INDENT_BLOCK);
		if(codeLines == 0) {
			markers.rollbackLastMarkerContentStart(STATE_CODE_BLOCK);
		}
		buffer.reset();
		return SMD_LINE_VOID;
	}

	@Override
	public void endLine(int position) {
		if(codeLines > 0) {
			ending = true;
			markers.addStopMarker(STATE_CODE_BLOCK, position);
		}
	}

	@Override
	public int compact(int shiftRemaining) {
		if(internalMarkers) {
			int r = markers.compactMarkers(shiftRemaining);
			if(r <= 0)
				return 0;
		}
		return shiftRemaining;
	}

	public int getCodeLines() {
		return this.codeLines;
	}

	@Override
	public int parseNext(CharBuffer buff) {
		return parseLine(buff);
	}

	
}
