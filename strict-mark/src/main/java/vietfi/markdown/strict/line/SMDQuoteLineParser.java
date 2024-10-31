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

public class SMDQuoteLineParser extends SMDLineParser {

	protected final SMDMarkers markers;
	protected final boolean internalMarkers;
	protected final SMDTextLineParser lineParser;
	
	private boolean ending = false;//the line of close blockquote
	private int quoteLines = 0; //quote block lines count
	private int paraLines = 0; //paragraph lines
	
	public SMDQuoteLineParser() {
		internalMarkers = true;
		markers = new SMDMarkers(256);
		lineParser = new SMDTextLineParser(markers);
    }
    
    public SMDQuoteLineParser(SMDMarkers markers) {
    	internalMarkers = false;
    	this.markers = markers;
    	lineParser = new SMDTextLineParser(markers);
	}
	
	/**
	 * Parsing the line in buffer to identify a quote of markdown, then calling the next line parser of paragraph text line.
	 */
	@Override
	public int parseLine(CharBuffer buffer) {
		if(!buffer.hasRemaining())
			return SMD_LINE_VOID;
		
		if(ending) {
			quoteLines = 0;
			paraLines = 0;
			ending = false;
		}
		
		char first = '\0';
		char next = '\0';
		
		buffer.mark();
		int startPos = buffer.position();
		first = buffer.get();
		if(first == '>' && buffer.hasRemaining())
			next = buffer.get();
		
		if(first == '>' && !buffer.hasRemaining()) { //
			buffer.reset();
			return SMD_LINE_VOID;
		}
		else if(first == '\n' || first != '>' || first == '>' && !Character.isWhitespace(next)) {//consumed
	
			if(paraLines == 0) {
				markers.rollbackLastMarkerContentStart(STATE_QUOTE_PARAGRAPH);
			}
			else
				markers.addStopMarker(STATE_QUOTE_PARAGRAPH, first != '\n' ? startPos : startPos+1);
			if(quoteLines == 0) {
				markers.rollbackLastMarkerContentStart(STATE_QUOTE_BLOCK);
			}
			else {
				markers.addStopMarker(STATE_QUOTE_BLOCK, first != '\n' ? startPos : startPos+1);
				ending = true;
			}
		
			if(first != '\n') { //invalid quote block, not a quote start, or a quote but not follow a space/tab
				buffer.reset();
				return SMD_LINE_INVALID;
			}
			
			return SMD_LINE_BLANK_OR_EMPTY;
		}
		
		//now, first is > and follow by a space/new line
		int pos = startPos + 2;
		
		//read next char bypass the space
		while(next == '\n' || ((next == ' ' || next == '\t') && buffer.hasRemaining())) {
			if(next == '\n') {//it is quote of empty line
				if(quoteLines == 0) {
					markers.addStartMarker(STATE_QUOTE_BLOCK, startPos);
				}
				quoteLines++;
				if(paraLines > 0) {
					markers.addStopMarker(STATE_QUOTE_PARAGRAPH, pos);
					paraLines = 0;
				}
				
				return SMD_LINE_PARSED;
			}
			if(buffer.hasRemaining()) {
				next = buffer.get();
				pos++;
			}
		}
		
		if(next == ' ' || next == '\t') {//no new line, no buffer remaining
			buffer.reset();
			return SMD_LINE_VOID;
		}
		
		if(quoteLines == 0) {
			markers.addStartMarker(STATE_QUOTE_BLOCK, startPos);
		}
		
		if(paraLines == 0) {
			//start of paragraph, same as quote block (start >)
			markers.addStartMarker(STATE_QUOTE_PARAGRAPH, startPos);
		}
		
		//rewind one char
		pos--;
		buffer.position(pos);
		
		//parseLine will read until the end of line, if not, then invalid
		//the procedure will mark itself.
		int r = lineParser.parseLine(buffer);
		
		if(r == SMDLineParser.SMD_LINE_VOID || r == SMDLineParser.SMD_LINE_INVALID) {
			if(quoteLines == 0) {
				markers.rollbackLastMarkerContentStart(STATE_QUOTE_PARAGRAPH);
				markers.rollbackLastMarkerContentStart(STATE_QUOTE_BLOCK);
				ending = true;
			}
			else if(r == SMDLineParser.SMD_LINE_INVALID) {
				if(paraLines > 0) {
					//stop of paragraph
					markers.addStopMarker(STATE_QUOTE_PARAGRAPH, buffer.position());
					paraLines = 0;
				}
				
				// treat remaining as unparseable
				markers.addStartContent(STATE_UNPARSABLE, pos);
				
				char ch;
				
				
				while(buffer.hasRemaining()) {
					ch = buffer.get();
					
					pos++;
					if(ch == '\n' || ch == '\u001C') {
						markers.addStopContent(STATE_UNPARSABLE, pos);
						return SMD_LINE_PARSED; //ending
					}
				}
				
				//not found the line end, it is void
				markers.rollbackLastContentStart(STATE_UNPARSABLE);
				r = SMD_LINE_VOID;
				
				/*
				 * removed markers.addMarkerStop(STATE_QUOTE_BLOCK, buffer.position());
				ending = true;
				*/
			}
			//can not parse the paragraphText (including the void state), set back to position
			buffer.position(startPos);
			return r;
		}
		
		quoteLines++;
		paraLines++;
		
		return r == SMD_LINE_BLANK_OR_EMPTY ? SMD_LINE_PARSED : r;
	}

	@Override
	public void endLine(int position) {
		this.lineParser.endLine(position);
		if(paraLines > 0) //close paragraph
			markers.addStopMarker(STATE_QUOTE_PARAGRAPH, position);
		if(quoteLines > 0) {
			markers.addStopMarker(STATE_QUOTE_BLOCK, position);
			ending = true;
		}
	}

	@Override
	public int compact(int shiftRemaining) {
		int r = 1;
		if(internalMarkers)
			r = markers.compactMarkers(shiftRemaining);
		this.lineParser.compact(shiftRemaining);
		if(r<=0)
			return 0;
		return shiftRemaining;
	}

	@Override
	public SMDMarkers markers() {
		return markers;
	}

	public int getQuoteLines() {
		return this.quoteLines;
	}

	@Override
	public int parseNext(CharBuffer buff) {
		return parseLine(buff);
	}

}
