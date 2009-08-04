package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.TestSuitePersister;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.util.*;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public class ViewTestSuiteAction extends StageActionBase
{
    private static final Logger LOG = Logger.getLogger(ViewTestSuiteAction.class);

    private String path;
    private PersistentTestSuiteResult suite;
    private List<String> paths;
    private MasterConfigurationManager configurationManager;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public PersistentTestSuiteResult getSuite()
    {
        return suite;
    }

    public List<String> getPaths()
    {
        return paths;
    }

    public String getChildUriPath(String childName)
    {
        String uriChildName = StringUtils.uriComponentEncode(childName);
        if (paths == null)
        {
            return uriChildName;
        }
        else
        {
            String uriPath = StringUtils.join("/", CollectionUtils.map(paths, new Mapping<String, String>()
            {
                public String map(String s)
                {
                    return StringUtils.uriComponentEncode(s);
                }
            }));

            return uriPath + "/" + uriChildName;
        }
    }

    public void validate()
    {

    }

    public String execute()
    {
        RecipeResultNode node = getRequiredRecipeResultNode();
        if(node.getResult() == null)
        {
            addActionError("Invalid stage [" + getStageName() + "]");
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
            paths = CollectionUtils.map(elements, new Mapping<String, String>()
            {
                public String map(String s)
                {
                    return StringUtils.uriComponentDecode(s);
                }
            });

            String[] encodedElements = CollectionUtils.mapToArray(paths, new Mapping<String, String>()
            {
                public String map(String s)
                {
                    return urlEncode(s);
                }
            }, new String[elements.length]);
            testDir = new File(testDir, FileSystemUtils.composeFilename(encodedElements));
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
