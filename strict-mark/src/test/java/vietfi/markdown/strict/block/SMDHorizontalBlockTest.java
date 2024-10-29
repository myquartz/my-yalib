package vietfi.markdown.strict.block;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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

public class SMDHorizontalBlockTest {
	@Test
	void test0() {
	    String inputText = "==\n" +
	            "=-=\n" +
	            "____=\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHorizontalBlockParser parser = new SMDHorizontalBlockParser();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    assertEquals(0, input.position());
	}
	
	@Test
	void test1() {
		String trailer = "This not include";
	    String inputText = "--- \n" +
	            "=====\n" +
	            "___\nBlock quote formatting.\n"+trailer;
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHorizontalBlockParser parser = new SMDHorizontalBlockParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    render.produceHtml(parser.markers(), input, sb);
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<hr>",
	    		"<hr class=\"double-line\">",
	    		"<hr class=\"underscore-line\">"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOutTest1() {
		String trailer = "This not include";
	    String inputText = "--- \n" +
	            "=====\n" +
	            "___\nBlock quote formatting.\n"+trailer;
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHorizontalBlockParser parser = new SMDHorizontalBlockParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r;
	    
	    StringBuilder sb = new StringBuilder();
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    writer.appendHtml(parser.markers(), input, output);
	    
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<hr>",
	    		"<hr class=\"double-line\">",
	    		"<hr class=\"underscore-line\">"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void xhtmlTest1() {
		String inputText = "--- \n" +
	            "=====\n" +
	            "___\n--";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHorizontalBlockParser parser = new SMDHorizontalBlockParser();
	    SMDXhtmlWriter xhtmlWriter = new XhtmlWriterImpl();
	    
	    int r;
	    
	    XMLOutputFactory factory = XMLOutputFactory.newFactory();
	    try {
	    	StringWriter writer = new StringWriter(1024);
		    XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
		    xmlWriter.writeStartDocument();
		    xmlWriter.writeStartElement("body");
		    
		    r = parser.parseNext(input);
		    assertEquals(SMDParser.SMD_BLOCK_END, r);
		    xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
		    
		    r = parser.parseNext(input);
		    assertEquals(SMDParser.SMD_BLOCK_END, r);
		    xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
		    
		    r = parser.parseNext(input);
		    assertEquals(SMDParser.SMD_BLOCK_END, r);
		    xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
		    
		    r = parser.parseNext(input);
		    assertEquals(SMDParser.SMD_VOID, r);
		    
		    xmlWriter.writeEndElement();
	    	xmlWriter.writeEndDocument();

	    	String result = writer.toString();

		    System.out.append("Result:\n").append(result).append("\n\n");
		    String expected = "<?xml version=\"1.0\" ?><body><hr/><hr class=\"double-line\"/><hr class=\"underscore-line\"/></body>";

		    assertEquals(expected, result);
	    } catch (XMLStreamException e) {
			fail(e.toString());
		}
	}
}
