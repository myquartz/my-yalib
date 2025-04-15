package vietfi.markdown.strict.render;

import vietfi.markdown.strict.SMDRender;
import vietfi.markdown.strict.line.HtmlEscapeUtil;

public abstract class HtmlBaseTagRender implements SMDRender {


	public static final String ATTR_CLASS_BEGIN = " class=\"";
	public static final String ATTR_CLASS_END = "\"";
	public static final String TAG_BEGIN_GT = ">";
	
	public static final String TAG_STRIKE_BEGIN = "<s>";
    public static final String TAG_STRIKE_END = "</s>";
	public static final String TAG_BOLD_BEGIN = "<b>";
    public static final String TAG_BOLD_END = "</b>";
    public static final String TAG_ITALIC_BEGIN = "<i>";
    public static final String TAG_ITALIC_END = "</i>";
    public static final String TAG_UNDERLINE_BEGIN = "<u>";
    public static final String TAG_UNDERLINE_END = "</u>";
    public static final String TAG_CODE_BEGIN = "<code";
    public static final String TAG_CODE_END = "</code>";
    
    public static final String TAG_A_BEGIN = "<a";
    public static final String TAG_A_URL_BEGIN = " href=\"";
    public static final String TAG_A_URL_END = "\"";
    public static final String TAG_A_TEXT_BEGIN = ">";
    //public static final String TAG_A_TEXT_END = "";
    public static final String TAG_A_END = "</a>";
    
    public static final String TAG_NEW_LINE = "<br>";
    public static final String ESCAPE_QUOTE = "\"";
    
    public static final String TAG_IMG_BEGIN = "<img";
    public static final String TAG_IMG_END = ">";
    public static final String TAG_IMG_TEXT_BEGIN = " alt=\"";
    public static final String TAG_IMG_TEXT_END = ESCAPE_QUOTE;
    public static final String TAG_IMG_URL_BEGIN = " src=\"";
    public static final String TAG_IMG_URL_END = ESCAPE_QUOTE;
    
    public static final String PRE_TAG = "<pre";
    public static final String PRE_STD = "><code>";
    public static final String PRE_WITH_LANGUAGUE = "><code class=\"language-";
    public static final String PRE_WITH_LANGUAGUE_POSTFIX = "\">";
    public static final String PRE_POSTFIX = "</code></pre>\n";
    
    public static final String BLOCKQUOTE_BEGIN = "<blockquote";
    public static final String BLOCKQUOTE_END = "</blockquote>\n";
    public static final String PARA_BEGIN = "<p";
    public static final String PARA_END = "</p>";
    
    public static final String UL_BEGIN = "<ul";
	public static final String UL_END = "</ul>\n";
	public static final String OL_BEGIN = "<ol";
	public static final String OL_END = "</ol>\n";
	
	public static final String LI_BEGIN = "<li";
	public static final String LI_END = "</li>\n";
	
	public static final String[] HEADINGS_BEGIN = {"<h1>","<h2>","<h3>","<h4>","<h5>","<h6>",};
	public static final String[] HEADINGS_END = {"</h1>\n","</h2>\n","</h3>\n","</h4>\n","</h5>\n","</h6>\n",};
	
	public final static String HR = "<hr>\n";
	public final static String HR_DOUBLE = "<hr class=\"double-line\">\n";
	public final static String HR_UNDERSCORE = "<hr class=\"underscore-line\">\n";

	protected String pClass;
	protected String linkClass;
	protected String imgClass;
	protected String codeClass;
	protected String preCodeClass;
	protected String blockquoteClass;
	protected String ulClass;
	protected String olClass;
	protected String liClass;

	@Override
	public void setClassNameForTag(String className, int classForTag) {
		if(className == null || className.isBlank())
			className = null;
		
		if(className != null) {
			StringBuilder sb = new StringBuilder();
			boolean isEscaped = false;
			for(int i = 0; i < className.length(); i++) {
				char c = className.charAt(i);
				//Using HtmlEscapeUtil to escape the class name
				String esc = HtmlEscapeUtil.escapeHtml(c, true);
				if(esc != null) {
					sb.append(esc);
					isEscaped = true;
				}
				else {
					sb.append(c);
				}
			}
			
			if(isEscaped) {
				className = sb.toString();
			}
			
			//else discard the class name string builder
		}
		
		switch(classForTag) {
		case CLASS_FOR_PARAGRAPH:
			this.pClass = className;
			break;
		case CLASS_FOR_LINK:
			this.linkClass = className;
			break;
		case CLASS_FOR_IMG:
			this.imgClass = className;
			break;
		case CLASS_FOR_INLINE_CODE:
			this.codeClass = className;
			break;
		case CLASS_FOR_PRE_CODE:
			this.preCodeClass = className;
			break;
		case CLASS_FOR_BLOCKQUOTE:
			this.blockquoteClass = className;
			break;
		case CLASS_FOR_UL:
			this.ulClass = className;
			break;
		case CLASS_FOR_OL:
			this.olClass = className;
			break;
		case CLASS_FOR_LI:
			this.liClass = className;
			break;
		};
	}

}
