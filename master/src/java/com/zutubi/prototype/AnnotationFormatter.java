package com.zutubi.prototype;

import com.zutubi.prototype.annotation.Format;
import com.zutubi.pulse.util.logging.Logger;

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
        try
        {
            Format formatAnnotation = obj.getClass().getAnnotation(Format.class);
            if (formatAnnotation == null)
            {
                return applyDefaultFormatting(obj);
            }
            
            Formatter formatter = formatAnnotation.value().newInstance();
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
