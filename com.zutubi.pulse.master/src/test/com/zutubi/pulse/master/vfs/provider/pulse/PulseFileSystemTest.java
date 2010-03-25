package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.ScmFile;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.vfs.provider.pulse.scm.ScmRootFileObject;
import com.zutubi.pulse.master.xwork.actions.vfs.FileDepthFilterSelector;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.bean.WiringObjectFactory;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.mockito.Matchers;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

    public void testListingProjectsWithChildren() throws FileSystemException
    {
        Project project = createProject(1);
        registerProject(project);

        FileObject obj = fileSystemManager.resolveFile("pulse:///projects");
        assertTrue(obj.exists());
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

        ScmRootFileObject scm = (ScmRootFileObject) fileSystemManager.resolveFile("pulse:///projects/"+id+"/scm");
        assertTrue(scm.exists());

        stub(client.browse(null, "", null)).toReturn(Arrays.asList(new ScmFile("1.txt"), new ScmFile("b.dir", true)));
        stub(client.browse(null, "b.dir", null)).toReturn(asFiles("a.txt", "b.txt", "c.txt", "d.txt"));

        List<FileObject> selected = new LinkedList<FileObject>();
        scm.findFiles(new FileDepthFilterSelector(new AcceptFileFilter(), 1), true, selected);
        assertEquals(2, selected.size());

        FileObject b = selected.get(1);
        assertEquals("b.dir", b.getName().getBaseName());
        assertEquals(FileType.FOLDER, b.getType());
        selected.clear();
        b.findFiles(new FileDepthFilterSelector(new AcceptFileFilter(), 1), true, selected);
        assertEquals(4, selected.size());
        assertEquals("a.txt", selected.get(0).getName().getBaseName());

        assertEquals(b, selected.get(0).getParent());
    }

    private List<ScmFile> asFiles(String... filenames)
    {
        return CollectionUtils.map(Arrays.asList(filenames), new Mapping<String, ScmFile>()
        {
            public ScmFile map(String s)
            {
                return new ScmFile(s);
            }
        });
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

    private static class AcceptFileFilter implements FileFilter
    {
        public boolean accept(final FileSelectInfo fileInfo)
        {
            return true;
        }
    }
}
