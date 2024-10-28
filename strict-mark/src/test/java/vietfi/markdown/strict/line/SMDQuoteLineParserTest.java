package vietfi.markdown.strict.line;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.CharBuffer;
import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;

public class SMDQuoteLineParserTest {
	@Test
	void test0() {
	    String inputText = "Simple text without formatting.\n" ;
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteLineParser parser = new SMDQuoteLineParser();

	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_INVALID, r);
	    assertEquals(0, input.position());
	}
	
	@Test
	void test1() {
	    String inputText = "> Block quote formatting.\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteLineParser parser = new SMDQuoteLineParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<blockquote><p>Block quote formatting.\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void bufferOutTest1() {
	    String inputText = ">  Another line of plain text.\n";
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteLineParser parser = new SMDQuoteLineParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip();
	    String result = output.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String expected = "<blockquote><p>Another line of plain text.\n";
	    		
	    assertEquals(expected, result);
	}
	
	@Test
	void bufferOut2Test1() {
		StringBuilder overSize = new StringBuilder(256);
		for(int j = 0;j<140;j++) {
			overSize.append("S"+j+" ");
		}
		
	    String inputText = "> "+overSize+"\n";
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteLineParser parser = new SMDQuoteLineParser();
	    StringBuilder sb = new StringBuilder(); 
	    CharBuffer output = CharBuffer.allocate(SMDParser.MINIMUM_BUFFER_SIZE);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r;
	    boolean b;
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    b = writer.appendHtml(parser.markers(), input, output);
	    output.flip(); sb.append(output.toString()); output.clear();
	    assertFalse(b);
	    System.out.append("Parially:\n").append(sb.toString()).append("\n");
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    output.flip(); sb.append(output.toString()); output.clear();
	    assertTrue(b);
	    
	    output.flip(); sb.append(output.toString()); output.clear();
	    	    
	    String result = sb.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String expected = "<blockquote><p>"+overSize.toString()+"\n";
	    		
	    assertEquals(expected, result);
	}
	
	@Test
	void test2() {
		String inputText = "> Here is a [link](http://example.com) and an image ![alt text](http://image.com/img.jpg) for example\n"
				+"not blockquote\n";
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteLineParser parser = new SMDQuoteLineParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r;
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_INVALID, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<blockquote><p>Here is a <a href=\"http://example.com\">link</a> and an image <img alt=\"alt text\" src=\"http://image.com/img.jpg\"> for example\n</p></blockquote>\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test3() {
		String inputText = "> > Invalid. Goodbye.\n";
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteLineParser parser = new SMDQuoteLineParser();
	    
	    int r;
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_INVALID, r);
	    
	}
	
}
