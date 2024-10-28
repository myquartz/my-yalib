
package vietfi.markdown.strict.block;

import java.nio.CharBuffer;

import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.line.SMDLineParser;
import vietfi.markdown.strict.line.SMDQuoteLineParser;

/**
 * Quote block parser. Parsing the quote block by prefix '>' + space character.
 * 
 * example 1:
 * 
 * > This quoted string or paragraph is
 * > **wrapped** in quoted. It support **bold** or [link](http://to.web/site) likes paragraph.
 * >
 * > Another paragraph.
 * 
 * then it produces:
 * 
 * <blockquote><p>This quoted string or paragraph is
 * <b>wrapped</b> in quoted. It support <b>bold</b> or <a href="http://to.web/site">link</a> likes paragraph.
 * </p><p>Another paragraph.
 * </p></blockquote>
 * 
 * The markers pattern are:
 * 
 * <quote
 * '>' space <paragraph<text[This quoted string or paragraph is] text>\n
 * '>' space <**[wrapped]**> in quoted. It support <**[bold]**> or <[link][http://to.web/site]> likes paragraph.] text> paragraph>\n
 * '>' space <paragraph<text [Another paragraph.] text>paragraph>\n
 * quote>
 * 
 */
public class SMDQuoteBlockParser implements SMDParser {

	private final SMDQuoteLineParser parser;
	
	public SMDQuoteBlockParser() {
		parser = new SMDQuoteLineParser();
    }
    
    public SMDQuoteBlockParser(SMDMarkers markers) {
    	parser = new SMDQuoteLineParser(markers);
	}

	@Override
	public int parseNext(CharBuffer buffer) {
		//simple loop
		while(buffer.hasRemaining()) {
			
			int r = parser.parseLine(buffer);
			switch(r) {
			case SMDLineParser.SMD_LINE_VOID:
				return SMD_VOID;
				
			case SMDLineParser.SMD_LINE_BLANK_OR_EMPTY:
				if(parser.getQuoteLines() == 0)
					return SMD_BLOCK_GETS_EMPTY_LINE;
			case SMDLineParser.SMD_LINE_INVALID:
				if(parser.getQuoteLines() > 0)
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
