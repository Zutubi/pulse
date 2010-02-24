package com.zutubi.pulse.core.scm.p4;

/**
 * A handler for p4 fstat output which determines the type of a file.
 */
public class FileTypeFStatFeedbackHandler extends AbstractPerforceFStatFeedbackHandler
{
    private boolean text = false;

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
