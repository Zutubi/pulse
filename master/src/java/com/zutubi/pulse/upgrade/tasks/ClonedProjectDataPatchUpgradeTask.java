package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class ClonedProjectDataPatchUpgradeTask extends DatabaseUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(ClonedProjectDataPatchUpgradeTask.class);

    public String getName()
    {
        return "Cloned SCM data patch.";
    }

    public String getDescription()
    {
        return "This upgrade tasks fixes data problems resulting from cloned projects not cloning " +
                "the scm details correctly. See CIB-409 for details.";
    }

    public void execute(Connection con) throws SQLException
    {
        // locate projects referencing cloned scms.
        List<Long> clonedScms = new LinkedList<Long>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String selectClonedScms = "SELECT scm FROM project GROUP BY scm HAVING count(scm) > 1";
            ps = con.prepareStatement(selectClonedScms);
            rs = ps.executeQuery();
            while (rs.next())
            {
                clonedScms.add(JDBCUtils.getLong(rs, "scm"));
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        // load the scm data.
        List<Scm> scmData = new LinkedList<Scm>();
        try
        {
            String selectScm = "SELECT * FROM scm WHERE id = ?";
            ps = con.prepareStatement(selectScm);
            for (long scmId : clonedScms)
            {
                JDBCUtils.setLong(ps, 1, scmId);
                rs = ps.executeQuery();
                if (rs.next())
                {
                    scmData.add(new Scm(rs));
                }
                JDBCUtils.close(rs);
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        PreparedStatement clone = null;
        PreparedStatement update = null;
        try
        {
            String selectProjects = "SELECT id FROM project WHERE scm = ?";
            ps = con.prepareStatement(selectProjects);

            String cloneScm = "INSERT INTO scm (id, scmtype, path, properties) VALUES (?, ?, ?, ?)";
            clone = con.prepareStatement(cloneScm);

            String updateProject = "UPDATE project SET scm = ? WHERE id = ?";
            update = con.prepareStatement(updateProject);

            for (Scm scm : scmData)
            {

                try
                {
                    JDBCUtils.setLong(ps, 1, scm.id);
                    rs = ps.executeQuery();
                    // ignore the first project, for the rest, clone the scm and update the project reference.
                    rs.next();
                    while (rs.next())
                    {
                        long projectId = JDBCUtils.getLong(rs, "id");
                        long nextId = HibernateUtils.getNextId(con);
                        // clone the scm.
                        JDBCUtils.setLong(clone, 1, nextId);
                        JDBCUtils.setString(clone, 2, scm.scmtype);
                        JDBCUtils.setString(clone, 3, scm.path);
//                        JDBCUtils.setLong(clone, 5, null);
                        if (clone.executeUpdate() != 1)
                        {
                            LOG.error("Failed to insert new scm.");
                        }

                        // update project reference.
                        JDBCUtils.setLong(update, 1, nextId);
                        JDBCUtils.setLong(update, 2, projectId);
                        if (update.executeUpdate() != 1)
                        {
                            LOG.error("Failed to update project "+projectId+" to reference new scm "+nextId+".");
                        }
                    }
                }
                finally
                {
                    JDBCUtils.close(rs);
                }
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(clone);
            JDBCUtils.close(update);
        }
    }

    /**
     * Failure of this task does not cause a critical failure. More a potential annoyance.
     *
     * @return false
     */
    public boolean haltOnFailure()
    {
        return false;
    }

    /**
     * Data holder.
     */
    private class Scm
    {
        public long id;
        public String scmtype;
        public String path;

        public Scm(ResultSet rs) throws SQLException
        {
            id = JDBCUtils.getLong(rs, "id");
            scmtype = JDBCUtils.getString(rs, "scmtype");
            path = JDBCUtils.getString(rs, "path");
        }
    }
}
