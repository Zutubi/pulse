package com.zutubi.pulse.master.tove.nimda;

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
 * Data structure used to send the details of a single file to the Kendo tree in the UI.  Used for
 * trivial conversion from Java->JSON->Kendo tree node.
 */
public class FileModel
{
    private String path;
    private String text;
    private boolean hasChildren;
    private String cls;
    private String spriteCssClass;
    private Map<String, Object> extraAttributes;
    /**
     * A list of children, if this is not a leaf.  Note that this may be left
     * empty for trees that are loaded dynamically.
     */
    private List<FileModel> items;

    public FileModel(String path, String text, boolean hasChildren, String cls, String spriteCssClass)
    {
        this.path = path;
        this.text = text;
        this.hasChildren = hasChildren;
        this.cls = cls;
        this.spriteCssClass = spriteCssClass;
        extraAttributes = new HashMap<String, Object>();
    }

    public String getPath()
    {
        return path;
    }

    public String getText()
    {
        return TextUtils.htmlEncode(text);
    }

    public boolean getHasChildren()
    {
        return hasChildren;
    }

    public String getCls()
    {
        return cls;
    }

    public String getSpriteCssClass()
    {
        return spriteCssClass;
    }

    @JSON
    public Map<String, Object> getExtraAttributes()
    {
        return extraAttributes;
    }

    @JSON
    public List<FileModel> getItems()
    {
        return items;
    }

    public void addChildren(FileModel... toAdd)
    {
        if (items == null)
        {
            items = new LinkedList<FileModel>();
        }

        this.items.addAll(asList(toAdd));
    }
}
