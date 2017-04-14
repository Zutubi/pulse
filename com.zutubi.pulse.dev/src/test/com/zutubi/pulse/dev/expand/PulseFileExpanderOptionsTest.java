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

package com.zutubi.pulse.dev.expand;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

public class PulseFileExpanderOptionsTest extends PulseTestCase
{
    public void testStandardOptions() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions(
                "-r", "test.recipe",
                "-e", "test.resources",
                "-b", "test.base",
                "test.file"
        );
        
        assertEquals("test.file", options.getPulseFile());
        assertEquals("test.recipe", options.getRecipe());
        assertEquals("test.resources", options.getResourcesFile());
        assertEquals(new File("test.base"), options.getBaseDir());
    }

    public void testStandardLongOptions() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions(
                "--recipe", "test.recipe",
                "--resources-file", "test.resources",
                "--base-dir", "test.base",
                "test.file"
        );
        
        assertEquals("test.file", options.getPulseFile());
        assertEquals("test.recipe", options.getRecipe());
        assertEquals("test.resources", options.getResourcesFile());
        assertEquals(new File("test.base"), options.getBaseDir());
    }

    public void testRequirements() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions(
                "-q", "r1",
                "--require", "r2/ver",
                "pulse.xml"
        );
        
        List<ResourceRequirement> requirements = options.getResourceRequirements();
        assertEquals(2, requirements.size());
        assertThat(requirements, hasItem(new ResourceRequirement("r1", false, false)));
        assertThat(requirements, hasItem(new ResourceRequirement("r2", "ver", false, false)));
    }
    
    public void testDefines() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions(
                "-d", "foo=bar",
                "--define", "baz=quux",
                "pulse.xml"
        );
        
        Properties defines = options.getDefines();
        assertEquals(2, defines.size());
        assertEquals("bar", defines.get("foo"));
        assertEquals("quux", defines.get("baz"));
    }
    
    public void testNoPulseFile()
    {
        try
        {
            new PulseFileExpanderOptions(new String[0]);
            fail("Should require pulse file");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("No pulse file specified."));
        }
    }
    
    public void testDefaultBaseDir() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions("test.file");
        assertEquals(FileSystemUtils.getWorkingDirectory(), options.getBaseDir());
    }
}
