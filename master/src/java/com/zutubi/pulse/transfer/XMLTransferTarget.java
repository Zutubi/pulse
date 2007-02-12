package com.zutubi.pulse.transfer;

import com.zutubi.pulse.util.JDBCTypes;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 */
public class XMLTransferTarget extends XMLTransferSupport implements TransferTarget
{
    private ProxySerializer serializer;

    private Element rootElement;

    private Element tableElement;

    private Table table;

    private String version;

    private Map<String, Integer> columnTypes;

    private OutputStream output;

    public XMLTransferTarget()
    {
    }

    public void setOutput(OutputStream output)
    {
        this.output = output;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public void start() throws TransferException
    {
        this.serializer = new ProxySerializer(output);

        try
        {
            rootElement = new Element("export");
            if (version != null)
            {
                rootElement.addAttribute(new Attribute("version", version));
            }

            new Document(rootElement);

            serializer.writeXMLDeclaration();
            serializer.writeStartTag(rootElement);
        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
    }

    public void startTable(Table table) throws TransferException
    {
        try
        {
            this.table = table;
            tableElement = new Element("table");
            tableElement.addAttribute(new Attribute("name", table.getName()));

            rootElement.appendChild(tableElement);
            serializer.writeStartTag(tableElement);

            // write out the serialize types..
            writeTableDef(table);
        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
    }

    private void writeTableDef(Table table) throws TransferException
    {
        try
        {
            columnTypes = new HashMap<String, Integer>();

            Element defs = new Element("type-defs");
            tableElement.appendChild(defs);

            Iterator columns = table.getColumnIterator();
            while (columns.hasNext())
            {
                Column column = (Column) columns.next();
                Element def = new Element("type-def");
                def.addAttribute(new Attribute("name", column.getName()));
                def.addAttribute(new Attribute("type", JDBCTypes.toString(column.getSqlTypeCode())));
                defs.appendChild(def);

                columnTypes.put(column.getName(), column.getSqlTypeCode());
            }

            serializer.write(defs);
        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
    }

    public void row(Map<String, Object> row) throws TransferException
    {
        try
        {
            Element rowElement = new Element("r");
            tableElement.appendChild(rowElement);
            serializer.writeStartTag(rowElement);

            for (Column column : MappingUtils.getColumns(table))
            {
                String columnName = column.getName();

                Element columnElement = new Element("c");
                rowElement.appendChild(columnElement);

                Object objValue = row.get(columnName);
                if (objValue != null)
                {
                    String value = toText(columnTypes.get(columnName), row.get(columnName));
                    columnElement.appendChild(value);
                }
                else
                {
                    columnElement.addAttribute(new Attribute("null", "t"));
                }

                serializer.write(columnElement);

            }

            serializer.writeEndTag(rowElement);
        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
    }

    public void endTable() throws TransferException
    {
        try
        {
            serializer.writeEndTag(tableElement);
        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
    }

    public void end() throws TransferException
    {
        try
        {
            serializer.writeEndTag(rootElement);
            serializer.flush();
        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
    }

    public void close()
    {

    }
}
