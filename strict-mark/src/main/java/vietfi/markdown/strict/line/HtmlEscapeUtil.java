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
package vietfi.markdown.strict.line;

import java.nio.CharBuffer;

public class HtmlEscapeUtil {

    /**
     * Escapes special HTML characters in a string to their safe representations.
     *
     * @param input The input string that may contain unsafe HTML characters.
     * @param safeQuote if true then produce safe quote for double / single quote character, otherwise return null; 
     * @return A string with special HTML characters replaced by safe HTML entities. if no need to quote, return null.
     */
    public static String escapeHtml(char input, boolean safeQuote) {
        switch (input) {
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '&':
                return "&amp;";
            case '\0':
                return "&#xFFFD;";
            case '"':
            	if(safeQuote)
            		return "&quot;";
                break;
//            case '\127':
//            	if(safeQuote)
//            		return "&apos;";
//                break;
            case '\'':
            	if(safeQuote)
            		return "&apos;";
                break;
            case '\n':
            	if(safeQuote)
            		return "&#10;";
                break;
            case '\r':
            	return "&#13;";
            case '\t':
            	if(safeQuote)
            		return "&nbsp;&nbsp;&nbsp;&nbsp;"; //replace with space
            	return "\t";
            default:
            	if(Character.isISOControl(input))
            		return "&#xFFFD;";
                break;
        }

        return null;
    }

    /**
     * Writes text from the input `buffer` to the `output` buffer, escaping unsafe characters for HTML.
     * The method processes characters from the `begin` position (inclusive) to the `end` position (exclusive),
     * ensuring that potentially unsafe characters are escaped to produce safe HTML content.
     * 
     * The `output` buffer should have a reserved space of `reserved` length at its end to accommodate escaped characters.
     * If there isn't enough space left in `output` to complete the operation, the method returns the last position 
     * it was able to process, either up to `end`, `buffer.limit()`, or until `output` is full, whichever comes first.
     * 
     * @param safeQuote specifies whether quotes should be safely escaped
     * @param buffer the input character buffer containing the text to be written
     * @param begin the starting position in `buffer` (inclusive)
     * @param end the ending position in `buffer` (exclusive)
     * @param reserved the number of characters reserved at the end of the `output` buffer
     * @param output the output buffer to receive the processed and escaped text
     * @return the final position up to which characters were successfully processed and written
     */
    public static int writeWithEscapeHtml(boolean safeQuote, CharBuffer buffer, int begin, int end, int reserved, CharBuffer output) {
    	int upTo = Math.min(end, buffer.limit()); //assuming to capacity
    	if(upTo - begin <= 0)
    		throw new IllegalArgumentException("output's ending is passes over min(end, limit) minus reserved");
    	int j = begin;
    	for (; j<upTo; j++) {
    		if(output.remaining() < reserved) //don't append, we has to reserve some spaces
				break;
			char c = buffer.get(j);
			String escape = HtmlEscapeUtil.escapeHtml(c, safeQuote);
			if(escape != null) {
				output.append(escape);
			}
			else if(output.remaining() < 1 + reserved) //don't append the char
				break;
			else
				output.append(c);
		}
    	return j;
    }
    
    /**
     * Writes characters from the `buffer` array to the `output` buffer, escaping unsafe characters for HTML content.
     * This method processes characters from the specified `begin` index (inclusive) up to `end` index (exclusive),
     * escaping characters as needed to produce safe HTML.
     * 
     * Ensure the `output` buffer has a reserved space of `reserved` length at its end to accommodate escaped characters.
     * If the output buffer does not have sufficient space, the method returns the last position in `buffer` that was 
     * successfully processed, up to `end`, `buffer.length`, or the point where `output` became full, whichever is reached first.
     * 
     * @param safeQuote if true, quotes will be escaped for HTML safety
     * @param buffer the input character array containing text to be processed
     * @param begin the starting index in `buffer` (inclusive)
     * @param end the ending index in `buffer` (exclusive)
     * @param reserved the number of characters reserved at the end of the `output` buffer
     * @param output the buffer receiving the escaped HTML text
     * @return the final position reached in `buffer`, either up to `end`, `buffer.length`, or the point where `output` filled up
     * 
     * @see #writeWithEscapeHtml(boolean, CharBuffer, int, int, int, CharBuffer)
     */
    public static int writeWithEscapeHtml(boolean safeQuote, char[] buffer, int begin, int end, int reserved, CharBuffer output) {
    	int upTo = Math.min(end, buffer.length); //assuming to the end
    	if(upTo - begin <= 0)
    		throw new IllegalArgumentException("output's ending is passes over min(end, limit) minus reserved");
    	int j = begin;
    	for (; j<upTo; j++) {
    		if(output.remaining() < reserved) //don't append, we has to reserve some spaces
				break;
			char c = buffer[j];
			String escape = HtmlEscapeUtil.escapeHtml(c, safeQuote);
			if(escape != null) {
				output.append(escape);
			}
			else if(output.remaining() < 1 + reserved) //don't append the char
				break;
			else
				output.append(c);
		}
    	return j;
    }
    
    /**
     * Appends characters from the `buffer` array to the `outputBuilder`, escaping unsafe characters for HTML.
     * This method processes characters from the specified `begin` index (inclusive) up to the `end` index (exclusive),
     * ensuring that potentially unsafe characters are escaped to produce HTML-safe content.
     * 
     * Since `StringBuilder` automatically expands its buffer, no reserved space is required.
     * 
     * @param safeQuote if true, quotes will also be escaped for HTML safety
     * @param buffer the input character array containing the text to be processed
     * @param begin the starting index in `buffer` (inclusive)
     * @param end the ending index in `buffer` (exclusive)
     * @param outputBuilder the `StringBuilder` receiving the escaped HTML text
     * 
     * @see #appendWithEscapeHtml(boolean, CharBuffer, int, int, StringBuilder)
     */
    public static void appendWithEscapeHtml(boolean safeQuote, char[] buffer, int begin, int end, StringBuilder outputBuilder)
 {
    	int upTo = Math.min(end, buffer.length); //assuming to the end
    	if(upTo - begin <= 0)
    		throw new IllegalArgumentException("output's ending is passes over min(end, limit) minus reserved");
    	int j = begin;
    	for (; j<upTo; j++) {
    		char c = buffer[j];
				String escape = HtmlEscapeUtil.escapeHtml(c, safeQuote);
			if(escape != null)
				outputBuilder.append(escape);
			else
				outputBuilder.append(c);
		}
    }
    
    /**
     * Appends characters from the `buffer` to the `outputBuilder`, escaping unsafe characters for HTML.
     * This method processes characters from the specified `begin` index (inclusive) up to the `end` index (exclusive),
     * ensuring that potentially unsafe characters are escaped to produce HTML-safe content.
     * 
     * Since `StringBuilder` automatically expands its buffer, no reserved space is required.
     * 
     * @param safeQuote if true, quotes will also be escaped for HTML safety
     * @param buffer the input character buffer containing the text to be processed
     * @param begin the starting index in `buffer` (inclusive)
     * @param end the ending index in `buffer` (exclusive)
     * @param outputBuilder the `StringBuilder` receiving the escaped HTML text
     */
    public static void appendWithEscapeHtml(boolean safeQuote, CharBuffer buffer, int begin, int end, StringBuilder outputBuilder) {
    	if(buffer.hasArray()) {
    		appendWithEscapeHtml(safeQuote, buffer.array(), begin, end, outputBuilder);
    		return;
    	}
    	
    	int upTo = Math.min(end, buffer.capacity()); //assuming to the end
    	if(upTo - begin <= 0)
    		throw new IllegalArgumentException("output's ending is passes over min(end, limit) minus reserved");
    	int j = begin;
    	for (; j<upTo; j++) {
    		char c = buffer.get(j);
				String escape = HtmlEscapeUtil.escapeHtml(c, safeQuote);
			if(escape != null)
				outputBuilder.append(escape);
			else
				outputBuilder.append(c);
		}
    }
}
