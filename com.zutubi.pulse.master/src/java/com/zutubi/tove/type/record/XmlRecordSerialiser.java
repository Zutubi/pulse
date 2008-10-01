package com.zutubi.tove.type.record;

import com.zutubi.pulse.core.util.XMLUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.logging.Logger;
import nu.xom.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 *
 */
public class XmlRecordSerialiser
{
    private static final Logger LOG = Logger.getLogger(DefaultRecordSerialiser.class);
    private static final DateFormat FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

    private static final String ELEMENT_RECORD = "record";
    private static final String ELEMENT_META = "meta";
    private static final String ELEMENT_VALUE = "value";
    private static final String ELEMENT_ARRAY = "array";
    private static final String ELEMENT_ITEM = "item";
    private static final String ATTRIBUTE_NAME = "name";

    // write the record to the specified file.
    public void serialise(File file, Record record) throws RecordSerialiseException
    {
        Document doc = recordToDocument(record);
        try
        {
            XMLUtils.writeDocument(file, doc, false);
        }
        catch (IOException e)
        {
            throw new RecordSerialiseException(e);
        }
    }

    // read the record from the specified file.
    public MutableRecord deserialise(File file, RecordHandler handler) throws RecordSerialiseException
    {
        try
        {
            MutableRecord record;
            if (file.exists())
            {
                Document doc = XMLUtils.readDocument(file);
                record = documentToRecord(file, doc);
            }
            else
            {
                // This happens for collections
                record = new MutableRecordImpl();
            }

            // depth first traversal of the record for the benefit of the handler.
            traverse("", record, handler);

            return record;
        }
        catch (Exception e)
        {
            throw new RecordSerialiseException("Unable to parse record file: " + e.getMessage(), e);
        }
    }

    private void traverse(String path, MutableRecord record, RecordHandler handler)
    {
        handler.handle(path, record);
        for (String child : record.nestedKeySet())
        {
            String childPath = PathUtils.getPath(path, child);
            traverse(childPath, (MutableRecord) record.get(child), handler);
        }
    }

    private Document recordToDocument(Record record)
    {
        Element root = new Element(ELEMENT_RECORD);
        root.appendChild(new Comment("Stored by Pulse at " + FORMAT.format(new Date())));
        
        Document doc = new Document(root);

        recordToDocument(record, root);

        return doc;
    }

    private void recordToDocument(Record record, Element root)
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

            for (String key : record.nestedKeySet())
            {
                Record nested = (Record) record.get(key);
                Element nestedElement = createElement(ELEMENT_RECORD, key);
                root.appendChild(nestedElement);
                recordToDocument(nested, nestedElement);
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
            result[i] = XMLUtils.getText(items.get(i), "");
        }
        return result;
    }

    private interface ElementHandler
    {
        void handle(String key, Element child);
    }
}
