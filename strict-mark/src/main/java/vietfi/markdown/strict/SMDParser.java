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

import java.nio.CharBuffer;

public interface SMDParser {
	
	//initial state
	public static final int STATE_NONE = 0;
	
	//state of line/sub block of paragraph text
	public static final int STATE_PARAGRAPH = 1;
    public static final int STATE_TEXT = 2;
    public static final int STATE_STRIKETHROUGH = 3;
    public static final int STATE_BOLD = 4;
    public static final int STATE_ITALIC = 5;
    public static final int STATE_UNDERLINE = 6;
    public static final int STATE_INLINE_CODE = 7;
    public static final int STATE_LINK = 8;
    public static final int STATE_URL = 9;
    public static final int STATE_IMAGE = 10;
    public static final int STATE_IMAGE_SRC = 11;
    public static final int STATE_NEW_LINE = 12;
    
    //of code
    public static final int STATE_CODE_INDENT_BLOCK = 13;
    public static final int STATE_CODE_BLOCK = 14;
    public static final int STATE_CODE_LANGUAGE = 15;
    
    //of quote
    public static final int STATE_QUOTE_BLOCK = 16;
    //public static final int STATE_QUOTE_PARAGRAPH = 17;
    
    //of list items with sub item
    public static final int STATE_ORDERED_LIST = 20;
    public static final int STATE_UNORDERED_LIST = 21;
    public static final int STATE_LIST_ITEM = 22;
    public static final int STATE_LIST_INDENT = 23;
    
    //of heading
    public static final int STATE_HEADING_1 = 31;
    public static final int STATE_HEADING_2 = 32;
    public static final int STATE_HEADING_3 = 33;
    public static final int STATE_HEADING_4 = 34;
    public static final int STATE_HEADING_5 = 35;
    public static final int STATE_HEADING_6 = 36;
    
    public static final int STATE_HORIZONTAL = 37;
    public static final int STATE_HORIZONTAL_D = 38;
    public static final int STATE_HORIZONTAL_U = 39;
    
    public static final int STATE_UNPARSABLE = 127; //0x7F, Inappreciable any more.


	public final static int MINIMUM_BUFFER_SIZE = 512;
	
	public final static int LOWER_BUFFER_REMAININNG = 32;
	
	/**
	 * Void result, there is no decision by the parser back to the caller.
	 * Caller needs to fulfill more data, or at the last parser, make the buff is end of paragraph (U+2029) to mark it end. 
	 */
	public static final int SMD_VOID = 0;
	
	/**
	 * No block, it is empty line.
	 */
	public static final int SMD_BLOCK_GETS_EMPTY_LINE = 1;
	
	/**
	 * Block ending result, the block has parsed with ending of block.
	 */
	public static final int SMD_BLOCK_END = 2;
	
	/**
	 * Block parsed but there is no ending of the block found. Continue to parse next buffer.
	 * If it is really ended (no more content to read), append a paragraph separator in the end (U+2029) to
	 * get a BLOCK_END gracefully.
	 */
	public static final int SMD_BLOCK_CONTINUE = 3;
	
	/**
	 * Block parse but invalid. the buffer position will be reset.
	 * The current block can be print out.
	 * 
	 * Call next parser for parsing.
	 */
	public static final int SMD_BLOCK_INVALID = 4;
	
	/**
	 * This method to parse next block in buff, and return to caller for the next circle. This instance is state-full machine, it must be called consecutively in a stream. The buffer.position moves
	 * to character after the end final markers (it often is the new line). 
	 * 
	 * 1. SMD_VOID: undetermined block or empty buffer.
	 * 2. SMD_BLOCK_GETS_EMPTY_LINE: empty lines, the caller can move to next block. 
	 * 2. SMD_BLOCK_END:  parse block successfully ending. The produceHtml will write a complete output - with the ending marker.
	 * 3. SMD_BLOCK_CONTINUE: parse successfully and the block should be continued by next call. The produceHtml will write incomplete output (but next call until SMD_BLOCK_END).
	 * 4. SMD_BLOCK_INVALID: the block ending marker or inside format is invalid.
	 * 
	 * @param buff input char to parse
	 * @return the result of parse.
	 */
	public int parseNext(CharBuffer buff);
	
	/**
	 * end the block, if it is opening. 
	 * 
	 * @param position the end position (exclusive)
	 */
	public void endBlock(int position);
	
	/**
	 * compacting the buffer position to zero.
	 * 
	 * @param position current position.
	 * @return number of bytes compacted
	 */
	public int compact(int position);
	
	/**
	 * get the markers
	 * @return
	 */
	public SMDMarkers markers();
}
