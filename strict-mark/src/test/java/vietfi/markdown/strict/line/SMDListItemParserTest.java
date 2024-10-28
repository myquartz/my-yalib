package vietfi.markdown.strict.line;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;

public class SMDListItemParserTest {
	@Test
	void test0() {
	    String inputText = " \nWrong item formatting.\n" ;
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();

	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_INVALID, r);
	    assertEquals(0, input.position());
	}
	
	@Test
	void test1() {
	    String inputText = " Simple item.\n\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "Simple item.\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test2() {
	    String inputText = " Complex item with [link & quote](http://website.com/link?quote=\").\n"
	    		+ "> quote here.\n"
	    		+ "> quote another.\n"
	    		+ "End of quote.\n"
	    		+ "\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n\n");
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "Complex item with <a href=\"http://website.com/link?quote=&quot;\">link &amp; quote</a>.\n"
	    		+ "<blockquote><p>quote here.\n"
	    		+ "quote another.\n"
	    		+ "</p></blockquote>\n"
	    		+ "End of quote.\n";
	    		
	    assertEquals(expected, sb.toString());
	}

	@Test
	void test3() {
	    String inputText = "Simple item.\n Code here.\n\tCode more here\nNot code.\n\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "Simple item.\n<pre><code>Code here.\nCode more here\n</code></pre>\n"
	    		+ "Not code.\n";
	    		
	    assertEquals(expected, sb.toString());
	}

	@Test
	void test4() {
	    String inputText = " Complex item with [link & quote](http://website.com/link?quote=\").\n"
	    		+ "> quote here.\n"
	    		+ "\n"
	    		+ "> quote another.\n"
	    		+ "\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "Complex item with <a href=\"http://website.com/link?quote=&quot;\">link &amp; quote</a>.\n"
	    		+ "<blockquote><p>quote here.\n"
	    		+ "</p></blockquote>\n"
	    		+ "<blockquote><p>quote another.\n"
	    		+ "</p></blockquote>\n";
	    		
	    assertEquals(expected, sb.toString());
	}

	@Test
	void bufferOutTest3() {
	    String inputText = "Simple item.\n"
	    		+ " Code here.\n"
	    		+ "\t  Code more here\n"
	    		+ "Not code.\n"
	    		+ "\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);

	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);

	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    writer.appendHtml(parser.markers(), input, output);
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "Simple item.\n<pre><code>Code here.\n  Code more here\n</code></pre>\n"
	    		+ "Not code.\n";
	    		
	    assertEquals(expected, sb.toString());
	}

	@Test
	void bufferOutTest4() {
	    String inputText = " Complex item with [link & quote](http://website.com/link?quote=\").\n"
	    		+ "> quote here.\n"
	    		+ "   \n"
	    		+ "> quote another.\n"
	    		+ "\n";
	    System.out.println("----buffer test4-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText.toCharArray());
	    SMDListItemParser parser = new SMDListItemParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);

	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    writer.appendHtml(parser.markers(), input, output);
	    output.flip(); sb.append(output.toString()); output.clear();
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    writer.appendHtml(parser.markers(), input, output);
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "Complex item with <a href=\"http://website.com/link?quote=&quot;\">link &amp; quote</a>.\n"
	    		+ "<blockquote><p>quote here.\n"
	    		+ "</p></blockquote>\n"
	    		+ "   \n"
	    		+ "<blockquote><p>quote another.\n"
	    		+ "</p></blockquote>\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test5() {
	    String inputText = " The following list:\n"
	    		+ "* item first.\n"
	    		+ "* item next.\n"
	    		+ "aha\n"
	    		+ "> quote another.\n"
	    		+ "\n";
	    System.out.println("----test5-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "The following list:\n"
	    		+ "<ul>\n"
	    		+ "<li>item first.\n</li>\n"
	    		+ "<li>item next.\n</li>\n"
	    		+ "</ul>\n"
	    		+ "aha\n"
	    		+ "<blockquote><p>quote another.\n"
	    		+ "</p></blockquote>\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test6() {
	    String inputText = " The following list:\n"
	    		+ "\n"
	    		+ "* item first.\n"
	    		+ "* item next.\n"
	    		+ "* item 3rd.\n"
	    		+ "\n"
	    		+ "end of list\n"
	    		+ "\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);	    
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "The following list:\n"
	    		+ "<ul>\n"
	    		+ "<li>item first.\n</li>\n"
	    		+ "<li>item next.\n</li>\n"
	    		+ "<li>item 3rd.\n</li>\n"
	    		+ "</ul>\n"
	    		+ "<p>end of list\n</p>";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test7() {
	    String inputText = " The following list:\n"
	    		+ "1. item first.\n"
	    		+ "2. item next.\n"
	    		+ "3. item 3rd.\n"
	    		+ "\n"
	    		+ "end of list\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);	    
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "The following list:\n"
	    		+ "<ol>\n"
	    		+ "<li>item first.\n</li>\n"
	    		+ "<li>item next.\n</li>\n"
	    		+ "<li>item 3rd.\n</li>\n"
	    		+ "</ol>\n"
	    		+ "<p>end of list\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test8() {
	    String inputText = " The following list:\n"
	    		+ "a. item first.\n"
	    		+ "   item next.\n"
	    		+ "b. item 3rd.\n"
	    		+ "\n"
	    		+ "end of list\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);	    
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "The following list:\n"
	    		+ "<ol>\n"
	    		+ "<li>item first.\n"
	    		+ "item next.\n</li>\n"
	    		+ "<li>item 3rd.\n</li>\n"
	    		+ "</ol>\n"
	    		+ "<p>end of list\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test9() {
	    String inputText = " The following list:\n"
	    		+ "* item first.\n"
	    		+ "   item next.\n"
	    		+ "* item 3rd.\n"
	    		+ "\titem 3rd+.\n"
	    		+ "\n"
	    		+ "end of list\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);	    
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "The following list:\n"
	    		+ "<ul>\n"
	    		+ "<li>item first.\n"
	    		+ "item next.\n"
	    		+ "</li>\n"
	    		+ "<li>item 3rd.\n"
	    		+ "item 3rd+.\n"
	    		+ "</li>\n"
	    		+ "</ul>\n"
	    		+ "<p>end of list\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test10() {
	    String inputText = " The following list:\n"
	    		+ "* item first.\n"
	    		+ "  > quote item next.\n"
	    		+ "* item 3rd.\n"
	    		+ "\titem 3rd+.\n"
	    		+ "end of list\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);	    
	    
	    //r = parser.parseLine(input);
	    //assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "The following list:\n"
	    		+ "<ul>\n"
	    		+ "<li>item first.\n"
	    		+ "<blockquote><p>quote item next.\n</p></blockquote>\n"
	    		+ "</li>\n"
	    		+ "<li>item 3rd.\n"
	    		+ "item 3rd+.\n"
	    		+ "</li>\n"
	    		+ "</ul>\n"
	    		+ "end of list\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test11() {
	    String inputText = " The following list:\n"
	    		+ "* item first.\n"
	    		+ "    Code here.\n"
	    		+ "  \n"
	    		+ "  New sub paragraph\n"
	    		+ "* item next.\n"
	    		+ "end of list\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);

	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);	    
	    	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "The following list:\n"
	    		+ "<ul>\n"
	    		+ "<li>item first.\n"
	    		+ "<pre><code>Code here.\n"
	    		+ "</code></pre>\n"
	    		+ "<p>New sub paragraph\n"
	    		+ "</p></li>\n"
	    		+ "<li>item next.\n"
	    		+ "</li>\n"
	    		+ "</ul>\n"
	    		+ "end of list\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test12() {
	    String inputText = " The following list:\n"
	    		+ "* item first.\n"
	    		+ "    Code here.\n"
	    		+ "\n"
	    		+ "  This line is separated code\n"
	    		+ "end of list\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDListItemParser parser = new SMDListItemParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);

	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "The following list:\n"
	    		+ "<ul>\n"
	    		+ "<li>item first.\n"
	    		+ "<pre><code>Code here.\n"
	    		+ "</code></pre>\n"
	    		+ "</li>\n"
	    		+ "</ul>\n"
	    		+ "<pre><code> This line is separated code\n"
	    		+ "</code></pre>\n"
	    		+ "end of list\n";
	    		
	    assertEquals(expected, sb.toString());
	}
}
