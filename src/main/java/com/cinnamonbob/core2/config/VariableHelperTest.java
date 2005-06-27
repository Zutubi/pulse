package com.cinnamonbob.core2.config;

import com.cinnamonbob.BobException;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 */
public class VariableHelperTest extends TestCase
{
 
    private Map<String, String> properties = null;
    
    public void setUp()
    {
        properties = new HashMap<String, String>();
        properties.put("foo", "foo");
        properties.put("bar", "baz");
        properties.put("a\\b", "slashed");
    }
    
    private void errorTest(String input, String expectedError)
    {
        String result = "";
        
        try
        {
            result = VariableHelper.replaceVariables(input, properties);
        }
        catch (BobException e)
        {
            assertEquals(expectedError, e.getMessage());
            return;
        }
        
        fail("Expected config exception, got '" + result + "'");        
    }
    
    private void successTest(String in, String out) throws Exception
    {
        String result = VariableHelper.replaceVariables(in, properties);
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
        properties.put("a", "${foo}");
        successTest("${a}", "foo");
    }
    
    public void testCircularReference() throws Exception
    {
        properties.put("1", "${2}");
        properties.put("2", "${1}");
        errorTest("${1}", "Variable error: could not resolve variables in input.");
        
        properties.put("3", "${3}");
        errorTest("${1}", "Variable error: could not resolve variables in input.");        
    }
    
    
}
