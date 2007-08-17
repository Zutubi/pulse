package com.zutubi.pulse.core.scm.cvs.client.commands;

/**
 *
 *
 */
public class RlsInfo
{
    private String module;
    private String name;
    private boolean directory;

    public RlsInfo(String module, String name, boolean directory)
    {
        this.module = module;
        this.name = name;
        this.directory = directory;
    }

    public String getModule()
    {
        return module;
    }

    public String getName()
    {
        return name;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public boolean isFile()
    {
        return !isDirectory();
    }


    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RlsInfo rlsInfo = (RlsInfo) o;

        if (directory != rlsInfo.directory) return false;
        if (module != null ? !module.equals(rlsInfo.module) : rlsInfo.module != null) return false;
        if (name != null ? !name.equals(rlsInfo.name) : rlsInfo.name != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (module != null ? module.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (directory ? 1 : 0);
        return result;
    }
}
