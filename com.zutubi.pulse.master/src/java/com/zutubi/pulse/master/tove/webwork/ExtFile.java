package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.master.xwork.actions.vfs.FileObjectWrapper;
import com.zutubi.util.StringUtils;
import flexjson.JSON;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Data structure used to send the details of a single file to the Ext tree
 * in the UI.  Used for trivial conversion from Java->JSON->Ext.TreeNode.
 */
public class ExtFile
{
    private String baseName;
    private String text;
    private String href;
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

    public ExtFile(FileObjectWrapper fo,  String contextPath)
    {
        baseName = fo.getBaseName();
        text = fo.getName();
        leaf = !fo.isContainer();
        cls = fo.getCls();
        href = fo.getUrl();
        if (StringUtils.stringSet(href))
        {
            boolean absolute = false;
            try
            {
                URI uri = new URI(href);
                absolute = uri.isAbsolute();
            }
            catch (URISyntaxException e)
            {
                // Treat as relative.
            }

            if (!absolute)
            {
                href = StringUtils.join('/', true, true, contextPath, href);
            }
        }
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
        return TextUtils.htmlEncode(text);
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

    public String getHref()
    {
        return href;
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
