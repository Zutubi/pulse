package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This upgrade task replaces the hibernate discriminator for base features
 * with 'FEATURE' - instead of using the qualified class name which can change.
 */
public class FeatureDiscriminatorUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Feature Discriminator";
    }

    public String getDescription()
    {
        return "Updates the discriminator for base features.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement statement = null;
        try
        {
            statement = con.prepareStatement("update FEATURE set FEATURE_TYPE = 'FEATURE' where FEATURE_TYPE = 'com.zutubi.pulse.core.model.Feature' or FEATURE_TYPE='com.zutubi.pulse.core.model.PersistentFeature'");
            statement.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(statement);
        }
    }
}