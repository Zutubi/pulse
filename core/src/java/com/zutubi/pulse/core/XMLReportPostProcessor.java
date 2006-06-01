package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.util.IOUtils;
import nu.xom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 */
public abstract class XMLReportPostProcessor implements PostProcessor
{
    private String reportType;
    private String name;

    protected XMLReportPostProcessor(String reportType)
    {
        this.reportType = reportType;
    }

    public void process(File outputDir, StoredFileArtifact artifact, CommandResult result)
    {
        File file = new File(outputDir, artifact.getPath());
        FileInputStream input = null;

        try
        {
            input = new FileInputStream(file);
            Builder builder = new Builder();
            Document doc;
            doc = builder.build(input);
            processDocument(doc, artifact);
        }
        catch (ParsingException pex)
        {
            throw new BuildException("Unable to parse " + reportType + " report '" + file.getAbsolutePath() + "': " + pex.getMessage());
        }
        catch (IOException e)
        {
            throw new BuildException("I/O error processing " + reportType + " report '" + file.getAbsolutePath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return this;
    }

    protected String getText(Element element)
    {
        Node child = element.getChild(0);
        if(child != null && child instanceof Text)
        {
            return child.getValue().trim();
        }

        return null;
    }

    protected abstract void processDocument(Document doc, StoredFileArtifact artifact);

}
