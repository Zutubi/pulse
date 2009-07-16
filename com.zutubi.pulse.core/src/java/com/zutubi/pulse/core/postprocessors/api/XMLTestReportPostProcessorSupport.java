package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.util.io.IOUtils;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Helper base class for post processors that find test results in XML files.
 * This class handles XML parsing, passing in a document for implementations
 * to walk.  <a href="http://www.xom.nu/">XOM</a> is used for parsing as it
 * has a convenient document API.
 *
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

    protected final void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
    {
        FileInputStream input = null;

        try
        {
            input = new FileInputStream(file);
            Builder builder = createBuilder();
            Document doc;
            doc = builder.build(input);
            processDocument(doc, tests);
        }
        catch (ParsingException pex)
        {
            String message = "Unable to parse " + getConfig().reportType() + " report '" + file.getAbsolutePath() + "'";
            if(pex.getMessage() != null)
            {
                message += ": " + pex.getMessage();
            }

            ppContext.addFeatureToCommand(new Feature(Feature.Level.WARNING, message));
        }
        catch (IOException e)
        {
            String message = "I/O error processing " + getConfig().reportType() + " report '" + file.getAbsolutePath() + "'";
            if(e.getMessage() != null)
            {
                message += ": " + e.getMessage();
            }
            ppContext.addFeatureToCommand(new Feature(Feature.Level.WARNING, message));
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    protected Builder createBuilder()
    {
        return new Builder();
    }

    /**
     * Called once for each XML document to be processed by this processor to
     * find test results.  Discovered results should be added to the given
     * suite.  Nested suites are supported.
     *
     * @param doc   XML document to post process
     * @param tests suite to add all discovered test results to
     */
    protected abstract void processDocument(Document doc, TestSuiteResult tests);
}
