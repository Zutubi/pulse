package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.util.JDBCUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Upgrade task to move artifact features from the db to disk.
 */
public class FeatureStorageUpgradeTask extends AbstractSchemaRefactorUpgradeTask implements ConfigurationAware
{
    private MasterConfigurationManager configurationManager;
    private PreparedStatement selectCommandResults;
    private PreparedStatement selectArtifactByCommandId;
    private PreparedStatement selectFileArtifactByArtifactId;
    private PreparedStatement selectFeatureByFileArtifactId;
    private PreparedStatement deleteFeatureByFileArtifactId;
    private OriginalFeaturePersister persister;

    public String getName()
    {
        return "Feature storage upgrade";
    }

    public String getDescription()
    {
        return "Moves artifact features from the database to disk";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        persister = new OriginalFeaturePersister();
        try
        {
            prepareStatements(con);
            processCommands(con);
        }
        finally
        {
            closePreparedStatements();
        }

        refactor.dropColumn("FEATURE", "FILE_ARTIFACT_ID");
    }

    private void prepareStatements(Connection con) throws SQLException
    {
        selectCommandResults = con.prepareStatement("SELECT ID, COMMAND_NAME, outputDir FROM COMMAND_RESULT", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        selectArtifactByCommandId = con.prepareStatement("SELECT ID, NAME FROM ARTIFACT WHERE COMMAND_RESULT_ID = ?");
        selectFileArtifactByArtifactId = con.prepareStatement("SELECT ID, FILE FROM FILE_ARTIFACT WHERE ARTIFACT_ID = ?");
        selectFeatureByFileArtifactId = con.prepareStatement("SELECT ID, LEVEL, FIRST_LINE, LAST_LINE, LINE, SUMMARY FROM FEATURE WHERE FILE_ARTIFACT_ID = ?");
        deleteFeatureByFileArtifactId = con.prepareStatement("DELETE FROM FEATURE WHERE FILE_ARTIFACT_ID = ?");
    }

    private void closePreparedStatements()
    {
        JDBCUtils.close(selectCommandResults);
        JDBCUtils.close(selectArtifactByCommandId);
        JDBCUtils.close(selectFileArtifactByArtifactId);
        JDBCUtils.close(selectFeatureByFileArtifactId);
    }

    private void processCommands(Connection con) throws SQLException, IOException
    {
        ResultSet rs = null;
        try
        {
            rs = selectCommandResults.executeQuery();
            while (rs.next())
            {
                processCommandResult(con, JDBCUtils.getLong(rs, "ID"), JDBCUtils.getString(rs, "COMMAND_NAME"), JDBCUtils.getString(rs, "outputDir"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private void processCommandResult(Connection con, Long id, String name, String outputDir) throws SQLException, IOException
    {
        if (outputDir != null)
        {
            OriginalFeaturePersister.CommandResult commandResult = new OriginalFeaturePersister.CommandResult(name);
            ResultSet rs = null;
            try
            {
                selectArtifactByCommandId.setLong(1, id);
                rs = selectArtifactByCommandId.executeQuery();
                while (rs.next())
                {
                    commandResult.addArtifact(processArtifact(con, rs.getLong("ID"), rs.getString("NAME")));
                }
            }
            finally
            {
                JDBCUtils.close(rs);
            }

            File recipeDir = new File(configurationManager.getDataDirectory(), outputDir).getParentFile().getParentFile();
            if (recipeDir.exists())
            {
                persister.writeFeatures(commandResult, recipeDir);
            }
        }
    }

    private OriginalFeaturePersister.StoredArtifact processArtifact(Connection con, long id, String name) throws SQLException
    {
        OriginalFeaturePersister.StoredArtifact storedArtifact = new OriginalFeaturePersister.StoredArtifact(name);
        ResultSet rs = null;
        try
        {
            selectFileArtifactByArtifactId.setLong(1, id);
            rs = selectFileArtifactByArtifactId.executeQuery();
            while (rs.next())
            {
                storedArtifact.addFile(processFileArtifact(con, rs.getLong("ID"), rs.getString("FILE")));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }

        return storedArtifact;
    }

    private OriginalFeaturePersister.StoredFileArtifact processFileArtifact(Connection con, long id, String path) throws SQLException
    {
        OriginalFeaturePersister.StoredFileArtifact fileArtifact = new OriginalFeaturePersister.StoredFileArtifact(path);
        ResultSet rs = null;
        try
        {
            selectFeatureByFileArtifactId.setLong(1, id);
            rs = selectFeatureByFileArtifactId.executeQuery();
            while (rs.next())
            {
                fileArtifact.addFeature(new OriginalFeaturePersister.PlainFeature(Feature.Level.valueOf(rs.getString("LEVEL")), rs.getString("SUMMARY"), rs.getLong("FIRST_LINE"), rs.getLong("LAST_LINE"), rs.getLong("LINE")));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }

        deleteFeatureByFileArtifactId.setLong(1, id);
        deleteFeatureByFileArtifactId.executeUpdate();
        return fileArtifact;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
