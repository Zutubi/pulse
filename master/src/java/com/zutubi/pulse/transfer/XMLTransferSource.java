package com.zutubi.pulse.transfer;

import com.zutubi.pulse.util.JDBCTypes;
import nu.xom.Builder;
import nu.xom.NodeFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Table;
import org.xml.sax.Attributes;

import java.io.InputStream;
import java.util.*;

/**
 *
 *
 */
public class XMLTransferSource extends XMLTransferSupport implements TransferSource
{
    private InputStream source;

    private Configuration configuration;

    private Map<String, String> row;

    private TransferTarget target;

    /**
     * The names of the columns, in the order in which the data is represented within the xml document.
     */
    private List<String> columnNames;
    private Map<String, Integer> columnTypes;

    private Iterator<String> columnIterator;
    private String columnName;

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
        String tableName = atts.getValue("name");
        Table table = getTable(tableName);
        target.startTable(table);
    }

    protected void endTable(String uri, String localName, String qName) throws TransferException
    {
        target.endTable();
    }

    protected void startRow(String uri, String localName, String qName, Attributes atts)
    {
        row = new HashMap<String, String>();
        columnIterator = columnNames.iterator();
    }

    protected void endRow(String uri, String localName, String qName) throws TransferException
    {
        // convert the items in the row.
        Map<String, Object> convertedRow = new HashMap<String, Object>();
        for (String key : row.keySet())
        {
            Object value = fromText(columnTypes.get(key), row.get(key));
            convertedRow.put(key, value);
        }
        target.row(convertedRow);
        row = null;
    }

    private void startTypeDefs(CharSequence uri, String localName, String qName, Attributes atts)
    {
        columnNames = new LinkedList<String>();
        columnTypes = new HashMap<String, Integer>();
    }

    private void startTypeDef(CharSequence uri, String localName, String qName, Attributes atts)
    {
        String columnName = atts.getValue("name");
        String columnType = atts.getValue("type");

        columnNames.add(columnName);
        columnTypes.put(columnName, JDBCTypes.valueOf(columnType));
    }

    protected void startColumn(String uri, String localName, String qName, Attributes atts)
    {
        columnName = columnIterator.next();
        if(atts.getValue("null") == null)
        {
            row.put(columnName, "");
        }
    }

    protected void endColumn(String uri, String localName, String qName)
    {
        columnName = null;
    }

    private Table getTable(String tableName)
    {
        Iterator tables = configuration.getTableMappings();
        while (tables.hasNext())
        {
            Table table = (Table) tables.next();
            if (table.getName().equals(tableName))
            {
                return table;
            }
        }
        if (HibernateUniqueKeyTable.isTable(tableName))
        {
            return HibernateUniqueKeyTable.getMapping();
        }
        return null;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

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

                if (localName.equals("type-defs"))
                {
                    startTypeDefs(uri, localName, qName, atts);
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
            }
            catch (TransferException e)
            {
                throw new RuntimeException(e);
            }
        }

        public void characters(char ch[], int start, int length)
        {
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
