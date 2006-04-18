/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.test;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.SlaveDao;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.util.logging.Logger;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class SetupDummyBuilds implements Runnable
{
    private static Logger LOG = Logger.getLogger(SetupDummyBuilds.class);

    private MasterBuildPaths masterBuildPaths;
    private SlaveDao slaveDao;
    private ProjectDao projectDao;
    private BuildResultDao buildResultDao;
    private UserDao userDao;
    private ConfigurationManager configManager;
    private Slave slave;
    private static final int BUILDS_IN_LONG_HISTORY_PROJECT = 100;

    public void run()
    {
        Project project;

        masterBuildPaths = new MasterBuildPaths(configManager);

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

            project = setupProject("medium history");
            for (long i = 1; i <= 34; i++)
            {
                createComplexSuccess(project, i);
            }

            project = setupProject("long history");
            for (long i = 1; i <= BUILDS_IN_LONG_HISTORY_PROJECT; i++)
            {
                double x = Math.random();
                if (x < 0.1)
                {
                    createCommandFailure(project, i);
                }
                else if (x < 0.2)
                {
                    createBuildError(project, i);
                }
                else if (x < 0.3)
                {
                    createWarningFeatures(project, i);
                }
                else
                {
                    createComplexSuccess(project, i);
                }
            }

            project = setupProject("terminating build");
            createTerminatingBuild(project);

            project = setupProject("test errors");
            createTestErrors(project);

            setupUsers(project);
            createLogMessages();
        }
    }

    private void createLogMessages()
    {
        for(int i = 0; i < 100; i++)
        {
            LOG.warning(String.format("%03d: some goon is filling your buffer", i));
        }

        LOG.debug("some debug message");
        LOG.fine("a fine message");
        LOG.warning("a warning message");
        LOG.severe("a severe message");
        LOG.severe("a longer severe message a longer severe message a longer severe message a longer severe message a longer severe message a longer severe message a longer severe message");
        LOG.warning("a warning:\n    formatted messages\n    may be closer than expected");

        try
        {
            throwMeSomething("with a message like this");
        }
        catch(RuntimeException e)
        {
            LOG.warning("got a throwable", e);
        }

        try
        {
            throwMeSomething("with a message like this");
        }
        catch(RuntimeException e)
        {
            LOG.error("testing out the error method on our own custom logger too to see if it is any different to using the severe method on a regular logger (it shouldn't be, it is just an alias!)", e);
        }
    }

    private void throwMeSomething(String s)
    {
        throwMeSomethingDeep(s);
    }

    private void throwMeSomethingDeep(String s)
    {
        throwMeSomethingDeeper(s);
    }

    private void throwMeSomethingDeeper(String s)
    {
        throw new RuntimeException(s);
    }

    private void setupSlave()
    {
        slave = new Slave("local slave", "localhost", 8090);
        slaveDao.save(slave);
    }

    private void setupUsers(Project project)
    {
        User user = new User("jsankey", "Jason Sankey");
        user.setPassword("password");
        user.setEnabled(true);
        user.add(GrantedAuthority.USER);
        user.add(GrantedAuthority.ADMINISTRATOR);

        ContactPoint contactPoint = new EmailContactPoint("jsankey@gmail.com");
        contactPoint.setName("gmail");
        Subscription subscription = new Subscription(project, contactPoint);
        contactPoint.add(subscription);
        user.add(contactPoint);
        userDao.save(user);
    }

    private Project setupProject(String name)
    {
        Project project = new Project(name, "A test project with a decently long description to test wrapping etc.");
        project.setPulseFileDetails(new CustomPulseFileDetails("pulse.xml"));

        P4 scm = new P4();
        scm.setPort(":1666");
        scm.setUser("jsankey");
        scm.setClient("pulse");
        project.setScm(scm);

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

    private void createComplexSuccess(Project project, long number)
    {
        BuildResult result = new BuildResult(project, getSpec(project), number);
        buildResultDao.save(result);

        List<Changelist> changelists = new LinkedList<Changelist>();

        NumericalRevision userRevision = new NumericalRevision(101);
        userRevision.setAuthor("jason");
        userRevision.setComment("a short comment");
        userRevision.setDate(new Date(System.currentTimeMillis() - 100000));
        Changelist list = new Changelist(userRevision);
        list.addChange(new Change("/home/jsankey/some/normal/file", "11", Change.Action.EDIT));
        list.addChange(new Change("/home/jsankey/some/other/file", "10", Change.Action.EDIT));
        list.addChange(new Change("/home/jsankey/a/silly/file/with/a/very/very/long/name/to/really/make/life/difficult/for/the/poor/little/UI/lets/see/if/it/gets/wrapped/shall/we", "1", Change.Action.BRANCH));
        list.addChange(new Change("/home/jsankey/some/branched/file", "1", Change.Action.BRANCH));
        list.addChange(new Change("/home/jsankey/some/branched/file2", "1", Change.Action.BRANCH));
        list.addChange(new Change("/home/jsankey/some/branched/file3", "1", Change.Action.BRANCH));
        list.addChange(new Change("/home/jsankey/some/branched/file4", "1", Change.Action.BRANCH));
        list.addProjectId(result.getProject().getId());
        list.addResultId(result.getId());
        changelists.add(list);

        userRevision = new NumericalRevision(9101);
        userRevision.setAuthor("jsankey");
        userRevision.setComment("a very long comment including\nnewlines like that one you just saw dude, not really very friendly at all");
        userRevision.setDate(new Date(System.currentTimeMillis() - 10000));
        list = new Changelist(userRevision);
        list.addChange(new Change("/home/jsankey/some/branched/file/with/a/very/long/filename/to/test/the/display/handling/of/such/things", "1", Change.Action.BRANCH));
        list.addProjectId(result.getProject().getId());
        list.addResultId(result.getId());
        changelists.add(list);

        userRevision = new NumericalRevision(9103);
        userRevision.setAuthor("jsankey");
        userRevision.setComment("short and sweet");
        userRevision.setDate(new Date(System.currentTimeMillis() - 9000));
        list = new Changelist(userRevision);
        list.addChange(new Change("/home/jsankey/some/file", "120", Change.Action.BRANCH));
        list.addProjectId(result.getProject().getId());
        list.addResultId(result.getId());
        changelists.add(list);

        BuildScmDetails scmDetails = new BuildScmDetails(new NumericalRevision(16672), changelists);
        result.setScmDetails(scmDetails);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createComplexRecipe("root recipe");
        RecipeResultNode childNode = createComplexRecipe("child recipe");
        rootResultNode.addChild(childNode);
        result.getRoot().addChild(rootResultNode);
        result.complete();
        buildResultDao.save(result);
    }

    private void createComplexSuccess(Project project)
    {
        createComplexSuccess(project, 1);
    }

    private void createInProgress(Project project)
    {
        BuildResult result = new BuildResult(project, getSpec(project), 1111);

        result.commence(new File("test"));
        RecipeResultNode node = createInProgressRecipe();
        result.getRoot().addChild(node);
        buildResultDao.save(result);
    }

    private void createPendingRecipes(Project project)
    {
        BuildResult result = new BuildResult(project, getSpec(project), 666);

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
        createCommandFailure(project, 10000);
    }

    private void createCommandFailure(Project project, long id)
    {
        BuildResult result = new BuildResult(project, getSpec(project), id);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createCommandFailedRecipe();
        RecipeResultNode childNode = createCommandFailedRecipe();
        rootResultNode.addChild(childNode);
        result.getRoot().addChild(rootResultNode);
        result.failure("my recipe failed me");
        result.complete();
        buildResultDao.save(result);
    }

    private void createBuildError(Project project, long id)
    {
        BuildResult result = new BuildResult(project, getSpec(project), id);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createComplexRecipe("root recipe");
        result.getRoot().addChild(rootResultNode);
        result.error("something real bad happened");
        result.complete();
        buildResultDao.save(result);
    }

    private void createWarningFeatures(Project project)
    {
        createWarningFeatures(project, 10000);
    }

    private void createWarningFeatures(Project project, long id)
    {
        BuildResult result = new BuildResult(project, getSpec(project), id);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createWarningFeaturesRecipe();
        RecipeResultNode childNode = createComplexRecipe("root recipe");
        rootResultNode.addChild(childNode);
        result.getRoot().addChild(rootResultNode);
        result.complete();
        buildResultDao.save(result);
    }

    private void createErrorFeatures(Project project)
    {
        BuildResult result = new BuildResult(project, getSpec(project), 10000);
        buildResultDao.save(result);
        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode rootResultNode = createErrorFeaturesRecipe(project, result);
        RecipeResultNode childNode = createComplexRecipe("child recipe");
        rootResultNode.addChild(childNode);
        result.getRoot().addChild(rootResultNode);
        result.complete();
        buildResultDao.save(result);
    }

    private void createTerminatingBuild(Project project)
    {
        BuildResult result = new BuildResult(project, getSpec(project), 101);

        result.commence(new File("/complex/build/output/dir"));
        RecipeResultNode node = createTerminatingRecipe();
        result.getRoot().addChild(node);
        result.terminate(true);
        buildResultDao.save(result);
    }

    private void createTestErrors(Project project)
    {
        BuildResult result = new BuildResult(project, getSpec(project), 666);

        buildResultDao.save(result);
        result.commence(System.currentTimeMillis());
        RecipeResultNode resultNode = createTestErrorsRecipe(project, result);
        result.getRoot().addChild(resultNode);
        result.failure("recipe :: " + resultNode.getResult().getRecipeNameSafe() + " :: some tests broken");
        result.complete();
        buildResultDao.save(result);
    }

    private String getSpec(Project project)
    {
        return project.getBuildSpecifications().get(0).getName();
    }

    private RecipeResultNode createComplexRecipe(String name)
    {
        RecipeResult recipeResult = new RecipeResult(name);

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

    private RecipeResultNode createTerminatingRecipe()
    {
        RecipeResult recipeResult = new RecipeResult(null);

        recipeResult.commence(new File("/complex/recipe/output/dir"));
        recipeResult.add(createComplexCommand());
        recipeResult.add(createTerminatingCommand());
        recipeResult.terminate(true);
        RecipeResultNode node = new RecipeResultNode(recipeResult);
        node.setHost(null);
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

    private RecipeResultNode createErrorFeaturesRecipe(Project project, BuildResult buildResult)
    {
        RecipeResult recipeResult = new RecipeResult(null);
        buildResultDao.save(recipeResult);
        File recipeDir = masterBuildPaths.getRecipeDir(project, buildResult, recipeResult.getId());
        recipeResult.commence(recipeDir);
        recipeResult.add(createComplexCommand());
        recipeResult.add(createWarningFeaturesCommand());
        recipeResult.add(createComplexCommand());
        recipeResult.add(createErrorFeaturesCommand(4, recipeDir));
        recipeResult.complete();
        RecipeResultNode node = new RecipeResultNode(recipeResult);
        node.setHost("[master]");

        return node;
    }

    private RecipeResultNode createTestErrorsRecipe(Project project, BuildResult buildResult)
    {
        RecipeResult recipeResult = new RecipeResult(null);
        buildResultDao.save(recipeResult);
        File recipeDir = masterBuildPaths.getRecipeDir(project, buildResult, recipeResult.getId());
        recipeResult.commence(recipeDir);
        CommandResult command = createTestErrorsCommand(0, recipeDir);
        recipeResult.add(command);
        recipeResult.failure("command :: " + command.getCommandName() + " :: some tests broken");
        recipeResult.complete();
        return new RecipeResultNode(recipeResult);
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

    private CommandResult createTerminatingCommand()
    {
        CommandResult result = new CommandResult("in progress command");
        result.commence(new File("wowsers"));
        result.terminate(true);
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
        result.failure("Command 'command/line/here' exited with code '1'");
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

    private CommandResult createErrorFeaturesCommand(int index, File recipeDir)
    {
        CommandResult result = new CommandResult("error features command");
        File commandDir = new File(recipeDir, RecipeProcessor.getCommandDirName(index, result));
        File outputDir = new File(commandDir, "output");
        result.commence(outputDir);
        result.addArtifact(createInfoArtifact("command output", "output.txt"));
        result.addArtifact(createWarningArtifact("warnings here", "this/file/is/nested/several/dirs/down"));
        result.addArtifact(createErrorArtifact(outputDir, "errors be here", "errors.txt"));
        result.addArtifact(createLargeArtifact(outputDir, "large errors", "large.txt"));
        result.addArtifact(createSimpleArtifact("junit report", "tests/junit.html"));
        result.addArtifact(createMultifileArtifact(outputDir, "multi ball"));
        result.complete();
        return result;
    }

    private CommandResult createTestErrorsCommand(int index, File recipeDir)
    {
        CommandResult result = new CommandResult("test errors command");
        File commandDir = new File(recipeDir, RecipeProcessor.getCommandDirName(index, result));
        File outputDir = new File(commandDir, "output");
        result.commence(outputDir);
        result.addArtifact(createTestErrorsArtifact(outputDir, "test errors artifact", "errors.txt"));
        result.failure("Some tests broken");
        result.complete();
        return result;
    }

    private StoredArtifact createInfoArtifact(String name, String filename)
    {
        StoredArtifact artifact = createSimpleArtifact(name, filename);
        StoredFileArtifact file = artifact.getFile();
        addInfoFeatures(file);
        return artifact;
    }

    private void addInfoFeatures(StoredFileArtifact file)
    {
        file.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information\nwith a newline in the middle"));
        file.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        file.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        file.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        file.addFeature(new Feature(Feature.Level.INFO, "this, on the other hand, is a useless piece of information that is just here to take up a whole lot of space"));
    }

    private StoredArtifact createWarningArtifact(String name, String filename)
    {
        StoredArtifact artifact = createSimpleArtifact(name, filename);
        StoredFileArtifact file = artifact.getFile();
        file.addFeature(new Feature(Feature.Level.WARNING, "this could be bad"));
        file.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        file.addFeature(new Feature(Feature.Level.WARNING, "watch out, behind you!"));
        file.addFeature(new Feature(Feature.Level.INFO, "this, on the other hand, is a useless piece of information that is just here to take up a whole lot of space"));
        return artifact;
    }

    private StoredArtifact createErrorArtifact(File outputDir, String name, String filename)
    {
        StoredArtifact artifact = createSimpleArtifact(name, filename);
        StoredFileArtifact file = artifact.getFile();
        File dir = new File(outputDir, name);
        dir.mkdirs();

        try
        {
            File root = PulseTestCase.getPulseRoot();
            File dummy = new File(root, FileSystemUtils.composeFilename("master", "src", "test", "com", "zutubi", "pulse", "test", "dummyArtifactFile.txt"));
            File artifactFile = new File(dir, filename);
            IOUtils.copyFile(dummy, artifactFile);
            findFeatures(file, artifactFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return artifact;
    }

    private StoredArtifact createLargeArtifact(File outputDir, String name, String filename)
    {
        StoredArtifact artifact = createSimpleArtifact(name, filename);
        StoredFileArtifact file = artifact.getFile();
        File dir = new File(outputDir, name);
        dir.mkdirs();

        try
        {
            File root = PulseTestCase.getPulseRoot();
            File artifactFile = new File(dir, filename);
            FileWriter writer = new FileWriter(artifactFile);
            for(int i = 0; i < 30000; i++)
            {
                if(Math.random() < 0.002)
                {
                    writer.write("Error: ");
                }
                writer.write("this is a test artficact with lines that are of a reasonable length and a large number, thus allowing us to test waht happens when we decorate it\n");
            }
            writer.close();
            findFeatures(file, artifactFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return artifact;
    }

    private StoredArtifact createTestErrorsArtifact(File outputDir, String name, String filename)
    {
        StoredArtifact artifact = createSimpleArtifact(name, filename);
        StoredFileArtifact file = artifact.getFile();

        TestSuiteResult suite = new TestSuiteResult("suite one");
        suite.add(new TestCaseResult("pass1", 100));
        suite.add(new TestCaseResult("pass2", 10000));
        suite.add(new TestCaseResult("fail1", 1, TestCaseResult.Status.FAILURE, "fail1 failure message"));
        file.addTest(suite);

        suite = new TestSuiteResult("suite two");
        suite.add(new TestCaseResult("2fail1", 1, TestCaseResult.Status.FAILURE, "2fail1 failure message"));
        suite.add(new TestCaseResult("2error1", 1, TestCaseResult.Status.ERROR, "2error1 error message"));
        file.addTest(suite);

        suite = new TestSuiteResult("complex suite");
        suite.add(new TestCaseResult("cpass1", 100));
        suite.add(new TestCaseResult("cpass2", 10000));
        suite.add(new TestCaseResult("cfail1", 1, TestCaseResult.Status.FAILURE, "a very long single line failure message that keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going"));
        suite.add(new TestCaseResult("cerror1", 1, TestCaseResult.Status.ERROR, "a formatted error message:\n    this is indented\n    and this is indented below it"));
        suite.add(new TestCaseResult("cerror2", 3245, TestCaseResult.Status.ERROR, "boring error"));
        suite.add(new TestCaseResult("cpass3", 1050));
        suite.add(new TestCaseResult("cpass4", 105005050));
        TestSuiteResult nestedSuite = new TestSuiteResult("nested suite");
        nestedSuite.add(new TestCaseResult("npass1", 11000));
        nestedSuite.add(new TestCaseResult("nfail1", 1, TestCaseResult.Status.FAILURE, "nfail1 failure message"));
        nestedSuite.add(new TestCaseResult("nerror1", 55067, TestCaseResult.Status.ERROR, "nerror1 failure message"));
        suite.add(nestedSuite);
        file.addTest(suite);

        return artifact;
    }

    private void findFeatures(StoredFileArtifact artifact, File file) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int i = 1;

        while ((line = br.readLine()) != null)
        {
            if (line.contains("error"))
            {
                artifact.addFeature(new PlainFeature(Feature.Level.ERROR, line, i));
            }

            if (line.contains("warning"))
            {
                artifact.addFeature(new PlainFeature(Feature.Level.WARNING, line, i - 1, i + 1, i));
            }

            if (line.contains("info"))
            {
                artifact.addFeature(new PlainFeature(Feature.Level.INFO, line.substring(4), i));
            }

            i++;
        }
    }

    private void addErrorFeatures(StoredFileArtifact file)
    {
        file.addFeature(new Feature(Feature.Level.ERROR, "dude, i can't help you now"));
        file.addFeature(new Feature(Feature.Level.ERROR, "fatal a saurus"));
        file.addFeature(new Feature(Feature.Level.ERROR, "this aint good"));
        file.addFeature(new Feature(Feature.Level.WARNING, "this could be bad"));
        file.addFeature(new Feature(Feature.Level.INFO, "this is a useful piece of information"));
        file.addFeature(new Feature(Feature.Level.WARNING, "watch out, behind you!"));
        file.addFeature(new Feature(Feature.Level.INFO, "this, on the other hand, is a useless piece of information that is just here to take up a whole lot of space"));
    }

    private StoredArtifact createSimpleArtifact(String name, String filename)
    {
        return new StoredArtifact(name, new StoredFileArtifact(name + "/" + filename, "text/plain"));
    }

    private StoredArtifact createMultifileArtifact(File outputDir, String name)
    {
        outputDir.mkdirs();
        StoredArtifact artifact = new StoredArtifact(name);
        artifact.setIndex("file01.html");
        for (int i = 0; i < 15; i++)
        {
            String filename = String.format("file%02d.html", i);
            StoredFileArtifact fileArtifact = new StoredFileArtifact(name + "/" + filename);
            if (i % 4 == 0)
            {
                addErrorFeatures(fileArtifact);
            }
            else if (i % 6 == 0)
            {
                addInfoFeatures(fileArtifact);
            }
            artifact.add(fileArtifact);

            File d = new File(outputDir, name);
            d.mkdir();
            File f = new File(d, filename);
            try
            {
                FileSystemUtils.createFile(f, String.format("<html><body><h1>this is " + filename + "</h1><a href=\"file%02d.html\">file%02d</a></body></html>", i + 1, i + 1));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return artifact;
    }

    public void setSlaveDao(SlaveDao slaveDao)
    {
        this.slaveDao = slaveDao;
    }

    public void setProjectDao(ProjectDao projectDao)
    {
        this.projectDao = projectDao;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void setConfigurationManager(ConfigurationManager configManager)
    {
        this.configManager = configManager;
    }
}
