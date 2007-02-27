package com.zutubi.prototype.wizard;

import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public interface WizardState
{
    Record getRecord();

    Type getType();

}
