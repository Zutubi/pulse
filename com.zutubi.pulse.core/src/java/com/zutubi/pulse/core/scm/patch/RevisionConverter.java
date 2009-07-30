package com.zutubi.pulse.core.scm.patch;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.zutubi.pulse.core.scm.api.Revision;

/**
 * A custom converter for writing out revisions that just folds it down to the
 * revision string.
 */
public class RevisionConverter implements Converter
{
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        Revision revision = (Revision) source;
        writer.setValue(revision.getRevisionString());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        return new Revision(reader.getValue());
    }

    public boolean canConvert(Class type)
    {
        return type == Revision.class;
    }
}
