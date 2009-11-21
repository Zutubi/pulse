package com.zutubi.tove.config;

import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import static com.zutubi.tove.type.record.PathUtils.getBaseName;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asMap;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Mapping;
import com.zutubi.validation.ValidationException;
import org.acegisecurity.AccessDeniedException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.*;
import static java.util.Arrays.asList;

public class ConfigurationRefactoringManagerTest extends AbstractConfigurationSystemTestCase
{
    private static final String SAMPLE_SCOPE = "sample";
    private static final String TEMPLATE_SCOPE = "template";
    private static final String NAME_ROOT = "root";

    private ConfigurationRefactoringManager configurationRefactoringManager;
    private String rootPath;
    private long rootHandle;

    protected void setUp() throws Exception
    {
        super.setUp();
        configurationRefactoringManager = new ConfigurationRefactoringManager();
        configurationRefactoringManager.setTypeRegistry(typeRegistry);
        configurationRefactoringManager.setConfigurationTemplateManager(configurationTemplateManager);
        configurationRefactoringManager.setConfigurationReferenceManager(configurationReferenceManager);
        configurationRefactoringManager.setConfigurationSecurityManager(configurationSecurityManager);
        configurationRefactoringManager.setRecordManager(recordManager);

        CompositeType typeA = typeRegistry.register(MockA.class);
        MapType mapA = new MapType(typeA, typeRegistry);

        MapType templatedMap = new TemplatedMapType(typeA, typeRegistry);

        configurationPersistenceManager.register(SAMPLE_SCOPE, mapA);
        configurationPersistenceManager.register(TEMPLATE_SCOPE, templatedMap);

        MutableRecord root = unstantiate(new MockA(NAME_ROOT));
        configurationTemplateManager.markAsTemplate(root);
        rootPath = configurationTemplateManager.insertRecord(TEMPLATE_SCOPE, root);
        rootHandle = configurationTemplateManager.getRecord(rootPath).getHandle();
    }

    public void testCanCloneEmptyPath()
    {
        assertFalse(configurationRefactoringManager.canClone(""));
    }

    public void testCanCloneSingleElementPath()
    {
        assertFalse(configurationRefactoringManager.canClone(SAMPLE_SCOPE));
    }

    public void testCanCloneNonexistantPath()
    {
        assertFalse(configurationRefactoringManager.canClone("sample/fu"));
    }

    public void testCanCloneTemplateRoot()
    {
        assertFalse(configurationRefactoringManager.canClone(rootPath));
    }

    public void testCanCloneParentPathNotACollection()
    {
        configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertFalse(configurationRefactoringManager.canClone("sample/a/b"));
    }

    public void testCanCloneParentPathAList()
    {
        String aPath = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        String listPath = getPath(aPath, "blist");
        String clonePath = getPath(listPath, configurationTemplateManager.getRecord(listPath).keySet().iterator().next());
        assertFalse(configurationRefactoringManager.canClone(clonePath));
    }

    public void testCanClonePermanent()
    {
        MockA a = createAInstance("a");
        a.setPermanent(true);
        String aPath = configurationTemplateManager.insert(SAMPLE_SCOPE, a);
        assertFalse(configurationRefactoringManager.canClone(aPath));
    }

    public void testCanCloneTemplateItem()
    {
        configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertTrue(configurationRefactoringManager.canClone("sample/a"));
    }

    public void testCanCloneItemBelowTopLevel()
    {
        configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertTrue(configurationRefactoringManager.canClone("sample/a/bmap/colby"));
    }

    public void testCloneEmptyPath()
    {
        illegalClonePathHelper("", "Invalid path '': no parent");
    }

    public void testCloneSingleElementPath()
    {
        illegalClonePathHelper(SAMPLE_SCOPE, "Invalid path 'sample': no parent");
    }

    public void testCloneInvalidParentPath()
    {
        illegalClonePathHelper("huh/instance", "Invalid path 'huh': references non-existant root scope 'huh'");
    }

    public void testCloneInvalidPath()
    {
        illegalClonePathHelper("sample/nosuchinstance", "Invalid path 'sample/nosuchinstance': path does not exist");
    }

    public void testCloneParentPathNotACollection()
    {
        configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        illegalClonePathHelper("sample/a/b", "Invalid parent path 'sample/a': only elements of a map collection may be cloned (parent has type com.zutubi.tove.type.CompositeType)");
    }

    public void testCloneParentPathAList()
    {
        String aPath = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        String listPath = getPath(aPath, "blist");
        String clonePath = getPath(listPath, configurationTemplateManager.getRecord(listPath).keySet().iterator().next());
        illegalClonePathHelper(clonePath, "Invalid parent path '" + listPath + "': only elements of a map collection may be cloned (parent has type com.zutubi.tove.type.ListType)");
    }

    public void testCloneTemplateRoot()
    {
        illegalClonePathHelper(rootPath, "Invalid path '" + rootPath + "': cannot clone root of a template hierarchy");
    }

    public void testClonePermanent() throws TypeException
    {
        MockA a = createAInstance("a");
        a.setPermanent(true);
        String aPath = configurationTemplateManager.insert(SAMPLE_SCOPE, a);
        invalidCloneNameHelper(aPath, "clone", "Invalid path '" + aPath + "': refers to a permanent record");
    }

    private void illegalClonePathHelper(String path, String expectedError)
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
        catch (ToveRuntimeException e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(expectedError, cause.getMessage());
        }
    }

    public void testCloneEmptyCloneName() throws TypeException
    {
        invalidCloneNameHelper(insertTemplateA(rootPath, "a", false), "", "Invalid empty clone key");
    }

    public void testCloneDuplicateCloneName() throws TypeException
    {
        insertTemplateA(rootPath, "existing", false);
        invalidCloneNameHelper(insertTemplateA(rootPath, "a", false), "existing", "name is already in use, please select another name");
    }

    public void testCloneCloneNameInAncestor() throws TypeException
    {
        addB(rootPath, "parentB");
        String childPath = insertTemplateA(rootPath, "child", false);
        configurationTemplateManager.delete(getPath(childPath, "bmap", "parentB"));
        String childBPath = addB(childPath, "childB");
        invalidCloneNameHelper(childBPath, "parentB", "name is already in use in ancestor \"root\", please select another name");
    }

    public void testCloneCloneNameInDescendent() throws TypeException
    {
        String parentBPath = addB(rootPath, "parentB");
        String childPath = insertTemplateA(rootPath, "child", false);
        addB(childPath, "childB");
        invalidCloneNameHelper(parentBPath, "childB", "name is already in use in descendent \"child\", please select another name");
    }

    public void testSimpleClone()
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        String clonePath = configurationRefactoringManager.clone(path, "clone of a");
        assertEquals("sample/clone of a", clonePath);
        assertClone(configurationTemplateManager.getInstance(clonePath, MockA.class), "a");
    }

    public void testSimpleCloneInTemplateScope() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a"), false);
        String clonePath = configurationRefactoringManager.clone(path, "clone of a");
        assertEquals("template/clone of a", clonePath);
        MockA clone = configurationTemplateManager.getInstance(clonePath, MockA.class);
        assertTrue(clone.isConcrete());
        assertEquals(rootHandle, configurationTemplateManager.getTemplateParentRecord(clonePath).getHandle());
        assertClone(clone, "a");
    }

    public void testCloneOfTemplate() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a"), true);
        String clonePath = configurationRefactoringManager.clone(path, "clone of a");
        assertEquals("template/clone of a", clonePath);
        MockA clone = configurationTemplateManager.getInstance(clonePath, MockA.class);
        assertFalse(clone.isConcrete());
        assertEquals(rootHandle, configurationTemplateManager.getTemplateParentRecord(clonePath).getHandle());
        assertClone(clone, "a");
    }

    public void testCloneBelowTopLevel()
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        MockB colby = configurationTemplateManager.getInstance(path, MockA.class).getBmap().values().iterator().next();
        String clonePath = configurationRefactoringManager.clone(colby.getConfigurationPath(), "clone");
        assertEquals("sample/a/bmap/clone", clonePath);
        MockB clone = configurationTemplateManager.getInstance(clonePath, MockB.class);
        
        assertNotSame(colby, clone);
        assertEquals("clone", clone.getName());
        assertEquals(1, clone.getY());
    }

    public void testMultipleClone()
    {
        configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("1"));
        configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("2"));

        configurationRefactoringManager.clone(SAMPLE_SCOPE, asMap(asPair("1", "clone of 1"), asPair("2", "clone of 2")));

        assertClone(configurationTemplateManager.getInstance(getPath(SAMPLE_SCOPE, "clone of 1"), MockA.class), "1");
        assertClone(configurationTemplateManager.getInstance(getPath(SAMPLE_SCOPE, "clone of 2"), MockA.class), "2");
    }

    public void testCloneWithInternalReference()
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
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

    public void testMultipleCloneWithReferenceBetween()
    {
        String path1 = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("1"));
        MockA instance1 = configurationTemplateManager.getInstance(path1, MockA.class);
        MockA instance2 = createAInstance("2");
        instance2.setRefToRef(instance1.getRef());
        String path2 = configurationTemplateManager.insert(SAMPLE_SCOPE, instance2);

        configurationRefactoringManager.clone(SAMPLE_SCOPE, asMap(asPair("1", "clone of 1"), asPair("2", "clone of 2")));

        instance1 = configurationTemplateManager.getInstance(path1, MockA.class);
        instance2 = configurationTemplateManager.getInstance(path2, MockA.class);
        MockA clone1 = configurationTemplateManager.getInstance(getPath(SAMPLE_SCOPE, "clone of 1"), MockA.class);
        MockA clone2 = configurationTemplateManager.getInstance(getPath(SAMPLE_SCOPE, "clone of 2"), MockA.class);

        assertClone(clone1, "1");
        assertClone(clone2, "2");
        assertSame(instance1.getRef(), instance2.getRefToRef());
        assertNotSame(instance1.getRef(), clone2.getRefToRef());
        assertSame(clone1.getRef(), clone2.getRefToRef());
    }

    public void testMultipleCloneOfTemplateHierarchy() throws TypeException
    {
        templateHierarchyHelper(asMap(asPair("parent", "clone of parent"), asPair("child", "clone of child")));
    }

    public void testMultipleCloneOfTemplateHierarchyChildFirst() throws TypeException
    {
        templateHierarchyHelper(asMap(asPair("child", "clone of child"), asPair("parent", "clone of parent")));
    }

    public void testCloneWithInheritedItem() throws TypeException
    {
        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        insertTemplateAInstance(parentPath, new MockA("child"), false);

        String clonePath = configurationRefactoringManager.clone("template/child/bmap/colby", "clone of colby");
        MockB clone = configurationTemplateManager.getInstance(clonePath, MockB.class);
        assertEquals(1, clone.getY());
    }

    public void testCloneWithOveriddenItem() throws TypeException
    {
        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        MockA childInstance = createAInstance("child");
        childInstance.getBmap().get("colby").setY(111222333);
        insertTemplateAInstance(parentPath, childInstance, false);

        String clonePath = configurationRefactoringManager.clone("template/child/bmap/colby", "clone of colby");
        MockB clone = configurationTemplateManager.getInstance(clonePath, MockB.class);
        assertEquals(111222333, clone.getY());
    }

    private void templateHierarchyHelper(Map<String, String> originalKeyToCloneKey) throws TypeException
    {
        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        insertTemplateAInstance(parentPath, createAInstance("child"), false);

        configurationRefactoringManager.clone(TEMPLATE_SCOPE, originalKeyToCloneKey);

        String parentClonePath = getPath(TEMPLATE_SCOPE, "clone of parent");
        MockA parentClone = configurationTemplateManager.getInstance(parentClonePath, MockA.class);
        assertFalse(parentClone.isConcrete());
        assertClone(parentClone, "parent");
        assertEquals(rootHandle, configurationTemplateManager.getTemplateParentRecord(parentClonePath).getHandle());

        String childClonePath = getPath(TEMPLATE_SCOPE, "clone of child");
        MockA childClone = configurationTemplateManager.getInstance(childClonePath, MockA.class);
        assertTrue(childClone.isConcrete());
        assertEquals(parentClone.getHandle(), configurationTemplateManager.getTemplateParentRecord(childClonePath).getHandle());
    }

    public void testCanExtractParentTemplateInvalidParentPath()
    {
        assertFalse(configurationRefactoringManager.canExtractParentTemplate("nosuchscope/foo"));
    }

    public void testCanExtractParentTemplateParentPathNotAMap()
    {
        assertFalse(configurationRefactoringManager.canExtractParentTemplate(rootPath + "/foo"));
    }

    public void testCanExtractParentTemplateParentPathNotATemplatedScope()
    {
        assertFalse(configurationRefactoringManager.canExtractParentTemplate(SAMPLE_SCOPE + "/foo"));
    }

    public void testCanExtractParentTemplateInvalidItem()
    {
        assertFalse(configurationRefactoringManager.canExtractParentTemplate(TEMPLATE_SCOPE + "/nope"));
    }

    public void testCanExtractParentTemplateRootTemplate()
    {
        assertFalse(configurationRefactoringManager.canExtractParentTemplate(rootPath));
    }

    public void testCanExtractParentTemplate() throws TypeException
    {
        String aPath = insertTemplateAInstance(rootPath, createAInstance("a"), false);
        assertTrue(configurationRefactoringManager.canExtractParentTemplate(aPath));
    }

    public void testExtractParentTemplateInvalidParentPath()
    {
        extractParentTemplateErrorHelper("nosuchscope", Collections.<String>emptyList(), "foo", "Invalid parent path 'nosuchscope': does not refer to a templated collection");
    }

    public void testExtractParentTemplateParentPathNotAMap()
    {
        extractParentTemplateErrorHelper(rootPath, Collections.<String>emptyList(), "foo", "Invalid parent path '" + rootPath + "': does not refer to a templated collection");
    }

    public void testExtractParentTemplateParentPathNotATemplatedScope()
    {
        extractParentTemplateErrorHelper(SAMPLE_SCOPE, Collections.<String>emptyList(), "foo", "Invalid parent path '" + SAMPLE_SCOPE + "': does not refer to a templated collection");
    }

    public void testExtractParentTemplateInvalidChildKey()
    {
        extractParentTemplateErrorHelper(TEMPLATE_SCOPE, asList("nope"), "foo", "Invalid child key 'nope': does not refer to an element of the templated collection");
    }

    public void testExtractParentTemplateRootTemplate()
    {
        extractParentTemplateErrorHelper(TEMPLATE_SCOPE, asList(getBaseName(rootPath)), "foo", "Invalid child key 'root': cannot extract parent from the root of a template hierarchy");
    }

    public void testExtractParentTemplateChildKeysNotSiblings() throws TypeException
    {
        String parentPath = insertTemplateA(rootPath, "parent", true);
        insertTemplateA(parentPath, "child", false);
        extractParentTemplateErrorHelper(TEMPLATE_SCOPE, asList("parent", "child"), "foo", "Invalid child keys: all child keys must refer to siblings in the template hierarchy");
    }

    public void testExtractParentTemplateParentNameEmpty() throws TypeException
    {
        insertTemplateA(rootPath, "a", true);
        extractParentTemplateErrorHelper(TEMPLATE_SCOPE, asList("a"), "", "Parent template name is required");
    }

    public void testExtractParentTemplateParentNameNotUnique() throws TypeException
    {
        insertTemplateA(rootPath, "a", true);
        extractParentTemplateErrorHelper(TEMPLATE_SCOPE, asList("a"), "a", "com.zutubi.validation.ValidationException: name is already in use, please select another name");
    }

    private void extractParentTemplateErrorHelper(String parentPath, List<String> childKeys, String parentTemplateName, String expectedError)
    {
        try
        {
            configurationRefactoringManager.extractParentTemplate(parentPath, childKeys, parentTemplateName);
            fail();
        }
        catch (Exception e)
        {
            assertEquals(expectedError, e.getMessage());
        }
    }

    public void testExtractParentTemplateSimple() throws TypeException
    {
        String aPath = insertTemplateAInstance(rootPath, createAInstance("a"), false);
        String extractedPath = configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, asList("a"), "extracted");

        // Ensure the instances both look as expected
        MockA extractedInstance = configurationTemplateManager.getInstance(extractedPath, MockA.class);
        assertAInstance(extractedInstance, "extracted");
        assertFalse(extractedInstance.isConcrete());

        MockA aInstance = configurationTemplateManager.getInstance(aPath, MockA.class);
        assertAInstance(aInstance, "a");
        assertTrue(aInstance.isConcrete());

        // Assert the expected shape of the hierarchy
        TemplateNode node = configurationTemplateManager.getTemplateNode(aPath);
        assertNotNull(node);
        assertEquals("root/extracted/a", node.getTemplatePath());

        node = configurationTemplateManager.getTemplateNode(extractedPath);
        assertNotNull(node);
        assertEquals("root/extracted", node.getTemplatePath());

        // Now assert that the fields have really been pulled up.
        assertAllValuesExtracted("a");
    }

    public void testExtractParentTemplateIdenticalSiblings() throws TypeException
    {
        String path1 = insertTemplateAInstance(rootPath, createAInstance("1"), false);
        String path2 = insertTemplateAInstance(rootPath, createAInstance("2"), false);
        String extractedPath = configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, asList("1", "2"), "extracted");

        // Ensure parent has all fields pulled up
        MockA extractedInstance = configurationTemplateManager.getInstance(extractedPath, MockA.class);
        assertAInstance(extractedInstance, "extracted");
        assertFalse(extractedInstance.isConcrete());

        // And both children look good too
        MockA instance1 = configurationTemplateManager.getInstance(path1, MockA.class);
        assertAInstance(instance1, "1");
        assertTrue(instance1.isConcrete());

        MockA instance2 = configurationTemplateManager.getInstance(path2, MockA.class);
        assertAInstance(instance2, "2");
        assertTrue(instance2.isConcrete());

        // Assert the expected shape of the hierarchy
        TemplateNode node = configurationTemplateManager.getTemplateNode(extractedPath);
        assertNotNull(node);
        assertEquals("root/extracted", node.getTemplatePath());

        assertEquals(2, node.getChildren().size());
        List<String> children = CollectionUtils.map(node.getChildren(), new Mapping<TemplateNode, String>()
        {
            public String map(TemplateNode templateNode)
            {
                return templateNode.getId();
            }
        });
        Collections.sort(children);
        assertEquals("1", children.get(0));
        assertEquals("2", children.get(1));

        // Now assert that the fields have really been pulled up.
        assertAllValuesExtracted("1");
        assertAllValuesExtracted("2");
    }

    public void testExtractParentTemplateReferenceToExtracted() throws TypeException
    {
        externalReferenceHelper("referee");
    }

    public void testExtractParentTemplateReferenceFromExtracted() throws TypeException
    {
        externalReferenceHelper("referer");
    }

    private void externalReferenceHelper(String extractKey) throws TypeException
    {
        String refereePath = insertTemplateAInstance(rootPath, createAInstance("referee"), false);
        MockA referee = configurationTemplateManager.getInstance(refereePath, MockA.class);
        MockA referer = new MockA("referer");
        referer.setRefToRef(referee.getRef());
        referer.getListOfRefs().add(referee.getRef());
        String refererPath = insertTemplateAInstance(rootPath, referer, false);

        configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, Arrays.asList(extractKey), "extracted");

        referee = configurationTemplateManager.getInstance(refereePath, MockA.class);
        referer = configurationTemplateManager.getInstance(refererPath, MockA.class);
        assertSame(referee.getRef(), referer.getRefToRef());
        assertSame(referee.getRef(), referer.getListOfRefs().get(0));
    }

    public void testExtractParentTemplateInternalReference() throws TypeException
    {
        final String EXTRACTED_NAME = "extracted";

        String aPath = insertAInstanceWithInternalReference("a");
        String extractedPath = configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, Arrays.asList("a"), EXTRACTED_NAME);

        assertExtractedInternalReference(aPath, extractedPath);
        assertExtractedInternalReference(extractedPath, extractedPath);
    }

    public void testExtractParentTemplateInternalReferenceList() throws TypeException
    {
        String aPath = insertAInstanceWithInternalReferenceList("a");
        String extractedPath = configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, Arrays.asList("a"), "extracted");

        assertExtractedInternalReferenceList(aPath, extractedPath);
        assertExtractedInternalReferenceList(extractedPath, extractedPath);
    }

    public void testExtractParentTemplateCommonExternalReferences() throws TypeException
    {
        String refereePath = insertTemplateAInstance(rootPath, createAInstance("referee"), false);
        String path1 = insertTemplateAInstance(rootPath, createAInstance("one"), false);
        String path2 = insertTemplateAInstance(rootPath, createAInstance("two"), false);

        MockA referee = configurationTemplateManager.getInstance(refereePath, MockA.class);
        MockA a1 = configurationTemplateManager.getInstance(path1, MockA.class);
        a1.setRefToRef(referee.getRef());
        configurationTemplateManager.save(a1);

        MockA a2 = configurationTemplateManager.getInstance(path2, MockA.class);
        a2.setRefToRef(referee.getRef());
        configurationTemplateManager.save(a2);

        String extractedPath = configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, Arrays.asList("one", "two"), "extracted");

        referee = configurationTemplateManager.getInstance(refereePath, MockA.class);
        a1 = configurationTemplateManager.getInstance(path1, MockA.class);
        a2 = configurationTemplateManager.getInstance(path2, MockA.class);
        MockA extracted = configurationTemplateManager.getInstance(extractedPath, MockA.class);
        
        assertSame(referee.getRef(), a1.getRefToRef());
        assertSame(referee.getRef(), a2.getRefToRef());
        assertSame(referee.getRef(), extracted.getRefToRef());
    }

    public void testExtractParentTemplateCommonInternalReferences() throws TypeException
    {
        String path1 = insertAInstanceWithInternalReference("one");
        String path2 = insertAInstanceWithInternalReference("two");
        String extractedPath = configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, Arrays.asList("one", "two"), "extracted");

        assertExtractedInternalReference(path1, extractedPath);
        assertExtractedInternalReference(path2, extractedPath);
        assertExtractedInternalReference(extractedPath, extractedPath);
    }

    public void testExtractParentTemplateCommonInternalReferenceLists() throws TypeException
    {
        String path1 = insertAInstanceWithInternalReferenceList("one");
        String path2 = insertAInstanceWithInternalReferenceList("two");
        String extractedPath = configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, Arrays.asList("one", "two"), "extracted");

        assertExtractedInternalReferenceList(path1, extractedPath);
        assertExtractedInternalReferenceList(path2, extractedPath);
        assertExtractedInternalReferenceList(extractedPath, extractedPath);
    }

    private String insertAInstanceWithInternalReference(String name) throws TypeException
    {
        MockA mockA = createAInstance(name);
        String aPath = insertTemplateAInstance(rootPath, mockA, false);

        mockA = configurationTemplateManager.getInstance(aPath, MockA.class);
        mockA.setRefToRef(mockA.getRef());
        return configurationTemplateManager.save(mockA);
    }

    private void assertExtractedInternalReference(String originalPath, String extractedPath)
    {
        MockA mockA = configurationTemplateManager.getInstance(originalPath, MockA.class);
        assertSame(mockA.getRef(), mockA.getRefToRef());
        // Check that the value is owned at the right level (scrubbing was
        // applied correctly).
        assertEquals(getBaseName(extractedPath), ((TemplateRecord) configurationTemplateManager.getRecord(extractedPath)).getOwner("refToRef"));
    }

    private String insertAInstanceWithInternalReferenceList(String name) throws TypeException
    {
        MockA mockA = createAInstance(name);
        String aPath = insertTemplateAInstance(rootPath, mockA, false);

        mockA = configurationTemplateManager.getInstance(aPath, MockA.class);
        mockA.getListOfRefs().add(mockA.getRef());
        configurationTemplateManager.save(mockA);
        return aPath;
    }

    private void assertExtractedInternalReferenceList(String originalPath, String extractedPath)
    {
        MockA mockA;
        mockA = configurationTemplateManager.getInstance(originalPath, MockA.class);
        assertSame(mockA.getRef(), mockA.getListOfRefs().get(0));
        assertEquals(getBaseName(extractedPath), ((TemplateRecord) configurationTemplateManager.getRecord(extractedPath)).getOwner("listOfRefs"));
    }

    public void testExtractParentTemplateNullReference() throws TypeException
    {
        MockA a = createAInstance("a");
        a.getBmap().put("b", new MockB("b"));
        insertTemplateAInstance(rootPath, a, false);
        String extractedPath = configurationRefactoringManager.extractParentTemplate(TEMPLATE_SCOPE, Arrays.asList("a"), "extracted");

        MockA extracted = configurationTemplateManager.getInstance(extractedPath, MockA.class);
        assertNull(extracted.getBmap().get("b").getRefToRef());
    }

    private void assertAllValuesExtracted(String key)
    {
        String path = getPath(TEMPLATE_SCOPE, key);
        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord(path);
        assertEquals(key, record.getOwner("name"));
        assertEquals("10", record.get("x"));
        assertEquals("extracted", record.getOwner("x"));

        TemplateRecord bRecord = (TemplateRecord) record.get("b");
        assertEquals("44", bRecord.get("y"));
        assertEquals("extracted", bRecord.getOwner("y"));

        TemplateRecord blistRecord = (TemplateRecord) record.get("blist");
        TemplateRecord lisbyRecord = (TemplateRecord) blistRecord.get(blistRecord.nestedKeySet().iterator().next());
        assertEquals("lisby", lisbyRecord.get("name"));

        TemplateRecord bmapRecord = (TemplateRecord) record.get("bmap");
        TemplateRecord colbyRecord = (TemplateRecord) bmapRecord.get("colby");
        assertEquals("1", colbyRecord.get("y"));
        assertEquals("extracted", colbyRecord.getOwner("y"));
    }

    public void testGetPullUpAncestorsInvalidPath()
    {
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPullUpAncestors("invalid/path/here"));
    }

    public void testGetPullUpAncestorsTemplateCollectionItem() throws TypeException
    {
        String path = insertTemplateA(rootPath, "a", false);
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPullUpAncestors(path));
    }

    public void testGetPullUpAncenstorsNotTemplate()
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPullUpAncestors(path));
    }

    public void testGetPullUpAncenstorsNoAncestor() throws TypeException
    {
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPullUpAncestors(getPath(rootPath, "b")));
    }

    public void testGetPullUpAncestorsNoValidAncestor() throws TypeException
    {
        String templateParentPath = insertTemplateAInstance(rootPath, createAInstance("a1"), true);
        String path = insertTemplateAInstance(templateParentPath, createAInstance("a2"), false);
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPullUpAncestors(getPath(path, "b")));
    }

    public void testGetPullUpAncestors() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        assertEquals(asList(NAME_ROOT), configurationRefactoringManager.getPullUpAncestors(getPath(path, "b")));
    }

    public void testCanPullUpAnyInvalidPath()
    {
        assertFalse(configurationRefactoringManager.canPullUp("invalid/path/here"));
    }

    public void testCanPullUpAnyTemplateCollectionItem() throws TypeException
    {
        String path = insertTemplateA(rootPath, "a", false);
        assertFalse(configurationRefactoringManager.canPullUp(path));
    }

    public void testCanPullUpAnyNotTemplate()
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertFalse(configurationRefactoringManager.canPullUp(path));
    }

    public void testCanPullUpAnyNoAncestor() throws TypeException
    {
        assertFalse(configurationRefactoringManager.canPullUp(getPath(rootPath, "b")));
    }

    public void testCanPullUpAnyNoValidAncestor() throws TypeException
    {
        String templateParentPath = insertTemplateAInstance(rootPath, createAInstance("a1"), true);
        String path = insertTemplateAInstance(templateParentPath, createAInstance("a2"), false);
        assertFalse(configurationRefactoringManager.canPullUp(getPath(path, "b")));
    }

    public void testCanPullUpAny() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        assertTrue(configurationRefactoringManager.canPullUp(getPath(path, "b")));
    }
    
    public void testCanPullUpInvalidPath()
    {
        assertFalse(configurationRefactoringManager.canPullUp("invalid/path/here", "anything"));
    }

    public void testCanPullUpTemplateCollectionItem() throws TypeException
    {
        String path = insertTemplateA(rootPath, "a", false);
        assertFalse(configurationRefactoringManager.canPullUp(path, NAME_ROOT));
    }

    public void testCanPullUpNonTemplatedItem() throws TypeException
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertFalse(configurationRefactoringManager.canPullUp(path, NAME_ROOT));
    }

    public void testCanPullUpNoAncestor() throws TypeException
    {
        assertFalse(configurationRefactoringManager.canPullUp(getPath(rootPath, "b"), NAME_ROOT));
    }

    public void testCanPullUpBadAncestor() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        insertTemplateAInstance(rootPath, createAInstance("a2"), false);
        assertFalse(configurationRefactoringManager.canPullUp(getPath(path, "b"), "a2"));
    }

    public void testCanPullUpAncestorDefinesPath() throws TypeException
    {
        String templateParentPath = insertTemplateAInstance(rootPath, createAInstance("a1"), true);
        String path = insertTemplateAInstance(templateParentPath, createAInstance("a2"), false);
        assertFalse(configurationRefactoringManager.canPullUp(getPath(path, "b"), NAME_ROOT));
    }

    public void testCanPullUpSiblingDefinesPath() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        insertTemplateAInstance(rootPath, createAInstance("a2"), false);
        assertFalse(configurationRefactoringManager.canPullUp(getPath(path, "b"), NAME_ROOT));
    }

    public void testCanPullUpSimple() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        assertFalse(configurationRefactoringManager.canPullUp(getPath(path, "b", "y"), NAME_ROOT));
    }

    public void testCanPullUpInternalReference() throws TypeException
    {
        final String NAME_PARENT = "parent";

        String templateParentPath = insertTemplateAInstance(rootPath, new MockA(NAME_PARENT), true);
        String childPath = insertTemplateAInstance(templateParentPath, createAInstance("child"), false);

        MockA child = configurationTemplateManager.getInstance(childPath, MockA.class);
        child.getB().setRefToRef(child.getRef());
        configurationTemplateManager.save(child);

        String pullPath = getPath(childPath, "b");
        assertFalse(configurationRefactoringManager.canPullUp(pullPath, NAME_PARENT));
    }

    public void testCanPullUpComposite() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        assertTrue(configurationRefactoringManager.canPullUp(getPath(path, "b"), NAME_ROOT));
    }

    public void testCanPullUpCollectionItem() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        assertTrue(configurationRefactoringManager.canPullUp(getPath(path, "bmap", "colby"), NAME_ROOT));
    }

    public void testCanPullUpInternalReferenceDefinedInAncestor() throws TypeException
    {
        final String NAME_PARENT = "parent";

        MockA parent = new MockA(NAME_PARENT);
        parent.setRef(new Referee("ee"));
        String templateParentPath = insertTemplateAInstance(rootPath, parent, true);

        parent = configurationTemplateManager.getInstance(templateParentPath, MockA.class);
        MockA child = new MockA("child");
        MockB childB = new MockB("b");
        childB.setRefToRef(parent.getRef());
        child.setB(childB);
        String childPath = insertTemplateAInstance(templateParentPath, child, false);

        String pullPath = getPath(childPath, "b");
        assertTrue(configurationRefactoringManager.canPullUp(pullPath, NAME_PARENT));
    }

    public void testPullUpUnableToWriteToAncestor() throws TypeException
    {
        final String ERROR_MESSAGE = "No such luck, bozo";

        ConfigurationSecurityManager securityManager = mock(ConfigurationSecurityManager.class);
        doThrow(new AccessDeniedException(ERROR_MESSAGE)).when(securityManager).ensurePermission(startsWith(rootPath), anyString());
        configurationRefactoringManager.setConfigurationSecurityManager(securityManager);

        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        try
        {
            configurationRefactoringManager.pullUp(getPath(path, "b"), NAME_ROOT);
            fail("Should not be able to pull up with no permission");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString(ERROR_MESSAGE));
        }
    }

    public void testPullUpSiblingDefinesPath() throws TypeException
    {
        // a1
        //   b <-- pulled up
        // a2
        //   b
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), false);
        insertTemplateAInstance(rootPath, createAInstance("a2"), false);
        try
        {
            configurationRefactoringManager.pullUp(getPath(path, "b"), NAME_ROOT);
            fail("Should not be able to pull up when sibling defines path");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Unable to pull up path 'template/a1/b': Ancestor 'root' already has descendents [template/a2/b] that define this path", e.getMessage());
        }
    }

    public void testPullUpInternalReference() throws TypeException
    {
        final String NAME_PARENT = "parent";

        // child
        //   ref: defines ee
        //   b <-- pulled up
        //     refToRef: refers to ee
        String templateParentPath = insertTemplateAInstance(rootPath, new MockA(NAME_PARENT), true);
        String childPath = insertTemplateAInstance(templateParentPath, createAInstance("child"), false);

        MockA child = configurationTemplateManager.getInstance(childPath, MockA.class);
        child.getB().setRefToRef(child.getRef());
        configurationTemplateManager.save(child);

        try
        {
            String pullPath = getPath(childPath, "b");
            configurationRefactoringManager.pullUp(pullPath, NAME_PARENT);
            fail("Should not be able to pull up when path contains reference not defined at ancestor level");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Unable to pull up path 'template/child/b': Path contains reference to 'template/child/ref' which does not exist in ancestor 'parent'", e.getMessage());
        }
    }

    public void testPullUpComposite() throws TypeException
    {
        final String NAME_PARENT = "parent";

        // child
        //   b <-- pulled up
        String templateParentPath = insertTemplateAInstance(rootPath, new MockA(NAME_PARENT), true);
        String childPath = insertTemplateAInstance(templateParentPath, createAInstance("child"), false);
        String pullPath = getPath(childPath, "b");

        String path = configurationRefactoringManager.pullUp(pullPath, NAME_PARENT);

        assertEquals(getPath(templateParentPath, "b"), path);
        TemplateRecord pulledUp = (TemplateRecord) configurationTemplateManager.getRecord(path);
        assertEquals("44", pulledUp.get("y"));
        assertEquals(NAME_PARENT, pulledUp.getOwner("y"));
        
        TemplateRecord inherited = (TemplateRecord) configurationTemplateManager.getRecord(pullPath);
        assertEquals("44", inherited.get("y"));
        assertEquals(NAME_PARENT, inherited.getOwner("y"));
    }

    public void testPullUpCollectionItem() throws TypeException
    {
        final String NAME_PARENT = "parent";

        // child
        //   bmap
        //     colby <-- pulled up
        String templateParentPath = insertTemplateAInstance(rootPath, new MockA(NAME_PARENT), true);
        String childPath = insertTemplateAInstance(templateParentPath, createAInstance("child"), false);
        String pullPath = getPath(childPath, "bmap", "colby");

        String path = configurationRefactoringManager.pullUp(pullPath, NAME_PARENT);

        assertEquals(getPath(templateParentPath, "bmap", "colby"), path);
        TemplateRecord pulledUp = (TemplateRecord) configurationTemplateManager.getRecord(path);
        assertEquals("1", pulledUp.get("y"));
        assertEquals(NAME_PARENT, pulledUp.getOwner("y"));

        TemplateRecord inherited = (TemplateRecord) configurationTemplateManager.getRecord(pullPath);
        assertEquals("1", inherited.get("y"));
        assertEquals(NAME_PARENT, inherited.getOwner("y"));
    }

    public void testPullUpEvents() throws TypeException
    {
        final String NAME_PARENT = "parent";

        String templateParentPath = insertTemplateAInstance(rootPath, new MockA(NAME_PARENT), true);
        String childPath = insertTemplateAInstance(templateParentPath, createAInstance("child"), false);
        String siblingPath = insertTemplateAInstance(templateParentPath, new MockA("sibling"), false);
        String pullPath = getPath(childPath, "b");

        Listener listener = registerListener();

        configurationRefactoringManager.pullUp(pullPath, NAME_PARENT);

        String siblingInsertPath = getPath(siblingPath, "b");
        listener.assertEvents(new InsertEventSpec(siblingInsertPath, false), new PostInsertEventSpec(siblingInsertPath, false));
    }

    public void testPullUpInternalReferenceDefinedInAncestor() throws TypeException
    {
        final String NAME_PARENT = "parent";

        // parent
        //   ref: defines ee
        //
        // child
        //   b <-- pulled up
        //     refToRef: refers to ee
        MockA parent = new MockA(NAME_PARENT);
        parent.setRef(new Referee("ee"));
        String templateParentPath = insertTemplateAInstance(rootPath, parent, true);

        parent = configurationTemplateManager.getInstance(templateParentPath, MockA.class);
        MockA child = new MockA("child");
        MockB childB = new MockB("b");
        childB.setRefToRef(parent.getRef());
        child.setB(childB);
        String childPath = insertTemplateAInstance(templateParentPath, child, false);


        String pullPath = getPath(childPath, "b");
        String path = configurationRefactoringManager.pullUp(pullPath, NAME_PARENT);


        parent = configurationTemplateManager.getInstance(templateParentPath, MockA.class);
        child = configurationTemplateManager.getInstance(childPath, MockA.class);

        assertSame(parent.getRef(), parent.getB().getRefToRef());
        assertSame(child.getRef(), child.getB().getRefToRef());
        
        TemplateRecord templateParentRecord = (TemplateRecord) configurationTemplateManager.getRecord(templateParentPath);
        assertEquals(NAME_PARENT, templateParentRecord.getOwner("b"));
        TemplateRecord pulledUpB = (TemplateRecord) templateParentRecord.get("b");
        assertEquals(NAME_PARENT, pulledUpB.getOwner("refToRef"));

        String handle = Long.toString(((Record) templateParentRecord.get("ref")).getHandle());
        assertEquals(handle, pulledUpB.get("refToRef"));

        TemplateRecord inherited = (TemplateRecord) configurationTemplateManager.getRecord(path);
        assertEquals(NAME_PARENT, inherited.getOwner("refToRef"));
        assertEquals(handle, inherited.get("refToRef"));
    }

    public void testPullUpReferenceWithinPulledUp() throws TypeException
    {
        final String NAME_PARENT = "parent";

        // child
        //   b <-- pulled up
        //     ref: defines ee
        //     refToRef: refers to ee
        String templateParentPath = insertTemplateAInstance(rootPath, new MockA(NAME_PARENT), true);

        MockA child = new MockA("child");
        MockB childB = new MockB("b");
        childB.setRef(new Referee("ee"));
        child.setB(childB);
        String childPath = insertTemplateAInstance(templateParentPath, child, false);

        child = configurationTemplateManager.getInstance(childPath, MockA.class);
        child.getB().setRefToRef(child.getB().getRef());
        configurationTemplateManager.save(child.getB());


        String pullPath = getPath(childPath, "b");
        configurationRefactoringManager.pullUp(pullPath, NAME_PARENT);


        MockA parent = configurationTemplateManager.getInstance(templateParentPath, MockA.class);
        child = configurationTemplateManager.getInstance(childPath, MockA.class);

        assertNotNull(parent.getB());
        assertSame(parent.getB().getRef(), parent.getB().getRef());
        assertSame(child.getB().getRef(), child.getB().getRefToRef());
    }

    public void testPullUpInternalReferenceToWithinPulledUp() throws TypeException
    {
        final String NAME_PARENT = "parent";

        // child
        //   refToRef: refers to ee
        //   b <-- pulled up
        //     ref: defines ee
        String templateParentPath = insertTemplateAInstance(rootPath, new MockA(NAME_PARENT), true);

        MockA child = new MockA("child");
        MockB childB = new MockB("b");
        childB.setRef(new Referee("ee"));
        child.setB(childB);
        String childPath = insertTemplateAInstance(templateParentPath, child, false);

        child = configurationTemplateManager.getInstance(childPath, MockA.class);
        child.setRefToRef(child.getB().getRef());
        configurationTemplateManager.save(child);


        String pullPath = getPath(childPath, "b");
        configurationRefactoringManager.pullUp(pullPath, NAME_PARENT);


        MockA parent = configurationTemplateManager.getInstance(templateParentPath, MockA.class);
        child = configurationTemplateManager.getInstance(childPath, MockA.class);

        assertNotNull(parent.getB());
        assertNotNull(parent.getB().getRef());
        assertNull(parent.getRefToRef());
        assertSame(child.getB().getRef(), child.getRefToRef());
    }

    public void testGetPushDownChildrenInvalidPath()
    {
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPushDownChildren("invalid/path/here"));
    }

    public void testGetPushDownChildrenTemplateCollectionItem() throws TypeException
    {
        String path = insertTemplateA(rootPath, "a", false);
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPushDownChildren(path));
    }

    public void testGetPushDownChildrenNotTemplate()
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPushDownChildren(path));
    }

    public void testGetPushDownChildrenNoChildren() throws TypeException
    {
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPushDownChildren(getPath(rootPath, "b")));
    }

    public void testGetPushDownChildrenHiddenInChild() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), true);
        String childPath = insertTemplateAInstance(path, createAInstance("a2"), false);
        // Hide the path in the child
        configurationTemplateManager.delete(getPath(childPath, "bmap", "colby"));
        assertEquals(Collections.<String>emptyList(), configurationRefactoringManager.getPushDownChildren(getPath(path, "bmap", "colby")));
    }

    public void testGetPushDownChildren() throws TypeException
    {
        final String NAME_CHILD = "child";

        String path = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        insertTemplateAInstance(path, createAInstance(NAME_CHILD), false);
        assertEquals(asList(NAME_CHILD), configurationRefactoringManager.getPushDownChildren(getPath(path, "b")));
    }

    public void testCanPushDownAnyInvalidPath()
    {
        assertFalse(configurationRefactoringManager.canPushDown("invalid/path/here"));
    }

    public void testCanPushDownAnyTemplateCollectionItem() throws TypeException
    {
        String path = insertTemplateA(rootPath, "a", false);
        assertFalse(configurationRefactoringManager.canPushDown(path));
    }

    public void testCanPushDownAnyNotTemplate()
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertFalse(configurationRefactoringManager.canPushDown(path));
    }

    public void testCanPushDownAnyNoChildren() throws TypeException
    {
        assertFalse(configurationRefactoringManager.canPushDown(getPath(rootPath, "b")));
    }

    public void testCanPushDownAnyNoValidChild() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a1"), true);
        String childPath = insertTemplateAInstance(path, createAInstance("a2"), false);
        // Hide the path in the child
        configurationTemplateManager.delete(getPath(childPath, "bmap", "colby"));
        assertFalse(configurationRefactoringManager.canPushDown(getPath(path, "bmap", "colby")));
    }

    public void testCanPushDownAny() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        insertTemplateAInstance(path, createAInstance("child"), false);
        assertTrue(configurationRefactoringManager.canPushDown(getPath(path, "b")));
    }

    public void testCanPushDownInvalidPath()
    {
        assertFalse(configurationRefactoringManager.canPushDown("invalid/path/here", "anything"));
    }

    public void testCanPushDownTemplateCollectionItem() throws TypeException
    {
        String path = insertTemplateA(rootPath, "a", false);
        assertFalse(configurationRefactoringManager.canPushDown(path, NAME_ROOT));
    }

    public void testCanPushDownNonTemplatedItem() throws TypeException
    {
        String path = configurationTemplateManager.insert(SAMPLE_SCOPE, createAInstance("a"));
        assertFalse(configurationRefactoringManager.canPushDown(path, NAME_ROOT));
    }

    public void testCanPushDownNoChild() throws TypeException
    {
        assertFalse(configurationRefactoringManager.canPushDown(getPath(rootPath, "b"), NAME_ROOT));
    }

    public void testCanPushDownBadChild() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a"), false);
        assertFalse(configurationRefactoringManager.canPushDown(getPath(path, "b"), NAME_ROOT));
    }

    public void testCanPushDownChildHidesPath() throws TypeException
    {
        final String NAME_CHILD = "child";

        String path = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        String childPath = insertTemplateAInstance(path, createAInstance(NAME_CHILD), false);
        // Hide the path in the child
        configurationTemplateManager.delete(getPath(childPath, "bmap", "colby"));
        assertFalse(configurationRefactoringManager.canPushDown(getPath(path, "bmap", "colby"), NAME_CHILD));
    }

    public void testCanPushDownSimple() throws TypeException
    {
        final String NAME_CHILD = "child";

        String path = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        insertTemplateAInstance(path, createAInstance(NAME_CHILD), false);
        assertFalse(configurationRefactoringManager.canPushDown(getPath(path, "b", "y"), NAME_CHILD));
    }

    public void testCanPushDownComposite() throws TypeException
    {
        final String NAME_CHILD = "child";

        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);
        assertTrue(configurationRefactoringManager.canPushDown(getPath(parentPath, "b"), NAME_CHILD));
    }

    public void testCanPushDownCollectionItem() throws TypeException
    {
        final String NAME_CHILD = "child";

        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);
        assertTrue(configurationRefactoringManager.canPushDown(getPath(parentPath, "bmap", "colby"), NAME_CHILD));
    }

    public void testPushDownInvalidPath()
    {
        try
        {
            configurationRefactoringManager.pushDown("invalid/path", asSet("dummy"));
            fail("Should not be able to push down invalid path");
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage(), containsString("Path does not exist"));
        }
    }

    public void testPushDownNoChildren() throws TypeException
    {
        String path = insertTemplateA(rootPath, "a", true);
        try
        {
            configurationRefactoringManager.pushDown(getPath(path, "b"), Collections.<String>emptySet());
            fail("Should not be able to push down to no children");
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage(), containsString("At least one child must be specified"));
        }
    }

    public void testPushDownInvalidChild() throws TypeException
    {
        String path = insertTemplateAInstance(rootPath, createAInstance("a"), true);
        try
        {
            configurationRefactoringManager.pushDown(getPath(path, "b"), asSet("invalid"));
            fail("Should not be able to push down to an invalid child");
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage(), containsString("Specified child 'invalid' is not a direct child of this path's template owner"));
        }
    }

    public void testPushDownPathHiddenInChild() throws TypeException
    {
        // parent
        //   bmap
        //     colby <-- push down(child)
        // child
        //   bmap
        //   [colby] (hidden)
        final String NAME_CHILD = "child";

        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        String childPath = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);
        configurationTemplateManager.delete(getPath(childPath, "bmap", "colby"));
        try
        {
            configurationRefactoringManager.pushDown(getPath(parentPath, "bmap", "colby"), asSet(NAME_CHILD));
            fail("Should not be able to push down to child that hides path");
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage(), containsString("Path is hidden in specified child 'child'"));
        }
    }

    public void testPushDownComposite() throws TypeException
    {
        // parent
        //   b <-- push down(child)
        // child
        //   b
        final String NAME_CHILD = "child";

        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        String childPath = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);

        String pushPath = getPath(parentPath, "b");
        Set<String> pushedToPaths = configurationRefactoringManager.pushDown(pushPath, asSet(NAME_CHILD));

        String toPath = getPath(childPath, "b");
        assertEquals(asSet(toPath), pushedToPaths);
        assertFalse(configurationTemplateManager.pathExists(pushPath));
        assertTrue(configurationTemplateManager.pathExists(toPath));
        Record record = configurationTemplateManager.getRecord(toPath);
        assertEquals("44", record.get("y"));
    }

    public void testPushDownCollectionItem() throws TypeException
    {
        // parent
        //   bmap
        //     colby <-- push down(child)
        // child
        //   bmap
        //     colby
        final String NAME_CHILD = "child";

        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        String childPath = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);

        String pushPath = getPath(parentPath, "bmap", "colby");
        Set<String> pushedToPaths = configurationRefactoringManager.pushDown(pushPath, asSet(NAME_CHILD));

        String toPath = getPath(childPath, "bmap", "colby");
        assertEquals(asSet(toPath), pushedToPaths);
        assertFalse(configurationTemplateManager.pathExists(pushPath));
        assertTrue(configurationTemplateManager.pathExists(toPath));
        Record record = configurationTemplateManager.getRecord(toPath);
        assertEquals("1", record.get("y"));
    }

    public void testPushDownPreservesOverride() throws TypeException
    {
        // parent
        //   b <-- push down(child)
        // child
        //   b
        final String NAME_CHILD = "child";

        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        MockA child = createAInstance(NAME_CHILD);
        child.getB().setY(88);
        String childPath = insertTemplateAInstance(parentPath, child, false);

        String pushPath = getPath(parentPath, "b");
        configurationRefactoringManager.pushDown(pushPath, asSet(NAME_CHILD));

        Record record = configurationTemplateManager.getRecord(getPath(childPath, "b"));
        assertEquals("88", record.get("y"));
    }

    public void testPushDownMultipleChildren() throws TypeException
    {
        // parent
        //   b <-- push down(child1, child2)
        // child1
        //   b
        // child2
        //   b
        final String NAME_CHILD1 = "child1";
        final String NAME_CHILD2 = "child2";

        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        String child1Path = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD1), false);
        String child2Path = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD2), false);

        String pushPath = getPath(parentPath, "b");
        Set<String> pushedToPaths = configurationRefactoringManager.pushDown(pushPath, asSet(NAME_CHILD1, NAME_CHILD2));

        String toPath1 = getPath(child1Path, "b");
        String toPath2 = getPath(child2Path, "b");
        assertEquals(asSet(toPath1, toPath2), pushedToPaths);
        assertFalse(configurationTemplateManager.pathExists(pushPath));
        assertTrue(configurationTemplateManager.pathExists(toPath1));
        assertTrue(configurationTemplateManager.pathExists(toPath2));
        Record record = configurationTemplateManager.getRecord(toPath1);
        assertEquals("44", record.get("y"));
        record = configurationTemplateManager.getRecord(toPath2);
        assertEquals("44", record.get("y"));
    }

    public void testPushDownSubsetOfChildren() throws TypeException
    {
        // parent
        //   b <-- push down(child1)
        // child1
        //   b
        // child2
        //   b
        final String NAME_CHILD1 = "child1";
        final String NAME_CHILD2 = "child2";

        String parentPath = insertTemplateAInstance(rootPath, createAInstance("parent"), true);
        String child1Path = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD1), false);
        String child2Path = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD2), false);
        Listener listener = registerListener();

        String pushPath = getPath(parentPath, "b");
        Set<String> pushedToPaths = configurationRefactoringManager.pushDown(pushPath, asSet(NAME_CHILD1));

        String toPath = getPath(child1Path, "b");
        String deletedPath = getPath(child2Path, "b");
        assertEquals(asSet(toPath), pushedToPaths);
        assertFalse(configurationTemplateManager.pathExists(pushPath));
        assertTrue(configurationTemplateManager.pathExists(toPath));
        assertFalse(configurationTemplateManager.pathExists(deletedPath));
        Record record = configurationTemplateManager.getRecord(toPath);
        assertEquals("44", record.get("y"));
        
        listener.assertEvents(new DeleteEventSpec(deletedPath, false), new PostDeleteEventSpec(deletedPath, false));
    }

    public void testPushDownWalksNestedPaths() throws TypeException
    {
        // parent
        //   b <-- pushed down
        //     cmap
        //       colc
        // child
        //   b
        //     cmap
        //       colc
        final String NAME_CHILD = "child";
        final String NAME_C = "colc";

        MockA parentA = createAInstance("parent");
        parentA.getB().getCmap().put(NAME_C, new MockC(NAME_C));
        String parentPath = insertTemplateAInstance(rootPath, parentA, true);
        String childPath = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);

        String pushPath = getPath(parentPath, "b");
        configurationRefactoringManager.pushDown(pushPath, asSet(NAME_CHILD));

        Record record = configurationTemplateManager.getRecord(getPath(childPath, "b", "cmap", "colc"));
        assertEquals(MockC.DEFAULT_VALUE, record.get("value"));
    }

    public void testPushDownHiddenPathNestedInChild() throws TypeException
    {
        // parent
        //   b <-- pushed down
        //     cmap
        //       colc
        // child
        //   b
        //     cmap
        //       [colc] (hidden)
        final String NAME_CHILD = "child";
        final String NAME_C = "colc";

        MockA parentA = createAInstance("parent");
        parentA.getB().getCmap().put(NAME_C, new MockC(NAME_C));
        String parentPath = insertTemplateAInstance(rootPath, parentA, true);
        String childPath = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);
        String hiddenPath = getPath(childPath, "b", "cmap", "colc");
        configurationTemplateManager.delete(hiddenPath);

        String pushPath = getPath(parentPath, "b");
        configurationRefactoringManager.pushDown(pushPath, asSet(NAME_CHILD));

        Record record = configurationTemplateManager.getRecord(getPath(childPath, "b"));
        assertEquals("44", record.get("y"));
        assertFalse(configurationTemplateManager.pathExists(hiddenPath));
    }

    public void testPushDownReferenceWithin() throws TypeException
    {
        // parent
        //   b <-- pushed down
        //     ref: ee
        //     refToRef: refers to ee
        // child
        //   b
        final String NAME_CHILD = "child";

        MockA parentA = createAInstance("parent");
        parentA.getB().setRef(new Referee("ee"));
        String parentPath = insertTemplateAInstance(rootPath, parentA, true);
        String childPath = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);
        parentA = configurationTemplateManager.getInstance(parentPath, MockA.class);
        parentA.getB().setRefToRef(parentA.getB().getRef());
        configurationTemplateManager.save(parentA);

        String pushPath = getPath(parentPath, "b");
        configurationRefactoringManager.pushDown(pushPath, asSet(NAME_CHILD));

        MockA childA = configurationTemplateManager.getInstance(childPath, MockA.class);
        assertNotNull(childA.getB().getRef());
        assertSame(childA.getB().getRef(), childA.getB().getRefToRef());
    }

    public void testPushDownInternalReferenceToPushedDown() throws TypeException
    {
        // parent
        //   refToRef: refers to ee
        //   b <-- pushed down
        //     ref: ee
        // child
        //   b
        final String NAME_CHILD = "child";

        MockA parentA = createAInstance("parent");
        parentA.getB().setRef(new Referee("ee"));
        String parentPath = insertTemplateAInstance(rootPath, parentA, true);
        insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);
        parentA = configurationTemplateManager.getInstance(parentPath, MockA.class);
        parentA.setRefToRef(parentA.getB().getRef());
        configurationTemplateManager.save(parentA);

        try
        {
            configurationRefactoringManager.pushDown(getPath(parentPath, "b"), asSet(NAME_CHILD));
            fail("Should not be able to push down when there are internal references to the item to push down");
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage(), containsString("Reference from 'template/parent' to within item to pull down that would become invalid"));
        }
    }

    public void testPushDownInternalReferenceFromPushedDown() throws TypeException
    {
        // parent
        //   ref: ee
        //   b <-- pushed down
        //     refToRef: refers to ee
        // child
        //   b
        final String NAME_CHILD = "child";

        MockA parentA = createAInstance("parent");
        parentA.setRef(new Referee("ee"));
        String parentPath = insertTemplateAInstance(rootPath, parentA, true);
        String childPath = insertTemplateAInstance(parentPath, new MockA(NAME_CHILD), false);
        parentA = configurationTemplateManager.getInstance(parentPath, MockA.class);
        parentA.getB().setRefToRef(parentA.getRef());
        configurationTemplateManager.save(parentA);

        configurationRefactoringManager.pushDown(getPath(parentPath, "b"), asSet(NAME_CHILD));

        MockA childA = configurationTemplateManager.getInstance(childPath, MockA.class);
        assertNotNull(childA.getRef());
        assertSame(childA.getRef(), childA.getB().getRefToRef());
    }

    private MockA createAInstance(String name)
    {
        MockA instance = new MockA(name);
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

    private void assertClone(MockA clone, String name)
    {
        assertAInstance(clone, "clone of " + name);
    }

    private void assertAInstance(MockA instance, String name)
    {
        assertEquals(name, instance.getName());
        assertEquals(10, instance.getX());
        MockB cloneB = instance.getB();
        assertEquals("b", cloneB.getName());
        assertEquals(44, cloneB.getY());
        assertEquals(1, instance.getBlist().size());
        assertEquals("lisby", instance.getBlist().get(0).getName());
        assertEquals(1, instance.getBmap().size());
        MockB cloneColby = instance.getBmap().get("colby");
        assertEquals("colby", cloneColby.getName());
        assertEquals(1, cloneColby.getY());
        Referee cloneRef = instance.getRef();
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
        catch(ToveRuntimeException e)
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
        MutableRecord record = unstantiate(instance);
        if(template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }
        configurationTemplateManager.setParentTemplate(record, configurationTemplateManager.getRecord(templateParentPath).getHandle());
        return configurationTemplateManager.insertRecord(TEMPLATE_SCOPE, record);
    }

    private String addB(String aPath, String name)
    {
        return configurationTemplateManager.insert(getPath(aPath, "bmap"), new MockB(name));
    }

    private HashSet<String> asSet(String... items)
    {
        return new HashSet<String>(asList(items));
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
        @Reference
        private List<Referee> listOfRefs = new LinkedList<Referee>();

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

        public List<Referee> getListOfRefs()
        {
            return listOfRefs;
        }

        public void setListOfRefs(List<Referee> listOfRefs)
        {
            this.listOfRefs = listOfRefs;
        }
    }

    @SymbolicName("b")
    public static class MockB extends AbstractNamedConfiguration
    {
        private int y;
        private Referee ref;
        @Reference
        private Referee refToRef;
        private Map<String, MockC> cmap = new HashMap<String, MockC>();

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

        public Map<String, MockC> getCmap()
        {
            return cmap;
        }

        public void setCmap(Map<String, MockC> cmap)
        {
            this.cmap = cmap;
        }
    }
    
    @SymbolicName("c")
    public static class MockC extends AbstractNamedConfiguration
    {
        public static final String DEFAULT_VALUE = "default";

        private String value = DEFAULT_VALUE;

        public MockC()
        {
        }

        public MockC(String name)
        {
            super(name);
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
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
