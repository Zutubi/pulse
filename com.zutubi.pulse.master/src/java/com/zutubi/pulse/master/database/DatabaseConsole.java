package com.zutubi.pulse.master.database;

import com.zutubi.pulse.core.Stoppable;

import java.sql.SQLException;

/**
 *
 *
 */
public interface DatabaseConsole extends Stoppable
{
    boolean isEmbedded();

    boolean schemaExists();

    void createSchema() throws SQLException;

    void dropSchema() throws SQLException;

    DatabaseConfig getConfig();

    double getDatabaseUsagePercent();

    void postSchemaHook();

    void postUpgradeHook(boolean changes);
}
