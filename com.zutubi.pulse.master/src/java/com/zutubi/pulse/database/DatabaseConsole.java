package com.zutubi.pulse.database;

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

    void postRestoreHook(boolean restored);

    void postUpgradeHook(boolean changes);
}
