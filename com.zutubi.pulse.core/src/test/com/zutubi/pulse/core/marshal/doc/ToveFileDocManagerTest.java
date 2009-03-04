package com.zutubi.pulse.core.marshal.doc;

import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.pulse.core.marshal.types.ExtensionOneConfiguration;
import com.zutubi.pulse.core.marshal.types.ExtensionTwoConfiguration;
import com.zutubi.pulse.core.marshal.types.RootConfiguration;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;

public class ToveFileDocManagerTest extends AbstractConfigurationSystemTestCase
{
    private static final String ELEMENT_ROOT = "root";
    private static final String ELEMENT_MIXED = "mixed";
    private static final String ELEMENT_REQUIRED = "required";
    private static final String ELEMENT_EXTENSION_ONE = "e1";
    private static final String ELEMENT_EXTENSION_TWO = "e2";

    private ElementDocs rootDocs;
    private ElementDocs mixedDocs;
    private ElementDocs requiredDocs;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ConfigurationDocsManager configurationDocsManager = new ConfigurationDocsManager();
        ToveFileDocManager toveFileDocManager = new ToveFileDocManager();
        toveFileDocManager.setConfigurationDocsManager(configurationDocsManager);

        typeRegistry.register(RootConfiguration.class);

        TypeDefinitions typeDefinitions = new TypeDefinitions();
        typeDefinitions.register(ELEMENT_EXTENSION_ONE, typeRegistry.register(ExtensionOneConfiguration.class));
        typeDefinitions.register(ELEMENT_EXTENSION_TWO, typeRegistry.register(ExtensionTwoConfiguration.class));

        toveFileDocManager.registerRoot(ELEMENT_ROOT, typeRegistry.getType(RootConfiguration.class), typeDefinitions);

        rootDocs = toveFileDocManager.lookupRoot(ELEMENT_ROOT);
        mixedDocs = rootDocs.getChild(ELEMENT_MIXED).getElementDocs();
        requiredDocs = rootDocs.getChild(ELEMENT_REQUIRED).getElementDocs();
    }

    public void testBasicDetailsMixedConfig()
    {
        assertEquals("root intro", rootDocs.getBrief());
        assertEquals("root verbose", rootDocs.getVerbose());

        assertChild(rootDocs, ELEMENT_MIXED, "mixed intro", "mixed verbose", Arity.ZERO_OR_MORE);
    }

    public void testIntProperty()
    {
        assertAttribute(mixedDocs, "intProperty", "intProperty verbose", false, "0");
    }

    public void testStringProperty()
    {
        assertAttribute(mixedDocs, "stringProperty", "stringProperty verbose", false, "");
    }

    public void testEnumProperty()
    {
        assertAttribute(mixedDocs, "enumProperty", "enumProperty verbose", false, "");
    }

    public void testReferenceProperty()
    {
        assertAttribute(mixedDocs, "referenceProperty", "referenceProperty verbose", false, "");
    }

    public void testStringList()
    {
        assertAttribute(mixedDocs, "stringList", "stringList verbose", false, "");
    }

    public void testEnumList()
    {
        assertAttribute(mixedDocs, "enumList", "enumList verbose", false, "");
    }

    public void testRequiredString()
    {
        assertAttribute(requiredDocs, "requiredString", "requiredString verbose", true, "");
    }

    public void testRequiredReference()
    {
        assertAttribute(requiredDocs, "requiredReference", "requiredReference verbose", true, "");
    }

    public void testComposite()
    {
        assertChild(mixedDocs, "compositeProperty", "trivial intro", "trivial verbose", Arity.ZERO_OR_ONE);
    }

    public void testAddableStringList()
    {
        ChildElementDocs child = assertChild(mixedDocs, "addableStringListItem", "addableStringList addable brief", "addableStringList addable verbose", Arity.ZERO_OR_MORE);
        assertAttribute(child.getElementDocs(), "str", "addableStringList addable attribute", true, "");
    }

    public void testContentStringList()
    {
        ChildElementDocs child = assertChild(mixedDocs, "contentStringListItem", "contentStringList addable brief", "contentStringList addable verbose", Arity.ZERO_OR_MORE);
        assertContent(child.getElementDocs(), "contentStringList addable content");
    }

    public void testReferenceList()
    {
        ChildElementDocs child = assertChild(mixedDocs, "referenceListItem", "referenceList addable brief", "referenceList addable verbose", Arity.ZERO_OR_MORE);
        assertAttribute(child.getElementDocs(), "ref", "referenceList addable attribute", true, "");
    }

    public void testContentReferenceList()
    {
        ChildElementDocs child = assertChild(mixedDocs, "contentReferenceListItem", "contentReferenceList addable brief", "contentReferenceList addable verbose", Arity.ZERO_OR_MORE);
        assertContent(child.getElementDocs(), "contentReferenceList addable content");
    }

    public void testCompositeMap()
    {
        assertChild(mixedDocs, "compositeMapItem", "trivial intro", "trivial verbose", Arity.ZERO_OR_MORE);
    }

    public void testExtendableMap()
    {
        assertChild(mixedDocs, "e1", "extension one intro", "extension one verbose", Arity.ZERO_OR_MORE);
        assertChild(mixedDocs, "e2", "extension two intro", "extension two verbose", Arity.ZERO_OR_MORE);
    }

    public void testRequiredComposite()
    {
        assertChild(requiredDocs, "requiredComposite", "trivial intro", "trivial verbose", Arity.EXACTLY_ONE);
    }

    private ChildElementDocs assertChild(ElementDocs docs, String name, String brief, String verbose, Arity arity)
    {
        ChildElementDocs child = docs.getChild(name);
        assertNotNull(child);
        assertEquals(name, child.getName());
        assertEquals(brief, child.getElementDocs().getBrief());
        assertEquals(verbose, child.getElementDocs().getVerbose());
        assertEquals(arity, child.getArity());
        return child;
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
