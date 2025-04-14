package vietfi.markdown.strict;

import java.util.function.BiFunction;

/**
 * Render base interface
 */
public interface SMDRender {

	public static final Integer IMG_LINK_WITH_TILTE = 1;
	public static final Integer IMG_LINK = 2;
	public static final Integer NORMAL_LINK_WITH_TILTE = 3;
	public static final Integer NORMAL_LINK_WITHOUT_TILTE = 4;
	public static final Integer AUTO_LINK = 5;
	public static final Integer WIKI_LINK = 6;
	public static final Integer HASH_LINK = 7;

	static final int MAX_FUNCTION_COUNT = HASH_LINK + 1;
	
	/**
	 * Set the link resolver.
	 * The resolver form is: method(type, inputString) return outputString (or null if not modified).
	 * 
	 * @param resolver function to call
	 */
	void setLinkURLResolver(BiFunction<Integer, String, String> resolver);

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
