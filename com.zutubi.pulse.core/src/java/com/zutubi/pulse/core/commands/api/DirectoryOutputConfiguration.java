package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.SymbolicName;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.directoryOutputConfig")
public class DirectoryOutputConfiguration extends FileSystemOutputConfigurationSupport
{
    private File base;
    private String index;
    @Addable("include")
    private List<String> exclusions = new LinkedList<String>();
    @Addable("exclude")
    private List<String> inclusions = new LinkedList<String>();
    private boolean followSymlinks;

    public File getBase()
    {
        return base;
    }

    public void setBase(File base)
    {
        this.base = base;
    }

    public String getIndex()
    {
        return index;
    }

    public void setIndex(String index)
    {
        this.index = index;
    }

    public List<String> getInclusions()
    {
        return inclusions;
    }

    public void setInclusions(List<String> inclusions)
    {
        this.inclusions = inclusions;
    }

    public List<String> getExclusions()
    {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions)
    {
        this.exclusions = exclusions;
    }

    public boolean isFollowSymlinks()
    {
        return followSymlinks;
    }

    public void setFollowSymlinks(boolean followSymlinks)
    {
        this.followSymlinks = followSymlinks;
    }

    public Output createOutput()
    {
        return buildOutput(DirectoryOutput.class);
    }
}