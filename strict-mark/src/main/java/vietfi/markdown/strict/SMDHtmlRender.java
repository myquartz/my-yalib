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
import java.util.function.BiFunction;

public interface SMDHtmlRender extends SMDRender {

	/**
	 * Produce the output from the current buffer to outputBuilder.
	 * Must call parseNextBlock/parseLine first to parse and setup the buffer.
	 * 
	 * @param markers the markers
	 * @param buffer (source data to copy)
	 * @param outputBuilder the output buffer.
	 */
	public void produceHtml(SMDMarkers markers, CharBuffer buffer, StringBuilder outputBuilder);
	
	/**
	 * Set the link URL resolver.
	 * 
	 * The resolver form is: method(inputBuffer, outputBuilder) 
	 * 		it returns how many char bypass from start of the inputBuffer (positive value) or from the end of the inputBuffer (negative value). 
	 * 		(or null if not modified, the render will continue as is).
	 * The resolve append its escaped text to the outputBuilder directly.
	 * 
	 * @param resolver function to call when a link is found.
	 */
	void setLinkHrefResolver(BiFunction<CharBuffer, StringBuilder, Integer> resolver);
	
	/**
	 * Set the image source URL resolver.
	 * 
	 * The resolver form is: method(inputBuffer, outputBuilder) 
	 * 		it returns how many char bypass from start of the inputBuffer (positive value) or from the end of the inputBuffer (negative value). 
	 * 		(or null if not modified, the render will continue as is).
	 * The resolve append its escaped text to the outputBuilder directly.
	 * 
	 * @param resolver function to call when a image (img tag) is found.
	 */
	void setImageSrcResolver(BiFunction<CharBuffer, StringBuilder, Integer> resolver);
}
