package vietfi.markdown.strict.block;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.CharBuffer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;

public class SMDOrderedListBlockParserTest {
	
	@Test
	void test0() {
	    String inputText = "Simple text without formatting.\n" +
	            "Another line of plain text.\n" +
	            "Goodbye.\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    assertEquals(0, input.position());
	}
	
	@Test
	void test1() {
		String trailer = "This not include";
	    String inputText = "1. Block quote formatting.\n" +
	            "2.  Another line of plain text.\n" +
	            "3.\tGoodbye.\n\n"+trailer;
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
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
	    		"<ol>","<li>Block quote formatting.","</li>",
	    		"<li>Another line of plain text.","</li>",
	    		"<li>Goodbye.","</li>",
	    		"</ol>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOutTest1() {
		String trailer = "This not include";
		 String inputText = "1. Block quote formatting.\n" +
		            "2.  Another line of plain text.\n" +
		            "3.\tGoodbye.\n\n"+trailer;
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip();
	    String result = output.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String[] expected = {
	    		"<ol>","<li>Block quote formatting.","</li>",
	    		"<li>Another line of plain text.","</li>",
	    		"<li>Goodbye.","</li>",
	    		"</ol>"
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
	    String inputText = "1. Block.\n" +
	            "2. Another line of plain text, for demo in the blockquote, but longer than buffer's length.\n" +
	    		"3. "+overSize+"\n"+
	            "4. The last line of plain text, for demo in the blockquote as well, Goodbye.\n\n"+trailer;
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
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
	    		"<ol>","<li>Block.","</li>",
	    		"<li>Another line of plain text, for demo in the blockquote, but longer than buffer's length.","</li>",
	    		"<li>"+overSize.toString(),"</li>",
	    		"<li>The last line of plain text, for demo in the blockquote as well, Goodbye.","</li>","</ol>"
	    };
	    		
	    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test2() {
		String trailer = "This not include";
	    String inputText = "a. Listing with **bold**.\n" +
	    		"b. Here is a [link](http://example.com) and an image ![alt text](http://image.com/img.jpg) for example.\n"+
	    		"   This continue.\n"+
	            "c. Goodbye.\n\n"+trailer;
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<ol>","<li>Listing with <b>bold</b>.","</li>",
	    		"<li>Here is a <a href=\"http://example.com\">link</a> and an image <img alt=\"alt text\" src=\"http://image.com/img.jpg\"> for example.",
	    		"This continue.",
	    		"</li>",
	    		"<li>Goodbye.",
	    		"</li>",
	    		"</ol>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test3() {
		String inputText = "i. Block quote *italic* is ok.\n" +
	            "ii. - Invalid. Goodbye.\n";
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
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
	    		"<ol>","<li>Block quote <i>italic</i> is ok.","</li>","</ol>",
	    		"ii. - Invalid. Goodbye."
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test4() {
		String inputText = "1. Block quote *italic* is ok.\n" +
				"\n"+
	            "2. Another blockquote. Goodbye.\n";
	    System.out.println("----test4-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
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
	    		"<ol>","<li>Block quote <i>italic</i> is ok.","</li>","</ol>",
	    		"2. Another blockquote. Goodbye."
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test5() {
		String inputText = "A) Block quote *italic* is ok.\n" +
				"   1. Another level.\n"+
				"   2. Continue.\n\n";
	    System.out.println("----test5-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
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
	    		"<ol>","<li>Block quote <i>italic</i> is ok.",
	    		"<ol>","<li>Another level.","</li>","<li>Continue.","</li>","</ol>",
	    		"</li>","</ol>",
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test6() {
		String inputText = "a. Block quote *italic* is ok.\n" +
				"    * Another level.\n\n";
	    System.out.println("----test6-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
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
	    		"<ol>","<li>Block quote <i>italic</i> is ok.",
	    		"<ul>","<li>Another level.","</li>","</ul>",
	    		"</li>","</ol>",
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test7() {
		String inputText = "1. Normal text\n"
				+ "   > Block quote *italic* is ok.\n"
				+ "   1. Another level.\n"
				+ "   > Block quote end\n\n";
	    System.out.println("----test7-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
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
	    		"<ol>","<li>Normal text",
	    		"<blockquote><p>Block quote <i>italic</i> is ok.","</p></blockquote>",
	    		"<ol>","<li>Another level.","</li>","</ol>",
	    		"<blockquote><p>Block quote end","</p></blockquote>",
	    		"</li>","</ol>",
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test8() {
		String inputText = "a) Normal text\n"
				+ "   > Block quote *italic* is ok.\n"
				+ "   * Another level.\n"
				+ "    \n"
				+ "   Paragraph at end\n"
				+ "b) Item text\n"
				+ "\n";
	    System.out.println("----test8-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDOrderedListBlockParser parser = new SMDOrderedListBlockParser();
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
	    		"<ol>","<li>Normal text",
	    		"<blockquote><p>Block quote <i>italic</i> is ok.","</p></blockquote>",
	    		"<ul>","<li>Another level.","</li>","</ul>",
	    		"<p>Paragraph at end","</p></li>",
	    		"<li>Item text","</li>","</ol>",
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
}
