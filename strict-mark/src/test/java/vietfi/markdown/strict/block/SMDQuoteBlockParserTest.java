package vietfi.markdown.strict.block;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringWriter;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.SMDXhtmlWriter;
import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;
import vietfi.markdown.strict.render.XhtmlWriterImpl;

public class SMDQuoteBlockParserTest {
	
	@Test
	void test0() {
	    String inputText = ">a\n" +
	            "> >\n" +
	            "Goodbye.\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();

	    int r = parser.parseNext(input);
	    
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    assertEquals(0, input.position());
	    
	    char ch = input.get(); //consume until the space
		while(input.hasRemaining() && ch != '\n') {
			ch = input.get(); //consume until the new line.
		}
		
		r = parser.parseNext(input);
	    
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
		ch = input.get(); //consume until the space
		while(input.hasRemaining() && ch != '\n') {
			ch = input.get(); //consume until the new line.
		}
		
		r = parser.parseNext(input);
	    
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	}
	
	@Test
	void test1() {
		String trailer = "This not include";
	    String inputText = "> Block quote formatting.\n" +
	            ">  Another line of plain text.\n" +
	    		">\n"+
	            "> Goodbye.\n>\n"+trailer;
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<blockquote><p>Block quote formatting.",
	    		"Another line of plain text.",
	    		"</p><p>Goodbye.",
	    		"</p></blockquote>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOutTest1() {
		String trailer = "This not include";
	    String inputText = "> Block quote formatting.\n" +
	            ">  Another line of plain text.\n" +
	            "> Goodbye.\n\n"+trailer;
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip();
	    String result = output.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String[] expected = {
	    		"<blockquote><p>Block quote formatting.",
	    		"Another line of plain text.",
	    		"Goodbye.","</p></blockquote>"
	    };
	    		
	    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void xhtmlTest1() {
		String trailer = "This not include";
	    String inputText = "> Block quote formatting.\n" +
	            ">  Another line of plain text.\n" +
	            "> Goodbye.\n\n"+trailer;
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    SMDXhtmlWriter xhtmlWriter = new XhtmlWriterImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    XMLOutputFactory factory = XMLOutputFactory.newFactory();
	    
	    try {
		    StringWriter writer = new StringWriter(1024);
		    XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
		    xmlWriter.writeStartDocument();
		    xmlWriter.writeStartElement("body");
		    
		    xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
		    xmlWriter.writeEndElement();
	    	xmlWriter.writeEndDocument();
	    	
	    	String result = writer.toString();
	    	
		    System.out.append("Result:\n").append(result).append("\n\n");
		    String[] expected = {
		    		"<?xml version=\"1.0\" ?><body><blockquote><p>Block quote formatting.",
		    		"Another line of plain text.",
		    		"Goodbye.",
		    		"</p></blockquote></body>"
		    };
		    		
		    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
		    		
		    assertArrayEquals(expected, actual);
		 } catch (XMLStreamException e) {
			fail(e.toString());
		}	    
	    
	}
	
	@Test
	void bufferOut2Test2() {
		StringBuilder overSize = new StringBuilder(640);
		for(int j = 0;j<140;j++) {
			overSize.append("S"+j+" ");
		}
		String trailer = "This not include";
	    String inputText = "> Block.\n" +
	            ">  Another line of plain text, for demo in the blockquote, but longer than buffer's length.\n" +
	    		"> "+overSize+"\n"+
	            "> The last line of plain text, for demo in the blockquote as well, Goodbye.\n\n"+trailer;
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    StringBuilder sb = new StringBuilder(); 
	    CharBuffer output = CharBuffer.allocate(512);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    boolean b;
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    assertFalse(b);
	    System.out.append("Parially:\n").append(sb.toString()).append("\n");
	    b = writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip(); sb.append(output.toString()); output.clear();
	    assertTrue(b);
	    
	    String result = sb.toString();
	    System.out.append("Result:\n").append(result).append("\n\n");
	    String[] expected = {
	    		"<blockquote><p>Block.",
	    		"Another line of plain text, for demo in the blockquote, but longer than buffer's length.",
	    		overSize.toString(),
	    		"The last line of plain text, for demo in the blockquote as well, Goodbye.","</p></blockquote>"
	    };
	    		
	    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test2() {
		String trailer = "This not include";
	    String inputText = "> Block quote **bold**.\n" +
	    		"> Here is a [link](http://example.com) and an image ![alt text](http://image.com/img.jpg) for example\n"+
	    		">\n"+
	            "> Goodbye.\n\n"+trailer;
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString())
	    	.append("\nResult:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<blockquote><p>Block quote <b>bold</b>.",
	    		"Here is a <a href=\"http://example.com\">link</a> and an image <img alt=\"alt text\" src=\"http://image.com/img.jpg\"> for example",
	    		"</p><p>Goodbye.","</p></blockquote>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test3() {
		String inputText = "> Block quote *italic* is ok.\n" +
	            "> > Invalid.\n"+
	            ">Goodbye.\n";
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    UnparseableBlockParser fallparser = new UnparseableBlockParser(parser.markers());
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
	    r = fallparser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<blockquote><p>Block quote <i>italic</i> is ok.",
	    		"</p>&gt; Invalid.","</blockquote>",
	    		"&gt;Goodbye."
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test4() {
		String[] inputTexts = {
				"Not quote\n",
				"> Block quote *italic* is ok.\n",
				"\n> Another blockquote. Goodbye.\n\n>Invalid\n"};
	    
	    CharBuffer input = CharBuffer.allocate(256);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    UnparseableBlockParser fallparser = new UnparseableBlockParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r1, r2;
	    int i = 0;
	    int c = 1;
	    while(i < inputTexts.length) {
	    	input.append(inputTexts[i]);
	    	input.flip();
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
		    
		    parser.compact(input.position());
		    fallparser.compact(input.position());
		    input.clear();
		    i++;
	    }
	    
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"Not quote",
	    		"<blockquote><p>Block quote <i>italic</i> is ok.","</p></blockquote>",
	    		"<blockquote><p>Another blockquote. Goodbye.","</p></blockquote>",
	    		"&gt;Invalid"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test5() {
		String inputText = "> Test 123\n"
				+ ">\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_CONTINUE, r);
	    
	    System.out.append("Result 1:\n").append(parser.markers().toString()).append("\n");
	    
	    int[] expectedState = { SMDParser.STATE_QUOTE_BLOCK, SMDParser.STATE_PARAGRAPH,  SMDParser.STATE_TEXT, SMDParser.STATE_TEXT, 
	    		SMDParser.STATE_PARAGRAPH, SMDParser.STATE_QUOTE_BLOCK};
	    
	    assertArrayEquals(Arrays.copyOf(expectedState, expectedState.length - 1), parser.markers().toStateArray());
	    
	    parser.endBlock(input.position());
	    
	    System.out.append("Result 2:\n").append(parser.markers().toString()).append("\n");
	    
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	    		
	}
	
	@Test
	void test6() {
		String inputText = "> Test 123\n"
				+ "> a\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDQuoteBlockParser parser = new SMDQuoteBlockParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_CONTINUE, r);
	    
	    System.out.append("Result 1:\n").append(parser.markers().toString()).append("\n");
	    
	    int[] expectedState = { SMDParser.STATE_QUOTE_BLOCK, SMDParser.STATE_PARAGRAPH,  SMDParser.STATE_TEXT, SMDParser.STATE_TEXT,
	    		SMDParser.STATE_TEXT, SMDParser.STATE_TEXT, SMDParser.STATE_PARAGRAPH, SMDParser.STATE_QUOTE_BLOCK};
	    
	    assertArrayEquals(Arrays.copyOf(expectedState, expectedState.length - 2), parser.markers().toStateArray());
	    
	    parser.endBlock(input.position());
	    
	    System.out.append("Result 2:\n").append(parser.markers().toString()).append("\n");
	    
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	    		
	}
}
