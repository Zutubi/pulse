package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class FileLoaderTest extends FileLoaderTestBase
{
    public void setUp() throws Exception 
    {
        super.setUp();

        // initialise the loader some test objects.
        loader.register("reference", SimpleReference.class);
        loader.register("nested", SimpleNestedType.class);
        loader.register("type", SimpleType.class);
        loader.register("some-reference", SomeReference.class);
        loader.register("validateable", SimpleValidateable.class);
    }

    public void testSimpleReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testSimpleReference", "xml"), root);

        Object o = root.getReference("a");
        assertNotNull(o);
        assertTrue(o instanceof SimpleReference);

        SimpleReference t = (SimpleReference) o;
        assertEquals("a", t.getName());
    }

    public void testResolveReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testResolveReference", "xml"), root);

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
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testNestedType", "xml"), root);

        assertNotNull(root.getReference("a"));

        SimpleNestedType a = (SimpleNestedType) root.getReference("a");
        assertNotNull(a.getNestedType("b"));
        assertNotNull(a.getNestedType("c"));
    }

    public void testNonBeanName() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testNonBeanName", "xml"), root);

        Object a = root.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SomeReference);
        assertEquals("a", ((SomeReference) a).getSomeValue());

    }

    public void testRegister() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testRegister", "xml"), pf);
        assertNotNull(pf.getRecipe("default"));
    }

    public void testScopeTopLevel() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testScopeTopLevel", "xml"), pf);

        assertNotNull(pf.getRecipe("first"));
        assertNotNull(pf.getRecipe("second"));
    }

    public void testMacroEmpty() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testMacroEmpty", "xml"), pf);

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
            assertThat(e.getMessage(), matchesRegex("Processing element 'macro-ref': starting at line 9 column (9|10): While expanding macro defined at line 4 column (5|6): Processing element 'no-such-type': starting at line 5 column (9|10): Undefined type 'no-such-type'"));
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

    public void testValidation() throws Exception
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput("testValidateable", "xml"), bf);
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
        loader.load(getInput(getName(), "xml"), root);
        assertNotNull(root.getReference("a"));
        assertNotNull(root.getScope("a"));
    }

    public void testAdderScope() throws PulseException
    {
        loader.register("ref", SimpleReference.class);
        ScopeAcceptingRoot root = new ScopeAcceptingRoot();
        loader.load(getInput(getName(), "xml"), root);
        assertNotNull(root.getReference("a"));
        assertNotNull(root.getScope("a"));
    }

    public void testSpecificRecipeError() throws PulseException
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput("xml"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, "don't load!"));
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
            loader.load(getInput("testDuplicateRecipe", "xml"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, "don't load!"));
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
            loader.load(getInput("testUnknownAttribute", "xml"), new SimpleType());
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
}
