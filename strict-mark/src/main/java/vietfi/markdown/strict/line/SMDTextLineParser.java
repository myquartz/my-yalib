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
	protected static final int ROLLBACK = 13;
	protected static final int PARSE_BREAK = 14; //stop parsing
       
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
    
    	int nl = lookForwardNewLine(buffer);
    	
    	if (nl < 0)
    		return SMD_LINE_VOID;
    	
    	if (nl == 0) {
    		//consume the new line char
    		buffer.get();
    		return SMD_LINE_BLANK_OR_EMPTY;
    	}
    	
    	buffer.mark();
    	
    	//reset status for the line parsing
    	stackPos = 0;
    	stack[0] = STATE_NONE; //set for replacing if catching any event
    	
    	int change = NO_CHANGE;
    	
    	boolean isFirstChange = true;
    	//the currChar position, unlike other parsers.
    	int pos = buffer.position();
    	char currChar = '\0';
    	char nextChar = '\0';
    	char next2Char = '\0';
    	//first char is always match the condition because the line has at least 2 chars.
        while (buffer.hasRemaining()) {
        	if(change == CONSUME_2_CHARS) {
        		//ignore last loop char
        		buffer.get();
        		pos++;
        	}
        	
        	currChar = buffer.get();
        	//provision the nextChar and next2Char.
        	if(buffer.remaining() > 0) {
    			nextChar = buffer.get(pos+1);
        		if(buffer.remaining() > 1)
        			next2Char = buffer.get(pos+2);
        		else
        			next2Char = '\0';
        	} else {
        		nextChar = '\0';
            	next2Char = '\0';
        	}
        	
        	int state = stack[stackPos];
            switch (state) {
                case STATE_NONE:
                    change = handleNoneState(isFirstChange, state, pos, currChar, nextChar, next2Char);
                    if(change == NO_CHANGE && isFirstChange) { //none must change to any other at first
                		buffer.reset();
                		//ending the last
                		endLine(buffer.position());
                		return SMD_LINE_INVALID;
                    }
                    break;
                case STATE_TEXT:
                	change = handleTextState(state, pos, currChar, nextChar, next2Char);
                    break;
                case STATE_STRIKETHROUGH:
                	change = handleStrikeThroughState(state, pos, currChar, nextChar, next2Char);
                    break;
                case STATE_BOLD:
                	change = handleBoldState(state, pos, currChar, nextChar, next2Char);
                    break;
                case STATE_ITALIC:
                	change = handleItalicState(state, pos, currChar, nextChar, next2Char);
                    break;
                case STATE_UNDERLINE:
                	change = handleUnderlineState(pos, currChar, nextChar, next2Char);
                    break;
                case STATE_INLINE_CODE:
                	change = handleInlineCodeState(pos, currChar, nextChar, next2Char);
                    break;
                case STATE_LINK:
                	change = handleLinkState(pos, currChar, nextChar, next2Char);
                    break;
                case STATE_URL:
                	change = handleUrlState(pos, currChar, nextChar, next2Char);
                	break;
                case STATE_IMAGE:
                	change = handleImageState(pos, currChar, nextChar, next2Char);
                	break;
                case STATE_IMAGE_SRC:
                	change = handleImageSourceState(pos, currChar, nextChar, next2Char);
                    break;
                case STATE_NEW_LINE:
                	change = handleNewLineState(pos, currChar, nextChar, next2Char);
                    break;
                case STATE_UNPARSABLE:
                	if (currChar == '\n' || currChar == '\u001C') {
                		// End of paragraphText
                		popAllStack( pos );
                    	change = SMD_LINE_PARSED;
                    }
                	else
                		change = NO_CHANGE;
                	break;
                default:
                    change = handleUnknownState(state, pos, currChar, nextChar, next2Char);
                    break;
            }
            
            //change reset notify
            if(change == PARSE_BREAK || change < NO_CHANGE) {
            	break;
            }
            if(stackPos == 0 && isFirstChange) //back to first
            	isFirstChange = false;
            
            //next loop to process
        	pos++;
        }
        
        if(change == ROLLBACK && isFirstChange) {
        	buffer.reset();
    		//ending the last
    		endLine(buffer.position());
    		return SMD_LINE_INVALID;
        }
        
    	//can not parse to any kind of marker.
        if (change < NO_CHANGE) {
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
    		for (int j = stackPos; j >= 0; j--) {
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
    			case STATE_NEW_LINE:
    				//marker only, including the new line
    				markers.addStopMarker(state, endPosition+1);
    				break;
    			case STATE_NONE:
    				//nothing
    				break;
    			default:
    				//content end before, the marker end next char
    				markers.addStopContentMarker(state, endPosition, endPosition+1);
    				break;
    			}
    		}
    	}
    	else if(stack[0] == STATE_TEXT) //content includes the new line
    		markers.addStopContent(STATE_TEXT, endPosition+1);
    	
    	stack[0] = STATE_NONE;
    	stackPos = 0;
    }
    
    private int changeState(int state, int position, char ch, char nextChar, char next2Char) {
	    if (ch == '\n' || ch == '\u001C') { //new line, break at end as well
        	popAllStack(position); //not including new line
        	return PARSE_BREAK;
        }
        else if (Character.isWhitespace(ch) || Character.isISOControl(ch)) { //space or control char will be ignore
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
        } else if (ch == '`' && nextChar != '\0' && nextChar != '`') { //accept a white space followed
            // Check for inline code
        	nextState = STATE_INLINE_CODE;
        	endPosition = position+1;
        	change = CONSUME_1_CHAR;
        } else if (ch == '[' && !Character.isWhitespace(nextChar)) {	//non-white space followed
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
        else if (ch == '\\') {
        	if(Character.isWhitespace(nextChar)) {
	        	// Check for new line backslash
	        	nextState = STATE_NEW_LINE;
	        	change = CONSUME_1_CHAR;
        	}
        	else if(isChangeableTo(STATE_TEXT)) {//escape the char, no change state, no add marker.
        		markers.addStartMarker(STATE_NONE, position);
        		markers.addStopMarker(STATE_NONE, position+1);
        		if(state == STATE_NONE) {
        			pushStack( STATE_TEXT );
        			markers.addStartContent(STATE_TEXT, position+1);
        		}
        		return CONSUME_2_CHARS;
        	}
        }
    	
    	if(change != NO_CHANGE && isChangeableTo(nextState)) {
    		//push new stack, keeping stack[0] == STATE_NONE
    		pushStack( nextState );
    		switch(nextState) {
    			case STATE_TEXT:
    			case STATE_URL:
    			case STATE_IMAGE_SRC:
    				markers.addStartContent(nextState, endPosition);
    				break;
    			case STATE_NEW_LINE:
    				markers.addStartMarker(nextState, position);
    				break;
    			default:
    				markers.addStartMarkerContent(nextState, position, endPosition);
    				break;
    		}
        	return change;
    	}
    	
    	if(change != NO_CHANGE && markers.remaining() >= 4 + stackPos) { //next state is not changeable, then it stops parsing
    		pushStack( STATE_UNPARSABLE );
    		markers.addStartMarkerContent(STATE_UNPARSABLE, position, position);
        	return CONSUME_1_CHAR;
    	}
    	return NO_CHANGE;
    }
    
    private int handleUnknownState(int state, int position, char ch, char nextChar, char next2Char) {
    	throw new RuntimeException("Unknown state: " + state);
    }
    
    private int handleNoneState(boolean isFirstChange, int state, int position, char ch, char nextChar, char next2Char) {
    	if (ch == '\n' || ch == '\u001C') { //new line, break at end as well
        	popAllStack(position); //not including new line
        	return PARSE_BREAK;
        }
    	else if(Character.isWhitespace(ch) || Character.isISOControl(ch)) {
    		if(!isFirstChange) {
    			pushStack(STATE_TEXT);
				markers.addStartContent(STATE_TEXT, position);
				return CONSUME_1_CHAR;
    		}
    		return NO_CHANGE;
    	}
    	else {
	    	int change = changeState(state, position, ch, nextChar, next2Char);
	    	//NONE is changed to STATE_TEXT if get a UnicodeIdentifierPart
	    	if(change == NO_CHANGE && isFirstChange && Character.isUnicodeIdentifierPart(ch)) {
				//text at first because it never roll back.
				stack[0] = STATE_TEXT;
				//pushStack(STATE_TEXT);
				markers.addStartContent(STATE_TEXT, position);
				// Assume change to text if get any other character.
				change = CONSUME_1_CHAR;
	    	}
	        return change;
    	}
    }

    private int handleTextState(int state, int position, char ch, char nextChar, char next2Char) {
    	//no ending, except end of line.
    	//full switching by changeState;
    	return changeState(state, position, ch, nextChar, next2Char);
    }
    
    private int handleNewLineState(int position, char ch, char nextChar, char next2Char) {
    	//handle ending marker
    	if (ch == '\n' || ch == '\u001C') {
    		popAllStack(position); //not including new line
        	return PARSE_BREAK;
        }
    	else if(!Character.isWhitespace(ch)) {
    		//rollback
    		markers.rollbackLastMarkerContentStart(STATE_NEW_LINE);
    		popStack();
    	}
    	
    	return NO_CHANGE;
	}
    
    private int handleStrikeThroughState(int state, int position, char ch, char nextChar, char next2Char) {
    	//handle ending marker
    	if (ch == '~' && nextChar == '~' && next2Char != '~') {
            // End of strike through
    		markers.addStopContentMarker(STATE_STRIKETHROUGH, position, position+2);
    		popStack();
    		return CONSUME_2_CHARS;
        }
    	
    	//this changeState calling will never match for bold again, but italic is possible.
        return changeState(state, position, ch, nextChar, next2Char);
	}

    private int handleBoldState(int state, int position, char ch, char nextChar, char next2Char) {
    	//handle ending marker
    	if (ch == '*' && nextChar == '*') {
            // End of bold
    		markers.addStopContentMarker(STATE_BOLD, position, position+2);
    		popStack();
    		return CONSUME_2_CHARS;
        }
    	//this changeState calling will never match for bold again because repeated 2 *, but italic (1 x *) is possible.
        return changeState(state, position, ch, nextChar, next2Char);
    }

    private int handleItalicState(int state, int position, char ch, char nextChar, char next2Char) {
    	if (ch == '*') {
            // End of italic
    		popStack();
    		markers.addStopContentMarker(STATE_ITALIC, position, position+1);
    		return CONSUME_1_CHAR;
        }
    	//this changeState calling will never match for bold or italic again.
    	return changeState(state, position, ch, nextChar, next2Char);
    }

    private int handleUnderlineState(int position, char ch, char nextChar, char next2Char) {
    	if (ch == '_' && nextChar == '_') {
            // End of underline
    		popStack();
    		markers.addStopContentMarker(STATE_UNDERLINE, position, position+2);
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
    	
    	if (ch == '\n' || ch == '\u001C') {
    		// End of underline
        	popAllStack(position);
        	return PARSE_BREAK;
        }
    	return NO_CHANGE;
    }

    private int handleInlineCodeState(int position, char ch, char nextChar, char next2Char) {
    	int openPosition = markers.getLastMarkerPosition(); 
    			
        if (ch == '`') {
        	popStack();
            // End of inline code, openPosition is start of content
        	if(position == openPosition && markers.getLastMarkerState() == STATE_INLINE_CODE) {
        		int oldPos = markers.getLastMarkerPosition();
        		//empty between open/close
        		markers.rollbackLastMarkerContentStart(STATE_INLINE_CODE);
        		if(markers.getLastMarkerState() != STATE_TEXT)
        			markers.addStartContent(STATE_TEXT, oldPos);
        		return ROLLBACK;
        	}
        	else
        		markers.addStopContentMarker(STATE_INLINE_CODE, position, position+1);
        	return CONSUME_1_CHAR;
        }
        
        if (ch == '\n' || ch == '\u001C') {
        	// End of image and all the line
        	if(position == openPosition && markers.getLastMarkerState() == STATE_INLINE_CODE) {
        		//ending line
        		popStack();
        		int oldPos = markers.getLastMarkerPosition();
        		markers.rollbackLastMarkerContentStart(STATE_INLINE_CODE);
        		if(markers.getLastMarkerState() != STATE_TEXT)
        			markers.addStartContent(STATE_TEXT, oldPos);
        	}
        	popAllStack(position);
        	return PARSE_BREAK;
        }
        
        return NO_CHANGE;
    }

    private int handleLinkState(int position, char ch, char nextChar, char next2Char) {
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
        
        if (ch == ']' || ch == '\n' || ch == '\u001C') { //invalid close of link, return back to value
        	popStack();
        	int oldPos = markers.getLastMarkerPosition();
        	markers.rollbackLastMarkerContentStart(STATE_LINK);
        	
        	if(markers.getLastMarkerState() != STATE_TEXT)
        		markers.addStartContent(STATE_TEXT, oldPos);
	        if (ch == '\n' || ch == '\u001C') {
	    		// Early end of Link with empty
	        	popAllStack(position );
	        }
	        return ROLLBACK;
        }
        
        return NO_CHANGE;
    }

    private int handleUrlState(int position, char ch, char nextChar, char next2Char) {
        // URL parsing logic
        // TODO: enhance of checking/parsing
        if (ch == ')') {
            // End of URL and end of Link as well
        	popStack();
        	markers.addStopContent(STATE_URL, position);
        	popStack();
        	markers.addStopMarker(STATE_LINK, position+1);
        	return CONSUME_1_CHAR;
        } else if (ch == '\n' || ch == '\u001C') {
    		// End of paragraphText
        	popAllStack(position );
        	return PARSE_BREAK;
        }
        return NO_CHANGE;
    }
    
    private int handleImageState(int position, char ch, char nextChar, char next2Char) {
        // Image parsing logic
        if (ch == ']' && nextChar == '(') {
            // Handle optional text within image
        	markers.addStopContent(STATE_IMAGE, position);
        	//follow by URL, already reserved markers' space
    		pushStack( STATE_IMAGE_SRC );
    		markers.addStartContent(STATE_IMAGE_SRC, position+2 );
    		return CONSUME_2_CHARS;
        }
        
        if (ch == ']' || ch == '\n' || ch == '\u001C') { //invalid close of image, return back to value
        	popStack();
        	int oldPos = markers.getLastMarkerPosition();
        	markers.rollbackLastMarkerContentStart(STATE_IMAGE);
        	
        	if(markers.getLastMarkerState() != STATE_TEXT)
        		markers.addStartContent(STATE_TEXT, oldPos);
	        if (ch == '\n' || ch == '\u001C') {
	    		// Early end of Link with empty
	        	popAllStack(position );
	        }
	        return ROLLBACK;
        }
        
        return NO_CHANGE;
    }

    private int handleImageSourceState(int position, char ch, char nextChar, char next2Char) {
    	// URL parsing logic
        // TODO: enhance of checking/parsing
        if (ch == ')') {
            // End of image source
        	popStack();
        	markers.addStopContent(STATE_IMAGE_SRC, position);
        	popStack();
        	markers.addStopMarker(STATE_IMAGE, position+1);
        	return CONSUME_1_CHAR;
        } else if (ch == '\n' || ch == '\u001C') {
    		// End of paragraphText
        	popAllStack(position );
        	return PARSE_BREAK;
        }
        return NO_CHANGE;
    }

	@Override
	public int compact(int position) {
		if(this.internalMarkers) {
			return markers.compactMarkers(position);
		}
		return position;
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