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
     * 
     * Print text (unsafe) characters in `buffer` from `begin` position up to `end` position (exclusive), to `output` buffer (HTML content with safe escape char).
     * The character will be escape (if needed), the output buffer has to reserve a space of `reserved` length at the end.
     * If not enough buffer space left, the routine will return position it can printed to.  
     * 
     * @param safeQuote true if required to safe quote
     * @param buffer input characters buffer.
     * @param begin start position (inclusive)
     * @param end end position (exclusive)
     * @param reserved number of space's reserved
     * @param output the output buffer
     * @return the last position it can print out, should be `end` or buffer.limit() or position when output is full which is the least.
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
     * 
     * Print text (unsafe) characters in `buffer` from `begin` position up to `end` position (exclusive), to `output` buffer (HTML content with safe escape char).
     * The character will be escape (if needed), the output buffer has to reserve a space of `reserved` length at the end.
     * If not enough buffer space left, the routine will return position it can printed to.  
     * 
     * @param safeQuote true if required to safe quote
     * @param buffer input characters buffer.
     * @param begin start position (inclusive)
     * @param end end position (exclusive)
     * @param reserved number of space's reserved
     * @param output the output buffer
     * @return the last position it can print out, should be `end` or buffer.limit() or position when output is full which is the least.
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
     * 
     * Print text (unsafe) characters in `buffer` from `begin` position up to `end` position (exclusive), to `output` string builder (HTML content with safe escape char).
     * The character will be escape (if needed), the output buffer has to reserve a space of `reserved` length at the end.
     * If not enough buffer space left, the routine will return position it can printed to.  
     * 
     * @param safeQuote true if required to safe quote
     * @param buffer input characters buffer.
     * @param begin start position (inclusive)
     * @param end end position (exclusive)
     * @param output the string builder
     * @return the last position it can print out, should be `end` or buffer.limit() or position when output is full which is the least.
     */
    public static void appendWithEscapeHtml(boolean safeQuote, char[] buffer, int begin, int end, StringBuilder outputBuilder) {
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
     * 
     * Print text (unsafe) characters in `buffer` from `begin` position up to `end` position (exclusive), to `output` string builder (HTML content with safe escape char).
     * The character will be escape (if needed), the output buffer has to reserve a space of `reserved` length at the end.
     * If not enough buffer space left, the routine will return position it can printed to.  
     * 
     * @param safeQuote true if required to safe quote
     * @param buffer input characters buffer.
     * @param begin start position (inclusive)
     * @param end end position (exclusive)
     * @param output the string builder
     * @return the last position it can print out, should be `end` or buffer.limit() or position when output is full which is the least.
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
