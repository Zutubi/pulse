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
    private Project project;
    private Slave slave;
    private P4 scm;

    public void run()
    {
        slaveDao = (SlaveDao) ComponentContext.getBean("slaveDao");
        projectDao = (ProjectDao) ComponentContext.getBean("projectDao");
        buildResultDao = (BuildResultDao) ComponentContext.getBean("buildResultDao");

        if (projectDao.findAll().size() == 0)
        {
            setupSlave();
            setupProject();
            setupBuilds();
        }
    }

    private void setupSlave()
    {
        slave = new Slave("local slave", "localhost", 8090);
        slaveDao.save(slave);
    }

    private void setupProject()
    {
        project = new Project("test", "A test project with a decently long description to test wrapping etc.");
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
    }

    private void setupBuilds()
    {
        createComplexSuccess();
    }

    private void createComplexSuccess()
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

    private StoredArtifact createSimpleArtifact(String name, String filename)
    {
        FileArtifact file = new FileArtifact(name, new File(filename));
        file.setTitle(name);
        file.setType("text/plain");
        return new StoredArtifact(file, filename);
    }
}
