package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.model.FileRevision;

/**
 * A file revision for CVS-style dotted-decimal revisions.
 */
public class CvsFileRevision extends FileRevision
{
    private String revision;

    private CvsFileRevision()
    {
    }

    public CvsFileRevision(String revision)
    {
        this.revision = revision;
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
    }

    public FileRevision getPrevious()
    {
        int index = revision.lastIndexOf(".");
        if(index != -1)
        {
            String end = revision.substring(index + 1);
            try
            {
                long last = Long.parseLong(end);
                if(last > 1)
                {
                    String start = revision.substring(0, index + 1);
                    return new CvsFileRevision(start + Long.toString(last - 1));
                }
            }
            catch(NumberFormatException e)
            {
                // Fall through.
            }
        }
        
        return null;
    }

    public String getRevisionString()
    {
        return revision;
    }

    public String toString()
    {
        return getRevisionString();
    }
}
