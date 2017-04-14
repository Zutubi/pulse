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

package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.PersistentFeature;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;

import java.io.IOException;
import java.util.List;

/**
 */
public class XCodePostProcessorTest extends PostProcessorTestBase
{
    private RegexPostProcessor pp;

    public void setUp() throws IOException
    {
        super.setUp();
        pp = new RegexPostProcessor(new XCodePostProcessorConfiguration());
    }

    public void testSimple() throws Exception
    {
        CommandResult result = createAndProcessArtifact(getName(), pp);
        assertTrue(result.failed());
        List<PersistentFeature> features = artifact.getFeatures();
        assertEquals(3, features.size());
        assertEquals("clang: warning: here\n" +
                     "buffer line\n" +
                     "buffer line\n" +
                     "buffer line", features.get(0).getSummary());
        assertEquals("Uncaught exception: Blah\n" +
                     "buffer line\n" +
                     "buffer line\n" +
                     "buffer line", features.get(1).getSummary());
        assertEquals("There was an Assertion failure\n" +
                     "buffer line\n" +
                     "buffer line\n" +
                     "buffer line", features.get(2).getSummary());
    }

    public void testSetContext() throws Exception
    {
        pp.getConfig().setLeadingContext(0);
        pp.getConfig().setTrailingContext(0);

        CommandResult result = createAndProcessArtifact("testSimple", pp);
        assertTrue(result.failed());
        List<PersistentFeature> features = artifact.getFeatures();
        assertEquals(3, features.size());
        assertEquals("clang: warning: here", features.get(0).getSummary());
        assertEquals("Uncaught exception: Blah", features.get(1).getSummary());
        assertEquals("There was an Assertion failure", features.get(2).getSummary());
    }

    public void testSignError() throws Exception
    {
        pp.getConfig().setLeadingContext(0);
        pp.getConfig().setTrailingContext(0);

        CommandResult result = createAndProcessArtifact(getName(), pp);
        assertTrue(result.failed());
        List<PersistentFeature> features = artifact.getFeatures();
        assertEquals(4, features.size());
        assertEquals("[WARN]iOS deployment target '5.0' for architecture 'armv6' and variant 'normal' is greater than the maximum value '4.3.99' for the iOS 4.3 SDK.", features.get(0).getSummary());
        assertEquals("[WARN]iOS deployment target '5.0' for architecture 'armv7' and variant 'normal' is greater than the maximum value '4.3.99' for the iOS 4.3 SDK.", features.get(1).getSummary());
        assertEquals("[BEROR]Code Sign error: The identity 'iPhone Developer' doesn't match any valid certificate/private key pair in the default keychain", features.get(2).getSummary());
        assertEquals("** BUILD FAILED **", features.get(3).getSummary());
    }

    public void testFatal() throws Exception
    {
        pp.getConfig().setLeadingContext(0);
        pp.getConfig().setTrailingContext(0);

        CommandResult result = createAndProcessArtifact(getName(), pp);
        assertTrue(result.failed());
        List<PersistentFeature> features = artifact.getFeatures();
        assertEquals(1, features.size());
        assertEquals("/Users/joe/pulse/data/agents/macbuild1/recipes/42598404/base/products/ngs/src/client/ios/interact/../ngslib/ngslib/NGSMenuFieldConfig.h:10:9: fatal error: 'NGSMenuEntry.h' file not found", features.get(0).getSummary());
    }
}
