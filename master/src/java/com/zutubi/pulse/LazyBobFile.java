package com.cinnamonbob;

import com.cinnamonbob.core.model.Revision;

/**
 * An instance of a bob file for a single build that is resolved lazily.  As
 * we do not know which revision we are building until the first recipe is
 * dispatched for the build, we can't eagerly evaluate all types of bob files
 * (in particular, those that come from the SCM).  Instead, a single lazy bob
 * file is shared amongst all recipe dispatch requests, and it is evaluated
 * when the revision is known: on the first dispatch.
 */
public class LazyBobFile
{
    private String bobFile;

    public LazyBobFile()
    {
    }

    public LazyBobFile(String bobFile)
    {
        this.bobFile = bobFile;
    }

    public String getBobFile()
    {
        return bobFile;
    }

    public void setBobFile(String bobFile)
    {
        this.bobFile = bobFile;
    }
}
