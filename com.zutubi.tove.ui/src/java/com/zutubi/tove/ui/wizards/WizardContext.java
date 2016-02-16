package com.zutubi.tove.ui.wizards;

import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.tove.ui.model.CompositeModel;

import java.util.Map;

/**
 * Simplifies signatures of wizard methods by bundling together the information collected when a
 * wizard is POSTed.
 */
public class WizardContext
{
    private final String parentPath;
    private final String baseName;
    private final TemplateRecord templateParentRecord;
    private final String templateOwnerPath;
    private final boolean concrete;
    private final Map<String, CompositeModel> models;

    public WizardContext(String parentPath, String baseName, TemplateRecord templateParentRecord, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models)
    {
        this.parentPath = parentPath;
        this.baseName = baseName;
        this.templateParentRecord = templateParentRecord;
        this.templateOwnerPath = templateOwnerPath;
        this.concrete = concrete;
        this.models = models;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public TemplateRecord getTemplateParentRecord()
    {
        return templateParentRecord;
    }

    public String getTemplateOwnerPath()
    {
        return templateOwnerPath;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public Map<String, CompositeModel> getModels()
    {
        return models;
    }
}
