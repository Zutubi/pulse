package com.zutubi.pulse.util;

import com.zutubi.pulse.scm.svn.config.SvnConfiguration;
import org.displaytag.decorator.TableDecorator;

/**
 * 
 *
 */
public class ScmTypeDecorator extends TableDecorator
{
    public String getType()
    {
        Object current = getCurrentRowObject();
        
        if(current instanceof SvnConfiguration)
        {
            return "subversion";
        }
        else
        {
            return "unknown";
        }
    }
}