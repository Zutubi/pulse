package com.zutubi.pulse.master.tove.webwork.help;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.docs.PropertyDocs;
import com.zutubi.util.StringUtils;

/**
 * This is the base support class for actions that generate content for the
 * UI help panels. 
 */
public class HelpActionSupport extends ActionSupport
{
    private String path;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String safeDetails(String s)
    {
        return StringUtils.stringSet(s) ? sentencify(s) : "No details.";
    }

    private String sentencify(String s)
    {
        if(Character.isLowerCase(s.charAt(0)))
        {
            s = s.substring(0, 1).toUpperCase() + s.substring(1);
        }

        if(s.charAt(s.length() - 1) != '.')
        {
            s = s + '.';
        }

        return s;
    }

    public boolean isExpandable(PropertyDocs docs)
    {
        return hasExamples(docs) || isAbbreviated(docs);
    }

    public boolean hasExamples(PropertyDocs docs)
    {
        return docs.getExamples().size() > 0;
    }

    private boolean isAbbreviated(PropertyDocs docs)
    {
        return docs.getVerbose() != null && !docs.getVerbose().equals(docs.getBrief());
    }
}
