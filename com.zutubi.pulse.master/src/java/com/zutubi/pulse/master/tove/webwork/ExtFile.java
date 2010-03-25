package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.master.xwork.actions.vfs.FileObjectWrapper;
import flexjson.JSON;

import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data structure used to send the details of a single file to the Ext tree
 * in the UI.  Used for trivial conversion from Java->JSON->Ext.TreeNode.
 */
public class ExtFile
{
    private String baseName;
    private String text;
    private boolean leaf;
    private String cls;
    private String iconCls;
    private Map<String, Object> extraAttributes;
    /**
     * A list of children, if this is not a leaf.  Note that this may be left
     * empty for trees that are loaded dynamically.
     */
    private List<ExtFile> children;

    public ExtFile(String baseName, String text, boolean leaf)
    {
        this.baseName = baseName;
        this.text = text;
        this.leaf = leaf;
    }

    public ExtFile(FileObjectWrapper fo)
    {
        baseName = fo.getBaseName();
        text = fo.getName();
        leaf = !fo.isContainer();
        cls = fo.getCls();
        iconCls = fo.getIconCls();
        extraAttributes = fo.getExtraAttributes();
        if (extraAttributes == null)
        {
            extraAttributes = new HashMap<String, Object>();
        }
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

    @JSON
    public Map<String, Object> getExtraAttributes()
    {
        return extraAttributes;
    }

    @JSON
    public List<ExtFile> getChildren()
    {
        return children;
    }

    public void addChildren(ExtFile... toAdd)
    {
        if (children == null)
        {
            children = new LinkedList<ExtFile>();
        }

        this.children.addAll(asList(toAdd));
    }
}
