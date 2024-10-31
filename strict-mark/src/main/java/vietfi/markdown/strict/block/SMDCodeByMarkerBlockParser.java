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
package vietfi.markdown.strict.block;

import java.nio.CharBuffer;

import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;

/**
 * parsing code block by marker, optionally with language for highlighting
 * This implementation will parse all the code at once (until got the end marker).
 * 
 * example:
 * ~~~ javascript
 * var x = "your code, any text, &lt;&gt; escaped."
 * ~~~
 * 
 * The markers of this kind are:
 * 
 * &lt; ~~~ space [language name] space \n [code line 1\n] [code line 2\n] ... [the last code line\n] ~~~ &gt; \n
 * 
 */
public class SMDCodeByMarkerBlockParser implements SMDParser {

	protected final SMDMarkers markers;
	protected final boolean internalMarkers;
	
	public SMDCodeByMarkerBlockParser() {
		this.markers = new SMDMarkers(8);
		internalMarkers = true;
	}
	
	public SMDCodeByMarkerBlockParser(SMDMarkers markers) {
		this.markers = markers;
		internalMarkers = false;
	}

	protected char markerType = '\0';
	
	@Override
	public int parseNext(CharBuffer buffer) {		
		if(!buffer.hasRemaining())
			return SMD_VOID;
		
		char ch = '\0';
		char ch1 = '\0';
		char ch2 = '\0';
		
		int startPos;
		int npos = startPos = buffer.position();
		boolean foundBeginning = false;
		//mark for reset if needed
		buffer.mark();
		
		if(markerType == '\0') {
	    	
			markerType = buffer.get();
			npos++;
			if(!(markerType == '~' || markerType == '`')) {
				markerType = '\0';
				buffer.reset();
				return SMD_BLOCK_INVALID;
			}
			
			int c = 2; //more 2
			while(buffer.hasRemaining()) {
				ch = buffer.get();
				
				npos++;
				if((c > 0 && markerType != ch) || (c <= 0 && (ch == '\n' || ch == '\u001C'))) {
					break;
				}
				if(c < 0 && !Character.isWhitespace(ch))
					break;
				c--;
			}
			
			//not enough marker or got the line end
			if(c > 0 || ch == '\u001C') {
				markerType = '\0';
				
				if(!buffer.hasRemaining()) {
					buffer.reset();
					return SMD_VOID;
				}
				buffer.reset();
				return SMD_BLOCK_INVALID;
			}

	    	//enough ~~~ characters
	    	foundBeginning = true;
	    	markers.addStartMarker(STATE_CODE_BLOCK, startPos);
	    	
			if(ch != '\n') {
				//start language definition after the spaces trailing of ``` or ~~~
				markers.addStartContent(STATE_CODE_LANGUAGE, npos - 1);
				
				boolean isNewLine = false;
				boolean languageDone = false;
				while(buffer.hasRemaining()) {
					ch = buffer.get();
					
					npos++;
					if(Character.isWhitespace(ch)) {
						if(!languageDone) {
							markers.addStopContent(STATE_CODE_LANGUAGE, npos - 1);
							languageDone = true;
						}
						if(ch == '\n' || ch == '\u001C') {//ok, end to parser
							isNewLine = true;
							break;
						}
						//else jump over trailing spaces
					}
					else if(languageDone) //invalid 
						break;
				}
				
				if(!isNewLine || ch == '\u001C') {
					boolean notRemain = !buffer.hasRemaining();
					buffer.reset();
					//roll back
					markers.rollbackLastContentStart(STATE_CODE_LANGUAGE);
					markers.rollbackLastMarkerContentStart(STATE_CODE_BLOCK);
					markerType = '\0';
					if(languageDone || notRemain) {
						return SMD_VOID;
					}
					return SMD_BLOCK_INVALID;
				}
			}
			//else no add content of Code Language
		}
		
		if(ch == '\n' && foundBeginning) {
			markers.addStartContent(STATE_CODE_BLOCK, npos);
			ch = '\0'; //for next reading
		}
				
		//read the line until found marker
		boolean endingFound = false;
		while(buffer.hasRemaining() || ch != '\0') {
			if(ch == '\0')
				ch = buffer.get();
			
			npos++;
        	
			if(endingFound) {//waiting for space or new line
				if(ch != markerType && !Character.isWhitespace(ch)) {
					markerType = '\0';
					buffer.reset();
					return SMD_BLOCK_INVALID;
				}
				if(ch == '\n' || ch == '\u001C') {//gracefully
					//finally,  add marker stop
					markers.addStopMarker(STATE_CODE_BLOCK, npos);
					markerType = '\0';
					return SMD_BLOCK_END;
				}
				//loop for need more characters
			}
			else {
				if(ch == '\u001C') {
					//stop parsing
	        		markers.addStopContentMarker(STATE_CODE_BLOCK, npos - 1, npos);
	        		markerType = '\0';
					return SMD_BLOCK_END;
				}
				if(ch != '\0' && ch1 == '\0' && buffer.hasRemaining())
	        		ch1 = buffer.get();
	        	if(ch1 != '\0' && ch2 == '\0' && buffer.hasRemaining())
	    			ch2 = buffer.get();
	        	if(ch == markerType && ch1 == markerType && ch2 == markerType) {
					//ending found, add content stop
	        		markers.addStopContent(STATE_CODE_BLOCK, npos - 1);
					endingFound = true;
	        	}
			}
			
			//shift one
        	ch = ch1;
        	ch1 = ch2;
        	ch2 = '\0';
		}
		
		if(endingFound) {
			markers.rollbackLastContentStart(STATE_CODE_BLOCK);
			if(foundBeginning) { //it is the same call
				markers.rollbackLastContentStart(STATE_CODE_LANGUAGE);
				markers.rollbackLastMarkerContentStart(STATE_CODE_BLOCK);
			}
			buffer.reset();
			return SMD_VOID;
		}
		//continue next block
		return SMD_BLOCK_CONTINUE;
	}

	
	@Override
	public void endBlock(int position) {
		if(markerType != '\0') {
			markers.addStopContentMarker(STATE_CODE_BLOCK, position, position);
    		markerType = '\0';
		}
	}
	
	@Override
	public int compact(int position) {
		if(this.internalMarkers) {
			int r = markers.compactMarkers(position);
			if(r <= 0)
				return 0;
		}
		return position;
	}

	@Override
	public SMDMarkers markers() {
		return this.markers;
	}

}
