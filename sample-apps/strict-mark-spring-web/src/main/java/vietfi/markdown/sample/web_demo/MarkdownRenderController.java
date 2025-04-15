package vietfi.markdown.sample.web_demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import vietfi.markdown.strict.SMDHtmlRender;
import vietfi.markdown.strict.SMDParser;
import vietfi.markdown.strict.SMDParserChain;
import vietfi.markdown.strict.render.HtmlRenderImpl;

@RestController
@RequestMapping("/api")
public class MarkdownRenderController {
	static final Logger logger = Logger.getLogger(MarkdownRenderController.class.getName());
	
	Semaphore lock = new Semaphore(1);
	
    private final SMDParser markdownParser = SMDParserChain.createParserOfStandard(4096);
    private final SMDHtmlRender htmlRenderer = new HtmlRenderImpl();
    StringBuilder sb = new StringBuilder(32*1024);
    StringBuilder debug = new StringBuilder(32*1024);
    CharBuffer buffer = CharBuffer.allocate(4*1024);
    
    @PostMapping(value = "/change-render-class", consumes = MediaType.APPLICATION_JSON_VALUE, 
    		produces = "application/json; charset=UTF-8")
    public String changeRenderClass(@RequestBody RenderConfigSetup input) {
    	try {
			lock.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    	try {
    		logger.info("Change render class as "+input.toString());
    		htmlRenderer.setClassNameForTag(input.getValue(), input.getClassForTag());
    		return "OK render class: "+input;
    	}
    	finally {
    		lock.release();
    	}
    }
    
    @PostMapping(value = "/change-url-resolver", consumes = MediaType.APPLICATION_JSON_VALUE, 
    		produces = "application/json; charset=UTF-8")
    public String changeBaseImage(@RequestBody RenderConfigSetup input) {
    	try {
			lock.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    	try {
    		final String val = input.getValue(); 
    		
    		logger.info("Change resolver as "+input.toString());
    		if("img".equals(input.getType()))
    			htmlRenderer.setImageSrcResolver((buff, sb) -> {
    				char firstChar = buff.get();
    				if(firstChar == '/' && val.endsWith("/")) { //double /, trim 1
    					sb.append(val);
    					return 1;
    				}
    				else {
    					sb.append(val);
    					if(!(firstChar == '/' || val.endsWith("/"))) {
    						sb.append('/');
    					}
    					
    				}
    				
    				return null;
    			});
    		else {
    			return ("type must be \"img\"");
    		}
    		return "OK resolver: "+input;
    	}
    	finally {
    		lock.release();
    	}
    }
    
    @PostMapping(value = "/markdown-render", consumes = "text/plain", produces = "application/json; charset=UTF-8")
    public RenderOutputDto renderMarkdown(InputStream input) {
    	try {
			lock.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    	try {
	    	buffer.clear();
	    	markdownParser.markers().resetMarkers();
	    	sb.setLength(0);
	    	debug.setLength(0);
	    	
	    	try (InputStreamReader isr = new InputStreamReader(input)) {
		    	int r = 0;
		    	int nRead = 0;
		    	int loop = 0;
		    	int pos = 0;
		    	while(nRead >= 0) {	    	
		    		nRead = isr.read(buffer);
		    		loop++;
			    	buffer.flip();
			    	r = markdownParser.parseNext(buffer);
			    	if(r == SMDParser.SMD_BLOCK_INVALID) {
			    		return new RenderOutputDto(buffer.toString(), "Loop "+loop+", invalid Markdown, result="+r);
			    	}
			    	debug.append("Loop "+loop+", nRead="+nRead+", "+markdownParser.markers().toString());
			    	htmlRenderer.produceHtml(markdownParser.markers(), buffer, sb);
			    	pos = markdownParser.markers().compactMarkers(buffer.position());
			    	debug.append("Compacted "+pos+", markers length="+markdownParser.markers().markedLength()).append('\n');
			    	markdownParser.compact(pos);
			    	if(pos < buffer.position()) {
			    		debug.append("WARN: compact "+pos+" less than "+buffer.position()).append('\n');
			    		buffer.position(pos);
			    	}
			    	buffer.compact();
		    		
		    		if(nRead < 0) {
		    			if(buffer.hasRemaining() && buffer.position() > 0) //force ending
		    				buffer.append('\u001C');
		    			
		    			buffer.flip();
		    			r = markdownParser.parseNext(buffer);
		    			//force end
		    	    	markdownParser.endBlock(buffer.position());
		    	    	debug.append("Loop "+loop+", end, compact "+pos+", ending position "+buffer.position()).append('\n');
		    		}
			    }
	    	} catch (IOException e) {
	    		//ignore
	    		logger.info(e.toString());
			}
	    	
	    	debug.append("Last, ").append(markdownParser.markers().toString()).append('\n');
	    	htmlRenderer.produceHtml(markdownParser.markers(), buffer, sb);
	    	
	    	return new RenderOutputDto("", sb.toString(), debug.toString());
    	}
    	finally {
    		lock.release();
    	}
    }
	
}
