package com.zutubi.pulse.core.postprocessors.api;

import static com.zutubi.pulse.core.util.api.XMLStreamUtils.close;
import com.zutubi.util.io.IOUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Helper base class for post processors that find test results in XML files.
 * This class handles XML parsing via the StAX API.
 *
 * @see com.zutubi.pulse.core.util.api.XMLStreamUtils
 */
public abstract class StAXTestReportPostProcessorSupport extends TestReportPostProcessorSupport
{
    protected StAXTestReportPostProcessorSupport(XMLTestReportPostProcessorConfigurationSupport config)
    {
        super(config);
    }

    @Override
    public XMLTestReportPostProcessorConfigurationSupport getConfig()
    {
        return (XMLTestReportPostProcessorConfigurationSupport) super.getConfig();
    }

    protected XMLInputFactory createFactory()
    {
        return XMLInputFactory.newInstance();
    }

    protected void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
    {
        FileInputStream input = null;
        XMLStreamReader reader = null;
        try
        {
            input = new FileInputStream(file);
            XMLInputFactory inputFactory = createFactory();
            reader = inputFactory.createXMLStreamReader(input);

            // scroll forward past the header to the first element
            while (reader.getEventType() != XMLStreamConstants.END_DOCUMENT && !reader.isStartElement())
            {
                reader.next();
            }

            // only trigger the callback if there is something to process.
            if (reader.isStartElement())
            {
                process(reader, tests);
            }
        }
        catch (IOException e)
        {
            handleException(file, ppContext, e);
        }
        catch (XMLStreamException e)
        {
            handleException(file, ppContext, e);
        }
        finally
        {
            IOUtils.close(input);
            close(reader);
        }
    }

    /**
     * Implementations of this class need to implement there processing logic through this method.  The
     * xml test report has been opened for processing and is accessible via the xml stream reader.
     *
     * @param reader    the reader that provides streaming access to the xml report.
     * @param tests     the test result instance in which the extracted test details are persisted.
     *
     * @throws XMLStreamException on error.
     */
    protected abstract void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException;
}
