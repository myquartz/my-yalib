package vietfi.markdown.sample.web_demo;

public class RenderOutputDto {

	private final String markdownContent;
	private final String htmlContent;
	private final String debugContent;
	
	public RenderOutputDto(String markdownContent, String debugContent) {
		this(markdownContent, null, debugContent);
	}
	
	public RenderOutputDto(String markdownContent, String htmlContent, String debugContent) {
		super();
		this.markdownContent = markdownContent;
		this.htmlContent = htmlContent;
		this.debugContent = debugContent;
	}
	
	public String getMarkdownContent() {
		return markdownContent;
	}
	public String getHtmlContent() {
		return htmlContent;
	}
	public String getDebugContent() {
		return debugContent;
	}
	
	
}
