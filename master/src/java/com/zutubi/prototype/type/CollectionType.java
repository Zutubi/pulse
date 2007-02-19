package com.zutubi.prototype.type;

import java.util.List;

/**
 *
 *
 */
public abstract class CollectionType extends AbstractType implements Type, Traversable
{
    private Type collectionType;

    public CollectionType(Class type)
    {
        super(type);
    }

    public CollectionType(Class type, String symbolicName)
    {
        super(type, symbolicName);
    }

    public Type getCollectionType()
    {
        return collectionType;
    }

    public void setCollectionType(Type collectionType)
    {
        this.collectionType = collectionType;
    }

    public Type getType(List<String> path)
    {
        if (path.size() == 0)
        {
            return this;
        }
        if (path.size() == 1)
        {
            return collectionType;
        }

        if (collectionType instanceof Traversable)
        {
            Traversable ctype = (Traversable) collectionType;
            return ctype.getType(path.subList(1, path.size()));
        }

        throw new IllegalArgumentException();
    }
}
