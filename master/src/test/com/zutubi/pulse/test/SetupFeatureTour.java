package com.zutubi.pulse.test;

import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.JUnitReportPostProcessor;
import com.zutubi.pulse.core.AntPostProcessor;
import com.zutubi.pulse.core.DirectoryArtifact;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.model.*;

import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;

/**
 */
public class SetupFeatureTour implements Runnable
{
    private static Logger LOG = Logger.getLogger(SetupFeatureTour.class);

    private MasterBuildPaths masterBuildPaths;
    private ProjectDao projectDao;
    private BuildResultDao buildResultDao;
    private UserDao userDao;
    private UserManager userManager;
    private ConfigurationManager configManager;

    private Project project;
    private BuildResult build;
    private RecipeResult recipe;
    private CommandResult command;
    private long buildNumber = 0;
    private int commandIndex = 0;

    private long revisions[] = {100, 102, 103, 109, 133, 150, 151, 152, 153, 155, 160, 177, 179, 180, 200, 201};
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
            "Improve acceptance testing reports."
    };

    int changeIndex = 0;

    public void run()
    {
        masterBuildPaths = new MasterBuildPaths(configManager);

        if (projectDao.findAll().size() == 0)
        {
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
            for (int i = 0; i < 54; i++)
            {
                successfulBuild();
            }
            testsFailedBuild();

            setupUsers(project);
            createLogMessages();
        }
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
        User user = new User("jsankey", "Jason Sankey");
        userManager.setPassword(user, "password");
        user.setEnabled(true);
        user.add(GrantedAuthority.USER);
        user.add(GrantedAuthority.ADMINISTRATOR);

        ContactPoint contactPoint = new EmailContactPoint("jason@zutubi.com");
        contactPoint.setName("zutubi mail");
        Subscription subscription = new Subscription(project, contactPoint);
        contactPoint.add(subscription);
        user.add(contactPoint);

        JabberContactPoint jabber = new JabberContactPoint();
        jabber.setName("jabber");
        jabber.setUsername("jason@jabber");
        subscription = new Subscription(project, contactPoint);
        subscription.setCondition("all changed or failed");
        contactPoint.add(subscription);
        user.add(contactPoint);

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
        BuildStage simpleStage = new BuildStage(new MasterBuildHostRequirements(), null);
        BuildSpecificationNode simpleNode = new BuildSpecificationNode(simpleStage);
        simpleSpec.getRoot().addChild(simpleNode);
        project.addBuildSpecification(simpleSpec);

        simpleSpec = new BuildSpecification("nightly");
        simpleSpec.setTimeout(120);
        simpleStage = new BuildStage(new MasterBuildHostRequirements(), "nightly-build");
        simpleNode = new BuildSpecificationNode(simpleStage);
        simpleSpec.getRoot().addChild(simpleNode);
        project.addBuildSpecification(simpleSpec);

        buildNumber = 0;

        projectDao.save(project);
        return project;
    }

    private void addBuildResult()
    {
        BuildResult previous = build;
        build = new BuildResult(project, "default", ++buildNumber);
        buildResultDao.save(build);

        recipe = new RecipeResult(null);
        buildResultDao.save(recipe);

        File buildDir = masterBuildPaths.getBuildDir(project, build);
        build.commence(buildDir);
        buildDir.mkdirs();
        try
        {
            IOUtils.copyFile(getDataFile("pulse.xml"), new File(buildDir, "pulse.xml"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        File recipeDir = masterBuildPaths.getRecipeDir(project, build, recipe.getId());
        recipe.commence(recipeDir);
        RecipeResultNode node = new RecipeResultNode(recipe);
        build.getRoot().addChild(node);

        addChanges(previous);

        commandIndex = 0;
        addCommandResult("bootstrap");
        completeCommandResult();
    }

    private void addChanges(BuildResult previous)
    {
        List<Changelist> changes = new LinkedList<Changelist>();

        while (Math.random() < 0.20)
        {
            String author = Math.random() > 0.3 ? "jsankey" : "dostermeier"; // ;)

            changes.add(createChange(previous, revisions[changeIndex], author, comments[changeIndex], (1000 - revisions[changeIndex]) * 600000, generateChangeFiles()));
            changeIndex++;
            if (changeIndex >= comments.length)
            {
                changeIndex = 0;
            }
        }

        BuildScmDetails details = new BuildScmDetails(new NumericalRevision(400), changes);
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
        recipe.complete();
        build.complete();
        buildResultDao.save(build);
    }

    private void addCommandResult(String name)
    {
        command = new CommandResult(name);
        File commandDir = new File(recipe.getOutputDir(), RecipeProcessor.getCommandDirName(commandIndex++, command));
        File outputDir = new File(commandDir, "output");
        command.commence(outputDir);
    }

    private void completeCommandResult()
    {
        recipe.add(command);
        command.complete();
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
        build.failure("Recipe [default] failed.");
        completeBuildResult();

        File f = getDataFile("base");
        try
        {
            FileSystemUtils.copyRecursively(f, new File(recipe.getOutputDir(), "base"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void addAntFailedArtifact()
    {
        try
        {
            File dummy = getDataFile("ant-failed.txt");
            StoredFileArtifact fileArtifact = addArtifact(dummy, "output.txt", "command output", "text/plain");
            AntPostProcessor pp = new AntPostProcessor();
            pp.process(new File(command.getOutputDir()), fileArtifact, command);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addFailedTestArtifact()
    {
        try
        {
            File dummy = getDataFile("junit-failed.xml");
            StoredFileArtifact fileArtifact = addArtifact(dummy, "TESTS-TestSuites.xml", "JUnit XML Report", "text/html");
            JUnitReportPostProcessor pp = new JUnitReportPostProcessor();
            pp.process(new File(command.getOutputDir()), fileArtifact, command);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addTestReportArtifact()
    {
        File dir = getDataFile("junit-report");
        DirectoryArtifact da = new DirectoryArtifact();
        da.setName("JUnit HTML Report");
        da.capture(command, dir, new File(command.getOutputDir()));
    }

    private File getDataFile(String name)
    {
        File root = PulseTestCase.getPulseRoot();
        return new File(root, FileSystemUtils.composeFilename("master", "src", "test", "com", "zutubi", "pulse", "test", name));
    }

    private StoredFileArtifact addArtifact(File from, String to, String name, String type)
    {
        File dir = new File(command.getOutputDir(), name);
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
            list.addChange(new Change(file, "3", Change.Action.EDIT));
        }

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

    public void setConfigurationManager(ConfigurationManager configManager)
    {
        this.configManager = configManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
