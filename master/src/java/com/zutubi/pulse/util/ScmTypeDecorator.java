/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.util;

import com.zutubi.pulse.model.Svn;
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
        
        if(current instanceof Svn)
        {
            return "subversion";
        }
        else
        {
            return "unknown";
        }
    }
}