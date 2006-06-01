package com.zutubi.pulse;

/**
 * An instance of a pulse file for a single build that is resolved lazily.  As
 * we do not know which revision we are building until the first recipe is
 * dispatched for the build, we can't eagerly evaluate all types of pulse files
 * (in particular, those that come from the SCM).  Instead, a single lazy pulse
 * file is shared amongst all recipe dispatch requests, and it is evaluated
 * when the revision is known: on the first dispatch.
 */
public class LazyPulseFile
{
    private String pulseFile;

    public LazyPulseFile()
    {
    }

    public LazyPulseFile(String pulseFile)
    {
        this.pulseFile = pulseFile;
    }

    public String getPulseFile()
    {
        return pulseFile;
    }

    public void setPulseFile(String pulseFile)
    {
        this.pulseFile = pulseFile;
    }
}
