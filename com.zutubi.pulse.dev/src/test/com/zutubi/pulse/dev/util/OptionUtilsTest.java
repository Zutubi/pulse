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

package com.zutubi.pulse.dev.util;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.apache.commons.cli.ParseException;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class OptionUtilsTest extends PulseTestCase
{
    public void testRequirement() throws PulseException
    {
        assertEquals(new ResourceRequirement("myresource", false, false), OptionUtils.parseResourceRequirement("myresource"));
    }

    public void testRequirementInverted() throws PulseException
    {
        assertEquals(new ResourceRequirement("myresource", true, false), OptionUtils.parseResourceRequirement("!myresource"));
    }

    public void testRequirementWithVersion() throws PulseException
    {
        assertEquals(new ResourceRequirement("myresource", "myversion", false, false), OptionUtils.parseResourceRequirement("myresource/myversion"));
    }

    public void testRequirementWithEmptyVersion() throws PulseException
    {
        assertEquals(new ResourceRequirement("myresource", false, false), OptionUtils.parseResourceRequirement("myresource/"));
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
