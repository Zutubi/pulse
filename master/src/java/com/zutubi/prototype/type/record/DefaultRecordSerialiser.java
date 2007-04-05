package com.zutubi.prototype.type.record;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.XMLUtils;
import com.zutubi.pulse.util.logging.Logger;
import nu.xom.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Used to (de)serialise records to permanent storage.
 */
public class DefaultRecordSerialiser implements RecordSerialiser
{
    private static final Logger LOG = Logger.getLogger(DefaultRecordSerialiser.class);
    private static final DateFormat FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

    private File baseDirectory;
    private static final String ELEMENT_RECORD = "record";
    private static final String ELEMENT_META = "meta";
    private static final String ELEMENT_DATA = "data";
    private static final String ATTRIBUTE_NAME = "name";

    public DefaultRecordSerialiser(File baseDirectory)
    {
        this.baseDirectory = baseDirectory;
        if (!baseDirectory.isDirectory())
        {
            baseDirectory.mkdirs();
        }
    }

    public void serialise(String path, MutableRecord record, boolean deep)
    {
        File storageDir = new File(baseDirectory, path);
        if (!storageDir.isDirectory())
        {
            if (!storageDir.mkdir())
            {
                // Only make one level: we want to fail fast if someone
                // requests nesting a record under a parent that does not
                // exist.
                throw new RecordSerialiseException("Could not create destination directory '" + storageDir.getAbsolutePath() + "'");
            }
        }

        if (record.metaKeySet().size() > 0 || record.simpleKeySet().size() > 0)
        {
            Document doc = recordToDocument(record);
            File file = getRecordFile(storageDir);
            try
            {
                XMLUtils.writeDocument(file, doc, true);
            }
            catch (IOException e)
            {
                throw new RecordSerialiseException(e);
            }
        }

        if (deep)
        {
            // Clear out any existing child record directories.
            File[] childDirs = storageDir.listFiles(new SubrecordDirFileFilter());
            for (File childDir : childDirs)
            {
                FileSystemUtils.rmdir(childDir);
            }

            for (String key : record.keySet())
            {
                Object value = record.get(key);
                if (value instanceof Record)
                {
                    serialise(PathUtils.getPath(path, key), (MutableRecord) value, deep);
                }
            }
        }
    }

    private File getRecordFile(File dir)
    {
        return new File(dir, "record.xml");
    }

    private Document recordToDocument(MutableRecord record)
    {
        Element root = new Element(ELEMENT_RECORD);
        Document doc = new Document(root);
        root.appendChild(new Comment("Stored by Pulse at " + FORMAT.format(new Date())));

        for (String key : record.metaKeySet())
        {
            root.appendChild(createElement(ELEMENT_META, key, record.getMeta(key)));
        }

        for (String key : record.simpleKeySet())
        {
            root.appendChild(createElement(ELEMENT_DATA, key, (String) record.get(key)));
        }

        return doc;
    }

    private Element createElement(String elementName, String key, String value)
    {
        Element element = new Element(elementName);
        element.addAttribute(new Attribute(ATTRIBUTE_NAME, key));
        element.appendChild(new Text(value));
        return element;
    }

    public MutableRecord deserialise(String path)
    {
        File dir = new File(baseDirectory, path);
        if (!dir.isDirectory())
        {
            throw new RecordSerialiseException("No record found at path '" + path + "': directory '" + dir.getAbsolutePath() + "' does not exist");
        }

        return deserialise(dir);
    }

    public void delete(String path) throws RecordSerialiseException
    {
        File dir = new File(baseDirectory, path);
        FileSystemUtils.rmdir(dir);
    }

    private MutableRecord deserialise(File dir)
    {
        try
        {
            MutableRecord record;
            File recordFile = getRecordFile(dir);
            if (recordFile.exists())
            {
                Document doc = XMLUtils.readDocument(recordFile);
                record = documentToRecord(recordFile, doc);
            }
            else
            {
                // This happens for collections
                record = new MutableRecordImpl();
            }

            for (File childDir : dir.listFiles(new SubrecordDirFileFilter()))
            {
                record.put(childDir.getName(), deserialise(childDir));
            }

            return record;
        }
        catch (Exception e)
        {
            throw new RecordSerialiseException("Unable to parse record file: " + e.getMessage(), e);
        }
    }

    private MutableRecord documentToRecord(File recordFile, Document doc)
    {
        final MutableRecord record = new MutableRecordImpl();
        Element root = doc.getRootElement();

        processChildren(recordFile, root.getChildElements(ELEMENT_META), new ElementHandler()
        {
            public void handle(String key, String value)
            {
                record.putMeta(key, value);
            }
        });

        processChildren(recordFile, root.getChildElements(ELEMENT_DATA), new ElementHandler()
        {
            public void handle(String key, String value)
            {
                record.put(key, value);
            }
        });

        return record;
    }

    private void processChildren(File recordFile, Elements children, ElementHandler handler)
    {
        for (int i = 0; i < children.size(); i++)
        {
            Element child = children.get(i);
            String key = child.getAttributeValue(ATTRIBUTE_NAME);
            if (key == null)
            {
                LOG.warning("Ignoring '" + child.getLocalName() + "' with no '" + ATTRIBUTE_NAME + "' attribute in file '" + recordFile.getAbsolutePath() + "'");
            }

            handler.handle(key, XMLUtils.getText(child, ""));
        }
    }

    private interface ElementHandler
    {
        void handle(String key, String value);
    }

    private class SubrecordDirFileFilter implements FileFilter
    {
        public boolean accept(File pathname)
        {
            return pathname.isDirectory();
        }
    }
}
