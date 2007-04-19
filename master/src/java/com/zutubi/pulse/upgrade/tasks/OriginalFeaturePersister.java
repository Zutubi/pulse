package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.util.XMLUtils;
import com.zutubi.util.logging.Logger;
import nu.xom.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * A FeaturePersister is used to read/write artifact feature information
 * from/to disk.  Note that result features are still stored in the database.
 */
public class OriginalFeaturePersister
{
    private static final Logger LOG = Logger.getLogger(OriginalFeaturePersister.class);

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

    static class CommandResult
    {
        private String commandName;
        private List<StoredArtifact> artifacts = new LinkedList<StoredArtifact>();

        public CommandResult(String name)
        {
            this.commandName = name;
        }

        public String getCommandName()
        {
            return commandName;
        }

        public List<StoredArtifact> getArtifacts()
        {
            return artifacts;
        }

        public StoredArtifact getArtifact(String name)
        {
            for(StoredArtifact a: artifacts)
            {
                if(name.equals(a.getName()))
                {
                    return a;
                }
            }

            return null;
        }

        public void addArtifact(StoredArtifact storedArtifact)
        {
            artifacts.add(storedArtifact);
        }
    }

    static class StoredArtifact
    {
        private String name;
        private List<StoredFileArtifact> children = new LinkedList<StoredFileArtifact>();

        public StoredArtifact(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public List<StoredFileArtifact> getChildren()
        {
            return children;
        }

        public StoredFileArtifact findFile(String path)
        {
            for(StoredFileArtifact a: children)
            {
                if(a.getPath().equals(path))
                {
                    return a;
                }
            }

            return null;
        }

        public void addFile(StoredFileArtifact fileArtifact)
        {
            children.add(fileArtifact);
        }
    }

    static class StoredFileArtifact
    {
        private String path;
        private List<PlainFeature> features = new LinkedList<PlainFeature>();

        public StoredFileArtifact(String path)
        {
            this.path = path;
        }

        public String getPath()
        {
            return path;
        }

        public List<PlainFeature> getFeatures()
        {
            return features;
        }

        public boolean hasFeatures()
        {
            return features.size() > 0;
        }

        public void addFeature(PlainFeature plainFeature)
        {
            features.add(plainFeature);
        }
    }

    static class PlainFeature
    {
        private Feature.Level level;
        private String summary;
        private long firstLine;
        private long lastLine;
        private long lineNumber;

        public PlainFeature(Feature.Level level, String summary, long firstLine, long lastLine, long line)
        {
            this.level = level;
            this.summary = summary;
            this.firstLine = firstLine;
            this.lastLine = lastLine;
            this.lineNumber = line;
        }

        public Feature.Level getLevel()
        {
            return level;
        }

        public String getSummary()
        {
            return summary;
        }

        public long getFirstLine()
        {
            return firstLine;
        }

        public long getLastLine()
        {
            return lastLine;
        }

        public long getLineNumber()
        {
            return lineNumber;
        }
    }

    public void writeFeatures(CommandResult result, File recipeDir) throws IOException
    {
        File featuresFile = getFeaturesFile(result, recipeDir);
        Element root = new Element(ELEMENT_ARTIFACTS);
        Document doc = new Document(root);
        addArtifacts(root, result);
        XMLUtils.writeDocument(featuresFile, doc);
    }

    private File getFeaturesFile(CommandResult result, File recipeDir)
    {
        File featuresDir = new File(recipeDir, "features");
        if(!featuresDir.exists())
        {
            featuresDir.mkdir();
        }

        return new File(featuresDir, result.getCommandName() + ".xml");
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
        for(PlainFeature pf: fileArtifact.getFeatures())
        {
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
        File featuresFile = getFeaturesFile(result, recipeDir);
        Builder builder = new Builder();
        Document doc = builder.build(featuresFile);
        readArtifacts(doc.getRootElement(), result);
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
                String summary = XMLUtils.getText(element);

                fileArtifact.addFeature(new PlainFeature(level, summary, firstLine, lastLine, line));
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
