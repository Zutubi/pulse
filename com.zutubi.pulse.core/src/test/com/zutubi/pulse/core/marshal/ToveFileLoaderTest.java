package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Content;
import com.zutubi.pulse.core.engine.api.PropertyConfiguration;
import com.zutubi.pulse.core.engine.api.Referenceable;
import com.zutubi.pulse.core.marshal.types.*;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ToveFileLoaderTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";

    private ToveFileLoader loader;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        TypeRegistry registry = new TypeRegistry();
        registry.register(SimpleRoot.class);

        TypeDefinitions definitions = new TypeDefinitions();
        definitions.register("reference", registry.getType(SimpleReference.class));
        definitions.register("referrer", registry.getType(Referrer.class));
        definitions.register("collection-referrer", registry.getType(CollectionReferrer.class));
        definitions.register("textual", registry.getType(Textual.class));
        definitions.register("property", registry.register(PropertyConfiguration.class));
        definitions.register("validateable", registry.register(SimpleValidateable.class));
        definitions.register("enumerator", registry.register(Enumerator.class));
        definitions.register("string-list", registry.register(StringList.class));

        registry.register(FakePulseFile.class);
        definitions.register("frecipe", registry.register(FakeRecipe.class));
        definitions.register("fant", registry.register(FakeAntCommand.class));
        definitions.register("fmake", registry.register(FakeMakeCommand.class));

        loader = new ToveFileLoader();
        loader.setTypeRegistry(registry);
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.setValidationManager(new PulseValidationManager());
        loader.setTypeDefinitions(definitions);
    }

    public void testSimpleReference() throws Exception
    {
        SimpleRoot root = load();
        SimpleReference t = root.getReferences().get("a");
        assertNotNull(t);
        assertEquals("a", t.getName());
    }

    public void testResolveReference() throws Exception
    {
        SimpleRoot root = load();
        Referrer er = root.getReferrers().get("er");
        assertNotNull(er);

        SimpleReference ee = er.getRef();
        assertNotNull(ee);
        assertEquals("ee", ee.getName());
    }

    public void testCollectionReference() throws Exception
    {
        SimpleRoot root = load();
        CollectionReferrer er = root.getCollectionReferrers().get("er");
        assertNotNull(er);

        assertEquals(1, er.getRefs().size());
        SimpleReference ee = er.getRefs().get(0);
        assertEquals("ee", ee.getName());
    }

    public void testContentProperty() throws Exception
    {
        SimpleRoot root = load();
        Textual textual = root.getTextual();
        assertNotNull(textual);
        assertEquals("text content v", textual.getX());
    }

    public void testMacroEmpty() throws Exception
    {
        SimpleRoot root = load();
        Recipe recipe = root.getRecipes().get("r1");
        assertNotNull(recipe);
        assertEquals(0, recipe.getCommands().size());
    }

    public void testMacroExpandError() throws Exception
    {
        try
        {
            load();
            fail();
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), matchesRegex("Processing element 'macro-ref': starting at line 9 column (9|10): While expanding macro defined at line 4 column (5|6):\n  Processing element 'no-such-type': starting at line 5 column (9|10): Unknown child element 'no-such-type'"));
        }
    }

    public void testMacroNoName() throws Exception
    {
        errorHelper("Required attribute 'name' not found");
    }

    public void testMacroUnknownAttribute() throws Exception
    {
        errorHelper("Unrecognised attribute 'unkat'");
    }

    public void testMacroRefNoMacro() throws Exception
    {
        errorHelper("Required attribute 'macro' not found");
    }

    public void testMacroRefNotMacro() throws Exception
    {
        errorHelper("Variable '${not-macro}' does not resolve to a macro");
    }

    public void testMacroRefNotFound() throws Exception
    {
        errorHelper("Unknown variable 'not-found'");
    }

    public void testMacroRefUnknownAttribute() throws Exception
    {
        errorHelper("Unrecognised attribute 'whatthe'");
    }

    public void testMacroInfiniteRecursion() throws Exception
    {
        errorHelper("Maximum recursion depth 128 exceeded");
    }

    public void testScopeTopLevel() throws Exception
    {
        SimpleRoot root = load();
        assertNotNull(root.getRecipes().get("first"));
        assertNotNull(root.getRecipes().get("second"));
    }

    public void testValidateable() throws Exception
    {
        try
        {
            load();
            fail();
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), matchesRegex("Processing element 'validateable': starting at line 4 column [56]: error\n"));
        }
    }

    public void testUnknownAttribute()
    {
        errorHelper("bad-attribute");
    }

    public void testEnumProperty() throws PulseException
    {
        enumPropertyHelper(Enumerator.Hotness.HANSEL);
    }

    public void testEnumPropertyNameConversion() throws PulseException
    {
        enumPropertyHelper(Enumerator.Hotness.JUST_BOILING);
    }

    public void testEnumPropertyInvalid() throws PulseException
    {
        errorHelper("Invalid value 'no such enum value'");
    }

    private void enumPropertyHelper(Enumerator.Hotness expectedHeat) throws PulseException
    {
        SimpleRoot simpleRoot = load();
        List<Enumerator> enumerators = simpleRoot.getEnumerators();
        assertEquals(1, enumerators.size());
        assertEquals(expectedHeat, enumerators.get(0).getHeat());
    }

    public void testStringListViaAdder() throws PulseException
    {
        SimpleRoot simpleRoot = load();
        assertNotNull(simpleRoot.getStringList());
        List<String> list = simpleRoot.getStringList().getAddableList();
        assertEquals(1, list.size());
        assertEquals("test value", list.get(0));
    }

    public void testStringListViaAttribute() throws PulseException
    {
        SimpleRoot simpleRoot = load();
        assertNotNull(simpleRoot.getStringList());
        List<String> list = simpleRoot.getStringList().getNotAddableList();
        assertEquals(2, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val2", list.get(1));
    }

    public void testAddables() throws PulseException
    {
        SimpleRoot root = load();
        Addables addables = root.getAddables();
        assertNotNull(addables);

        List<AddableItem> dashList = addables.getDashList();
        assertEquals(2, dashList.size());
        assertEquals("dashdash", dashList.get(0).getName());
        assertEquals("dashcamel", dashList.get(1).getName());

        Map<String, AddableItem> camelMap = addables.getCamelMap();
        assertEquals(2, camelMap.size());
        assertNotNull(camelMap.get("camelcamel"));
        assertNotNull(camelMap.get("cameldash"));
    }

    public void testBasicImport() throws IOException, PulseException
    {
        File tempDir = createTempDirectory();
        try
        {
            copyInputToDirectory(EXTENSION_XML, tempDir);
            copyInputToDirectory(getName() + ".commands", EXTENSION_XML, tempDir);

            loadAndAssertImportedCommands(tempDir);
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    public void testTopLevelImport() throws IOException, PulseException
    {
        File tempDir = createTempDirectory();
        try
        {
            copyInputToDirectory(EXTENSION_XML, tempDir);
            copyInputToDirectory(getName() + ".recipe", EXTENSION_XML, tempDir);

            loadAndAssertImportedCommands(tempDir);
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    public void testSelfImport() throws IOException, PulseException
    {
        File tempDir = createTempDirectory();
        try
        {
            copyInputToDirectory(EXTENSION_XML, tempDir);

            loader.load(getInput(EXTENSION_XML), new FakePulseFile(), new LocalFileResolver(tempDir));
            fail("Cannot process self-including file.");
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), containsString("Maximum recursion depth 128 exceeded"));
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    public void testImportUnknownFile() throws IOException, PulseException
    {
        File tempDir = createTempDirectory();
        try
        {
            copyInputToDirectory(EXTENSION_XML, tempDir);

            loader.load(getInput(EXTENSION_XML), new FakePulseFile(), new LocalFileResolver(tempDir));
            fail("Cannot import non-existant file.");
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), containsString("nosuchfile"));
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    public void testOptionalImportUnknownFile() throws IOException, PulseException
    {
        FakePulseFile pf = new FakePulseFile();
        loader.load(getInput(EXTENSION_XML), pf, new ImportingNotSupportedFileResolver());
        FakeRecipe recipe = pf.getRecipe("default");
        assertNotNull(recipe);
        assertEquals(0, recipe.getCommands().size());
    }

    public void testImportFromNestedDirectory() throws IOException, PulseException
    {
        File tempDir = createTempDirectory();
        try
        {
            copyInputToDirectory(EXTENSION_XML, tempDir);

            File nestedDir = new File(tempDir, "nested");
            assertTrue(nestedDir.mkdir());
            copyInputToDirectory(getName() + ".commands", EXTENSION_XML, nestedDir);

            loadAndAssertImportedCommands(tempDir);
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    public void testImportRelativeToImportingFile() throws IOException, PulseException
    {
        File tempDir = createTempDirectory();
        try
        {
            File importDir = new File(tempDir, "import");
            File nestedDir = new File(importDir, "nested");
            assertTrue(nestedDir.mkdirs());

            copyInputToDirectory(EXTENSION_XML, tempDir);

            copyInputToDirectory(getName() + ".recipes", EXTENSION_XML, importDir);
            copyInputToDirectory(getName() + ".alongside", EXTENSION_XML, importDir);
            copyInputToDirectory(getName() + ".above", EXTENSION_XML, tempDir);
            copyInputToDirectory(getName() + ".nested", EXTENSION_XML, nestedDir);

            FakePulseFile pf = new FakePulseFile();
            loader.load(getInput(EXTENSION_XML), pf, new LocalFileResolver(tempDir));
            assertSingleCommandRecipe(pf, "alongside");
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    public void testImportUnknownAttribute()
    {
        FakePulseFile pf = new FakePulseFile();
        try
        {
            loader.load(getInput(EXTENSION_XML), pf, new ImportingNotSupportedFileResolver());
            fail("Should not load with bad attribute on import tag");
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), containsString("Unrecognised attribute 'pith'"));
        }
    }

    public void testImportNoPath()
    {
        FakePulseFile pf = new FakePulseFile();
        try
        {
            loader.load(getInput(EXTENSION_XML), pf, new ImportingNotSupportedFileResolver());
            fail("Should not load with no path specified on import tag");
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), containsString("Required attribute 'path' not set"));
        }
    }

    public void testErrorInImportedMacro() throws IOException, PulseException
    {
        File tempDir = createTempDirectory();
        try
        {
            copyInputToDirectory(EXTENSION_XML, tempDir);
            copyInputToDirectory(getName() + ".macros", EXTENSION_XML, tempDir);

            loader.load(getInput(EXTENSION_XML), new FakePulseFile(), new LocalFileResolver(tempDir));
            fail("Cannot reference a macro containing an unknown element");
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), matchesRegex("(?s).*While expanding macro defined at line 3 column [0-9]+ of file testErrorInImportedMacro\\.macros\\.xml.*"));
            assertThat(e.getMessage(), matchesRegex("(?s).*  Processing element 'unknown': starting at line 4 column [0-9]+ of file testErrorInImportedMacro\\.macros\\.xml: Unknown child element 'unknown'.*"));
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    private void assertSingleCommandRecipe(FakePulseFile pf, String name)
    {
        FakeRecipe recipe = pf.getRecipe(name);
        assertNotNull(recipe);
        Map<String, FakeCommand> commands = recipe.getCommands();
        assertEquals(1, commands.size());
        assertTrue(commands.containsKey(name));
    }

    private void loadAndAssertImportedCommands(File tempDir) throws PulseException
    {
        FakePulseFile pf = new FakePulseFile();
        loader.load(getInput(EXTENSION_XML), pf, new LocalFileResolver(tempDir));
        FakeRecipe recipe = pf.getRecipe("default");
        assertNotNull(recipe);
        Map<String, FakeCommand> commands = recipe.getCommands();
        assertEquals(2, commands.size());
        assertTrue(commands.containsKey("build"));
        assertTrue(commands.containsKey("test"));
    }

    private SimpleRoot load() throws PulseException
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput(EXTENSION_XML), root, new ImportingNotSupportedFileResolver());
        return root;
    }

    private void errorHelper(String messageContent)
    {
        try
        {
            load();
            fail();
        }
        catch (PulseException e)
        {
            if (!e.getMessage().contains(messageContent))
            {
                fail("Message '" + e.getMessage() + "' does not contain '" + messageContent + "'");
            }
        }
    }

    @SymbolicName("simpleRoot")
    public static class SimpleRoot extends AbstractConfiguration
    {
        private Map<String, SimpleReference> references = new HashMap<String, SimpleReference>();
        private Map<String, Referrer> referrers = new HashMap<String, Referrer>();
        private Map<String, CollectionReferrer> collectionReferrers = new HashMap<String, CollectionReferrer>();
        @Addable("recipe")
        private Map<String, Recipe> recipes = new LinkedHashMap<String, Recipe>();
        private Textual textual;
        private List<Enumerator> enumerators = new LinkedList<Enumerator>();
        private StringList stringList;
        private Addables addables;

        public Map<String, SimpleReference> getReferences()
        {
            return references;
        }

        public void setReferences(Map<String, SimpleReference> references)
        {
            this.references = references;
        }

        public Map<String, Referrer> getReferrers()
        {
            return referrers;
        }

        public void setReferrers(Map<String, Referrer> referrers)
        {
            this.referrers = referrers;
        }

        public Map<String, CollectionReferrer> getCollectionReferrers()
        {
            return collectionReferrers;
        }

        public void setCollectionReferrers(Map<String, CollectionReferrer> collectionReferrers)
        {
            this.collectionReferrers = collectionReferrers;
        }

        public Textual getTextual()
        {
            return textual;
        }

        public void setTextual(Textual textual)
        {
            this.textual = textual;
        }

        public Map<String, Recipe> getRecipes()
        {
            return recipes;
        }

        public void setRecipes(Map<String, Recipe> recipes)
        {
            this.recipes = recipes;
        }

        public List<Enumerator> getEnumerators()
        {
            return enumerators;
        }

        public void setEnumerators(List<Enumerator> enumerators)
        {
            this.enumerators = enumerators;
        }

        public StringList getStringList()
        {
            return stringList;
        }

        public void setStringList(StringList stringList)
        {
            this.stringList = stringList;
        }

        public Addables getAddables()
        {
            return addables;
        }

        public void setAddables(Addables addables)
        {
            this.addables = addables;
        }
    }

    @SymbolicName("simpleReference")
    @Referenceable
    public static class SimpleReference extends AbstractNamedConfiguration
    {
    }

    @SymbolicName("referrer")
    public static class Referrer extends AbstractNamedConfiguration
    {
        private SimpleReference ref;

        public SimpleReference getRef()
        {
            return ref;
        }

        public void setRef(SimpleReference ref)
        {
            this.ref = ref;
        }
    }

    @SymbolicName("collectionReferrer")
    public static class CollectionReferrer extends AbstractNamedConfiguration
    {
        @com.zutubi.tove.annotations.Reference @Addable(value = "el", attribute = "at")
        private List<SimpleReference> refs = new LinkedList<SimpleReference>();

        public List<SimpleReference> getRefs()
        {
            return refs;
        }

        public void setRefs(List<SimpleReference> refs)
        {
            this.refs = refs;
        }
    }

    @SymbolicName("textual")
    public static class Textual extends AbstractConfiguration
    {
        @Content
        private String x;

        public String getX()
        {
            return x;
        }

        public void setX(String x)
        {
            this.x = x;
        }
    }

    @SymbolicName("recipe")
    public static class Recipe extends AbstractNamedConfiguration
    {
        @Addable("command")
        private List<Command> commands = new LinkedList<Command>();

        public List<Command> getCommands()
        {
            return commands;
        }

        public void setCommands(List<Command> commands)
        {
            this.commands = commands;
        }
    }

    @SymbolicName("command")
    public static class Command extends AbstractNamedConfiguration
    {
    }

    @SymbolicName("validateable")
    public static class SimpleValidateable extends AbstractConfiguration implements Validateable
    {
        public void validate(ValidationContext context)
        {
            context.addFieldError("field", "error");
        }
    }

    @SymbolicName("enumerator")
    public static class Enumerator extends AbstractConfiguration
    {
        public enum Hotness
        {
            VERY_COLD,
            MILDLY_TEPID,
            SLIGHTLY_WARM,
            QUITE_HOT,
            JUST_BOILING,
            HANSEL
        }

        private Hotness heat;

        public Hotness getHeat()
        {
            return heat;
        }

        public void setHeat(Hotness heat)
        {
            this.heat = heat;
        }
    }

    @SymbolicName("stringlist")
    public static class StringList extends AbstractConfiguration
    {
        @Addable(value = "add-item", attribute = "val")
        private List<String> addableList = new LinkedList<String>();
        private List<String> notAddableList = new LinkedList<String>();

        public List<String> getAddableList()
        {
            return addableList;
        }

        public void setAddableList(List<String> addableList)
        {
            this.addableList = addableList;
        }

        public List<String> getNotAddableList()
        {
            return notAddableList;
        }

        public void setNotAddableList(List<String> notAddableList)
        {
            this.notAddableList = notAddableList;
        }
    }

    @SymbolicName("addables")
    public static class Addables extends AbstractConfiguration
    {
        @Addable(value = "dash-item")
        private List<AddableItem> dashList = new LinkedList<AddableItem>();
        @Addable(value = "camelItem")
        private Map<String, AddableItem> camelMap = new HashMap<String, AddableItem>();

        public List<AddableItem> getDashList()
        {
            return dashList;
        }

        public void setDashList(List<AddableItem> dashList)
        {
            this.dashList = dashList;
        }

        public Map<String, AddableItem> getCamelMap()
        {
            return camelMap;
        }

        public void setCamelMap(Map<String, AddableItem> camelMap)
        {
            this.camelMap = camelMap;
        }
    }

    @SymbolicName("addableItem")
    public static class AddableItem extends AbstractNamedConfiguration
    {

    }
}
