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

package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;

import java.io.IOException;

/**
 * Tests for RegexPostProcessor.
 */
public class MsBuildPostProcessorTest extends PostProcessorTestBase
{
    private RegexPostProcessor pp;

    public void setUp() throws IOException
    {
        pp = new RegexPostProcessor(new MsBuildPostProcessorConfiguration());
        super.setUp();
    }

    public void testSuccess() throws Exception
    {
        CommandResult result = createAndProcessArtifact("success", pp);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testNoBuildFile() throws Exception
    {
        createAndProcessArtifact("nobuildfile", pp);
        assertErrors("MSBUILD : error MSB1003: Specify a project or solution file. The current working directory does not contain a project or solution file.");
    }

    public void testBadBuildFile() throws Exception
    {
        createAndProcessArtifact("badbuildfile", pp);
        assertErrors("MSBUILD : error MSB1009: Project file does not exist.");
    }

    public void testBadSourceFile() throws Exception
    {
        createAndProcessArtifact("badsourcefile", pp);
        assertErrors("CSC : error CS2001: Source file 'Test.cs' could not be found",
                     "CSC : fatal error CS2008: No inputs specified",
                     "Build FAILED.");
    }

    public void testCSharpCompile() throws Exception
    {
        CommandResult result = createAndProcessArtifact("csharpcompile", pp);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testCSharpCompileError() throws Exception
    {
        createAndProcessArtifact("csharpcompileerror", pp);
        assertErrors("Test.cs(5,16): error CS0103: The name 'i' does not exist in the current context",
                     "Build FAILED.");
    }

    public void testCSharpCompileWarning() throws Exception
    {
        createAndProcessArtifact("csharpcompilewarning", pp);
        assertWarnings("Test.cs(5,13): warning CS0168: The variable 'i' is declared but never used");
        assertErrors();
    }
}
