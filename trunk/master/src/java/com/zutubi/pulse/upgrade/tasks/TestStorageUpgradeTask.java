package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.File;
import java.io.IOException;

/**
 */
public class TestStorageUpgradeTask extends DatabaseUpgradeTask implements ConfigurationAware
{
    private static final Logger LOG = Logger.getLogger(TestStorageUpgradeTask.class);
    private MasterConfigurationManager configurationManager;
    private long nextId;
    private TestSuitePersister persister = new TestSuitePersister();

    public String getName()
    {
        return "Test storage";
    }

    public String getDescription()
    {
        return "Moves test results from the database to disk and improves layout and performance for large test suites";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        // For every recipe, use the current data to construct a new,
        // TestSuiteResult.  This can be used to calculate the new test
        // summary (stored in the DB) and then be persisted to disk in
        // the new format.

        // In the old schema, each file artifact had a list of test results.
        // Test results are cases or suites, and suites themselves have a list
        // of child results.  Thus the TEST_RESULT table has two FK columns:
        //
        //   - FILE_ARTIFACT_ID: the id of the owning artifacgt, if any; and
        //   - SUITE_ID: the id of the owning suite, if any
        //
        // To match the tests up to the recipe in question, we need to trace
        // the file artifact back to the artifact (ARTIFACT_ID->ARTIFACT), the
        // artifact back to the command (COMMAND_RESULT_ID->COMMAND_RESULT),
        // and the command back to the recipe (RECIPE_RESULT_ID->RECIPE_RESULT).
        //
        // So, for each recipe result:
        //   - create a new empty test suite
        //   - for each command
        //       for each artifact
        //         for each file artifact
        //            for each test result
        //              if result is a case
        //                -> add to test suite
        //              else (it is a suite)
        //                -> load all children (naturally recursive)
        //                -> add to test suite

        nextId = HibernateUtils.getNextId(con);
        processRecipes(con);

        PreparedStatement stmt = null;

        try
        {
            stmt = con.prepareCall("DROP TABLE test_result");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void processRecipes(Connection con) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, outputDir FROM RECIPE_RESULT");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                processRecipe(con, JDBCUtils.getLong(rs, "id"), JDBCUtils.getString(rs, "outputDir"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void processRecipe(Connection con, Long id, String outputDir)
    {
        TestSuiteResult testResults = new TestSuiteResult();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id FROM COMMAND_RESULT where RECIPE_RESULT_ID = ?");
            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            while(rs.next())
            {
                processCommand(con, JDBCUtils.getLong(rs, "id"), testResults);
            }

            insertTestSummary(con, id, testResults);
            saveTestResults(outputDir, testResults);
        }
        catch(Exception e)
        {
            LOG.warning("Unable to convert test results for recipe " + id + ": " + e.getMessage(), e);
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void saveTestResults(String outputDir, TestSuiteResult testResults) throws IOException
    {
        File absoluteOutput = new File(configurationManager.getDataDirectory(), outputDir);
        File testDir = new File(absoluteOutput, RecipeResult.TEST_DIR);

        if(!testDir.isDirectory() && !testDir.mkdirs())
        {
            throw new IOException("Unable to create test directory '" + testDir.getAbsolutePath() + "'");
        }

        persister.write(testResults, testDir);
    }

    private void insertTestSummary(Connection con, Long recipeId, TestSuiteResult testResults) throws SQLException
    {
        TestResultSummary summary = testResults.getSummary();
        PreparedStatement stmt = null;
        long id = nextId++;

        try
        {
            stmt = con.prepareCall("INSERT INTO test_result_summary VALUES (?, ?, ?, ?)");
            JDBCUtils.setLong(stmt, 1, id);
            JDBCUtils.setInt(stmt, 2, summary.getErrors());
            JDBCUtils.setInt(stmt, 3, summary.getFailures());
            JDBCUtils.setInt(stmt, 4, summary.getTotal());
            stmt.executeUpdate();

            JDBCUtils.close(stmt);
            stmt = con.prepareCall("UPDATE recipe_result SET test_summary_id = ? WHERE id = ?");
            JDBCUtils.setLong(stmt, 1, id);
            JDBCUtils.setLong(stmt, 2, recipeId);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void processCommand(Connection con, Long id, TestSuiteResult testResults) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id FROM ARTIFACT where COMMAND_RESULT_ID = ?");
            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            while(rs.next())
            {
                processArtifact(con, JDBCUtils.getLong(rs, "id"), testResults);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void processArtifact(Connection con, Long id, TestSuiteResult testResults) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id FROM FILE_ARTIFACT where ARTIFACT_ID = ?");
            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            while(rs.next())
            {
                processFileArtifact(con, JDBCUtils.getLong(rs, "id"), testResults);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void processFileArtifact(Connection con, Long id, TestSuiteResult testResults) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, type, name, duration, statusname, message FROM TEST_RESULT where FILE_ARTIFACT_ID = ?");
            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            while(rs.next())
            {
                processTestResult(con, getResultInfo(rs), testResults);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

    }

    private void processTestResult(Connection con, TestResultInfo info, TestSuiteResult suite) throws SQLException
    {
        if(info.isSuite())
        {
            TestSuiteResult childSuite = info.createSuite();

            // Load the childrens.
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try
            {
                stmt = con.prepareCall("SELECT id, type, name, duration, statusname, message FROM TEST_RESULT where SUITE_ID = ?");
                JDBCUtils.setLong(stmt, 1, info.id);
                rs = stmt.executeQuery();
                while(rs.next())
                {
                    processTestResult(con, getResultInfo(rs), childSuite);
                }
            }
            finally
            {
                JDBCUtils.close(rs);
                JDBCUtils.close(stmt);
            }

            suite.add(childSuite);
        }
        else
        {
            suite.add(info.createCase());
        }
    }

    private TestResultInfo getResultInfo(ResultSet rs) throws SQLException
    {
        return new TestResultInfo(rs.getLong("id"), rs.getString("type"), rs.getString("name"), rs.getLong("duration"), rs.getString("statusname"), rs.getString("message"));
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    private class TestResultInfo
    {
        long id;
        String type;
        String name;
        long duration;
        TestCaseResult.Status status;
        String message;

        public TestResultInfo(long id, String type, String name, long duration, String statusName, String message)
        {
            this.id = id;
            this.type = type;
            this.name = name;
            this.duration = duration;

            if(statusName != null)
            {
                this.status = TestCaseResult.Status.valueOf(statusName);
            }

            this.message = message;
        }

        public boolean isSuite()
        {
            return type.equals("SUITE");
        }

        public TestSuiteResult createSuite()
        {
            return new TestSuiteResult(name, duration);
        }

        public TestCaseResult createCase()
        {
            return new TestCaseResult(name, duration, status, message);
        }
    }
}
