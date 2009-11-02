package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import com.zutubi.util.FileSystemUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileLoaderTest extends FileLoaderTestBase
{
    private static final String EXTENSION_XML = "xml";

    public void setUp() throws Exception
    {
        super.setUp();

        // initialise the loader some test objects.
        loader.register("reference", SimpleReference.class);
        loader.register("nested", SimpleNestedType.class);
        loader.register("type", SimpleType.class);
        loader.register("some-reference", SomeReference.class);
        loader.register("validateable", SimpleValidateable.class);

        loader.register("frecipe", FakeRecipe.class);
        loader.register("fant", FakeAntCommand.class);
        loader.register("fmake", FakeMakeCommand.class);
    }

    public void testSimpleReference() throws Exception
    {
        SimpleRoot root = loadRoot();

        Object o = root.getReference("a");
        assertNotNull(o);
        assertTrue(o instanceof SimpleReference);

        SimpleReference t = (SimpleReference) o;
        assertEquals("a", t.getName());
    }

    public void testResolveReference() throws Exception
    {
        SimpleRoot root = loadRoot();

        Object a = root.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SimpleReference);

        Object b = root.getReference("b");
        assertNotNull(b);
        assertTrue(b instanceof SimpleReference);

        SimpleReference rb = (SimpleReference) b;
        assertEquals(a, rb.getRef());
    }

    public void testNestedType() throws Exception
    {
        SimpleRoot root = loadRoot();

        assertNotNull(root.getReference("a"));

        SimpleNestedType a = (SimpleNestedType) root.getReference("a");
        assertNotNull(a.getNestedType("b"));
        assertNotNull(a.getNestedType("c"));
    }

    public void testNonBeanName() throws Exception
    {
        SimpleRoot root = loadRoot();

        Object a = root.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SomeReference);
        assertEquals("a", ((SomeReference) a).getSomeValue());

    }

    public void testRegister() throws Exception
    {
        PulseFile pf = loadPulseFile();
        assertNotNull(pf.getRecipe("default"));
    }

    public void testScopeTopLevel() throws Exception
    {
        PulseFile pf = loadPulseFile();

        assertNotNull(pf.getRecipe("first"));
        assertNotNull(pf.getRecipe("second"));
    }

    public void testMacroEmpty() throws Exception
    {
        PulseFile pf = loadPulseFile();

        Recipe recipe = pf.getRecipe("r1");
        assertNotNull(recipe);
        assertEquals(0, recipe.getCommands().size());
    }

    public void testMacroExpandError() throws Exception
    {
        try
        {
            load(getName());
            fail();
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), matchesRegex("Processing element 'macro-ref': starting at line 9 column (9|10): While expanding macro defined at line 4 column (5|6):\\n  Processing element 'no-such-type': starting at line 5 column (9|10): Undefined type 'no-such-type'"));
        }
    }

    public void testMacroNoName() throws Exception
    {
        errorHelper("testMacroNoName", "Required attribute 'name' not found");
    }

    public void testMacroUnknownAttribute() throws Exception
    {
        errorHelper("testMacroUnknownAttribute", "Unrecognised attribute 'unkat'");
    }

    public void testMacroRefNoMacro() throws Exception
    {
        errorHelper("testMacroRefNoMacro", "Required attribute 'macro' not found");
    }

    public void testMacroRefNotMacro() throws Exception
    {
        errorHelper("testMacroRefNotMacro", "Reference '${not-macro}' does not resolve to a macro");
    }

    public void testMacroRefNotFound() throws Exception
    {
        errorHelper("testMacroRefNotFound", "Unknown reference 'not-found'");
    }

    public void testMacroRefUnknownAttribute() throws Exception
    {
        errorHelper("testMacroRefUnknownAttribute", "Unrecognised attribute 'whatthe'");
    }

    public void testMacroInfiniteRecursion() throws Exception
    {
        errorHelper("testMacroInfiniteRecursion", "Maximum recursion depth 128 exceeded");
    }

    public void testValidateable() throws Exception
    {
        try
        {
            loadPulseFile();
            fail();
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), matchesRegex("Processing element 'validateable': starting at line 4 column [56]: error\n"));
        }
    }

    public void testAdderTypeScope() throws PulseException
    {
        ScopeAcceptingRoot root = new ScopeAcceptingRoot();
        loader.load(getInput(EXTENSION_XML), root, new ImportingNotSupportedFileResolver());
        assertNotNull(root.getReference("a"));
        assertNotNull(root.getScope("a"));
    }

    public void testAdderScope() throws PulseException
    {
        loader.register("ref", SimpleReference.class);
        ScopeAcceptingRoot root = new ScopeAcceptingRoot();
        loader.load(getInput(EXTENSION_XML), root, new ImportingNotSupportedFileResolver());
        assertNotNull(root.getReference("a"));
        assertNotNull(root.getScope("a"));
    }

    public void testSpecificRecipeError() throws PulseException
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput(EXTENSION_XML), bf, null, new ImportingNotSupportedFileResolver(), new FileResourceRepository(), new RecipeLoadPredicate(bf, "don't load!"));
            fail();
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), containsString("Undefined type 'resource'"));
        }
    }

    public void testDuplicateRecipe() throws PulseException
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput(EXTENSION_XML), bf, null, new ImportingNotSupportedFileResolver(), new FileResourceRepository(), new RecipeLoadPredicate(bf, "don't load!"));
            fail();
        }
        catch (PulseException e)
        {
            assertTrue(e.getMessage().contains("A recipe with name 'a' already exists"));
        }
    }

    public void testUnknownAttribute()
    {
        try
        {
            loader.load(getInput(EXTENSION_XML), new SimpleType(), new ImportingNotSupportedFileResolver());
            fail();
        }
        catch (PulseException e)
        {
            if (!e.getMessage().contains("bad-attribute"))
            {
                fail();
            }
        }
    }

    public void testBasicImport() throws IOException, PulseException
    {
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
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
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
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
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
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
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
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
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
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
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
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
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
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
            assertThat(e.getMessage(), matchesRegex("(?s).*  Processing element 'unknown': starting at line 4 column [0-9]+ of file testErrorInImportedMacro\\.macros\\.xml: Undefined type 'unknown'.*"));
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
        List<FakeCommand> commands = recipe.getCommands();
        assertEquals(1, commands.size());
        assertEquals(name, commands.get(0).getName());
    }

    private void loadAndAssertImportedCommands(File tempDir) throws PulseException
    {
        FakePulseFile pf = new FakePulseFile();
        loader.load(getInput(EXTENSION_XML), pf, new LocalFileResolver(tempDir));
        FakeRecipe recipe = pf.getRecipe("default");
        assertNotNull(recipe);
        List<FakeCommand> commands = recipe.getCommands();
        assertEquals(2, commands.size());
        assertEquals("build", commands.get(0).getName());
        assertEquals("test", commands.get(1).getName());
    }

    private SimpleRoot loadRoot() throws PulseException
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput(EXTENSION_XML), root, new ImportingNotSupportedFileResolver());
        return root;
    }

    private PulseFile loadPulseFile() throws PulseException
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput(EXTENSION_XML), pf, new ImportingNotSupportedFileResolver());
        return pf;
    }
}
