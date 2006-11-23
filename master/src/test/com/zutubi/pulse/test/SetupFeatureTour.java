package com.zutubi.pulse.test;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.committransformers.JiraCommitMessageTransformer;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.*;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class SetupFeatureTour implements Runnable
{
    private static Logger LOG = Logger.getLogger(SetupFeatureTour.class);

    private MasterBuildPaths masterBuildPaths;
    private ProjectDao projectDao;
    private BuildResultDao buildResultDao;
    private GroupDao groupDao;
    private UserDao userDao;
    private UserManager userManager;
    private MasterConfigurationManager configManager;
    private SlaveDao slaveDao;
    private CommitMessageTransformerDao commitMessageTransformerDao;
    private ChangelistDao changelistDao;
    private AgentManager agentManager;

    private Slave slave;
    private Project project;
    private BuildResult build;
    private RecipeResult recipes[];
    private CommandResult commands[];
    private long buildNumber = 0;
    private int commandIndex = 0;

    private String comments[] = {
            "Fixed tab indexes and text field sizes.",
            "CIB-315: Build specification triggering links shown to unauthorised users.",
            "Where for art thou, local build?.",
            "Fixed macro reference harder.",
            "Fixed macro reference.",
            "Added a recipe to run acceptance tests.",
            "Fixed default help base URL.",
            "Added license.edit string.",
            "Fixed typo.",
            "Update for release 1.0. Added 1.0 ivy files.",
            "Acceptance test case for enable RSS link option in General Config.",
            "CIB-291: browser now sees the rss feeds as an xml file.",
            "CIB-311: beefing up remote API.  Added project listing, latest build results, project state monitoring/changing and build triggering.",
            "CIB-304: Allow dashboard projects to be customised.  Added a configure link to the dashboard to allow users to select which projects they want displayed.",
            "CIB-290: Cron triggers: add improved inline documentation and usage examples.",
            "Improve acceptance testing reports.",
            "CIB-611: link project name on dashboard",
            "CIB-606: merge from branch 1.1.x. Fisheye timestamp is now in GMT",
            "Reload project and build spec when crossing thread boundary into build controller to prevent races between hibernate sessions.",
            "Remove the shell package, it is not used, and there is no intention of using it at this stage.\n" +
                    "Document the form FieldType and Descriptors.",
            "CIB-609: merge change from branch 1.1.x onto trunk.  This change allows wildcard characters to be used for artifact names",
            "CIB-608: Hang when svn server goes down.  Set socket timeout on svn sockets so reads don't block forever.",
            "CIB-608: Updated to new version of JavaSvn.",
            "CIB-610: Cleanup subversion working copies before update.",
            "CIB-619: Please display the real time a uit test takes.  Show milliseconds for any elapsed time < 1 sec.",
            "CIB-616: Improve maven 2 error detection on windows.",
            "Restructured packages for autoupdates.  Updated boot component so that it is configured via files (classpath and available commands).",
            "Update the annotations to allow specification of messageKey and defaultMessage for the validators.",
            "CIB-620: fixed the broken rss link on the recent projects panel.",
            "CIB-613, CIB-621: record the execution environment of a command",
            "CIB-622: Failed to delete agent: Constraint violation.  That was a little trickier than expected!",
            "CIB-623: Agent ping timeout is not logged.  Log more errors and show the agent ping error message on the status page.",
    };

    int changeIndex = 0;
    private int revision = 200;

    public void run()
    {
        masterBuildPaths = new MasterBuildPaths(configManager);

        if (projectDao.findAll().size() == 0)
        {
            setupSlaves();

            project = setupProject("ant", "Apache ant build tools");
            project.setUrl("http://ant.apache.org/");
            successfulBuild();
            successfulBuild();

            project = setupProject("make", "GNU variant of make.");
            project.setUrl("http://www.gnu.org/software/make/");
            successfulBuild();
            successfulBuild();
            successfulBuild();
            successfulBuild();

            project = setupProject("maven", "Apache maven build lord");
            project.setUrl("http://maven.apache.org/");
            successfulBuild();
            successfulBuild();
            successfulBuild();
            successfulBuild();
            successfulBuild();

            project = setupProject("pulse", "The pulse automated build server");
            project.setUrl("http://zutubi.com/products/pulse/");
            addBuildStage("remote", slave);
            for (int i = 0; i < 54; i++)
            {
                successfulBuild();
            }
            testsFailedBuild();

            setupUsers(project);
            createLogMessages();
            createCommitLinks();
        }
    }

    private void setupSlaves()
    {
        slave = new Slave("linux64", "sneezy", 8090);
        slaveDao.save(slave);
        agentManager.slaveAdded(slave.getId());
    }

    private void createLogMessages()
    {
        for (int i = 0; i < 100; i++)
        {
            SetupFeatureTour.LOG.warning(String.format("%03d: some goon is filling your buffer", i));
        }

        SetupFeatureTour.LOG.debug("some debug message");
        SetupFeatureTour.LOG.fine("a fine message");
        SetupFeatureTour.LOG.warning("a warning message");
        SetupFeatureTour.LOG.severe("a severe message");
        SetupFeatureTour.LOG.severe("a longer severe message a longer severe message a longer severe message a longer severe message a longer severe message a longer severe message a longer severe message");
        SetupFeatureTour.LOG.warning("a warning:\n    formatted messages\n    may be closer than expected");

        try
        {
            throwMeSomething("with a message like this");
        }
        catch (RuntimeException e)
        {
            SetupFeatureTour.LOG.warning("got a throwable", e);
        }

        try
        {
            throwMeSomething("with a message like this");
        }
        catch (RuntimeException e)
        {
            SetupFeatureTour.LOG.error("testing out the error method on our own custom logger too to see if it is any different to using the severe method on a regular logger (it shouldn't be, it is just an alias!)", e);
        }
    }

    private void createCommitLinks()
    {
        JiraCommitMessageTransformer jira = new JiraCommitMessageTransformer();
        jira.setName("Jira");
        jira.setUrl("http://jira.zutubi.com");
        commitMessageTransformerDao.save(jira);
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

    private void setupUsers(Project project)
    {
        Group admins = groupDao.findByName("administrators");
        Group projectAdmins = groupDao.findByName("project administrators");

        User user = new User("jsankey", "Jason Sankey");
        userManager.setPassword(user, "password");
        user.setEnabled(true);
        user.add(GrantedAuthority.USER);

        admins.addUser(user);
        groupDao.save(admins);
        projectAdmins.addUser(user);
        groupDao.save(projectAdmins);

        ContactPoint contactPoint = new EmailContactPoint("jason@zutubi.com");
        contactPoint.setName("zutubi mail");
        List<Project> projects = new LinkedList<Project>();
        projects.add(project);
        ProjectBuildSubscription subscription = new ProjectBuildSubscription(contactPoint, "html-email", "true");
        subscription.setProjects(projects);
        contactPoint.add(subscription);
        user.add(contactPoint);

        JabberContactPoint jabber = new JabberContactPoint();
        jabber.setName("jabber");
        jabber.setUsername("jason@jabber");
        projects = new LinkedList<Project>();
        projects.add(project);
        subscription = new ProjectBuildSubscription(jabber, "simple-instant-message", "true");
        subscription.setProjects(projects);
        subscription.setCondition("all changed or failed");
        jabber.add(subscription);
        user.add(jabber);

        userDao.save(user);
    }

    private Project setupProject(String name, String description)
    {
        Project project = new Project(name, description);
        project.setPulseFileDetails(new VersionedPulseFileDetails("pulse.xml"));

        P4 scm = new P4();
        scm.setPort(":1666");
        scm.setUser("pulse");
        scm.setClient("pulse");
        project.setScm(scm);

        BuildSpecification simpleSpec = new BuildSpecification("default");
        BuildStage simpleStage = new BuildStage("default", new MasterBuildHostRequirements(), null);
        BuildSpecificationNode simpleNode = new BuildSpecificationNode(simpleStage);
        simpleSpec.getRoot().addChild(simpleNode);
        project.addBuildSpecification(simpleSpec);

        simpleSpec = new BuildSpecification("nightly");
        simpleSpec.setTimeout(120);
        simpleStage = new BuildStage("default", new MasterBuildHostRequirements(), "nightly-build");
        simpleNode = new BuildSpecificationNode(simpleStage);
        simpleSpec.getRoot().addChild(simpleNode);
        project.addBuildSpecification(simpleSpec);

        buildNumber = 0;

        projectDao.save(project);
        return project;
    }

    private void addBuildStage(String name, Slave slave)
    {
        BuildSpecification spec = project.getBuildSpecifications().get(0);
        BuildStage stage = new BuildStage(name, new SlaveBuildHostRequirements(slave), null);
        BuildSpecificationNode node = new BuildSpecificationNode(stage);
        node.addResourceRequirement(new ResourceRequirement("ant", "1.6.5"));
        spec.getRoot().addChild(node);
        projectDao.save(project);
    }

    private void addBuildResult()
    {
        BuildResult previous = build;
        BuildSpecification spec = project.getBuildSpecifications().get(0);
        build = new BuildResult(new TriggerBuildReason("scm trigger"), project, spec, ++buildNumber);
        buildResultDao.save(build);

        project.setNextBuildNumber(buildNumber);
        projectDao.save(project);

        int i = 0;
        recipes = new RecipeResult[spec.getRoot().getChildren().size()];

        for(BuildSpecificationNode specNode: spec.getRoot().getChildren())
        {
            recipes[i] = new RecipeResult(null);
            buildResultDao.save(recipes[i]);

            File recipeDir = masterBuildPaths.getRecipeDir(build, recipes[i].getId());
            recipes[i].commence();
            recipes[i].setAbsoluteOutputDir(configManager.getDataDirectory(), recipeDir);
            RecipeResultNode node = new RecipeResultNode(specNode.getStage().getPname(), recipes[i]);
            node.setHost(specNode.getStage().getHostRequirements().getSummary());
            build.getRoot().addChild(node);
            i++;
        }

        File buildDir = masterBuildPaths.getBuildDir(build);
        build.commence();
        build.setAbsoluteOutputDir(configManager.getDataDirectory(), buildDir);
        buildDir.mkdirs();
        try
        {
            IOUtils.copyFile(getDataFile("pulse.xml"), new File(buildDir, "pulse.xml"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        addChanges(previous);

        commandIndex = 0;
        addCommandResult("bootstrap");
        completeCommandResult();
    }

    private void addChanges(BuildResult previous)
    {
        List<Changelist> changes = new LinkedList<Changelist>();

        while (Math.random() > 0.20)
        {
            String author = Math.random() > 0.3 ? "jsankey" : "dostermeier"; // ;)

            changes.add(createChange(previous, revision++, author, comments[changeIndex], (1000 - revision) * 600000, generateChangeFiles()));
            changeIndex++;
            if (changeIndex >= comments.length)
            {
                changeIndex = 0;
            }
        }

        BuildScmDetails details = new BuildScmDetails(new NumericalRevision(400));
        build.setScmDetails(details);
    }

    private String[] generateChangeFiles()
    {
        return new String[]{
                "src/java/com/zutubi/pulse/SourceFile.java",
                "src/java/com/zutubi/pulse/AnotherSourceFile.java",
                "src/java/com/zutubi/pulse/Foo.java",
                "src/java/com/zutubi/pulse/Bar.java",
                "src/java/com/zutubi/pulse/Baz.java",
                "src/java/com/zutubi/pulse/Quux.java",
                "src/java/com/zutubi/pulse/Quuux.java",
                "src/java/com/zutubi/pulse/Quuuux.java",
        };
    }

    private void completeBuildResult()
    {
        for(RecipeResult recipe: recipes)
        {
            recipe.complete();
        }
        build.complete();
        buildResultDao.save(build);
    }

    private void addCommandResult(String name)
    {
        int i = 0;
        commands = new CommandResult[recipes.length];
        for(RecipeResult recipe: recipes)
        {
            commands[i] = new CommandResult(name);
            File commandDir = new File(recipe.getAbsoluteOutputDir(configManager.getDataDirectory()), RecipeProcessor.getCommandDirName(commandIndex, commands[i]));
            File outputDir = new File(commandDir, "output");
            commands[i].commence();
            commands[i].setAbsoluteOutputDir(configManager.getDataDirectory(), outputDir);
            i++;
        }

        commandIndex++;
    }

    private void completeCommandResult()
    {
        for (int i = 0; i < recipes.length; i++)
        {
            recipes[i].add(commands[i]);
            commands[i].complete();
        }
    }

    private void successfulBuild()
    {
        addBuildResult();
        addCommandResult("build");
        completeCommandResult();
        completeBuildResult();
    }

    private void testsFailedBuild()
    {
        addBuildResult();
        addCommandResult("build");
        addAntFailedArtifact();
        addFailedTestArtifact();
        addTestReportArtifact();
        completeCommandResult();

        for(BuildSpecificationNode node: project.getBuildSpecifications().get(0).getRoot().getChildren())
        {
            build.failure("Stage " + node.getStage().getName() + " failed.");
        }

        completeBuildResult();

        File f = getDataFile("base");
        try
        {
            for (RecipeResult recipe: recipes)
            {
                FileSystemUtils.copy(new File(recipe.getAbsoluteOutputDir(configManager.getDataDirectory()), "base"), f);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void addAntFailedArtifact()
    {
        AntPostProcessor pp = new AntPostProcessor();
        File dummy = getDataFile("ant-failed.txt");

        for (CommandResult command: commands)
        {
            try
            {
                StoredFileArtifact fileArtifact = addArtifact(command, dummy, "output.txt", "command output", "text/plain");
                TestSuiteResult testResults = new TestSuiteResult();
                CommandContext context = new CommandContext(null, command.getAbsoluteOutputDir(configManager.getDataDirectory()), testResults);
                pp.process(fileArtifact, command, context);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void addFailedTestArtifact()
    {
        File dummy = getDataFile("junit-failed.xml");
        JUnitReportPostProcessor pp = new JUnitReportPostProcessor();

        for (int i = 0; i < commands.length; i++)
        {
            CommandResult command = commands[i];

            try
            {
                StoredFileArtifact fileArtifact = addArtifact(command, dummy, "TESTS-TestSuites.xml", "JUnit XML Report", "text/html");
                TestSuiteResult testResults = new TestSuiteResult();
                CommandContext context = new CommandContext(null, command.getAbsoluteOutputDir(configManager.getDataDirectory()), testResults);
                pp.process(fileArtifact, command, context);

                recipes[i].setTestSummary(testResults.getSummary());
                TestSuitePersister persister = new TestSuitePersister();
                File testDir = new File(recipes[i].getAbsoluteOutputDir(configManager.getDataDirectory()), RecipeResult.TEST_DIR);
                testDir.mkdirs();
                persister.write(testResults, testDir);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void addTestReportArtifact()
    {
        File dir = getDataFile("junit-report");

        for (CommandResult command: commands)
        {
            DirectoryArtifact da = new DirectoryArtifact();
            da.setName("JUnit HTML Report");
            RecipePaths paths = new SimpleRecipePaths(dir, null);
            TestSuiteResult tests = new TestSuiteResult();
            CommandContext context = new CommandContext(paths, command.getAbsoluteOutputDir(configManager.getDataDirectory()), tests);
            da.capture(command, context);
        }
    }

    private File getDataFile(String name)
    {
        File root = PulseTestCase.getPulseRoot();
        return new File(root, FileSystemUtils.composeFilename("master", "src", "test", "com", "zutubi", "pulse", "test", name));
    }

    private StoredFileArtifact addArtifact(CommandResult command, File from, String to, String name, String type)
    {
        File dir = new File(command.getAbsoluteOutputDir(configManager.getDataDirectory()), name);
        File file = new File(dir, to);
        StoredFileArtifact fileArtifact = new StoredFileArtifact(name + "/" + to, type);
        StoredArtifact artifact = new StoredArtifact(name, fileArtifact);

        dir.mkdirs();

        try
        {
            IOUtils.copyFile(from, file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        command.addArtifact(artifact);
        return fileArtifact;
    }

    private Changelist createChange(BuildResult previous, long revision, String author, String comment, long ago, String... files)
    {
        NumericalRevision rev = new NumericalRevision(revision);
        rev.setAuthor(author);
        rev.setComment(comment);
        rev.setDate(new Date(System.currentTimeMillis() - ago));

        Changelist list = new Changelist(":1666", rev);
        list.addProjectId(project.getId());
        list.addResultId(build.getId());
        if(previous != null)
        {
            list.addProjectId(previous.getProject().getId());
            list.addResultId(previous.getId());
        }

        for (String file : files)
        {
            list.addChange(new Change(file, new NumericalFileRevision(3), Change.Action.EDIT));
        }

        changelistDao.save(list);
        return list;
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

    public void setConfigurationManager(MasterConfigurationManager configManager)
    {
        this.configManager = configManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setSlaveDao(SlaveDao slaveDao)
    {
        this.slaveDao = slaveDao;
    }

    public void setCommitMessageTransformerDao(CommitMessageTransformerDao commitMessageTransformerDao)
    {
        this.commitMessageTransformerDao = commitMessageTransformerDao;
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }

    public void setGroupDao(GroupDao groupDao)
    {
        this.groupDao = groupDao;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
