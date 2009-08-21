package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import static com.zutubi.pulse.core.dependency.ivy.IvyManager.STATUS_MILESTONE;
import com.zutubi.pulse.core.engine.api.ResultState;
import static com.zutubi.pulse.master.model.Project.State.IDLE;
import static com.zutubi.pulse.master.model.Project.State.BUILDING;
import static com.zutubi.pulse.master.tove.config.project.DependencyConfiguration.*;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.FileSystemUtils;

import java.io.File;

public class RebuildDependenciesAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private File tmpDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        Repository repository = new Repository();
        repository.clear();

        tmpDir = FileSystemUtils.createTempDir(randomName());
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        logout();

        super.tearDown();
    }

    public void testRebuildSingleDependency() throws Exception
    {
        String projectName = randomName();
        WaitAntProjectHelper projectA = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "A");
        projectA.createProject();

        WaitAntProjectHelper projectB = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "B");
        projectB.addDependency(projectA);
        projectB.createProject();

        projectB.triggerRebuild();

        // expect projectA to be building, projectB to be pending_dependency.
        xmlRpcHelper.waitForBuildInProgress(projectA.getName(), 1);

        assertEquals(ResultState.PENDING_DEPENDENCY, projectB.getBuildStatus(1));

        projectA.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectA.getName(), 1);

        assertEquals(ResultState.SUCCESS, projectA.getBuildStatus(1));

        // expect projectB to be building.
        xmlRpcHelper.waitForBuildInProgress(projectB.getName(), 1);
        projectB.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        assertEquals(ResultState.SUCCESS, projectB.getBuildStatus(1));
    }

    public void testRebuildMultipleDependencies() throws Exception
    {
        String projectName = randomName();
        WaitAntProjectHelper projectA = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "A");
        projectA.createProject();

        WaitAntProjectHelper projectB = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "B");
        projectB.createProject();

        WaitAntProjectHelper projectC = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "C");
        projectC.addDependency(projectA);
        projectC.addDependency(projectB);
        projectC.createProject();

        projectC.triggerRebuild();

        WaitAntProjectHelper firstDependency;
        WaitAntProjectHelper secondDependency;
        if (projectA.getState() == BUILDING)
        {
            firstDependency = projectA;
            secondDependency = projectB;
        }
        else
        {
            firstDependency = projectB;
            secondDependency = projectA;
        }

        xmlRpcHelper.waitForBuildInProgress(firstDependency.getName(), 1);
        assertEquals(ResultState.IN_PROGRESS, firstDependency.getBuildStatus(1));
        assertEquals(ResultState.PENDING, secondDependency.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectC.getBuildStatus(1));
        firstDependency.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(firstDependency.getName(), 1);

        xmlRpcHelper.waitForBuildInProgress(secondDependency.getName(), 1);
        assertEquals(ResultState.SUCCESS, firstDependency.getBuildStatus(1));
        assertEquals(ResultState.IN_PROGRESS, secondDependency.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectC.getBuildStatus(1));
        secondDependency.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(secondDependency.getName(), 1);

        xmlRpcHelper.waitForBuildInProgress(projectC.getName(), 1);
        assertEquals(ResultState.SUCCESS, projectA.getBuildStatus(1));
        assertEquals(ResultState.SUCCESS, projectB.getBuildStatus(1));
        assertEquals(ResultState.IN_PROGRESS, projectC.getBuildStatus(1));
        projectC.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectC.getName(), 1);
    }

    public void testRebuildTransitiveDependency() throws Exception
    {
        String projectName = randomName();
        WaitAntProjectHelper projectA = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "A");
        projectA.createProject();

        WaitAntProjectHelper projectB = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "B");
        projectB.addDependency(projectA);
        projectB.createProject();

        WaitAntProjectHelper projectC = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "C");
        projectC.addDependency(projectB);
        projectC.createProject();

        projectC.triggerRebuild();

        xmlRpcHelper.waitForBuildInProgress(projectA.getName(), 1);

        assertEquals(ResultState.IN_PROGRESS, projectA.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectB.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectC.getBuildStatus(1));

        projectA.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectA.getName(), 1);
        xmlRpcHelper.waitForBuildInProgress(projectB.getName(), 1);

        assertEquals(ResultState.SUCCESS, projectA.getBuildStatus(1));
        assertEquals(ResultState.IN_PROGRESS, projectB.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectC.getBuildStatus(1));

        projectB.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);
        xmlRpcHelper.waitForBuildInProgress(projectC.getName(), 1);

        assertEquals(ResultState.SUCCESS, projectA.getBuildStatus(1));
        assertEquals(ResultState.SUCCESS, projectB.getBuildStatus(1));
        assertEquals(ResultState.IN_PROGRESS, projectC.getBuildStatus(1));

        projectC.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectC.getName(), 1);
    }

    public void testRebuildUsesTransitiveProperty() throws Exception
    {
        String projectName = randomName();
        WaitAntProjectHelper projectA = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "A");
        projectA.createProject();

        WaitAntProjectHelper projectB = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "B");
        projectB.addDependency(projectA).setTransitive(false);
        projectB.createProject();

        WaitAntProjectHelper projectC = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "C");
        projectC.addDependency(projectB);
        projectC.createProject();

        projectC.triggerRebuild();

        xmlRpcHelper.waitForBuildInProgress(projectB.getName(), 1);

        assertEquals(IDLE, projectA.getState());
        assertEquals(ResultState.IN_PROGRESS, projectB.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectC.getBuildStatus(1));

        projectB.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);
        xmlRpcHelper.waitForBuildInProgress(projectC.getName(), 1);

        assertEquals(ResultState.SUCCESS, projectB.getBuildStatus(1));
        assertEquals(ResultState.IN_PROGRESS, projectC.getBuildStatus(1));

        projectC.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectC.getName(), 1);
    }

    public void testRebuildUsesStatusProperty() throws Exception
    {
        String projectName = randomName();
        WaitAntProjectHelper projectA = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "A");
        projectA.createProject();

        WaitAntProjectHelper projectB = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "B");
        projectB.addDependency(projectA).setRevision(REVISION_LATEST_RELEASE);
        projectB.createProject();

        WaitAntProjectHelper projectC = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "C");
        projectC.addDependency(projectB).setRevision(REVISION_LATEST_MILESTONE);
        projectC.createProject();

        WaitAntProjectHelper projectD = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "D");
        projectD.addDependency(projectC).setRevision(REVISION_LATEST_INTEGRATION);
        projectD.createProject();

        projectD.triggerRebuild(asPair("status", (Object) STATUS_MILESTONE));

        xmlRpcHelper.waitForBuildInProgress(projectB.getName(), 1);

        assertEquals(IDLE, projectA.getState());
        assertEquals(ResultState.IN_PROGRESS, projectB.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectC.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectD.getBuildStatus(1));

        projectB.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);
        xmlRpcHelper.waitForBuildInProgress(projectC.getName(), 1);

        assertEquals(ResultState.SUCCESS, projectB.getBuildStatus(1));
        assertEquals(ResultState.IN_PROGRESS, projectC.getBuildStatus(1));
        assertEquals(ResultState.PENDING_DEPENDENCY, projectD.getBuildStatus(1));

        projectC.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectC.getName(), 1);
        xmlRpcHelper.waitForBuildInProgress(projectD.getName(), 1);

        assertEquals(ResultState.SUCCESS, projectB.getBuildStatus(1));
        assertEquals(ResultState.SUCCESS, projectC.getBuildStatus(1));
        assertEquals(ResultState.IN_PROGRESS, projectD.getBuildStatus(1));

        projectD.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectD.getName(), 1);
    }

    public void testRebuildStopsOnFailure() throws Exception
    {
        String projectName = randomName();
        FailAntProjectHelper projectA = new FailAntProjectHelper(xmlRpcHelper, projectName + "A");
        projectA.createProject();

        WaitAntProjectHelper projectB = new WaitAntProjectHelper(xmlRpcHelper, tmpDir, projectName + "B");
        projectB.addDependency(projectA);
        projectB.createProject();

        projectB.triggerRebuild();

        xmlRpcHelper.waitForBuildToComplete(projectA.getName(), 1);

        assertEquals(ResultState.FAILURE, projectA.getBuildStatus(1));
        assertEquals(ResultState.ERROR, projectB.getBuildStatus(1));

        // We would normally have to release projectBs' build.  However, it did not run,
        // because projectA failed. 
    }
}
