package vietfi.markdown.strict.block;

import java.nio.CharBuffer;

import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.line.SMDCodeLineParser;
import vietfi.markdown.strict.line.SMDLineParser;

/**
 * parsing code block by indent, at least one space or tab started each line
 * This implementation will parse line by line. It requires SMDCodeLineParser.
 * 
 * example 1:
 * \s //javascript
 * \s var x = "your code, any text, <> escaped.";
 * 
 * example 2:
 * \t //javascript
 * \t var x = "your code, any text, <> escaped."; 
 * 
 * The markers of this kind are:
 * 
 * < space [code line 1\n] space [code line 2\n] ... [the last code line\n ]> \n
 * 
 */
public class SMDCodeByIndentBlockParser implements SMDParser {

	private final SMDCodeLineParser parser;
	
	public SMDCodeByIndentBlockParser() {
		parser = new SMDCodeLineParser(256);
    }
    
    public SMDCodeByIndentBlockParser(SMDMarkers markers) {
    	parser = new SMDCodeLineParser(markers);
	}

	@Override
	public int parseNext(CharBuffer buffer) {
		if(!buffer.hasRemaining())
			return SMD_VOID;
		
		while(buffer.hasRemaining()) {
			int r = parser.parseLine(buffer);
			switch(r) {
			case SMDLineParser.SMD_LINE_VOID:
				return SMD_VOID;
			
			case SMDLineParser.SMD_LINE_BLANK_OR_EMPTY:
				if(parser.getCodeLines() == 0)
					return SMD_BLOCK_GETS_EMPTY_LINE;
			case SMDLineParser.SMD_LINE_INVALID:
				if(parser.getCodeLines() > 0)
					return SMD_BLOCK_END;
				return SMD_BLOCK_INVALID;
			case SMDLineParser.SMD_LINE_PARSED_END:
			case SMDLineParser.SMD_LINE_PARSED:
				break;
			default:
				throw new IllegalStateException();
			}
		}
		
		return SMD_BLOCK_CONTINUE;
	}

	@Override
	public int compact(int position) {
		return parser.compact(position);
	}

	@Override
	public SMDMarkers markers() {
		return parser.markers();
	}

}
