package vietfi.markdown.strict;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.CharBuffer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.render.HtmlRenderImpl;

public class SMDParserChainTests {
	
	@Test
	void test1() {
	    String inputText = "Simple text without formatting.\n" +
	            "> quote text.\n\n" +
	            " code text.\n\n" +
	            "1. item text.\n\n" +
	            "* bullet text.\n\n" +
	            "Paragraph **bold** ~~strike~~ *italic* `code` text.\n\n" +
	            "Goodbye.\n\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDParser parser = SMDParserChain.createParserOfContents();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_CONTINUE, r);
	    
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<p>Simple text without formatting.",
	            "</p><blockquote><p>quote text.","</p></blockquote>",
	            "<pre><code>code text.","</code></pre>",
	            "<ol>",
	            "<li>item text.","</li>","</ol>",
	            "<ul>",
	            "<li>bullet text.","</li>","</ul>",
	            "<p>Paragraph <b>bold</b> <s>strike</s> <i>italic</i> <code>code</code> text.",
	            "</p><p>Goodbye.","</p>"
	    };
	    
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test2() {
	    String inputText = "# heading #\n"
	    		+ "Simple text without formatting.\n" +
	            "> quote text.\n\n" +
	            " code text.\n\n" +
	            "1. item text.\n\n" +
	            "* bullet text.\n\n" +
	            "heading 2\n----\n\n"+
	            "Paragraph **bold** ~~strike~~ *italic* `code` text.\n\n\n\n" +
	            "Goodbye.\n\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDParser parser = SMDParserChain.createParserOfStandard();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_CONTINUE, r);
	    
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<h1>heading</h1>",
	    		"<p>Simple text without formatting.",
	            "</p><blockquote><p>quote text.","</p></blockquote>",
	            "<pre><code>code text.","</code></pre>",
	            "<ol>",
	            "<li>item text.","</li>","</ol>",
	            "<ul>",
	            "<li>bullet text.","</li>","</ul>",
	            "<h2>heading 2</h2>", "",
	            "<p>Paragraph <b>bold</b> <s>strike</s> <i>italic</i> <code>code</code> text.",
	            "</p>","",
	            "<p>Goodbye.","</p>"
	    };
	    
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
		
	    assertArrayEquals(expected, actual);
	}
}
