package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

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
    private List<PatternConfiguration> inclusions = new LinkedList<PatternConfiguration>();
    private List<PatternConfiguration> exclusions = new LinkedList<PatternConfiguration>();
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

    public List<PatternConfiguration> getInclusions()
    {
        return inclusions;
    }

    public void setInclusions(List<PatternConfiguration> inclusions)
    {
        this.inclusions = inclusions;
    }

    public List<PatternConfiguration> getExclusions()
    {
        return exclusions;
    }

    public void setExclusions(List<PatternConfiguration> exclusions)
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

    @SymbolicName("zutubi.directoryOutputConfig.patternConfig")
    public static class PatternConfiguration extends AbstractConfiguration
    {
        private String pattern;

        public String getPattern()
        {
            return pattern;
        }

        public void setPattern(String pattern)
        {
            this.pattern = pattern;
        }
    }
}