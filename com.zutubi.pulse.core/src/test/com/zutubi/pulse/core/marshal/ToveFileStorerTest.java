package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.GenericReference;
import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.marshal.types.*;
import com.zutubi.pulse.core.test.IOAssertions;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import nu.xom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

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
        scope.add(new GenericReference<TrivialConfiguration>(referenceable1.getName(), referenceable1));
        scope.add(new GenericReference<TrivialConfiguration>(referenceable2.getName(), referenceable2));
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
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        try
        {
            FakeRecipe recipe = new FakeRecipe("default");
            FakeAntCommand ant = new FakeAntCommand("eatit");
            ant.setBuildFile("build.xml");
            ant.setTargets("build test");
            recipe.addCommand(ant);
            FakeMakeCommand make = new FakeMakeCommand("makeit");
            make.setTargets(Arrays.asList("all", "test"));
            recipe.addCommand(make);
            
            File out = new File(tempDir, "pulse.xml");
            storer.store(out, recipe, new Element("frecipe"));
            IOAssertions.assertFilesEqual(copyInputToDirectory("xml", tempDir), out);
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    private MixedConfiguration roundTrip(MixedConfiguration mixed) throws IOException, PulseException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        storer.store(os, mixed, new Element("root"));
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        mixed = new MixedConfiguration();
        loader.load(is, mixed, scope, new ImportingNotSupportedFileResolver(), new DefaultTypeLoadPredicate());
        return mixed;
    }
}
