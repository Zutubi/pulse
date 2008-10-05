package com.zutubi.pulse.master.vfs.provider.agent;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileName;

/**
 * <class comment/>
 */
public class AgentFileName extends AbstractFileName
{
    private final String address;

    public AgentFileName(final String scheme, final String address, final String absPath, FileType type)
    {
        super(scheme, absPath, type);
        this.address = address;
    }

    public String getAddress()
    {
        return address;
    }

    public FileName createName(String absPath, FileType type)
    {
        return new AgentFileName(getScheme(), address, absPath, type);
    }

    protected void appendRootUri(StringBuffer buffer, boolean addPassword)
    {
        buffer.append(getScheme());
        buffer.append("://");
        buffer.append(address);
    }

}
