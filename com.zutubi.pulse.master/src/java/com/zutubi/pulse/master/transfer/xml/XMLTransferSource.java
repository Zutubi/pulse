/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.transfer.xml;

import com.zutubi.pulse.core.util.JDBCTypes;
import com.zutubi.pulse.master.transfer.*;
import nu.xom.Builder;
import org.xml.sax.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 */
public class XMLTransferSource extends XMLTransferSupport implements TransferSource
{
    private InputStream source;

    private Map<String, String> row;

    private TransferTarget target;

    private TransferTable currentTable;

    private Column currentColumn;
    private Iterator<Column> columnIterator;

    public void setSource(InputStream source)
    {
        this.source = source;
    }

    public void transferTo(TransferTarget target) throws TransferException
    {
        try
        {
            this.target = target;
            this.target.start();

            XMLReader reader = loadXMLReader();
            reader.setContentHandler(new Callback());
            reader.parse(new InputSource(source));

            this.target.end();
        }
        catch (Exception e)
        {
            if (e.getCause() instanceof TransferException)
            {
                throw (TransferException)e.getCause();
            }
            throw new TransferException(e);
        }
    }

    private XMLReader loadXMLReader() throws TransferException
    {
        try
        {
            Builder builder = new Builder();

            Field field = Builder.class.getDeclaredField("parser");
            field.setAccessible(true);
            return (XMLReader) field.get(builder);
        }
        catch (Exception e)
        {
            throw new TransferException(e);
        }
    }

    protected void startTable(String uri, String localName, String qName, Attributes atts) throws TransferException
    {
        currentTable = new TransferTable();
        currentTable.setName(atts.getValue("name"));
    }

    protected void endTable(String uri, String localName, String qName) throws TransferException
    {
        target.endTable();
    }

    protected void startRow(String uri, String localName, String qName, Attributes atts)
    {
        row = new HashMap<String, String>();
        columnIterator = currentTable.getColumns().iterator();
    }

    protected void endRow(String uri, String localName, String qName) throws TransferException
    {
        // convert the items in the row.
        Map<String, Object> convertedRow = new HashMap<String, Object>();
        for (String key : row.keySet())
        {
            Object value = fromText(currentTable.getColumnType(key), row.get(key));
            convertedRow.put(key, value);
        }
        target.row(convertedRow);
        row = null;
    }

    private void startTypeDef(CharSequence uri, String localName, String qName, Attributes atts)
    {
        String columnName = atts.getValue("name");
        String columnType = atts.getValue("type");

        TransferColumn column = new TransferColumn();
        column.setName(columnName);
        column.setSqlTypeCode(JDBCTypes.valueOf(columnType));
        currentTable.add(column);
    }

    private void endTypeDefs(CharSequence uri, String localName, String qName) throws TransferException
    {
        // we now have the full table definition, so we can start the table.
        target.startTable(currentTable);
    }

    protected void startColumn(String uri, String localName, String qName, Attributes atts)
    {
        currentColumn = columnIterator.next();

        if(atts.getValue("null") == null)
        {
            row.put(currentColumn.getName(), "");
        }
    }

    protected void endColumn(String uri, String localName, String qName)
    {
        currentColumn = null;
    }

    private class Callback implements ContentHandler
    {
        public void setDocumentLocator(Locator locator) {}
        public void startDocument() throws SAXException {}
        public void endDocument() throws SAXException {}
        public void startPrefixMapping(String prefix, String uri) throws SAXException {}
        public void endPrefixMapping(String prefix) throws SAXException {}

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
        {
            try
            {
                if (localName.equals("table"))
                {
                    startTable(uri, localName, qName, atts);
                }

                if (localName.equals("r"))
                {
                    startRow(uri, localName, qName, atts);
                }

                if (localName.equals("c"))
                {
                    startColumn(uri, localName, qName, atts);
                }

                if (localName.equals("type-def"))
                {
                    startTypeDef(uri, localName, qName, atts);
                }
            }
            catch (TransferException e)
            {
                throw new RuntimeException(e);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            try
            {
                if (localName.equals("c"))
                {
                    endColumn(uri, localName, qName);
                }

                if (localName.equals("r"))
                {
                    endRow(uri, localName, qName);
                }

                if (localName.equals("table"))
                {
                    endTable(uri, localName, qName);
                }

                if (localName.equals("type-defs"))
                {
                    endTypeDefs(uri, localName, qName);
                }
            }
            catch (TransferException e)
            {
                throw new RuntimeException(e);
            }
        }

        public void characters(char ch[], int start, int length) throws SAXException
        {
            String columnName = currentColumn.getName();
            if (columnName != null)
            {
                String str = new String(ch, start, length);
                if (row.containsKey(columnName))
                {
                    str = row.get(columnName) + str;
                }
                row.put(columnName, str);
            }
        }

        public void ignorableWhitespace(char ch[], int start, int length) throws SAXException { }
        public void processingInstruction(String target, String data) throws SAXException { }
        public void skippedEntity(String name) throws SAXException { }
    }
}
