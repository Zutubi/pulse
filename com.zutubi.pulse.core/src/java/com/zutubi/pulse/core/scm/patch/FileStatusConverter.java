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

package com.zutubi.pulse.core.scm.patch;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;

import java.util.HashMap;
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
        writer.startNode("payloadType");
        writer.setValue(status.getPayloadType().toString());
        writer.endNode();
        writer.startNode("directory");
        context.convertAnother(status.isDirectory());
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
        String path = null;
        String targetPath = null;
        FileStatus.State state = null;
        FileStatus.PayloadType payloadType = null;
        boolean directory = false;
        Map<String, String> properties = new HashMap<String, String>();

        while (reader.hasMoreChildren())
        {
            reader.moveDown();
            String fieldName = reader.getNodeName();
            if(fieldName.equals("path"))
            {
                path = reader.getValue();
            }
            else if(fieldName.equals("targetPath"))
            {
                targetPath = reader.getValue();
            }
            else if(fieldName.equals("state"))
            {
                String stateString = reader.getValue();

                try
                {
                    state = FileStatus.State.valueOf(stateString);
                }
                catch (IllegalArgumentException e)
                {
                    throw new ConversionException("Unrecognised file state '" + stateString + "'");
                }
            }
            else if(fieldName.equals("payloadType"))
            {
                String payloadString = reader.getValue();

                try
                {
                    payloadType = FileStatus.PayloadType.valueOf(payloadString);
                }
                catch (IllegalArgumentException e)
                {
                    throw new ConversionException("Unrecognised payload type '" + payloadString + "'");
                }
            }
            else if(fieldName.equals("directory"))
            {
                directory = Boolean.valueOf(reader.getValue());
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

                properties.put(name, value);
            }
            else
            {
                throw new ConversionException("Unrecognised field '" + fieldName + "'");
            }

            reader.moveUp();
        }

        if (path == null)
        {
            throw new ConversionException("Incomplete file status: missing path");
        }

        if (state == null)
        {
            throw new ConversionException("Incomplete file status: missing state");
        }

        if (payloadType == null)
        {
            throw new ConversionException("Incomplete file status: missing payload type");
        }

        FileStatus status = new FileStatus(path, state, directory, targetPath);
        status.setPayloadType(payloadType);
        for (Map.Entry<String, String> entry: properties.entrySet())
        {
            status.setProperty(entry.getKey(), entry.getValue());
        }
        return status;
    }

    public boolean canConvert(Class type)
    {
        return type == FileStatus.class;
    }
}
