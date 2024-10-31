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
import vietfi.markdown.strict.line.SMDCodeLineParser;
import vietfi.markdown.strict.line.SMDLineParser;

/**
 * parsing code block by indent, at least one space or tab started each line
 * This implementation will parse line by line. It requires SMDCodeLineParser.
 * 
 * example 1:
 * \s //javascript
 * \s var x = "your code, any text, &lt;&gt; escaped.";
 * 
 * example 2:
 * \t //javascript
 * \t var x = "your code, any text, &lt;&gt; escaped."; 
 * 
 * The markers of this kind are:
 * 
 * &lt; space [code line 1\n] space [code line 2\n] ... [the last code line\n ]&gt; \n
 * 
 */
public class SMDCodeByIndentBlockParser implements SMDParser {

	private final SMDCodeLineParser parser;
	
	public SMDCodeByIndentBlockParser() {
		parser = new SMDCodeLineParser(256);
    }
    
    public SMDCodeByIndentBlockParser(SMDMarkers markers) {
    	parser = new SMDCodeLineParser(markers);
	}

	@Override
	public int parseNext(CharBuffer buffer) {
		if(!buffer.hasRemaining())
			return SMD_VOID;
		
		while(buffer.hasRemaining()) {
			int r = parser.parseLine(buffer);
			switch(r) {
			case SMDLineParser.SMD_LINE_VOID:
				return SMD_VOID;
			
			case SMDLineParser.SMD_LINE_BLANK_OR_EMPTY:
				if(parser.getCodeLines() == 0)
					return SMD_BLOCK_GETS_EMPTY_LINE;
			case SMDLineParser.SMD_LINE_INVALID:
				if(parser.getCodeLines() > 0)
					return SMD_BLOCK_END;
				return SMD_BLOCK_INVALID;
			case SMDLineParser.SMD_LINE_PARSED_END:
			case SMDLineParser.SMD_LINE_PARSED:
				break;
			default:
				throw new IllegalStateException();
			}
		}
		
		return SMD_BLOCK_CONTINUE;
	}

	@Override
	public void endBlock(int position) {
		parser.endLine(position);
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
