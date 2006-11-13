package com.zutubi.pulse.committransformers;

import junit.framework.TestCase;

/**
 * <class comment/>
 */
public class CommitMessageBuilderTest extends TestCase
{
    public void testReplaceLiteral()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("The quick brown fox jumps over the lazy dog.");
        assertEquals("The QUICK brown fox jumps over the lazy dog.", builder.replace("quick", "QUICK"));
    }

    public void testReplaceWithGrouping()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("The quick brown fox jumps over the lazy dog.");
        assertEquals("The quick <b>brown fox jumps</b> over the lazy dog.", builder.replace("brown fox jumps", "<b>$0</b>"));
    }

    public void testEncodeAfterHtmlReplacement()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("A <i>very</i> bold statement.");
        assertEquals("A <i>very</i> <b>bold</b> statement.", builder.replace("bold", "<b>$0</b>"));
        assertEquals("A &lt;i&gt;very&lt;/i&gt; <b>bold</b> statement.", builder.encode());
// Upgrade to oscore 2.2.5 which contains a encodeHtml method that does not re-encode encoded html.        
//        assertEquals("A &lt;i&gt;very&lt;/i&gt; <b>bold</b> statement.", builder.encode());
    }

    public void testTrimUnmodifiedBlock()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a String");
        assertEquals("This is a String", buffer.trim(50));
        assertEquals("This is a String", buffer.trim(16));
        assertEquals("This is...", buffer.trim(10));
        assertEquals("...", buffer.trim(3));
        assertEquals("..", buffer.trim(2));
    }

    public void testTrimModifiedBlock()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a string");
        assertEquals("<b>This is a string</b>", buffer.replace("This is a string", "<b>This is a string</b>"));
        assertEquals("<b>This is a string</b>", buffer.trim(16));
        assertEquals("<b>This is a st...</b>", buffer.trim(15));
        assertEquals("<b>This is...</b>", buffer.trim(10));
        assertEquals("<b>..</b>", buffer.trim(2));
    }

    public void testTrimMixedBlock()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a string");
        assertEquals("This <b>is a</b> string", buffer.replace("is a", "<b>$0</b>"));
        assertEquals("This <b>is a</b> string", buffer.trim(20));
        assertEquals("This <b>is a</b> string", buffer.trim(16));
        assertEquals("This <b>is a</b> st...", buffer.trim(15));
        assertEquals("This <b>is..</b>.", buffer.trim(10));
        assertEquals("This <b>...</b>", buffer.trim(8));
        assertEquals("Thi..<b>.</b>", buffer.trim(6));
        assertEquals("T...", buffer.trim(4));
    }

    public void testWrapShort()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("12345");
        assertEquals("12345", buffer.wrap(10));
    }

    public void testWrapSimple()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("12345 67890");
        assertEquals("12345\n67890", buffer.wrap(5));
    }

    public void testWrapEarlierSpace()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("123 4567");
        assertEquals("123\n4567", buffer.wrap(5));
    }

    public void testWrapMultiline()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("this is a very fine multiline example");
        assertEquals("this is a\nvery fine\nmultiline\nexample", buffer.wrap(9));
    }
/*
    public void testWrapNoSpace()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("1234567890");
        assertEquals("12345\n67890", buffer.wrap(5));
    }
*/
    public void testWrapWithTags()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a string");
        assertEquals("<b>This is a string</b>", buffer.replace("This is a string", "<b>This is a string</b>"));
        assertEquals("<b>This\nis a\nstring</b>", buffer.wrap(5));
    }

    public void testWrapWithMixedTags()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a string");
        assertEquals("This <b>is a</b> string", buffer.replace("is a", "<b>$0</b>"));
        assertEquals("This\n<b>is a</b>\nstring", buffer.wrap(4));
    }

    public void testSampleA()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("BUG ID: 0;\nNo Bug Number Provided\nChange Description Improve performance in the server code.");
        assertEquals("Improve performance in the server code.", buffer.replace("BUG ID: 0;[\\s]+No Bug Number Provided[\\s]+Change Description[\\s]+", ""));
        assertEquals("Improve performance in the server code.", buffer.trim(50));
        assertEquals("Improve performance in the server code.", buffer.encode());
    }

    public void testSampleB()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a very long line that needs to be wrapped else it will look just a little bit ridiculous.");
        assertEquals("This is a very long line that needs to be wrapped\nelse it will look just a little bit ridiculous.", buffer.wrap(50));
        assertEquals("This is a very long line that needs to be wrapped\nelse it will look just a little bit ridiculous.", buffer.encode());

        buffer = new CommitMessageBuilder("This is a very long line that needs to be wrapped else it will look just a little bit ridiculous.");
        assertEquals("This is a very long line that needs to be wrapp...", buffer.trim(50));
    }

}
