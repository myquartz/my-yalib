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
 * List item parser is complex because it can parse indentation block for sub level of list.
 * For the sub-level, it calls recursively.
 * 
 * It will reset back to start of the line (not start of the block).
 * 
 * Example unordered list:
 * 
 * * Item 1 here with *italic* or [link](http://test.link)
 *   Next line **with bold text**.
 *   &gt; Quote of line
 *   &gt; another quote of __underscore__ line
 *   
 *   1. Sub ordered list
 *   2. another item
 * * the next item
 * 
 * It produces the markers:
 * <pre>
 * 
 * &lt;UL
 * '*' space&lt;LI	para_text[Item 1 here with &lt;*[italic]*&gt; or &lt;[link][http://test.link]&gt;\n]
 * space space	para_text[Next line &lt;**[with bold text]**&gt;.\n]
 * space space	&lt;quote '&gt;'	para_text[Quote of line\n]
 * space space	'&gt;'	para_text[another quote of &lt;__[underscore]__&gt; line\n]\n&gt;
 * space space	&lt;OL&lt;LI	para_text[Sub ordered list\n]&gt;
 * 		   		&lt;LI	para_text[another item\n]&gt;
 * 				&gt;
 * &gt;
 * * space &lt;LI	para_text[the next item\n]&gt;&gt;
 * 
 * </pre>
 */

public class SMDListItemParser extends SMDLineParser {
	
	
	protected final SMDMarkers markers;
	protected final boolean internalMarkers;
		
	//main kind
	private final SMDTextLineParser textParser;
	private final SMDCodeLineParser codeParser;
	private final SMDQuoteLineParser quoteParser;
	private final SMDListItemParser listParser;
	
	private final int level;
	private int itemLines = 0; //last item line counting
	private char subListItemType = '\0'; 
	private int subItemLines = 0; //sub item line counting
	private int currParser = PARSE_UNKNOWN;
	private int spacesOrTabLimit = DEFAULT_SPACES_THRESHOLD;
	protected boolean inSubParagraph = false;
	
	public SMDListItemParser()  {
		this(0);
	}
	
	public SMDListItemParser(int level)  {
		this.level = level;
		internalMarkers = true;
		markers = new SMDMarkers(256);
		
		textParser = new SMDTextLineParser(markers);
		codeParser = new SMDCodeLineParser(markers);
		quoteParser = new SMDQuoteLineParser(markers);
		if(level < 5)
			listParser = new SMDListItemParser(level + 1, markers);
		else
			listParser = null;
	}
	
	public SMDListItemParser(SMDMarkers markers)  {
		this(0, markers);
	}
	
	public SMDListItemParser(int level, SMDMarkers markers)  {
		this.level = level;
		internalMarkers = false;
		this.markers = markers;
		
		textParser = new SMDTextLineParser(markers);
		codeParser = new SMDCodeLineParser(markers);
		quoteParser = new SMDQuoteLineParser(markers);
		if(level < 5)
			listParser = new SMDListItemParser(level + 1, markers);
		else
			listParser = null;
	}
	
	protected static final int PARSE_UNKNOWN = 0;
	protected static final int PARSE_PARAGRAPH_TEXT = 1;
	protected static final int PARSE_CODE_LINE = 2;
	protected static final int PARSE_QUOTE_LINE = 3;
	protected static final int PARSE_ORDERED_LIST = 4;
	protected static final int PARSE_UNORDERED_LIST = 5;
	
	protected static final int DEFAULT_SPACES_THRESHOLD = 4;
	
	@Override
	public void endLine(int position) {
		switch(currParser) {
		case PARSE_PARAGRAPH_TEXT:
			textParser.endLine(position);
			if(inSubParagraph && subItemLines > 0) {
				markers.addStopMarker(STATE_PARAGRAPH, position);
			}
			break;
		case PARSE_CODE_LINE:
			codeParser.endLine(position);
			break;
		case PARSE_QUOTE_LINE:
			quoteParser.endLine(position);
			break;
		case PARSE_ORDERED_LIST:
		case PARSE_UNORDERED_LIST:
			listParser.endLine(position);
			markers.addStopMarker(STATE_LIST_ITEM, position);
			if(currParser == PARSE_ORDERED_LIST)
				markers.addStopMarker(STATE_ORDERED_LIST, position);
			else if(currParser == PARSE_UNORDERED_LIST)
				markers.addStopMarker(STATE_UNORDERED_LIST, position);
			break;
		}
		
		subItemLines = 0;
		currParser = PARSE_UNKNOWN;
		inSubParagraph = false;
		spacesOrTabLimit = DEFAULT_SPACES_THRESHOLD;
	}
	
	public void reset() {
		currParser = PARSE_UNKNOWN;
		itemLines = 0;
		subItemLines = 0;
		subListItemType = '\0';
		inSubParagraph = false;
		spacesOrTabLimit = DEFAULT_SPACES_THRESHOLD;
	}
	
	/**
	 * Parsing start at position of the char after the marker, or one indentSpaces (1 tab or 2-4 spaces).
	 *
	 * Note: the buffer position started right before the 'I' character. and 'N', '&gt;' consecutive repeating.
	 *   
	 *   @param buffer input data to parse
	 *   @return SMD line result code.
	 */
	@Override
	public int parseLine(CharBuffer buffer) {
		if(!buffer.hasRemaining())
			return SMD_LINE_VOID;
		
		int pos = buffer.position();
		
		if(itemLines == 0) {
			//the first line is always paragraph
			buffer.mark();
			char ch = buffer.get();
			
			//ch must not new line
			if(ch == '\n' || ch == '\u001C') {
				buffer.reset();
				return SMD_LINE_INVALID;
			}
			
			if(level > 0)
				markers.addStartMarker(STATE_LIST_ITEM, pos);
			
			pos++;
			while((ch == ' ' || ch == '\t') && buffer.hasRemaining()) {
				ch = buffer.get();
				pos++;
				if(!Character.isWhitespace(ch)) {
					break;
				}
			}
			if(ch == '\n' || ch == '\u001C') {
				//catch spaces only, that is invalid
				if(level > 0)
					markers.rollbackLastMarkerContentStart(STATE_LIST_ITEM);
				buffer.reset();
				return SMD_LINE_INVALID;
			}
			
			//back one char (non-white space char)
			buffer.position(pos-1);
			//first line is paragraph text
			int r = textParser.parseLine(buffer);
			assert r != SMD_LINE_BLANK_OR_EMPTY : "";
			if(r == SMD_LINE_PARSED || r == SMD_LINE_PARSED_END) {
				//don't change currParser = PARSE_PARAGRAPH_TEXT;
				itemLines++;
				return SMD_LINE_PARSED;
			}
			//VOID or INVALID
			if(level > 0)
				markers.rollbackLastMarkerContentStart(STATE_LIST_ITEM);
			buffer.reset();
			return r;
		}
		
		//second or later line
		int r = -1;
		//counting number of spaces
		final int sp = SMDLineParser.lookForwardTabOrSpaces(buffer, spacesOrTabLimit, 1);
		boolean blankLine = SMDLineParser.detectBlankLine(buffer);
		
		//continue parsing as the last, the indent spaces has been passed
		switch(currParser) {
		case PARSE_PARAGRAPH_TEXT:
			if(sp == 0 || blankLine) {
				//text there is no marker at start or end
				r = textParser.parseLine(buffer);
				
				if(inSubParagraph) {
					if(r == SMD_LINE_PARSED || r == SMD_LINE_PARSED_END) {
						if(r == SMD_LINE_PARSED_END) {
							markers.addStopMarker(STATE_PARAGRAPH, buffer.position());
							subItemLines = 0;
							inSubParagraph = false;
						}
						else {
							subItemLines++;
						}
					}
					else if( r == SMD_LINE_INVALID || r == SMD_LINE_BLANK_OR_EMPTY) {
						if(subItemLines > 0) {
							markers.addStopMarker(STATE_PARAGRAPH, buffer.position());
							subItemLines = 0;
							inSubParagraph = false;
						}
						this.currParser = PARSE_UNKNOWN;
						if(r == SMD_LINE_BLANK_OR_EMPTY) //blank line is marking a new paragraph.
							inSubParagraph = true;
					}
				}
				else if(r == SMD_LINE_BLANK_OR_EMPTY) //blank line is marking a new paragraph.
					inSubParagraph = true;
			}
			break;
		case PARSE_CODE_LINE:
			if(sp > 0 || blankLine) {
				r = codeParser.parseLine(buffer);
				if(r == SMD_LINE_INVALID || r == SMD_LINE_BLANK_OR_EMPTY) {
					this.currParser = PARSE_UNKNOWN;
					if(r == SMD_LINE_BLANK_OR_EMPTY) //blank line is marking a new paragraph.
						inSubParagraph = true;
				}
				else
					spacesOrTabLimit = 1;//code prefixes with 1 space.
			}
			else {
				codeParser.endLine(pos);
				r = SMD_LINE_INVALID;
				this.currParser = PARSE_UNKNOWN;
				spacesOrTabLimit = DEFAULT_SPACES_THRESHOLD;
			}
			break;
		case PARSE_QUOTE_LINE:
			if(sp == 0 || blankLine) {
				r = quoteParser.parseLine(buffer);
				if(r == SMD_LINE_INVALID || r == SMD_LINE_BLANK_OR_EMPTY)
					this.currParser = PARSE_UNKNOWN;
				if(r == SMD_LINE_BLANK_OR_EMPTY) //blank line is marking a new paragraph.
					inSubParagraph = true;
			}
			break;
		case PARSE_ORDERED_LIST:
		case PARSE_UNORDERED_LIST:
			int threshold = (currParser == PARSE_UNORDERED_LIST ? 2 : 3);
			if(sp >= threshold) { //a tab or more than 2 or 3 spaces, depending on parser
				markers.addStartMarker(STATE_LIST_INDENT, pos);
				//consume next 2-3 spaces or 1 tab
				pos += consumeSpaceOrTab(buffer, Math.min(threshold, sp), 1);
				
				markers.addStopMarker(STATE_LIST_INDENT, pos);
				//continue parsing of listParser
				r = listParser.parseLine(buffer);
				if(r == SMD_LINE_PARSED || r == SMD_LINE_PARSED_END || r == SMD_LINE_BLANK_OR_EMPTY) {
					subItemLines++;
				}
				else if(r == SMD_LINE_INVALID) { //invalid, close the sub list item
					buffer.position(pos); //reset back
					markers.addStopMarker(STATE_LIST_ITEM, pos);
					if(currParser == PARSE_ORDERED_LIST)
						markers.addStopMarker(STATE_ORDERED_LIST, pos);
					else if(currParser == PARSE_UNORDERED_LIST)
						markers.addStopMarker(STATE_UNORDERED_LIST, pos);
					subItemLines = 0;
					this.subListItemType = '\0';
					this.currParser = PARSE_UNKNOWN;
				}
				if(spacesOrTabLimit > sp)
					spacesOrTabLimit = sp;
			}
			else {
				byte list = sp == 0 ? currParser == PARSE_UNORDERED_LIST 
								?  lookForwardUnorderedList(buffer, this.subListItemType)
								: lookForwardOrderedList(buffer, this.subListItemType)
								: 0;
				
				if(list > 0) {
					if(subItemLines > 0) {
						listParser.endLine(pos);
						markers.addStopMarker(STATE_LIST_ITEM, pos);
						markers.addStartMarker(STATE_LIST_ITEM, pos);
					}
					//continue next item of sub-list
					//next 2 chars
					buffer.get(); buffer.get();
					if(currParser == PARSE_ORDERED_LIST)
						SMDLineParser.consumeUtilCatchSpace(buffer);
					r = listParser.parseLine(buffer);
					if(r == SMD_LINE_PARSED || r == SMD_LINE_PARSED_END) {
						if(r == SMD_LINE_PARSED_END) {
							if(currParser == PARSE_ORDERED_LIST)
								markers.addStopMarker(STATE_ORDERED_LIST, pos);
							else if(currParser == PARSE_UNORDERED_LIST)
								markers.addStopMarker(STATE_UNORDERED_LIST, pos);
							subItemLines = 0;
							this.subListItemType = '\0';
						}
						else
							subItemLines++;
					}
					else {
						markers.rollbackLastMarkerContentStart(STATE_LIST_ITEM);
						buffer.position(pos); //reset
						if(currParser == PARSE_ORDERED_LIST)
							markers.addStopMarker(STATE_ORDERED_LIST, pos);
						else if(currParser == PARSE_UNORDERED_LIST)
							markers.addStopMarker(STATE_UNORDERED_LIST, pos);
						subItemLines = 0;
						this.subListItemType = '\0';
						this.currParser = PARSE_UNKNOWN;
					}
				}
				else {
					listParser.endLine(pos);
					markers.addStopMarker(STATE_LIST_ITEM, pos);
					if(blankLine) {
						//consume the line ending
						SMDLineParser.consumeBlankLine(buffer);	
						pos = buffer.position();
					}
					
					if(currParser == PARSE_ORDERED_LIST)
						markers.addStopMarker(STATE_ORDERED_LIST, pos);
					else if(currParser == PARSE_UNORDERED_LIST)
						markers.addStopMarker(STATE_UNORDERED_LIST, pos);
					this.subListItemType = '\0';
					this.currParser = PARSE_UNKNOWN;
					this.spacesOrTabLimit = DEFAULT_SPACES_THRESHOLD;
					if(blankLine) {
						r = SMD_LINE_BLANK_OR_EMPTY;
						inSubParagraph = true;
					}
					else
						r = list < 0 ? SMD_LINE_VOID : SMD_LINE_INVALID;
				}
			}
			break;
		}
		
		if(r == SMD_LINE_PARSED || r == SMD_LINE_PARSED_END || r == SMD_LINE_BLANK_OR_EMPTY) {//parsed
			/*if(r == SMD_LINE_BLANK_OR_EMPTY) { //blank line is marking a new paragraph.
				inSubParagraph = true;
			}
			else if(r == SMD_LINE_PARSED_END)
				currParser = PARSE_UNKNOWN;*/
			itemLines++;
			return r == SMD_LINE_PARSED_END ? SMD_LINE_PARSED : r;
		}
		
		if((r == SMD_LINE_INVALID || r == -1) && blankLine) { //consume the blank line
			char ch = buffer.get(); //consume until the new line.
			pos++;
			while(buffer.hasRemaining() 
					&& !(ch == '\n' || ch == '\u001C')) {
				ch = buffer.get(); //consume until the new line.
				
			}
			if(blankLine)
				inSubParagraph = true;
			return SMD_LINE_BLANK_OR_EMPTY;
		}
		
		if(r == SMD_LINE_VOID || sp < 6 && buffer.remaining() < 6
				&& !SMDLineParser.detectBlankLine(buffer)) { //can not determine the complete line
			return SMD_LINE_VOID;
		}
		
		//try to find a new parser
		
		if(sp == 0) { //no space prefixed or new line only, starting a new parser.
			if((r == -1 || r == SMD_LINE_INVALID) && currParser != PARSE_UNORDERED_LIST && listParser != null) {
				
				byte l = lookForwardUnorderedList(buffer, '\0');
				
				if(l < 0) {
					if(blankLine) {
						//consume the line ending
						SMDLineParser.consumeBlankLine(buffer);
					}
					return blankLine ? SMD_LINE_BLANK_OR_EMPTY : SMD_LINE_VOID;
				}
				else if(l > 0) { //match
					this.subListItemType = buffer.get(pos);
					markers.addStartMarker(STATE_UNORDERED_LIST, pos);
					//next 2 chars
					buffer.get(); buffer.get();
					//start of new list item.
					listParser.reset();
					r = listParser.parseLine(buffer);
					if(r == SMD_LINE_PARSED) {
						itemLines++;
						subItemLines++; //one
						currParser = PARSE_UNORDERED_LIST;
					}
					else {
						buffer.position(pos); //reset
						markers.rollbackLastMarkerContentStart(STATE_UNORDERED_LIST);
					}
				}
			}
			
			if((r == -1 || r == SMD_LINE_INVALID) && currParser != PARSE_ORDERED_LIST && listParser != null) {
				byte l = lookForwardOrderedList(buffer,'\0');
				
				if(l < 0) {
					if(blankLine) {
						//consume the line ending
						SMDLineParser.consumeBlankLine(buffer);
					}
					return blankLine ? SMD_LINE_BLANK_OR_EMPTY : SMD_LINE_VOID;
				}
				else if(l > 0) { //match
					this.subListItemType = buffer.get(pos);
					markers.addStartMarker(STATE_ORDERED_LIST, pos);
					//next 3 chars
					SMDLineParser.consumeUtilCatchSpace(buffer);
					//start of new list item.
					listParser.reset();
					r = listParser.parseLine(buffer);
					if(r == SMD_LINE_PARSED) {
						itemLines++;
						subItemLines++; //one
						currParser = PARSE_ORDERED_LIST;
					}
					else {
						buffer.position(pos); //reset
						markers.rollbackLastMarkerContentStart(STATE_ORDERED_LIST);
					}
				}
			}
			
			if((r == -1 || r == SMD_LINE_INVALID) && currParser != PARSE_QUOTE_LINE) {
				//quote block line parsing has its-own markers already 
				r = quoteParser.parseLine(buffer);
				if(r == SMD_LINE_PARSED)
					currParser = PARSE_QUOTE_LINE;
			}
			
			if((r == -1 || r == SMD_LINE_INVALID) && currParser != PARSE_PARAGRAPH_TEXT) {
				if(inSubParagraph)
					markers.addStartMarker(STATE_PARAGRAPH, pos); //not including the last new line
				//text there is no marker at start or end
				r = textParser.parseLine(buffer);
				
				if(inSubParagraph) {
					if( r == SMD_LINE_INVALID || r == SMD_LINE_BLANK_OR_EMPTY) {
						if(subItemLines > 0) {
							markers.addStopMarker(STATE_PARAGRAPH, buffer.position());
							subItemLines = 0;
							inSubParagraph = false;
						}
						else {
							inSubParagraph = false;
							markers.rollbackLastMarkerContentStart(STATE_PARAGRAPH);
						}
					}
					else if(r == SMD_LINE_PARSED || r == SMD_LINE_PARSED_END) {
						currParser = PARSE_PARAGRAPH_TEXT;
						subItemLines++;
					}
					else if(subItemLines == 0) {
						inSubParagraph = false;
						markers.rollbackLastMarkerContentStart(STATE_PARAGRAPH);
					}
				}
				else if(r == SMD_LINE_PARSED || r == SMD_LINE_PARSED_END) {
					currParser = PARSE_PARAGRAPH_TEXT;
				}
			}
		}
		
		//else sp > 0, or not parsed, treat all other line is code
		if((r == -1 || r == SMD_LINE_INVALID)) {
			//code line parsing has its-own markers already
			r = codeParser.parseLine(buffer);
			if(r == SMD_LINE_PARSED)
				currParser = PARSE_CODE_LINE;
		}
		
		if(r == SMD_LINE_PARSED || r == SMD_LINE_PARSED_END) {
			if(currParser != PARSE_PARAGRAPH_TEXT)
				inSubParagraph = false;
			itemLines++;
			return r;
		}
		
		if(r == SMD_LINE_VOID) { //can not determine the complete line
			buffer.position(pos);
			return SMD_LINE_VOID;
		}
		
		markers.addStopMarker(STATE_LIST_ITEM, buffer.position());
		//void, invalid or parsed
		return r;
	}

	/**
	 * Detect sub unordered list by checking 3 forward characters  
	 * The result will be:
	 * 
	 * 1: the list is possible of the first line (* item 1) 
	 * 0: it is not an unordered list.
	 * -1: not enough characters
	 * 
	 * @param buffer to look
	 * @param subListItemType the type of item to look for
	 * @return 
	 */
	public static byte lookForwardUnorderedList(CharBuffer buffer, char subListItemType) {
		if(!buffer.hasRemaining())
			throw new IllegalArgumentException();
		
		if(buffer.remaining() < 2) //at least 2 characters
			return -1;
		
		int pos = buffer.position();
		
		char first = buffer.get(pos);
		char second = buffer.get(pos+1);
		
		if((subListItemType == '\0' && (first == '*' || first == '-' || first == '+')
			|| subListItemType != '\0' && first == subListItemType)
				&& (second == ' ' || second == '\t'))
			return 1;
		
		return 0;
	}
	
	/**
	 * Detect sub unordered list by checking 3 to 5 forward characters  
	 * The result will be:
	 * 
	 * 1: the list is possible of the first line ( 1. item 1 or a. or A. or i.) 
	 * 0: it is not an unordered list.
	 * -1: not enough characters
	 * 
	 * @param buffer to look
	 * @param subListItemType the type of item to look for
	 * @return 
	 */
	public static byte lookForwardOrderedList(CharBuffer buffer, char subListItemType) {
		if(!buffer.hasRemaining())
			throw new IllegalArgumentException();
		
		if(buffer.remaining() < 4) //at least 4 characters
			return -1;
		
		int pos = buffer.position();
		
		char first = buffer.get(pos);
		char second = buffer.get(pos+1);
		char third = buffer.get(pos+2);
		char forth = buffer.get(pos+3);
		
		if(subListItemType == '\0' && (second == '.' || second == ')') && (third == ' ' || third == '\t')
			&& (first == '1' || first == 'a' || first == 'A' || first == 'i')) {
			return 1;
		}
		
		if(subListItemType != '\0') {
			switch(subListItemType) {
			case '1':
				//numbering
				if(Character.isDigit(first) && Character.isDigit(second) && (third == '.' || third == ')') && (forth == ' ' || forth == '\t')
						|| Character.isDigit(first) && (second == '.' || second == ')') && (third == ' ' || third == '\t'))
					return 1;
				break;
			case 'a':
				//alphabetic lower case
				if(Character.isLetter(first) && Character.isLowerCase(first)
						&& (second == '.' || second == ')') && (third == ' ' || third == '\t'))
					return 1;
				break;
			case 'A':
				//alphabetic upper case
				if(Character.isLetter(first) && Character.isUpperCase(first)
						&& (second == '.' || second == ')') && (third == ' ' || third == '\t'))
					return 1;
				break;
			case 'i':
				//Latin numbering
				char dot = '\0';
				char space = '\0';

				if(first == 'i' || first == 'v' || first == 'x') {
					if(second == 'i' || second == 'v' || second == 'x') {
						if(third == 'i' || third == 'v' || third == 'x') {
							if(buffer.remaining() < 5) //at least 5 characters
								return -1;
							char fifth = buffer.get(pos+4);
							if(forth == 'i' || forth == 'v' || forth == 'x') {
								if(buffer.remaining() < 6) //at least 6 characters
									return -1;
								dot = fifth;
								space = buffer.get(pos+5);
							}
							else {
								dot = forth;
								space = fifth;
							}
						}
						else {
							dot = third;
							space = forth;
						}
					}
					else {
						dot = second;
						space = third;
					}
				}
				
				
				if((dot == '.' || dot == ')') && (space == ' ' || space == '\t'))
					return 1;
				break;
			}
		}
		
		return 0;
	}
	
	@Override
	public int compact(int shiftRemaining) {
		this.codeParser.compact(shiftRemaining);
		this.quoteParser.compact(shiftRemaining);
		this.textParser.compact(shiftRemaining);
		if(listParser != null)
			this.listParser.compact(shiftRemaining);
		return shiftRemaining;
	}

	@Override
	public SMDMarkers markers() {
		return this.markers;
	}

	@Override
	public int parseNext(CharBuffer buff) {
		return parseLine(buff);
	}
}
