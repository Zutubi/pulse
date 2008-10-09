package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;

/**
 * 
 *
 */
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
        loader.load(getInput("testSimpleReference"), root);

        Object o = root.getReference("a");
        assertNotNull(o);
        assertTrue(o instanceof SimpleReference);

        SimpleReference t = (SimpleReference) o;
        assertEquals("a", t.getName());
    }

    public void testResolveReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testResolveReference"), root);

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
        loader.load(getInput("testNestedType"), root);

        assertNotNull(root.getReference("a"));

        SimpleNestedType a = (SimpleNestedType) root.getReference("a");
        assertNotNull(a.getNestedType("b"));
        assertNotNull(a.getNestedType("c"));
    }

    public void testNonBeanName() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testNonBeanName"), root);

        Object a = root.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SomeReference);
        assertEquals("a", ((SomeReference) a).getSomeValue());

    }

    public void testRegister() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testRegister"), pf);
        assertNotNull(pf.getRecipe("default"));
    }

    public void testScopeTopLevel() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testScopeTopLevel"), pf);

        assertNotNull(pf.getRecipe("first"));
        assertNotNull(pf.getRecipe("second"));
    }

    public void testMacroEmpty() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testMacroEmpty"), pf);

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
            assertMatches("Processing element 'macro-ref': starting at line 9 column (9|10): While expanding macro defined at line 4 column (5|6): Processing element 'no-such-type': starting at line 5 column (9|10): Undefined type 'no-such-type'", e.getMessage());
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
        errorHelper("testMacroRefNotFound", "Unknown variable reference 'not-found'");
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
            loader.load(getInput("testValidateable"), bf);
            fail();
        }
        catch (ParseException e)
        {
            assertMatches("Processing element 'validateable': starting at line 4 column [56]: error\n", e.getMessage());
        }
    }

    public void testAdderTypeScope() throws PulseException
    {
        ScopeAcceptingRoot root = new ScopeAcceptingRoot();
        loader.load(getInput(getName()), root);
        assertNotNull(root.getReference("a"));
        assertNotNull(root.getScope("a"));
    }

    public void testAdderScope() throws PulseException
    {
        loader.register("ref", SimpleReference.class);
        ScopeAcceptingRoot root = new ScopeAcceptingRoot();
        loader.load(getInput(getName()), root);
        assertNotNull(root.getReference("a"));
        assertNotNull(root.getScope("a"));
    }

    public void testSpecificRecipeError() throws PulseException
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput("testSpecificRecipe"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, "don't load!"));
            fail();
        }
        catch (PulseException e)
        {
            e.printStackTrace();
        }
    }

    public void testDuplicateRecipe() throws PulseException
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput("testDuplicateRecipe"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, "don't load!"));
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
            loader.load(getInput("testUnknownAttribute"), new SimpleType());
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
