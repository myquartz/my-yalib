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

public class SMDUnorderedListBlockParser implements SMDParser {
	public SMDUnorderedListBlockParser() {
		parser = new SMDListItemParser(0);
	}

	public SMDUnorderedListBlockParser(SMDMarkers markers) {
		parser = new SMDListItemParser(0, markers);
	}

	private final SMDListItemParser parser;
	private char markerType = '\0';
	private boolean ending = false;//the ending
	private int itemCount = 0;
	private int spacesOrTabLimit = 3;//space stop counts
	
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
		
		byte list = -1;
		while(!ending && buffer.hasRemaining()) {
			int startPos = buffer.position();
			if(list < 0)
				list = SMDListItemParser.lookForwardUnorderedList(buffer, markerType);
			
			if(list > 0) {
				//next sub list
				spacesOrTabLimit = 3;
				if(itemCount > 0) { //end the last LI
					parser.endLine(startPos);
					parser.markers().addStopMarker(STATE_LIST_ITEM, startPos);
					parser.reset();
				}
				else {
					parser.reset();
					parser.markers().addStartMarker(STATE_UNORDERED_LIST, startPos);
					markerType = buffer.get(startPos);
				}
				
				parser.markers().addStartMarker(STATE_LIST_ITEM, startPos);
				//next 2 chars
				buffer.get(); buffer.get();
				int r = parser.parseLine(buffer);
				if(r == SMDLineParser.SMD_LINE_PARSED || r == SMDLineParser.SMD_LINE_PARSED_END) {
					if(r == SMDLineParser.SMD_LINE_PARSED_END) {
						parser.markers().addStopMarker(STATE_UNORDERED_LIST, buffer.position());
						ending = true;
						return SMD_BLOCK_END;
					}
					else
						itemCount++;
				}
				else if(r == SMDLineParser.SMD_LINE_VOID) {
					buffer.position(startPos); //reset back to start of line
					parser.markers().rollbackLastMarkerContentStart(STATE_LIST_ITEM);
					if(itemCount == 0) {
						parser.markers().rollbackLastMarkerContentStart(STATE_UNORDERED_LIST);
						markerType = '\0';
					}
					return SMD_VOID;
				}
				else {
					if(itemCount == 0) {
						parser.markers().rollbackLastMarkerContentStart(STATE_LIST_ITEM);
						parser.markers().rollbackLastMarkerContentStart(STATE_UNORDERED_LIST);
					}
					else {
						parser.markers().rollbackLastMarkerContentStart(STATE_LIST_ITEM);
						parser.markers().addStopMarker(STATE_UNORDERED_LIST, startPos);
						ending = true;
					}
					buffer.position(startPos); //reset
					return SMD_BLOCK_INVALID;
				}
				list = -1;
			}
			else { //list <= 0
				boolean blankLine = SMDLineParser.detectBlankLine(buffer);
			
				if(itemCount == 0) {
					if(blankLine) {
						SMDLineParser.consumeBlankLine(buffer);
						return SMD_BLOCK_GETS_EMPTY_LINE;
					}
					return SMD_BLOCK_INVALID;
				}
				else { //list <= 0 and in the middle of list
					final int sp = SMDLineParser.lookForwardTabOrSpaces(buffer, spacesOrTabLimit, 1);
					
					if(sp >= 2) { //a tab or more than 2 or 3 spaces, depending on parser
						if(!blankLine)
							parser.markers().addStartMarker(STATE_LIST_INDENT, startPos);
						//next 2 chars or 1 tab
						int pos = startPos + SMDLineParser.consumeSpaceOrTab(buffer, sp, 1);
						
						if(!blankLine)
							parser.markers().addStopMarker(STATE_LIST_INDENT, pos);
						//continue parsing of listParser
						int r = parser.parseLine(buffer);
						/*if(r == SMDLineParser.SMD_LINE_PARSED || r == SMDLineParser.SMD_LINE_PARSED_END || r == SMDLineParser.SMD_LINE_BLANK_OR_EMPTY) {
							//ok
						}
						else */
						if(r == SMDLineParser.SMD_LINE_INVALID) { //invalid, close the sub list item
							buffer.position(startPos); //reset back
							parser.markers().addStopMarker(STATE_LIST_ITEM, startPos);
							parser.markers().addStopMarker(STATE_UNORDERED_LIST, startPos);
							ending = true;
							return SMD_BLOCK_END;
						}
						if(r == SMDLineParser.SMD_LINE_VOID) {
							buffer.position(startPos); //reset back to start of line
							parser.markers().rollbackState(STATE_LIST_INDENT);
							return SMD_VOID;
						}
						//next line is a sub list
						if(spacesOrTabLimit > sp)
							spacesOrTabLimit = sp;
					}
					else {
						parser.endLine(buffer.position());
						if(blankLine) {
							SMDLineParser.consumeBlankLine(buffer);
						}
						byte nl = buffer.hasRemaining() ? SMDListItemParser.lookForwardUnorderedList(buffer, markerType) : 0;
						
						if(nl == 0) {
							//not a list anymore
							parser.markers().addStopMarker(STATE_LIST_ITEM, startPos);
							parser.markers().addStopMarker(STATE_UNORDERED_LIST, buffer.position());
							ending = true;
							spacesOrTabLimit = 3;
							return SMD_BLOCK_END;
						}
						else if(nl < 0) //not detected a next list
							break;
						else
							list = nl;
					}
				}
			}
		}
		
		return SMD_BLOCK_CONTINUE;
	}
	
	@Override
	public void endBlock(int position) {
		if(itemCount > 0) { //end the last LI
			parser.endLine(position);
			parser.reset();
			parser.markers().addStopMarker(STATE_LIST_ITEM, position);
			parser.markers().addStopMarker(STATE_ORDERED_LIST, position);
			ending = true;
			spacesOrTabLimit = 3;
		}
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
