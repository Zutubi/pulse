package com.zutubi.tove.type.record;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import nu.xom.*;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;

/**
 * A record serialiser that takes a record and writes it to file.
 * Nested records are written to a single file.
 */
public class XmlRecordSerialiser
{
    private static final Logger LOG = Logger.getLogger(XmlRecordSerialiser.class);
    private static final DateFormat FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

    private static final String ELEMENT_RECORD = "record";
    private static final String ELEMENT_META = "meta";
    private static final String ELEMENT_VALUE = "value";
    private static final String ELEMENT_ARRAY = "array";
    private static final String ELEMENT_ITEM = "item";
    private static final String ATTRIBUTE_NAME = "name";

    /**
     * Serialise a record to a file.
     * @param file      the file to which the serialised record will be writen
     * @param record    the record being serialised.
     * @param deep      if true, the full record will be serialised. If false, will not serialise nested records.
     * @throws RecordSerialiseException if there is a problem serialising the record.
     */
    public void serialise(File file, Record record, boolean deep) throws RecordSerialiseException
    {
        Document doc = recordToDocument(record, deep);
        try
        {
            writeDocument(file, doc, false);
        }
        catch (IOException e)
        {
            throw new RecordSerialiseException(e);
        }
    }

    // read the record from the specified file.
    public MutableRecord deserialise(File file) throws RecordSerialiseException
    {
        try
        {
            MutableRecord record;
            if (file.exists())
            {
                Document doc = readDocument(file);
                record = documentToRecord(file, doc);
            }
            else
            {
                // This happens for collections
                record = new MutableRecordImpl();
            }

            return record;
        }
        catch (Exception e)
        {
            throw new RecordSerialiseException("Unable to parse record file '" + file.getAbsolutePath() + "': " + e.getMessage(), e);
        }
    }

    private Document recordToDocument(Record record, boolean deep)
    {
        Element root = new Element(ELEMENT_RECORD);
        root.appendChild(new Comment("Stored by Pulse at " + FORMAT.format(new Date())));
        
        Document doc = new Document(root);

        recordToDocument(record, root, deep);

        return doc;
    }

    private void recordToDocument(Record record, Element root, boolean deep)
    {
        if (record != null)
        {
            for (String key : record.metaKeySet())
            {
                root.appendChild(createElement(ELEMENT_META, key, record.getMeta(key)));
            }

            for (String key : record.simpleKeySet())
            {
                Object value = record.get(key);
                if (value instanceof String)
                {
                    root.appendChild(createElement(ELEMENT_VALUE, key, (String) value));
                }
                else
                {
                    root.appendChild(createElement(ELEMENT_ARRAY, key, (String[]) value));
                }
            }

            if (deep)
            {
                for (String key : record.nestedKeySet())
                {
                    Record nested = (Record) record.get(key);
                    Element nestedElement = createElement(ELEMENT_RECORD, key);
                    root.appendChild(nestedElement);
                    recordToDocument(nested, nestedElement, deep);
                }
            }
        }
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
        for (Node child : children)
        {
            element.appendChild(child);
        }
        return element;
    }

    private MutableRecord documentToRecord(File recordFile, Document doc)
    {
        final MutableRecord record = new MutableRecordImpl();
        Element root = doc.getRootElement();

        return documentToRecord(recordFile, record, root);
    }

    private MutableRecord documentToRecord(final File recordFile, final MutableRecord record, final Element root)
    {
        processChildren(recordFile, root.getChildElements(ELEMENT_META), new ElementHandler()
        {
            public void handle(String key, Element child)
            {
                record.putMeta(key, getText(child, ""));
            }
        });

        processChildren(recordFile, root.getChildElements(ELEMENT_VALUE), new ElementHandler()
        {
            public void handle(String key, Element child)
            {
                record.put(key, getText(child, ""));
            }
        });

        processChildren(recordFile, root.getChildElements(ELEMENT_ARRAY), new ElementHandler()
        {
            public void handle(String key, Element child)
            {
                record.put(key, getItems(child));
            }
        });

        processChildren(recordFile, root.getChildElements(ELEMENT_RECORD), new ElementHandler()
        {
            public void handle(String key, Element child)
            {
                MutableRecord childRecord = new MutableRecordImpl();
                record.put(key, documentToRecord(recordFile, childRecord, child));
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

            handler.handle(key, child);
        }
    }

    private String[] getItems(Element element)
    {
        Elements items = element.getChildElements(ELEMENT_ITEM);
        String[] result = new String[items.size()];
        for (int i = 0; i < items.size(); i++)
        {
            result[i] = getText(items.get(i), "");
        }
        return result;
    }

    private void writeDocument(File file, Document doc, boolean prettyPrint) throws IOException
    {
        BufferedOutputStream bos = null;
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            Serializer serializer = new Serializer(bos);
            if(prettyPrint)
            {
                serializer.setIndent(4);
            }
            serializer.write(doc);
        }
        finally
        {
            IOUtils.close(bos);
        }
    }

    private String getText(Element element, String defaultValue)
    {
        return getText(element, defaultValue, true);
    }

    private String getText(Element element, String defaultValue, boolean trim)
    {
        if (element.getChildCount() > 0)
        {
            Node child = element.getChild(0);
            if(child != null && child instanceof Text)
            {
                String value = child.getValue();
                if(trim)
                {
                    value = value.trim();
                }

                return value;
            }
        }

        return defaultValue;
    }

    private Document readDocument(File file) throws IOException, ParsingException
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(file);
            Builder builder = new Builder();
            return builder.build(is);
        }
        finally
        {
            IOUtils.close(is);
        }
    }

    private interface ElementHandler
    {
        void handle(String key, Element child);
    }
}
