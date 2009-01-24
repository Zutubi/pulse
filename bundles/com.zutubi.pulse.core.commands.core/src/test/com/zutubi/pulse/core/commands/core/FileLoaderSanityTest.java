//package com.zutubi.pulse.core.commands.core;
//
//import com.zutubi.pulse.core.*;
//import com.zutubi.pulse.core.engine.api.Property;
//import com.zutubi.pulse.core.api.PulseException;
//
///**
// *
// *
// */
//public class FileLoaderSanityTest extends FileLoaderTestBase
//{
//    public void setUp() throws Exception
//    {
//        super.setUp();
//
//        // initialise the loader some test objects.
//        loader.register("dependency", Dependency.class);
//
//        loader.register("executable", ExecutableCommand.class);
//        loader.register("property", Property.class);
//        loader.register("post-processor", PostProcessorGroup.class);
//        loader.register("regex.pp", RegexPostProcessor.class);
//    }
//
//    public void testSampleProject() throws Exception
//    {
//        PulseFile bf = new PulseFile();
//        PulseScope scope = new PulseScope();
//        Property property = new Property("base.dir", "/whatever");
//        scope.add(property);
//
//        loader.load(getInput("testSampleProject", "xml"), bf, scope, new FileResourceRepository(), null);
//    }
//
//    public void testScope() throws Exception
//    {
//        PulseFile pf = new PulseFile();
//        loader.load(getInput("testScope", "xml"), pf);
//
//        Recipe recipe = pf.getRecipe("r1");
//        assertNotNull(recipe);
//        assertNotNull(recipe.getCommand("scope1"));
//        assertNotNull(recipe.getCommand("scope2"));
//    }
//
//    public void testScoping() throws Exception
//    {
//        PulseFile pf = new PulseFile();
//        loader.load(getInput("testScoping", "xml"), pf);
//
//        Recipe recipe = pf.getRecipe("global");
//        assertNotNull(recipe);
//        Command command = recipe.getCommand("in recipe");
//        assertNotNull(command);
//
//        ExecutableCommand exe = (ExecutableCommand) ((CommandGroup)command).getCommand();
//        assertEquals("in command", exe.getExe());
//    }
//
//    public void testMacro() throws Exception
//    {
//        PulseFile pf = new PulseFile();
//        loader.load(getInput("testMacro", "xml"), pf);
//
//        Recipe recipe = pf.getRecipe("r1");
//        assertNotNull(recipe);
//        Command command = recipe.getCommand("m1-e1");
//        assertNotNull(command);
//        command = recipe.getCommand("m1-e2");
//        assertNotNull(command);
//    }
//
//    public void testArtifactInvalidName() throws Exception
//    {
//        errorHelper("testArtifactInvalidName", "alphanumeric");
//    }
//
//    public void testArtifactMissingName() throws Exception
//    {
//        errorHelper("testArtifactMissingName", "Required attribute name not specified");
//    }
//
//    public void testProcessNoProcessor() throws PulseException
//    {
//        try
//        {
//            PulseFile bf = new PulseFile();
//            loader.load(getInput("testProcessNoProcessor", "xml"), bf);
//            fail();
//        }
//        catch (ParseException e)
//        {
//            assertTrue(e.getMessage().contains("attribute 'processor' not specified"));
//        }
//    }
//
//    public void testSpecificRecipe() throws PulseException
//    {
//        PulseFile bf = new PulseFile();
//        loader.load(getInput("testSpecificRecipe", "xml"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, "default"));
//        assertEquals(2, bf.getRecipes().size());
//        assertNotNull(bf.getRecipe("default"));
//        assertNotNull(bf.getRecipe("default").getCommand("build"));
//        assertNotNull(bf.getRecipe("don't load!"));
//    }
//
//    public void testSpecificRecipeDefault() throws PulseException
//    {
//        PulseFile bf = new PulseFile();
//        loader.load(getInput("testSpecificRecipe", "xml"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, null));
//        assertEquals(2, bf.getRecipes().size());
//        assertNotNull(bf.getRecipe("default"));
//        assertNotNull(bf.getRecipe("default").getCommand("build"));
//        assertNotNull(bf.getRecipe("don't load!"));
//    }
//}
