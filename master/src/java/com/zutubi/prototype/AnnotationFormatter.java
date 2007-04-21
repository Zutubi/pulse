package com.zutubi.prototype;

import com.zutubi.config.annotations.annotation.Format;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.logging.Logger;

/**
 *
 *
 */
public class AnnotationFormatter implements Formatter
{
    private static final Logger LOG  = Logger.getLogger(AnnotationFormatter.class);

    private Formatter<Object> defaultFormatter = null;

    public AnnotationFormatter(Formatter<Object> defaultFormatter)
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
            
            Class<? extends Formatter> formatterClass = ClassLoaderUtils.loadAssociatedClass(obj.getClass(), formatAnnotation.value());
            Formatter formatter = formatterClass.newInstance();
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
