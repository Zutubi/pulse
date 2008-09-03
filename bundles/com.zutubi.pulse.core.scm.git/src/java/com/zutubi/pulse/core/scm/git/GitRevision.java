package com.zutubi.pulse.core.scm.git;

/**
 *
 *
 */
public class GitRevision
{
    private String revision;

    public GitRevision(String revision)
    {
        this.revision = revision;
    }

    public String getRevision()
    {
        return revision;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitRevision that = (GitRevision) o;

        if (revision != null ? !revision.equals(that.revision) : that.revision != null) return false;

        return true;
    }

    public int hashCode()
    {
        return (revision != null ? revision.hashCode() : 0);
    }
}
