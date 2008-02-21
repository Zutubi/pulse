package com.zutubi.prototype.config;

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
import java.util.Map;

/**
 */
public class ConfigurationRefactoringManagerTest extends AbstractConfigurationSystemTestCase
{
    private ConfigurationRefactoringManager configurationRefactoringManager;
    private CompositeType typeA;
    private String rootPath;

    protected void setUp() throws Exception
    {
        super.setUp();
        configurationRefactoringManager = new ConfigurationRefactoringManager();
        configurationRefactoringManager.setConfigurationTemplateManager(configurationTemplateManager);

        typeA = typeRegistry.register(MockA.class);
        typeRegistry.getType(MockB.class);
        MapType mapA = new MapType(typeA, typeRegistry);

        MapType templatedMap = new TemplatedMapType(typeA, typeRegistry);

        configurationPersistenceManager.register("sample", mapA);
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
        illegalPathHelper("sample", "Invalid path 'sample': no parent");
    }

    public void testCloneInvalidParentPath() throws ValidationException
    {
        illegalPathHelper("huh/instance", "Invalid path 'huh/instance': references non-existant root scope 'huh'");
    }

    public void testCloneInvalidPath() throws ValidationException
    {
        illegalPathHelper("sample/nosuchinstance", "Invalid path 'sample/nosuchinstance': path does not exist");
    }

    private void illegalPathHelper(String path, String expectedError) throws ValidationException
    {
        try
        {
            configurationRefactoringManager.clone(path, "clone", false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(expectedError, e.getMessage());
        }
    }

    public void testCloneEmptyCloneName() throws ValidationException, TypeException
    {
        invalidCloneNameHelper(insertTemplateA(rootPath, "a", false), "", "name requires a value");
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
        String path = configurationTemplateManager.insert("sample", createAInstance());
        String clonePath = configurationRefactoringManager.clone(path, "clone", false);
        assertEquals("sample/clone", clonePath);
        assertClone(configurationTemplateManager.getInstance(clonePath, MockA.class));
    }

    public void testSimpleCloneInTemplateScope() throws ValidationException, TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance(), false);
        String clonePath = configurationRefactoringManager.clone(path, "clone", false);
        assertEquals("template/clone", clonePath);
        MockA clone = configurationTemplateManager.getInstance(clonePath, MockA.class);
        assertTrue(clone.isConcrete());
        assertEquals(configurationTemplateManager.getRecord(rootPath).getHandle(), configurationTemplateManager.getTemplateParentRecord(clonePath).getHandle());
        assertClone(clone);
    }

    public void testCloneOfTemplate() throws ValidationException, TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance(), true);
        String clonePath = configurationRefactoringManager.clone(path, "clone", false);
        assertEquals("template/clone", clonePath);
        MockA clone = configurationTemplateManager.getInstance(clonePath, MockA.class);
        assertFalse(clone.isConcrete());
        assertEquals(configurationTemplateManager.getRecord(rootPath).getHandle(), configurationTemplateManager.getTemplateParentRecord(clonePath).getHandle());
        assertClone(clone);
    }

    private MockA createAInstance()
    {
        MockA instance = new MockA("a");
        instance.setX(10);
        MockB b = new MockB("b");
        b.setY(44);
        instance.setB(b);
        MockB colby = new MockB("colby");
        colby.setY(1);
        instance.getBmap().put("colby", colby);
        return instance;
    }

    private void assertClone(MockA clone)
    {
        assertEquals("clone", clone.getName());
        assertEquals(10, clone.getX());
        MockB cloneB = clone.getB();
        assertEquals("b", cloneB.getName());
        assertEquals(44, cloneB.getY());
        assertEquals(1, clone.getBmap().size());
        MockB cloneColby = clone.getBmap().get("colby");
        assertEquals("colby", cloneColby.getName());
        assertEquals(1, cloneColby.getY());
    }

    private void invalidCloneNameHelper(String path, String cloneName, String expectedError) throws TypeException
    {
        try
        {
            configurationRefactoringManager.clone(path, cloneName, false);
            fail();
        }
        catch(ValidationException e)
        {
            assertEquals(expectedError, e.getMessage());
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
        private Map<String, MockB> bmap = new HashMap<String, MockB>();

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

        public Map<String, MockB> getBmap()
        {
            return bmap;
        }

        public void setBmap(Map<String, MockB> bmap)
        {
            this.bmap = bmap;
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
}
