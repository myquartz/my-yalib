package vietfi.markdown.strict.block;

import java.nio.CharBuffer;

import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;

public class UnparseableBlockParser implements SMDParser {

	protected final SMDMarkers markers;
	protected final boolean internalMarkers;
	
	public UnparseableBlockParser() {
		internalMarkers = true;
		markers = new SMDMarkers(256);
    }
    
    public UnparseableBlockParser(SMDMarkers markers) {
    	internalMarkers = false;
    	this.markers = markers;
	}
    
	@Override
	public int parseNext(CharBuffer buff) {
		
		buff.mark();
		char ch = '\0';
		int chType;
		int pos = buff.position();
		int startPos = pos;
		markers.addStartMarkerContent(STATE_UNPARSABLE, pos, pos);
		
		if(buff.hasRemaining()) {
			ch = buff.get();
			chType = Character.getType(ch);
			pos++;
			
			if(startPos+1 == pos && ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {
				return SMD_BLOCK_GETS_EMPTY_LINE;
			}
			
			while(buff.hasRemaining()) {
				ch = buff.get();
				chType = Character.getType(ch);
				pos++;
				if(ch == '\n' || chType == Character.LINE_SEPARATOR  || chType == Character.PARAGRAPH_SEPARATOR) {
					markers.addStopMarkerContent(STATE_UNPARSABLE, pos, pos);
					return SMD_BLOCK_END; //ending
				}
			}
			
		}
		
		markers.rollbackLastMarkerContentStart(STATE_UNPARSABLE);
		//not found the line end, reset back
		buff.reset();
		return SMD_VOID;
	}

	@Override
	public int compact(int position) {
		int r = 1;
		if(internalMarkers)
			r = markers.compactMarkers(position);
		if(r<=0)
			return 0;
		return position;
	}

	@Override
	public SMDMarkers markers() {
		return markers;
	}

}
