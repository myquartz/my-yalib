package vietfi.markdown.strict.line;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringWriter;
import java.nio.CharBuffer;
import java.util.Arrays;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;

import vietfi.markdown.strict.render.HtmlRenderImpl;
import vietfi.markdown.strict.render.HtmlWriterImpl;
import vietfi.markdown.strict.render.XhtmlWriterImpl;
import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDHtmlWriter;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.SMDXhtmlWriter;

public class SMDTextLineParserTest {

	@Test
	void testNoEnd() {
	    String inputText = "Simple text,\n"+
	    				"but without no line end";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();

	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    int c = 1;
	    while (input.hasRemaining() && c<5) {
	        System.out.println("testNoend " + c);
	        int b = parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
	        System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	        switch (c) {
	            case 1:
	                assertEquals("Simple text,\n", sb.toString());
	                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
	                break;
	            case 2:
	                assertEquals(SMDLineParser.SMD_LINE_VOID, b);
	                break;
	        }
	        if(SMDLineParser.SMD_LINE_VOID == b)
	        	break;
	        sb.setLength(0);
	        c++;
	    }
	}

	@Test
	void test1char() {
	    String inputText = "S";
	    
	    CharBuffer input = CharBuffer.allocate(1024);
	    input.append(inputText);
	    input.flip();
	    
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_VOID, r);
	    
	    input.compact();
	    input.append('\n');
	    input.flip();
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    parser.endLine(input.position());
	    
	    int[] expectedState = { SMDParser.STATE_TEXT, SMDParser.STATE_TEXT };
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	}
	
	@Test
	void test2char() {
	    String inputText = "Si";
	    
	    CharBuffer input = CharBuffer.allocate(1024);
	    input.append(inputText);
	    input.flip();
	    
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_VOID, r);
	    
	    input.compact();
	    input.append('\n');
	    input.flip();
	    
	    r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    parser.endLine(input.position());
	    
	    int[] expectedState = { SMDParser.STATE_TEXT, SMDParser.STATE_TEXT };
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	}
	
	@Test
	void testEmptyLine() {
	    String inputText = "\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseLine(input);
	    assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, r);
	    assertEquals(0, parser.markers().markedLength());
	}
	
	@Test
	void test0() {
	    String inputText = "Simple text without formatting.\n" +
	            "Another line of plain text.\n" +
	            "Goodbye.\n";
	    System.out.println("----test0-----\n" + inputText + "\n----------");
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);

	    StringBuilder sb = new StringBuilder(256);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    int c = 1;
	    while (input.hasRemaining()) {
	        System.out.println("test0 " + c);
	        parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
	        System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	        switch (c) {
	            case 1:
	                assertEquals("Simple text without formatting.\n", sb.toString());
	                break;
	            case 2:
	                assertEquals("Another line of plain text.\n", sb.toString());
	                break;
	            case 3:
	                assertEquals("Goodbye.\n", sb.toString());
	                break;
	        }
	        
	        sb.setLength(0);
	        c++;
	    }
	}
	
	@Test
	void xhtmlTest0() {
	    String inputText = "Simple text without formatting.\n" +
	            "Another line of plain text.\n" +
	            "Goodbye.\n";
	    System.out.println("----XHTML test0-----\n" + inputText + "\n----------");
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);
	    SMDXhtmlWriter xhtmlWriter = new XhtmlWriterImpl();
	    
	    XMLOutputFactory factory = XMLOutputFactory.newFactory();
	    
	    int c = 1;
	    try {
		    while (input.hasRemaining()) {
		        System.out.println("test0 " + c);
		        StringWriter writer = new StringWriter(1024);
			    XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
			    xmlWriter.writeStartDocument();
			    xmlWriter.writeStartElement("body");
			    
		        parser.parseLine(input);
	        	xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
	        	
	        	xmlWriter.writeEndElement();
	        	xmlWriter.writeEndDocument();
	        	
	        	String result = writer.toString();
	        	//need <?xml version="1.0" ?><body>
		        switch (c) {
		            case 1:
		            	parser.printDebug();
		            	System.out.append("Result:\n").append(result).append("\n\n");
		                assertEquals("<?xml version=\"1.0\" ?><body>Simple text without formatting.\n</body>", result);
		                break;
		            case 2:
		                assertEquals("<?xml version=\"1.0\" ?><body>Another line of plain text.\n</body>", result);
		                break;
		            case 3:
		                assertEquals("<?xml version=\"1.0\" ?><body>Goodbye.\n</body>", result);
		                break;
		        }
		        
		        
		        c++;
		    }
	    } catch (XMLStreamException e) {
			fail(e.toString());
		}
	}
	
	@Test
	void test1() {
		String inputText = "This is some text. **Bold text** and *italic text*.\n" +
                "__Underlined text__ with `inline code`.\n" +
                "Here is a [link](http://example.com) and an image ![alt text](http://image.com/img.jpg) for example\n"+
                "Some markers (bold, italic, underline) do not allow a space right followed the begin marker, eg. ** bold ** * italic* __ underline__ but ` name` is ok.\n"+
                "Bold is not like this: ** it would not bold** neither after.\n"+
                "Good bye\n";
		System.out.println("----test1-----\n"+inputText+"\n----------");
        CharBuffer input = CharBuffer.wrap(inputText);
        SMDTextLineParser parser = new SMDTextLineParser();
        
        StringBuilder sb = new StringBuilder(256);
        SMDHtmlRender render = new HtmlRenderImpl();
        int c = 1;
        while(input.hasRemaining()) {
        	System.out.println("test1 = "+c+" buffer pos="+input.position());
        	//parse input to sb, that is appending the result
        	int r = parser.parseLine(input);
        	System.out.println("return "+r+" buffer pos="+input.position());
        	parser.printDebug();
        	render.produceHtml(parser.markers(), input, sb);
        	
        	System.out.append("Result:\n").append(sb.toString()).append("\n\n")
        	.println();
        	//each line asserts 
        	switch(c) {
        	case 1:
        		assertEquals("This is some text. <b>Bold text</b> and <i>italic text</i>.\n", sb.toString());
        		break;
        	case 2:
        		assertEquals("<u>Underlined text</u> with <code>inline code</code>.\n", sb.toString());
        		break;
        	case 3:
        		assertEquals("Here is a <a href=\"http://example.com\">link</a> and an image <img alt=\"alt text\" src=\"http://image.com/img.jpg\"> for example\n", sb.toString());
        		break;
        	case 4:
        		assertEquals("Some markers (bold, italic, underline) do not allow a space right followed the begin marker, eg. ** bold ** * italic* __ underline__ but <code> name</code> is ok.\n", sb.toString());
        		break;
        	case 5:
        		assertEquals("Bold is not like this: ** it would not bold** neither after.\n", sb.toString());
        		break;
        	case 6:
        		assertEquals("Good bye\n", sb.toString());
        		break;
        	}
        	sb.setLength(0);
        	c++;
        }
	}

	@Test
	void bufferOutTest1() {
		String inputText = "This is some text. **Bold text** and *italic text*.\n" +
                "__Underlined text__ with `inline code`.\n" +
                "Here is a [link](http://example.com) and an image ![alt text](http://image.com/img.jpg) for example\n"+
                "Some markers (bold, italic, underline) do not allow a space right followed the begin marker, eg. ** bold ** * italic* __ underline__ but ` name` is ok.\n"+
                "Bold is not like this: ** it would not bold** neither after.\n"+
                "Good bye\n";
		System.out.println("----test1-----\n"+inputText+"\n----------");
        CharBuffer input = CharBuffer.wrap(inputText);
        SMDTextLineParser parser = new SMDTextLineParser();
        CharBuffer output = CharBuffer.allocate(1024);
        SMDHtmlWriter writer = new HtmlWriterImpl();
        
        int r = 0;
        int c = 0;
        while(input.hasRemaining() && r != SMDLineParser.SMD_LINE_BLANK_OR_EMPTY && r != SMDLineParser.SMD_LINE_INVALID) {
        	//parse input to sb, that is appending the result
        	c++;
        	r = parser.parseLine(input);
        	writer.appendHtml(parser.markers(), input, output);
        	parser.printDebug();
        	output.flip();
        	String result = output.toString();
        	output.clear();
        	System.out.append("Result:\n").append(result).println();
        	//each line asserts 
        	switch(c) {
        	case 1:
        		assertEquals("This is some text. <b>Bold text</b> and <i>italic text</i>.\n", result);
        		break;
        	case 2:
        		assertEquals("<u>Underlined text</u> with <code>inline code</code>.\n", result);
        		break;
        	case 3:
        		assertEquals("Here is a <a href=\"http://example.com\">link</a> and an image <img alt=\"alt text\" src=\"http://image.com/img.jpg\"> for example\n", result);
        		break;
        	case 4:
        		assertEquals("Some markers (bold, italic, underline) do not allow a space right followed the begin marker, eg. ** bold ** * italic* __ underline__ but <code> name</code> is ok.\n", result);
        		break;
        	case 5:
        		assertEquals("Bold is not like this: ** it would not bold** neither after.\n", result);
        		break;
        	case 6:
        		assertEquals("Good bye\n", result);
        		break;
        	}
        	
        	
        }
	}

	@Test
	void xhtmlTest1() {
		String inputText = "This is some text. **Bold text** and *italic text*.\n" +
                "__Underlined text__ with `inline code`.\n" +
                "Here is a [link](http://example.com) and an image ![alt text](http://image.com/img.jpg) for example\n"+
                "Some markers (bold, italic, underline) do not allow a space right followed the begin marker, eg. ** bold ** * italic* __ underline__ but ` name` is ok.\n"+
                "Bold is not like this: ** it would not bold** neither after.\n"+
                "Good bye\n";
		System.out.println("----test1-----\n"+inputText+"\n----------");
        CharBuffer input = CharBuffer.wrap(inputText);
        SMDTextLineParser parser = new SMDTextLineParser(64);
        SMDXhtmlWriter xhtmlWriter = new XhtmlWriterImpl();
        
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        int c = 1;
        try {
	        while(input.hasRemaining()) {
	        	System.out.println("xhtml test1 = "+c);
	        	StringWriter writer = new StringWriter(1024);
			    XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
			    xmlWriter.writeStartDocument();
			    xmlWriter.writeStartElement("body");
			    
		        parser.parseLine(input);
		        xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
	        	
	        	xmlWriter.writeEndElement();
	        	xmlWriter.writeEndDocument();
	        	
	        	String result = writer.toString();
	        	
	        	parser.printDebug();
	        	System.out.append("Result:\n").append(result).append("\n\n")
	        	.println();
	        	//each line asserts 
	        	switch(c) {
	        	case 1:
	        		assertEquals("<?xml version=\"1.0\" ?><body>This is some text. <b>Bold text</b> and <i>italic text</i>.\n</body>", result);
	        		break;
	        	case 2:
	        		assertEquals("<?xml version=\"1.0\" ?><body><u>Underlined text</u> with <code>inline code</code>.\n</body>", result);
	        		break;
	        	case 3:
	        		assertEquals("<?xml version=\"1.0\" ?><body>Here is a <a href=\"http://example.com\">link</a> and an image <img alt=\"alt text\" src=\"http://image.com/img.jpg\"></img> for example\n</body>", result);
	        		break;
	        	case 4:
	        		assertEquals("<?xml version=\"1.0\" ?><body>Some markers (bold, italic, underline) do not allow a space right followed the begin marker, eg. ** bold ** * italic* __ underline__ but <code> name</code> is ok.\n</body>", result);
	        		break;
	        	case 5:
	        		assertEquals("<?xml version=\"1.0\" ?><body>Bold is not like this: ** it would not bold** neither after.\n</body>", result);
	        		break;
	        	case 6:
	        		assertEquals("<?xml version=\"1.0\" ?><body>Good bye\n</body>", result);
	        		break;
	        	}
	        	c++;
	        	
	        }
        } catch (XMLStreamException e) {
			fail(e.toString());
		}
	}
	
	@Test
	void test2() {
		String inputText = "Hello. **Bold text contains *italic*** but *italic text can not contain **bold text***.\n" +
                "Good bye\n";
		System.out.println("----test2-----\n"+inputText+"\n----------");
        CharBuffer input = CharBuffer.wrap(inputText);
        SMDTextLineParser parser = new SMDTextLineParser(32);
        StringBuilder sb = new StringBuilder(256);
        SMDHtmlRender render = new HtmlRenderImpl();
        int c = 1;
        while(input.hasRemaining()) {
        	System.out.println("test2 = "+c);
        	//parse input to sb, that is appending the result
        	parser.parseLine(input);
        	parser.printDebug();
        	render.produceHtml(parser.markers(), input, sb);
        	System.out.append("Result:\n").append(sb.toString()).append("\n\n")
        	.println();
        	//each line asserts 
        	switch(c) {
        	case 1:
        		assertEquals("Hello. <b>Bold text contains <i>italic</i></b> but <i>italic text can not contain </i><i>bold text</i><b>.</b>", sb.toString());
        		break;
        	case 2:
        		assertEquals("Good bye\n", sb.toString());
        		break;
        	}
        	sb.setLength(0);
        	
        	c++;
        }
	}

	@Test
	void test3() {
	    String inputText = "**Nested *italic* in bold** and __underline__.\n" +
	            "`Inline code with * and __`\n" +
	            "![image alt](http://example.com/image.png) and [link with *italic*](http://example.com).\n";
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining() && c < 4) {
	        System.out.println("test3 " + c);
	        parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
	        System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	        switch (c) {
	            case 1:
	                assertEquals("<b>Nested <i>italic</i> in bold</b> and <u>underline</u>.\n", sb.toString());
	                break;
	            case 2:
	            	parser.printDebug();
	                assertEquals("<code>Inline code with * and __</code>\n", sb.toString());
	                break;
	            case 3:
	            	assertEquals("<img alt=\"image alt\" src=\"http://example.com/image.png\"> and <a href=\"http://example.com\">link with *italic*</a>.\n", sb.toString());
	                break;
	        }
	        sb.setLength(0);
	        parser.markers().resetMarkers();
	        c++;
	    }
	}
	
	@Test
	void test3a() {
	    String inputText = "**Nested *italic* in bold** and __underline__.\n" +
	            "`Inline code with * and __`\n" +
	            "![image alt](http://example.com/image.png) and [link with *italic*](http://example.com).\n";
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    SMDHtmlRender render = new HtmlRenderImpl("a", "b", "c", "d", "e", null, null, null, null);
	    
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining() && c < 4) {
	        System.out.println("test3 " + c);
	        parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
	        System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	        switch (c) {
	            case 1:
	                assertEquals("<b>Nested <i>italic</i> in bold</b> and <u>underline</u>.\n", sb.toString());
	                break;
	            case 2:
	            	parser.printDebug();
	                assertEquals("<code class=\"d\">Inline code with * and __</code>\n", sb.toString());
	                break;
	            case 3:
	            	assertEquals("<img class=\"c\" alt=\"image alt\" src=\"http://example.com/image.png\"> and <a class=\"b\" href=\"http://example.com\">link with *italic*</a>.\n", sb.toString());
	                break;
	        }
	        sb.setLength(0);
	        parser.markers().resetMarkers();
	        c++;
	    }
	}

	@Test
	void xhtmlTest3() {
	    String inputText = "**Nested *italic* in bold** and __underline__.\n" +
	            "`Inline code with * and __`.\n" +
	            "![image alt](http://example.com/image.png) and [link with *italic*](http://example.com).\n";
	    System.out.println("----test3-----\n" + inputText + "\n----------");
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    SMDXhtmlWriter xhtmlWriter = new XhtmlWriterImpl();
	    
	    XMLOutputFactory factory = XMLOutputFactory.newFactory();
        int c = 1;
        try {
	        while(input.hasRemaining()) {
	        	System.out.println("xhtml test3 = "+c);
	        	StringWriter writer = new StringWriter(1024);
			    XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
			    xmlWriter.writeStartDocument();
			    xmlWriter.writeStartElement("body");
			    
		        parser.parseLine(input);
		        xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
	        	
	        	xmlWriter.writeEndElement();
	        	xmlWriter.writeEndDocument();
	        	
	        	String result = writer.toString();
		    
	        	parser.printDebug();
		        System.out.append("Result:\n").append(result).append("\n\n");
		        switch (c) {
		            case 1:
		                assertEquals("<?xml version=\"1.0\" ?><body><b>Nested <i>italic</i> in bold</b> and <u>underline</u>.\n</body>", result);
		                break;
		            case 2:
		                assertEquals("<?xml version=\"1.0\" ?><body><code>Inline code with * and __</code>.\n</body>", result);
		                break;
		            case 3:
		            	assertEquals("<?xml version=\"1.0\" ?><body><img alt=\"image alt\" src=\"http://example.com/image.png\"></img> and <a href=\"http://example.com\">link with *italic*</a>.\n</body>", result);
		                break;
		        }
		        c++;
		        
		    }
		} catch (XMLStreamException e) {
			fail(e.toString());
		}
	}
	
	@Test
	void test4() {
	    String inputText = "Multiple paragraphs with different markers.\n" +
	            "\n" +
	            "*Italic* before **bold** and then __underline__.\r\n" +
	            "`Code` at the end.\n";
	    System.out.println("----test4-----\n" + inputText + "\n----------");
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining() && c < 5) {
	        System.out.println("test4 " + c);
	        int b = parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
	        switch (c) {
	            case 1:
	            	parser.printDebug();
	                assertEquals("Multiple paragraphs with different markers.\n", sb.toString());
	                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
	                break;
	            case 2:
	            	parser.printDebug();
	            	assertEquals(SMDLineParser.SMD_LINE_BLANK_OR_EMPTY, b);
	                //new line already consumed
	                break;
	            case 3:
	            	parser.printDebug();
	                assertEquals("<i>Italic</i> before <b>bold</b> and then <u>underline</u>.&#13;\n", sb.toString());
	                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
	                break;
	            case 4:
	            	parser.printDebug();
	                assertEquals("<code>Code</code> at the end.\n", sb.toString());
	                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
	                break;
	        }
	        System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	        sb.setLength(0);
	        
	        c++;
	    }
	}

	@Test
	void test5() {
	    String inputText = "Text with **bold** and *italic* and _mixed_ __underline__.\n" +
	            "Complex [link & data](https://example.com/a=b&c=d) formatting with nested *italic* inside link.\n" +
	            "![alt quote \"](https://example.com/img.png) followed by simple text.\n";
	    System.out.println("----test5-----\n" + inputText + "\n----------");
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining()) {
	        System.out.println("test5 " + c);
	        parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
	        System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	        switch (c) {
	            case 1:
	                assertEquals("Text with <b>bold</b> and <i>italic</i> and _mixed_ <u>underline</u>.\n", sb.toString());
	                break;
	            case 2:
	                assertEquals("Complex <a href=\"https://example.com/a=b&amp;c=d\">link &amp; data</a> formatting with nested <i>italic</i> inside link.\n", sb.toString());
	                break;
	            case 3:
	                assertEquals("<img alt=\"alt quote &quot;\" src=\"https://example.com/img.png\"> followed by simple text.\n", sb.toString());
	                break;
	        }
	        sb.setLength(0);
	        
	        c++;
	    }
	}
	
	@Test
	void test6() {
	    String inputText = "Link is able to empty text [](https://example.com/)\n";
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining()) {
	        System.out.println("test6 " + c);
	        int b = parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
        	parser.printDebug();
        	System.out.append("Result (for input length="+inputText.length()+"):\n").append(sb.toString()).append("\n\n");
        	switch (c) {
            case 1:
                assertEquals("Link is able to empty text <a href=\"https://example.com/\">https://example.com/</a>\n", sb.toString());
                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
                break;
        	}
        	c++;
        	sb.setLength(0);
        	
	    }
	}
	
	@Test
	void test7() {
	    String inputText = "!(https://example.com/img.png)\n";
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining() && c<10) {
	        System.out.println("test7 " + c);
	        int b = parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
        	parser.printDebug();
        	System.out.append("Input:\n"+inputText+"\nResult (for input length="+inputText.length()+"):\n").append(sb.toString()).append("\n\n");
        	switch (c) {
            case 1:
                assertEquals("<img src=\"https://example.com/img.png\">\n", sb.toString());
                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
                break;
        	}
        	c++;
        	sb.setLength(0);
        	
	    }
	}
	
	@Test
	void test8() {
	    String inputText = "* This is item with **bold** and *italic* and _mixed_ __underline__.\n";
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);
	    
	    int c = 1;
	    if (input.hasRemaining() && c < 3) {
	        System.out.println("test8 " + c);
	        int b = parser.parseLine(input);
        	assertEquals(0, input.position());
        	System.out.println("Position: "+input.position());
        	assertEquals(SMDLineParser.SMD_LINE_INVALID, b);
	    }
	}
	
	@Test
	void test9() {
	    String inputText = "Parsing ***overflow of output*** with **bold** and *italic* and _mixed_ __underline__ ![alt with \"some text\"](https://example.com/img.png?a=b&y=x) followed, **etc**... with `code`.\n"
	    		+ "Parsing ***overflow of <> output*** with enough room and _mixed_ __underline__ ![alt](https://example.com/img.png) followed, **etc**... with `code`.\n";
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining()) {
	        System.out.println("test9 " + c);
	        int b = parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
        	parser.printDebug();
        	System.out.append("Result (for input length="+inputText.length()+"):\n").append(sb.toString()).append("\n\n");
        	assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
        	c++;
        	sb.setLength(0);
        	//parser.markers().resetMarkers();
	    }
	}
	
	@Test
	void test10() {
	    String inputText = "Parsing ***overflow of output*** with **bold** and *italic* and _mixed_ __underline__ ![alt](https://example.com/img.png\n"
	    		+ "Parsing __Code in `the >` & underline__ with enough room and _mixed_ __underline__ ![alternative of the image\n";
	    CharBuffer input = CharBuffer.wrap(inputText.toCharArray());
	    SMDTextLineParser parser = new SMDTextLineParser(48);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining()) {
	        System.out.println("test10 " + c);
	        int b = parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
        	parser.printDebug();
        	System.out.append("Result (for input length="+inputText.length()+"):\n").append(sb.toString()).append("\n\n");
        	assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
        	c++;
        	sb.setLength(0);
        	
	    }
	}
	
	@Test
	void test11() {
	    String inputText = "Invalid link is ok as well [](http://invalid\n"+
	    			"Invalid link is ok [], just as it\n" +
	    		"Break line after [] is ok []\n";
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining() && c < 5) {
	        System.out.println("test11 " + c);
	        int b = parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
        	parser.printDebug();
        	System.out.append("Result:\n").append(sb.toString()).append("\n");
        	switch (c) {
            case 1:
            	assertEquals("Invalid link is ok as well <a href=\"http://invalid\">http://invalid</a>", sb.toString());
                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
                break;
            case 2:    
                assertEquals("Invalid link is ok [], just as it\n", sb.toString());
                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
                break;
            case 3:
                assertEquals("Break line after [] is ok []\n", sb.toString());
                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
                break;
        	}
        	c++;
        	
        	sb.setLength(0);
	    }
	}
	
	@Test
	void test12() {
	    String inputText = "Invalid image is ok ![], just as it\n" +
	    		"Break line after [] is ok ![]\n" +
	    		"Image to invalid source !(http://invalid/img\n";
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser(24);
	    SMDHtmlRender render = new HtmlRenderImpl();
	    StringBuilder sb = new StringBuilder(256);
	    int c = 1;
	    while (input.hasRemaining() && c < 5) {
	        System.out.println("test11 " + c);
	        int b = parser.parseLine(input);
	        render.produceHtml(parser.markers(), input, sb);
        	parser.printDebug();
        	System.out.append("Result:\n").append(sb.toString()).append("\n");
        	switch (c) {
            case 1:
                assertEquals("Invalid image is ok ![], just as it\n", sb.toString());
                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
                break;
            case 2:
                assertEquals("Break line after [] is ok ![]\n", sb.toString());
                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
                break;
            case 3:
                assertEquals("Image to invalid source <img src=\"http://invalid/img\">", sb.toString());
                assertEquals(SMDLineParser.SMD_LINE_PARSED, b);
                break;
        	}
        	c++;
        	sb.setLength(0);
        	
	    }
	}
	
	@Test
	void test13() {
		String inputText = "This is some text. ~~deleted text~~\n" +
                "~~__Underlined text__~~ with `~~inline code~~`.\n" +
                "Good bye\n";
		System.out.println("----test13-----\n"+inputText+"\n----------");
        CharBuffer input = CharBuffer.wrap(inputText);
        SMDTextLineParser parser = new SMDTextLineParser();
        
        StringBuilder sb = new StringBuilder(256);
        SMDHtmlRender render = new HtmlRenderImpl();
        int c = 1;
        while(input.hasRemaining()) {
        	System.out.println("test13 = "+c);
        	//parse input to sb, that is appending the result
        	parser.parseLine(input);
        	render.produceHtml(parser.markers(), input, sb);
        	parser.printDebug();
        	System.out.append("Result:\n").append(sb.toString()).append("\n\n")
        	.println();
        	//each line asserts 
        	switch(c) {
        	case 1:
        		assertEquals("This is some text. <s>deleted text</s>\n", sb.toString());
        		break;
        	case 2:
        		assertEquals("<s><u>Underlined text</u></s> with <code>~~inline code~~</code>.\n", sb.toString());
        		break;
        	case 3:
        		assertEquals("Good bye\n", sb.toString());
        		break;
        	}
        	sb.setLength(0);
        	c++;
        }
	}

	@Test
	void bufferOutTest13() {
		String inputText = "This is some text. ~~deleted text~~\n" +
                "~~__Underlined text__~~ with `~~inline code~~`.\n" +
                "Good bye\n";
		System.out.println("----test13-----\n"+inputText+"\n----------");
        CharBuffer input = CharBuffer.wrap(inputText);
        SMDTextLineParser parser = new SMDTextLineParser();
        CharBuffer output = CharBuffer.allocate(1024);
        SMDHtmlWriter writer = new HtmlWriterImpl();
        
        int r = 0;
        int c = 0;
        while(input.hasRemaining() && r != SMDLineParser.SMD_LINE_BLANK_OR_EMPTY && r != SMDLineParser.SMD_LINE_INVALID) {
        	//parse input to sb, that is appending the result
        	c++;
        	r = parser.parseLine(input);
        	writer.appendHtml(parser.markers(), input, output);
        	parser.printDebug();
        	output.flip();
        	String result = output.toString();
        	output.clear();
        	System.out.append("Result:\n").append(result).println();
        	//each line asserts 
        	switch(c) {
        	case 1:
        		assertEquals("This is some text. <s>deleted text</s>\n", result);
        		break;
        	case 2:
        		assertEquals("<s><u>Underlined text</u></s> with <code>~~inline code~~</code>.\n", result);
        		break;
        	case 3:
        		assertEquals("Good bye\n", result);
        		break;
        	}
        	
        	
        }
	}

	@Test
	void xhtmlTest13() {
		String inputText = "This is some text. ~~deleted text~~\n" +
                "~~__Underlined text__~~ with `~~inline code~~`.\n" +
                "Good bye\n";
		System.out.println("----test1-----\n"+inputText+"\n----------");
        CharBuffer input = CharBuffer.wrap(inputText);
        SMDTextLineParser parser = new SMDTextLineParser(64);
        SMDXhtmlWriter xhtmlWriter = new XhtmlWriterImpl();
        
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        int c = 1;
        try {
	        while(input.hasRemaining()) {
	        	System.out.println("xhtml test1 = "+c);
	        	StringWriter writer = new StringWriter(1024);
			    XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
			    xmlWriter.writeStartDocument();
			    xmlWriter.writeStartElement("body");
			    
		        parser.parseLine(input);
		        xhtmlWriter.writeXhtml(parser.markers(), input, xmlWriter);
	        	
	        	xmlWriter.writeEndElement();
	        	xmlWriter.writeEndDocument();
	        	
	        	String result = writer.toString();
	        	
	        	parser.printDebug();
	        	System.out.append("Result:\n").append(result).append("\n\n")
	        	.println();
	        	//each line asserts 
	        	switch(c) {
	        	case 1:
	        		assertEquals("<?xml version=\"1.0\" ?><body>This is some text. <s>deleted text</s>\n</body>", result);
	        		break;
	        	case 2:
	        		assertEquals("<?xml version=\"1.0\" ?><body><s><u>Underlined text</u></s> with <code>~~inline code~~</code>.\n</body>", result);
	        		break;
	        	case 3:
	        		assertEquals("<?xml version=\"1.0\" ?><body>Good bye\n</body>", result);
	        		break;
	        	}
	        	c++;
	        	
	        }
        } catch (XMLStreamException e) {
			fail(e.toString());
		}
	}
	
	@Test
	void test14() {
		String inputText = "Test\\\n"
				+ "any\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    System.out.append("Result 1:\n").append(parser.markers().toString()).append("\n");
	    
	    int[] expectedState = { SMDParser.STATE_TEXT, SMDParser.STATE_NEW_LINE, SMDParser.STATE_NEW_LINE, SMDParser.STATE_TEXT,
	    		SMDParser.STATE_TEXT, SMDParser.STATE_TEXT};
	    
	    assertArrayEquals(Arrays.copyOf(expectedState, expectedState.length - 2), parser.markers().toStateArray());
	    
	    r = parser.parseNext(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    
	    System.out.append("Result 2:\n").append(parser.markers().toString()).append("\n");
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	    		
	}
	
	@Test
	void test15() {
		String inputText = "Test\\ a char\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    int[] expectedState = { SMDParser.STATE_TEXT, SMDParser.STATE_TEXT};
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	    
	}
	
	@Test
	void test16() {
		String inputText = "Hello: `\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    int[] expectedState = { SMDParser.STATE_TEXT, SMDParser.STATE_TEXT};
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	    
	}
	
	@Test
	void test17() {
		String inputText = "Is \\*not emphasized*\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    
	    int[] expectedState = { SMDParser.STATE_TEXT, SMDParser.STATE_NONE, SMDParser.STATE_NONE, SMDParser.STATE_TEXT};
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	    
	    SMDHtmlRender render = new HtmlRenderImpl();
	    StringBuilder sb = new StringBuilder();
	    render.produceHtml(parser.markers(), input, sb);
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    assertEquals("Is *not emphasized*\n", sb.toString());
	}
	
	@Test
	void test17a() {
		String inputText = "\\[not a link](/foo)\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    System.out.append("Markers:\n").append(parser.markers().toString()).append("\n");
	    
	    int[] expectedState = { SMDParser.STATE_NONE, SMDParser.STATE_NONE, SMDParser.STATE_TEXT, SMDParser.STATE_TEXT};
	    
	    assertArrayEquals(expectedState, parser.markers().toStateArray());
	    
	    SMDHtmlRender render = new HtmlRenderImpl();
	    StringBuilder sb = new StringBuilder();
	    render.produceHtml(parser.markers(), input, sb);
	    System.out.append("Result:\n").append(sb.toString()).append("\n\n");
	    
	    assertEquals("[not a link](/foo)\n", sb.toString());
	    
	}
	
	@Test
	void test18() {
		String inputText = "[foo]: /url1\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDLineParser.SMD_LINE_PARSED, r);
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    //assertEquals(0, parser.markers().markedLength());
	    
	}

	@Test
	void test19() {
		String inputText = "``` markdown\n";
	    
	    CharBuffer input = CharBuffer.wrap(inputText);
	    SMDTextLineParser parser = new SMDTextLineParser();
	    
	    int r = parser.parseNext(input);
	    assertEquals(SMDLineParser.SMD_LINE_INVALID, r);
	    
	    System.out.append("Result:\n").append(parser.markers().toString()).append("\n");
	    
	    assertEquals(0, parser.markers().markedLength());
	    
	}
	
	
}
