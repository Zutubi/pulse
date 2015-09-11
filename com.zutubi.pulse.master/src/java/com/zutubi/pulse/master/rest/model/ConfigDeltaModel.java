package com.zutubi.pulse.master.rest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A model that represents changes to the configuration tree as a result of an update, be it an
 * insert, save or delete.  This is sent as a response to such operations to allow UIs to show the
 * changes.
 * <p/>
 * Note that a path may be renamed and updated in one operation. In this case the old->new mapping
 * is represented in renamedPaths, and the model (if present) will be in updatedPaths keyed by the
 * new path.
 */
public class ConfigDeltaModel
{
    private List<String> addedPaths;
    private List<String> deletedPaths;
    private Map<String, String> renamedPaths;
    private List<String> updatedPaths;

    private Map<String, ConfigModel> models;

    public List<String> getAddedPaths()
    {
        return addedPaths;
    }

    public void addAddedPath(String path, ConfigModel model)
    {
        if (addedPaths == null)
        {
            addedPaths = new ArrayList<>();
        }

        addedPaths.add(path);
        addModel(path, model);
    }

    public List<String> getDeletedPaths()
    {
        return deletedPaths;
    }

    public void addDeletedPath(String path)
    {
        if (deletedPaths == null)
        {
            deletedPaths = new ArrayList<>();
        }

        deletedPaths.add(path);
    }

    public Map<String, String> getRenamedPaths()
    {
        return renamedPaths;
    }

    public void addRenamedPath(String oldPath, String newPath)
    {
        if (renamedPaths == null)
        {
            renamedPaths = new HashMap<>();
        }

        renamedPaths.put(oldPath, newPath);
    }

    public List<String> getUpdatedPaths()
    {
        return updatedPaths;
    }

    public void addUpdatedPath(String path, ConfigModel model)
    {
        if (updatedPaths == null)
        {
            updatedPaths = new ArrayList<>();
        }

        updatedPaths.add(path);
        addModel(path, model);
    }

    public Map<String, ConfigModel> getModels()
    {
        return models;
    }

    private void addModel(String path, ConfigModel model)
    {
        if (model != null)
        {
            if (models == null)
            {
                models = new HashMap<>();
            }

            models.put(path, model);
        }
    }
}
