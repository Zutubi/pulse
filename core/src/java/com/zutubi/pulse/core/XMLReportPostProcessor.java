package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.util.IOUtils;
import nu.xom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 */
public abstract class XMLReportPostProcessor extends TestReportPostProcessor
{
    private String reportType;

    protected XMLReportPostProcessor(String reportType)
    {
        this.reportType = reportType;
    }

    protected void internalProcess(StoredFileArtifact artifact, CommandResult result, CommandContext context)
    {
        File file = new File(context.getOutputDir(), artifact.getPath());
        FileInputStream input = null;

        try
        {
            input = new FileInputStream(file);
            Builder builder = new Builder();
            Document doc;
            doc = builder.build(input);
            processDocument(doc, context.getTestResults());
        }
        catch (ParsingException pex)
        {
            String message = "Unable to parse " + reportType + " report '" + file.getAbsolutePath() + "'";
            if(pex.getMessage() != null)
            {
                message += ": " + pex.getMessage();
            }

            result.warning(message);
        }
        catch (IOException e)
        {
            String message = "I/O error processing " + reportType + " report '" + file.getAbsolutePath() + "'";
            if(e.getMessage() != null)
            {
                message += ": " + e.getMessage();
            }
            result.warning(message);
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    protected String getText(Element element)
    {
        if (element.getChildCount() > 0)
        {
            Node child = element.getChild(0);
            if(child != null && child instanceof Text)
            {
                return child.getValue().trim();
            }
        }

        return null;
    }

    protected abstract void processDocument(Document doc, TestSuiteResult tests);

}
