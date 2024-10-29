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
 * Parsing heading styles of the pound prepend line.
 * 
 * Example:
 * 
 * # Heading 1 here
 * 
 * ## \tHeading 2 here ##
 * 
 * The markers's pattern is:
 * 
 * &lt;# space [Heading 1 here]\n&gt;
 * 
 * or
 * 
 * &lt;## space tab [Heading 2 here]\n&gt;
 * 
 */

public class SMDHeadingByPoundsBlockParser implements SMDParser {

	protected final SMDMarkers markers;
	protected final boolean internalMarkers;
	
	public SMDHeadingByPoundsBlockParser() {
		this.markers = new SMDMarkers(8);
		internalMarkers = true;
	}
	
	public SMDHeadingByPoundsBlockParser(SMDMarkers markers) {
		this.markers = markers;
		internalMarkers = false;
	}
	
	@Override
	public int parseNext(CharBuffer buff) {
		if(!buff.hasRemaining())
			return SMD_VOID;
		
		int level = 0;
		int startPos = -1;
		int endPos = -1;
		
		int startMarker = 0;
		buff.mark();
		int chType;
		int pos = startMarker = buff.position();
		char ch = buff.get();
		pos++;
		
		if(ch != '#') {
			buff.reset();
			return SMD_BLOCK_INVALID;
		}
		
		if(ch == '#')
			level = 1;
		
		while(buff.hasRemaining()) {
			ch = buff.get();
			chType = Character.getType(ch);
			pos++;
			if(ch == '#')
				level++;
			
			if(level > 6 || ch != '#' && ch != ' ' && ch != '\t') {
				level = 0;
				buff.reset();
				return SMD_BLOCK_INVALID;
			}
			else if(ch == ' ' || ch == '\t') {
				markers.addStartMarker(STATE_HEADING_1 + level - 1, startMarker);
				break;
			}
		}
		
		//trailing pounds
		int pounds = 0;
		
		while(buff.hasRemaining()) {
			ch = buff.get();
			chType = Character.getType(ch);
			pos++;
			if(startPos < 0 && ch == ' ') {
				continue;
			}
			else if(startPos < 0) {
				if(ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {
					markers.rollbackLastMarkerContentStart(STATE_HEADING_1 + level - 1);
					buff.reset();
					return SMD_BLOCK_INVALID;
				}
				
				markers.addStartContent(STATE_HEADING_1 + level - 1, pos - 1);
				startPos = pos - 1;
			}
			else if(endPos > startPos) { //wait for ending
				if(ch == '#') {
					if(pounds < level)
						pounds++;
					else
						endPos = pos - pounds;
					continue;
				}
				else if(ch == ' ' || ch == '\t') { //ignore trailing space
					pounds = 0;
					continue;
				}
			}
			
			if(pos > startPos && ch != '#' && !Character.isWhitespace(ch)) //letter, digits...
				endPos = pos;
			//end of line
			else if(ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {
				if(endPos < 0) { //invalid
					markers.rollbackLastMarkerContentStart(STATE_HEADING_1 + level - 1);
					buff.reset();
					return SMD_BLOCK_INVALID;
				}
				markers.addStopMarkerContent(STATE_HEADING_1 + level - 1, endPos, pos);
				return SMD_BLOCK_END;
			}
		}
		
		markers.rollbackLastMarkerContentStart(STATE_HEADING_1 + level - 1);
		buff.reset();
		return SMD_VOID;
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
		return markers;
	}

}
