package com.zutubi.prototype;

import com.zutubi.prototype.model.Table;
import com.zutubi.prototype.type.record.TemplateRecord;

/**
 * Applies decoration to table models to account for template inheritance.
 * For example, an inherited row is decorated with an extra parameter, and
 * has any delete action replaced with a hide action.
 */
public class TemplateTableDecorator
{
    private TemplateRecord templateRecord;

    public TemplateTableDecorator(TemplateRecord templateRecord)
    {
        this.templateRecord = templateRecord;
    }

    public void decorate(Table table)
    {
        
    }
}
