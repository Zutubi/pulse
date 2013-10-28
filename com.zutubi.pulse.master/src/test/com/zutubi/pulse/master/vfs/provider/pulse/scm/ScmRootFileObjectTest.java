package com.zutubi.pulse.master.vfs.provider.pulse.scm;

import com.google.common.base.Function;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.vfs.provider.pulse.*;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.bean.WiringObjectFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.cache.NullFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.mockito.Matchers;

import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the ScmRootFileObject AND the ScmFileObject classes.  It does not make
 * sense to test the ScmFileObject separately as it does not make sense to have a
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

    public void testRootDisplayName() throws ScmException, FileSystemException
    {
        String location = RandomUtils.insecureRandomString(10);
        stub(scmClient.getLocation(Matchers.<ScmContext>anyObject())).toReturn(location);

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

    public void testChildGetDisplayName() throws ScmException, FileSystemException
    {
        FileObject c = createAndRetrieveFileObject("a.txt");
        assertEquals("a.txt", ((ScmFileObject)c).getDisplayName());
    }

    public void testDoListChildren() throws Exception
    {
        registerDirectoryListing("", "a", "b", "c.txt", "d.txt");

        ScmRootFileObject fo = resolveRoot();
        String[] listing = fo.doListChildren();
        assertEquals(4, listing.length);
        assertEquals("a.dir", listing[0]);
        assertEquals("b.dir", listing[1]);
        assertEquals("c.txt.file", listing[2]);
        assertEquals("d.txt.file", listing[3]);
    }

    public void testNumberOfCallsToScmWhenNavigating() throws ScmException, FileSystemException
    {
        registerDirectoryListing("", "a", "b");
        registerDirectoryListing("a", "a/1.txt", "a/2.txt");
        registerDirectoryListing("b", "b/c", "b/3.txt", "b/4.txt");
        registerDirectoryListing("b/c", "b/c/5.txt", "b/c/6.txt");

        ScmRootFileObject srfo = resolveRoot();
        FileObject[] rootChildren = srfo.getChildren();

        verify(scmClient, times(1)).browse((ScmContext)anyObject(), anyString(), (Revision)anyObject());

        rootChildren[0].getChildren();
        verify(scmClient, times(2)).browse((ScmContext)anyObject(), anyString(), (Revision)anyObject());

        rootChildren[1].getChildren();
        verify(scmClient, times(3)).browse((ScmContext)anyObject(), anyString(), (Revision)anyObject());
    }

    public void testSampleNagivation() throws FileSystemException, ScmException
    {
        registerDirectoryListing("", "a");
        registerDirectoryListing("a", "a/b");
        registerDirectoryListing("a/b", "a/b/c.txt");

        ScmRootFileObject srfo = resolveRoot();

        FileObject[] cfos = srfo.getChildren();
        assertEquals(1, cfos.length);
        FileObject cfo = cfos[0];
        assertEquals("a", cfo.getName().getBaseName());

        cfos = cfo.getChildren();
        cfo = cfos[0];
        assertEquals("b", cfo.getName().getBaseName());

        cfos = cfo.getChildren();
        cfo = cfos[0];
        assertEquals("c.txt", cfo.getName().getBaseName());
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
        return newArrayList(transform(asList(names), new Function<String, ScmFile>()
        {
            public ScmFile apply(String s)
            {
                return new ScmFile(s, !s.endsWith(".txt"));
            }
        }));
    }

    private void registerDirectoryListing(String dir, String... listing) throws ScmException
    {
        stub(scmClient.browse((ScmContext)anyObject(), eq(dir), (Revision)anyObject())).toReturn(list(listing));
    }

    private ScmRootFileObject resolveRoot() throws FileSystemException
    {
        return (ScmRootFileObject) fsm.resolveFile("pulse:///scm");
    }

    /**
     * Root pulse file system file object that has the ScmRootFileObject as its only child to
     * simplify testing
     */
    public static class TestRootFileObject extends AbstractPulseFileObject implements ProjectConfigProvider, ProjectProvider
    {
        public TestRootFileObject(final FileName name, final AbstractFileSystem fs)
        {
            super(name, fs);
        }

        public AbstractPulseFileObject createFile(final FileName fileName)
        {
            return objectFactory.buildBean(ScmRootFileObject.class, fileName, pfs);
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
