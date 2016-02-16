package com.zutubi.tove.ui.model;

import java.util.List;

/**
 * Used to preview a move refactoring, i.e. moving a template collection item to a new parent.
 */
public class MoveModel
{
    private String scope;
    private String key;
    private String newParentKey;
    private List<String> pathsToDelete;

    public MoveModel()
    {
    }

    public MoveModel(MoveModel input, List<String> pathsToDelete)
    {
        this.scope = input.getScope();
        this.key = input.getKey();
        this.newParentKey = input.getNewParentKey();
        this.pathsToDelete = pathsToDelete;
    }

    public String getScope()
    {
        return scope;
    }

    public String getKey()
    {
        return key;
    }

    public String getNewParentKey()
    {
        return newParentKey;
    }

    public List<String> getPathsToDelete()
    {
        return pathsToDelete;
    }
}
