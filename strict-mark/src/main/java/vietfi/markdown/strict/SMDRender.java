package vietfi.markdown.strict;

/**
 * Render base interface
 */
public interface SMDRender {


	public static final int CLASS_FOR_PARAGRAPH = 1;
	public static final int CLASS_FOR_LINK = 2;
	public static final int CLASS_FOR_IMG = 3;
	public static final int CLASS_FOR_INLINE_CODE = 4;
	public static final int CLASS_FOR_PRE_CODE = 5;
	public static final int CLASS_FOR_BLOCKQUOTE = 6;
	public static final int CLASS_FOR_UL = 7;
	public static final int CLASS_FOR_OL = 8;
	public static final int CLASS_FOR_LI = 9;
	
	/**
	 * Set the class for element when rendering.
	 * 
	 * @param name class name, will be print as is (not escape). Null for clear the setting
	 * @param classForTag tag type (see CLASS_FOR_* constant)
	 * 
	 */
	void setClassNameForTag(String name, int classForTag);
	
}
