/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.ui.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.zutubi.tove.ui.model.tables.TableModel;

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
    private List<String> declaredOrder;
    private String orderTemplateOwner;
    private String orderOverriddenOwner;

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

    public List<String> getDeclaredOrder()
    {
        return declaredOrder;
    }

    public void setDeclaredOrder(List<String> declaredOrder)
    {
        this.declaredOrder = declaredOrder;
    }

    public String getOrderTemplateOwner()
    {
        return orderTemplateOwner;
    }

    public String getOrderOverriddenOwner()
    {
        return orderOverriddenOwner;
    }

    public void decorateWithOrderTemplateDetails(String orderTemplateOwner, String orderOverriddenOwner)
    {
        this.orderTemplateOwner = orderTemplateOwner;
        this.orderOverriddenOwner = orderOverriddenOwner;
    }
}
