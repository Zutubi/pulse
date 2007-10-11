package com.zutubi.prototype.type.record;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.XMLUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
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
    private static final String ELEMENT_VALUE = "value";
    private static final String ELEMENT_ARRAY = "array";
    private static final String ELEMENT_ITEM = "item";
    private static final String ATTRIBUTE_NAME = "name";

    public DefaultRecordSerialiser(File baseDirectory)
    {
        this.baseDirectory = baseDirectory;
        if (!baseDirectory.isDirectory())
        {
            baseDirectory.mkdirs();
        }
    }

    public void serialise(String path, Record record, boolean deep)
    {
        File storageDir = getStorageDir(path);
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
                XMLUtils.writeDocument(file, doc, false);
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

    private File getStorageDir(String path)
    {
        path = StringUtils.encodeAndJoin(new Predicate<Character>()
        {
            public boolean satisfied(Character character)
            {
                if(StringUtils.isAsciiAlphaNumeric(character))
                {
                    return true;
                }
                else
                {
                    // A few more likely-used characters
                    switch(character)
                    {
                        case ' ':
                        case '-':
                        case '_':
                        case '.':
                            return true;
                    }
                }

                return false;
            }
        }, File.separatorChar, PathUtils.getPathElements(path));
        return new File(baseDirectory, path);
    }

    private File getRecordFile(File dir)
    {
        return new File(dir, "record.xml");
    }

    private Document recordToDocument(Record record)
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
            Object value = record.get(key);
            if(value instanceof String)
            {
                root.appendChild(createElement(ELEMENT_VALUE, key, (String) value));
            }
            else
            {
                root.appendChild(createElement(ELEMENT_ARRAY, key, (String[])value));
            }
        }

        return doc;
    }

    private Element createElement(String elementName, String key, String value)
    {
        return createElement(elementName, key, new Text(value));
    }

    private Element createElement(String elementName, String key, String[] values)
    {
        return createElement(elementName, key, CollectionUtils.mapToArray(values, new Mapping<String, Element>()
        {
            public Element map(String s)
            {
                Element e = new Element(ELEMENT_ITEM);
                e.appendChild(new Text(s));
                return e;
            }
        }, new Element[values.length]));
    }

    private Element createElement(String elementName, String key, Node... children)
    {
        Element element = new Element(elementName);
        element.addAttribute(new Attribute(ATTRIBUTE_NAME, key));
        for(Node child: children)
        {
            element.appendChild(child);
        }
        return element;
    }

    public MutableRecord deserialise(String path)
    {
        return deserialise(path, new NoopRecordHandler());
    }

    public MutableRecord deserialise(String path, RecordHandler handler)
    {
        File dir = getStorageDir(path);
        if (!dir.isDirectory())
        {
            throw new RecordSerialiseException("No record found at path '" + path + "': directory '" + dir.getAbsolutePath() + "' does not exist");
        }

        return deserialise(dir, handler, "");
    }

    private MutableRecord deserialise(File dir, RecordHandler handler, String path)
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
                String childKey = StringUtils.uriComponentDecode(childDir.getName());
                record.put(childKey, deserialise(childDir, handler, PathUtils.getPath(path, childKey)));
            }

            handler.handle(path, record);
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
            public void handle(String key, Element child)
            {
                record.putMeta(key, XMLUtils.getText(child, ""));
            }
        });

        processChildren(recordFile, root.getChildElements(ELEMENT_VALUE), new ElementHandler()
        {
            public void handle(String key, Element child)
            {
                record.put(key, XMLUtils.getText(child, ""));
            }
        });

        processChildren(recordFile, root.getChildElements(ELEMENT_ARRAY), new ElementHandler()
        {
            public void handle(String key, Element child)
            {
                record.put(key, getItems(child));
            }
        });

        return record;
    }

    private String[] getItems(Element element)
    {
        Elements items = element.getChildElements(ELEMENT_ITEM);
        String[] result = new String[items.size()];
        for(int i = 0; i < items.size(); i++)
        {
            result[i] = XMLUtils.getText(items.get(i), "");
        }
        return result;
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

            handler.handle(key, child);
        }
    }

    private interface ElementHandler
    {
        void handle(String key, Element child);
    }

    private class SubrecordDirFileFilter implements FileFilter
    {
        public boolean accept(File pathname)
        {
            return pathname.isDirectory();
        }
    }
}
