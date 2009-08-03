package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfigurationRevisionOptionProvider;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A base test case for the dependencies system that helps with the configuration
 * and setup of the numerous dependency cases.
 */
public abstract class BaseDependenciesAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String PROPERTY_CREATE_LIST = "create.list";
    private static final String PROPERTY_EXPECTED_LIST = "expected.list";
    private static final String PROPERTY_NOT_EXPECTED_LIST = "not.expected.list";
    protected Repository repository;

    protected int triggerSuccessfulBuild(Project project) throws Exception
    {
        AntBuildConfiguration build = new AntBuildConfiguration();
        for (Recipe recipe : project.getRecipes())
        {
            for (Artifact artifact : recipe.getArtifacts())
            {
                build.addFilesToCreate("build/" + artifact.getName() + "." + artifact.getExtension());
            }
        }

        return triggerSuccessfulBuild(project, build);
    }

    protected int triggerSuccessfulBuild(Project project, AntBuildConfiguration build) throws Exception
    {
        int buildNumber = triggerBuild(project, build);
        assertEquals(ResultState.SUCCESS, getBuildStatus(project.getName(), buildNumber));
        return buildNumber;
    }

    protected int triggerSuccessfulBuild(Project project, AntBuildConfiguration build, String status) throws Exception
    {
        int buildNumber = triggerBuild(project, build, status);
        assertEquals(ResultState.SUCCESS, getBuildStatus(project.getName(), buildNumber));
        return buildNumber;
    }

    private int triggerBuild(Project project, AntBuildConfiguration build, String status) throws Exception
    {
        triggerBuildCommon(project, build);
        return xmlRpcHelper.runBuild(project.getName(), com.zutubi.util.CollectionUtils.asPair("status", (Object)status));
    }

    protected int triggerBuild(Project project, AntBuildConfiguration build) throws Exception
    {
        triggerBuildCommon(project, build);
        return xmlRpcHelper.runBuild(project.getName());
    }

    private void triggerBuildCommon(Project project, AntBuildConfiguration build) throws Exception
    {
        // for each stage, set the necessary build properties.
        for (Stage stage : project.stages)
        {
            stage.getRecipe();
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_CREATE_LIST, build.getCreateList());
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_EXPECTED_LIST, build.getExpectedList());
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_NOT_EXPECTED_LIST, build.getNotExpectedList());
        }
    }

    protected void createProject(Project project) throws Exception
    {
        String target = "present not.present create";
        String args = "-Dcreate.list=\"${"+PROPERTY_CREATE_LIST+"}\" -Dpresent.list=\"${"+PROPERTY_EXPECTED_LIST+"}\" -Dnot.present.list=\"${"+PROPERTY_NOT_EXPECTED_LIST+"}\"";

        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("name", "build");
        antConfig.put("targets", target);
        antConfig.put("args", args);

        xmlRpcHelper.insertSingleCommandProject(project.getName(), ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.DEP_ANT_REPOSITORY), antConfig);

        setProjectOrganisation(project);
        configureDependencies(project);

        for (Recipe recipe : project.getRecipes())
        {
            ensureRecipeExists(project, recipe, antConfig);
            for (Artifact artifact : recipe.getArtifacts())
            {
                String command = "build";
                addArtifact(project, recipe.getName(), command, artifact.getName(), artifact.getExtension(), artifact.getArtifactPattern());
            }
        }

        for (Stage stage : project.stages)
        {
            // create stage.
            ensureStageExists(project, stage);

            // set blank default properties.
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_CREATE_LIST, "");
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_EXPECTED_LIST, "");
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_NOT_EXPECTED_LIST, "");

            // setup the rest of the properties configured for the stage.
            for (Map.Entry<String, String> entry : stage.properties.entrySet())
            {
                xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), entry.getKey(), entry.getValue());
            }
        }

        for (Dependency dependency : project.dependencies)
        {
            addDependency(project, dependency);
        }

        String triggersPath = com.zutubi.tove.type.record.PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project.name, "triggers");
        Hashtable<String, Object> trigger = xmlRpcHelper.createEmptyConfig(DependentBuildTriggerConfiguration.class);
        trigger.put("name", "dependency trigger");
        trigger.put("propagateStatus", project.isPropagateStatus());
        trigger.put("propagateVersion", project.isPropagateVersion());
        xmlRpcHelper.insertConfig(triggersPath, trigger);
    }

    private void setProjectOrganisation(Project project) throws Exception
    {
        if (TextUtils.stringSet(project.getOrg()))
        {
            String path = "projects/" + project.getName();
            Hashtable<String, Object> projectConfig = xmlRpcHelper.getConfig(path);
            projectConfig.put(Constants.Project.ORGANISATION, project.getOrg());
            xmlRpcHelper.saveConfig(path, projectConfig, true);
        }
    }

    private void ensureRecipeExists(Project project, Recipe recipe, Hashtable<String, Object> commandConfig) throws Exception
    {
        String recipePath = "projects/" + project.getName() + "/type/recipes/" + recipe.getName();
        if (!xmlRpcHelper.configPathExists(recipePath))
        {
            Hashtable<String, Object> recipeConfig = xmlRpcHelper.createDefaultConfig(RecipeConfiguration.class);
            recipeConfig.put(Constants.Project.MultiRecipeType.NAME, recipe.getName());

            Hashtable<String, Object> commands = new Hashtable<String, Object>();
            commands.put((String)commandConfig.get("name"), commandConfig);
            recipeConfig.put("commands", commands);

            xmlRpcHelper.insertConfig("projects/" + project.getName() + "/type/recipes", recipeConfig);
        }
    }

    public void ensureStageExists(Project project, Stage stage) throws Exception
    {
        // configure the default stage.
        String stagePath = "projects/" + project.getName() + "/stages/" + stage.getName();
        if (!xmlRpcHelper.configPathExists(stagePath))
        {
            Hashtable<String, Object> stageConfig = xmlRpcHelper.createDefaultConfig(BuildStageConfiguration.class);
            stageConfig.put(Constants.Project.Stage.NAME, stage.getName());
            stageConfig.put(Constants.Project.Stage.RECIPE, stage.getRecipe().getName());
            xmlRpcHelper.insertConfig("projects/" + project.getName() + "/stages", stageConfig);
        }
    }

    public void configureDependencies(Project project) throws Exception
    {
        // configure the default stage.
        String dependenciesPath = "projects/" + project.getName() + "/dependencies";
        Hashtable<String, Object> dependencies = xmlRpcHelper.getConfig(dependenciesPath);
        dependencies.put(com.zutubi.pulse.acceptance.Constants.Project.Dependencies.RETRIEVAL_PATTERN, project.retrievalPattern);
        if (TextUtils.stringSet(project.status))
        {
            dependencies.put(com.zutubi.pulse.acceptance.Constants.Project.Dependencies.STATUS, project.status);
        }
        if (TextUtils.stringSet(project.version))
        {
            dependencies.put(com.zutubi.pulse.acceptance.Constants.Project.Dependencies.VERSION, project.version);
        }
        xmlRpcHelper.saveConfig(dependenciesPath, dependencies, false);
    }

    private void addArtifact(Project project, String recipe, String command, String artifactName, String artifactExtension, String pattern) throws Exception
    {
        String artifactsPath = "projects/" + project.getName() + "/type/recipes/" + recipe + "/commands/" + command + "/outputs";

        Hashtable<String, Object> artifactData = xmlRpcHelper.createDefaultConfig(FileOutputConfiguration.class);
        artifactData.put("name", artifactName);
        artifactData.put("file", "build/" + artifactName + "." + artifactExtension);
        artifactData.put("publish", true);
        if (pattern != null)
        {
            artifactData.put("artifactPattern", pattern);
        }

        xmlRpcHelper.insertConfig(artifactsPath, artifactData);
    }

    private void addDependency(Project project, Dependency projectDependency) throws Exception
    {
        // configure the default stage.
        String projectDependenciesPath = "projects/" + project.getName() + "/dependencies";

        Hashtable<String, Object> projectDependencies = xmlRpcHelper.getConfig(projectDependenciesPath);
        if (!projectDependencies.containsKey("dependencies"))
        {
            projectDependencies.put("dependencies", new Vector<Hashtable<String, Object>>());
        }

        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> dependencies = (Vector<Hashtable<String, Object>>) projectDependencies.get("dependencies");

        List<String> revisionOptions = new DependencyConfigurationRevisionOptionProvider().getOptions(null, null, null);

        Hashtable<String, Object> dependency = xmlRpcHelper.createEmptyConfig(DependencyConfiguration.class);
        dependency.put("project", "projects/" + projectDependency.project.getName());
        if (revisionOptions.contains(projectDependency.revision))
        {
            dependency.put("revision", projectDependency.revision);
        }
        else
        {
            dependency.put("revision", DependencyConfiguration.REVISION_CUSTOM);
            dependency.put("customRevision", projectDependency.revision);
        }
        
        dependency.put("allStages", (projectDependency.stage == null));
        dependency.put("stages", asStagePaths(projectDependency));
        dependency.put("transitive", projectDependency.transitive);
        dependencies.add(dependency);

        xmlRpcHelper.saveConfig(projectDependenciesPath, projectDependencies, true);
    }

    private Vector<String> asStagePaths(Dependency dependency)
    {
        Vector<String> v = new Vector<String>();
        if (dependency.stage != null)
        {
            v.add("projects/" + dependency.project.name + "/stages/" + dependency.stage);
        }
        return v;
    }

    /**
     * The project model used by these tests to simplify management of the test configuration.
     * This model differs from the ProjectConfiguration in that only properties used by this
     * test suite are available.
     */
    protected class Project
    {
        private String name;
        private String org;
        private String status;
        private String version;
        private List<Dependency> dependencies = new LinkedList<Dependency>();

        private List<Stage> stages = new LinkedList<Stage>();
        private List<Recipe> recipes = new LinkedList<Recipe>();

        private boolean propagateStatus = false;
        private boolean propagateVersion = false;

        private String retrievalPattern = "lib/[artifact].[ext]";

        protected Project(String name)
        {
            this.setName(name);
            addStage("default");
            addRecipe("default");
        }

        protected Project(String name, String org)
        {
            this(name);
            this.setOrg(org);
        }

        public Recipe addRecipe(String recipeName)
        {
            Recipe recipe = new Recipe(this, recipeName);
            this.recipes.add(recipe);
            return recipe;
        }

        public Stage addStage(String stageName)
        {
            Stage stage = new Stage(this, stageName);
            this.stages.add(stage);
            return stage;
        }

        protected void addDependency(Project dependency)
        {
            dependencies.add(new Dependency(dependency));
        }

        protected void addDependency(Dependency dependnecy)
        {
            dependencies.add(dependnecy);
        }

        protected Stage getDefaultStage()
        {
            return getStage("default");
        }

        protected Artifact addArtifact(String artifact)
        {
            return getRecipe("default").addArtifact(artifact);
        }

        protected List<Artifact> addArtifacts(String... artifacts)
        {
            return getRecipe("default").addArtifacts(artifacts);
        }

        protected void setRetrievalPattern(String retrievalPattern)
        {
            this.retrievalPattern = retrievalPattern;
        }

        protected String getName()
        {
            return name;
        }

        protected void setName(String name)
        {
            this.name = name;
        }

        protected String getOrg()
        {
            return org;
        }

        protected void setOrg(String org)
        {
            this.org = org;
        }

        protected String getStatus()
        {
            return status;
        }

        protected void setStatus(String status)
        {
            this.status = status;
        }

        protected void setVersion(String version)
        {
            this.version = version;
        }

        protected boolean isPropagateStatus()
        {
            return propagateStatus;
        }

        protected void setPropagateStatus(boolean propagateStatus)
        {
            this.propagateStatus = propagateStatus;
        }

        protected boolean isPropagateVersion()
        {
            return propagateVersion;
        }

        protected void setPropagateVersion(boolean b)
        {
            this.propagateVersion = b;
        }

        protected List<Stage> getStages()
        {
            return stages;
        }

        protected Stage getStage(final String stageName)
        {
            return CollectionUtils.find(stages, new Predicate<Stage>()
            {
                public boolean satisfied(Stage stage)
                {
                    return stage.getName().equals(stageName);
                }
            });
        }
        protected Recipe getRecipe(final String recipeName)
        {
            return CollectionUtils.find(recipes, new Predicate<Recipe>()
            {
                public boolean satisfied(Recipe recipe)
                {
                    return recipe.getName().equals(recipeName);
                }
            });
        }

        protected List<Recipe> getRecipes()
        {
            return recipes;
        }

        protected Recipe getDefaultRecipe()
        {
            return getRecipe("default");
        }
    }

    protected class Dependency
    {
        private Project project;

        private boolean transitive = true;

        private String stage = null;

        private String revision = DependencyConfiguration.REVISION_LATEST_INTEGRATION;

        protected Dependency(Project project, boolean transitive, String stage, String revision)
        {
            this(project, transitive, stage);
            this.revision = revision;
        }

        protected Dependency(Project project, boolean transitive, String stage)
        {
            this(project, transitive);
            this.stage = stage;
        }

        protected Dependency(Project project, boolean transitive)
        {
            this(project);
            this.transitive = transitive;
        }

        protected Dependency(Project project)
        {
            this.project = project;
        }
    }

    protected class Stage
    {
        private Project project;
        private String name;
        private Recipe recipe;

        private Map<String, String> properties = new HashMap<String, String>();

        protected Stage(Project project, String name)
        {
            this.setName(name);
            this.setProject(project);
        }

        protected void addProperty(String name, String value)
        {
            properties.put(name, value);
        }

        protected String getName()
        {
            return name;
        }

        protected void setName(String name)
        {
            this.name = name;
        }

        protected Project getProject()
        {
            return project;
        }

        protected void setProject(Project project)
        {
            this.project = project;
        }

        protected Recipe getRecipe()
        {
            if (recipe != null)
            {
                return recipe;
            }
            return project.getDefaultRecipe();
        }

        protected void setRecipe(Recipe recipe)
        {
            this.recipe = recipe;
        }
    }

    protected class Recipe
    {
        private Project project;
        private String name;
        private List<Artifact> artifacts = new LinkedList<Artifact>();

        protected Recipe(Project project, String name)
        {
            this.project = project;
            this.name = name;
        }

        protected Artifact addArtifact(String artifactName)
        {
            Artifact artifact = new Artifact(artifactName, this);
            this.artifacts.add(artifact);
            return artifact;
        }

        protected List<Artifact> addArtifacts(String... artifactNames)
        {
            List<Artifact> artifacts = new LinkedList<Artifact>();
            for (String artifactName : artifactNames)
            {
                artifacts.add(addArtifact(artifactName));
            }
            return artifacts;
        }

        protected List<Artifact> getArtifacts()
        {
            return artifacts;
        }

        protected String getName()
        {
            return name;
        }
    }

    protected class Artifact
    {
        private final Pattern pattern = Pattern.compile("(.+)\\.(.+)");

        private String name;
        private String extension;
        private Recipe recipe;
        private String artifactPattern;

        protected Artifact(String filename, Recipe recipe)
        {
            this.recipe = recipe;
            Matcher m = pattern.matcher(filename);
            if (m.matches())
            {
                name = m.group(1);
                extension = m.group(2);
            }
        }

        protected String getName()
        {
            return name;
        }

        protected String getExtension()
        {
            return extension;
        }

        protected void setExtension(String extension)
        {
            this.extension = extension;
        }

        protected void setName(String name)
        {
            this.name = name;
        }

        protected Recipe getRecipe()
        {
            return recipe;
        }

        protected String getArtifactPattern()
        {
            return artifactPattern;
        }

        protected void setArtifactPattern(String artifactPattern)
        {
            this.artifactPattern = artifactPattern;
        }
    }

    /**
     * Contains the configuration details to be passed through to the ant build
     * to a) produce the specified artifacts for each stage and b) to assert the
     * existance of the specified artifacts
     */
    protected class AntBuildConfiguration
    {
        /**
         * The list of files to be created by the ant build.
         */
        private List<String> filesToCreate = new LinkedList<String>();
        /**
         * The list of files whose presence is to be asserted by the build.  If any of these
         * files are missing, the build fails.
         */
        private List<String> expectedFiles = new LinkedList<String>();
        /**
         * The list of files whose absence is asserted by the build.  If any of these
         * files are present, the build fails.
         */
        private List<String> notExpected = new LinkedList<String>();

        protected void addFileToCreate(String artifact)
        {
            filesToCreate.add(artifact);
        }

        protected void addFilesToCreate(String... artifacts)
        {
            this.filesToCreate.addAll(Arrays.asList(artifacts));
        }

        protected void addExpectedFile(String dependency)
        {
            expectedFiles.add(dependency);
        }

        protected void addExpectedFiles(String... dependencies)
        {
            this.expectedFiles.addAll(Arrays.asList(dependencies));
        }

        protected void addNotExpectedFile(String file)
        {
            this.notExpected.add(file);
        }

        protected String getCreateList()
        {
            return StringUtils.join(",", filesToCreate);
        }

        protected String getExpectedList()
        {
            return StringUtils.join(",", expectedFiles);
        }

        protected String getNotExpectedList()
        {
            return StringUtils.join(",", notExpected);
        }
    }
}
