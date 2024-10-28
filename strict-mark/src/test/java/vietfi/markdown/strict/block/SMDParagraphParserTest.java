package vietfi.markdown.strict.block;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.CharBuffer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;

public class SMDParagraphParserTest {
	
	@Test
	void test0() {
	    String inputText = "Unlike others, the paragraph can be parse without any marker.";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDParagraphParser parser = new SMDParagraphParser();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_VOID, r);
	}
	
	@Test
	void test1() {
	    String inputText = "Simple text without formatting.\n" +
	            "Unlike others, the paragraph can be parse without any marker.\n" +
	            "Goodbye.\n\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDParagraphParser parser = new SMDParagraphParser();
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<p>Simple text without formatting.",
	    		"Unlike others, the paragraph can be parse without any marker.",
	    		"Goodbye.", 
	    		"</p>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void bufferOutTest1() {
		String inputText = "Simple text without formatting.\n" +
	            "Unlike others, the paragraph can be parse without any marker.\n" +
	            "Goodbye.\n\n";
	    System.out.println("----test1-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDParagraphParser parser = new SMDParagraphParser();
	    CharBuffer output = CharBuffer.allocate(1024);
	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlWriter writer = new HtmlWriterImpl();
	    
	    int r;
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    writer.appendHtml(parser.markers(), input, output);
	    output.flip(); sb.append(output.toString()); output.clear();
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<p>Simple text without formatting.",
	    		"Unlike others, the paragraph can be parse without any marker.",
	    		"Goodbye.", 
	    		"</p>"
	    };
	    		
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
	    		
	    assertArrayEquals(expected, actual);
	}
}
