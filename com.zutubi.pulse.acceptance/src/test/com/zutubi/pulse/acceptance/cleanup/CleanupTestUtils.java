package com.zutubi.pulse.acceptance.cleanup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import static com.zutubi.pulse.acceptance.Constants.Project.Cleanup.*;
import static com.zutubi.pulse.acceptance.Constants.Project.Options.RETAIN_WORKING_COPY;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.XmlRpcHelper;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupUnit;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * A set of utility methods used by the cleanup acceptance tests.
 */
public class CleanupTestUtils
{
    private XmlRpcHelper xmlRpcHelper;
    private Selenium selenium;
    private Urls urls;

    public CleanupTestUtils(XmlRpcHelper xmlRpcHelper, Selenium selenium, Urls urls)
    {
        this.xmlRpcHelper = xmlRpcHelper;
        this.selenium = selenium;
        this.urls = urls;
    }

    public void setRetainWorkingCopy(String projectName, boolean b) throws Exception
    {
        String optionsPath = "projects/" + projectName + "/options";
        Hashtable<String, Object> data = xmlRpcHelper.getConfig(optionsPath);
        data.put(RETAIN_WORKING_COPY, b);
        xmlRpcHelper.saveConfig(optionsPath, data, false);
    }

    public void deleteCleanupRule(String projectName, String name) throws Exception
    {
        String cleanupPath = "projects/" + projectName + "/cleanup/" + name;
        xmlRpcHelper.deleteConfig(cleanupPath);
    }

    public void addCleanupRule(String projectName, String name, CleanupWhat... whats) throws Exception
    {
        Hashtable<String, Object> data = xmlRpcHelper.createDefaultConfig(CleanupConfiguration.class);
        data.put(NAME, name);
        data.put(RETAIN, 1);
        data.put(UNIT, CleanupUnit.BUILDS.toString());
        if (whats != null && whats.length > 0)
        {
            Vector<String> vector = new Vector<String>();
            for (CleanupWhat w : whats)
            {
                vector.add(w.toString());
            }
            data.put(WHAT, vector);
            data.put(CLEANUP_ALL, false);
        }
        else
        {
            data.put(CLEANUP_ALL, true);
        }

        String cleanupPath = "projects/" + projectName + "/cleanup";
        xmlRpcHelper.insertConfig(cleanupPath, data);
    }
    
    public boolean hasBuild(String projectName, int buildNumber) throws Exception
    {
        return xmlRpcHelper.getBuild(projectName, buildNumber) != null;
    }

    public File getBuildDirectory(String projectName, int buildNumber) throws Exception
    {
        Hashtable<String, Object> projectConfig = xmlRpcHelper.getProject(projectName);
        long projectId = Long.parseLong((String) projectConfig.get("id"));

        File data = AcceptanceTestUtils.getDataDirectory();

        return new File(data, "projects/" + projectId + "/" + String.format("%08d", buildNumber));
    }

    public boolean hasBuildDirectory(String projectName, int buildNumber) throws Exception
    {
        return getBuildDirectory(projectName, buildNumber).isDirectory();
    }

    public boolean hasBuildWorkingCopy(String projectName, int buildNumber) throws Exception
    {
        return hasStageDirectory(projectName, buildNumber, "base");
    }

    public boolean hasBuildOutputDirectory(String projectName, int buildNumber) throws Exception
    {
        return hasStageDirectory(projectName, buildNumber, "output");
    }

    public boolean hasBuildFeaturesDirectory(String projectName, int buildNumber) throws Exception
    {
        return hasStageDirectory(projectName, buildNumber, "features");
    }

    private boolean hasStageDirectory(String projectName, int buildNumber, final String directoryName) throws Exception
    {
        File buildDir = getBuildDirectory(projectName, buildNumber);

        List<File> stageDirectories = CollectionUtils.filter(buildDir.listFiles(), new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return file.isDirectory();
            }
        });

        return CollectionUtils.contains(stageDirectories, new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return new File(file, directoryName).isDirectory();
            }
        });
    }

    public boolean isWorkingCopyPresentViaUI(String projectName, int buildNumber)
    {
        BuildWorkingCopyPage page = new BuildWorkingCopyPage(selenium, urls, projectName, buildNumber);
        page.goTo();
        return page.isWorkingCopyPresent();
    }

    public boolean isBuildPresentViaUI(String projectName, int buildNumber)
    {
        BuildSummaryPage page = new BuildSummaryPage(selenium, urls, projectName, buildNumber);
        return openPage(page);
    }

    public boolean isBuildPulseFilePresentViaUI(String projectName, int buildNumber)
    {
        BuildFilePage page = new BuildFilePage(selenium, urls, projectName, buildNumber);
        return openPage(page);
    }

    public boolean isBuildLogsPresentViaUI(String projectName, int buildNumber)
    {
        BuildDetailedViewPage page = new BuildDetailedViewPage(selenium, urls, projectName, buildNumber);
        if (!openPage(page))
        {
            return false;
        }
        if (!page.isBuildLogLinkPresent())
        {
            return false;
        }
        page.clickBuildLogLink();
        return selenium.isTextPresent("tail of build log");
    }

    public boolean isBuildArtifactsPresentViaUI(String projectName, int buildNumber)
    {
        BuildArtifactsPage page = new BuildArtifactsPage(selenium, urls, projectName, buildNumber);
        if (!openPage(page))
        {
            return false;
        }

        // if artifacts are available, we should have the build command open in the tree.
        SeleniumUtils.waitForLocator(selenium, page.getArtifactLocator("environment"));
        return page.isArtifactAvailable("environment");
    }

    private boolean openPage(SeleniumPage page)
    {
        selenium.open(page.getUrl());
        try
        {
            selenium.waitForPageToLoad("5000");
            SeleniumUtils.waitForElementId(selenium, page.getId());
            return true;
        }
        catch (RuntimeException e)
        {
            // failed to load, should see: Unknown build [buildNumber] for project [projectName]
            return false;
        }
    }
}
