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

public class UnparseableBlockParser implements SMDParser {

	protected final SMDMarkers markers;
	protected final boolean internalMarkers;
	
	public UnparseableBlockParser() {
		internalMarkers = true;
		markers = new SMDMarkers(256);
    }
    
    public UnparseableBlockParser(SMDMarkers markers) {
    	internalMarkers = false;
    	this.markers = markers;
	}
    
	@Override
	public int parseNext(CharBuffer buff) {
		
		buff.mark();
		char ch = '\0';
		
		int pos = buff.position();
		int startPos = pos;
		markers.addStartMarkerContent(STATE_UNPARSABLE, pos, pos);
		
		if(buff.hasRemaining()) {
			ch = buff.get();
			
			pos++;
			
			if(startPos+1 == pos && ch == '\n' || ch == '\u001C') {
				return SMD_BLOCK_GETS_EMPTY_LINE;
			}
			
			while(buff.hasRemaining()) {
				ch = buff.get();
				
				pos++;
				if(ch == '\n' || ch == '\u001C') {
					markers.addStopContentMarker(STATE_UNPARSABLE, pos, pos);
					return SMD_BLOCK_END; //ending
				}
			}
			
		}
		
		markers.rollbackLastMarkerContentStart(STATE_UNPARSABLE);
		//not found the line end, reset back
		buff.reset();
		return SMD_VOID;
	}

	@Override
	public void endBlock(int position) {
		markers.addStopContentMarker(STATE_UNPARSABLE, position, position);
	}
	
	@Override
	public int compact(int position) {
		int r = 1;
		if(internalMarkers)
			r = markers.compactMarkers(position);
		if(r<=0)
			return 0;
		return position;
	}

	@Override
	public SMDMarkers markers() {
		return markers;
	}

}
