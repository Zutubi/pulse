/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.committransformers;

import com.zutubi.util.junit.ZutubiTestCase;

/**
 * <class comment/>
 */
public class CommitMessageBuilderTest extends ZutubiTestCase
{
    public void testReplaceLiteral()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("The quick brown fox jumps over the lazy dog.");
        assertEquals("The QUICK brown fox jumps over the lazy dog.", builder.replace("quick", "QUICK", false));
    }

    public void testReplaceWithGrouping()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("The quick brown fox jumps over the lazy dog.");
        assertEquals("The quick <b>brown fox jumps</b> over the lazy dog.", builder.replace("brown fox jumps", "<b>$0</b>", false));
    }

    public void testEncodeAfterHtmlReplacement()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("A <i>very</i> bold statement.");
        assertEquals("A <i>very</i> <b>bold</b> statement.", builder.replace("bold", "<b>$0</b>", false));
        assertEquals("A &lt;i&gt;very&lt;/i&gt; <b>bold</b> statement.", builder.encode());
// Upgrade to oscore 2.2.5 which contains a encodeHtml method that does not re-encode encoded html.        
//        assertEquals("A &lt;i&gt;very&lt;/i&gt; <b>bold</b> statement.", builder.encode());
    }

    public void testMultipleReplacements()
    {
        // The second match hits as the first is non-exclusive
        CommitMessageBuilder builder = new CommitMessageBuilder("A JIRA-123 issue");
        builder.replace("[A-Z]+-[0-9]+", "<a href='foo'>$0</a>", false);
        builder.replace("[A-Z]+-[0-9]+", "<a href='bar'>$0</a>", false);
        assertEquals("A <a href='foo'><a href='bar'>JIRA-123</a></a> issue", builder.toString());
    }

    public void testMultipleReplacementsExclusive()
    {
        // The second match hits as the first is non-exclusive
        CommitMessageBuilder builder = new CommitMessageBuilder("A JIRA-123 issue");
        builder.replace("[A-Z]+-[0-9]+", "<a href='foo'>$0</a>", true);
        builder.replace("[A-Z]+-[0-9]+", "<a href='bar'>$0</a>", false);
        assertEquals("A <a href='foo'>JIRA-123</a> issue", builder.toString());
    }

    public void testMultiplePartialOverlapping()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("Some ABC characters");
        builder.replace("AB", "-$0", false);
        builder.replace("BC", "-$0", false);
        assertEquals("Some -A-BC characters", builder.toString());
    }

    public void testMultiplePartialOverlappingExclusive()
    {
        // Second match ignored - the B already participated in the first
        // match
        CommitMessageBuilder builder = new CommitMessageBuilder("Some ABC characters");
        builder.replace("AB", "-$0", true);
        builder.replace("BC", "-$0", false);
        assertEquals("Some -ABC characters", builder.toString());
    }

    public void testMultiplePartialOverlappingOriginalReplacementMatches()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("Some iAbC characters");
        builder.replace("i([^ ]+)", "<i>$1</i>", false);
        builder.replace("b([^ ]+)", "<b>$1</b>", false);
        assertEquals("Some <i>A<b>C</i></b> characters", builder.toString());
    }

    public void testMultiplePartialOverlappingOriginalReplacementMatchesExclusive()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder("Some iAbC characters");
        builder.replace("i([^ ]+)", "<i>$1</i>", true);
        builder.replace("b([^ ]+)", "<b>$1</b>", false);
        assertEquals("Some <i>AbC</i> characters", builder.toString());
    }

    public void testMultipleOverlappingSingleExpression()
    {
        // Greedy matching may be what is letting us get away with this?
        CommitMessageBuilder builder = new CommitMessageBuilder("This has a AAAAA rating!");
        builder.replace("A+", "<b>$0</b>", false);
        assertEquals("This has a <b>AAAAA</b> rating!", builder.toString());
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
        assertEquals("<b>This is a string</b>", buffer.replace("This is a string", "<b>This is a string</b>", false));
        assertEquals("<b>This is a string</b>", buffer.trim(16));
        assertEquals("<b>This is a st...</b>", buffer.trim(15));
        assertEquals("<b>This is...</b>", buffer.trim(10));
        assertEquals("<b>..</b>", buffer.trim(2));
    }

    public void testTrimMixedBlock()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a string");
        assertEquals("This <b>is a</b> string", buffer.replace("is a", "<b>$0</b>", false));
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

    public void testWrapMultilineLotsOfWhitespace()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("1 2 3 4 5 6 7 8 9 0");
        assertEquals("1 2 3\n4 5 6\n7 8 9\n0", buffer.wrap(5));
    }

    public void testWrapMultilineLotsOfWhitespace2()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("1 2 3 4 5 6 7 8 9 0");
        assertEquals("1 2 3\n4 5 6\n7 8 9\n0", buffer.wrap(6));
    }

    public void testWrapExistingLinebreaks()
    {
        // CIB-1583
        CommitMessageBuilder buffer = new CommitMessageBuilder("1 2\n3 4 5 6 7 8 9 0");
        assertEquals("1 2\n3 4 5\n6 7 8\n9 0", buffer.wrap(6));
    }

    public void testWrapMultipleExistingLinebreaks()
    {
        // CIB-1583
        CommitMessageBuilder buffer = new CommitMessageBuilder("1 2\n3 4 5 6 7\n8 9 0");
        assertEquals("1 2\n3 4 5\n6 7\n8 9 0", buffer.wrap(5));
    }

    public void testWrapMultipleExistingTags()
    {
        // CIB-1583
        CommitMessageBuilder buffer = new CommitMessageBuilder("1 2\n3 4 5 6 7\n8 9 0");
        buffer.replace("1", "<a href=\"yay\">$0</a>", false);
        buffer.replace("5", "<b>$0</b>", false);
        assertEquals("<a href=\"yay\">1</a> 2\n3 4 <b>5</b>\n6 7\n8 9 0", buffer.wrap(5));
    }

    public void testWrapWithTags()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a string");
        assertEquals("<b>This is a string</b>", buffer.replace("This is a string", "<b>This is a string</b>", false));
        assertEquals("<b>This\nis a\nstring</b>", buffer.wrap(5));
    }

    public void testWrapWithMixedTags()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("This is a string");
        assertEquals("This <b>is a</b> string", buffer.replace("is a", "<b>$0</b>", false));
        assertEquals("This\n<b>is a</b>\nstring", buffer.wrap(4));
    }

    public void testSampleA()
    {
        CommitMessageBuilder buffer = new CommitMessageBuilder("BUG ID: 0;\nNo Bug Number Provided\nChange Description Improve performance in the server code.");
        assertEquals("Improve performance in the server code.", buffer.replace("BUG ID: 0;[\\s]+No Bug Number Provided[\\s]+Change Description[\\s]+", "", false));
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
