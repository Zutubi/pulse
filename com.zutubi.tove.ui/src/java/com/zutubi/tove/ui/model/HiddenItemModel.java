package com.zutubi.tove.ui.model;

/**
 * Models a hidden item in a collection.
 */
public class HiddenItemModel
{
    private String key;
    private String templateOwner;

    public HiddenItemModel(String key, String templateOwner)
    {
        this.key = key;
        this.templateOwner = templateOwner;
    }

    public String getKey()
    {
        return key;
    }

    public String getTemplateOwner()
    {
        return templateOwner;
    }
}
