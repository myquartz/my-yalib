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
package vietfi.markdown.strict;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import vietfi.markdown.strict.block.SMDCodeByIndentBlockParser;
import vietfi.markdown.strict.block.SMDCodeByMarkerBlockParser;
import vietfi.markdown.strict.block.SMDHeading12BlockParser;
import vietfi.markdown.strict.block.SMDHeadingByPoundsBlockParser;
import vietfi.markdown.strict.block.SMDHorizontalBlockParser;
import vietfi.markdown.strict.block.SMDOrderedListBlockParser;
import vietfi.markdown.strict.block.SMDParagraphParser;
import vietfi.markdown.strict.block.SMDQuoteBlockParser;
import vietfi.markdown.strict.block.SMDUnorderedListBlockParser;
import vietfi.markdown.strict.block.UnparseableBlockParser;

/** 
 * This class to create a chain of SMDParser to parse the markdown buffer into markers.
 * 
 *  The markers can be use for generate HTML or XHTML.
 *  
 */
public class SMDParserChain implements SMDParser {

	private final SMDMarkers markers;
	private final SMDParser[] parsers;
	private SMDParser current = null;
	
	private SMDParserChain(SMDMarkers markers, SMDParser... parsers) {
		this.markers = markers;
		this.parsers = parsers;
	}
	
	/**
	 * Create a chain of customized list of parser.
	 * 
	 * The Unparseable parser is always created at the last of chain.
	 * 
	 * @param parserClasses Class of parsers to use.
	 * @return the SMDParser to use.
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SafeVarargs
	public static SMDParser createParserOf(Class<SMDParser>... parserClasses) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		SMDMarkers markers = new SMDMarkers(4096);
		List<SMDParser> list = new ArrayList<>(parserClasses.length);
		for(Class<SMDParser> pc : parserClasses) {
			Constructor<SMDParser> cons = pc.getConstructor(SMDMarkers.class);
			list.add(cons.newInstance(markers));
		}
		list.add(new UnparseableBlockParser(markers));
		
		return new SMDParserChain(markers, list.toArray(new SMDParser[list.size()]));
	}
	
	/**
	 * Create a chain for parsing content block only (unordered list, ordered list, code by marker and by indentation, quote and paragraph).
	 * 
	 * @return a parser for content parsing.
	 */
	public static SMDParser createParserOfContents() {
		SMDMarkers markers = new SMDMarkers(1024);
		
		return new SMDParserChain(markers,
				
				new SMDOrderedListBlockParser(markers),
				new SMDUnorderedListBlockParser(markers),
				
				new SMDCodeByIndentBlockParser(markers),
				new SMDCodeByMarkerBlockParser(markers),
				
				new SMDQuoteBlockParser(markers),
				
				new SMDParagraphParser(markers),
				
				new UnparseableBlockParser(markers));
	}
	
	/**
	 * Create of standard chain with the following parsers:
	 * 
	 * 1. Heading by dash/equals
	 * 2. Heading by pounds
	 * 3. Horizontal line
	 * 4. Ordered list
	 * 5. Unordered list.
	 * 6. Quote block
	 * 7. Code block (by marker and by indentation)
	 * 8. Paragraph
	 * 9. Unparseable (everything else).
	 *
	 * This chain has implemented all syntax of Strict Mark Syntax  
	 * 
	 * Please review document at https://github.com/myquartz/my-yalib/tree/main/strict-mark/docs
	 * 
	 * @return a parser for page parsing
	 * 
	 */
	public static SMDParser createParserOfStandard() {
		return createParserOfStandard(4096);
	}
	
	/**
	 * Create of standard chain, same description as  createParserOfStandard.
	 * 
	 * @param markerSize the size of markers to parse.
	 * @return same as createParserOfStandard
	 * @see createParserOfStandard()
	 */
	public static SMDParser createParserOfStandard(int markerSize) {
		SMDMarkers markers = new SMDMarkers(markerSize);
		
		return new SMDParserChain(markers,
				new SMDHorizontalBlockParser(markers),
				
				new SMDHeading12BlockParser(markers),
				new SMDHeadingByPoundsBlockParser(markers),
				
				new SMDOrderedListBlockParser(markers),
				new SMDUnorderedListBlockParser(markers),
				
				new SMDCodeByIndentBlockParser(markers),
				new SMDCodeByMarkerBlockParser(markers),
				
				new SMDQuoteBlockParser(markers),
				
				new SMDParagraphParser(markers),
				
				new UnparseableBlockParser(markers));
	}
	
	/**
	 * Parse the buffer until there is no remaining to read.
	 * 
	 * After calling, render the markers, then buffer should be fulfill afterward.
	 * 
	 * 1. If no more to read, add 2 new lines character then calling it again to mark its' end.
	 * 2. If buffer compacted before next call, call the compact method with the buffer.position() to shift all markers position.
	 * 
	 * Return constants at SMDParser:
	 * 
	 * 1. SMD_VOID: no new line at end.
	 * 2. SMD_BLOCK_CONTINUE: parsed successfully, wait for continue. 
	 * 
	 * @return code for continue or not.
	 */
	@Override
	public int parseNext(CharBuffer buff) {
		if(!buff.hasRemaining())
			return SMD_VOID;
		int ret = -1;
		
		while(buff.hasRemaining()) {
			if(current != null) {//try to parsing
				ret = current.parseNext(buff);
				if(ret == SMD_BLOCK_CONTINUE) {
					return SMD_BLOCK_CONTINUE;
				}
				if(ret == SMD_BLOCK_END || ret == SMD_BLOCK_GETS_EMPTY_LINE) {
					current = null;
					continue;
				}
			}

			current = null;
			//invalid, try another in order.
			for(SMDParser p : parsers) {
				ret = p.parseNext(buff);
				if(ret == SMD_VOID) {//no new line at end
					if(p == parsers[parsers.length-1])
						return SMD_VOID;
				}
				if(ret == SMD_BLOCK_GETS_EMPTY_LINE || ret == SMD_BLOCK_END || ret == SMD_BLOCK_CONTINUE) {
					if(ret == SMD_BLOCK_CONTINUE) {
						current = p;
						return SMD_BLOCK_CONTINUE;
					}
					//another loop
					break;
				}
				//else try next INVALID
			}
		}
		if(ret < 0)
			ret = SMD_VOID;
		return ret;
	}

	@Override
	public void endBlock(int position) {
		if(current != null) {//try to end the current
			current.endBlock(position);
			current = null;
		}
	}
	
	/**
	 * Compact the markers along with buffer compacting
	 */
	@Override
	public int compact(int position) {
		int min = position;
		for(SMDParser p : parsers) {
			int x = p.compact(position);
			if(min > x)
				min = x;
		}
		return min;
	}

	/**
	 * Get the markers, there are 4096 element by default.
	 */
	@Override
	public SMDMarkers markers() {
		return markers;
	}

}
