package vietfi.markdown.strict.block;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;
import java.nio.CharBuffer;
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

public class SMDCodeByIndentBlockParserTest {
	@Test
	void test0() {
	    String inputText = "Simple text without formatting.\n" +
	            "Another line of plain text.\n" +
	            "Goodbye.\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByIndentBlockParser parser = new SMDCodeByIndentBlockParser();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    assertEquals(0, input.position());
	}
	
	@Test
	void test1() {
	    String inputText = " Code formatting.\n" +
	            " Another line of plain text & special text.\n" +
	            "   Goodbye.\n~~~\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByIndentBlockParser parser = new SMDCodeByIndentBlockParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of plain text &amp; special text.",
	    		"  Goodbye.", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOutTest1() {
	    String inputText = " Code formatting.\n" +
	            " Another line of plain text & special text.\n" +
	            "   Goodbye.\n~~~\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByIndentBlockParser parser = new SMDCodeByIndentBlockParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    int r;
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    writer.appendHtml(parser.markers(), input, output);
	    
	  //flip from read to write
	    output.flip();sb.append(output.toString());output.clear();
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of plain text &amp; special text.",
	    		"  Goodbye.", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOut2Test1() {
		StringBuilder overSize = new StringBuilder(SMDParser.MINIMUM_BUFFER_SIZE);
		for(int j = 0;j<SMDParser.MINIMUM_BUFFER_SIZE/4;j++) {
			overSize.append("S"+j+" ");
		}
	    String inputText = " Code formatting.\n" +
	            " Another line of plain text & special text.\n" +
	            " "+overSize+"\n"+
	            "   Goodbye.\n~~~\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByIndentBlockParser parser = new SMDCodeByIndentBlockParser();
	    CharBuffer output = CharBuffer.allocate(SMDParser.MINIMUM_BUFFER_SIZE);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    StringBuilder sb = new StringBuilder(SMDParser.MINIMUM_BUFFER_SIZE*2);
	    
	    int r;
	    boolean b;
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    assertFalse(b);
	    
	  //flip from read to write
	    output.flip();sb.append(output.toString());output.clear();
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    assertTrue(b);
	    
	  //flip from read to write
	    output.flip();sb.append(output.toString());output.clear();
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of plain text &amp; special text.",
	    		overSize.toString(),
	    		"  Goodbye.", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test2() {
	    String inputText = " Code formatting.";
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByIndentBlockParser parser = new SMDCodeByIndentBlockParser();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_VOID, r);
	}
	
	@Test
	void test3() {
	    String inputText1 = " Code formatting.\n" +
	            " Another line of code\n";
	    String inputText2 = " partially parsing.\n" +
	            " end after this\n\n";
	    System.out.println("----test3-----\n" + inputText1 + "\n----------\n"+inputText2+"\n---------\n");
	    
	    CharBuffer input = CharBuffer.allocate(1024);
	    StringBuilder sb = new StringBuilder(1024);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    input.append(inputText1);
	    input.flip();
	    SMDCodeByIndentBlockParser parser = new SMDCodeByIndentBlockParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_CONTINUE, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    parser.compact(input.position());
	    input.compact();	    
	    input.append(inputText2);
	    
	    input.flip();
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    render.produceHtml(parser.markers(), input, sb);	    
	    input.flip();
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of code",
	    		"partially parsing.",
	    		"end after this", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void xhtmlOutTest3() {
	    String inputText1 = " Code formatting.\n" +
	            " Another line of code\n";
	    String inputText2 = " partially parsing.\n" +
	            " end after this\n\n";
	    System.out.println("----test3-----\n" + inputText1 + "\n----------\n"+inputText2+"\n---------\n");
	    
	    CharBuffer input = CharBuffer.allocate(1024);
	    	    
	    input.append(inputText1);
	    input.flip();
	    SMDCodeByIndentBlockParser parser = new SMDCodeByIndentBlockParser();
	    SMDXhtmlWriter xhtmlWriter = new XhtmlWriterImpl();
	    
	    XMLOutputFactory factory = XMLOutputFactory.newFactory();
	    
	    try {
		    
		    StringWriter writer = new StringWriter(1024);
		    XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
		    xmlWriter.writeStartDocument();
		    xmlWriter.writeStartElement("body");
		    
		    int r = parser.parseNext(input);
		    assertEquals(SMDParser.SMD_BLOCK_CONTINUE, r);
		    xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
		    
		    parser.compact(input.position());
		    input.compact();	    
		    input.append(inputText2);
		    
		    input.flip();
		    
		    r = parser.parseNext(input);
		    assertEquals(SMDParser.SMD_BLOCK_END, r);
		    xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
		    input.flip();
		    
		    xmlWriter.writeEndElement();
	    	xmlWriter.writeEndDocument();
	    	
		    String result = writer.toString();
		    
		    System.out.append("Result:\n").append(result).append("\n\n");
		    String[] expected = {
		    		"<?xml version=\"1.0\" ?><body><pre>Code formatting.",
		    		"Another line of code",
		    		"partially parsing.",
		    		"end after this", 
		    		"</pre></body>"
		    };
		    		
		    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
		    
		    assertArrayEquals(expected, actual);
	    } catch (XMLStreamException e) {
			fail(e.toString());
		}
	}
}
