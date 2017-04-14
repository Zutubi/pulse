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

package com.zutubi.pulse.core.postprocessors.googletest;

import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessor;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;

import java.util.Map;

/**
 * Post-processor for Google Test XML reports (JUnit-compatible).  See:
 * http://code.google.com/p/googletest/wiki/AdvancedGuide#Generating_an_XML_Report
 */
public class GoogleTestReportPostProcessor extends JUnitReportPostProcessor
{
    private static final String PREFIX_DISABLED = "DISABLED_";
    private static final String ATTRIBUTE_STATUS = "status";
    private static final String STATUS_NOT_RUN = "notrun";

    public GoogleTestReportPostProcessor(GoogleTestReportPostProcessorConfiguration config)
    {
        super(config);
    }

    @Override
    protected String getTestSuiteName(Map<String, String> attributes)
    {
        return filterDisabled(super.getTestSuiteName(attributes));
    }

    @Override
    protected String getTestCaseName(Map<String, String> attributes)
    {
        return filterDisabled(super.getTestCaseName(attributes));
    }

    @Override
    protected TestStatus getTestCaseImmediateStatus(Map<String, String> attributes)
    {
        if (STATUS_NOT_RUN.equals(attributes.get(ATTRIBUTE_STATUS)))
        {
            return TestStatus.SKIPPED;
        }
        else
        {
            return super.getTestCaseImmediateStatus(attributes);
        }
    }

    private String filterDisabled(String name)
    {
        if (name != null && name.startsWith(PREFIX_DISABLED))
        {
            return name.substring(PREFIX_DISABLED.length());
        }

        return name;
    }
}
