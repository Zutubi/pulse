package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

public class PerforceWorkspaceManagerTest extends PulseTestCase
{
    private static final File TEST_DIR = new File("some/dir");
    private static final String TEST_ROOT = FileSystemUtils.getNormalisedAbsolutePath(TEST_DIR);

    private static final String TEST_AGENT = "master";
    private static final String TEST_PROJECT = "my project";
    private static final String TEST_STAGE = "my stage";

    private static final String TEST_PORT = ":1234";
    private static final String TEST_USER = "user";
    private static final String TEST_PASSWORD = "pass";
    private static final String TEST_TEMPLATE_WORKSPACE = "template";
    private static final PerforceConfiguration TEST_PERFORCE_CONFIGURATION = new PerforceConfiguration(TEST_PORT, TEST_USER, TEST_PASSWORD, TEST_TEMPLATE_WORKSPACE);

    private PerforceWorkspaceManager workspaceManager = new PerforceWorkspaceManager();
    private PerforceCore core;

    @Override
    protected void setUp() throws Exception
    {
        core = mock(PerforceCore.class);
        stub(core.createOrUpdateWorkspace(anyString(), anyString(), anyString(), anyString(), (String) isNull(), (String) isNull(), (String) isNull())).toAnswer(new Answer<PerforceWorkspace>()
        {
            public PerforceWorkspace answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                PerforceWorkspace workspace = new PerforceWorkspace(
                        (String) invocationOnMock.getArguments()[1],
                        (String) invocationOnMock.getArguments()[3],
                        new LinkedList<String>()
                );
                workspace.setDescription(Arrays.asList((String) invocationOnMock.getArguments()[2]));
                return workspace;
            }
        });
    }

    public void testGetSyncWorkspaceNameConsistent()
    {
        assertEquals(PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, createExecutionContext(1, 1, 1)), PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, createExecutionContext(1, 1, 1)));
    }

    public void testGetSyncWorkspaceNameDependsOnProject()
    {
        assertFalse(PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, createExecutionContext(1, 0, 0)).equals(PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, createExecutionContext(2, 0, 0))));
    }

    public void testGetSyncWorkspaceNameDependsOnStage()
    {
        assertFalse(PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, createExecutionContext(0, 1, 0)).equals(PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, createExecutionContext(0, 2, 0))));
    }

    public void testGetSyncWorkspaceNameDependsOnAgent()
    {
        assertFalse(PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, createExecutionContext(0, 0, 1)).equals(PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, createExecutionContext(0, 0, 2))));
    }

    public void testGetSyncWorkspaceDescriptionIncludesAgent()
    {
        assertTrue(PerforceWorkspaceManager.getSyncWorkspaceDescription(createExecutionContext(0, 0, 1)).contains(TEST_AGENT));
    }

    public void testGetSyncWorkspaceDescriptionIncludesProject()
    {
        assertTrue(PerforceWorkspaceManager.getSyncWorkspaceDescription(createExecutionContext(0, 0, 1)).contains(TEST_PROJECT));
    }

    public void testGetSyncWorkspaceDescriptionIncludesStage()
    {
        assertTrue(PerforceWorkspaceManager.getSyncWorkspaceDescription(createExecutionContext(0, 0, 1)).contains(TEST_STAGE));
    }

    public void testGetTemporaryWorkspaceDescription()
    {
        assertTrue(PerforceWorkspaceManager.getTemporaryWorkspaceDescription().startsWith("Temporary"));
    }

    public void testGetPersistentWorkspaceDescriptionIncludesProject()
    {
        assertTrue(PerforceWorkspaceManager.getPersistentWorkspaceDescription(createScmContext(1)).contains(TEST_PROJECT));
    }
    
    public void testGetSyncWorkspaceBasicProperties() throws ScmException
    {
        ExecutionContext context = createExecutionContext(1, 0, 1);
        String workspaceName = PerforceWorkspaceManager.getSyncWorkspaceName(TEST_PERFORCE_CONFIGURATION, context);

        PerforceWorkspace workspace = workspaceManager.getSyncWorkspace(core, TEST_PERFORCE_CONFIGURATION, context);
        assertEquals(workspaceName, workspace.getName());
        assertEquals(TEST_ROOT, workspace.getRoot());
        assertFalse(workspace.isTemporary());
    }

    public void testGetSyncWorkspaceCreatesExpectedClient() throws ScmException
    {
        ExecutionContext context = createExecutionContext(1, 0, 1);
        PerforceConfiguration config = new PerforceConfiguration(TEST_PORT, TEST_USER, TEST_PASSWORD, TEST_TEMPLATE_WORKSPACE);
        final PerforceWorkspace workspace = workspaceManager.getSyncWorkspace(core, config, context);
        verify(core).createOrUpdateWorkspace(TEST_TEMPLATE_WORKSPACE, workspace.getName(), PerforceWorkspaceManager.getSyncWorkspaceDescription(context), TEST_ROOT, null, null, null);
        verifyNoMoreInteractions(core);
    }

    public void testAllocateWorkspaceBasicProperties() throws ScmException
    {
        ScmContextImpl context = createScmContext(11223344);
        
        PerforceWorkspace workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, context);
        assertTrue(workspace.getName().contains(Long.toString(context.getProjectHandle())));
        assertEquals(TEST_ROOT, workspace.getRoot());
        assertFalse(workspace.isTemporary());
    }

    public void testAllocateWorkspaceNullContextCreatesTempClient() throws ScmException
    {
        PerforceWorkspace workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, null);
        assertTrue(workspace.isTemporary());
    }

    public void testAllocateWorkspaceCreatesExpectedClient() throws ScmException
    {
        ScmContextImpl scmContext = createScmContext(88);
        PerforceWorkspace workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, scmContext);
        verify(core).createOrUpdateWorkspace(TEST_TEMPLATE_WORKSPACE, workspace.getName(), PerforceWorkspaceManager.getPersistentWorkspaceDescription(scmContext), TEST_ROOT, null, null, null);
        verifyNoMoreInteractions(core);
    }

    public void testFreePersistentWorkspaceDoesNotDeleteClient() throws ScmException
    {
        ScmContextImpl scmContext = createScmContext(88);
        PerforceWorkspace workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, scmContext);
        verify(core).createOrUpdateWorkspace(TEST_TEMPLATE_WORKSPACE, workspace.getName(), PerforceWorkspaceManager.getPersistentWorkspaceDescription(scmContext), TEST_ROOT, null, null, null);
        workspaceManager.freeWorkspace(core, workspace);
        verifyNoMoreInteractions(core);
    }

    public void testFreeTempWorkspaceDeletesClient() throws ScmException
    {
        PerforceWorkspace workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, null);
        verify(core).createOrUpdateWorkspace(eq(TEST_TEMPLATE_WORKSPACE), eq(workspace.getName()), anyString(), anyString(), (String) isNull(), (String) isNull(), (String) isNull());
        workspaceManager.freeWorkspace(core, workspace);
        verify(core).deleteWorkspace(workspace.getName());
        verifyNoMoreInteractions(core);
    }

    public void testAllocatedWorkspaceNotReused() throws ScmException
    {
        ScmContextImpl context = createScmContext(99);
        PerforceWorkspace workspace1 = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, context);
        PerforceWorkspace workspace2 = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, context);

        assertFalse(workspace1.getName().equals(workspace2.getName()));
    }

    public void testFreedWorkspaceIsReused() throws ScmException
    {
        ScmContextImpl scmContext = createScmContext(23);
        PerforceWorkspace workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, scmContext);
        String allocatedName = workspace.getName();
        workspaceManager.freeWorkspace(core, workspace);
        workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, scmContext);
        assertEquals(allocatedName, workspace.getName());
    }

    public void testErrorOnWorkspaceUpdateDoesNotConsumeName() throws ScmException
    {
        // CIB-1765: Perforce workspace name leaks on error updating workspace
        final String ERROR_MESSAGE = "I failed";

        ScmContextImpl scmContext = createScmContext(23);

        // The most reliable way to figure out the next name is to allocate it
        // successfuly and free it for reuse.
        PerforceWorkspace workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, scmContext);
        String nextName = workspace.getName();
        workspaceManager.freeWorkspace(core, workspace);

        // Now do a broken allocation, which should not hold onto the name
        PerforceCore brokenCore = mock(PerforceCore.class);
        stub(brokenCore.createOrUpdateWorkspace(anyString(), anyString(), anyString(), anyString(), (String) isNull(), (String) isNull(), (String) isNull())).toThrow(new ScmException(ERROR_MESSAGE));
        try
        {
            workspaceManager.allocateWorkspace(brokenCore, TEST_PERFORCE_CONFIGURATION, scmContext);
            fail("Allocation should throw using broken mock");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString(ERROR_MESSAGE));
        }

        // Finally the successful allocation should reuse the original name
        workspace = workspaceManager.allocateWorkspace(core, TEST_PERFORCE_CONFIGURATION, scmContext);
        assertEquals(nextName, workspace.getName());
    }

    public void testCleanupPersistentWorkspacesNoneToFree() throws ScmException
    {
        stub(core.getAllWorkspaceNames()).toReturn(Collections.<String>emptyList());
        workspaceManager.cleanupPersistentWorkspaces(core, createScmContext(1), new RecordingScmFeedbackHandler());
        verify(core).getAllWorkspaceNames();
        verifyNoMoreInteractions(core);
    }

    public void testCleanupPersistentWorkspacesOneToFree() throws ScmException
    {
        final String WORKSPACE_NAME = "pulse-1-free-me";
        stub(core.getAllWorkspaceNames()).toReturn(Arrays.asList(WORKSPACE_NAME));
        workspaceManager.cleanupPersistentWorkspaces(core, createScmContext(1), new RecordingScmFeedbackHandler());
        verify(core).getAllWorkspaceNames();
        verify(core).deleteWorkspace(WORKSPACE_NAME);
        verifyNoMoreInteractions(core);
    }

    public void testCleanupPersistentWorkspacesSomeToFree() throws ScmException
    {
        final String MATCHING_WORKSPACE_NAME1 = "pulse-1-free-me";
        final String MATCHING_WORKSPACE_NAME2 = "pulse-1$";
        final String NON_MATCHING_WORKSPACE_NAME1 = "pulse-2-keep-me";
        final String NON_MATCHING_WORKSPACE_NAME2 = "pulse-and-keep-me";

        stub(core.getAllWorkspaceNames()).toReturn(Arrays.asList(MATCHING_WORKSPACE_NAME1, NON_MATCHING_WORKSPACE_NAME1, NON_MATCHING_WORKSPACE_NAME2, MATCHING_WORKSPACE_NAME2));
        workspaceManager.cleanupPersistentWorkspaces(core, createScmContext(1), new RecordingScmFeedbackHandler());
        verify(core).getAllWorkspaceNames();
        verify(core).deleteWorkspace(MATCHING_WORKSPACE_NAME1);
        verify(core).deleteWorkspace(MATCHING_WORKSPACE_NAME2);
        verifyNoMoreInteractions(core);
    }

    private ExecutionContext createExecutionContext(long projectHandle, long stageHandle, long agentHandle)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT, TEST_AGENT);
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT_HANDLE, agentHandle);
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_PROJECT, TEST_PROJECT);
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_PROJECT_HANDLE, projectHandle);
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_STAGE, TEST_STAGE);
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_STAGE_HANDLE, stageHandle);
        context.setWorkingDir(TEST_DIR);
        return context;
    }

    private ScmContextImpl createScmContext(int projectHandle)
    {
        ScmContextImpl scmContext = new ScmContextImpl();
        scmContext.setProjectName(TEST_PROJECT);
        scmContext.setProjectHandle(projectHandle);
        scmContext.setPersistentWorkingDir(TEST_DIR);
        return scmContext;
    }
}
