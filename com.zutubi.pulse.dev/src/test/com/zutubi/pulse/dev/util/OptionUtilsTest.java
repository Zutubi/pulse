package com.zutubi.pulse.dev.util;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.apache.commons.cli.ParseException;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class OptionUtilsTest extends PulseTestCase
{
    public void testRequirement() throws PulseException
    {
        assertEquals(new ResourceRequirement("myresource", false), OptionUtils.parseResourceRequirement("myresource"));
    }

    public void testRequirementWithVersion() throws PulseException
    {
        assertEquals(new ResourceRequirement("myresource", "myversion", false), OptionUtils.parseResourceRequirement("myresource/myversion"));
    }

    public void testRequirementWithEmptyVersion() throws PulseException
    {
        assertEquals(new ResourceRequirement("myresource", false), OptionUtils.parseResourceRequirement("myresource/"));
    }

    public void testRequirementEmpty() throws PulseException
    {
        failedRequirementTest("Resource requirement is empty", "");
    }

    public void testRequirementWithEmptyName() throws PulseException
    {
        failedRequirementTest("Resource requirement '/version' has empty resource name", "/version");
    }

    private void failedRequirementTest(String expectedMessage, String arg)
    {
        try
        {
            OptionUtils.parseResourceRequirement(arg);
            fail("Invalid requirements should not parse");
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), containsString(expectedMessage));
        }
    }
    
    public void testDefine() throws ParseException
    {
        Properties properties = new Properties();
        OptionUtils.addDefinedOption("foo=bar", properties);
        assertEquals(1, properties.size());
        assertEquals("bar", properties.get("foo"));
    }
    
    public void testDefineEmptyValue() throws ParseException
    {
        try
        {
            OptionUtils.addDefinedOption("foo=", new Properties());
            fail("Empty values should not be allowed");
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), containsString("Invalid property definition syntax"));
        }
    }

    public void testDefineBadSyntax() throws ParseException
    {
        try
        {
            OptionUtils.addDefinedOption("huh", new Properties());
            fail("Lack of = should not be allowed");
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), containsString("Invalid property definition syntax"));
        }
    }
}
