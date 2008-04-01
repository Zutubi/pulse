package com.zutubi.pulse.transfer;

import com.zutubi.pulse.util.JDBCTypes;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
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

    private com.zutubi.pulse.transfer.Table table;

    private String version;

    private Map<String, Integer> columnTypes;

    private OutputStream output;

    private String dataDefinition;

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

    public void startTable(com.zutubi.pulse.transfer.Table table) throws TransferException
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

    private void writeTableDef(com.zutubi.pulse.transfer.Table table) throws TransferException
    {
        try
        {
            columnTypes = new HashMap<String, Integer>();

            Element defs = new Element("type-defs");
            tableElement.appendChild(defs);

            for (com.zutubi.pulse.transfer.Column column : table.getColumns())
            {
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
            patchElementWithParent(rowElement, tableElement);
            serializer.writeStartTag(rowElement);

            for (com.zutubi.pulse.transfer.Column column : table.getColumns())
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

    private void patchElementWithParent(Element node, Node parent)
    {
        try
        {
            Class nodeClass = node.getClass();
            while (nodeClass != null && nodeClass != Node.class)
            {
                nodeClass = nodeClass.getSuperclass();
            }
            if (nodeClass == null)
            {
                // we did not find what we wanted, abort the patch.
                return;
            }

            Field parentField = nodeClass.getDeclaredField("parent");
            if (parentField == null)
            {
                // we did not find what we wanted, abort the patch.
                return;
            }

            parentField.setAccessible(true);
            parentField.set(node, parent);
        }
        catch (Exception e)
        {
            // noop.
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
