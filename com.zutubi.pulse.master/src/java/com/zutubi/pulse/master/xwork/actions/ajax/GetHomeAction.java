package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;

import java.util.Map;
import java.util.HashMap;

/**
 * An action that provides access to data required by some UI components.
 */
public class GetHomeAction extends ActionSupport
{
    private Map<String, String> data;

    public Map<String, String> getData()
    {
        return data;
    }

    @Override
    public String execute() throws Exception
    {
        data = new HashMap<String, String>();

        // user home
        String userHome = System.getProperty("user.home");
        data.put("userHome", makeJavascriptFriendly(userHome));

        return SUCCESS;
    }

    /**
     * The '\' character is the javascript escape character. It is also the separator character on
     * some systems. When we render a path that contains this character, the path is corrupted with the
     * '\' escaping the first character of the files it separates. SO, to fix this, we escape it if
     * necessary.
     *
     * @param str string to be processed.
     *
     * @return a version of str with the '\' escaped.
     */
    protected String makeJavascriptFriendly(String str)
    {
        return str.replace("\\", "/");
    }
}
