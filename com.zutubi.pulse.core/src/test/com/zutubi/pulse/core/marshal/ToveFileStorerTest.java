package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.marshal.types.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.variables.GenericVariable;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.IOAssertions;
import com.zutubi.util.bean.DefaultObjectFactory;
import nu.xom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class ToveFileStorerTest extends PulseTestCase
{
    private ToveFileLoader loader;
    private ToveFileStorer storer;
    private Scope scope;
    private TrivialConfiguration referenceable1;
    private TrivialConfiguration referenceable2;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        TypeRegistry typeRegistry = new TypeRegistry();
        typeRegistry.register(RootConfiguration.class);

        TypeDefinitions typeDefinitions = new TypeDefinitions();
        typeDefinitions.register("mixed", typeRegistry.getType(MixedConfiguration.class));
        typeDefinitions.register("defaults", typeRegistry.register(DefaultValuesConfiguration.class));
        typeDefinitions.register("e1", typeRegistry.register(ExtensionOneConfiguration.class));
        typeDefinitions.register("e2", typeRegistry.register(ExtensionTwoConfiguration.class));
        typeDefinitions.register("frecipe", typeRegistry.register(FakeRecipe.class));
        typeDefinitions.register("fant", typeRegistry.register(FakeAntCommand.class));
        typeDefinitions.register("fmake", typeRegistry.register(FakeMakeCommand.class));

        loader = new ToveFileLoader();
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.setTypeDefinitions(typeDefinitions);
        loader.setTypeRegistry(typeRegistry);
        loader.setValidationManager(new PulseValidationManager());

        storer = new ToveFileStorer();
        storer.setTypeDefinitions(typeDefinitions);
        storer.setTypeRegistry(typeRegistry);

        referenceable1 = new TrivialConfiguration();
        referenceable1.setName("t1");
        referenceable2 = new TrivialConfiguration();
        referenceable2.setName("t2");

        scope = new PulseScope();
        scope.add(new GenericVariable<TrivialConfiguration>(referenceable1.getName(), referenceable1));
        scope.add(new GenericVariable<TrivialConfiguration>(referenceable2.getName(), referenceable2));
    }

    public void testStoreInt() throws IOException, PulseException
    {
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setIntProperty(42);

        mixed = roundTrip(mixed);
        assertEquals(42, mixed.getIntProperty());
    }

    public void testStoreString() throws IOException, PulseException
    {
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setStringProperty("shazam!");

        mixed = roundTrip(mixed);
        assertEquals("shazam!", mixed.getStringProperty());
    }

    public void testStoreEnum() throws IOException, PulseException
    {
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setEnumProperty(TestEnum.C_2);

        mixed = roundTrip(mixed);
        assertEquals(TestEnum.C_2, mixed.getEnumProperty());
    }

    public void testStoreReference() throws IOException, PulseException
    {
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setReferenceProperty(referenceable1);

        mixed = roundTrip(mixed);
        assertSame(referenceable1, mixed.getReferenceProperty());
    }

    public void testStoreComposite() throws IOException, PulseException
    {
        MixedConfiguration mixed = new MixedConfiguration();
        TrivialConfiguration compositeIn = new TrivialConfiguration("foo");
        mixed.setCompositeProperty(compositeIn);

        mixed = roundTrip(mixed);
        TrivialConfiguration compositeOut = mixed.getCompositeProperty();
        assertNotNull(compositeOut);
        assertNotSame(compositeIn, compositeOut);
        assertEquals(compositeIn.getName(), compositeOut.getName());
    }

    public void testStoreStringList() throws IOException, PulseException
    {
        List<String> stringList = asList("foo", "bar", "baz");
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setStringList(stringList);

        mixed = roundTrip(mixed);
        assertEquals(stringList, mixed.getStringList());
    }

    public void testStoreAddableStringList() throws IOException, PulseException
    {
        List<String> stringList = asList("foo", "bar", "baz");
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setAddableStringList(stringList);

        mixed = roundTrip(mixed);
        assertEquals(stringList, mixed.getAddableStringList());
    }

    public void testStoreContentStringList() throws IOException, PulseException
    {
        List<String> stringList = asList("foo", "bar", "baz");
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setContentStringList(stringList);

        mixed = roundTrip(mixed);
        assertEquals(stringList, mixed.getContentStringList());
    }

    public void testStoreEnumList() throws IOException, PulseException
    {
        List<TestEnum> enumList = asList(TestEnum.C_2, TestEnum.C3, TestEnum.C1);
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setEnumList(enumList);

        mixed = roundTrip(mixed);
        assertEquals(enumList, mixed.getEnumList());
    }

    public void testStoreReferenceList() throws IOException, PulseException
    {
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setReferenceList(asList(referenceable2, referenceable1));

        mixed = roundTrip(mixed);
        assertEquals(2, mixed.getReferenceList().size());
        assertSame(referenceable2, mixed.getReferenceList().get(0));
        assertSame(referenceable1, mixed.getReferenceList().get(1));
    }

    public void testStoreContentReferenceList() throws IOException, PulseException
    {
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.setContentReferenceList(asList(referenceable2, referenceable1));

        mixed = roundTrip(mixed);
        assertEquals(2, mixed.getContentReferenceList().size());
        assertSame(referenceable2, mixed.getContentReferenceList().get(0));
        assertSame(referenceable1, mixed.getContentReferenceList().get(1));
    }

    public void testStoreCompositeMap() throws IOException, PulseException
    {
        TrivialConfiguration t1 = new TrivialConfiguration("foo");
        TrivialConfiguration t2 = new TrivialConfiguration("bar");
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.getCompositeMap().put(t1.getName(), t1);
        mixed.getCompositeMap().put(t2.getName(), t2);

        mixed = roundTrip(mixed);
        Map<String,TrivialConfiguration> map = mixed.getCompositeMap();
        assertEquals(2, map.size());
        assertEquals("foo", map.get("foo").getName());
        assertEquals("bar", map.get("bar").getName());
    }

    public void testStoreExtendableMap() throws IOException, PulseException
    {
        ExtensionOneConfiguration e1 = new ExtensionOneConfiguration("foo");
        ExtensionTwoConfiguration e2 = new ExtensionTwoConfiguration("bar");
        MixedConfiguration mixed = new MixedConfiguration();
        mixed.getExtendableMap().put(e1.getName(), e1);
        mixed.getExtendableMap().put(e2.getName(), e2);

        mixed = roundTrip(mixed);
        Map<String, ExtendableConfiguration> map = mixed.getExtendableMap();
        assertEquals(2, map.size());
        ExtendableConfiguration e1Out = map.get("foo");
        assertTrue(e1Out instanceof ExtensionOneConfiguration);
        ExtendableConfiguration e2Out = map.get("bar");
        assertTrue(e2Out instanceof ExtensionTwoConfiguration);
    }

    public void testFakeRecipeFile() throws IOException
    {
        FakeRecipe recipe = new FakeRecipe("default");
        FakeAntCommand ant = new FakeAntCommand("eatit");
        ant.setBuildFile("build.xml");
        ant.setTargets("build test");
        recipe.addCommand(ant);
        FakeMakeCommand make = new FakeMakeCommand("makeit");
        make.setTargets(Arrays.asList("all", "test"));
        recipe.addCommand(make);

        expectedOutputHelper(recipe, new Element("frecipe"));
    }

    public void testDefaultValuesAllDefaults() throws IOException
    {
        expectedOutputHelper(new DefaultValuesConfiguration(), new Element("defaults"));
    }

    public void testDefaultValuesStringNullSetEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringNull("");
        expectedOutputHelper(config, new Element("defaults"));

        // Null and empty are the same, so we get our default on the other end.
        config = roundTrip(config);
        assertNull(config.getStringNull());
    }

    public void testDefaultValuesStringNullSetNonEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringNull("something");
        config = roundTrip(config);
        assertEquals("something", config.getStringNull());
    }

    public void testDefaultValuesStringEmptySetNull() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringEmpty(null);
        expectedOutputHelper(config, new Element("defaults"));

        // Null and empty are the same, so we get our default on the other end.
        config = roundTrip(config);
        assertEquals("", config.getStringEmpty());
    }

    public void testDefaultValuesStringEmptySetNonEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringEmpty("something");
        config = roundTrip(config);
        assertEquals("something", config.getStringEmpty());
    }

    public void testDefaultValuesStringNonEmptySetNull() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringNonEmpty(null);
        config = roundTrip(config);
        assertEquals("", config.getStringNonEmpty());
    }

    public void testDefaultValuesStringNonEmptySetEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringNonEmpty("");
        config = roundTrip(config);
        assertEquals("", config.getStringNonEmpty());
    }

    public void testDefaultValuesStringNonEmptyChanged() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringNonEmpty("new&shiny");
        config = roundTrip(config);
        assertEquals("new&shiny", config.getStringNonEmpty());
    }

    public void testDefaultValuesIntZeroSetNonZero() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setIntZero(198);
        config = roundTrip(config);
        assertEquals(198, config.getIntZero());
    }

    public void testDefaultValuesIntNonZeroSetZero() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setIntNonZero(0);
        config = roundTrip(config);
        assertEquals(0, config.getIntNonZero());
    }

    public void testDefaultValuesEnumNullSetNonNull() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setEnumNull(TestEnum.C_2);
        config = roundTrip(config);
        assertEquals(TestEnum.C_2, config.getEnumNull());
    }

    public void testDefaultValuesEnumNonNullSetNull() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setEnumNonNull(null);
        config = roundTrip(config);
        assertNull(config.getEnumNonNull());
    }

    public void testDefaultValuesStringListNullSetEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringListNull(Collections.<String>emptyList());
        config = roundTrip(config);
        assertNull(config.getStringListNull());
    }

    public void testDefaultValuesStringListNullSetNonEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringListNull(Arrays.asList("hehe"));
        config = roundTrip(config);
        assertEquals(Arrays.asList("hehe"), config.getStringListNull());
    }

    public void testDefaultValuesStringListEmptySetNull() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringListEmpty(null);
        config = roundTrip(config);
        assertEquals(Collections.<String>emptyList(), config.getStringListEmpty());
    }

    public void testDefaultValuesStringListEmptySetNonEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringListEmpty(Arrays.asList("fee", "fie"));
        config = roundTrip(config);
        assertEquals(Arrays.asList("fee", "fie"), config.getStringListEmpty());
    }

    public void testDefaultValuesStringListNonEmptySetNull() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringListNonEmpty(null);
        config = roundTrip(config);
        assertEquals(Collections.<String>emptyList(), config.getStringListNonEmpty());
    }

    public void testDefaultValuesStringListNonEmptySetEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setStringListNonEmpty(Collections.<String>emptyList());
        config = roundTrip(config);
        assertEquals(Collections.<String>emptyList(), config.getStringListNonEmpty());
    }

    public void testDefaultValuesEnumListNullSetEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setEnumListNull(Collections.<TestEnum>emptyList());
        config = roundTrip(config);
        assertNull(config.getEnumListNull());
    }

    public void testDefaultValuesEnumListNullSetNonEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setEnumListNull(Arrays.asList(TestEnum.C1));
        config = roundTrip(config);
        assertEquals(Arrays.asList(TestEnum.C1), config.getEnumListNull());
    }

    public void testDefaultValuesEnumListEmptySetNull() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setEnumListEmpty(null);
        config = roundTrip(config);
        assertEquals(Collections.<TestEnum>emptyList(), config.getEnumListEmpty());
    }

    public void testDefaultValuesTestEnumListEmptySetNonEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setEnumListEmpty(Arrays.asList(TestEnum.C3));
        config = roundTrip(config);
        assertEquals(Arrays.asList(TestEnum.C3), config.getEnumListEmpty());
    }

    public void testDefaultValuesEnumListNonEmptySetNull() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setEnumListNonEmpty(null);
        config = roundTrip(config);
        assertEquals(Collections.<TestEnum>emptyList(), config.getEnumListNonEmpty());
    }

    public void testDefaultValuesEnumListNonEmptySetEmpty() throws IOException, PulseException
    {
        DefaultValuesConfiguration config = new DefaultValuesConfiguration();
        config.setEnumListNonEmpty(Collections.<TestEnum>emptyList());
        config = roundTrip(config);
        assertEquals(Collections.<TestEnum>emptyList(), config.getEnumListNonEmpty());
    }
    
    private void expectedOutputHelper(Configuration root, Element element) throws IOException
    {
        File tempDir = createTempDirectory();
        try
        {
            File out = new File(tempDir, "pulse.xml");
            storer.store(out, root, element);
            IOAssertions.assertFilesEqual(copyInputToDirectory("xml", tempDir), out);
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    private MixedConfiguration roundTrip(MixedConfiguration in) throws IOException, PulseException
    {
        return roundTrip(in, new MixedConfiguration());
    }

    private DefaultValuesConfiguration roundTrip(DefaultValuesConfiguration in) throws IOException, PulseException
    {
        return roundTrip(in, new DefaultValuesConfiguration());
    }

    private <T extends Configuration> T roundTrip(T in, T out) throws IOException, PulseException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        storer.store(os, in, new Element("root"));
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        loader.load(is, out, scope, new ImportingNotSupportedFileResolver(), new DefaultTypeLoadPredicate());
        return out;
    }
}
