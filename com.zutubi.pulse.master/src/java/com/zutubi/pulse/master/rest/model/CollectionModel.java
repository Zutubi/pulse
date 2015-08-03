package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CollectionType;

/**
 * Model representing collections.
 */
public class CollectionModel extends ConfigModel
{
    private CollectionType collectionType;

    public CollectionModel(CollectionType collectionType, Configuration instance)
    {
        super(new CollectionTypeModel(collectionType));
        this.collectionType = collectionType;
    }

}
