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

package com.zutubi.pulse.core.postprocessors.gcc;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import static com.zutubi.pulse.core.engine.api.FeatureMatchers.hasOrderedErrors;
import static com.zutubi.pulse.core.engine.api.FeatureMatchers.hasOrderedWarnings;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase;
import com.zutubi.pulse.core.postprocessors.api.TestPostProcessorContext;
import static org.hamcrest.MatcherAssert.assertThat;

public class GccPostProcessorTest extends PostProcessorTestCase
{
    private RegexPostProcessor pp;

    public void setUp() throws Exception
    {
        super.setUp();
        GccPostProcessorConfiguration config = new GccPostProcessorConfiguration();
        config.setLeadingContext(0);
        config.setTrailingContext(0);
        pp = new RegexPostProcessor(config);
    }

    public void testErrors() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors(
                "../libs/config/test/boost_has_tr1_result_of.ipp:16: error: ‘std::tr1’ has not been declared",
                "../libs/config/test/boost_has_tr1_result_of.ipp:16: error: expected initializer before ‘<’ token",
                "../libs/config/test/boost_has_tr1_result_of.ipp:17: error: using ‘typename’ outside of template",
                "../libs/config/test/boost_has_tr1_result_of.ipp:17: error: ‘r’ has not been declared",
                "../libs/config/test/boost_has_tr1_result_of.ipp:17: error: invalid type in declaration before ‘;’ token"
        ));
    }

    public void testLinkerErrors() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedErrors(
                "/tmp/ccYjFg0f.o:main.c:(.text+0x2b): undefined reference to `_doit'",
                "collect2: ld returned 1 exit status"
        ));
    }

    public void testWarnings() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        assertThat(context.getFeatures(), hasOrderedWarnings(
                "/usr/lib/gcc/i486-linux-gnu/4.1.2/../../../../include/c++/4.1.2/backward/backward_warning.h:32:2: warning: #warning This file includes at least one deprecated or antiquated header. Please consider using one of the 32 headers found in section 17.4.1.2 of the C++ standard. Examples include substituting the <X> header for the <X.h> header for C++ includes, or <iostream> instead of the deprecated header <iostream.h>. To disable this warning use -Wno-deprecated."
        ));
    }
}
