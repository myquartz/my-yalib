package vietfi.markdown.strict.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;

public class SMDCodeLineParserTest {
	@Test
	void test0() {
	    String inputText = "Simple text without formatting.\n" ;
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();

	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_INVALID, r);
	    assertEquals(0, input.position());
	}
	
	@Test
	void test1() {
	    String inputText = " Code Block formatting.\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<pre><code>Code Block formatting.\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test1a() {
	    String inputText = " Code Block formatting.\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl(null, null, null, "abc", "def", null, null, null, null);
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<pre class=\"def\"><code>Code Block formatting.\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void bufferOutTest1() {
	    String inputText = "\tAnother line of plain text.\n";
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip();
	    String result = output.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String expected = "<pre><code>Another line of plain text.\n";
	    		
	    assertEquals(expected, result);
	}
	
	@Test
	void bufferOutTest1a() {
	    String inputText = "\tAnother line of plain text.\n";
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl(null, null, null, "abc", "def", null, null, null, null);
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip();
	    String result = output.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String expected = "<pre class=\"def\"><code>Another line of plain text.\n";
	    		
	    assertEquals(expected, result);
	}
	
	@Test
	void bufferOut2Test1() {
		StringBuilder overSize = new StringBuilder(256);
		for(int j = 0;j<80;j++) {
			overSize.append("S"+j+" ");
		}
		
	    String inputText = " "+overSize+"\n";
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    StringBuilder sb = new StringBuilder(); 
	    CharBuffer output = CharBuffer.allocate(256);
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
	    String expected = "<pre><code>"+ overSize.toString()+"\n";
	    		
	    assertEquals(expected, result);
	}
	
	@Test
	void test2() {
		String inputText = " Here is a [link](http://example.com) and an image ![alt & text](http://image.com/img.jpg) for example\n";
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r;
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<pre><code>Here is a [link](http://example.com) and an image ![alt &amp; text](http://image.com/img.jpg) for example\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void bufferOutTest3() {
	    String inputText = "\t  \tAnother line of plain text.\n";
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip();
	    String result = output.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String expected = "<pre><code>  \tAnother line of plain text.\n";
	    		
	    assertEquals(expected, result);
	}
	
	@Test
	void test3() {
	    String inputText = "\t  Another line of plain text.\n";
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<pre><code>  Another line of plain text.\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test4() {
	    String inputText = " Not a new line at end";
	    System.out.println("----test4-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_VOID, r);
	}
	
	@Test
	void test5() {
	    String inputText = "     \t   ";
	    System.out.println("----test5-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText.toCharArray());
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_VOID, r);
	}
	
	@Test
	void test6() {
	    String inputText = " \n";
	    System.out.println("----test5-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeLineParser parser = new SMDCodeLineParser();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	}
}
