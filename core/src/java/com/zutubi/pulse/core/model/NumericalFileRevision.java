package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.model.FileRevision;

/**
 * A file revision that is a simple number.  Used for Perforce and Subversion
 * for example.
 */
public class NumericalFileRevision extends FileRevision
{
    private long number;

    private NumericalFileRevision()
    {
    }

    public NumericalFileRevision(long number)
    {
        this.number = number;
    }

    public long getNumber()
    {
        return number;
    }

    public void setNumber(long number)
    {
        this.number = number;
    }

    public FileRevision getPrevious()
    {
        if(number > 0)
        {
            return new NumericalFileRevision(number - 1);
        }

        return null;
    }

    public String getRevisionString()
    {
        return Long.toString(number);
    }
}
