package com.cinnamonbob.test;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.FileArtifact;
import com.cinnamonbob.core.model.*;
import com.cinnamonbob.model.*;
import com.cinnamonbob.model.persistence.BuildResultDao;
import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.model.persistence.SlaveDao;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class SetupDummyBuilds implements Runnable
{
    private SlaveDao slaveDao;
    private ProjectDao projectDao;
    private BuildResultDao buildResultDao;
    private Slave slave;
    private P4 scm;

    public void run()
    {
        Project project;

        slaveDao = (SlaveDao) ComponentContext.getBean("slaveDao");
        projectDao = (ProjectDao) ComponentContext.getBean("projectDao");
        buildResultDao = (BuildResultDao) ComponentContext.getBean("buildResultDao");

        if (projectDao.findAll().size() == 0)
        {
            setupSlave();
            project = setupProject("complex success");
            createComplexSuccess(project);

            project = setupProject("build in progress");
            createInProgress(project);

            project = setupProject("recipes pending");
            createPendingRecipes(project);

            project = setupProject("command failure");
            createCommandFailure(project);

            project = setupProject("warning features");
            createWarningFeatures(project);

            project = setupProject("error features");
            createErrorFeatures(project);
        }
    }

    private void setupSlave()
    {
        slave = new Slave("local slave", "localhost", 8090);
        slaveDao.save(slave);
    }

    private Project setupProject(String name)
    {
        Project project = new Project(name, "A test project with a decently long description to test wrapping etc.");
        project.setBobFile("bob.xml");

        scm = new P4();
        scm.setName("perforce");
        scm.setPort(":1666");
        scm.setUser("jsankey");
        scm.setClient("bob");
        project.addScm(scm);

        BuildSpecification simpleSpec = new BuildSpecification("simple");
        BuildStage simpleStage = new BuildStage(new MasterBuildHostRequirements(), null);
        BuildSpecificationNode simpleNode = new BuildSpecificationNode(simpleStage);
        simpleSpec.getRoot().addChild(simpleNode);
        project.addBuildSpecification(simpleSpec);

        BuildSpecification slaveSpec = new BuildSpecification("slave");
        BuildStage slaveStage = new BuildStage(new SlaveBuildHostRequirements(slave), null);
        BuildSpecificationNode slaveNode = new BuildSpecificationNode(slaveStage);
        slaveSpec.getRoot().addChild(slaveNode);
        project.addBuildSpecification(slaveSpec);

        BuildSpecification chainedSpec = new BuildSpecification("master to slave");
        BuildStage masterStage = new BuildStage(new MasterBuildHostRequirements(), null);
        BuildStage chainedStage = new BuildStage(new SlaveBuildHostRequirements(slave), "chained");
        BuildSpecificationNode masterNode = new BuildSpecificationNode(masterStage);
        BuildSpecificationNode chainedNode = new BuildSpecificationNode(chainedStage);
        masterNode.addChild(chainedNode);
        chainedSpec.getRoot().addChild(masterNode);
        project.addBuildSpecification(chainedSpec);

        projectDao.save(project);
        return project;
    }

    private void createComplexSuccess(Project project)
    {
        BuildResult result = new BuildResult(project, 1);
        List<Changelist> changelists = new LinkedList<Changelist>();

        NumericalRevision userRevision = new NumericalRevision(101);
        userRevision.setAuthor("jsankey");
        userRevision.setComment("a short comment");
        userRevision.setDate(new Date(System.currentTimeMillis() - 100000));
        Changelist list = new Changelist(userRevision);
        list.addChange(new Change("/home/jsankey/some/normal/file", "11", Change.Action.EDIT));
        list.addChange(new Change("/home/jsankey/some/other/file", "10", Change.Action.EDIT));
        list.addChange(new Change("/home/jsankey/some/branched/file", "1", Change.Action.BRANCH));
        changelists.add(list);

        BuildScmDetails scmDetails = new BuildScmDetails(scm.getName(), new NumericalRevision(16672), changelists);
        result.addScmDetails(scm.getId(), scmDetails);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createComplexRecipe();
        RecipeResultNode childNode = createComplexRecipe();
        rootResultNode.addChild(childNode);
        result.getRoot().addChild(rootResultNode);
        result.complete();
        buildResultDao.save(result);
    }

    private void createInProgress(Project project)
    {
        BuildResult result = new BuildResult(project, 1111);

        result.commence(new File("test"));
        RecipeResultNode node = createInProgressRecipe();
        result.getRoot().addChild(node);
        buildResultDao.save(result);
    }

    private void createPendingRecipes(Project project)
    {
        BuildResult result = new BuildResult(project, 666);

        result.commence(new File("test"));
        RecipeResultNode root = createInProgressRecipe();
        RecipeResultNode pending1 = createPendingRecipe();
        RecipeResultNode pending2 = createPendingRecipe();
        root.addChild(pending1);
        root.addChild(pending2);
        result.getRoot().addChild(root);
        buildResultDao.save(result);
    }

    private void createCommandFailure(Project project)
    {
        BuildResult result = new BuildResult(project, 10000);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createComplexRecipe();
        RecipeResultNode childNode = createCommandFailedRecipe();
        rootResultNode.addChild(childNode);
        result.getRoot().addChild(rootResultNode);
        result.failure("Recipe '" + childNode.getResult().getRecipeNameSafe() + "@" + childNode.getHost() + "' failed");
        result.complete();
        buildResultDao.save(result);
    }

    private void createWarningFeatures(Project project)
    {
        BuildResult result = new BuildResult(project, 10000);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createComplexRecipe();
        RecipeResultNode childNode = createWarningFeaturesRecipe();
        rootResultNode.addChild(childNode);
        result.getRoot().addChild(rootResultNode);
        result.complete();
        buildResultDao.save(result);
    }

    private void createErrorFeatures(Project project)
    {
        BuildResult result = new BuildResult(project, 10000);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createComplexRecipe();
        RecipeResultNode childNode = createErrorFeaturesRecipe();
        rootResultNode.addChild(childNode);
        result.getRoot().addChild(rootResultNode);
        result.complete();
        buildResultDao.save(result);
    }

    private RecipeResultNode createComplexRecipe()
    {
        RecipeResult recipeResult = new RecipeResult(null);

        recipeResult.commence(new File("/complex/recipe/output/dir"));
        recipeResult.add(createComplexCommand());
        recipeResult.add(createComplexCommand());
        recipeResult.add(createComplexCommand());
        recipeResult.complete();
        RecipeResultNode node = new RecipeResultNode(recipeResult);
        node.setHost("[master]");

        return node;
    }

    private RecipeResultNode createInProgressRecipe()
    {
        RecipeResult recipeResult = new RecipeResult(null);

        recipeResult.commence(new File("/complex/recipe/output/dir"));
        recipeResult.add(createComplexCommand());
        recipeResult.add(createInProgressCommand());
        recipeResult.add(createPendingCommand());
        recipeResult.add(createPendingCommand());

        RecipeResultNode node = new RecipeResultNode(recipeResult);
        node.setHost("my slave");
        return node;
    }

    private RecipeResultNode createPendingRecipe()
    {
        RecipeResult recipeResult = new RecipeResult("my recipe");
        return new RecipeResultNode(recipeResult);
    }

    private RecipeResultNode createCommandFailedRecipe()
    {
        RecipeResult recipeResult = new RecipeResult(null);

        recipeResult.commence(new File("/complex/recipe/output/dir"));
        recipeResult.add(createComplexCommand());
        recipeResult.add(createComplexCommand());
        recipeResult.add(createFailedCommand());
        recipeResult.complete();
        RecipeResultNode node = new RecipeResultNode(recipeResult);
        node.setHost("[master]");

        return node;
    }

    private RecipeResultNode createWarningFeaturesRecipe()
    {
        RecipeResult recipeResult = new RecipeResult(null);

        recipeResult.commence(new File("/complex/recipe/output/dir"));
        recipeResult.add(createComplexCommand());
        recipeResult.add(createWarningFeaturesCommand());
        recipeResult.add(createComplexCommand());
        recipeResult.complete();
        RecipeResultNode node = new RecipeResultNode(recipeResult);
        node.setHost("[master]");

        return node;
    }

    private RecipeResultNode createErrorFeaturesRecipe()
    {
        RecipeResult recipeResult = new RecipeResult(null);

        recipeResult.commence(new File("/complex/recipe/output/dir"));
        recipeResult.add(createComplexCommand());
        recipeResult.add(createWarningFeaturesCommand());
        recipeResult.add(createComplexCommand());
        recipeResult.add(createErrorFeaturesCommand());
        recipeResult.complete();
        RecipeResultNode node = new RecipeResultNode(recipeResult);
        node.setHost("[master]");

        return node;
    }

    private CommandResult createComplexCommand()
    {
        CommandResult result = new CommandResult("complex command");
        result.commence(new File("/complex/command/output/dir"));
        result.getProperties().put("command line", "/usr/local/bin/make -f my/path/to/Makefile build");
        result.getProperties().put("exit code", "0");
        result.addArtifact(createInfoArtifact("command output", "output.txt"));
        result.addArtifact(createSimpleArtifact("deeply nested", "this/file/is/nested/several/dirs/down"));
        result.addArtifact(createSimpleArtifact("junit report", "tests/junit.html"));
        result.complete();
        return result;
    }

    private CommandResult createInProgressCommand()
    {
        CommandResult result = new CommandResult("in progress command");
        result.commence(new File("wowsers"));
        return result;
    }

    private CommandResult createPendingCommand()
    {
        return new CommandResult("pending command");
    }

    private CommandResult createFailedCommand()
    {
        CommandResult result = new CommandResult("complex command");
        result.commence(new File("/complex/command/output/dir"));
        result.getProperties().put("command line", "/usr/local/bin/make -f my/path/to/Makefile build");
        result.getProperties().put("exit code", "1");
        result.failure("Command exited with code '1'");
        result.complete();
        return result;
    }

    private CommandResult createWarningFeaturesCommand()
    {
        CommandResult result = new CommandResult("complex command");
        result.commence(new File("/complex/command/output/dir"));
        result.getProperties().put("command line", "/usr/local/bin/make -f my/path/to/Makefile build");
        result.getProperties().put("exit code", "0");
        result.addArtifact(createInfoArtifact("command output", "output.txt"));
        result.addArtifact(createWarningArtifact("warnified!", "this/file/is/nested/several/dirs/down"));
        result.addArtifact(createSimpleArtifact("junit report", "tests/junit.html"));
        result.complete();
        return result;
    }

    private CommandResult createErrorFeaturesCommand()
    {
        CommandResult result = new CommandResult("complex command");
        result.commence(new File("/complex/command/output/dir"));
        result.getProperties().put("command line", "/usr/local/bin/make -f my/path/to/Makefile build");
        result.getProperties().put("exit code", "0");
        result.addArtifact(createInfoArtifact("command output", "output.txt"));
        result.addArtifact(createWarningArtifact("warnings here", "this/file/is/nested/several/dirs/down"));
        result.addArtifact(createErrorArtifact("errors be here", "this/file/is/nested/several/dirs/down"));
        result.addArtifact(createSimpleArtifact("junit report", "tests/junit.html"));
        result.complete();
        return result;
    }

    private StoredArtifact createInfoArtifact(String name, String filename)
    {
        StoredArtifact artifact = createSimpleArtifact(name, filename);
        artifact.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        artifact.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        artifact.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        artifact.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        artifact.addFeature(new Feature(Feature.Level.INFO, "this, on the other hand, is a useless piece of information that is just here to take up a whole lot of space"));
        return artifact;
    }

    private StoredArtifact createWarningArtifact(String name, String filename)
    {
        StoredArtifact artifact = createSimpleArtifact(name, filename);
        artifact.addFeature(new Feature(Feature.Level.WARNING, "this could be bad"));
        artifact.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        artifact.addFeature(new Feature(Feature.Level.WARNING, "watch out, behind you!"));
        artifact.addFeature(new Feature(Feature.Level.INFO, "this, on the other hand, is a useless piece of information that is just here to take up a whole lot of space"));
        return artifact;
    }

    private StoredArtifact createErrorArtifact(String name, String filename)
    {
        StoredArtifact artifact = createSimpleArtifact(name, filename);
        artifact.addFeature(new Feature(Feature.Level.ERROR, "dude, i can't help you now"));
        artifact.addFeature(new Feature(Feature.Level.ERROR, "fatal a saurus"));
        artifact.addFeature(new Feature(Feature.Level.ERROR, "this aint good"));
        artifact.addFeature(new Feature(Feature.Level.WARNING, "this could be bad"));
        artifact.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        artifact.addFeature(new Feature(Feature.Level.WARNING, "watch out, behind you!"));
        artifact.addFeature(new Feature(Feature.Level.INFO, "this, on the other hand, is a useless piece of information that is just here to take up a whole lot of space"));
        return artifact;
    }

    private StoredArtifact createSimpleArtifact(String name, String filename)
    {
        FileArtifact file = new FileArtifact(name, new File(filename));
        file.setTitle(name);
        file.setType("text/plain");
        return new StoredArtifact(file, filename);
    }
}
