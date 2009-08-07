package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.util.StringUtils;
import org.apache.commons.vfs.FileObject;

/**
 * <class comment/>
 */
public class MkdirAction extends VFSActionSupport
{
    /**
     * @deprecated
     */
    private String root;

    private String path;
    private String name;

    /**
     * @deprecated
     */
    public void setRoot(String root)
    {
        this.root = root;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String execute() throws Exception
    {
        if (StringUtils.stringSet(root))
        {
            path = root + path;
        }

        FileObject fo = getFS().resolveFile(path);

        FileObject newFolder = fo.resolveFile(name);
        if (newFolder.exists())
        {
            addActionError(String.format("The folder '%s' already exists. Please use a different name", name));
            return ERROR;
        }

        newFolder.createFolder();
        return SUCCESS;
    }
}
