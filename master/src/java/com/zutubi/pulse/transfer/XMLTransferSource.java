package com.zutubi.pulse.transfer;

import com.zutubi.pulse.util.JDBCTypes;
import nu.xom.Builder;
import nu.xom.NodeFactory;
import org.xml.sax.Attributes;

import java.io.InputStream;
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

/*
    private Configuration configuration;
*/

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
            Builder builder = new Builder(new Callback());
            builder.build(source);
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

/*
    private Table getTable(String tableName)
    {
        Iterator tables = configuration.getTableMappings();
        while (tables.hasNext())
        {
            org.hibernate.mapping.Table table = (org.hibernate.mapping.Table) tables.next();
            if (table.getName().equals(tableName))
            {
                return new HibernateTable(table);
            }
        }
        if (HibernateUniqueKeyTable.isTable(tableName))
        {
            return new HibernateTable(HibernateUniqueKeyTable.getMapping());
        }
        return null;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
*/

    private class Callback extends NodeFactory
    {
        public void startElement(String uri, String localName, String qName, Attributes atts)
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

        public void endElement(String uri, String localName, String qName)
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

        public void characters(char ch[], int start, int length)
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
    }
}
