package vietfi.markdown.sample.web_demo;

import java.nio.CharBuffer;

import org.springframework.web.bind.annotation.*;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.SMDParserChain;
import vietfi.markdown.strict.render.HtmlRenderImpl;

@RestController
@RequestMapping("/api")
public class MarkdownRenderController {

    private final SMDParser markdownParser = SMDParserChain.createParserOfStandard(100*4096);
    private final SMDHtmlRender htmlRenderer = new HtmlRenderImpl();
    StringBuilder sb = new StringBuilder(48*1024);
    StringBuilder debug = new StringBuilder(48*1024);
    CharBuffer buffer = CharBuffer.allocate(32*1024);
    
    @PostMapping(value = "/markdown-render", produces = "application/json; charset=UTF-8")
    public RenderOutputDto renderMarkdown(@RequestParam("markdown_input") String markdownInput) {
    	buffer.clear();
    	buffer.append(markdownInput);
    	//force end
    	if(!markdownInput.trim().endsWith("\n")) {
    		buffer.append('\u001C');
    	}
    	buffer.flip();
    	markdownParser.markers().resetMarkers();
    	sb.setLength(0);
    	int r = markdownParser.parseNext(buffer);
    	if(r == SMDParser.SMD_BLOCK_INVALID) {
    		return new RenderOutputDto(markdownInput, "Invalid Markdown, result="+r);
    	}
    	
    	debug.setLength(0);
    	if(r == SMDParser.SMD_VOID) {
    		debug.append(markdownParser.markers().toString()).append('\n');
    		htmlRenderer.produceHtml(markdownParser.markers(), buffer, sb);
    		markdownParser.compact(buffer.position());
    		buffer.compact();
    		buffer.append("\n");
    		buffer.flip();
    		r = markdownParser.parseNext(buffer);
    	}
    	
    	markdownParser.endBlock(buffer.position());
    	
    	debug.append(markdownParser.markers().toString()).append('\n');
    	htmlRenderer.produceHtml(markdownParser.markers(), buffer, sb);
    	
    	return new RenderOutputDto(markdownInput, sb.toString(), debug.toString());
    }
	
}
