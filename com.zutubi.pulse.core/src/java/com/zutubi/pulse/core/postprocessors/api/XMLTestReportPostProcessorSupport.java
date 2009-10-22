package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.Feature;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.close;
import com.zutubi.util.io.IOUtils;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper base class for post processors that find test results in XML files.
 * This class handles XML parsing, providing support for Dom and Stax.  You
 * can select between the XML parsing APIs by using the appropriate callbacks
 * for the process method.
 *
 * Typical usage of this class involves overriding the {@link #extractTestResults(java.io.File, PostProcessorContext, TestSuiteResult)}
 * method and calling the appropriate process method depending on which of the
 * XML parsing APIs you intend to use.
 *
 * @see #process(java.io.File, PostProcessorContext, TestSuiteResult, com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorSupport.DomCallback) 
 * @see #process(java.io.File, PostProcessorContext, TestSuiteResult, com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorSupport.XMLStreamCallback)
 * @see com.zutubi.pulse.core.util.api.XMLUtils
 */
public abstract class XMLTestReportPostProcessorSupport extends TestReportPostProcessorSupport
{
    protected XMLTestReportPostProcessorSupport(XMLTestReportPostProcessorConfigurationSupport config)
    {
        super(config);
    }

    @Override
    public XMLTestReportPostProcessorConfigurationSupport getConfig()
    {
        return (XMLTestReportPostProcessorConfigurationSupport) super.getConfig();
    }

    protected Builder createBuilder()
    {
        return new Builder();
    }

    protected XMLInputFactory createFactory()
    {
        return XMLInputFactory.newInstance();
    }

    /**
     * Utility method that handles parsing the specified file into a DOM tree which is then
     * passed on to the callback.
     *
     * @param file          the file handle to the xml file.
     * @param ppContext     the post processor context for this execution.
     * @param tests         the tests instance that is used to record the extracted test data.
     * @param callback      the callback to which the parsed DOM tree is passed for processing.
     */
    protected void process(File file, PostProcessorContext ppContext, TestSuiteResult tests, DomCallback callback)
    {
        Document doc = null;
        InputStream input = null;
        try
        {
            input = new FileInputStream(file);
            doc = createBuilder().build(input);
        }
        catch (IOException e)
        {
            handleException(file, ppContext, e);
            return;
        }
        catch (ValidityException e)
        {
            handleException(file, ppContext, e);
            return;
        }
        catch (ParsingException e)
        {
            String message = "Unable to parse " + getConfig().reportType() + " report '" + file.getAbsolutePath() + "'";
            handleException(message, ppContext, e);
            return;
        }
        finally
        {
            IOUtils.close(input);
        }

        // We close the input stream before we trigger the callback so that we minimise the
        // length of time we are holding on to it.

        callback.process(doc, tests);
    }

    /**
     * Utility method that handles setting up the stax resources which are then
     * passed on to the callback.
     *
     * @param file          the file handle to the xml file.
     * @param ppContext     the post processor context for this execution.
     * @param tests         the tests instance that is used to record the extracted test data.
     * @param callback      the callback to which the xml stream reader is passed for processing.
     */
    protected void process(File file, PostProcessorContext ppContext, TestSuiteResult tests, XMLStreamCallback callback)
    {
        FileInputStream input = null;
        XMLStreamReader reader = null;
        try
        {
            input = new FileInputStream(file);
            XMLInputFactory inputFactory = createFactory();
            reader = inputFactory.createXMLStreamReader(input);

            callback.process(reader, tests);
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

    private void handleException(File file, PostProcessorContext ppContext, Exception e)
    {
        String message = e.getClass().getName() + " processing " + getConfig().reportType() + " report '" + file.getAbsolutePath() + "'";
        handleException(message, ppContext, e);
    }

    private void handleException(String message, PostProcessorContext ppContext, Exception e)
    {
        if(e.getMessage() != null)
        {
            message += ": " + e.getMessage();
        }
        ppContext.addFeatureToCommand(new Feature(Feature.Level.WARNING, message));
    }

    /**
     * Implement this callback if you wish to use the DOM XML API.
     *
     * Note that DOM reads the entire file into memory.  If the file is large, consider
     * using the streaming api.
     */
    protected interface DomCallback
    {
        /**
         * This method is called with a parsed DOM document instance representing the
         * xml contents of the file being processed.  Test result data that is extracted
         * from the DOM can be persisted by updating the TestSuiteResult instance.
         *
         * @param doc       the parsed DOM tree of the xml file.
         * @param tests     the tests result instance used to persist the extracted data.
         */
        void process(Document doc, TestSuiteResult tests);
    }

    /**
     * Implement this callback if you which to use the Stax XML API.
     */
    protected interface XMLStreamCallback
    {
        /**
         * This method is called with an XMLStreamReader instance configured to read
         * from the file being processed.  Test result data that is extracted
         * from the DOM can be persisted by updating the TestSuiteResult instance.
         *
         * Note that for as long as this method is active, the file will remain open.
         *
         * @param reader    the xml stream reader that provides streaming access to the xml file.
         * @param tests     the tests result instance used to persist the extracted data.
         *
         * @throws XMLStreamException   on error
         */
        void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException;
    }
}
