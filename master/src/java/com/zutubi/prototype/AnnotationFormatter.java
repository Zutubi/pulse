package com.zutubi.prototype;

import com.zutubi.prototype.annotation.Format;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.ClassLoaderUtils;

/**
 *
 *
 */
public class AnnotationFormatter implements Formatter
{
    private static final Logger LOG  = Logger.getLogger(AnnotationFormatter.class);

    private Formatter defaultFormatter = null;

    public AnnotationFormatter(Formatter defaultFormatter)
    {
        this.defaultFormatter = defaultFormatter;
    }

    public String format(Object obj)
    {
        // FIXME This is particularly inefficient: loading all this gear
        // FIXME every row of the table, and every page reload.  We should
        // FIXME look up once and cache somehow.
        try
        {
            Format formatAnnotation = obj.getClass().getAnnotation(Format.class);
            if (formatAnnotation == null)
            {
                return applyDefaultFormatting(obj);
            }
            
            Class formatterClass = ClassLoaderUtils.loadAssociatedClass(obj.getClass(), formatAnnotation.value());
            Formatter formatter = (Formatter) formatterClass.newInstance();
            return formatter.format(obj);
        }
        catch (Exception e)
        {
            LOG.warning("Failed to format object: " + obj, e);
            return applyDefaultFormatting(obj);
        }
    }

    private String applyDefaultFormatting(Object obj)
    {
        return defaultFormatter.format(obj);
    }
}
