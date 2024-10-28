package vietfi.markdown.strict.line;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ HtmlEscapeUtilTest.class, SMDTextLineParserTest.class,
	SMDQuoteLineParserTest.class, SMDCodeLineParserTest.class, SMDListItemParserTest.class })
public class AllLineTests {

}
