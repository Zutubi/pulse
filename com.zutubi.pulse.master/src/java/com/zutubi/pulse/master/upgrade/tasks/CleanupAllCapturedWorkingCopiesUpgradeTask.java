package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.upgrade.ConfigurationAware;
import com.zutubi.pulse.master.upgrade.DataSourceAware;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.servercore.cleanup.FileDeletionService;
import com.zutubi.util.io.DirectoryFileFilter;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * CIB-2365 removes the concept of the working directory as a separately managed
 * artifact from a build.  Therefore we should go through and clean everything up.
 */
public class CleanupAllCapturedWorkingCopiesUpgradeTask extends AbstractUpgradeTask implements ConfigurationAware, DataSourceAware
{
    private FileDeletionService fileDeletionService;
    private MasterConfigurationManager configurationManager;
    private DataSource dataSource;

    public void execute() throws TaskException
    {
        try
        {
            File data = configurationManager.getDataDirectory();

            List<ProjectBuild> buildsWithWorkingCopies = lookupBuildsWithWorkingCopies();
            for (ProjectBuild build : buildsWithWorkingCopies)
            {
                File buildDir = new File(data, "projects/" + build.projectId + "/" + String.format("%08d", build.buildNumber));
                for (File recipeDir : buildDir.listFiles(new DirectoryFileFilter()))
                {
                    fileDeletionService.delete(new File(recipeDir, "base"));
                }
            }
        }
        catch (SQLException e)
        {
            throw new TaskException(e);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    private List<ProjectBuild> lookupBuildsWithWorkingCopies() throws SQLException
    {
        List<ProjectBuild> buildsWithWorkingCopies = new LinkedList<ProjectBuild>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            con = dataSource.getConnection();
            stmt = con.prepareStatement("select project, number from BUILD_RESULT where BUILD_RESULT.hasWorkDir = true");
            rs = stmt.executeQuery();
            while (rs.next())
            {
                buildsWithWorkingCopies.add(new ProjectBuild(rs.getLong(1), rs.getLong(2)));
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(con);
            JDBCUtils.close(rs);
        }
        return buildsWithWorkingCopies;
    }

    public void setDataSource(DataSource source)
    {
        this.dataSource = source;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setFileDeletionService(FileDeletionService fileDeletionService)
    {
        this.fileDeletionService = fileDeletionService;
    }

    /**
     * A simple value holder for a builds number and the builds associated project id.
     */
    private class ProjectBuild
    {
        private long projectId;
        private long buildNumber;

        private ProjectBuild(long projectId, long buildNumber)
        {
            this.projectId = projectId;
            this.buildNumber = buildNumber;
        }
    }
}
