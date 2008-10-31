package com.zutubi.pulse.master.tove.webwork.help;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.TextUtils;

/**
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
        return TextUtils.stringSet(s) ? sentencify(s) : "No details.";
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
}
