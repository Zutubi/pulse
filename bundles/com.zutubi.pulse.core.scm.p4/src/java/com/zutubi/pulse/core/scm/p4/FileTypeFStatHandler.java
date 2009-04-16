package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;

/**
 * A handler for p4 fstat output which determines the type of a file.
 */
public class FileTypeFStatHandler extends AbstractPerforceFStatHandler
{
    private boolean text = false;

    public FileTypeFStatHandler(PersonalBuildUI ui)
    {
        super(ui);
    }

    public boolean isText()
    {
        return text;
    }

    protected void handleCurrentItem()
    {
        String type = getCurrentItemType();
        if (type == null)
        {
            type = getCurrentItemHeadType();
        }

        text = fileIsText(type);
    }
}
