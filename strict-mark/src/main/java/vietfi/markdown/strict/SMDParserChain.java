package vietfi.markdown.strict;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import vietfi.markdown.strict.block.SMDCodeByIndentBlockParser;
import vietfi.markdown.strict.block.SMDCodeByMarkerBlockParser;
import vietfi.markdown.strict.block.SMDHeading12BlockParser;
import vietfi.markdown.strict.block.SMDHeadingByPoundsBlockParser;
import vietfi.markdown.strict.block.SMDHorizontalBlockParser;
import vietfi.markdown.strict.block.SMDOrderedListBlockParser;
import vietfi.markdown.strict.block.SMDParagraphParser;
import vietfi.markdown.strict.block.SMDQuoteBlockParser;
import vietfi.markdown.strict.block.SMDUnorderedListBlockParser;
import vietfi.markdown.strict.block.UnparseableBlockParser;

public class SMDParserChain implements SMDParser {

	private final SMDMarkers markers;
	private final SMDParser[] parsers;
	private SMDParser current = null;
	
	private SMDParserChain(SMDMarkers markers, SMDParser... parsers) {
		this.markers = markers;
		this.parsers = parsers;
	}
	
	public static SMDParser createParserOf(Class<SMDParser>... parserClasses) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		SMDMarkers markers = new SMDMarkers(4096);
		List<SMDParser> list = new ArrayList<>(parserClasses.length);
		for(Class<SMDParser> pc : parserClasses) {
			Constructor<SMDParser> cons = pc.getConstructor(SMDMarkers.class);
			list.add(cons.newInstance(markers));
		}
		list.add(new UnparseableBlockParser(markers));
		
		return new SMDParserChain(markers, list.toArray(new SMDParser[list.size()]));
	}
	
	public static SMDParser createParserOfContents() {
		SMDMarkers markers = new SMDMarkers(1024);
		
		return new SMDParserChain(markers,
				
				new SMDOrderedListBlockParser(markers),
				new SMDUnorderedListBlockParser(markers),
				
				new SMDCodeByIndentBlockParser(markers),
				new SMDCodeByMarkerBlockParser(markers),
				
				new SMDQuoteBlockParser(markers),
				
				new SMDParagraphParser(markers),
				
				new UnparseableBlockParser(markers));
	}
	
	public static SMDParser createParserOfStandard() {
		SMDMarkers markers = new SMDMarkers(4096);
		
		return new SMDParserChain(markers,
				new SMDHorizontalBlockParser(markers),
				
				new SMDHeading12BlockParser(markers),
				new SMDHeadingByPoundsBlockParser(markers),
				new SMDHeading12BlockParser(markers),
				
				new SMDOrderedListBlockParser(markers),
				new SMDUnorderedListBlockParser(markers),
				
				new SMDCodeByIndentBlockParser(markers),
				new SMDCodeByMarkerBlockParser(markers),
				
				new SMDQuoteBlockParser(markers),
				
				new SMDParagraphParser(markers),
				
				new UnparseableBlockParser(markers));
	}
	
	@Override
	public int parseNext(CharBuffer buff) {
		if(!buff.hasRemaining())
			return SMD_VOID;
		
		while(buff.hasRemaining()) {
			int ret = -1;
			if(current != null) {//try to parsing
				ret = current.parseNext(buff);
				if(ret == SMD_VOID || ret == SMD_BLOCK_CONTINUE)
					return ret;
				if(ret == SMD_BLOCK_END || ret == SMD_BLOCK_GETS_EMPTY_LINE) {
					current = null;
					continue;
				}
			}

			current = null;
			//invalid, try another in order.
			for(SMDParser p : parsers) {
				ret = p.parseNext(buff);
				if(ret == SMD_VOID)
					return SMD_VOID;
				if(ret == SMD_BLOCK_GETS_EMPTY_LINE || ret == SMD_BLOCK_END || ret == SMD_BLOCK_CONTINUE) {
					if(ret == SMD_BLOCK_CONTINUE) {
						current = p;
						return SMD_BLOCK_CONTINUE;
					}
					//another loop
					break;
				}
				//else try next INVALID
			}
		}
		
		return SMD_BLOCK_CONTINUE;
	}

	@Override
	public int compact(int position) {
		int min = position;
		for(SMDParser p : parsers) {
			int x = p.compact(position);
			if(min > x)
				min = x;
		}
		return min;
	}

	@Override
	public SMDMarkers markers() {
		return markers;
	}

}
