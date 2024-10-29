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

public class SMDTextLineParser extends SMDLineParser {
    
    //change result codes
	protected static final int NO_CHANGE = 10;
	protected static final int CONSUME_1_CHAR = 11;
	protected static final int CONSUME_2_CHARS = 12;
	protected static final int PARSE_BREAK = 13; //stop parsing
       
	//These variables will be reset each time to call parseLine.
    protected final SMDMarkers markers;
    protected final boolean internalMarkers;

    //stack of recursive component, max 8 elements only
    protected final int[] stack;
    protected int stackPos = 0;
    
    public SMDTextLineParser() {
        this(256); //*8 bytes = 2KBytes.
    }
    
    public SMDTextLineParser(SMDMarkers markers) {
		this.markers = markers;
		this.stack  = new int[8]; //max 8 depth length of stack
		internalMarkers = false;
	}
    
    public SMDTextLineParser(int markerLength) {
    	markers = new SMDMarkers(markerLength);
    	this.stack  = new int[8]; //max 8 depth length of stack
    	internalMarkers = true;
    }

	@Override
	public void endLine(int position) {
		popAllStack(position);
	}
	
    /**
     * Parse a line from buffer. The buffer must be start of a line with its position.
     * 
     * If parse success, buffer will be consumed until the last of new line character (inclusive). 
     * Otherwise the position will be reset back to the original position.
     *  
     * @param buffer input buffer to parse
     * @return the parsing result
     */
    @Override
    public int parseLine(CharBuffer buffer) {
    	if(buffer.length() >= 0x0FFFFF) //over addressable position
    		throw new IllegalArgumentException("Buffer is too large, over its addressable of position (Integer)");
    
    	//reset for next line
    	stackPos = 0;
    	stack[0] = STATE_NONE; //set for replacing if catching any event
    	
    	int change = NO_CHANGE;
    	
    	char ch = '\0';
    	char nextChar = '\0';
    	char next2Char = '\0';
    	
    	buffer.mark();
    	//initial position
    	int startPos;
    	int pos = startPos = buffer.position();
    	
        while (buffer.hasRemaining() || ch != '\0') {
        	//get 3 chars at once for forward lookup
        	if(ch == '\0')
        		ch = buffer.get();
        	int chType = Character.getType(ch);
        	
        	//provision the nextChar and next2Char.
        	if(ch != '\n' && chType != Character.LINE_SEPARATOR && nextChar == '\0' && buffer.hasRemaining())
        		nextChar = buffer.get();
        	if(nextChar != '\0' && nextChar != '\n' && Character.getType(nextChar) != Character.LINE_SEPARATOR && next2Char == '\0' && buffer.hasRemaining())
    			next2Char = buffer.get();
        	
        	int state = stack[stackPos];
            switch (state) {
                case STATE_NONE:
                    change = handleNoneState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_TEXT:
                	change = handleTextState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_STRIKETHROUGH:
                	change = handleStrikeThroughState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_BOLD:
                	change = handleBoldState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_ITALIC:
                	change = handleItalicState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_UNDERLINE:
                	change = handleUnderlineState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_INLINE_CODE:
                	change = handleInlineCodeState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_LINK:
                	change = handleLinkState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_URL:
                	change = handleUrlState(pos, chType, ch, nextChar, next2Char);
                	break;
                case STATE_IMAGE:
                	change = handleImageState(pos, chType, ch, nextChar, next2Char);
                	break;
                case STATE_IMAGE_SRC:
                	change = handleImageSourceState(pos, chType, ch, nextChar, next2Char);
                    break;
                case STATE_UNPARSABLE:
                	if (ch == '\n' ||	chType == Character.LINE_SEPARATOR
                		||	chType == Character.PARAGRAPH_SEPARATOR) {
                		// End of paragraphText
                		popAllStack( pos );
                    	change = SMD_LINE_PARSED;
                    }
                	else
                		change = NO_CHANGE;
                	break;
                default:
                    change = handleUnknownState(state, pos, chType, ch, nextChar, next2Char);
                    break;
            }
            
            switch(change) {
            case NO_CHANGE:
            	//no change, you can use ch as usual
            case CONSUME_1_CHAR:
	            //handler consume only 1, get next char
	            ch = nextChar;
	            nextChar = next2Char;
	            next2Char = '\0'; //to read from buffer
	            pos++;
	            break;
            case CONSUME_2_CHARS:
            	//handle function has consume 2 char already
            	ch = next2Char;
	            nextChar = next2Char = '\0'; //to read from buffer in next loop
	            pos+=2;
	            break;
            }
            //change reset notify
            if(change == PARSE_BREAK || change < NO_CHANGE) {
            	break;
            }
        }
        
    	//can not parse to any kind of marker.
        if(change == PARSE_BREAK && pos == startPos) { //Empty line
        	return SMD_LINE_BLANK_OR_EMPTY;
        }
        else if(change == NO_CHANGE) {//not new line at end, can not detect any
        	buffer.reset();
        	return SMD_LINE_VOID;
        }
        else if (change < NO_CHANGE) {
        	if(change == SMD_LINE_VOID || change == SMD_LINE_INVALID)
        		buffer.reset();
        	return change;
        }
        
        //parse at least one char
        return SMD_LINE_PARSED;
    }

	public void printDebug() {
    	System.out.println(markers.toString());

    }
	
    private boolean isChangeableTo(int nextState) {
    	int reserved = 1+4; //end of the marker 1 (end) need 4 for next state (beginMarker beginContent endContent endMarker)
    	if(nextState == STATE_LINK || nextState == STATE_IMAGE) //following by an URL
    		reserved += 4; //more 4 (begin begin end end) for url

    	//if stack not empty, must include the ending of them
    	for(int i = stackPos; i>0; i--) {
    		reserved += 2; //ending of endContent and endMarker
    		if(stack[i] == STATE_LINK || stack[i] == STATE_IMAGE)
    			reserved += 4; //four more of url
    	}
    	return markers.remaining() > reserved;
    }
    
    private void pushStack(int newState) {
        if(stackPos < stack.length - 1)
        	stack[++stackPos] = newState;	
        // can not recursive more, assume text always
        else {
        	stackPos = stack.length - 1;
        	stack[stackPos] = STATE_TEXT;
        }
    }
    
    private int popStack() {
    	if(stackPos == 0)
    		return -1;
    	return stack[stackPos--];
    }
    
    private void popAllStack(int endPosition) {
    	if(stackPos > 0) {
    		for (int j = stackPos; j > 0; j--) {
    			int state = stack[j];
    			switch (state) {
    			case STATE_UNPARSABLE:
    			case STATE_TEXT:
    				//content includes the new line
    				markers.addStopContent(state, endPosition+1);
    				break;
    			case STATE_IMAGE_SRC:
    			case STATE_URL:
    				//content not include the new line
    				markers.addStopContent(state, endPosition);
    				break;
    			case STATE_IMAGE:
    			case STATE_LINK:
    				//marker only, including the new line
    				markers.addStopMarker(state, endPosition+1);
    				break;
    			case STATE_NONE:
    				//nothing
    				break;
    			default:
    				//content end before, the marker end next char
    				markers.addStopMarkerContent(state, endPosition, endPosition+1);
    				break;
    			}
    		}
    	}
    	else if(stack[0] == STATE_TEXT) //content includes the new line
    		markers.addStopContent(STATE_TEXT, endPosition+1);
    	
    	stack[0] = STATE_NONE;
    	stackPos = 0;
    }
    
    private int changeState(int position, int chType, char ch, char nextChar, char next2Char) {
	    if (ch == '\n'
	    		||	chType == Character.LINE_SEPARATOR
	    		||	chType == Character.PARAGRAPH_SEPARATOR) { //new line
            // break of paragraphText
        	popAllStack(position); //not including new line
        	return PARSE_BREAK;
        }
        else if (chType == Character.DIRECTIONALITY_WHITESPACE || chType == Character.CONTROL) { //space or control char will be ignore
    		return NO_CHANGE;
    	}
    		
    	int change = NO_CHANGE;
    	int nextState = STATE_NONE;
    	int endPosition = position;
    	if (ch == '~' && nextChar == '~' && next2Char != '~' && !Character.isWhitespace(next2Char)) {
			 nextState = STATE_STRIKETHROUGH;
             endPosition = position+2; //next char
             change = CONSUME_2_CHARS;
    	}
    	else if (ch == '*') {
            if (nextChar == '*' && !Character.isWhitespace(next2Char)) {
            	// Check for bold
            	nextState = STATE_BOLD;
                endPosition = position+2; //next char
                change = CONSUME_2_CHARS;
            }
            else if(nextChar != '*' && nextChar != '\0' && !Character.isWhitespace(nextChar)) {
            	// It is italic
            	nextState = STATE_ITALIC;
            	endPosition = position+1;
            	change = CONSUME_1_CHAR;
            }
        } else if (ch == '_' && nextChar == '_' && !Character.isWhitespace(next2Char)) {
        	// Check for underline
        	nextState = STATE_UNDERLINE;
        	endPosition = position+2;
        	change = CONSUME_2_CHARS;
        } else if (ch == '`' && nextChar != '\0') { //accept a white space followed
            // Check for inline code
        	nextState = STATE_INLINE_CODE;
        	endPosition = position+1;
        	change = CONSUME_1_CHAR;
        } else if (ch == '[') {	//accept white space followed
            // Check for link
    		nextState = STATE_LINK;
        	endPosition = position+1;
        	change = CONSUME_1_CHAR;
    	
        } else if (ch == '!' && (nextChar == '[' || nextChar == '(')) {
            // Check for image (with or without alternative text prefix
        	if(nextChar == '[') {
	        	nextState = STATE_IMAGE;
        	}
        	else {
        		pushStack( STATE_IMAGE );
            	markers.addStartMarker(STATE_IMAGE, position);
            	nextState = STATE_IMAGE_SRC;
        	}
        	endPosition = position+2;
            change = CONSUME_2_CHARS;
        }
        
    	if(change != NO_CHANGE && isChangeableTo(nextState)) {
			//replace zero to state text
			stack[0] = STATE_TEXT;
    		pushStack( nextState );
    		switch(nextState) {
    			case STATE_TEXT:
    			case STATE_URL:
    			case STATE_IMAGE_SRC:
    				markers.addStartContent(nextState, endPosition);
    				break;
    			default:
    				markers.addStartMarkerContent(nextState, position, endPosition);
    				break;
    		}
        	return change;
    	}
    	
    	if(change != NO_CHANGE) { //next state is not changeable, then it stops parsing
    		pushStack( STATE_UNPARSABLE );
    		markers.addStartMarkerContent(STATE_UNPARSABLE, position, position);
        	return CONSUME_1_CHAR;
    	}
    	return NO_CHANGE;
    }
    
    private int handleUnknownState(int state, int position, int chType, char ch, char nextChar, char next2Char) {
    	throw new RuntimeException("Unknown state: " + state);
    }
    
    private int handleNoneState(int position, int chType, char ch, char nextChar, char next2Char) {
    	int change = changeState(position, chType, ch, nextChar, next2Char);
    	//NONE is changed to STATE_TEXT if get a UnicodeIdentifierPart
    	if(change == NO_CHANGE) {
    		//not start with a paragraph text, it may be empty line as well
    		if (stack[0] == STATE_NONE && !Character.isUnicodeIdentifierPart(ch))
    			return SMD_LINE_INVALID;
    		
			//text
			pushStack(STATE_TEXT);
			markers.addStartContent(STATE_TEXT, position);
			// Assume change to text if get any other character.
			change = CONSUME_1_CHAR;
    	}
        return change;
    }

    private int handleTextState(int position, int chType, char ch, char nextChar, char next2Char) {
    	//no ending, except end of line.
    	//full switching by changeState;
    	return changeState(position, chType, ch, nextChar, next2Char);
    }
    
    private int handleStrikeThroughState(int position, int chType, char ch, char nextChar, char next2Char) {
    	//handle ending marker
    	if (ch == '~' && nextChar == '~' && next2Char != '~') {
            // End of strike through
    		markers.addStopMarkerContent(STATE_STRIKETHROUGH, position, position+2);
    		popStack();
    		return CONSUME_2_CHARS;
        }
    	
    	//this changeState calling will never match for bold again, but italic is possible.
        return changeState(position, chType, ch, nextChar, next2Char);
	}

    private int handleBoldState(int position, int chType, char ch, char nextChar, char next2Char) {
    	//handle ending marker
    	if (ch == '*' && nextChar == '*') {
            // End of bold
    		markers.addStopMarkerContent(STATE_BOLD, position, position+2);
    		popStack();
    		return CONSUME_2_CHARS;
        }
    	//this changeState calling will never match for bold again because repeated 2 *, but italic (1 x *) is possible.
        return changeState(position, chType, ch, nextChar, next2Char);
    }

    private int handleItalicState(int position, int chType, char ch, char nextChar, char next2Char) {
    	if (ch == '*') {
            // End of italic
    		popStack();
    		markers.addStopMarkerContent(STATE_ITALIC, position, position+1);
    		return CONSUME_1_CHAR;
        }
    	//this changeState calling will never match for bold or italic again.
    	return changeState(position, chType, ch, nextChar, next2Char);
    }

    private int handleUnderlineState(int position, int chType, char ch, char nextChar, char next2Char) {
    	if (ch == '_' && nextChar == '_') {
            // End of underline
    		popStack();
    		markers.addStopMarkerContent(STATE_UNDERLINE, position, position+2);
    		return CONSUME_2_CHARS;
        }

    	//the underline can contain only inline code
    	if (ch == '`') {
        	if(isChangeableTo(STATE_INLINE_CODE)) {
        		pushStack( STATE_INLINE_CODE);
        		markers.addStartMarkerContent(STATE_INLINE_CODE, position, position+1 );
        	}
        	else {  //change to stop parsing
        		pushStack( STATE_UNPARSABLE );
        		markers.addStartMarkerContent(STATE_UNPARSABLE, position, position);
        	}
    		return CONSUME_1_CHAR;
    	}
    	
    	if (ch == '\n'
        		||	chType == Character.LINE_SEPARATOR
	    		||	chType == Character.PARAGRAPH_SEPARATOR) {
    		// End of underline
        	popAllStack(position);
        	return SMD_LINE_PARSED;
        }
    	return NO_CHANGE;
    }

    private int handleInlineCodeState(int position, int chType, char ch, char nextChar, char next2Char) {
        if (ch == '`') {
            // End of inline code
        	popStack();
        	markers.addStopMarkerContent(STATE_INLINE_CODE, position, position+1);
        	return CONSUME_1_CHAR;
        }
        
        if (ch == '\n'
        		||	chType == Character.LINE_SEPARATOR
	    		||	chType == Character.PARAGRAPH_SEPARATOR) {
        	// End of image and all the line
        	popAllStack(position);
        	return SMD_LINE_PARSED;
        }
        
        return NO_CHANGE;
    }

    private int handleLinkState(int position, int chType, char ch, char nextChar, char next2Char) {
        // Link parsing logic
        // Skip to closing brackets and parentheses
        if (ch == ']' && nextChar == '(') {
            // Handle link text
        	markers.addStopContent(STATE_LINK, position);
        	//follow by URL, already reserved markers' space
    		pushStack( STATE_URL );
    		markers.addStartContent(STATE_URL, position+2 );
    		return CONSUME_2_CHARS;
        }
        
        //Image, bold, text.. inside the link is not supported yet.
        
        if (ch == ']' || ch == '\n' ||	chType == Character.LINE_SEPARATOR
	    		||	chType == Character.PARAGRAPH_SEPARATOR) { //invalid close of link, return back to value
        	popStack();
        	markers.rollbackLastMarkerContentStart(STATE_LINK);
        	
	        if (ch == '\n' ||	chType == Character.LINE_SEPARATOR
		    		||	chType == Character.PARAGRAPH_SEPARATOR) {
	    		// Early end of Link with empty
	        	popAllStack(position );
	        	return SMD_LINE_PARSED;
	        }
	        return CONSUME_1_CHAR;
        }
        
        return NO_CHANGE;
    }

    private int handleUrlState(int position, int chType, char ch, char nextChar, char next2Char) {
        // URL parsing logic
        // TODO: enhance of checking/parsing
        if (ch == ')') {
            // End of URL and end of Link as well
        	popStack();
        	markers.addStopContent(STATE_URL, position);
        	popStack();
        	markers.addStopMarker(STATE_LINK, position+1);
        	return CONSUME_1_CHAR;
        } else if (ch == '\n' ||	chType == Character.LINE_SEPARATOR
	    		||	chType == Character.PARAGRAPH_SEPARATOR) {
    		// End of paragraphText
        	popAllStack(position );
        	return SMD_LINE_PARSED;
        }
        return NO_CHANGE;
    }
    
    private int handleImageState(int position, int chType, char ch, char nextChar, char next2Char) {
        // Image parsing logic
        if (ch == ']' && nextChar == '(') {
            // Handle optional text within image
        	markers.addStopContent(STATE_IMAGE, position);
        	//follow by URL, already reserved markers' space
    		pushStack( STATE_IMAGE_SRC );
    		markers.addStartContent(STATE_IMAGE_SRC, position+2 );
    		return CONSUME_2_CHARS;
        }
        
        if (ch == ']' || ch == '\n' ||	chType == Character.LINE_SEPARATOR
	    		||	chType == Character.PARAGRAPH_SEPARATOR) { //invalid close of image, return back to value
        	popStack();
        	markers.rollbackLastMarkerContentStart(STATE_IMAGE);
	        if (ch == '\n' ||	chType == Character.LINE_SEPARATOR
		    		||	chType == Character.PARAGRAPH_SEPARATOR) {
	    		// Early end of Link with empty
	        	popAllStack(position );
	        	return SMD_LINE_PARSED;
	        }
	        return CONSUME_1_CHAR;
        }
        
        return NO_CHANGE;
    }

    private int handleImageSourceState(int position, int chType, char ch, char nextChar, char next2Char) {
    	// URL parsing logic
        // TODO: enhance of checking/parsing
        if (ch == ')') {
            // End of image source
        	popStack();
        	markers.addStopContent(STATE_IMAGE_SRC, position);
        	popStack();
        	markers.addStopMarker(STATE_IMAGE, position+1);
        	return CONSUME_1_CHAR;
        } else if (ch == '\n' ||	chType == Character.LINE_SEPARATOR
	    		||	chType == Character.PARAGRAPH_SEPARATOR) {
    		// End of paragraphText
        	popAllStack(position );
        	return SMD_LINE_PARSED;
        }
        return NO_CHANGE;
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

	@Override
	public SMDMarkers markers() {
		return markers;
	}

	@Override
	public int parseNext(CharBuffer buff) {
		return parseLine(buff);
	}

}