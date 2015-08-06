package com.zutubi.pulse.master.rest.model;

/**
 * Model representing collections.
 */
public class CollectionModel extends ConfigModel
{
    public CollectionModel(String key, String handle, String label)
    {
        super("collection", handle, key, label);
    }

}
