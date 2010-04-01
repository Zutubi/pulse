package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import static com.zutubi.util.FileSystemUtils.normaliseSeparators;

import java.util.Map;
import java.util.HashMap;

/**
 * An action that provides access to the users home directory location.
 */
public class GetHomeDirectoryAction extends ActionSupport
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

        String userHome = System.getProperty("user.home");
        data.put("userHome", normaliseSeparators(userHome));

        return SUCCESS;
    }
}
