package vietfi.markdown.strict.block;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ SMDCodeByMarkerBlockParserTest.class, 
	SMDCodeByIndentBlockParserTest.class, SMDQuoteBlockParserTest.class, SMDParagraphParserTest.class, 
	SMDHorizontalBlockTest.class, SMDHeading12BlockParserTest.class, SMDHeadingByPoundsBlockParserTest.class,
	SMDOrderedListBlockParserTest.class, SMDUnorderedListBlockParserTest.class})
public class AllBlockTests {

}
