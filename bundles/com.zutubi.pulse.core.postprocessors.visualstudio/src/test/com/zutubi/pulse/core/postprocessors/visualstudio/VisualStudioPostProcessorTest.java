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

package com.zutubi.pulse.core.postprocessors.visualstudio;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import static com.zutubi.pulse.core.engine.api.FeatureMatchers.hasOrderedErrors;
import static com.zutubi.pulse.core.engine.api.FeatureMatchers.hasOrderedWarnings;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase;
import com.zutubi.pulse.core.postprocessors.api.TestPostProcessorContext;
import static org.hamcrest.MatcherAssert.assertThat;

public class VisualStudioPostProcessorTest extends PostProcessorTestCase
{
    private RegexPostProcessor pp;

    public void setUp() throws Exception
    {
        super.setUp();
        VisualStudioPostProcessorConfiguration config = new VisualStudioPostProcessorConfiguration();
        config.setLeadingContext(0);
        config.setTrailingContext(0);
        pp = new RegexPostProcessor(config);
    }

    public void testCSCErrors() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors("Test.cs(5,16): error CS0103: The name 'i' does not exist in the current context"));
    }

    public void testCSCWarnings() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedWarnings("Test.cs(5,13): warning CS0168: The variable 'i' is declared but never used"));
    }
}
