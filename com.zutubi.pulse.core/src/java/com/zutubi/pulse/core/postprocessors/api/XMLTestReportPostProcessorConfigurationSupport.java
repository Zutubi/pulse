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

package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;

/**
 * Helper base class for post processors that find test results in XML files.
 * This class handles XML parsing, passing in a document for implementations
 * to walk.  <a href="http://www.xom.nu/">XOM</a> is used for parsing as it
 * has a convenient document API.
 *
 * @see com.zutubi.pulse.core.util.api.XMLUtils
 */
@SymbolicName("zutubi.xmlTestReportPostProcessorConfigSupport")
public abstract class XMLTestReportPostProcessorConfigurationSupport extends TestReportPostProcessorConfigurationSupport
{
    private String reportType;

    /**
     * Creates a new XML report processor for the given report type.
     *
     * @param postProcessorType type of processor created for this config
     * @param reportType human-readable name of the type of report being
     *                   processed (e.g. JUnit)
     */
    protected XMLTestReportPostProcessorConfigurationSupport(Class<? extends TestReportPostProcessorSupport> postProcessorType, String reportType)
    {
        super(postProcessorType);
        this.reportType = reportType;
    }

    public String reportType()
    {
        return reportType;
    }
}
