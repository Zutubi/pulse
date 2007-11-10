package com.zutubi.prototype.webwork;

import com.zutubi.pulse.web.vfs.FileObjectWrapper;

/**
 * Data structure used to send the details of a single file to the Ext tree
 * in the UI.  Used for trivial conversion from Java->JSON->Ext.TreeNode.
 */
public class ExtFile
{
    private String id;
    private String text;
    private boolean leaf;
    private String cls;
    private String iconCls;

    public ExtFile(FileObjectWrapper fo)
    {
        id = fo.getId();
        text = fo.getName();
        leaf = !fo.isContainer();
        cls = fo.getCls();
        iconCls = fo.getIconCls();
    }

    public ExtFile(String id, String text, boolean leaf)
    {
        this.id = id;
        this.text = text;
        this.leaf = leaf;
    }

    public String getId()
    {
        return id;
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
