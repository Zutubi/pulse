package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.TestSuitePersister;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 *
 */
public class ViewTestSuiteAction extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(ViewTestSuiteAction.class);

    private long id;
    private long nodeId;
    private String path;
    private BuildResult result;
    private RecipeResultNode node;
    private TestSuiteResult suite;
    private List<String> paths;
    private MasterConfigurationManager configurationManager;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(long nodeId)
    {
        this.nodeId = nodeId;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public Project getProject()
    {
        return result.getProject();
    }

    public BuildResult getResult()
    {
        return result;
    }

    public RecipeResultNode getNode()
    {
        return node;
    }

    public TestSuiteResult getSuite()
    {
        return suite;
    }

    public List<String> getPaths()
    {
        return paths;
    }

    public void validate()
    {

    }

    public String execute()
    {
        result = getBuildManager().getBuildResult(id);
        if (result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        node = result.findResultNode(nodeId);
        if(node == null || node.getResult() == null)
        {
            addActionError("Unknown recipe node [" + nodeId + "]");
            return ERROR;
        }

        File testDir = new File(node.getResult().getAbsoluteOutputDir(configurationManager.getDataDirectory()), RecipeResult.TEST_DIR);
        if(path != null && path.startsWith("/"))
        {
            path = path.substring(1);
        }

        if(TextUtils.stringSet(path))
        {
            String[] elements = path.split("/");
            paths = Arrays.asList(elements);
            testDir = new File(testDir, FileSystemUtils.composeFilename(elements));
        }

        TestSuitePersister persister = new TestSuitePersister();
        try
        {
            suite = persister.read(null, testDir, false, false, -1);
        }
        catch (Exception e)
        {
            LOG.warning(e);
            addActionError("Unable to read test suite from directory '" + testDir.getAbsolutePath() + "': " + e.getMessage());
            return ERROR;
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
