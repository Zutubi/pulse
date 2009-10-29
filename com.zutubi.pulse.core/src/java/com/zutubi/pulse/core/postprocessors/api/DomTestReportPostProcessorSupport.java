package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.util.io.IOUtils;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper base class for post processors that find test results in XML files.
 * This class handles XML parsing via the DOM API.
 *
 * @see com.zutubi.pulse.core.util.api.XMLUtils
 */
public abstract class DomTestReportPostProcessorSupport extends TestReportPostProcessorSupport
{
    protected DomTestReportPostProcessorSupport(XMLTestReportPostProcessorConfigurationSupport config)
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

    protected void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
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
        process(doc, tests);
    }

    /**
     * Implementations of this class need to implement there processing logic through this method.  The
     * xml test report has already been parsed and is available in the form of a dom tree.
     *
     * @param doc   the dom tree of the parsed xml report
     * @param tests the test result instance in which the extracted test details are persisted.
     */
    protected abstract void process(Document doc, TestSuiteResult tests);
}
