package com.zutubi.pulse.master.rest.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.zutubi.pulse.master.rest.model.tables.TableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing collections.
 */
@JsonTypeName("collection")
public class CollectionModel extends ConfigModel
{
    private CollectionTypeModel type;
    private TableModel table;
    private List<String> allowedActions;
    private List<HiddenItemModel> hiddenItems;
    private StateModel state;

    public CollectionModel()
    {
    }

    public CollectionModel(String key, String handle, String label, boolean deeplyValid)
    {
        super(handle, key, label, deeplyValid);
    }

    public CollectionTypeModel getType()
    {
        return type;
    }

    public void setType(CollectionTypeModel type)
    {
        this.type = type;
    }

    public TableModel getTable()
    {
        return table;
    }

    public void setTable(TableModel table)
    {
        this.table = table;
    }

    public List<String> getAllowedActions()
    {
        return allowedActions;
    }

    public void addAllowedAction(String action)
    {
        if (allowedActions == null)
        {
            allowedActions = new ArrayList<>();
        }

        allowedActions.add(action);
    }

    public List<HiddenItemModel> getHiddenItems()
    {
        return hiddenItems;
    }

    public void addHiddenItem(HiddenItemModel model)
    {
        if (hiddenItems == null)
        {
            hiddenItems = new ArrayList<>();
        }

        hiddenItems.add(model);
    }

    public StateModel getState()
    {
        return state;
    }

    public void setState(StateModel state)
    {
        this.state = state;
    }
}
