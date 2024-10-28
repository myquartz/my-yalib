package vietfi.markdown.strict.line;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.CharBuffer;

class HtmlEscapeUtilTest {

    @Test
    void testEscapeLessThan() {
        assertEquals("&lt;", HtmlEscapeUtil.escapeHtml('<', false));
        assertEquals("&lt;", HtmlEscapeUtil.escapeHtml('<', true));
    }

    @Test
    void testEscapeGreaterThan() {
        assertEquals("&gt;", HtmlEscapeUtil.escapeHtml('>', false));
        assertEquals("&gt;", HtmlEscapeUtil.escapeHtml('>', true));
    }

    @Test
    void testEscapeAmpersand() {
        assertEquals("&amp;", HtmlEscapeUtil.escapeHtml('&', false));
        assertEquals("&amp;", HtmlEscapeUtil.escapeHtml('&', true));
    }

    @Test
    void testEscapeNullChar() {
        assertEquals("&#xFFFD;", HtmlEscapeUtil.escapeHtml('\0', false));
        assertEquals("&#xFFFD;", HtmlEscapeUtil.escapeHtml('\0', true));
    }

    @Test
    void testEscapeDoubleQuoteSafeQuote() {
        assertEquals("&quot;", HtmlEscapeUtil.escapeHtml('"', true));
    }

    @Test
    void testEscapeDoubleQuoteWithoutSafeQuote() {
        assertNull(HtmlEscapeUtil.escapeHtml('"', false));
    }

    @Test
    void testEscapeSingleQuoteSafeQuote() {
        assertEquals("&apos;", HtmlEscapeUtil.escapeHtml('\'', true));
    }

    @Test
    void testEscapeSingleQuoteWithoutSafeQuote() {
        assertNull(HtmlEscapeUtil.escapeHtml('\'', false));
    }

    @Test
    void testEscapeNewLineSafeQuote() {
        assertEquals("&#10;", HtmlEscapeUtil.escapeHtml('\n', true));
    }

    @Test
    void testEscapeNewLineWithoutSafeQuote() {
        assertNull(HtmlEscapeUtil.escapeHtml('\n', false));
    }

    @Test
    void testEscapeCarriageReturnSafeQuote() {
        assertEquals("&#13;", HtmlEscapeUtil.escapeHtml('\r', true));
    }

    @Test
    void testEscapeCarriageReturnWithoutSafeQuote() {
        assertEquals("&#13;", HtmlEscapeUtil.escapeHtml('\r', false));
    }

    @Test
    void testNoEscapeNeeded() {
        // Test a character that does not need escaping
        assertNull(HtmlEscapeUtil.escapeHtml('A', true));
        assertNull(HtmlEscapeUtil.escapeHtml('Z', false));
    }
    
    @Test
    void testValidWriteWithEscape() {
        CharBuffer input = CharBuffer.wrap("Hello <World>!");
        CharBuffer output = CharBuffer.allocate(50);

        int lastPos = HtmlEscapeUtil.writeWithEscapeHtml(
                true, input, 0, input.length(), 5, output);

        // Check that the output is escaped correctly
        assertEquals("Hello &lt;World&gt;!", output.flip().toString());
        // Verify the last position is as expected (after the last char printed)
        assertEquals(input.length(), lastPos);
    }

    @Test
    void testValidWriteWithEscapeChars() {
        char[] input = "Hello <World>!".toCharArray();
        CharBuffer output = CharBuffer.allocate(50);

        int lastPos = HtmlEscapeUtil.writeWithEscapeHtml(
                true, input, 0, input.length, 5, output);

        // Check that the output is escaped correctly
        assertEquals("Hello &lt;World&gt;!", output.flip().toString());
        // Verify the last position is as expected (after the last char printed)
        assertEquals(input.length, lastPos);
    }
    
    @Test
    void testOutputBufferFull() {
        CharBuffer input = CharBuffer.wrap("Hello <World>!");
        CharBuffer output = CharBuffer.allocate(10);  // Not enough space for full output

        int lastPos = HtmlEscapeUtil.writeWithEscapeHtml(
                true, input, 0, input.length(), 0, output);

        // Check that the output stopped when the buffer became full
        assertEquals("Hello &lt;", output.flip().toString());
        // Verify that the last position corresponds to the point where the output stopped
        assertEquals(7, lastPos); // Stops at '<'
    }

    @Test
    void testOutputBufferFullChars() {
        char[] input = "Hello <World>!".toCharArray();
        CharBuffer output = CharBuffer.allocate(10);  // Not enough space for full output

        int lastPos = HtmlEscapeUtil.writeWithEscapeHtml(
                true, input, 0, input.length, 0, output);

        // Check that the output stopped when the buffer became full
        assertEquals("Hello &lt;", output.flip().toString());
        // Verify that the last position corresponds to the point where the output stopped
        assertEquals(7, lastPos); // Stops at '<'
    }
    
    @Test
    void testInvalidBeginEndThrowsException() {
        CharBuffer input = CharBuffer.wrap("Test");
        CharBuffer output = CharBuffer.allocate(10);

        // Test invalid range (begin > upTo)
        assertThrows(IllegalArgumentException.class, () -> {
            HtmlEscapeUtil.writeWithEscapeHtml(true, input, 5, 10, 0, output);
        });
    }

    @Test
    void testNoEscapingNeeded() {
        CharBuffer input = CharBuffer.wrap("Hello World");
        CharBuffer output = CharBuffer.allocate(20);

        int lastPos = HtmlEscapeUtil.writeWithEscapeHtml(
                true, input, 0, input.length(), 0, output);

        // Check that the output matches the input exactly
        assertEquals("Hello World", output.flip().toString());
        // Verify that the last position is the length of the input buffer
        assertEquals(input.length(), lastPos);
    }

    @Test
    void testReservedSpaceHandling() {
        CharBuffer input = CharBuffer.wrap("Hello <World>");
        CharBuffer output = CharBuffer.allocate(15);  // Smaller buffer with reserved space

        int lastPos = HtmlEscapeUtil.writeWithEscapeHtml(
                true, input, 0, input.length(), 3, output);

        // Check that the output stops at the reserved limit
        assertEquals("Hello &lt;Wo", output.flip().toString());
        // Verify that the last position is as expected
        assertEquals(9, lastPos);  // Stops after 'o' because of reserved space
        
    }

    @Test
    void testReservedSpaceHandlingChars() {
        char[] input = "Hello <World>".toCharArray();
        CharBuffer output = CharBuffer.allocate(15);  // Smaller buffer with reserved space

        int lastPos = HtmlEscapeUtil.writeWithEscapeHtml(
                true, input, 0, input.length, 3, output);

        // Check that the output stops at the reserved limit
        assertEquals("Hello &lt;Wo", output.flip().toString());
        // Verify that the last position is as expected
        assertEquals(9, lastPos);  // Stops after 'o' because of reserved space
        
    }
}
