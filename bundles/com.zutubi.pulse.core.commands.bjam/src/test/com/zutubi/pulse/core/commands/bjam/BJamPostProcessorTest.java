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

package com.zutubi.pulse.core.commands.bjam;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;

import java.io.IOException;

public class BJamPostProcessorTest extends PostProcessorTestBase
{
    private RegexPostProcessor pp;

    public void setUp() throws IOException
    {
        super.setUp();
        pp = new RegexPostProcessor(new BJamPostProcessorConfiguration());
    }

    public void testSuccess() throws Exception
    {
        CommandResult result = createAndProcessArtifact("success", pp);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testBadFile() throws Exception
    {
        createAndProcessArtifact("badfile", pp);
        assertErrors("bad: No such file or directory\n" +
                "don't know how to make all\n" +
                "...found 1 target...\n" +
                "...can't find 1 target...");
    }

    public void testBadRule() throws Exception
    {
        createAndProcessArtifact("badrule", pp);
        assertErrors("Jamroot:4: in modules.load\n" +
                "rule badrule unknown in module Jamfile</home/jsankey/archives/boost_1_34_1/tools/build/v2/example/libraries>.\n" +
                "/home/jsankey/archives/boost_1_34_1/tools/build/v2/build/project.jam:312: in load-jamfile\n" +
                "/home/jsankey/archives/boost_1_34_1/tools/build/v2/build/project.jam:68: in load\n" +
                "/home/jsankey/archives/boost_1_34_1/tools/build/v2/build/project.jam:170: in project.find");
    }

    public void testCompileError() throws Exception
    {
        createAndProcessArtifact("compileerror", pp);
        assertErrors("\n" +
                "...failed gcc.compile.c++ util/foo/bin/gcc-4.1.2/debug/bar.o...\n" +
                "...skipped <putil/foo/bin/gcc-4.1.2/debug>libbar.so for lack of <putil/foo/bin/gcc-4.1.2/debug>bar.o...\n" +
                "...skipped <papp/bin/gcc-4.1.2/debug>app for lack of <putil/foo/bin/gcc-4.1.2/debug>libbar.so...\n" +
                "...failed updating 1 target...\n" +
                "...skipped 2 targets...");
    }

    public void testMissingSemi() throws Exception
    {
        createAndProcessArtifact("missingsemi", pp);
        assertErrors("Jamroot:2: in modules.load\n" +
                "*** argument error\n" +
                "* rule use-project ( id : where )\n" +
                "* called with: ( /library-example/foo : util/foo build-project app )\n" +
                "* extra argument build-project");
    }

    public void testToolsetWarnings() throws Exception
    {
        createAndProcessArtifact("toolsetwarnings", pp);
        assertWarnings("warning: No toolsets are configured.\n" +
                "warning: Configuring default toolset \"gcc\".\n" +
                "warning: If the default is wrong, you may not be able to build C++ programs.\n" +
                "warning: Use the \"--toolset=xxxxx\" option to override our guess.\n" +
                "warning: For more configuration options, please consult\n" +
                "warning: http://boost.org/boost-build2/doc/html/bbv2/advanced/configuration.html\n" +
                "...found 9 targets...\n" +
                "...updating 5 targets...\n" +
                "MkDir1 bin");
    }

    public void testUnknownTarget() throws Exception
    {
        createAndProcessArtifact("unknowntarget", pp);
        assertErrors("notice: assuming it's a name of file to create\n" +
                "don't know how to make <e>nosuchtarget\n" +
                "...found 1 target...\n" +
                "...can't find 1 target...");
    }
}
