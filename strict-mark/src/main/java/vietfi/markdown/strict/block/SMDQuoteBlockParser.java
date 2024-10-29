
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
import vietfi.markdown.strict.line.SMDQuoteLineParser;

/**
 * Quote block parser. Parsing the quote block by prefix '&gt;' + space character.
 * 
 * example 1:
 * 
 * &gt; This quoted string or paragraph is
 * &gt; **wrapped** in quoted. It support **bold** or [link](http://to.web/site) likes paragraph.
 * &gt;
 * &gt; Another paragraph.
 * 
 * then it produces:
 * 
 * <blockquote><p>This quoted string or paragraph is
 * <b>wrapped</b> in quoted. It support <b>bold</b> or <a href="http://to.web/site">link</a> likes paragraph.
 * </p><p>Another paragraph.
 * </p></blockquote>
 * 
 * The markers pattern are:
 * 
 * &lt;quote
 * '&gt;' space &lt;paragraph&lt;text[This quoted string or paragraph is] text&gt;\n
 * '&gt;' space &lt;**[wrapped]**&gt; in quoted. It support &lt;**[bold]**&gt; or &lt;[link][http://to.web/site]&gt; likes paragraph.] text&gt; paragraph&gt;\n
 * '&gt;' space &lt;paragraph&lt;text [Another paragraph.] text&gt;paragraph&gt;\n
 * quote&gt;
 * 
 */
public class SMDQuoteBlockParser implements SMDParser {

	private final SMDQuoteLineParser parser;
	
	public SMDQuoteBlockParser() {
		parser = new SMDQuoteLineParser();
    }
    
    public SMDQuoteBlockParser(SMDMarkers markers) {
    	parser = new SMDQuoteLineParser(markers);
	}

	@Override
	public int parseNext(CharBuffer buffer) {
		//simple loop
		while(buffer.hasRemaining()) {
			
			int r = parser.parseLine(buffer);
			switch(r) {
			case SMDLineParser.SMD_LINE_VOID:
				return SMD_VOID;
				
			case SMDLineParser.SMD_LINE_BLANK_OR_EMPTY:
				if(parser.getQuoteLines() == 0)
					return SMD_BLOCK_GETS_EMPTY_LINE;
			case SMDLineParser.SMD_LINE_INVALID:
				if(parser.getQuoteLines() > 0)
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
	public int compact(int position) {
		return parser.compact(position);
	}

	@Override
	public SMDMarkers markers() {
		return parser.markers();
	}
}
