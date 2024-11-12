package vietfi.markdown.sample.web_demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    CharBuffer buffer = CharBuffer.allocate(4*1024);
    
    @PostMapping(value = "/markdown-render", consumes = "text/plain", produces = "application/json; charset=UTF-8")
    public RenderOutputDto renderMarkdown(InputStream input) {
    	buffer.clear();
    	markdownParser.markers().resetMarkers();
    	sb.setLength(0);
    	debug.setLength(0);
    	
    	try (InputStreamReader isr = new InputStreamReader(input)) {
	    	int r = 0;
	    	int nRead = 0;
	    	int loop = 0;
	    	while(nRead >= 0) {	    	
	    		nRead = isr.read(buffer);
	    		loop++;
	    		if(nRead < 0)
	    			//force end
	    	    	markdownParser.endBlock(buffer.position());
	    		else {
			    	buffer.flip();
			    	r = markdownParser.parseNext(buffer);
			    	if(r == SMDParser.SMD_BLOCK_INVALID) {
			    		return new RenderOutputDto(buffer.toString(), "Invalid Markdown, result="+r);
			    	}
			    	debug.append("Loop "+loop+" "+markdownParser.markers().toString()).append('\n');
			    	htmlRenderer.produceHtml(markdownParser.markers(), buffer, sb);
			    	int pos = markdownParser.markers().compactMarkers(buffer.position());
			    	markdownParser.compact(pos);
			    	if(pos < buffer.position())
			    		buffer.position(pos);
			    	buffer.compact();
	    		}
		    }
    	} catch (IOException e) {
    		//ignore
		}
    	
    	debug.append(markdownParser.markers().toString()).append('\n');
    	htmlRenderer.produceHtml(markdownParser.markers(), buffer, sb);
    	
    	return new RenderOutputDto("", sb.toString(), debug.toString());
    }
	
}
