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

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.line.HtmlEscapeUtil;

public class HtmlRenderImpl implements SMDHtmlRender {

	public static final String TAG_STRIKE_BEGIN = "<s>";
    public static final String TAG_STRIKE_END = "</s>";
	public static final String TAG_BOLD_BEGIN = "<b>";
    public static final String TAG_BOLD_END = "</b>";
    public static final String TAG_ITALIC_BEGIN = "<i>";
    public static final String TAG_ITALIC_END = "</i>";
    public static final String TAG_UNDERLINE_BEGIN = "<u>";
    public static final String TAG_UNDERLINE_END = "</u>";
    public static final String TAG_CODE_BEGIN = "<code>";
    public static final String TAG_CODE_END = "</code>";
    
    public static final String TAG_A_BEGIN = "<a";
    public static final String TAG_A_URL_BEGIN = " href=\"";
    public static final String TAG_A_URL_END = "\"";
    public static final String TAG_A_TEXT_BEGIN = ">";
    //public static final String TAG_A_TEXT_END = "";
    public static final String TAG_A_END = "</a>";
    
    public static final String ESCAPE_QUOTE = "\"";
    
    public static final String TAG_IMG_BEGIN = "<img";
    public static final String TAG_IMG_END = ">";
    public static final String TAG_IMG_TEXT_BEGIN = " alt=\"";
    public static final String TAG_IMG_TEXT_END = ESCAPE_QUOTE;
    public static final String TAG_IMG_URL_BEGIN = " src=\"";
    public static final String TAG_IMG_URL_END = ESCAPE_QUOTE;
    
    public static final String PRE_STD = "<pre><code>";
    public static final String PRE_WITH_LANGUAGUE = "<pre><code class=\"language-";
    public static final String PRE_WITH_LANGUAGUE_POSTFIX = "\">";
    public static final String PRE_POSTFIX = "</code></pre>\n";
    
    public static final String BLOCKQUOTE_BEGIN = "<blockquote>";
    public static final String BLOCKQUOTE_END = "</blockquote>\n";
    public static final String PARA_BEGIN = "<p>";
    public static final String PARA_END = "</p>";
    
    public static final String UL_BEGIN = "<ul>\n";
	public static final String UL_END = "</ul>\n";
	public static final String OL_BEGIN = "<ol>\n";
	public static final String OL_END = "</ol>\n";
	
	public static final String LI_BEGIN = "<li>";
	public static final String LI_END = "</li>\n";
	
	public static final String[] HEADINGS_BEGIN = {"<h1>","<h2>","<h3>","<h4>","<h5>","<h6>",};
	public static final String[] HEADINGS_END = {"</h1>\n","</h2>\n","</h3>\n","</h4>\n","</h5>\n","</h6>\n",};
	
	public final static String HR = "<hr>\n";
	public final static String HR_DOUBLE = "<hr class=\"double-line\">\n";
	public final static String HR_UNDERSCORE = "<hr class=\"underscore-line\">\n";
    
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
    	
        while(markers.cursorIsAvailable()) {
        	int state = markers.cursorState(); //extract state
        	//if(!isMyState(state) && state != STATE_UNPARSABLE)
        		//break;
        	
        	safeQuote = false;
        	isInLinkText = false;
        	isInUrl = false;
        	
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
	        			break;
	        		case SMDParser.STATE_LINK:
	        			outputBuilder.append(TAG_A_BEGIN);
	        			break;
	        		case SMDParser.STATE_IMAGE:
	        			outputBuilder.append(TAG_IMG_BEGIN);
	        			break;
	        		case SMDParser.STATE_CODE_BLOCK:
	        			if(markers.cursorNextState() == SMDParser.STATE_CODE_LANGUAGE)
							outputBuilder.append(PRE_WITH_LANGUAGUE);
						else
							outputBuilder.append(PRE_STD);
	        			break;
	        		case SMDParser.STATE_QUOTE_BLOCK:
	        			outputBuilder.append(BLOCKQUOTE_BEGIN);
	        			break;
	        		case SMDParser.STATE_QUOTE_PARAGRAPH:
	        		case SMDParser.STATE_PARAGRAPH:
	    				outputBuilder.append(PARA_BEGIN);
	    				break;
	    				
	        		case SMDParser.STATE_ORDERED_LIST:
	        			outputBuilder.append(OL_BEGIN);
	        			break;
	        		case SMDParser.STATE_UNORDERED_LIST:
	        			outputBuilder.append(UL_BEGIN);
	        			break;
	        		case SMDParser.STATE_LIST_ITEM:
	        			outputBuilder.append(LI_BEGIN);
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
        	else if(markers.cursorIsMarkerStop()) { //MARKER_STOP
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
	        		case SMDParser.STATE_QUOTE_PARAGRAPH:
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
        	
        	int contentBegin = markers.cursorPosition1();
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
					if(isInUrl) {//copy to URL as well
	        			for (int j = contentBegin; j< contentEnd; j++) {
	        				char c = buffer.get(j);
	        				String escape = HtmlEscapeUtil.escapeHtml(c, true);
	        					if(escape != null)
	            					lastUrl.append(escape);
	            				else
	            					lastUrl.append(c);
	        			}
	    			}
				
					//copy to output
					HtmlEscapeUtil.appendWithEscapeHtml(safeQuote, 
    					buffer, contentBegin, contentEnd, outputBuilder);
				}
			}
			
        	markers.cursorGoNext();
        }
	}

}
