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

package com.zutubi.pulse.core.commands.nant;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import static com.zutubi.pulse.core.engine.api.FeatureMatchers.*;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase;
import com.zutubi.pulse.core.postprocessors.api.TestPostProcessorContext;
import static org.hamcrest.MatcherAssert.assertThat;

public class NAntPostProcessorTest extends PostProcessorTestCase
{
    private RegexPostProcessor pp;

    public void setUp() throws Exception
    {
        pp = new RegexPostProcessor(new NAntPostProcessorConfiguration());
        super.setUp();
    }

    public void testSimpleSuccess() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedFeatures());
    }

    public void testNoBuildFile() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors("http://nant.sourceforge.net\n" +
                "\n" +
                "\n" +
                "BUILD FAILED\n" +
                "\n" +
                "Could not find a '*.build' file in 'C:\\tools\\cygwin\\home\\jsankey'\n"));
    }

    public void testBuildFileParseError() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors("http://nant.sourceforge.net\n" +
                "\n" +
                "\n" +
                "BUILD FAILED\n" +
                "\n" +
                "C:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\nant.build(12,17):\n" +
                "Error loading buildfile.\n" +
                "    Name cannot begin with the '<' character, hexadecimal value 0x3C. Line 12, position 17.\n" +
                "\n" +
                "For more information regarding the cause of the build failure, run the build again in debug mode."));
    }

    public void testBuildFileInvalid() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors("bad:\n" +
                "\n" +
                "\n" +
                "BUILD FAILED\n" +
                "\n" +
                "C:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\nant.build(17,10):\n" +
                "Either the 'tofile' or 'todir' attribute should be set."));
    }

    public void testSimpleTaskFailure() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors("bad:\n" +
                "\n" +
                "\n" +
                "BUILD FAILED\n" +
                "\n" +
                "C:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\nant.build(17,10):\n" +
                "Could not find file 'C:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\nonexist' to copy."));        
    }

    public void testSimpleCompileError() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors("build:\n" +
                "\n" +
                "      [csc] Compiling 1 files to 'C:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\HelloWorld.exe'.\n" +
                "      [csc] c:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\HelloWorld.cs(3,24): error CS0117: 'System.Console' does not contain a definition for 'WriteLin'\n" +
                "\n" +
                "BUILD FAILED\n" +
                "\n" +
                "C:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\nant.build(10,10):\n" +
                "External Program Failed: C:\\Windows\\Microsoft.NET\\Framework\\v2.0.50727\\csc.exe (return code was 1)"));        
    }

    public void testSimpleWarning() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedWarnings("build:\n" +
                "\n" +
                "      [csc] Compiling 1 files to 'C:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\HelloWorld.exe'.\n" +
                "      [csc] c:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\HelloWorld.cs(3,6): warning CS0168: The variable 'neverUsed' is declared but never used\n" +
                "\n" +
                "BUILD SUCCEEDED\n" +
                ""));
    }
}
