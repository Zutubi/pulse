package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * 
 *
 */
public class VariableHelperTest extends TestCase
{

    private Scope scope = null;

    public void setUp() throws FileLoadException
    {
        scope = new Scope();
        scope.setReference(new Property("foo", "foo"));
        scope.setReference(new Property("bar", "baz"));
        scope.setReference(new Property("a\\b", "slashed"));
    }

    private void errorTest(String input, String expectedError)
    {
        String result = "";

        try
        {
            result = VariableHelper.replaceVariables(input, scope);
            fail("Expected config exception, got '" + result + "'");
        }
        catch (PulseException e)
        {
            assertEquals(expectedError, e.getMessage());
        }
    }

    private void successTest(String in, String out) throws Exception
    {
        String result = VariableHelper.replaceVariables(in, scope);
        assertEquals(out, result);
    }

    private void successSplitTest(String in, String... out) throws Exception
    {
        List<String> result = VariableHelper.splitAndReplaceVariables(in, scope, false);
        assertEquals(Arrays.asList(out), result);
    }

    private void errorSplitTest(String input, String expectedError)
    {
        List<String> result;
        try
        {
            result = VariableHelper.splitAndReplaceVariables(input, scope, false);
            fail("Expected config exception, got '" + result + "'");
        }
        catch (PulseException e)
        {
            assertEquals(expectedError, e.getMessage());
        }
    }

    public void testDollarEOI()
    {
        errorTest("ladida $", "Syntax error: unexpected end of input looking for '{'");
    }

    public void testDollarBraceEOI()
    {
        errorTest("ladida ${", "Syntax error: unexpected end of input looking for '}'");
    }

    public void testDollarNoBrace()
    {
        errorTest("$e", "Syntax error: expecting '{', got 'e'");
    }

    public void testHalfEscape()
    {
        errorTest("hoorah \\", "Syntax error: unexpected end of input in escape sequence (\\)");
    }

    public void testEmptyVariable()
    {
        errorTest("${}", "Syntax error: empty variable reference");
    }

    public void testUnknownVariable()
    {
        errorTest("${greebo}", "Reference to unknown variable 'greebo'");
    }

    public void testSimpleSubstitution() throws Exception
    {
        successTest("${foo}", "foo");
    }

    public void testSimpleSubstitution2() throws Exception
    {
        successTest("${bar}", "baz");
    }

    public void testSubstitutionLeading() throws Exception
    {
        successTest("leading text ${bar}", "leading text baz");
    }

    public void testSubstitutionTrailing() throws Exception
    {
        successTest("${bar} trailing text", "baz trailing text");
    }

    public void testSubstitutionTwice() throws Exception
    {
        successTest("${foo}${bar}", "foobaz");
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

    public void testSlashInVariable() throws Exception
    {
        successTest("${a\\b}", "slashed");
    }

    public void testNestedVariable() throws Exception
    {
        scope.setReference(new Property("a", "${foo}"));
        successTest("${a}", "${foo}");
    }

    public void testSpacesPreserved() throws Exception
    {
        successTest("in space  two   three ${bar} vars", "in space  two   three baz vars");
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

    public void testSplitVariableReference() throws Exception
    {
        successSplitTest("${bar}", "baz");
    }

    public void testSplitAroundVariableReferences() throws Exception
    {
        successSplitTest("${foo} and ${bar}", "foo", "and", "baz");
    }

    public void testSplitVariableReferenceInQuotes() throws Exception
    {
        successSplitTest("quotes \"around ${bar}\"", "quotes", "around baz");
    }

    public void testSplitQuotesInVariableReference() throws Exception
    {
        scope.setReference(new Property("a\"b", "val"));
        successSplitTest("odd ${a\"b} variable", "odd", "val", "variable");        
    }

    public void testSplitSpaceInVariableReference() throws Exception
    {
        scope.setReference(new Property("space invader", "val"));
        successSplitTest("odd ${space invader} variable", "odd", "val", "variable");
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

    public void testSplitUnterminatedQuotes() throws Exception
    {
        errorSplitTest("\"unterminated", "Syntax error: unexpected end of input looking for closing quotes (\")");
    }
}
