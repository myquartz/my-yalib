package vietfi.markdown.strict.block;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;

public class SMDUnorderedListBlockParserTest {

	@Test
	void test0() {
	    String inputText = "Simple text without formatting.\n" +
	            "Another line of plain text.\n" +
	            "Goodbye.\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    assertEquals(0, input.position());
	}
	
	@Test
	void test1() {
		String trailer = "This not include";
	    String inputText = "* Block quote formatting.\n" +
	            "*  Another line of plain text.\n" +
	            "*\tGoodbye.\n\n"+trailer;
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = 0;
	    int c = 0;
	    while(r != SMDParser.SMD_BLOCK_END && r != SMDParser.SMD_BLOCK_INVALID && c < 30) {
	    	c++;
	    	r = parser.parseNext(input);
	    }
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Block quote formatting.","</li>",
	    		"<li>Another line of plain text.","</li>",
	    		"<li>Goodbye.","</li>",
	    		"</ul>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOutTest1() {
		String trailer = "This not include";
		 String inputText = "* Block quote formatting.\n" +
		            "*  Another line of plain text.\n" +
		            "*\tGoodbye.\n\n"+trailer;
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip();
	    String result = output.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Block quote formatting.","</li>",
	    		"<li>Another line of plain text.","</li>",
	    		"<li>Goodbye.","</li>",
	    		"</ul>"
	    };
	    		
	    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOut2Test1() {
		StringBuilder overSize = new StringBuilder(294);
		for(int j = 0;j<80;j++) {
			overSize.append("S"+j+" ");
		}
		String trailer = "This not include";
	    String inputText = "- Block.\n" +
	            "- Another line of plain text, for demo in the blockquote, but longer than buffer's length.\n" +
	    		"- "+overSize+"\n"+
	            "- The last line of plain text, for demo in the blockquote as well, Goodbye.\n\n"+trailer;
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    StringBuilder sb = new StringBuilder(); 
	    CharBuffer output = CharBuffer.allocate(294);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    
	    boolean b = writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    assertTrue(b);
	    
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    assertTrue(b);
	    
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    assertTrue(b);
	    
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    String result = sb.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Block.","</li>",
	    		"<li>Another line of plain text, for demo in the blockquote, but longer than buffer's length.","</li>",
	    		"<li>"+overSize.toString(),"</li>",
	    		"<li>The last line of plain text, for demo in the blockquote as well, Goodbye.","</li>","</ul>"
	    };
	    		
	    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test2() {
		String trailer = "This not include";
	    String inputText = "* Listing with **bold**.\n" +
	    		"*  Here is a [link](http://example.com) and an image ![alt text](http://image.com/img.jpg) for example.\n"+
	    		"   This continue.\n"+
	            "* Goodbye.\n\n"+trailer;
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Listing with <b>bold</b>.","</li>",
	    		"<li>Here is a <a href=\"http://example.com\">link</a> and an image <img alt=\"alt text\" src=\"http://image.com/img.jpg\"> for example.",
	    		"This continue.",
	    		"</li>",
	    		"<li>Goodbye.",
	    		"</li>",
	    		"</ul>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test3() {
		String inputText = "- Block quote *italic* is ok.\n" +
	            "- - Invalid. Goodbye.\n";
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    UnparseableBlockParser fallparser = new UnparseableBlockParser(parser.markers());
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
	    r = fallparser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Block quote <i>italic</i> is ok.","</li>","</ul>",
	    		"- - Invalid. Goodbye."
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test4() {
		String inputText = "* Block quote *italic* is ok.\n" +
				"\n"+
	            "* Another blockquote. Goodbye.\n\n";
	    System.out.println("----test4-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    UnparseableBlockParser fallparser = new UnparseableBlockParser(parser.markers());
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r1, r2;
	    int c = 1;
	    while(input.hasRemaining() && c < 50) {
		    r1 = parser.parseNext(input);
		    if(r1 == SMDParser.SMD_VOID || r1 == SMDParser.SMD_BLOCK_INVALID) {
		    	r2 = fallparser.parseNext(input);
		    	
		    	if(r2 == SMDParser.SMD_VOID || r2 == SMDParser.SMD_BLOCK_INVALID) {
		    		//end of file?
		    		break;
		    	}
		    }
		    
		    c++;
	    }
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Block quote <i>italic</i> is ok.","</li>",
	    		"<li>Another blockquote. Goodbye.","</li>","</ul>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test5() {
		String inputText = "* Block quote *italic* is ok.\n" +
				"   - Another level.\n"+
				"   - Continue.\n\n";
	    System.out.println("----test5-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    UnparseableBlockParser fallparser = new UnparseableBlockParser(parser.markers());
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r1, r2;
	    int c = 1;
	    while(input.hasRemaining() && c < 50) {
		    r1 = parser.parseNext(input);
		    if(r1 != SMDParser.SMD_VOID && r1 != SMDParser.SMD_BLOCK_INVALID)
		    	render.produceHtml(parser.markers(), input, sb);
		    if(r1 == SMDParser.SMD_VOID || r1 == SMDParser.SMD_BLOCK_INVALID) {
		    	r2 = fallparser.parseNext(input);
		    	if(r2 == SMDParser.SMD_VOID || r2 == SMDParser.SMD_BLOCK_INVALID) {
		    		//end of file?
		    		break;
		    	}
		    }
		    
		    c++;
	    }
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Block quote <i>italic</i> is ok.",
	    		"<ul><li>Another level.","</li>","<li>Continue.","</li>","</ul>",
	    		"</li>","</ul>",
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test6() {
		String inputText = "* Block quote *italic* is ok.\n" +
				"   1. Another level.\n\n";
	    System.out.println("----test6-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    UnparseableBlockParser fallparser = new UnparseableBlockParser(parser.markers());
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r1, r2;
	    int c = 1;
	    while(input.hasRemaining() && c < 50) {
		    r1 = parser.parseNext(input);
		    if(r1 == SMDParser.SMD_VOID || r1 == SMDParser.SMD_BLOCK_INVALID) {
		    	r2 = fallparser.parseNext(input);
		    	if(r2 == SMDParser.SMD_VOID || r2 == SMDParser.SMD_BLOCK_INVALID) {
		    		//end of file?
		    		break;
		    	}
		    }
		    
		    c++;
	    }
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Block quote <i>italic</i> is ok.",
	    		"<ol><li>Another level.","</li>","</ol>",
	    		"</li>","</ul>",
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test7() {
		String inputText = "* Normal text\n"
				+ "  > Block quote *italic* is ok.\n"
				+ "  1. Another level.\n"
				+ "  > Block quote end\n\n";
	    System.out.println("----test7-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    UnparseableBlockParser fallparser = new UnparseableBlockParser(parser.markers());
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r1, r2;
	    int c = 1;
	    while(input.hasRemaining() && c < 50) {
		    r1 = parser.parseNext(input);
		    if(r1 == SMDParser.SMD_VOID || r1 == SMDParser.SMD_BLOCK_INVALID) {
		    	r2 = fallparser.parseNext(input);
		    	if(r2 == SMDParser.SMD_VOID || r2 == SMDParser.SMD_BLOCK_INVALID) {
		    		//end of file?
		    		break;
		    	}
		    }
		    
		    c++;
	    }
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Normal text",
	    		"<blockquote><p>Block quote <i>italic</i> is ok.","</p></blockquote>",
	    		"<ol><li>Another level.","</li>","</ol>",
	    		"<blockquote><p>Block quote end","</p></blockquote>",
	    		"</li>","</ul>",
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test8() {
		String inputText = "* Normal text\n"
				+ "  > Block quote *italic* is ok.\n"
				+ "  1. Another level.\n"
				+ "    \n"
				+ "  Paragraph at end\n\n";
	    System.out.println("----test8-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    UnparseableBlockParser fallparser = new UnparseableBlockParser(parser.markers());
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r1, r2;
	    int c = 1;
	    while(input.hasRemaining() && c < 50) {
		    r1 = parser.parseNext(input);
		    if(r1 == SMDParser.SMD_VOID || r1 == SMDParser.SMD_BLOCK_INVALID) {
		    	r2 = fallparser.parseNext(input);
		    	if(r2 == SMDParser.SMD_VOID || r2 == SMDParser.SMD_BLOCK_INVALID) {
		    		//end of file?
		    		break;
		    	}
		    }
		    
		    c++;
	    }
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ul><li>Normal text",
	    		"<blockquote><p>Block quote <i>italic</i> is ok.","</p></blockquote>",
	    		"<ol><li>Another level.","</li>","</ol>",
	    		"<p>Paragraph at end","</p></li>","</ul>",
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test9() {
		String inputText = "- # Heading\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDUnorderedListBlockParser parser = new SMDUnorderedListBlockParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    assertEquals(0, parser.markers().markedLength());
	    		
	}
	
}
