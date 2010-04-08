package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.util.logging.Logger;
import nu.xom.*;

import java.io.File;
import java.io.IOException;

/**
 * A FeaturePersister is used to read/write artifact feature information
 * from/to disk.  Note that result features are still stored in the database.
 */
public class FeaturePersister
{
    private static final Logger LOG = Logger.getLogger(FeaturePersister.class);

    private static final String ELEMENT_ARTIFACTS = "artifacts";
    private static final String ELEMENT_ARTIFACT = "artifact";
    private static final String ELEMENT_FILE_ARTIFACT = "file-artifact";
    private static final String ELEMENT_FEATURE = "feature";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_PATH = "path";
    private static final String ATTRIBUTE_LEVEL = "level";
    private static final String ATTRIBUTE_FIRST_LINE = "first-line";
    private static final String ATTRIBUTE_LAST_LINE = "last-line";
    private static final String ATTRIBUTE_LINE = "line";


    public void writeFeatures(CommandResult result, File recipeDir) throws IOException
    {
        File featuresFile = getFeaturesFile(result, recipeDir, true);
        Element root = new Element(ELEMENT_ARTIFACTS);
        Document doc = new Document(root);
        addArtifacts(root, result);
        XMLUtils.writeDocument(featuresFile, doc);
    }

    private File getFeaturesFile(CommandResult result, File recipeDir, boolean ensureDir) throws IOException
    {
        File featuresDir = getFeaturesDirectory(recipeDir);
        if (ensureDir && !featuresDir.exists() && !featuresDir.mkdirs())
        {
            throw new IOException("Failed to create new directory: '" + featuresDir.getAbsolutePath() + "'");
        }

        return new File(featuresDir, result.getCommandName() + ".xml");
    }

    public static File getFeaturesDirectory(File recipeDir)
    {
        return new File(recipeDir, "features");
    }

    private void addArtifacts(Element root, CommandResult result)
    {
        for (StoredArtifact a : result.getArtifacts())
        {
            Element element = new Element(ELEMENT_ARTIFACT);
            element.addAttribute(new Attribute(ATTRIBUTE_NAME, a.getName()));
            addFiles(element, a);

            if (element.getChildCount() > 0)
            {
                root.appendChild(element);
            }
        }
    }

    private void addFiles(Element parent, StoredArtifact artifact)
    {
        for (StoredFileArtifact fa : artifact.getChildren())
        {
            if (fa.hasFeatures())
            {
                Element element = new Element(ELEMENT_FILE_ARTIFACT);
                element.addAttribute(new Attribute(ATTRIBUTE_PATH, fa.getPath()));
                addFeatures(element, fa);
                parent.appendChild(element);
            }
        }
    }

    private void addFeatures(Element parent, StoredFileArtifact fileArtifact)
    {
        for(PersistentFeature f: fileArtifact.getFeatures())
        {
            PersistentPlainFeature pf = (PersistentPlainFeature) f;
            Element element = new Element(ELEMENT_FEATURE);
            element.addAttribute(new Attribute(ATTRIBUTE_LEVEL, pf.getLevel().toString()));
            element.addAttribute(new Attribute(ATTRIBUTE_FIRST_LINE, Long.toString(pf.getFirstLine())));
            element.addAttribute(new Attribute(ATTRIBUTE_LAST_LINE, Long.toString(pf.getLastLine())));
            element.addAttribute(new Attribute(ATTRIBUTE_LINE, Long.toString(pf.getLineNumber())));
            element.appendChild(new Text(XMLUtils.removeIllegalCharacters(pf.getSummary())));
            parent.appendChild(element);
        }
    }

    public void readFeatures(CommandResult result, File recipeDir) throws ParsingException, IOException
    {
        File featuresFile = getFeaturesFile(result, recipeDir, false);
        Builder builder = new Builder();
        if (featuresFile.exists())
        {
            Document doc = builder.build(featuresFile);
            readArtifacts(doc.getRootElement(), result);
        }
    }

    private void readArtifacts(Element root, CommandResult result)
    {
        Elements children = root.getChildElements(ELEMENT_ARTIFACT);
        for(int i = 0; i < children.size(); i++)
        {
            Element element = children.get(i);
            String name = element.getAttributeValue(ATTRIBUTE_NAME);
            if(name != null)
            {
                StoredArtifact artifact = result.getArtifact(name);
                if(artifact == null)
                {
                    LOG.warning("Features file refers to unknown artifact '" + name + "'");
                    continue;
                }

                readArtifact(element, artifact);
            }
        }
    }

    private void readArtifact(Element artifactElement, StoredArtifact artifact)
    {
        Elements children = artifactElement.getChildElements(ELEMENT_FILE_ARTIFACT);
        for(int i = 0; i < children.size(); i++)
        {
            Element element = children.get(i);
            String path = element.getAttributeValue(ATTRIBUTE_PATH);
            if(path != null)
            {
                StoredFileArtifact fileArtifact = artifact.findFile(path);
                if(fileArtifact == null)
                {
                    LOG.warning("Features file refers to unknown file artifact '" + path + "'");
                    continue;
                }

                readFileArtifact(element, fileArtifact);
            }
        }
    }

    private void readFileArtifact(Element fileArtifactElement, StoredFileArtifact fileArtifact)
    {
        Elements children = fileArtifactElement.getChildElements(ELEMENT_FEATURE);
        for(int i = 0; i < children.size(); i++)
        {
            Element element = children.get(i);
            try
            {
                Feature.Level level = Feature.Level.valueOf(getRequiredAttribute(element, ATTRIBUTE_LEVEL));
                long firstLine = Long.parseLong(getRequiredAttribute(element, ATTRIBUTE_FIRST_LINE));
                long lastLine = Long.parseLong(getRequiredAttribute(element, ATTRIBUTE_LAST_LINE));
                long line = Long.parseLong(getRequiredAttribute(element, ATTRIBUTE_LINE));
                String summary = XMLUtils.getText(element, null, false);

                fileArtifact.addFeature(new PersistentPlainFeature(level, summary, firstLine, lastLine, line));
            }
            catch (IllegalArgumentException e)
            {
                LOG.warning(e);
            }
        }
    }

    private String getRequiredAttribute(Element element, String name)
    {
        String value = element.getAttributeValue(name);
        if(value == null)
        {
            throw new IllegalArgumentException("Required attribute '" + name + "' not found");
        }

        return value;
    }
}
