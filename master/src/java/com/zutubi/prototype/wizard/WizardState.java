package com.zutubi.prototype.wizard;

import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;

import java.util.Map;

/**
 *
 *
 */
public interface WizardState
{
    TemplateRecord getTemplateRecord();

    Type getType();

    Record getRecord();

}
