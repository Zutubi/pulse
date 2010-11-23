package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Text;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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

    /**
     * Reads all features stored for file artifacts captured for the given
     * command result.  Limits may be applied both on a per-file artifact and
     * total basis, as features are loaded into memory.
     *  
     * @param result               command to load the features into
     * @param recipeDir            directory where the recipe result containing
     *                             the command is stored
     * @param perFileArtifactLimit maximum number of features to load for a
     *                             single file artifact
     * @param totalLimit           maximum number of features to load in total
     *                             (i.e. across all file artifacts)
     * @throws IOException         on error reading the features file
     * @throws XMLStreamException  if the features file is badly formed
     */
    public void readFeatures(CommandResult result, File recipeDir, int perFileArtifactLimit, int totalLimit) throws IOException, XMLStreamException
    {
        File featuresFile = getFeaturesFile(result, recipeDir, false);
        if (featuresFile.exists())
        {
            InputStream input = null;
            try
            {
                input = new FileInputStream(featuresFile);
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                XMLStreamReader reader = inputFactory.createXMLStreamReader(input);
                while (reader.getEventType() != XMLStreamConstants.END_DOCUMENT && !reader.isStartElement())
                {
                    reader.next();
                }

                // only trigger the callback if there is something to process.
                if (reader.isStartElement())
                {
                    readArtifacts(reader, result, perFileArtifactLimit, totalLimit);
                }
            }
            finally
            {
                IOUtils.close(input);
            }
        }
    }

    private int readArtifacts(XMLStreamReader reader, CommandResult result, int perFileArtifactLimit, int totalLimit) throws XMLStreamException
    {
        int featuresRead = 0;
        
        nextTagOrEnd(reader);
        while (nextSiblingTag(reader, ELEMENT_ARTIFACT))
        {
            Map<String,String> attributes = getAttributes(reader);
            String name = attributes.get(ATTRIBUTE_NAME);
            if (name != null)
            {
                StoredArtifact artifact = result.getArtifact(name);
                if(artifact == null)
                {
                    LOG.warning("Features file refers to unknown artifact '" + name + "'");
                    continue;
                }

                featuresRead += readArtifact(reader, artifact, Math.min(perFileArtifactLimit, totalLimit - featuresRead), totalLimit - featuresRead);
                if (featuresRead >= totalLimit)
                {
                    return featuresRead;
                }
            }

            expectEndTag(ELEMENT_ARTIFACT, reader);
            nextTagOrEnd(reader);
        }

        return featuresRead;
    }

    private int readArtifact(XMLStreamReader reader, StoredArtifact artifact, int perFileArtifactLimit, int totalLimit) throws XMLStreamException
    {
        int featuresRead = 0;

        nextTagOrEnd(reader);
        while (nextSiblingTag(reader, ELEMENT_FILE_ARTIFACT))
        {
            Map<String,String> attributes = getAttributes(reader);
            String path = attributes.get(ATTRIBUTE_PATH);
            if (path != null)
            {
                StoredFileArtifact fileArtifact = artifact.findFile(path);
                if (fileArtifact == null)
                {
                    LOG.warning("Features file refers to unknown file artifact '" + path + "'");
                    continue;
                }

                featuresRead += readFileArtifact(reader, fileArtifact, Math.min(perFileArtifactLimit, totalLimit - featuresRead));
                if (featuresRead >= totalLimit)
                {
                    return featuresRead;
                }
            }

            expectEndTag(ELEMENT_FILE_ARTIFACT, reader);
            nextTagOrEnd(reader);
        }

        return featuresRead;
    }

    private int readFileArtifact(XMLStreamReader reader, StoredFileArtifact fileArtifact, int limit) throws XMLStreamException
    {
        int featuresRead = 0;
        
        nextTagOrEnd(reader);
        while (nextSiblingTag(reader, ELEMENT_FEATURE))
        {
            if (featuresRead < limit)
            {
                try
                {
                    Map<String, String> attributes = getAttributes(reader);
                    Feature.Level level = Feature.Level.valueOf(getRequiredAttribute(attributes, ATTRIBUTE_LEVEL));
                    long firstLine = Long.parseLong(getRequiredAttribute(attributes, ATTRIBUTE_FIRST_LINE));
                    long lastLine = Long.parseLong(getRequiredAttribute(attributes, ATTRIBUTE_LAST_LINE));
                    long line = Long.parseLong(getRequiredAttribute(attributes, ATTRIBUTE_LINE));
                    String summary = reader.getElementText();

                    fileArtifact.addFeature(new PersistentPlainFeature(level, summary, firstLine, lastLine, line));
                    featuresRead++;
                }
                catch (IllegalArgumentException e)
                {
                    LOG.warning(e);
                }
            }
            else
            {
                nextTagOrEnd(reader);
            }
            
            expectEndTag(ELEMENT_FEATURE, reader);
            nextTagOrEnd(reader);
        }

        return featuresRead;
    }

    private String getRequiredAttribute(Map<String, String> attributes, String name)
    {
        String value = attributes.get(name);
        if (value == null)
        {
            throw new IllegalArgumentException("Required attribute '" + name + "' not found");
        }

        return value;
    }
}
