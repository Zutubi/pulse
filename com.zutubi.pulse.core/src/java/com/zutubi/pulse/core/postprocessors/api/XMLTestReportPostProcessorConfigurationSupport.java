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
    protected XMLTestReportPostProcessorConfigurationSupport(Class<? extends XMLTestReportPostProcessorSupport> postProcessorType, String reportType)
    {
        super(postProcessorType);
        this.reportType = reportType;
    }

    public String reportType()
    {
        return reportType;
    }
}
