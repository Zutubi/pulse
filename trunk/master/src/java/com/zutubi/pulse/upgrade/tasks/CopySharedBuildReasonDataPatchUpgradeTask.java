package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.upgrade.UpgradeContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class CopySharedBuildReasonDataPatchUpgradeTask extends DatabaseUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(CopySharedBuildReasonDataPatchUpgradeTask.class);

    public String getName()
    {
        return "Copy shared build reason data patch.";
    }

    public String getDescription()
    {
        return "This upgrade tasks fixes data problems resulting from build reasons being shared " +
                "by multiple build results. See CIB-723 for details.";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        // Step A) Locate the build reasons referenced by multiple build results.
        List<Long> reasons = new LinkedList<Long>();

        PreparedStatement select = null;
        ResultSet rs = null;
        try
        {
            String selectClonedScms = "SELECT reason FROM build_result GROUP BY reason HAVING count(reason) > 1";
            select = con.prepareStatement(selectClonedScms);
            rs = select.executeQuery();
            while (rs.next())
            {
                reasons.add(JDBCUtils.getLong(rs, "reason"));
            }
        }
        finally
        {
            JDBCUtils.close(select);
            JDBCUtils.close(rs);
        }

        // For each of these build reasons, load the data.
        List<Reason> reasonData = new LinkedList<Reason>();
        try
        {
            String selectScm = "SELECT * FROM build_reason WHERE id = ?";
            select = con.prepareStatement(selectScm);
            for (long scmId : reasons)
            {
                JDBCUtils.setLong(select, 1, scmId);
                rs = select.executeQuery();
                if (rs.next())
                {
                    reasonData.add(new CopySharedBuildReasonDataPatchUpgradeTask.Reason(rs));
                }
                JDBCUtils.close(rs);
            }
        }
        finally
        {
            JDBCUtils.close(select);
            JDBCUtils.close(rs);
        }

        PreparedStatement clone = null;
        PreparedStatement update = null;
        try
        {
            String selectBuildResults = "SELECT id FROM build_result WHERE reason = ?";
            select = con.prepareStatement(selectBuildResults);

            String cloneBuildReason = "INSERT INTO build_reason (id, reason_type, details, user) VALUES (?, ?, ?, ?)";
            clone = con.prepareStatement(cloneBuildReason);

            String updateBuildResult = "UPDATE build_result SET reason = ? WHERE id = ?";
            update = con.prepareStatement(updateBuildResult);

            long nextId = HibernateUtils.getNextId(con);

            for (Reason reason : reasonData)
            {
                try
                {
                    JDBCUtils.setLong(select, 1, reason.id);
                    rs = select.executeQuery();
                    
                    // ignore the first build result, for the rest, clone the scm and update the project reference.
                    rs.next();
                    while (rs.next())
                    {
                        long resultId = JDBCUtils.getLong(rs, "id");

                        // clone the build reason.
                        JDBCUtils.setLong(clone, 1, nextId);
                        JDBCUtils.setString(clone, 2, reason.reasonType);
                        JDBCUtils.setString(clone, 3, reason.details);
                        JDBCUtils.setString(clone, 4, reason.user);

                        if (clone.executeUpdate() != 1)
                        {
                            LOG.error("Failed to insert new build reason.");
                        }

                        // update build result reference.
                        JDBCUtils.setLong(update, 1, nextId);
                        JDBCUtils.setLong(update, 2, resultId);
                        if (update.executeUpdate() != 1)
                        {
                            LOG.error("Failed to update build result "+resultId+" to reference new build reason "+nextId+".");
                        }

                        nextId = HibernateUtils.getNextId(con);
                    }
                }
                finally
                {
                    JDBCUtils.close(rs);
                }
            }

            HibernateUtils.ensureNextId(con, nextId);
        }
        finally
        {
            JDBCUtils.close(select);
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
    private class Reason
    {
        public long id;
        public String reasonType;
        public String details;
        public String user;

        public Reason(ResultSet rs) throws SQLException
        {
            id = JDBCUtils.getLong(rs, "id");
            reasonType = JDBCUtils.getString(rs, "reason_type");
            details = JDBCUtils.getString(rs, "details");
            user = JDBCUtils.getString(rs, "user");
        }
    }
}
