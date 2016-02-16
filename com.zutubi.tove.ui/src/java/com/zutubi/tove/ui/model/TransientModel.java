package com.zutubi.tove.ui.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Model representing transient composites, i.e. those that are never saved, just addressable by
 * path in a non-persistent scope.
 */
@JsonTypeName("transient")
public class TransientModel extends ConfigModel
{
    private CompositeTypeModel type;

    public TransientModel()
    {
    }

    public TransientModel(String key, String label)
    {
        super(null, key, label, true);
    }

    public CompositeTypeModel getType()
    {
        return type;
    }

    public void setType(CompositeTypeModel type)
    {
        this.type = type;
    }
}
