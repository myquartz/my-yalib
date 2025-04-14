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

public interface SMDHtmlWriter extends SMDRender {
	
	/**
	 * Produce the output from the current buffer to outputBuffer by HTML 5.0 Standard
	 * Must call parseNextBlock/parseLine first to the buffer.
	 * 
	 * Exclusive to call writeXhtml (not mixing both) 
	 * 
	 * @param markers the markers
	 * @param buffer (source data to copy)
	 * @param outputBuffer the output buffer.
	 * @return true if written completed, false if there is no space left (remaining not enough), needing another call to appending out (after the buffer clean).
	 */
	public boolean appendHtml(SMDMarkers markers, CharBuffer buffer, CharBuffer outputBuffer);
	
	/**
	 * compacting the buffer position to zero.
	 * 
	 * @param position current position.
	 */
	public void compact(int position);
}
