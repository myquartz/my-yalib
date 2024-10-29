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
import vietfi.markdown.strict.line.SMDTextLineParser;

public class SMDParagraphParser implements SMDParser {
	
	private final SMDTextLineParser lineParser; 
	private int pLines = 0; //paragraph lines count
	private boolean ending = false; //to print the ending
	
	public SMDParagraphParser() {
		lineParser = new SMDTextLineParser(1024);
    }
    
    public SMDParagraphParser(SMDMarkers markers) {
    	lineParser = new SMDTextLineParser(markers);
	}
    
	@Override
	public int parseNext(CharBuffer buff) {	
		if(!buff.hasRemaining())
			return SMD_VOID;
	
		if(ending) {
			pLines = 0;
			ending = false;
		}
		
		//mark it the start of block.
		if(pLines == 0)
			lineParser.markers().addStartMarker(STATE_PARAGRAPH, buff.position());
		
		while(buff.hasRemaining()) {
			int r = lineParser.parseLine(buff);
			if(r == SMDLineParser.SMD_LINE_PARSED) {//it is a correct line
				pLines++;
			}
			//otherwise, empty line is the end marker
			else if(r == SMDLineParser.SMD_LINE_BLANK_OR_EMPTY || r == SMDLineParser.SMD_LINE_INVALID) {
				if(pLines > 0) {
					ending = true;
					lineParser.markers().addStopMarker(STATE_PARAGRAPH, buff.position());
					return SMD_BLOCK_END;
				}
				else
					lineParser.markers().rollbackLastMarkerContentStart(STATE_PARAGRAPH);
				return r == SMDLineParser.SMD_LINE_INVALID ? SMD_BLOCK_INVALID : SMD_BLOCK_GETS_EMPTY_LINE;
			}
			else if(r == SMDLineParser.SMD_LINE_VOID) {
				if(pLines == 0) {
					lineParser.markers().rollbackLastMarkerContentStart(STATE_PARAGRAPH);
				}
				return SMD_VOID;
			}
		
		}
		return SMD_BLOCK_CONTINUE;
	}

	@Override
	public int compact(int position) {
		return lineParser.compact(position);
	}

	@Override
	public SMDMarkers markers() {
		return lineParser.markers();
	}
	
}
