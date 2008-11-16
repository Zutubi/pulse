package com.zutubi.pulse.master.vfs.provider.pulse.scm;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.vfs.provider.pulse.*;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.bean.WiringObjectFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.cache.NullFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.eq;

import java.util.List;

/**
 * Tests for the ScmRootFileObject AND the ScmFileObject classes.  It does not make
 * sence to test the ScmFileObject separately as it does not make sense to have a
 * ScmFileObject without also having an ScmRootFileObject.
 */
public class ScmRootFileObjectTest extends PulseTestCase
{
    private WiringObjectFactory objectFactory;

    private ScmManager scmManager;
    
    private ScmClient scmClient;

    private DefaultFileSystemManager fsm;

    protected void setUp() throws Exception
    {
        super.setUp();

        scmManager = mock(ScmManager.class);
        scmClient = mock(ScmClient.class);

        stub(scmManager.createClient((ScmConfiguration) anyObject())).toReturn(scmClient);

        objectFactory = new WiringObjectFactory();
        objectFactory.initProperties(this);

        fsm = new DefaultFileSystemManager();
        fsm.setFilesCache(new NullFilesCache()); // disable caching.

        PulseFileProvider pulseFileProvider = new PulseFileProvider();
        pulseFileProvider.setObjectFactory(objectFactory);
        pulseFileProvider.setRootFileType(TestRootFileObject.class);
        fsm.addProvider("pulse", pulseFileProvider);
        fsm.init();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testRootDisplayName() throws ScmException, FileSystemException
    {
        String location = RandomUtils.randomString(10);
        stub(scmClient.getLocation()).toReturn(location);

        ScmRootFileObject srfo = resolveRoot();
        assertTrue(srfo.exists()); // needed to ensure the srfo is attached..
        
        assertEquals(location, srfo.getDisplayName());
    }

    public void testRootListChildrenNone() throws FileSystemException, ScmException
    {
        List<ScmFile> result = list();
        stub(scmClient.browse((ScmContext)anyObject(), anyString(), (Revision)anyObject())).toReturn(result);

        ScmRootFileObject srfo = resolveRoot();

        assertEquals(0, srfo.getChildren().length);
    }

    public void testRootListChildrenSome() throws FileSystemException, ScmException
    {
        List<ScmFile> result = list("a.txt", "b.txt");
        stub(scmClient.browse((ScmContext)anyObject(), anyString(), (Revision)anyObject())).toReturn(result);

        ScmRootFileObject srfo = resolveRoot();

        FileObject[] cfos = srfo.getChildren();
        assertEquals(2, cfos.length);
    }

    public void testRootGetFileType() throws FileSystemException
    {
        ScmRootFileObject srfo = resolveRoot();

        assertEquals(FileTypeConstants.FOLDER, srfo.getFileType());
    }

    public void testChildFolderGetFileType() throws ScmException, FileSystemException
    {
        FileObject c = createAndRetrieveFileObject("a");
        assertEquals(FileTypeConstants.FOLDER, ((ScmFileObject)c).getFileType());
    }

    public void testChildFileGetFileType() throws FileSystemException, ScmException
    {
        FileObject c = createAndRetrieveFileObject("a.txt");
        assertEquals(FileTypeConstants.FILE, ((ScmFileObject)c).getFileType());
    }

    public void testChildGetDisplayName() throws ScmException, FileSystemException
    {
        FileObject c = createAndRetrieveFileObject("a.txt");
        assertEquals("a.txt", ((ScmFileObject)c).getDisplayName());
    }

    public void testDoListChildren()
    {

    }

    public void testNumberOfCallsToScmWhenNavigating()
    {

    }

    public void testSampleNagivation() throws FileSystemException, ScmException
    {
        stub(scmClient.browse((ScmContext)anyObject(), eq(""), (Revision)anyObject())).toReturn(list("a"));
        stub(scmClient.browse((ScmContext)anyObject(), eq("a"), (Revision)anyObject())).toReturn(list("a/b"));
        stub(scmClient.browse((ScmContext)anyObject(), eq("a/b"), (Revision)anyObject())).toReturn(list("a/b/c.txt"));

        ScmRootFileObject srfo = resolveRoot();

        FileObject[] cfos = srfo.getChildren();
        assertEquals(1, cfos.length);
        FileObject cfo = cfos[0];
        assertEquals("a", cfo.getName().getBaseName());
        assertTrue(cfo instanceof ScmFileObject);
        assertEquals(FileTypeConstants.FOLDER,((ScmFileObject)cfo).getFileType());

        cfos = cfo.getChildren();
        cfo = cfos[0];
        assertEquals("b", cfo.getName().getBaseName());
        assertTrue(cfo instanceof ScmFileObject);
        assertEquals(FileTypeConstants.FOLDER,((ScmFileObject)cfo).getFileType());

        cfos = cfo.getChildren();
        cfo = cfos[0];
        assertEquals("c.txt", cfo.getName().getBaseName());
        assertTrue(cfo instanceof ScmFileObject);
        assertEquals(FileTypeConstants.FILE,((ScmFileObject)cfo).getFileType());
    }

    public void testScmFileAvailableActions() throws ScmException, FileSystemException
    {
        FileObject c = createAndRetrieveFileObject("a.txt");
        List<FileAction> actions = ((ScmFileObject) c).getActions();
        assertEquals(1, actions.size());
        assertEquals(FileAction.TYPE_DOWNLOAD, actions.get(0).getType());
    }

    public void testScmFolderAvailableActions() throws ScmException, FileSystemException
    {
        FileObject c = createAndRetrieveFileObject("a");
        assertEquals(0, ((ScmFileObject)c).getActions().size());
    }

    private FileObject createAndRetrieveFileObject(String name) throws ScmException, FileSystemException
    {
        stub(scmClient.browse((ScmContext)anyObject(), anyString(), (Revision)anyObject())).toReturn(list(name));

        ScmRootFileObject srfo = resolveRoot();

        FileObject fo = srfo.getChildren()[0];
        assertTrue(fo.exists()); // ensures attached.
        
        return fo;
    }

    private List<ScmFile> list(String... names)
    {
        return CollectionUtils.map(names, new Mapping<String, ScmFile>()
        {
            public ScmFile map(String s)
            {
                return new ScmFile(s, !s.endsWith(".txt"));
            }
        });
    }

    private ScmRootFileObject resolveRoot() throws FileSystemException
    {
        return (ScmRootFileObject) fsm.resolveFile("pulse:///scm");
    }

    public static class TestRootFileObject extends AbstractPulseFileObject implements ProjectConfigProvider, ProjectProvider
    {
        public TestRootFileObject(final FileName name, final AbstractFileSystem fs)
        {
            super(name, fs);
        }

        public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
        {
            return objectFactory.buildBean(ScmRootFileObject.class,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs});
        }

        protected FileType doGetType() throws Exception
        {
            return FileType.FOLDER;
        }

        protected String[] doListChildren() throws Exception
        {
            return new String[]{"scm"};
        }

        public ProjectConfiguration getProjectConfig() throws FileSystemException
        {
            return getProject().getConfig();
        }

        public Project getProject()
        {
            Project p = new Project();
            p.setId(1);
            p.setConfig(new ProjectConfiguration());
            return p;
        }

        public long getProjectId()
        {
            return getProject().getId();
        }
    }

}
