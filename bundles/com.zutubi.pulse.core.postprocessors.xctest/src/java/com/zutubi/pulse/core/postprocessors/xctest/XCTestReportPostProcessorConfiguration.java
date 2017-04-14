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

package com.zutubi.pulse.core.postprocessors.xctest;

import com.zutubi.pulse.core.postprocessors.api.TestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link XCTestReportPostProcessor}.
 */
@SymbolicName("zutubi.xcTestReportPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnFailure", "suite", "shortenSuiteNames", "resolveConflicts", "expectedFailureFile"})
public class XCTestReportPostProcessorConfiguration extends TestReportPostProcessorConfigurationSupport
{
    private boolean shortenSuiteNames;

    public XCTestReportPostProcessorConfiguration()
    {
        super(XCTestReportPostProcessor.class);
    }

    public XCTestReportPostProcessorConfiguration(String name)
    {
        super(XCTestReportPostProcessor.class);
        setName(name);
    }

    /**
     * @return true if suite names that look like paths should be shortened where possible
     */
    public boolean isShortenSuiteNames()
    {
        return shortenSuiteNames;
    }

    public void setShortenSuiteNames(boolean shortenSuiteNames)
    {
        this.shortenSuiteNames = shortenSuiteNames;
    }
}
