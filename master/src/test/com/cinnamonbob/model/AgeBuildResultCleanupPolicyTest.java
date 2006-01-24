package com.cinnamonbob.model;

import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.test.BobTestCase;

/**
 */
public class AgeBuildResultCleanupPolicyTest extends BobTestCase
{
    private AgeBuildResultCleanupPolicy policy;

    protected void setUp() throws Exception
    {
        super.setUp();
        policy = new AgeBuildResultCleanupPolicy(5, 10);
    }

    public void testWorkDirNoCleanup()
    {
        BuildResult result = createBuild(System.currentTimeMillis());
        assertFalse(policy.canCleanupWorkDir(result));
    }

    public void testWorkDirCleanup()
    {
        BuildResult result = createBuild(0);
        assertTrue(policy.canCleanupWorkDir(result));
    }

    public void testWorkDirJustCleanup()
    {
        BuildResult result = createBuild(System.currentTimeMillis() - Constants.DAY * 5);
        assertTrue(policy.canCleanupWorkDir(result));
    }

    public void testResultNoCleanup()
    {
        BuildResult result = createBuild(System.currentTimeMillis());
        assertFalse(policy.canCleanupResult(result));
    }

    public void testResultCleanup()
    {
        BuildResult result = createBuild(0);
        assertTrue(policy.canCleanupResult(result));
    }

    public void testCleanupWorkNotResult()
    {
        BuildResult result = createBuild(System.currentTimeMillis() - Constants.DAY * 7);
        assertTrue(policy.canCleanupWorkDir(result));
        assertFalse(policy.canCleanupResult(result));
    }

    public void testNeverCleanWorkDir()
    {
        policy = new AgeBuildResultCleanupPolicy(AgeBuildResultCleanupPolicy.NEVER_CLEAN, AgeBuildResultCleanupPolicy.NEVER_CLEAN);
        BuildResult result = createBuild(0);
        assertFalse(policy.canCleanupWorkDir(result));
    }

    public void testNeverCleanResult()
    {
        policy = new AgeBuildResultCleanupPolicy(AgeBuildResultCleanupPolicy.NEVER_CLEAN, AgeBuildResultCleanupPolicy.NEVER_CLEAN);
        BuildResult result = createBuild(0);
        assertFalse(policy.canCleanupResult(result));
    }

    private BuildResult createBuild(long startTime)
    {
        BuildResult result = new BuildResult();
        result.commence(startTime);
        result.complete();
        return result;
    }
}
