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
package vietfi.markdown.strict.render;

import java.nio.CharBuffer;
import java.util.function.BiFunction;
import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.line.HtmlEscapeUtil;

public class HtmlRenderImpl extends HtmlBaseTagRender implements SMDHtmlRender {
	
	protected BiFunction<CharBuffer, StringBuilder, Integer> linkHrefResolver = null;
	protected BiFunction<CharBuffer, StringBuilder, Integer> imgSrcResolver = null;
	
	@Override
	public void setLinkHrefResolver(BiFunction<CharBuffer, StringBuilder, Integer> resolver) {
		this.linkHrefResolver = resolver;
	}

	@Override
	public void setImageSrcResolver(BiFunction<CharBuffer, StringBuilder, Integer> resolver) {
		this.imgSrcResolver = resolver;
	}
	
	@Override
	public void produceHtml(SMDMarkers markers, CharBuffer buffer, StringBuilder outputBuilder) {
        
    	if(markers.isEmpty() && buffer.position() > 0) {
			//whole buffer is the line of code block;
			HtmlEscapeUtil.appendWithEscapeHtml(false, buffer, 0, buffer.position(), outputBuilder);
			return;
		}
    	
        StringBuilder lastText = null;
        StringBuilder lastUrl = null;
        boolean safeQuote = false;
    	boolean isInLinkText = false;
    	boolean isInUrl = false;
    	boolean isInImgSrc = false;
    	
        while(markers.cursorIsAvailable()) {
        	boolean isMarkerStop = markers.cursorIsMarkerStop();
        	int contentBegin = markers.cursorPosition1();
			if(!isMarkerStop && contentBegin >= buffer.position()) //exceed the output point
				break;
			
			int state = markers.cursorState(); //extract state
        	safeQuote = false;
        	isInLinkText = false;
        	isInUrl = false;
        	isInImgSrc = false;
        	
        	if(markers.cursorIsMarkerStart()) {
        		//start marker
        		switch(state) {
        			case SMDParser.STATE_STRIKETHROUGH:
        				outputBuilder.append(TAG_STRIKE_BEGIN);
        				break;
	        		case SMDParser.STATE_BOLD:
	        			outputBuilder.append(TAG_BOLD_BEGIN);
	        			break;
	        		case SMDParser.STATE_ITALIC:
	        			outputBuilder.append(TAG_ITALIC_BEGIN);
	        			break;
	        		case SMDParser.STATE_UNDERLINE:
	        			outputBuilder.append(TAG_UNDERLINE_BEGIN);
	        			break;
	        		case SMDParser.STATE_INLINE_CODE:
	        			outputBuilder.append(TAG_CODE_BEGIN);
	        			if(codeClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(codeClass).append(ATTR_CLASS_END);
	        			outputBuilder.append(TAG_BEGIN_GT);
	        			break;
	        		case SMDParser.STATE_LINK:
	        			outputBuilder.append(TAG_A_BEGIN);
	        			if(linkClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(linkClass).append(ATTR_CLASS_END);
	        			break;
	        		case SMDParser.STATE_IMAGE:
	        			outputBuilder.append(TAG_IMG_BEGIN);
	        			if(imgClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(imgClass).append(ATTR_CLASS_END);
	        			break;
	        		case SMDParser.STATE_CODE_BLOCK:
	        			outputBuilder.append(PRE_TAG);
	        			if(preCodeClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(preCodeClass).append(ATTR_CLASS_END);
	        			if(markers.cursorNextState() == SMDParser.STATE_CODE_LANGUAGE)
							outputBuilder.append(PRE_WITH_LANGUAGUE);
						else
							outputBuilder.append(PRE_STD);
	        			break;
	        		case SMDParser.STATE_QUOTE_BLOCK:
	        			outputBuilder.append(BLOCKQUOTE_BEGIN);
	        			if(blockquoteClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(blockquoteClass).append(ATTR_CLASS_END);
	        			outputBuilder.append(TAG_BEGIN_GT);
	        			break;
	        		case SMDParser.STATE_PARAGRAPH:
	    				outputBuilder.append(PARA_BEGIN);
	    				if(pClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(pClass).append(ATTR_CLASS_END);
	        			outputBuilder.append(TAG_BEGIN_GT);
	    				break;
	    				
	        		case SMDParser.STATE_NEW_LINE:
	        			outputBuilder.append(TAG_NEW_LINE);
	    				break;
	    				
	        		case SMDParser.STATE_ORDERED_LIST:
	        			outputBuilder.append(OL_BEGIN);
	        			if(olClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(olClass).append(ATTR_CLASS_END);
	        			outputBuilder.append(TAG_BEGIN_GT);
	        			break;
	        		case SMDParser.STATE_UNORDERED_LIST:
	        			outputBuilder.append(UL_BEGIN);
	        			if(ulClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(ulClass).append(ATTR_CLASS_END);
	        			outputBuilder.append(TAG_BEGIN_GT);
	        			break;
	        		case SMDParser.STATE_LIST_ITEM:
	        			outputBuilder.append(LI_BEGIN);
	        			if(liClass != null)
	        				outputBuilder.append(ATTR_CLASS_BEGIN).append(liClass).append(ATTR_CLASS_END);
	        			outputBuilder.append(TAG_BEGIN_GT);
	        			break;
	        			
	        		case SMDParser.STATE_HEADING_1:
	        		case SMDParser.STATE_HEADING_2:
	        		case SMDParser.STATE_HEADING_3:
	        		case SMDParser.STATE_HEADING_4:
	        		case SMDParser.STATE_HEADING_5:
	        		case SMDParser.STATE_HEADING_6:
	        			outputBuilder.append(HEADINGS_BEGIN[state - SMDParser.STATE_HEADING_1]);
	        			break;
	        		
	        		case SMDParser.STATE_HORIZONTAL:
	        			outputBuilder.append(HR);
	        			break;
	        		case SMDParser.STATE_HORIZONTAL_D:
	        			outputBuilder.append(HR_DOUBLE);
	        			break;
	        		case SMDParser.STATE_HORIZONTAL_U:
	        			outputBuilder.append(HR_UNDERSCORE);
	        			break;
	        		
        		}
        	}
        	//stop
        	else if(isMarkerStop) { //MARKER_STOP
        		//stop marker
        		switch(state) {
        			case SMDParser.STATE_STRIKETHROUGH:
        				outputBuilder.append(TAG_STRIKE_END);
        				break;
	        		case SMDParser.STATE_BOLD:
	        			outputBuilder.append(TAG_BOLD_END);
	        			break;
	        		case SMDParser.STATE_ITALIC:
	        			outputBuilder.append(TAG_ITALIC_END);
	        			break;
	        		case SMDParser.STATE_UNDERLINE:
	        			outputBuilder.append(TAG_UNDERLINE_END);
	        			break;
	        		case SMDParser.STATE_INLINE_CODE:
	        			outputBuilder.append(TAG_CODE_END);
	        			break;
	        		case SMDParser.STATE_LINK:
	        			if(lastText != null && lastText.length() > 0) {
	        				outputBuilder.append(TAG_A_TEXT_BEGIN).append(lastText).append(TAG_A_END);
	        			}
	        			else {
	        				outputBuilder.append(TAG_A_TEXT_BEGIN).append(lastUrl).append(TAG_A_END);
	        			}
	        			break;
	        		case SMDParser.STATE_IMAGE:
	        			outputBuilder.append(TAG_IMG_END);
	        			break;
	        		case SMDParser.STATE_CODE_BLOCK:
	        			outputBuilder.append(PRE_POSTFIX);
	        			break;
	        		case SMDParser.STATE_QUOTE_BLOCK:
	        			outputBuilder.append(BLOCKQUOTE_END);
	        			break;
	        		
	        		case SMDParser.STATE_PARAGRAPH:
	    				outputBuilder.append(PARA_END);
	    				break;
	    				
	        		case SMDParser.STATE_ORDERED_LIST:
	        			outputBuilder.append(OL_END);
	        			break;
	        		case SMDParser.STATE_UNORDERED_LIST:
	        			outputBuilder.append(UL_END);
	        			break;
	        		case SMDParser.STATE_LIST_ITEM:
	        			outputBuilder.append(LI_END);
	        			break;
	        			
	        		case SMDParser.STATE_HEADING_1:
	        		case SMDParser.STATE_HEADING_2:
	        		case SMDParser.STATE_HEADING_3:
	        		case SMDParser.STATE_HEADING_4:
	        		case SMDParser.STATE_HEADING_5:
	        		case SMDParser.STATE_HEADING_6:
	        			outputBuilder.append(HEADINGS_END[state - SMDParser.STATE_HEADING_1]);
	        			break;
	    		}
        	}
        	
        	if(markers.cursorIsContentStart()) {
        		safeQuote = (state == SMDParser.STATE_IMAGE || state == SMDParser.STATE_IMAGE_SRC || state == SMDParser.STATE_URL);
        		isInLinkText = (state == SMDParser.STATE_LINK);
        		isInUrl = (state == SMDParser.STATE_URL);
        		isInImgSrc = (state == SMDParser.STATE_IMAGE_SRC);
        		
        		switch(state) {
        		case SMDParser.STATE_URL: //state URL content is differ
        			outputBuilder.append(TAG_A_URL_BEGIN);
        			if(lastUrl == null)
        				lastUrl = new StringBuilder();
        			else
        				lastUrl.setLength(0);
        			break;
        		case SMDParser.STATE_LINK:
        			if(lastText == null)
        				lastText = new StringBuilder();
        			else
        				lastText.setLength(0);
        			break;
        		case SMDParser.STATE_IMAGE:
        			outputBuilder.append(TAG_IMG_TEXT_BEGIN);
        			break;
        		case SMDParser.STATE_IMAGE_SRC:
        			outputBuilder.append(TAG_IMG_URL_BEGIN);
        			break;
        		}
        	}
        	else if(markers.cursorIsContentStop()) { //End of content
        		switch(state) {
        		case SMDParser.STATE_URL: //state URL content is differ
        			outputBuilder.append(TAG_A_URL_END);
        			break;
        		case SMDParser.STATE_IMAGE:
        			outputBuilder.append(TAG_IMG_TEXT_END);
        			break;
        		case SMDParser.STATE_IMAGE_SRC:
        			outputBuilder.append(TAG_IMG_URL_END);
        			break;
        		case SMDParser.STATE_CODE_LANGUAGE:
        			outputBuilder.append(PRE_WITH_LANGUAGUE_POSTFIX);
        			break;
        		}
        	}
        	
			int contentEnd = Math.min(markers.cursorPosition2(), buffer.position());
			
			if (contentBegin >= 0 && contentBegin < contentEnd) {
				if(isInLinkText) {
        			for (int j = contentBegin; j< contentEnd; j++) {
        				char c = buffer.get(j);
        				String escape = HtmlEscapeUtil.escapeHtml(c, false);
        					if(escape != null)
        						lastText.append(escape);
            				else
            					lastText.append(c);
        			}	
					
				}
				else {
					if(isInUrl || isInImgSrc) {
						if(isInUrl) {
							//copy to URL to print later (if needed)
		        			for (int j = contentBegin; j< contentEnd; j++) {
		        				char c = buffer.get(j);
		        				String escape = HtmlEscapeUtil.escapeHtml(c, true);
		        					if(escape != null)
		            					lastUrl.append(escape);
		            				else
		            					lastUrl.append(c);
		        			}
						}
						Integer bypass = null;
						
						if(isInUrl && linkHrefResolver != null 
								|| isInImgSrc && imgSrcResolver != null) {
							CharBuffer urlBuff = buffer.asReadOnlyBuffer();
							urlBuff.limit(contentEnd);
							urlBuff.position(contentBegin);
							if(isInUrl)	
								bypass = linkHrefResolver.apply(urlBuff, outputBuilder);
							else if(isInImgSrc)
								bypass = imgSrcResolver.apply(urlBuff, outputBuilder);
						}
						
						if(bypass != null) {
							if(bypass > 0) { //trip from start
								contentBegin += bypass;
							}
							else { //trip from end
								contentEnd += bypass;
							}
						}
						if (contentBegin >= 0 && contentBegin < contentEnd) {
							//copy to output the left part
							HtmlEscapeUtil.appendWithEscapeHtml(safeQuote, 
									buffer, contentBegin, contentEnd, outputBuilder);
						}
	    			}
					else {
						//copy to output with escaped
						HtmlEscapeUtil.appendWithEscapeHtml(safeQuote, 
								buffer, contentBegin, contentEnd, outputBuilder);
					}
				}
			}
			
        	markers.cursorGoNext();
        }
	}

}
