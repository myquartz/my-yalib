package vietfi.markdown.strict.render;

import java.nio.CharBuffer;

import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.line.HtmlEscapeUtil;

public class HtmlWriterImpl extends HtmlRenderImpl implements SMDHtmlWriter {

	public static final int MINIMUM_BUFFER_SIZE = 256; //max size of URL or encoded `text` string in <a>`Text`</>
	
	//max size of begin tag
    protected static final int START_TAG_ROOM = 16 + Math.max(
    		Math.max(TAG_BOLD_BEGIN.length(), TAG_CODE_BEGIN.length()),
    		Math.max(TAG_A_URL_BEGIN.length(), TAG_IMG_URL_BEGIN.length())); 
    protected static final int END_TAG_ROOM = 16 + Math.max(
    		Math.max(TAG_BOLD_BEGIN.length(), TAG_CODE_BEGIN.length()),
    		Math.max(TAG_A_URL_BEGIN.length(), TAG_IMG_URL_BEGIN.length()));
    
    private int contentOutput = 0;
	    
	@Override
	public boolean appendHtml(SMDMarkers markers, CharBuffer buffer, CharBuffer outputBuffer) {
		if(outputBuffer.capacity() < MINIMUM_BUFFER_SIZE)
			throw new IllegalArgumentException("Buffer capacity too small, at least "+MINIMUM_BUFFER_SIZE+" required!");
		if(outputBuffer.remaining() < MINIMUM_BUFFER_SIZE)
			return false; //no write out, please flush buffer and flip it first.
		
		if(markers.isEmpty() && buffer.position() > 0) {
			//whole buffer is the line of paragraph
			contentOutput = HtmlEscapeUtil.writeWithEscapeHtml(false, buffer, contentOutput, buffer.position(), 0, outputBuffer);
			return contentOutput >= buffer.position();
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
	        			outputBuffer.append(TAG_STRIKE_BEGIN);
	    				break;
	        		case SMDParser.STATE_BOLD:
	        			outputBuffer.append(TAG_BOLD_BEGIN);
	        			break;
	        		case SMDParser.STATE_ITALIC:
	        			outputBuffer.append(TAG_ITALIC_BEGIN);
	        			break;
	        		case SMDParser.STATE_UNDERLINE:
	        			outputBuffer.append(TAG_UNDERLINE_BEGIN);
	        			break;
	        		case SMDParser.STATE_INLINE_CODE:
	        			outputBuffer.append(TAG_CODE_BEGIN);
	        			break;
	        		case SMDParser.STATE_LINK:
	        			outputBuffer.append(TAG_A_BEGIN);
	        			break;
	        		case SMDParser.STATE_IMAGE:
	        			outputBuffer.append(TAG_IMG_BEGIN);
	        			break;
	        		case SMDParser.STATE_CODE_BLOCK:
	        			if(markers.cursorNextState() == SMDParser.STATE_CODE_LANGUAGE)
							outputBuffer.append(PRE_WITH_LANGUAGUE);
						else
							outputBuffer.append(PRE_STD);
	        			break;
	        		case SMDParser.STATE_QUOTE_BLOCK:
	        			outputBuffer.append(BLOCKQUOTE_BEGIN);
	        			break;
	        		case SMDParser.STATE_QUOTE_PARAGRAPH:
	        		case SMDParser.STATE_PARAGRAPH:
	    				outputBuffer.append(PARA_BEGIN);
	    				break;
	    				
	        		case SMDParser.STATE_ORDERED_LIST:
	        			outputBuffer.append(OL_BEGIN);
	        			break;
	        		case SMDParser.STATE_UNORDERED_LIST:
	        			outputBuffer.append(UL_BEGIN);
	        			break;
	        		case SMDParser.STATE_LIST_ITEM:
	        			outputBuffer.append(LI_BEGIN);
	        			break;
	        			
	        		case SMDParser.STATE_HEADING_1:
	        		case SMDParser.STATE_HEADING_2:
	        		case SMDParser.STATE_HEADING_3:
	        		case SMDParser.STATE_HEADING_4:
	        		case SMDParser.STATE_HEADING_5:
	        		case SMDParser.STATE_HEADING_6:
	        			outputBuffer.append(HEADINGS_BEGIN[state - SMDParser.STATE_HEADING_1]);
	        			break;
	        			
	        		case SMDParser.STATE_HORIZONTAL:
	        			outputBuffer.append(HR);
	        			break;
	        		case SMDParser.STATE_HORIZONTAL_D:
	        			outputBuffer.append(HR_DOUBLE);
	        			break;
	        		case SMDParser.STATE_HORIZONTAL_U:
	        			outputBuffer.append(HR_UNDERSCORE);
	        			break;
        		}
        	}
        	//stop
        	else if(markers.cursorIsMarkerStop()) { //MARKER_STOP
        		//stop marker
        		switch(state) {
	        		case SMDParser.STATE_STRIKETHROUGH:
	        			outputBuffer.append(TAG_STRIKE_END);
	    				break;
	        		case SMDParser.STATE_BOLD:
	        			outputBuffer.append(TAG_BOLD_END);
	        			break;
	        		case SMDParser.STATE_ITALIC:
	        			outputBuffer.append(TAG_ITALIC_END);
	        			break;
	        		case SMDParser.STATE_UNDERLINE:
	        			outputBuffer.append(TAG_UNDERLINE_END);
	        			break;
	        		case SMDParser.STATE_INLINE_CODE:
	        			outputBuffer.append(TAG_CODE_END);
	        			break;
	        		case SMDParser.STATE_LINK:
	        			if(lastText != null && lastText.length() > 0) {
	        				outputBuffer.append(TAG_A_TEXT_BEGIN).append(lastText).append(TAG_A_END);
	        			}
	        			else {
	        				outputBuffer.append(TAG_A_TEXT_BEGIN).append(lastUrl).append(TAG_A_END);
	        			}
	        			break;
	        		case SMDParser.STATE_IMAGE:
	        			outputBuffer.append(TAG_IMG_END);
	        			break;
	        		case SMDParser.STATE_CODE_BLOCK:
	        			outputBuffer.append(PRE_POSTFIX);
	        			break;
	        		case SMDParser.STATE_QUOTE_BLOCK:
	        			outputBuffer.append(BLOCKQUOTE_END);
	        			break;
	        		case SMDParser.STATE_QUOTE_PARAGRAPH:
	        		case SMDParser.STATE_PARAGRAPH:
	    				outputBuffer.append(PARA_END);
	    				break;
	        		
	        		case SMDParser.STATE_ORDERED_LIST:
	        			outputBuffer.append(OL_END);
	        			break;
	        		case SMDParser.STATE_UNORDERED_LIST:
	        			outputBuffer.append(UL_END);
	        			break;
	        		case SMDParser.STATE_LIST_ITEM:
	        			outputBuffer.append(LI_END);
	        			break;
	        			
	        		case SMDParser.STATE_HEADING_1:
	        		case SMDParser.STATE_HEADING_2:
	        		case SMDParser.STATE_HEADING_3:
	        		case SMDParser.STATE_HEADING_4:
	        		case SMDParser.STATE_HEADING_5:
	        		case SMDParser.STATE_HEADING_6:
	        			outputBuffer.append(HEADINGS_END[state - SMDParser.STATE_HEADING_1]);
	        			break;
	    		}
        	}
        	
        	if(markers.cursorIsContentStart()) {
        		safeQuote = (state == SMDParser.STATE_IMAGE || state == SMDParser.STATE_IMAGE_SRC || state == SMDParser.STATE_URL);
        		isInLinkText = (state == SMDParser.STATE_LINK);
        		isInUrl = (state == SMDParser.STATE_URL);
        		switch(state) {
        		case SMDParser.STATE_URL: //state URL content is differ
        			outputBuffer.append(TAG_A_URL_BEGIN);
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
        			outputBuffer.append(TAG_IMG_TEXT_BEGIN);
        			break;
        		case SMDParser.STATE_IMAGE_SRC:
        			outputBuffer.append(TAG_IMG_URL_BEGIN);
        			break;
        		}
        	}
        	else if(markers.cursorIsContentStop()) { //End of content
        		switch(state) {
        		case SMDParser.STATE_URL: //state URL content is differ
        			outputBuffer.append(TAG_A_URL_END);
        			break;
        		case SMDParser.STATE_IMAGE:
        			outputBuffer.append(TAG_IMG_TEXT_END);
        			break;
        		case SMDParser.STATE_IMAGE_SRC:
        			outputBuffer.append(TAG_IMG_URL_END);
        			break;
        		case SMDParser.STATE_CODE_LANGUAGE:
        			outputBuffer.append(PRE_WITH_LANGUAGUE_POSTFIX);
        			break;
        		}
        	}
        	
        	int contentBegin = markers.cursorPosition1();
			int contentEnd = Math.min(markers.cursorPosition2(), buffer.position());
        	//content or plain text
			if (contentBegin >= 0 && contentBegin < contentEnd && contentOutput < contentEnd) {
        		//the link content, do not print out, save to lastText,
        		if(isInLinkText) {
        			//copy to lastText
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
        			if(contentOutput < contentBegin)
    					contentOutput = contentBegin;
        			
        			if(isInUrl) {//copy to URL as well
	        			for (int j = contentOutput; j< contentEnd; j++) {
	        				if(outputBuffer.remaining() < 1+1)
	        					return false;
	        				
	        				char c = buffer.get(j);
	        				String escape = HtmlEscapeUtil.escapeHtml(c, true);
	        					if(escape != null)
	            					lastUrl.append(escape);
	            				else
	            					lastUrl.append(c);
	        			}
        			}
        			
        			//copy to output
        			contentOutput = HtmlEscapeUtil.writeWithEscapeHtml(safeQuote, 
        					buffer, contentOutput, contentEnd, 1, outputBuffer);
    				if(contentOutput < contentEnd)
    					return false;
    				if(!markers.cursorCanGoNext() && contentEnd == buffer.position()) {//break, don't increase markerIndex
    					return true;
    				}
        		}
        		
        	}
			markers.cursorGoNext();
        }
        
		return true;
	}

	@Override
	public void compact(int position) {
		if(this.contentOutput > position) {
			this.contentOutput -= position;
		}
		else
			this.contentOutput = 0;
	}

}
