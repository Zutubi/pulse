package com.zutubi.pulse.scm;

/**
 * A file revision that is a simple number.  Used for Perforce and Subversion
 * for example.
 */
public class NumericalFileRevision implements FileRevision
{
    private long number;

    public NumericalFileRevision(long number)
    {
        this.number = number;
    }

    public long getNumber()
    {
        return number;
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
