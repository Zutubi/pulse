package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmException;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base for handlers which deal with p4 fstat output.
 */
public abstract class AbstractPerforceFStatHandler extends PerforceErrorDetectingHandler
{
    protected PersonalBuildUI ui;
    protected Map<String, String> currentItem = new HashMap<String, String>();

    public AbstractPerforceFStatHandler(PersonalBuildUI ui)
    {
        super(true);
        this.ui = ui;
    }

    public void handleStdout(String line)
    {
        line = line.trim();
        if(line.length() == 0)
        {
            if(currentItem.size() > 0)
            {
                handleItem();
                currentItem.clear();
            }
        }
        else
        {
            String[] parts = line.split(" ", 3);
            if(parts.length == 3)
            {
                currentItem.put(parts[1], parts[2]);
            }
            else if(parts.length == 2)
            {
                currentItem.put(parts[1], "");
            }
        }
    }

    public void handleStderr(String line)
    {
        // Filter out spurious error (nothing changed)
        if(!line.contains("file(s) not opened on this client") && !line.equals("//... - no such file(s)."))
        {
            super.handleStderr(line);
        }
    }

    public void handleExitCode(int code) throws ScmException
    {
        super.handleExitCode(code);
        if(currentItem.size() > 0)
        {
            handleItem();
        }
    }

    public void checkCancelled() throws ScmCancelledException
    {
    }

    protected String getCurrentItemType()
    {
        String type = currentItem.get(com.zutubi.pulse.core.scm.p4.PerforceConstants.FSTAT_TYPE);
        if(type == null)
        {
            type = getCurrentItemHeadType();
        }
        return type;
    }

    protected String getCurrentItemHeadType()
    {
        String type = currentItem.get(com.zutubi.pulse.core.scm.p4.PerforceConstants.FSTAT_HEAD_TYPE);
        if(type == null)
        {
            type = "text";
        }

        return type;
    }

    protected boolean fileIsText(String type)
    {
        return type.contains("text");
    }

    protected void warning(String message)
    {
        if(ui != null)
        {
            ui.warning(message);
        }
    }

    protected abstract void handleItem();
}
