package com.zutubi.prototype.type;

import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;

import java.util.List;
import java.util.Collections;

/**
 *
 *
 */
public class PrimitiveType extends AbstractType implements Type
{
    public PrimitiveType(Class type)
    {
        this(type, null);
    }

    public PrimitiveType(Class type, String symbolicName)
    {
        super(type, symbolicName);
        if (Squeezers.findSqueezer(type) == null)
        {
            throw new IllegalArgumentException("Unsupported primitive type: " + type);
        }
    }

    public Object instantiate(Object data) throws TypeException
    {
        TypeSqueezer squeezer = Squeezers.findSqueezer(getClazz());
        try
        {
            if (data instanceof String[])
            {
                return squeezer.unsqueeze((String[])data);
            }
            else if (data instanceof String)
            {
                return squeezer.unsqueeze((String)data);
            }
            return data;
        }
        catch (SqueezeException e)
        {
            throw new TypeConversionException(e);
        }
    }

    public Object unstantiate(Object data) throws TypeException
    {
        if (data == null)
        {
            return null;
        }
        
        TypeSqueezer squeezer = Squeezers.findSqueezer(getClazz());
        try
        {
            return squeezer.squeeze(data);
        }
        catch (SqueezeException e)
        {
            throw new TypeConversionException(e);
        }
    }

    public Object instantiate() throws TypeConversionException
    {
        // should not need to instantiate a primitive type.
        throw new RuntimeException("not yet implemented.");
    }

    public List<TypeProperty> getProperties()
    {
        return Collections.EMPTY_LIST;
    }

    public TypeProperty getProperty(String name)
    {
        return null;
    }

    public List<TypeProperty> getProperties(Class<? extends Type> type)
    {
        return Collections.EMPTY_LIST;
    }

    public void setRecord(String path, Record record, RecordManager recordManager)
    {
        throw new RuntimeException("Method not implemented.");
    }
}
