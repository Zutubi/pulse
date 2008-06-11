package com.zutubi.prototype.webwork;

import com.zutubi.pulse.web.vfs.FileObjectWrapper;

/**
 * Data structure used to send the details of a single file to the Ext tree
 * in the UI.  Used for trivial conversion from Java->JSON->Ext.TreeNode.
 */
public class ExtFile
{
    private String id;
    private String baseName;
    private String text;
    private boolean leaf;
    private String cls;
    private String iconCls;

    public ExtFile(FileObjectWrapper fo)
    {
        id = fo.getUniqueId();
        baseName = fo.getBaseName();
        text = fo.getName();
        leaf = !fo.isContainer();
        cls = fo.getCls();
        iconCls = fo.getIconCls();
    }

    public String getId()
    {
        return id;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public String getText()
    {
        return text;
    }

    public boolean isLeaf()
    {
        return leaf;
    }

    public String getCls()
    {
        return cls;
    }

    public String getIconCls()
    {
        return iconCls;
    }
}
