package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.StringList;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for directory artifacts: used to capture a bunch of files
 * nested under a single directory.
 *
 * @see DirectoryArtifact
 */
@SymbolicName("zutubi.directoryArtifactConfig")
@Form(fieldOrder = {"name", "base", "inclusions", "exclusions", "captureAsZip", "featured", "postProcessors", "calculateHash", "hashAlgorithm", "index", "type", "failIfNotPresent", "ignoreStale", "followSymlinks", "publish", "artifactPattern"})
public class DirectoryArtifactConfiguration extends FileSystemArtifactConfigurationSupport
{
    private String base;
    @Wizard.Ignore
    private String index;
    @Addable(value = "exclude", attribute = "pattern") @StringList
    private List<String> exclusions = new LinkedList<String>();
    @Addable(value = "include", attribute = "pattern") @StringList
    private List<String> inclusions = new LinkedList<String>();
    private boolean captureAsZip;
    @Wizard.Ignore
    private boolean followSymlinks;

    public DirectoryArtifactConfiguration()
    {
    }

    public DirectoryArtifactConfiguration(String name, String base)
    {
        super(name);
        this.base = base;
    }

    public String getBase()
    {
        return base;
    }

    public void setBase(String base)
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

    public boolean isCaptureAsZip()
    {
        return captureAsZip;
    }

    public void setCaptureAsZip(boolean captureAsZip)
    {
        this.captureAsZip = captureAsZip;
    }

    public boolean isFollowSymlinks()
    {
        return followSymlinks;
    }

    public void setFollowSymlinks(boolean followSymlinks)
    {
        this.followSymlinks = followSymlinks;
    }

    public Class<? extends Artifact> artifactType()
    {
        return DirectoryArtifact.class;
    }
}