package vietfi.markdown.strict;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<p>Simple text without formatting.",
	            "</p>",
	    		"<blockquote><p>quote text.",
	            "</p></blockquote>",
	            "<pre><code>code text.",
	            "</code></pre>",
	            "<ol><li>item text.",
	            "</li>",
	            "</ol>",
	            "<ul><li>bullet text.",
	            "</li>",
	            "</ul>",
	            "<p>Paragraph <b>bold</b> <s>strike</s> <i>italic</i> <code>code</code> text.",
	            "</p>","<p>Goodbye.",
	            "</p>"
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
	    assertEquals(SMDParser.SMD_BLOCK_END, r);
	    
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    String[] expected = {
	    		"<h1>heading</h1>",
	    		"<p>Simple text without formatting.",
	            "</p>",
	            "<blockquote><p>quote text.","</p></blockquote>",
	            "<pre><code>code text.","</code></pre>",
	            "<ol><li>item text.","</li>","</ol>",
	            "<ul><li>bullet text.","</li>","</ul>",
	            "<h2>heading 2</h2>", "",
	            "<p>Paragraph <b>bold</b> <s>strike</s> <i>italic</i> <code>code</code> text.",
	            "</p>","","",
	            "<p>Goodbye.","</p>"
	    };
	    
	    Object[] actual = sb.toString().lines().collect(Collectors.toList()).toArray();
		
	    assertArrayEquals(expected, actual);
	}
	
	@Test
	void test3() {
	    String inputText = "# Test heading\n"
	    		+ "---\n"
	    		+ "After 7 weeks, I conclude that:\n"
	    		+ "\n"
	    		+ "1. Writing a parser for the **markdown** text is not easy.\n"
	    		+ "2. This is the [complex](https://daringfireball.net/projects/markdown/syntax#list) markdown sub list:\n"
	    		+ "   > Quote here.\n"
	    		+ "   \n"
	    		+ "    Code here.\n"
	    		+ "   \n"
	    		+ "   Paragraph at the end.\n"
	    		+ "\n"
	    		+ "\n"
	    		+ "* Unordered list here.\n"
	    		+ "   1. Here is sub item.\n"
	    		+ "       Body of code.\n"
	    		+ "   2. Continue\n"
	    		+ "* Continue the list item.\n"
	    		+ "\n"
	    		+ "===\n"
	    		+ "Last item.\n"
	    		+ "\n\n";
	    String expected = "<h1>Test heading</h1>\n"
	    		+ "<hr>\n"
	    		+ "<p>After 7 weeks, I conclude that:\n"
	    		+ "</p>\n<ol>"
	    		+ "<li>Writing a parser for the <b>markdown</b> text is not easy.\n"
	    		+ "</li>\n"
	    		+ "<li>This is the <a href=\"https://daringfireball.net/projects/markdown/syntax#list\">complex</a> markdown sub list:\n"
	    		+ "<blockquote><p>Quote here.\n"
	    		+ "</p></blockquote>\n"
	    		+ "<pre><code>Code here.\n"
	    		+ "</code></pre>\n"
	    		+ "<p>Paragraph at the end.\n"
	    		+ "</p></li>\n"
	    		+ "</ol>\n"
	    		+ "\n"
	    		+ "<ul>"
	    		+ "<li>Unordered list here.\n"
	    		+ "<ol>"
	    		+ "<li>Here is sub item.\n"
	    		+ "<pre><code>Body of code.\n"
	    		+ "</code></pre>\n"
	    		+ "</li>\n"
	    		+ "<li>Continue\n"
	    		+ "</li>\n"
	    		+ "</ol>\n"
	    		+ "</li>\n"
	    		+ "<li>Continue the list item.\n"
	    		+ "</li>\n"
	    		+ "</ul>\n"
	    		+ "<hr class=\"double-line\">\n"
	    		+ "<p>Last item.\n"
	    		+ "</p>\n\n"
	    		;
	    
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDParser parser = SMDParserChain.createParserOfStandard();

	    int r = parser.parseNext(input);
	    assertEquals(SMDParser.SMD_BLOCK_GETS_EMPTY_LINE, r);
	    
	    parser.compact(input.position());
	    
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    
	    render.produceHtml(parser.markers(), input, sb);
	    
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
		
	    assertEquals(expected, sb.toString());
	}
	
	@Test
	void test4() {
	    String[] inputText = {"# Test heading\n"
	    		+ "---\n"
	    		+ "After 7 weeks, I conclude that:\n"
	    		+ "\n"
	    		+ "1. Writing a parser for the **markdown** text is not ",
	    		"easy.\n"
	    		+ "2. This is the [complex](https://daringfireball.net/projects/markdown/syntax#list) markdown sub list:\n"
	    		+ "   > Quote here.\n"
	    		+ "   \n"
	    		+ "    Code here.\n"
	    		+ "   \n"
	    		+ "   Paragraph at the end.\n"
	    		+ "\n"
	    		+ "\n"
	    		+ "* Unordered list here.\n"
	    		+ "   1. Here is sub",
	    		" item.\n"
	    		+ "       Body of code.\n"
	    		+ "   2. Continue\n"
	    		+ "* Continue the list item.\n"
	    		+ "\n"
	    		+ "===\n"
	    		+ "Last item.\n"
	    		+ "\n\n"};
	    String expected = "<h1>Test heading</h1>\n"
	    		+ "<hr>\n"
	    		+ "<p>After 7 weeks, I conclude that:\n"
	    		+ "</p>\n<ol>"
	    		+ "<li>Writing a parser for the <b>markdown</b> text is not easy.\n"
	    		+ "</li>\n"
	    		+ "<li>This is the <a href=\"https://daringfireball.net/projects/markdown/syntax#list\">complex</a> markdown sub list:\n"
	    		+ "<blockquote><p>Quote here.\n"
	    		+ "</p></blockquote>\n"
	    		+ "<pre><code>Code here.\n"
	    		+ "</code></pre>\n"
	    		+ "<p>Paragraph at the end.\n"
	    		+ "</p></li>\n"
	    		+ "</ol>\n"
	    		+ "\n"
	    		+ "<ul>"
	    		+ "<li>Unordered list here.\n"
	    		+ "<ol>"
	    		+ "<li>Here is sub item.\n"
	    		+ "<pre><code>Body of code.\n"
	    		+ "</code></pre>\n"
	    		+ "</li>\n"
	    		+ "<li>Continue\n"
	    		+ "</li>\n"
	    		+ "</ol>\n"
	    		+ "</li>\n"
	    		+ "<li>Continue the list item.\n"
	    		+ "</li>\n"
	    		+ "</ul>\n"
	    		+ "<hr class=\"double-line\">\n"
	    		+ "<p>Last item.\n"
	    		+ "</p>\n\n"
	    		;
	    
	    System.out.println("----test4-----\n");
	    
	    CharBuffer buffer = CharBuffer.allocate(1024);
	    SMDParser markdownParser = SMDParserChain.createParserOfStandard();
	    SMDHtmlRender htmlRenderer = new HtmlRenderImpl();
	    StringBuilder sb = new StringBuilder(48*1024);
	    StringBuilder debug = new StringBuilder(48*1024);
	    
	    int r;
	    for(int i=0;i<=inputText.length;i++) {
	    	
    		if(i==inputText.length)
    			//force end
    	    	markdownParser.endBlock(buffer.position());
    		else {
    			buffer.append(inputText[i]);
		    	buffer.flip();
		    	r = markdownParser.parseNext(buffer);
		    	if(r == SMDParser.SMD_BLOCK_INVALID) {
		    		System.err.println("Invalid Markdown, result="+r);
		    		break;
		    	}
		    	debug.append("Loop "+i+" "+markdownParser.markers().toString()).append('\n');
		    	htmlRenderer.produceHtml(markdownParser.markers(), buffer, sb);
		    	int pos = markdownParser.markers().compactMarkers(buffer.position());
		    	markdownParser.compact(pos);
		    	if(pos < buffer.position())
		    		buffer.position(pos);
		    	buffer.compact();
    		}
	    }
	    
		debug.append(markdownParser.markers().toString()).append('\n');
		htmlRenderer.produceHtml(markdownParser.markers(), buffer, sb);
	    
		System.out.append("Debug:\n").append(debug.toString()).append("\n");
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
		
	    assertEquals(expected, sb.toString());
	}
}
