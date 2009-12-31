package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.marshal.types.*;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.ReferenceType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeProperty;

import static com.zutubi.pulse.core.marshal.ToveFileUtils.convertPropertyNameToLocalName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ToveFileUtilsTest extends AbstractConfigurationSystemTestCase
{
    private CompositeType mixedType;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        typeRegistry.register(RootConfiguration.class);
        mixedType = typeRegistry.getType(MixedConfiguration.class);
    }

    public void testGetReferenceNameProperty()
    {
        TypeProperty nameProperty = ToveFileUtils.getReferenceNameProperty(mixedType, mixedType.getProperty("referenceProperty"), typeRegistry.getType(TrivialConfiguration.class));
        assertNotNull(nameProperty);
        assertEquals("name", nameProperty.getName());
    }

    public void testGetReferenceNamePropertyNotReferenceable() throws TypeException
    {
        CompositeType type = typeRegistry.register(BadReferencesConfiguration.class);
        try
        {
            TypeProperty referencingProperty = type.getProperty("unreferenceable");
            ReferenceType referenceType = (ReferenceType) referencingProperty.getType();
            ToveFileUtils.getReferenceNameProperty(type, referencingProperty, referenceType.getReferencedType());
            
            fail("Can't get reference name property when not annotated with @Referenceable");
        }
        catch (PulseRuntimeException e)
        {
            assertThat(e.getMessage(), containsString("the referenced type 'com.zutubi.pulse.core.marshal.types.UnreferenceableConfiguration' is not @Referenceable"));
        }
    }

    public void testGetReferenceNamePropertyBadName() throws TypeException
    {
        CompositeType type = typeRegistry.register(BadReferencesConfiguration.class);
        try
        {
            TypeProperty referencingProperty = type.getProperty("badReferenceName");
            ReferenceType referenceType = (ReferenceType) referencingProperty.getType();
            ToveFileUtils.getReferenceNameProperty(type, referencingProperty, referenceType.getReferencedType());

            fail("Can't get reference name property the @Referenceable annotation is broken");
        }
        catch (PulseRuntimeException e)
        {
            assertThat(e.getMessage(), containsString("the referenced type 'com.zutubi.pulse.core.marshal.types.BadReferenceNameConfiguration' has an invalid nameProperty 'nosuchproperty' specified in its @Referenceable annotation"));
        }
    }

    public void testToReference()
    {
        assertEquals("$(foo)", ToveFileUtils.toReference("foo"));
    }

    public void testToReferenceSpecialCharacter()
    {
        assertEquals("${fo|o}", ToveFileUtils.toReference("fo|o"));
    }

    public void testGetPropertyValueNull()
    {
        assertNull(ToveFileUtils.getPropertyValue(new MixedConfiguration(), mixedType.getProperty("stringProperty")));
    }

    public void testGetPropertyValue()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.setStringProperty("foo");
        assertEquals("foo", ToveFileUtils.getPropertyValue(configuration, mixedType.getProperty("stringProperty")));
    }

    public void testConvertsToAttributePrimitive()
    {
        assertTrue(ToveFileUtils.convertsToAttribute(mixedType.getProperty("intProperty")));
    }

    public void testConvertsToAttributeEnum()
    {
        assertTrue(ToveFileUtils.convertsToAttribute(mixedType.getProperty("enumProperty")));
    }

    public void testConvertsToAttributeReference()
    {
        assertTrue(ToveFileUtils.convertsToAttribute(mixedType.getProperty("referenceProperty")));
    }

    public void testConvertsToAttributeComposite()
    {
        assertFalse(ToveFileUtils.convertsToAttribute(mixedType.getProperty("compositeProperty")));
    }

    public void testConvertsToAttributeStringList()
    {
        assertTrue(ToveFileUtils.convertsToAttribute(mixedType.getProperty("stringList")));
    }

    public void testConvertsToAttributeAddableStringList()
    {
        assertFalse(ToveFileUtils.convertsToAttribute(mixedType.getProperty("addableStringList")));
    }

    public void testConvertsToAttributeContentStringList()
    {
        assertFalse(ToveFileUtils.convertsToAttribute(mixedType.getProperty("contentStringList")));
    }

    public void testConvertsToAttributeEnumList()
    {
        assertTrue(ToveFileUtils.convertsToAttribute(mixedType.getProperty("enumList")));
    }

    public void testConvertsToAttributeAddableReferenceList()
    {
        assertFalse(ToveFileUtils.convertsToAttribute(mixedType.getProperty("referenceList")));
    }

    public void testConvertsToAttributeContentReferenceList()
    {
        assertFalse(ToveFileUtils.convertsToAttribute(mixedType.getProperty("contentReferenceList")));
    }
    
    public void testConvertsToAttributeCompositeMap()
    {
        assertFalse(ToveFileUtils.convertsToAttribute(mixedType.getProperty("compositeMap")));
    }

    public void testConvertsToAttributeExtendableMap()
    {
        assertFalse(ToveFileUtils.convertsToAttribute(mixedType.getProperty("extendableMap")));
    }

    public void testConvertAttributePrimitive()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.setIntProperty(22);
        assertEquals("22", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("intProperty")));
    }

    public void testConvertAttributeString()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.setStringProperty("str");
        assertEquals("str", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("stringProperty")));
    }

    public void testConvertAttributeStringNull()
    {
        assertEquals("", ToveFileUtils.convertAttribute(new MixedConfiguration(), mixedType, mixedType.getProperty("stringProperty")));
    }

    public void testConvertAttributeEnum()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.setEnumProperty(TestEnum.C1);
        assertEquals("c1", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("enumProperty")));
    }

    public void testConvertAttributeEnumNull()
    {
        assertEquals("", ToveFileUtils.convertAttribute(new MixedConfiguration(), mixedType, mixedType.getProperty("enumProperty")));
    }

    public void testConvertAttributeReference()
    {
        TrivialConfiguration ref = new TrivialConfiguration();
        ref.setName("foo");
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.setReferenceProperty(ref);
        assertEquals("$(foo)", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("referenceProperty")));
    }

    public void testConvertAttributeReferenceNull()
    {
        assertEquals("", ToveFileUtils.convertAttribute(new MixedConfiguration(), mixedType, mixedType.getProperty("referenceProperty")));
    }

    public void testConvertAttributeStringListEmpty()
    {
        assertEquals("", ToveFileUtils.convertAttribute(new MixedConfiguration(), mixedType, mixedType.getProperty("stringList")));
    }

    public void testConvertAttributeStringListNull()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.setStringList(null);
        assertEquals("", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("stringList")));
    }

    public void testConvertAttributeStringList()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.getStringList().add("s1");
        configuration.getStringList().add("s2");
        configuration.getStringList().add("s3");
        assertEquals("s1 s2 s3", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("stringList")));
    }

    public void testConvertAttributeStringListEmptyItem()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.getStringList().add("s1");
        configuration.getStringList().add("");
        configuration.getStringList().add("s3");
        assertEquals("s1 \"\" s3", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("stringList")));
    }

    public void testConvertAttributeStringListItemWithSpaces()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.getStringList().add("s1");
        configuration.getStringList().add("s 2");
        configuration.getStringList().add("s3");
        assertEquals("s1 \"s 2\" s3", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("stringList")));
    }

    public void testConvertAttributeEnumListEmpty()
    {
        assertEquals("", ToveFileUtils.convertAttribute(new MixedConfiguration(), mixedType, mixedType.getProperty("enumList")));
    }

    public void testConvertAttributeEnumListNull()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.setEnumList(null);
        assertEquals("", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("enumList")));
    }

    public void testConvertAttributeEnumList()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.getEnumList().add(TestEnum.C1);
        configuration.getEnumList().add(TestEnum.C3);
        assertEquals("c1 c3", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("enumList")));
    }

    public void testConvertAttributeEnumListItemWithSpace()
    {
        MixedConfiguration configuration = new MixedConfiguration();
        configuration.getEnumList().add(TestEnum.C1);
        configuration.getEnumList().add(TestEnum.C_2);
        configuration.getEnumList().add(TestEnum.C3);
        assertEquals("c1 \"c 2\" c3", ToveFileUtils.convertAttribute(configuration, mixedType, mixedType.getProperty("enumList")));
    }

    public void testConvertPropertyNameEmpty()
    {
        assertEquals("", convertPropertyNameToLocalName(""));
    }

    public void testConvertPropertyAllLower()
    {
        assertEquals("nochange", convertPropertyNameToLocalName("nochange"));
    }

    public void testConvertPropertyStartsWithUpper()
    {
        assertEquals("capped", convertPropertyNameToLocalName("Capped"));
    }

    public void testConvertPropertyStartsWithMultipleUpper()
    {
        assertEquals("shou-tout", convertPropertyNameToLocalName("SHOUTout"));
    }

    public void testConvertPropertyEndsWithUpper()
    {
        assertEquals("cappe-d", convertPropertyNameToLocalName("cappeD"));
    }

    public void testConvertPropertyEndsWithMultipleUpper()
    {
        assertEquals("shout-out", convertPropertyNameToLocalName("shoutOUT"));
    }

    public void testConvertPropertyMiddleMultipleUpper()
    {
        assertEquals("sho-ut-out", convertPropertyNameToLocalName("shoUTOut"));
    }
    
    public void testConvertPropertyAllUpper()
    {
        assertEquals("shout", convertPropertyNameToLocalName("SHOUT"));
    }

    public void testConvertPropertyCamel()
    {
        assertEquals("camel-case-words", convertPropertyNameToLocalName("camelCaseWords"));
    }
}
