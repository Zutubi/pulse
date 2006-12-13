package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <class comment/>
 */
public class FeatureCountUpgradeTask extends DatabaseUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(FeatureCountUpgradeTask.class);

    private PreparedStatement selectAllBuildResults;
    private PreparedStatement selectRecipeResultByParent;
    private PreparedStatement selectCommandResultByRecipeId;
    private PreparedStatement selectFeatureLevelByCommand;
    private PreparedStatement selectFeatureLevelByBuildResult;
    private PreparedStatement selectFeatureLevelByRecipeResult;
    private PreparedStatement selectFeatureLevelByCommandResult;
    private PreparedStatement updateFeatureCountOnRecipeResult;
    private PreparedStatement updateFeatureCountOnBuildResult;
    private PreparedStatement updateFeatureCountOnCommandResult;

    public String getName()
    {
        return "Feature count upgrade task.";
    }

    public String getDescription()
    {
        return "Evaluate the warning and error feature count fields for existing build results.";
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(UpgradeContext context, Connection con) throws IOException, SQLException
    {
        // for each build result - evaluate the warning and feature count
        //  a) sum warning / error count for associated features.
        //  b) add warning / error count for associated recipe tree.
        //     for each recipe node
        //         a) process the results features.
        //         b) loop over associated commands results.
        //            for each command result
        //                 a) pocess the results features.
        try
        {
            prepareStatements(con);
            processBuilds(con);
        }
        finally
        {
            closePreparedStatements();
        }
    }

    private void prepareStatements(Connection con) throws SQLException
    {
        selectAllBuildResults = con.prepareStatement("SELECT id, recipe_result_id FROM BUILD_RESULT");
        selectRecipeResultByParent = con.prepareStatement("SELECT recipe_result_id FROM recipe_result_node WHERE parent_id = ?");
        selectCommandResultByRecipeId = con.prepareStatement("SELECT id FROM command_result where recipe_result_id = ?");
        selectFeatureLevelByCommand = con.prepareStatement("SELECT feature.level AS level " +
                "FROM file_artifact, artifact, feature " +
                "WHERE ? = artifact.command_result_id " +
                "AND artifact.id = file_artifact.artifact_id " +
                "AND file_artifact.id = feature.file_artifact_id");

        selectFeatureLevelByBuildResult = con.prepareStatement("SELECT level FROM feature WHERE build_result_id = ?");
        selectFeatureLevelByRecipeResult = con.prepareStatement("SELECT level FROM feature WHERE recipe_result_id = ?");
        selectFeatureLevelByCommandResult = con.prepareStatement("SELECT level FROM feature WHERE command_result_id = ?");

        updateFeatureCountOnCommandResult = con.prepareStatement("UPDATE command_result SET error_feature_count = ?, warning_feature_count = ? WHERE id = ?");
        updateFeatureCountOnBuildResult = con.prepareStatement("UPDATE build_result SET error_feature_count = ?, warning_feature_count = ? WHERE id = ?");
        updateFeatureCountOnRecipeResult = con.prepareStatement("UPDATE recipe_result SET error_feature_count = ?, warning_feature_count = ? WHERE id = ?");
    }

    private void closePreparedStatements()
    {
        JDBCUtils.close(selectAllBuildResults);
        JDBCUtils.close(selectRecipeResultByParent);
        JDBCUtils.close(selectCommandResultByRecipeId);
        JDBCUtils.close(selectFeatureLevelByCommand);

        JDBCUtils.close(selectFeatureLevelByBuildResult);
        JDBCUtils.close(selectFeatureLevelByRecipeResult);
        JDBCUtils.close(selectFeatureLevelByCommandResult);

        JDBCUtils.close(updateFeatureCountOnBuildResult);
        JDBCUtils.close(updateFeatureCountOnRecipeResult);
        JDBCUtils.close(updateFeatureCountOnCommandResult);
    }

    private void processBuilds(Connection con) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            rs = selectAllBuildResults.executeQuery();
            while (rs.next())
            {
                processBuildResult(con, JDBCUtils.getLong(rs, "id"), JDBCUtils.getLong(rs, "recipe_result_id"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private void processBuildResult(Connection con, Long id, Long rootNodeId) throws SQLException
    {
        FeatureCount count = new FeatureCount();

        processFeatureCount(selectFeatureLevelByBuildResult, id, count);

        processRecipeResultHierarchy(con, rootNodeId, count);

        // update the build result.
        updateFeatureCount(updateFeatureCountOnBuildResult, id, count);
    }

    private void processRecipeResultHierarchy(Connection con, Long id, FeatureCount count) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            selectRecipeResultByParent.setLong(1, id);
            rs = selectRecipeResultByParent.executeQuery();
            while (rs.next())
            {
                processRecipe(con, JDBCUtils.getLong(rs, "recipe_result_id"), count);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private void processRecipe(Connection con, Long id, FeatureCount count) throws SQLException
    {
        FeatureCount recipeCount = new FeatureCount();

        processFeatureCount(selectFeatureLevelByRecipeResult, id, recipeCount);

        // count the associated command result features.
        ResultSet rs = null;

        try
        {
            JDBCUtils.setLong(selectCommandResultByRecipeId, 1, id);
            rs = selectCommandResultByRecipeId.executeQuery();
            while (rs.next())
            {
                processCommand(con, JDBCUtils.getLong(rs, "id"), recipeCount);
            }

        }
        finally
        {
            JDBCUtils.close(rs);
        }

        // update the process recipe result count.
        updateFeatureCount(updateFeatureCountOnRecipeResult, id, recipeCount);

        count.warningCount += recipeCount.warningCount;
        count.errorCount += recipeCount.errorCount;
        count.featureCount += recipeCount.featureCount;
    }

    private void processCommand(Connection con, Long id, FeatureCount count) throws SQLException
    {
        FeatureCount commandCount = new FeatureCount();


        processFeatureCount(selectFeatureLevelByCommandResult, id, commandCount);

        ResultSet rs = null;

        try
        {
            JDBCUtils.setLong(selectFeatureLevelByCommand, 1, id);
            rs = selectFeatureLevelByCommand.executeQuery();
            while (rs.next())
            {
                String level = JDBCUtils.getString(rs, "level");
                if (level.equals("WARNING"))
                {
                    commandCount.warningCount++;
                }
                else if (level.equals("ERROR"))
                {
                    commandCount.errorCount++;
                }
                commandCount.featureCount++;
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }

        // update the process recipe result count.
        updateFeatureCount(updateFeatureCountOnCommandResult, id, commandCount);

        count.warningCount += commandCount.warningCount;
        count.errorCount += commandCount.errorCount;
        count.featureCount += commandCount.featureCount;
    }

    private void updateFeatureCount(PreparedStatement stmt, Long id, FeatureCount count) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            JDBCUtils.setInt(stmt, 1, count.errorCount);
            JDBCUtils.setInt(stmt, 2, count.warningCount);
            JDBCUtils.setLong(stmt, 3, id);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private void processFeatureCount(PreparedStatement stmt, Long id, FeatureCount count) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            while (rs.next())
            {
                String level = JDBCUtils.getString(rs, "level");
                count.featureCount++;
                if (level.equals("WARNING"))
                {
                    count.warningCount++;
                }
                else if (level.equals("ERROR"))
                {
                    count.errorCount++;
                }
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private class FeatureCount
    {
        int featureCount;
        int warningCount;
        int errorCount;
    }

}
