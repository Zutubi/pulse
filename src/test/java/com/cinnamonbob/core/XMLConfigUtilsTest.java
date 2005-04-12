package com.cinnamonbob.core;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

public class XMLConfigUtilsTest extends TestCase
{
    private static final String FAKE_FILENAME = "filename.fake";
    
    private Map<String, String> variables;
    
    
    public void setUp()
    {
        variables = new TreeMap<String, String>();
        variables.put("foo", "foo");
        variables.put("bar", "baz");
        variables.put("a\\b", "slashed");
    }
    
    private void errorTest(String input, String expectedError)
    {
        String result = "";
        
        try
        {
            result = XMLConfigUtils.replaceVariables(FAKE_FILENAME, variables, input);
        }
        catch(ConfigException e)
        {
            assertEquals(expectedError, e.getDetails());
            return;
        }
        
        fail("Expected config exception, got '" + result + "'");        
    }
    
    private void successTest(String in, String out) throws ConfigException
    {
        String result = XMLConfigUtils.replaceVariables(FAKE_FILENAME, variables, in);
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
        
    public void testSimpleSubstitution() throws ConfigException
    {
        successTest("${foo}", "foo");
    }

    public void testSimpleSubstitution2() throws ConfigException
    {
        successTest("${bar}", "baz");
    }

    public void testSubstitutionLeading() throws ConfigException
    {
        successTest("leading text ${bar}", "leading text baz");
    }
    
    public void testSubstitutionTrailing() throws ConfigException
    {
        successTest("${bar} trailing text", "baz trailing text");
    }
    
    public void testSubstitutionTwice() throws ConfigException
    {
        successTest("${foo}${bar}", "foobaz");
    }
    
    public void testSimpleEscape() throws ConfigException
    {
        successTest("esc\\ape", "escape");
    }
    
    public void testEscapeDollar() throws ConfigException
    {
        successTest("\\${foo}", "${foo}");
    }
    
    public void testEscapeSlash() throws ConfigException
    {
        successTest("\\\\", "\\");
    }

    public void testEscapeSlashSlash() throws ConfigException
    {
        successTest("\\\\\\\\", "\\\\");
    }

    public void testEscapeSlashOther() throws ConfigException
    {
        successTest("\\\\\\x", "\\x");
    }

    public void testSlashInVariable() throws ConfigException
    {
        successTest("${a\\b}", "slashed");
    }
}
