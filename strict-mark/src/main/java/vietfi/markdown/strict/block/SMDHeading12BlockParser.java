package vietfi.markdown.strict.block;

import java.nio.CharBuffer;

import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;

/**
 * Parsing heading styles of the "dash" following line.
 * 
 * Example:
 * 
 * Heading 1 here
 * ==============
 * 
 * Heading 2 here
 * --------------
 * 
 * The markers's pattern is:
 * 
 * <[Heading 1 here]\n=====\n>
 * 
 * or
 * 
 * <[Heading 2 here]\n-----\n>
 * 
 */
public class SMDHeading12BlockParser implements SMDParser {

	protected final SMDMarkers markers;
	protected final boolean internalMarkers;
	
	public SMDHeading12BlockParser() {
		this.markers = new SMDMarkers(8);
		internalMarkers = true;
	}
	
	public SMDHeading12BlockParser(SMDMarkers markers) {
		this.markers = markers;
		internalMarkers = false;
	}

	@Override
	public int parseNext(CharBuffer buff) {
		if(!buff.hasRemaining())
			return SMD_VOID;
		
		int startPos = -1;
		int endPos = -1;
		
		//finding style 1 or style 2
		buff.mark();
		int chType;
		
		int pos = buff.position();
		startPos = pos; //including the first char
		char ch = buff.get();

		if(!Character.isUnicodeIdentifierStart(ch)) {
			//new line, start with a space or empty string does not treat as unparseable
			buff.reset();
			return SMD_BLOCK_INVALID;
		}
		pos++;
    	
		char markerType = '\0';
		
		while(buff.hasRemaining()) {
			ch = buff.get();
			chType = Character.getType(ch);
			pos++;
			if(endPos > 0) { //wait for marker Type
				if(markerType == '\0' && (ch == '-' || ch == '='))
					markerType = ch;
				else if (markerType == ch) //repeat
					continue;
				else if(!Character.isWhitespace(ch)) {
					buff.reset();
					markerType = '\0';
					return SMD_BLOCK_INVALID;
				}
			}
			
			if(ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {
				if(endPos < 0)
					endPos = pos-1;
				else if(markerType == '-' || markerType == '=') {
					markers.addStartMarkerContent(markerType == '=' ? STATE_HEADING_1 : STATE_HEADING_2, startPos, startPos);
					markers.addStopMarkerContent(markerType == '=' ? STATE_HEADING_1 : STATE_HEADING_2, endPos, pos);
					markerType = '\0';
					return SMD_BLOCK_END;
				}
				else {
					buff.reset();
					return SMD_BLOCK_INVALID;
				}
			}
		}
		
		//end of buffer
		if(endPos < 0 || markerType == '\0') {
			buff.reset();
			return SMD_VOID;
		}
		return SMD_BLOCK_CONTINUE;	
	}

	@Override
	public int compact(int position) {
		if(this.internalMarkers) {
			int r = markers.compactMarkers(position);
			if(r <= 0)
				return 0;
		}
		return position;
	}

	@Override
	public SMDMarkers markers() {
		return markers;
	}

}
