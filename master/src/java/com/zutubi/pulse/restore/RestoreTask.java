package com.zutubi.pulse.restore;

/**
 *
 *
 */
public interface RestoreTask
{
    void execute() throws ArchiveException;

    String getName();
    
    String getDescription();

    boolean hasFailed();

    String[] getErrors();

    boolean haltOnFailure();

    ArchiveableComponent getComponent();
}
