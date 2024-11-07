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
import vietfi.markdown.strict.SMDMarkers;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.SMDXhtmlWriter;
import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;
import vietfi.markdown.strict.render.XhtmlWriterImpl;

public class SMDCodeByMarkerBlockParserTest {
	
	@Test
	void test0() {
	    String inputText = "Simple text without formatting.\n" +
	            "~~~ lang\n\n"+'\u001C';
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDMarkers markers = new SMDMarkers(32);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser(markers);

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    assertEquals(0, input.position());

	    while(input.hasRemaining() && input.get() != '\n');
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	}
	
	@Test
	void test1() {
	    String inputText = "~~~\nCode formatting.\n" +
	            "Another line of plain text & special text.\n" +
	            "   Goodbye.\n~~~\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    assertEquals(inputText.length(), input.position());
	    
	    StringBuilder sb = new StringBuilder(256);
	    render.produceHtml(parser.markers(), input, sb);
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of plain text &amp; special text.",
	    		"   Goodbye.", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOutTest1() {
	    String inputText = "~~~\nCode formatting.\n" +
	            "Another line of plain text.\n" +
	            "Goodbye.\n~~~\n";
	    System.out.println("----buffer out test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    SMDHtmlWriter writer = new HtmlWriterImpl();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    assertEquals(inputText.length(), input.position());
	    CharBuffer output = CharBuffer.allocate(1024);
	    writer.appendHtml(parser.markers(), input, output);
	    
	    //flip from read to write
	    output.flip();
	    
	    System.out.append("Result:\n").append(output.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of plain text.",
	    		"Goodbye.", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = output.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOut2Test1() {
		StringBuilder overSize = new StringBuilder(SMDParser.MINIMUM_BUFFER_SIZE);
		for(int j = 0;j<SMDParser.MINIMUM_BUFFER_SIZE/4;j++) {
			overSize.append("S"+j+" ");
		}
	    String inputText = "~~~\nCode formatting.\n" +
	            "Another line of plain text. 1234567890\n" +
	    		overSize+"\n"+
	            "goodbye!\n"+
	            "~~~\n";
	    System.out.println("----buffer out 2 test1-----\n" + inputText + "\n----------");
	    
	    String[] expected = {
	    	"<pre><code>Code formatting.",
	    	"Another line of plain text. 1234567890",
	    	overSize.toString(),
	    	"goodbye!",
		    "</code></pre>"
		 };
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    StringBuilder sb = new StringBuilder();
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    assertEquals(inputText.length(), input.position());
	    CharBuffer output = CharBuffer.allocate(SMDParser.MINIMUM_BUFFER_SIZE);
	    
	    boolean b = writer.appendHtml(parser.markers(), input, output);
	    
	    assertFalse(b);
	    
	    //flip from read to write
	    output.flip();sb.append(output.toString());output.clear();
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    assertTrue(b);
	    
	    output.flip();sb.append(output.toString());output.clear();
	    
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    System.out.append("Result:\n")
	    	.append(sb.toString())
	    	.append("\n\n");
	    
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOut3Test1() {
		StringBuilder overSize = new StringBuilder(SMDParser.MINIMUM_BUFFER_SIZE);
		for(int j = 0;j<SMDParser.MINIMUM_BUFFER_SIZE/4;j++) {
			overSize.append("S"+j+" ");
		}
	    String inputText = "~~~\nCode formatting.\n" +
	            "Another line of plain text 1234567890 more.\n" +
	            overSize+"\n"+
	            "\n"+
	            overSize+"\n"+
	            "~~~\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    String[] expected = {
	    		"<pre><code>Code formatting.",
		    	"Another line of plain text 1234567890 more.",
		    	overSize.toString(),
		    	"",
		    	overSize.toString(),
		    	"</code></pre>"
	    };
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    SMDHtmlWriter writer = new HtmlWriterImpl();

	    StringBuilder sb = new StringBuilder();
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    assertEquals(inputText.length(), input.position());
	    CharBuffer output = CharBuffer.allocate(SMDParser.MINIMUM_BUFFER_SIZE);
	    
	    boolean b = writer.appendHtml(parser.markers(), input, output);
	    
	    assertFalse(b);

	    //flip from read to write
	    output.flip();sb.append(output.toString());output.clear();
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    assertFalse(b);
	    
	    output.flip();sb.append(output.toString());output.clear();
	    
	    b = writer.appendHtml(parser.markers(), input, output);
	    assertTrue(b);
	    
	  //flip from read to write
	    output.flip();sb.append(output.toString());output.clear();
	    
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    
	    System.out.append("Result:\n")
	    	.append(sb.toString())
	    	.append("\n\n");
	    
	    assertArrayEquals(expected, actual);
	    
	}
	
	@Test
	void test2() {
	    String inputText = "```\nCode formatting.\n" +
	            "Another line of code. Next is empty line\n" +
	            "\n" +
	            "Goodbye.\n```\n";
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    StringBuilder sb = new StringBuilder(256);
	    render.produceHtml(parser.markers(), input, sb);
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of code. Next is empty line",
	    		"",
	    		"Goodbye.", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}

	@Test
	void xhtmlOutTest2() {
	    String inputText = "```\nCode formatting.\n" +
	            "Another line of code. Next is empty line\n" +
	            "\n" +
	            "Goodbye.\n```\n";
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
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
		    		"<?xml version=\"1.0\" ?><body><pre>Code formatting.",
		    		"Another line of code. Next is empty line",
		    		"",
		    		"Goodbye.", 
		    		"</pre></body>"
		    };
		    		
		    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
		    		
		    assertArrayEquals(expected, actual);
		 } catch (XMLStreamException e) {
			fail(e.toString());
		}
	}
	
	@Test
	void test3() {
	    String inputText1 = "```\nCode formatting.\n" +
	            "Another line of code\n";
	    String inputText2 = "partially parsing.\n" +
	            "end after this\n```\n";
	    System.out.println("----test3-----\n" + inputText1 + "\n----------\n"+inputText2+"\n---------\n");
	    
	    CharBuffer input = CharBuffer.allocate(1024);
	    StringBuilder sb = new StringBuilder(1024);
	    
	    input.append(inputText1);
	    input.flip();
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
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
	void bufferOut1test3() {
	    String inputText1 = "```\nCode formatting.\n" +
	            "Another line of code\n";
	    String inputText2 = "partially parsing.\n" +
	            "end after this\n```\n";
	    System.out.println("----test3-----\n" + inputText1 + "\n----------\n"+inputText2+"\n---------\n");
	    
	    CharBuffer input = CharBuffer.allocate(1024);
	    CharBuffer output = CharBuffer.allocate(1024);
	    
	    input.append(inputText1);
	    input.flip();
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_CONTINUE, r);
	    boolean b = writer.appendHtml(parser.markers(), input, output);
	    
	    assertTrue(b);
	    
	    parser.compact(input.position());
	    writer.compact(input.position());
	    input.compact();
	    input.append(inputText2);
	    
	    input.flip();
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    b = writer.appendHtml(parser.markers(), input, output);
	    
	    assertTrue(b);	    
	    input.flip();
	    
	    output.flip();
	    
	    System.out.append("Result:\n").append(output.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of code",
	    		"partially parsing.",
	    		"end after this", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = output.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test4() {
	    String inputText = "~~ Not a code\n```\nCode formatting.\n" +
	            "Another line of code. Next is empty line\n" +
	            "\n" +
	            "Goodbye.\n```\nSome data\n";
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    assertEquals(0, input.position());
	    
	    while(input.hasRemaining() && input.get() != '\n');
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    StringBuilder sb = new StringBuilder(256);
	    render.produceHtml(parser.markers(), input, sb);
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code>Code formatting.",
	    		"Another line of code. Next is empty line",
	    		"",
	    		"Goodbye.", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test5() {
	    String inputText = "~~~ javascript\nCode formatting.\n" +
	            "Another line of code. Next is empty line\n" +
	            "```\n" +
	            "Goodbye.\n~~~\n";
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    StringBuilder sb = new StringBuilder(256);
	    render.produceHtml(parser.markers(), input, sb);
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<pre><code class=\"language-javascript\">Code formatting.",
	    		"Another line of code. Next is empty line",
	    		"```",
	    		"Goodbye.", 
	    		"</code></pre>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void xhtmlOutTest5() {
	    String inputText = "~~~javascript\nCode formatting.\n" +
	            "Another line of code. Next is empty line\n" +
	            "```\n" +
	            "Goodbye.\n~~~\n";
	    System.out.println("----test2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
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
		    		"<?xml version=\"1.0\" ?><body><pre class=\"language-javascript\">Code formatting.",
		    		"Another line of code. Next is empty line",
		    		"```",
		    		"Goodbye.", 
		    		"</pre></body>"
		    };
		    		
		    Object[] actual = result.lines().collect(Collectors.toList()).toArray();
		    		
		    assertArrayEquals(expected, actual);
		 } catch (XMLStreamException e) {
			fail(e.toString());
		}
	    
	}
	
	@Test
	void test6() {
		String inputText = "~~~ language invalid\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    assertEquals(0, parser.markers().markedLength());
	    		
	}
	
	@Test
	void test7() {
		String inputText = "~~~\nsome\n~~~2\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDCodeByMarkerBlockParser parser = new SMDCodeByMarkerBlockParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_CONTINUE, r);
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    int[] expectedState = { SMDParser.STATE_CODE_BLOCK, SMDParser.STATE_CODE_BLOCK };
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	    		
	}
}
