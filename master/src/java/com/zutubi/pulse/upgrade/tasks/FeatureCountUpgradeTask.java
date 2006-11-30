package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.core.model.TestSuiteResult;

import java.sql.*;
import java.io.IOException;

/**
 * <class comment/>
 */
public class FeatureCountUpgradeTask extends DatabaseUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(FeatureCountUpgradeTask.class);

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

        processBuilds(con);
    }

    private void processBuilds(Connection con) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, recipe_result_id FROM BUILD_RESULT");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                processBuildResult(con, JDBCUtils.getLong(rs, "id"), JDBCUtils.getLong(rs, "recipe_result_id"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void processBuildResult(Connection con, Long id, Long rootNodeId) throws SQLException
    {
        FeatureCount count = new FeatureCount();
        processFeatureCount(con, "build_result_id", id, count);

        processRecipeResultHierarchy(con, rootNodeId, count);

        // update the build result.
        updateFeatureCount(con, "build_result", id, count);
    }

    private void processRecipeResultHierarchy(Connection con, Long id, FeatureCount count) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT recipe_result_id FROM recipe_result_node WHERE parent_id = ?");
            stmt.setLong(1, id);

            rs = stmt.executeQuery();
            while(rs.next())
            {
                processRecipe(con, JDBCUtils.getLong(rs, "recipe_result_id"), count);
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private void processRecipe(Connection con, Long id, FeatureCount count) throws SQLException
    {
        FeatureCount recipeCount = new FeatureCount();
        processFeatureCount(con, "recipe_result_id", id, recipeCount);

        // count the associated command result features.
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id FROM command_result where recipe_result_id = ?");
            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            while(rs.next())
            {
                processCommand(con, JDBCUtils.getLong(rs, "id"), recipeCount);
            }

        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        // update the process recipe result count.
        updateFeatureCount(con, "recipe_result", id, recipeCount);

        count.warningCount += recipeCount.warningCount;
        count.errorCount += recipeCount.errorCount;
    }

    private void processCommand(Connection con, Long id, FeatureCount count) throws SQLException
    {
        FeatureCount commandCount = new FeatureCount();
        processFeatureCount(con, "command_result_id", id, commandCount);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT feature.level AS level " +
                    "FROM file_artifact, artifact, feature " +
                    "WHERE ? = artifact.command_result_id " +
                    "AND artifact.id = file_artifact.artifact_id " +
                    "AND file_artifact.id = feature.file_artifact_id");

            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            while(rs.next())
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
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        // update the process recipe result count.
        updateFeatureCount(con, "command_result", id, commandCount);

        count.warningCount += commandCount.warningCount;
        count.errorCount += commandCount.errorCount;
    }

    private void updateFeatureCount(Connection con, String tableName, Long id, FeatureCount count) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareCall("UPDATE "+tableName+" SET error_feature_count = ?, warning_feature_count = ? WHERE id = ?");
            JDBCUtils.setInt(stmt, 1, count.errorCount);
            JDBCUtils.setInt(stmt, 2, count.warningCount);
            JDBCUtils.setLong(stmt, 3, id);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void processFeatureCount(Connection con, String foreignKeyCol, Long id, FeatureCount count) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareCall("SELECT level FROM feature WHERE " + foreignKeyCol + " = ?");
            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            while(rs.next())
            {
                String level = JDBCUtils.getString(rs, "level");
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
            JDBCUtils.close(stmt);
        }
    }

    private class FeatureCount
    {
        int warningCount;
        int errorCount;
    }

}
