package com.zutubi.pulse.master.rest.model;

import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing composites.
 */
public class CompositeModel extends ConfigModel
{
    private FormModel form;
    private List<ActionModel> actions = new ArrayList<>();

    public CompositeModel(CompositeType type, Configuration instance)
    {
        super(new CompositeTypeModel(type));
    }

    public FormModel getForm()
    {
        return form;
    }

    public void setForm(FormModel form)
    {
        this.form = form;
    }

    public List<ActionModel> getActions()
    {
        return actions;
    }

    public void addAction(ActionModel action)
    {
        actions.add(action);
    }
}
