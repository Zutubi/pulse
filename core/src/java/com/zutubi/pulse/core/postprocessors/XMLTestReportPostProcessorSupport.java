package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.util.IOUtils;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 */
public abstract class XMLTestReportPostProcessorSupport extends TestReportPostProcessorSupport
{
    private String reportType;

    protected XMLTestReportPostProcessorSupport(String reportType)
    {
        this.reportType = reportType;
    }

    protected void process(File file, TestSuiteResult suite, PostProcessorContext ppContext)
    {
        FileInputStream input = null;

        try
        {
            input = new FileInputStream(file);
            Builder builder = createBuilder();
            Document doc;
            doc = builder.build(input);
            processDocument(doc, suite);
        }
        catch (ParsingException pex)
        {
            String message = "Unable to parse " + reportType + " report '" + file.getAbsolutePath() + "'";
            if(pex.getMessage() != null)
            {
                message += ": " + pex.getMessage();
            }

            ppContext.addFeatureToCommand(new Feature(Feature.Level.WARNING, message));
        }
        catch (IOException e)
        {
            String message = "I/O error processing " + reportType + " report '" + file.getAbsolutePath() + "'";
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

    protected abstract void processDocument(Document doc, TestSuiteResult tests);
}
