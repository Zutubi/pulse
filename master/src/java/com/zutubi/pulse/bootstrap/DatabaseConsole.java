package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.core.Stoppable;

/**
 *
 *
 */
public interface DatabaseConsole extends Stoppable
{
    boolean isEmbedded();

    boolean schemaExists();

    void createSchema();

    DatabaseConfig getConfig();

    double getDatabaseUsagePercent();
}
