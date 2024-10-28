package vietfi.markdown.strict.block;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;
import java.nio.CharBuffer;

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

public class SMDHeading12BlockParserTest {
	@Test
	void test0() {
	    String inputText = "Simple text without formatting.\n" +
	            "Another line of plain text.\n" +
	            "Goodbye.\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHeading12BlockParser parser = new SMDHeading12BlockParser();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_INVALID, r);
	    assertEquals(0, input.position());
	}
	
	@Test
	void test1() {
	    String inputText = "Heading 1 formatting.\n" +
	            "===\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHeading12BlockParser parser = new SMDHeading12BlockParser();
	    SMDHtmlRender render = new HtmlRenderImpl();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    assertEquals(inputText.length(), input.position());
	    StringBuilder sb = new StringBuilder(256);
	    render.produceHtml(parser.markers(), input, sb);
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<h1>Heading 1 formatting.</h1>\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test2() {
	    String inputText = "Heading 2 formatting.\n" +
	            "--- \nNot format\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHeading12BlockParser parser = new SMDHeading12BlockParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    StringBuilder sb = new StringBuilder(256);
	    render.produceHtml(parser.markers(), input, sb);
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<h2>Heading 2 formatting.</h2>\n";
	    		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void xhtmlTest2() {
	    String inputText = "Heading 1 XHTML space \n" +
	            "=== \nNot format\n";
	    System.out.println("----xhtmlTest2-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHeading12BlockParser parser = new SMDHeading12BlockParser();
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
		    String expected = "<?xml version=\"1.0\" ?><body><h1>Heading 1 XHTML space </h1></body>";
		    		
		    assertEquals(expected, result);
	    } catch (XMLStreamException e) {
			fail(e.toString());
		}
	}
	
	@Test
	void bufferOutTest2() {
	    String inputText = "Heading 2 formatting.\n" +
	            "--- \n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDHeading12BlockParser parser = new SMDHeading12BlockParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    assertEquals(inputText.length(), input.position());
	    StringBuilder sb = new StringBuilder(256);
	    writer.appendHtml(parser.markers(), input, output);
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String expected = "<h2>Heading 2 formatting.</h2>\n";
	    		
	    assertEquals(expected, sb.toString());
	}
}
