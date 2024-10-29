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
 * Parsing horizontal line
 * 
 * Example:
 * 
 * ====
 * 
 * ---
 * 
 * ____
 * 
 * The markers's pattern is:
 * 
 * &lt;====\n&gt;
 * 
 * or
 * 
 * &lt;---\n&gt;
 * 
 * or 
 * 
 * &lt;____\n&gt;
 * 
 */

public class SMDHorizontalBlockParser implements SMDParser {

	protected final SMDMarkers markers;
	protected final boolean internalMarkers;
	
	public SMDHorizontalBlockParser() {
		this.markers = new SMDMarkers(8);
		internalMarkers = true;
	}
	
	public SMDHorizontalBlockParser(SMDMarkers markers) {
		this.markers = markers;
		internalMarkers = false;
	}
	
	private char markerType = '\0';
	
	@Override
	public int parseNext(CharBuffer buff) {
		if(!buff.hasRemaining())
			return SMD_VOID;
		
		//mark for reset if needed
		buff.mark();
		int pos = buff.position();
		
		markerType = buff.get();
		pos++;
		if(!(markerType == '-' || markerType == '_' || markerType == '=')) {
			markerType = '\0';
			buff.reset();
			return SMD_BLOCK_INVALID;
		}
		else {
			markers.addStartMarker(markerType == '=' ? STATE_HORIZONTAL_D : markerType == '_' ? STATE_HORIZONTAL_U : STATE_HORIZONTAL, pos-1);
		}
		
		char ch = '\0';
		int chType = 0;
		int c = 2; //more 2 of marker, markers can be longer
		while(buff.hasRemaining()) {
			ch = buff.get();
			chType = Character.getType(ch);
			pos++;
			if(c > 0 && markerType != ch
				|| c < 0 && markerType != ch && !Character.isWhitespace(ch)) {
				markers.rollbackLastMarkerContentStart(markerType == '=' ? STATE_HORIZONTAL_D : markerType == '_' ? STATE_HORIZONTAL_U : STATE_HORIZONTAL);
				markerType = '\0';
				buff.reset();
				return SMD_BLOCK_INVALID;
			}
			//else is white space or markerType
			if(ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {
				markers.addStopMarker(markerType == '=' ? STATE_HORIZONTAL_D : markerType == '_' ? STATE_HORIZONTAL_U : STATE_HORIZONTAL, pos);
				return SMD_BLOCK_END;
			}
			
			c--;
		}
		markers.rollbackLastMarkerContentStart(markerType == '=' ? STATE_HORIZONTAL_D : markerType == '_' ? STATE_HORIZONTAL_U : STATE_HORIZONTAL);
		markerType = '\0';
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
