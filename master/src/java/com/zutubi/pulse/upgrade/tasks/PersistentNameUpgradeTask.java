package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class PersistentNameUpgradeTask extends DatabaseUpgradeTask
{
    private long nextId;

    public String getName()
    {
        return "Persistent names.";
    }

    public String getDescription()
    {
        return "Changes the way build specifications and stages are associated with build results.";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        nextId = HibernateUtils.getNextId(con);

        List<Long> projects = getAllProjects(con);
        for(Long project: projects)
        {
            upgradeProject(con, new ProjectInfo(project));
        }

        dropColumns(con);
        HibernateUtils.ensureNextId(con, nextId);
    }

    private void upgradeProject(Connection con, ProjectInfo projectInfo) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // First, create persistent names for all existing specs and stages
            stmt = con.prepareCall("SELECT id, name, root_id FROM build_specification WHERE project_id = ?");
            stmt.setLong(1, projectInfo.id);

            rs = stmt.executeQuery();
            while(rs.next())
            {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                long rootId = rs.getLong("root_id");

                long nameId = createName(con, name);
                linkName(con, "build_specification", "pname", id, nameId);

                SpecInfo info = new SpecInfo(id, rootId, nameId, name);
                projectInfo.addSpec(info);

                upgradeSpec(con, info);
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }

        // Now upgrade this project's builds
        upgradeBuilds(con, projectInfo);
    }

    private void upgradeSpec(Connection con, SpecInfo info) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // First, create persistent names for all existing specs and stages
            stmt = con.prepareCall("SELECT id, name FROM build_specification_node WHERE parent_id = ?");
            stmt.setLong(1, info.rootId);

            rs = stmt.executeQuery();
            while(rs.next())
            {
                long id = rs.getLong("id");
                String name = rs.getString("name");

                long nameId = createName(con, name);
                linkName(con, "build_specification_node", "pname", id, nameId);

                StageInfo stageInfo = new StageInfo(id, nameId, name);
                info.addStage(stageInfo);
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private void upgradeBuilds(Connection con, ProjectInfo projectInfo) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, build_spec, recipe_result_id FROM build_result WHERE project = ?");
            stmt.setLong(1, projectInfo.id);

            rs = stmt.executeQuery();
            while(rs.next())
            {
                long id = rs.getLong("id");
                String spec = rs.getString("build_spec");
                long rootId = rs.getLong("recipe_result_id");

                SpecInfo specInfo = getSpecInfo(con, projectInfo, spec);
                linkName(con, "build_result", "spec_name", id, specInfo.nameId);
                upgradeRecipes(con, rootId, specInfo);
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private SpecInfo getSpecInfo(Connection con, ProjectInfo projectInfo, String spec) throws SQLException
    {
        SpecInfo result = projectInfo.getSpec(spec);
        if(result == null)
        {
            // We need to create one, with a persistent name.
            long nameId = createName(con, spec);
            result = new SpecInfo(0, 0, nameId, spec);
            projectInfo.addSpec(result);
        }

        return result;
    }

    private void upgradeRecipes(Connection con, long rootId, SpecInfo specInfo) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, stage FROM recipe_result_node WHERE parent_id = ?");
            stmt.setLong(1, rootId);

            rs = stmt.executeQuery();
            while(rs.next())
            {
                long id = rs.getLong("id");
                String stage = rs.getString("stage");

                StageInfo stageInfo = getStageInfo(con, specInfo, stage);
                linkName(con, "recipe_result_node", "stage_name", id, stageInfo.nameId);
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private StageInfo getStageInfo(Connection con, SpecInfo specInfo, String stage) throws SQLException
    {
        StageInfo result = specInfo.getStage(stage);
        if(result == null)
        {
            // We need to create one, with a persistent name.
            long nameId = createName(con, stage);
            result = new StageInfo(0, nameId, stage);
            specInfo.addStage(result);
        }

        return result;
    }

    private long createName(Connection con, String name) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            long id = nextId++;
            stmt = con.prepareCall("INSERT INTO persistent_name VALUES (?, ?)");
            stmt.setLong(1, id);
            stmt.setString(2, name);
            stmt.executeUpdate();
            return id;
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void linkName(Connection con, String table, String column, long id, long nameId) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("UPDATE " + table + " SET " + column + " = ? WHERE id = ?");
            stmt.setLong(1, nameId);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }

    }

    private void dropColumns(Connection con) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("ALTER TABLE build_specification DROP COLUMN name");
            stmt.executeUpdate();
            stmt.close();

            stmt = con.prepareCall("ALTER TABLE build_specification_node DROP COLUMN name");
            stmt.executeUpdate();
            stmt.close();

            stmt = con.prepareCall("ALTER TABLE build_result DROP COLUMN build_spec");
            stmt.executeUpdate();
            stmt.close();

            stmt = con.prepareCall("ALTER TABLE recipe_result_node DROP COLUMN stage");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    private class ProjectInfo
    {
        public long id;
        public Map<String, SpecInfo> specs;

        public ProjectInfo(long id)
        {
            this.id = id;
            specs = new HashMap<String, SpecInfo>();
        }

        public void addSpec(SpecInfo info)
        {
            specs.put(info.name, info);
        }

        public SpecInfo getSpec(String name)
        {
            return specs.get(name);
        }
    }

    private class SpecInfo
    {
        public long id;
        public long rootId;
        public long nameId;
        public String name;
        public Map<String, StageInfo> stages;

        public SpecInfo(long id, long rootId, long nameId, String name)
        {
            this.id = id;
            this.rootId = rootId;
            this.nameId = nameId;
            this.name = name;
            stages = new HashMap<String, StageInfo>();
        }

        public void addStage(StageInfo stageInfo)
        {
            stages.put(stageInfo.name, stageInfo);
        }

        public StageInfo getStage(String stage)
        {
            return stages.get(stage);
        }
    }

    private class StageInfo
    {
        public long id;
        public long nameId;
        public String name;

        public StageInfo(long id, long nameId, String name)
        {
            this.id = id;
            this.nameId = nameId;
            this.name = name;
        }
    }
}
