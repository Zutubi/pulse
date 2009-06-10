package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.FileLoadException;
import com.zutubi.pulse.core.engine.api.HashReferenceMap;
import com.zutubi.pulse.core.engine.api.Property;
import com.zutubi.pulse.core.engine.api.ReferenceMap;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.List;

public class ReferenceResolverTest extends ZutubiTestCase
{
    private ReferenceMap scope = null;

    public void setUp() throws FileLoadException
    {
        scope = new HashReferenceMap();
        scope.add(new Property("foo", "foo"));
        scope.add(new Property("bar", "baz"));
        scope.add(new Property("a\\b", "slashed"));
        scope.add(new Property("empty", ""));
        scope.add(new Property("a{b}c", "braced"));
        scope.add(new Property("a(b)c", "parened"));
    }

    private void errorTest(String input, String expectedError)
    {
        String result;

        try
        {
            result = ReferenceResolver.resolveReferences(input, scope);
            fail("Expected config exception, got '" + result + "'");
        }
        catch (PulseException e)
        {
            assertEquals(expectedError, e.getMessage());
        }
    }

    private void successTest(String in, String out) throws Exception
    {
        String result = ReferenceResolver.resolveReferences(in, scope);
        assertEquals(out, result);
    }

    private void successSplitTest(String in, String... out) throws Exception
    {
        List<String> result = ReferenceResolver.splitAndResolveReferences(in, scope, ReferenceResolver.ResolutionStrategy.RESOLVE_STRICT);
        assertEquals(Arrays.asList(out), result);
    }

    private void errorSplitTest(String input, String expectedError)
    {
        List<String> result;
        try
        {
            result = ReferenceResolver.splitAndResolveReferences(input, scope, ReferenceResolver.ResolutionStrategy.RESOLVE_STRICT);
            fail("Expected config exception, got '" + result + "'");
        }
        catch (PulseException e)
        {
            assertEquals(expectedError, e.getMessage());
        }
    }

    public void testDollarEOI()
    {
        errorTest("ladida $", "Syntax error: unexpected end of input looking for '{' or '('");
    }

    public void testDollarBraceEOI()
    {
        errorTest("ladida ${", "Syntax error: unexpected end of input looking for '}'");
    }

    public void testDollarParenEOI()
    {
        errorTest("ladida $(", "Syntax error: unexpected end of input looking for ')'");
    }

    public void testDollarNoBraceOrParen()
    {
        errorTest("$e", "Syntax error: expecting '{' or '(', got 'e'");
    }

    public void testHalfEscape()
    {
        errorTest("hoorah \\", "Syntax error: unexpected end of input in escape sequence (\\)");
    }

    public void testEmptyReference()
    {
        errorTest("${}", "Syntax error: empty reference");
    }

    public void testParenEmptyReference()
    {
        errorTest("$()", "Syntax error: empty reference");
    }

    public void testUnknownReference()
    {
        errorTest("${greebo}", "Unknown reference 'greebo'");
    }

    public void testParenUnknownReference()
    {
        errorTest("$(greebo)", "Unknown reference 'greebo'");
    }

    public void testSimpleSubstitution() throws Exception
    {
        successTest("${foo}", "foo");
    }

    public void testParentSimpleSubstitution() throws Exception
    {
        successTest("$(foo)", "foo");
    }

    public void testSimpleSubstitution2() throws Exception
    {
        successTest("${bar}", "baz");
    }

    public void testSubstitutionLeading() throws Exception
    {
        successTest("leading text ${bar}", "leading text baz");
    }

    public void testParenSubstitutionLeading() throws Exception
    {
        successTest("leading text $(bar)", "leading text baz");
    }

    public void testSubstitutionTrailing() throws Exception
    {
        successTest("${bar} trailing text", "baz trailing text");
    }

    public void testParenSubstitutionTrailing() throws Exception
    {
        successTest("$(bar) trailing text", "baz trailing text");
    }

    public void testSubstitutionTwice() throws Exception
    {
        successTest("${foo}${bar}", "foobaz");
    }

    public void testParenSubstitutionTwice() throws Exception
    {
        successTest("$(foo)$(bar)", "foobaz");
    }

    public void testSimpleEscape() throws Exception
    {
        successTest("esc\\ape", "escape");
    }

    public void testEscapeDollar() throws Exception
    {
        successTest("\\${foo}", "${foo}");
    }

    public void testEscapeSlash() throws Exception
    {
        successTest("\\\\", "\\");
    }

    public void testEscapeSlashSlash() throws Exception
    {
        successTest("\\\\\\\\", "\\\\");
    }

    public void testEscapeSlashOther() throws Exception
    {
        successTest("\\\\\\x", "\\x");
    }

    public void testSlashInReference() throws Exception
    {
        successTest("${a\\b}", "slashed");
    }

    public void testParenSlashInReference() throws Exception
    {
        successTest("$(a\\b)", "slashed");
    }

    public void testNestedReference() throws Exception
    {
        scope.add(new Property("a", "${foo}"));
        successTest("${a}", "${foo}");
    }

    public void testSpacesPreserved() throws Exception
    {
        successTest("in space  two   three ${bar} vars", "in space  two   three baz vars");
    }

    public void testParenSpacesPreserved() throws Exception
    {
        successTest("in space  two   three $(bar) vars", "in space  two   three baz vars");
    }

    public void testQuote() throws Exception
    {
        successTest("a \"quote", "a \"quote");
    }

    public void testQuotes() throws Exception
    {
        successTest("some \"quotes in\" here", "some \"quotes in\" here");
    }

    public void testSplitSimple() throws Exception
    {
        successSplitTest("hello", "hello");
    }

    public void testSplitTwoPieces() throws Exception
    {
        successSplitTest("two pieces", "two", "pieces");
    }

    public void testSplitMultipleSpaces() throws Exception
    {
        successSplitTest("multiple   spaces", "multiple", "spaces");
    }

    public void testSplitEmptyString() throws Exception
    {
        successSplitTest("");
    }

    public void testSplitSpace() throws Exception
    {
        successSplitTest(" ");
    }

    public void testSplitSpaces() throws Exception
    {
        successSplitTest("   ");
    }

    public void testSplitManyPieces() throws Exception
    {
        successSplitTest("this string  has   many pieces in it", "this", "string", "has", "many", "pieces", "in", "it");
    }

    public void testSplitQuoted() throws Exception
    {
        successSplitTest("with \"a quoted\" bit", "with", "a quoted", "bit");
    }

    public void testSplitQuotedInMiddle() throws Exception
    {
        successSplitTest("with\"a quoted\"bit there", "witha quotedbit", "there");
    }

    public void testSplitEscapedSpace() throws Exception
    {
        successSplitTest("an\\ escaped space", "an escaped", "space");
    }

    public void testSplitEscapedQuote() throws Exception
    {
        successSplitTest("an \\\"escaped quote \"and then this\"", "an", "\"escaped", "quote", "and then this");
    }

    public void testSplitEscapedQuoteInQuotes() throws Exception
    {
        successSplitTest("\"inside \\\" quotes\"", "inside \" quotes");
    }

    public void testSplitSingleReference() throws Exception
    {
        successSplitTest("${bar}", "baz");
    }

    public void testSplitParenSingleReference() throws Exception
    {
        successSplitTest("$(bar)", "baz");
    }

    public void testSplitAroundMultipleReferences() throws Exception
    {
        successSplitTest("${foo} and ${bar}", "foo", "and", "baz");
    }

    public void testSplitParenAroundMultipleReferences() throws Exception
    {
        successSplitTest("$(foo) and $(bar)", "foo", "and", "baz");
    }

    public void testSplitReferenceInQuotes() throws Exception
    {
        successSplitTest("quotes \"around ${bar}\"", "quotes", "around baz");
    }

    public void testSplitParenReferenceInQuotes() throws Exception
    {
        successSplitTest("quotes \"around $(bar)\"", "quotes", "around baz");
    }

    public void testSplitQuotesInReference() throws Exception
    {
        scope.add(new Property("a\"b", "val"));
        successSplitTest("odd ${a\"b} ref", "odd", "val", "ref");
    }

    public void testSplitParenQuotesInReference() throws Exception
    {
        scope.add(new Property("a\"b", "val"));
        successSplitTest("odd $(a\"b) ref", "odd", "val", "ref");
    }

    public void testSplitSpaceInReference() throws Exception
    {
        scope.add(new Property("space invader", "val"));
        successSplitTest("odd ${space invader} ref", "odd", "val", "ref");
    }

    public void testSplitParenSpaceInReference() throws Exception
    {
        scope.add(new Property("space invader", "val"));
        successSplitTest("odd $(space invader) ref", "odd", "val", "ref");
    }

    public void testSplitSpaceAtStart() throws Exception
    {
        successSplitTest(" space at start", "space", "at", "start");
    }

    public void testSplitSpaceAtEnd() throws Exception
    {
        successSplitTest("space at end ", "space", "at", "end");
    }

    public void testSplitQuotesAtStart() throws Exception
    {
        successSplitTest("\"quotes at\" start", "quotes at", "start");
    }

    public void testSplitQuotesAtEnd() throws Exception
    {
        successSplitTest("quotes \"at end\"", "quotes", "at end");
    }

    public void testSplitEmptyQuotedString() throws Exception
    {
        successSplitTest("\"\"", "");
    }

    public void testSplitEmptyStringInMiddle() throws Exception
    {
        successSplitTest("in \"\" middle", "in", "", "middle");
    }

    public void testSplitEmptyStringAdjacent() throws Exception
    {
        successSplitTest("empty\"\" adjacent", "empty", "adjacent");
    }

    public void testSplitEmptyReference() throws Exception
    {
        successSplitTest("${empty}");
    }

    public void testSplitParenEmptyReference() throws Exception
    {
        successSplitTest("$(empty)");
    }

    public void testSplitQuotedEmptyReference() throws Exception
    {
        successSplitTest("\"${empty}\"", "");
    }

    public void testSplitParenQuotedEmptyReference() throws Exception
    {
        successSplitTest("\"$(empty)\"", "");
    }

    public void testSplitEmptyReferenceAdjacent() throws Exception
    {
        successSplitTest("adjacent${empty} ref", "adjacent", "ref");
    }

    public void testSplitParenEmptyReferenceAdjacent() throws Exception
    {
        successSplitTest("adjacent$(empty) ref", "adjacent", "ref");
    }

    public void testSplitUnterminatedQuotes() throws Exception
    {
        errorSplitTest("\"unterminated", "Syntax error: unexpected end of input looking for closing quotes (\")");
    }

    public void testDefaultReferenceDefined() throws Exception
    {
        successTest("$(bar?def)", "baz");
    }

    public void testDefaultReferenceDefinedButEmpty() throws Exception
    {
        successTest("$(empty?def)", "");
    }

    public void testDefaultReferenceNotDefined() throws Exception
    {
        successTest("$(undefined?def)", "def");
    }

    public void testDefaultEmpty() throws Exception
    {
        successTest("$(undefined?)", "");
    }

    public void testDefaultSingleReference() throws Exception
    {
        assertEquals("def", ReferenceResolver.resolveReference("$(undefined?def)", scope));
    }

    public void testMixedBracketsBraceParen()
    {
        errorTest("${foo)", "Syntax error: unexpected end of input looking for '}'");
    }

    public void testMixedBracketsParenBrace()
    {
        errorTest("$(foo}", "Syntax error: unexpected end of input looking for ')'");
    }

    public void testBraceInExtendedName() throws Exception
    {
        successTest("$(a{b}c)", "braced");
    }

    public void testParenInName() throws Exception
    {
        successTest("${a(b)c}", "parened");
    }

    public void testReservedCharactersInExtendedName() throws Exception
    {
        for (Character c: Arrays.asList('!', '%', '#', '&', '/', ':', ';', '/'))
        {
            errorTest("$(a" + c + "b)", "Syntax error: '" + c + "' is reserved and may not be used in an extended reference name");
        }
    }
}
