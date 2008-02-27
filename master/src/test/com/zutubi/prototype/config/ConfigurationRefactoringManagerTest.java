package com.zutubi.prototype.config;

import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TemplatedMapType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.validation.ValidationException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class ConfigurationRefactoringManagerTest extends AbstractConfigurationSystemTestCase
{
    private ConfigurationRefactoringManager configurationRefactoringManager;
    private CompositeType typeA;
    private String rootPath;
    private static final String SAMPLE_SCOPE = "sample";

    protected void setUp() throws Exception
    {
        super.setUp();
        configurationRefactoringManager = new ConfigurationRefactoringManager();
        configurationRefactoringManager.setTypeRegistry(typeRegistry);
        configurationRefactoringManager.setConfigurationTemplateManager(configurationTemplateManager);
        configurationRefactoringManager.setConfigurationReferenceManager(configurationReferenceManager);

        typeA = typeRegistry.register(MockA.class);
        MapType mapA = new MapType(typeA, typeRegistry);

        MapType templatedMap = new TemplatedMapType(typeA, typeRegistry);

        configurationPersistenceManager.register(SAMPLE_SCOPE, mapA);
        configurationPersistenceManager.register("template", templatedMap);

        MutableRecord root = typeA.unstantiate(new MockA("root"));
        configurationTemplateManager.markAsTemplate(root);
        rootPath = configurationTemplateManager.insertRecord("template", root);
    }

    public void testCloneEmptyPath() throws ValidationException
    {
        illegalPathHelper("", "Invalid path '': no parent");
    }

    public void testCloneSingleElementPath() throws ValidationException
    {
        illegalPathHelper(SAMPLE_SCOPE, "Invalid path 'sample': no parent");
    }

    public void testCloneInvalidParentPath() throws ValidationException
    {
        illegalPathHelper("huh/instance", "Invalid path 'huh': references non-existant root scope 'huh'");
    }

    public void testCloneInvalidPath() throws ValidationException
    {
        illegalPathHelper("sample/nosuchinstance", "Invalid path 'sample/nosuchinstance': path does not exist");
    }

    public void testCloneParentPathNotACollection() throws ValidationException
    {
        configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance());
        illegalPathHelper("sample/a/b", "Invalid parent path 'sample/a': only elements of a map collection may be cloned (parent has type com.zutubi.prototype.type.CompositeType)");
    }

    public void testCloneParentPathAList() throws ValidationException
    {
        String aPath = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance());
        String listPath = PathUtils.getPath(aPath, "blist");
        String clonePath = PathUtils.getPath(listPath, configurationTemplateManager.getRecord(listPath).keySet().iterator().next());
        illegalPathHelper(clonePath, "Invalid parent path '" + listPath + "': only elements of a map collection may be cloned (parent has type com.zutubi.prototype.type.ListType)");
    }

    private void illegalPathHelper(String path, String expectedError) throws ValidationException
    {
        try
        {
            configurationRefactoringManager.clone(path, "clone");
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals(expectedError, e.getMessage());            
        }
        catch (ConfigRuntimeException e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(expectedError, cause.getMessage());
        }
    }

    public void testCloneEmptyCloneName() throws ValidationException, TypeException
    {
        invalidCloneNameHelper(insertTemplateA(rootPath, "a", false), "", "Invalid empty clone key");
    }

    public void testCloneDuplicateCloneName() throws ValidationException, TypeException
    {
        insertTemplateA(rootPath, "existing", false);
        invalidCloneNameHelper(insertTemplateA(rootPath, "a", false), "existing", "name is already in use, please select another name");
    }

    public void testCloneCloneNameInAncestor() throws ValidationException, TypeException
    {
        addB(rootPath, "parentB");
        String childPath = insertTemplateA(rootPath, "child", false);
        configurationTemplateManager.delete(PathUtils.getPath(childPath, "bmap", "parentB"));
        String childBPath = addB(childPath, "childB");
        invalidCloneNameHelper(childBPath, "parentB", "name is already in use in ancestor \"root\", please select another name");
    }

    public void testCloneCloneNameInDescendent() throws ValidationException, TypeException
    {
        String parentBPath = addB(rootPath, "parentB");
        String childPath = insertTemplateA(rootPath, "child", false);
        addB(childPath, "childB");
        invalidCloneNameHelper(parentBPath, "childB", "name is already in use in descendent \"child\", please select another name");
    }

    public void testSimpleClone() throws ValidationException
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance());
        String clonePath = configurationRefactoringManager.clone(path, "clone");
        assertEquals("sample/clone", clonePath);
        assertClone(configurationTemplateManager.getInstance(clonePath, MockA.class));
    }

    public void testSimpleCloneInTemplateScope() throws ValidationException, TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance(), false);
        String clonePath = configurationRefactoringManager.clone(path, "clone");
        assertEquals("template/clone", clonePath);
        MockA clone = configurationTemplateManager.getInstance(clonePath, MockA.class);
        assertTrue(clone.isConcrete());
        assertEquals(configurationTemplateManager.getRecord(rootPath).getHandle(), configurationTemplateManager.getTemplateParentRecord(clonePath).getHandle());
        assertClone(clone);
    }

    public void testCloneOfTemplate() throws ValidationException, TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance(), true);
        String clonePath = configurationRefactoringManager.clone(path, "clone");
        assertEquals("template/clone", clonePath);
        MockA clone = configurationTemplateManager.getInstance(clonePath, MockA.class);
        assertFalse(clone.isConcrete());
        assertEquals(configurationTemplateManager.getRecord(rootPath).getHandle(), configurationTemplateManager.getTemplateParentRecord(clonePath).getHandle());
        assertClone(clone);
    }

    public void testCloneBelowTopLevel() throws ValidationException
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance());
        MockB colby = configurationTemplateManager.getInstance(path, MockA.class).getBmap().values().iterator().next();
        String clonePath = configurationRefactoringManager.clone(colby.getConfigurationPath(), "clone");
        assertEquals("sample/a/bmap/clone", clonePath);
        MockB clone = configurationTemplateManager.getInstance(clonePath, MockB.class);
        
        assertNotSame(colby, clone);
        assertEquals("clone", clone.getName());
        assertEquals(1, clone.getY());
    }

    public void testCloneWithInternalReference() throws ValidationException
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance());
        MockA instance = configurationTemplateManager.getInstance(path, MockA.class);
        instance.setRefToRef(instance.getRef());
        configurationTemplateManager.save(instance);

        String clonePath = configurationRefactoringManager.clone(path, "clone");
        instance = configurationTemplateManager.getInstance(path, MockA.class);
        assertNotNull(instance.getRef());
        assertSame(instance.getRef(), instance.getRefToRef());

        MockA cloneInstance = configurationTemplateManager.getInstance(clonePath, MockA.class);
        assertNotSame(instance.getRef(), cloneInstance.getRef());
        assertNotSame(instance.getRefToRef(), cloneInstance.getRefToRef());
        assertSame(cloneInstance.getRef(), cloneInstance.getRefToRef());
    }

    private MockA createAInstance()
    {
        MockA instance = new MockA("a");
        instance.setX(10);
        MockB b = new MockB("b");
        b.setY(44);
        instance.setB(b);
        instance.getBlist().add(new MockB("lisby"));
        MockB colby = new MockB("colby");
        colby.setY(1);
        instance.getBmap().put("colby", colby);
        instance.setRef(new Referee("ee"));
        return instance;
    }

    private void assertClone(MockA clone)
    {
        assertEquals("clone", clone.getName());
        assertEquals(10, clone.getX());
        MockB cloneB = clone.getB();
        assertEquals("b", cloneB.getName());
        assertEquals(44, cloneB.getY());
        assertEquals(1, clone.getBlist().size());
        assertEquals("lisby", clone.getBlist().get(0).getName());
        assertEquals(1, clone.getBmap().size());
        MockB cloneColby = clone.getBmap().get("colby");
        assertEquals("colby", cloneColby.getName());
        assertEquals(1, cloneColby.getY());
        Referee cloneRef = clone.getRef();
        assertNotNull(cloneRef);
        assertEquals("ee", cloneRef.getName());
    }

    private void invalidCloneNameHelper(String path, String cloneName, String expectedError) throws TypeException
    {
        try
        {
            configurationRefactoringManager.clone(path, cloneName);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals(expectedError, e.getMessage());            
        }
        catch(ConfigRuntimeException e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof ValidationException);
            assertEquals(expectedError, cause.getMessage());
        }
    }

    private String insertTemplateA(String templateParentPath, String name, boolean template) throws TypeException
    {
        return insertTemplateAInstance(templateParentPath, new MockA(name), template);
    }

    private String insertTemplateAInstance(String templateParentPath, MockA instance, boolean template) throws TypeException
    {
        MutableRecord record = typeA.unstantiate(instance);
        if(template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }
        configurationTemplateManager.setParentTemplate(record, configurationTemplateManager.getRecord(templateParentPath).getHandle());
        return configurationTemplateManager.insertRecord("template", record);
    }

    private String addB(String aPath, String name)
    {
        return configurationTemplateManager.insert(PathUtils.getPath(aPath, "bmap"), new MockB(name));
    }

    @SymbolicName("a")
    public static class MockA extends AbstractNamedConfiguration
    {
        private int x;
        private MockB b;
        private List<MockB> blist = new LinkedList<MockB>();
        private Map<String, MockB> bmap = new HashMap<String, MockB>();
        private Referee ref;
        @Reference
        private Referee refToRef;

        public MockA()
        {
        }

        public MockA(String name)
        {
            super(name);
        }

        public int getX()
        {
            return x;
        }

        public void setX(int x)
        {
            this.x = x;
        }

        public MockB getB()
        {
            return b;
        }

        public void setB(MockB b)
        {
            this.b = b;
        }

        public List<MockB> getBlist()
        {
            return blist;
        }

        public void setBlist(List<MockB> blist)
        {
            this.blist = blist;
        }

        public Map<String, MockB> getBmap()
        {
            return bmap;
        }

        public void setBmap(Map<String, MockB> bmap)
        {
            this.bmap = bmap;
        }

        public Referee getRef()
        {
            return ref;
        }

        public void setRef(Referee ref)
        {
            this.ref = ref;
        }

        public Referee getRefToRef()
        {
            return refToRef;
        }

        public void setRefToRef(Referee refToRef)
        {
            this.refToRef = refToRef;
        }
    }

    @SymbolicName("b")
    public static class MockB extends AbstractNamedConfiguration
    {
        private int y;

        public MockB()
        {
        }

        public MockB(String name)
        {
            super(name);
        }

        public int getY()
        {
            return y;
        }

        public void setY(int y)
        {
            this.y = y;
        }
    }

    @SymbolicName("referee")
    public static class Referee extends AbstractNamedConfiguration
    {
        public Referee()
        {
        }

        public Referee(String name)
        {
            super(name);
        }
    }
}
