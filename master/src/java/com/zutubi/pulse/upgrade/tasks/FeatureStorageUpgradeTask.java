package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.upgrade.UpgradeContext;
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
public class FeatureStorageUpgradeTask extends DatabaseUpgradeTask implements ConfigurationAware
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

    public void execute(UpgradeContext context, Connection con) throws IOException, SQLException
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
    }

    private void prepareStatements(Connection con) throws SQLException
    {
        selectCommandResults = con.prepareStatement("SELECT id, command_name, outputDir FROM command_result", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        selectArtifactByCommandId = con.prepareStatement("SELECT id, name FROM artifact where command_result_id = ?");
        selectFileArtifactByArtifactId = con.prepareStatement("SELECT id, file FROM file_artifact where artifact_id = ?");
        selectFeatureByFileArtifactId = con.prepareStatement("SELECT id, level, first_line, last_line, line, summary FROM feature WHERE file_artifact_id = ?");
        deleteFeatureByFileArtifactId = con.prepareStatement("DELETE FROM feature WHERE file_artifact_id = ?");
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
                processCommandResult(con, JDBCUtils.getLong(rs, "id"), JDBCUtils.getString(rs, "command_name"), JDBCUtils.getString(rs, "outputDir"));
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
                    commandResult.addArtifact(processArtifact(con, rs.getLong("id"), rs.getString("name")));
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
                storedArtifact.addFile(processFileArtifact(con, rs.getLong("id"), rs.getString("file")));
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
                fileArtifact.addFeature(new OriginalFeaturePersister.PlainFeature(Feature.Level.valueOf(rs.getString("level")), rs.getString("summary"), rs.getLong("first_line"), rs.getLong("last_line"), rs.getLong("line")));
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
