package com.zutubi.pulse.monitor;

import java.util.List;

/**
 *
 *
 */
public interface Task
{
    String getName();

    String getDescription();

    List<String> getErrors();

    boolean haltOnFailure();

    boolean hasFailed();

    void execute() throws TaskException;
}
