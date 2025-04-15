package vietfi.markdown.sample.web_demo;

/**
 * DTO (JSON) 
 * for render configuration
 */

public class RenderConfigSetup {
	
	/**
	 * Class for tag type
	 * @see SMDRender.CLASS_FOR_*
	 */
	private int classForTag;
	private String type;
	private String value;
	
	
	public int getClassForTag() {
		return classForTag;
	}
	public void setClassForTag(int classForTag) {
		this.classForTag = classForTag;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "RenderConfigSetup [classForTag=" + classForTag + ", type=" + type + ", value=" + value + "]";
	}
	
	
}
