package com.zutubi.pulse.core.marshal.doc;

import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.pulse.core.marshal.types.*;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.type.TypeException;
import com.zutubi.util.CollectionUtils;

public class ToveFileDocManagerTest extends AbstractConfigurationSystemTestCase
{
    private static final String ELEMENT_ROOT = "root";
    private static final String ELEMENT_MIXED = "mixed";
    private static final String ELEMENT_REQUIRED = "required";
    private static final String ELEMENT_EXTENSION_ONE = "e1";
    private static final String ELEMENT_EXTENSION_TWO = "e2";
    private static final String ELEMENT_TOP_LEVEL = "top-level";

    private ElementDocs rootDocs;
    private ElementDocs mixedDocs;
    private ElementDocs requiredDocs;
    private ToveFileDocManager toveFileDocManager;
    private TypeDefinitions typeDefinitions;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        PulseFileLoaderFactory fileLoaderFactory = new PulseFileLoaderFactory();
        fileLoaderFactory.setObjectFactory(objectFactory);
        fileLoaderFactory.setTypeRegistry(typeRegistry);

        ConfigurationDocsManager configurationDocsManager = new ConfigurationDocsManager();
        toveFileDocManager = new ToveFileDocManager();
        toveFileDocManager.setConfigurationDocsManager(configurationDocsManager);
        toveFileDocManager.setObjectFactory(objectFactory);
        toveFileDocManager.setFileLoaderFactory(fileLoaderFactory);

        typeRegistry.register(RootConfiguration.class);
        fileLoaderFactory.register("root", RootConfiguration.class);

        typeDefinitions = new TypeDefinitions();
        typeDefinitions.register(ELEMENT_EXTENSION_ONE, typeRegistry.register(ExtensionOneConfiguration.class));
        typeDefinitions.register(ELEMENT_EXTENSION_TWO, typeRegistry.register(ExtensionTwoConfiguration.class));
        typeDefinitions.register(ELEMENT_TOP_LEVEL, typeRegistry.register(TopLevelConfiguration.class));

        toveFileDocManager.registerRoot(ELEMENT_ROOT, typeRegistry.getType(RootConfiguration.class), typeDefinitions);

        rootDocs = toveFileDocManager.lookupRoot(ELEMENT_ROOT);
        mixedDocs = (ElementDocs) rootDocs.getNode(ELEMENT_MIXED);
        requiredDocs = (ElementDocs) rootDocs.getNode(ELEMENT_REQUIRED);
    }

    public void testBasicDetailsMixedConfig()
    {
        assertEquals("root intro", rootDocs.getBrief());
        assertEquals("root verbose", rootDocs.getVerbose());

        assertChild(rootDocs, ELEMENT_MIXED, "mixed intro", "mixed verbose", Arity.ZERO_OR_MORE);
    }

    public void testIntProperty()
    {
        assertAttribute(mixedDocs, "int-property", "intProperty verbose", false, "0");
    }

    public void testStringProperty()
    {
        assertAttribute(mixedDocs, "string-property", "stringProperty verbose", false, "");
    }

    public void testEnumProperty()
    {
        assertAttribute(mixedDocs, "enum-property", "enumProperty verbose", false, "");
    }

    public void testReferenceProperty()
    {
        assertAttribute(mixedDocs, "reference-property", "referenceProperty verbose", false, "");
    }

    public void testStringList()
    {
        assertAttribute(mixedDocs, "string-list", "stringList verbose", false, "");
    }

    public void testEnumList()
    {
        assertAttribute(mixedDocs, "enum-list", "enumList verbose", false, "");
    }

    public void testRequiredString()
    {
        assertAttribute(requiredDocs, "required-string", "requiredString verbose", true, "");
    }

    public void testRequiredReference()
    {
        assertAttribute(requiredDocs, "required-reference", "requiredReference verbose", true, "");
    }

    public void testComposite()
    {
        assertChild(mixedDocs, "composite-property", "trivial intro", "trivial verbose", Arity.ZERO_OR_ONE);
    }

    public void testAddableStringList()
    {
        ChildNodeDocs child = assertChild(mixedDocs, "addableStringListItem", "addableStringList addable brief", "addableStringList addable verbose", Arity.ZERO_OR_MORE);
        assertAttribute((ElementDocs) child.getNodeDocs(), "str", "addableStringList addable attribute", true, "");
    }

    public void testContentStringList()
    {
        ChildNodeDocs child = assertChild(mixedDocs, "contentStringListItem", "contentStringList addable brief", "contentStringList addable verbose", Arity.ZERO_OR_MORE);
        assertContent((ElementDocs) child.getNodeDocs(), "contentStringList addable content");
    }

    public void testReferenceList()
    {
        ChildNodeDocs child = assertChild(mixedDocs, "referenceListItem", "referenceList addable brief", "referenceList addable verbose", Arity.ZERO_OR_MORE);
        assertAttribute((ElementDocs) child.getNodeDocs(), "ref", "referenceList addable attribute", true, "");
    }

    public void testContentReferenceList()
    {
        ChildNodeDocs child = assertChild(mixedDocs, "contentReferenceListItem", "contentReferenceList addable brief", "contentReferenceList addable verbose", Arity.ZERO_OR_MORE);
        assertContent((ElementDocs) child.getNodeDocs(), "contentReferenceList addable content");
    }

    public void testCompositeMap()
    {
        assertChild(mixedDocs, "compositeMapItem", "trivial intro", "trivial verbose", Arity.ZERO_OR_MORE);
    }

    public void testExtendableMap()
    {
        ExtensibleDocs extensibleDocs = getExtendableMapDocs();
        assertExtension(extensibleDocs, "e1", "extension one intro", "extension one verbose");
        assertExtension(extensibleDocs, "e2", "extension two intro", "extension two verbose");
    }

    public void testRegisterType() throws TypeException
    {
        toveFileDocManager.registerType("newe", typeRegistry.register(NewExtensionConfiguration.class), typeDefinitions);

        ExtensibleDocs extensibleDocs = getExtendableMapDocs();
        assertExtension(extensibleDocs, "newe", "new extension intro", "new extension verbose");
    }

    public void testUnregisterType() throws TypeException
    {
        ExtensibleDocs extensibleDocs = getExtendableMapDocs();
        assertTrue(extensibleDocs.hasExtension("e1"));
        toveFileDocManager.unregisterType("e1", typeRegistry.getType(ExtensionOneConfiguration.class));
        assertFalse(extensibleDocs.hasExtension("e1"));
    }

    public void testTopLevelMap()
    {
        assertChild(mixedDocs, "top-level", "top level intro", "top level verbose", Arity.ZERO_OR_MORE);
    }

    private ExtensibleDocs getExtendableMapDocs()
    {
        ChildNodeDocs child = assertChild(mixedDocs, "extendable-map", "extendable intro", "extendable verbose", Arity.ZERO_OR_MORE);
        assertTrue(child.getNodeDocs() instanceof ExtensibleDocs);
        return (ExtensibleDocs) child.getNodeDocs();
    }

    public void testRequiredComposite()
    {
        assertChild(requiredDocs, "required-composite", "trivial intro", "trivial verbose", Arity.EXACTLY_ONE);
    }

    public void testGetRoots()
    {
        assertEquals(CollectionUtils.asSet(ELEMENT_ROOT), toveFileDocManager.getRoots());
    }

    public void testBuiltins()
    {
        ChildNodeDocs child = rootDocs.getChild("macro");
        assertNotNull(child);
        assertTrue(child.getNodeDocs() instanceof BuiltinElementDocs);
    }

    public void testExamples()
    {
        assertEquals(1, mixedDocs.getExamples().size());
        ExampleDocs exampleDocs = mixedDocs.getExamples().get(0);
        assertEquals("simple", exampleDocs.getName());
        assertEquals("simple blurb", exampleDocs.getBlurb());
        assertEquals("<mixed name=\"simple-example\">\r\n" +
                "    <compositeMapItem name=\"foo\"/>\r\n" +
                "</mixed>", exampleDocs.getXmlSnippet());
    }

    private ChildNodeDocs assertChild(ElementDocs docs, String name, String brief, String verbose, Arity arity)
    {
        ChildNodeDocs child = docs.getChild(name);
        assertNotNull(child);
        assertEquals(name, child.getName());
        assertEquals(brief, child.getNodeDocs().getBrief());
        assertEquals(verbose, child.getNodeDocs().getVerbose());
        assertEquals(arity, child.getArity());
        return child;
    }

    private void assertExtension(ExtensibleDocs extensibleDocs, String name, String brief, String verbose)
    {
        ElementDocs element = extensibleDocs.getExtension(name);
        assertNotNull(element);
        assertEquals(brief, element.getBrief());
        assertEquals(verbose, element.getVerbose());
    }

    private AttributeDocs assertAttribute(ElementDocs docs, String name, String description, boolean required, String defaultValue)
    {
        AttributeDocs attribute = docs.getAttribute(name);
        assertNotNull(attribute);
        assertEquals(name, attribute.getName());
        assertEquals(description, attribute.getDescription());
        assertEquals(required, attribute.isRequired());
        assertEquals(defaultValue, attribute.getDefaultValue());
        return attribute;
    }

    private ContentDocs assertContent(ElementDocs docs, String verbose)
    {
        ContentDocs contentDocs = docs.getContentDocs();
        assertNotNull(contentDocs);
        assertEquals(verbose, contentDocs.getVerbose());
        return contentDocs;
    }
}
