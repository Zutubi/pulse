package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.ScmFile;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.bean.WiringObjectFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.mockito.Matchers;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedList;

public class PulseFileSystemTest extends PulseTestCase
{
    private DefaultFileSystemManager fileSystemManager;
    private WiringObjectFactory objectFactory;
    private ProjectManager projectManager;
    private ScmManager scmManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        projectManager = mock(ProjectManager.class);
        scmManager = mock(ScmManager.class);

        objectFactory = new WiringObjectFactory();
        objectFactory.initProperties(this);

        PulseFileProvider fileProvider = new PulseFileProvider();
        fileProvider.setObjectFactory(objectFactory);

        fileSystemManager = new DefaultFileSystemManager();
        fileSystemManager.addProvider("pulse", fileProvider);
        fileSystemManager.init();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testListingProjectsWithNoChildren() throws FileSystemException
    {
        FileObject obj = fileSystemManager.resolveFile("pulse:///projects");
        assertTrue(obj.exists());
        assertEquals(0, obj.getChildren().length);
    }

    public void testListingProectsWithChildren() throws FileSystemException
    {
        Project project = new Project();
        project.setId(1);
        stub(projectManager.getProjects(false)).toReturn(Arrays.asList(project));

        FileObject obj = fileSystemManager.resolveFile("pulse:///projects");
        assertTrue(obj.exists());
        // NOTE: the projects object currently does not support listing for performance reasons.
        assertEquals(0, obj.getChildren().length);
    }

    public void testResolveNonExistantProject() throws FileSystemException
    {
        FileObject obj = fileSystemManager.resolveFile("pulse:///projects/666/");
        assertFalse(obj.exists());
        assertTrue(obj instanceof ProjectFileObject);
    }

    public void testResolveProjectScm() throws FileSystemException, ScmException
    {
        ScmConfiguration scmConfig = mock(ScmConfiguration.class);
        stub(scmConfig.isValid()).toReturn(true);

        ScmClient client = mock(ScmClient.class);
        stub(scmManager.createClient(scmConfig)).toReturn(client);

        int id = 1;
        Project project = createProject(id);
        project.getConfig().setScm(scmConfig);

        registerProject(project);

        ScmRootFileObject obj = (ScmRootFileObject) fileSystemManager.resolveFile("pulse:///projects/"+id+"/scm");
        assertTrue(obj.exists());

        // not sure why we have this dude in the middle, will probably remove him.
        assertEquals(1, obj.getChildren().length);

        ScmFileObject scmFileObject = (ScmFileObject) obj.getChildren()[0];
        assertEquals("scm", scmFileObject.getName().getBaseName());

        stub(client.browse(null, "", null)).toReturn(Arrays.asList(new ScmFile("a"), new ScmFile("b", true)));
        stub(client.browse(null, "b", null)).toReturn(new LinkedList<ScmFile>());

        FileObject[] listing = scmFileObject.getChildren();
        assertEquals(2, listing.length);
        assertEquals("a", listing[0].getName().getBaseName());
        FileObject b = listing[1];
        assertEquals("b", b.getName().getBaseName());
        assertEquals(FileType.FOLDER, b.getType());

        verify(client, times(1)).browse(null, "", null);

        listing = b.getChildren();
        assertEquals(0, listing.length);

        verify(client, times(1)).browse(null, "b", null);
    }

    private Project createProject(long id)
    {
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setProjectId(id);
        Project project = new Project();
        project.setId(id);
        project.setConfig(projectConfig);

        return project;
    }

    private void registerProject(Project project)
    {
        stub(projectManager.getProject(eq(project.getId()), Matchers.anyBoolean())).toReturn(project);
        stub(projectManager.getProjectConfig(eq(project.getId()), Matchers.anyBoolean())).toReturn(project.getConfig());
    }
}
