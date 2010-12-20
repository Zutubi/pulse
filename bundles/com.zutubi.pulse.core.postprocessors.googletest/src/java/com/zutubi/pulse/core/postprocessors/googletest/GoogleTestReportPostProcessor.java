package com.zutubi.pulse.core.postprocessors.googletest;

import com.zutubi.pulse.core.postprocessors.api.*;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Post-processor for Google Test XML reports (JUnit-compatible).  See:
 * http://code.google.com/p/googletest/wiki/AdvancedGuide#Generating_an_XML_Report
 */
public class GoogleTestReportPostProcessor extends JUnitReportPostProcessor
{
    public GoogleTestReportPostProcessor(GoogleTestReportPostProcessorConfiguration config)
    {
        super(config);
    }
}
