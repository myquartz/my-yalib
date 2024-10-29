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
import vietfi.markdown.strict.line.SMDLineParser;
import vietfi.markdown.strict.line.SMDListItemParser;

public class SMDOrderedListBlockParser implements SMDParser {
	
	public SMDOrderedListBlockParser() {
		parser = new SMDListItemParser(0);
	}

	public SMDOrderedListBlockParser(SMDMarkers markers) {
		parser = new SMDListItemParser(0, markers);
	}

	private final SMDListItemParser parser;
	private char markerType = '\0';
	private boolean ending = false;//the ending
	private int itemCount = 0; //li counts
	
	@Override
	public int parseNext(CharBuffer buffer) {
		if(!buffer.hasRemaining())
			return SMD_VOID;
		
		//starting a new block
		if(ending) {
			markerType = '\0';
			itemCount = 0;
			ending = false;
		}
		
		while(!ending && buffer.hasRemaining()) {
			int startPos = buffer.position();
			int l = SMDListItemParser.lookForwardOrderedList(buffer, markerType);
			
			if(l > 0) {
				if(itemCount > 0) { //end the last LI
					parser.endLine(startPos);
					parser.markers().addStopMarker(STATE_LIST_ITEM, startPos);
					parser.reset();
				}
				else {
					parser.reset();
					parser.markers().addStartMarker(STATE_ORDERED_LIST, startPos);
					markerType = buffer.get(startPos);
				}
				
				parser.markers().addStartMarker(STATE_LIST_ITEM, startPos);

				SMDListItemParser.consumeUtilCatchSpace(buffer);
					
				int r = parser.parseLine(buffer);
				if(r == SMDLineParser.SMD_LINE_PARSED || r == SMDLineParser.SMD_LINE_PARSED_END) {
					if(r == SMDLineParser.SMD_LINE_PARSED_END) {
						parser.markers().addStopMarker(STATE_ORDERED_LIST, buffer.position());
						ending = true;
						return SMD_BLOCK_END;
					}
					else
						itemCount++;
				}
				else {
					if(itemCount == 0)
						parser.markers().rollbackLastMarkerContentStart(STATE_ORDERED_LIST);
					else {
						parser.markers().rollbackLastMarkerContentStart(STATE_LIST_ITEM);
						parser.markers().addStopMarker(STATE_ORDERED_LIST, startPos);
						ending = true;
					}
					buffer.position(startPos); //reset
					return SMD_BLOCK_INVALID;
				}
			}
			else {
				boolean blankLine = SMDListItemParser.detectBlankLine(buffer);
				if(itemCount == 0) {
					if(blankLine) {
						SMDListItemParser.consumeBlankLine(buffer);
						return SMD_BLOCK_GETS_EMPTY_LINE;
					}
					return SMD_BLOCK_INVALID;
				}
				else { //l <= 0 and in the list
					int sp = SMDListItemParser.lookForwardTabOrSpaces(buffer, 3, 1);
					
					if(sp >= 2) { //a tab or more than 2 or 3 spaces, depending on parser
						if(!blankLine)
							parser.markers().addStartMarker(STATE_LIST_INDENT, startPos);
						//next 2 chars or 1 tab
						char ch = ' ';
						int chType;
						int pos = startPos;
						
						while(sp >= 0) {
							ch = buffer.get();
							chType = Character.getType(ch);
							pos++;
							if(ch == '\t') {//a tab, don't care space any more
								break;
							}
							if((ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR)) {
								break;
							}
							sp--;
							assert buffer.hasRemaining():"invalid sp vs spc";
						}
						if(ch != ' ' && ch != '\t') {
							//because the indent2Spaces or indent3Spaces has one optional space
							//back the optional space if not space.
							pos--;
							buffer.position(pos);
						}
						if(!blankLine)
							parser.markers().addStopMarker(STATE_LIST_INDENT, pos);
						//continue parsing of listParser
						int r = parser.parseLine(buffer);
						/*if(r == SMDLineParser.SMD_LINE_PARSED || r == SMDLineParser.SMD_LINE_PARSED_END || r == SMDLineParser.SMD_LINE_BLANK_OR_EMPTY) {
							//ok
						}
						else */
						if(r == SMDLineParser.SMD_LINE_INVALID) { //invalid, close the sub list item
							buffer.position(pos); //reset back
							parser.markers().addStopMarker(STATE_LIST_ITEM, pos);
							parser.markers().addStopMarker(STATE_ORDERED_LIST, pos);
							ending = true;
							return SMD_BLOCK_END;
						}
					}
					else {
						parser.endLine(buffer.position());
						if(blankLine)
							SMDListItemParser.consumeBlankLine(buffer);
						//not a list anymore
						parser.markers().addStopMarker(STATE_LIST_ITEM, startPos);
						parser.markers().addStopMarker(STATE_ORDERED_LIST, buffer.position());
						ending = true;
						return SMD_BLOCK_END;
					}
					
				}
			}
		}
		
		return SMD_BLOCK_CONTINUE;
	}

	
	@Override
	public int compact(int position) {
		return parser.compact(position);
	}
	
	@Override
	public SMDMarkers markers() {
		return parser.markers();
	}
}
