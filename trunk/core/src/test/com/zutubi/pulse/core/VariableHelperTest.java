package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import junit.framework.TestCase;

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
        }
        catch (PulseException e)
        {
            assertEquals(expectedError, e.getMessage());
            return;
        }

        fail("Expected config exception, got '" + result + "'");
    }

    private void successTest(String in, String out) throws Exception
    {
        String result = VariableHelper.replaceVariables(in, scope);
        assertEquals(out, result);
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
}
