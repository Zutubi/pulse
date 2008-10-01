package com.zutubi.pulse.core.personal;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.zutubi.pulse.core.scm.FileStatus;

import java.util.Map;

/**
 * A custom converter for writing out file statuses.  Unfortunately this is
 * the simplest way I could find to get the enum handling right (i.e. to
 * suppress spurious "class" attributes on the "state" tag).
 */
public class FileStatusConverter implements Converter
{
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        FileStatus status = (FileStatus) source;
        writer.startNode("path");
        context.convertAnother(status.getPath());
        writer.endNode();
        writer.startNode("targetPath");
        context.convertAnother(status.getTargetPath());
        writer.endNode();
        writer.startNode("state");
        writer.setValue(status.getState().toString());
        writer.endNode();
        writer.startNode("directory");
        context.convertAnother(status.isDirectory());
        writer.endNode();
        writer.startNode("outOfDate");
        context.convertAnother(status.isOutOfDate());
        writer.endNode();

        for(Map.Entry<String, String> property: status.getProperties().entrySet())
        {
            writer.startNode("property");
            writer.startNode("name");
            context.convertAnother(property.getKey());
            writer.endNode();
            writer.startNode("value");
            context.convertAnother(property.getValue());
            writer.endNode();
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        FileStatus result = new FileStatus(null, null, false);

        while (reader.hasMoreChildren())
        {
            reader.moveDown();
            String fieldName = reader.getNodeName();
            if(fieldName.equals("path"))
            {
                result.setPath((String) context.convertAnother(result, String.class));
            }
            else if(fieldName.equals("targetPath"))
            {
                result.setTargetPath((String) context.convertAnother(result, String.class));
            }
            else if(fieldName.equals("state"))
            {
                String state = reader.getValue();

                try
                {
                    result.setState(FileStatus.State.valueOf(state));
                }
                catch (IllegalArgumentException e)
                {
                    throw new ConversionException("Unrecognised file state '" + state + "'");
                }
            }
            else if(fieldName.equals("directory"))
            {
                result.setDirectory((Boolean) context.convertAnother(result, boolean.class));
            }
            else if(fieldName.equals("outOfDate"))
            {
                result.setOutOfDate((Boolean) context.convertAnother(result, boolean.class));
            }
            else if(fieldName.equals("property"))
            {
                if(!reader.hasMoreChildren())
                {
                    throw new ConversionException("Property has no child elements");
                }

                reader.moveDown();
                String name = reader.getValue();
                reader.moveUp();

                if(!reader.hasMoreChildren())
                {
                    throw new ConversionException("Property has no value element");
                }

                reader.moveDown();
                String value = reader.getValue();
                reader.moveUp();

                result.setProperty(name, value);
            }
            else
            {
                throw new ConversionException("Unrecognised field '" + fieldName + "'");
            }

            reader.moveUp();
        }

        if(result.getPath() == null)
        {
            throw new ConversionException("Incomplete file status: missing path");
        }

        if(result.getState() == null)
        {
            throw new ConversionException("Incomplete file status: missing state");
        }

        return result;
    }

    public boolean canConvert(Class type)
    {
        return type == FileStatus.class;
    }
}
