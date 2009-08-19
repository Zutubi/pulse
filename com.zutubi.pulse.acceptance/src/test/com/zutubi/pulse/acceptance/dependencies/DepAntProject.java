package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.acceptance.XmlRpcHelper;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.Pair;
import com.zutubi.util.StringUtils;

import java.util.*;

public class DepAntProject extends Project
{
    private static final String PROPERTY_CREATE_LIST = "create.list";
    private static final String PROPERTY_EXPECTED_LIST = "expected.list";
    private static final String PROPERTY_NOT_EXPECTED_LIST = "not.expected.list";

    private AntBuildConfiguration build = null;

    public DepAntProject(XmlRpcHelper xmlRpcHelper, String name)
    {
        super(xmlRpcHelper, name);
    }

    public DepAntProject(XmlRpcHelper xmlRpcHelper, String name, String org)
    {
        super(xmlRpcHelper, name, org);
    }

    public int triggerSuccessfulBuild(Pair<String, Object>... options) throws Exception
    {
        triggerBuildCommon();
        return super.triggerSuccessfulBuild(options);
    }

    public int triggerBuild(Pair<String, Object>... options) throws Exception
    {
        triggerBuildCommon();
        return super.triggerBuild(options);
    }

    private void triggerBuildCommon() throws Exception
    {
        initBuildAntConfigurationIfRequired();

        // for each stage, set the necessary build properties.
        for (Stage stage : getStages())
        {
            stage.getRecipe();
            xmlRpcHelper.insertOrUpdateStageProperty(getName(), stage.getName(), PROPERTY_CREATE_LIST, build.getCreateList());
            xmlRpcHelper.insertOrUpdateStageProperty(getName(), stage.getName(), PROPERTY_EXPECTED_LIST, build.getExpectedList());
            xmlRpcHelper.insertOrUpdateStageProperty(getName(), stage.getName(), PROPERTY_NOT_EXPECTED_LIST, build.getNotExpectedList());
        }
    }

    private void initBuildAntConfigurationIfRequired()
    {
        if (build == null)
        {
            build = new AntBuildConfiguration();
            for (Recipe recipe : getRecipes())
            {
                for (Artifact artifact : recipe.getArtifacts())
                {
                    build.addFilesToCreate("build/" + artifact.getName() + "." + artifact.getExtension());
                }
            }
        }
    }

    public Hashtable<String, Object> insertProject() throws Exception
    {
        String target = "present not.present create";
        String args = "-Dcreate.list=\"${" + PROPERTY_CREATE_LIST + "}\" -Dpresent.list=\"${" + PROPERTY_EXPECTED_LIST + "}\" -Dnot.present.list=\"${" + PROPERTY_NOT_EXPECTED_LIST + "}\"";

        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("name", "build");
        antConfig.put("targets", target);
        antConfig.put("args", args);

        xmlRpcHelper.insertSingleCommandProject(getName(), ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.DEP_ANT_REPOSITORY), antConfig);
        
        return antConfig;
    }

    @Override
    protected void insertStage(Stage stage) throws Exception
    {
        super.insertStage(stage);

        // set blank default properties.
        xmlRpcHelper.insertOrUpdateStageProperty(getName(), stage.getName(), PROPERTY_CREATE_LIST, "");
        xmlRpcHelper.insertOrUpdateStageProperty(getName(), stage.getName(), PROPERTY_EXPECTED_LIST, "");
        xmlRpcHelper.insertOrUpdateStageProperty(getName(), stage.getName(), PROPERTY_NOT_EXPECTED_LIST, "");
    }

    protected void addFileToCreate(String artifact)
    {
        getBuild().addFileToCreate(artifact);
    }

    protected void addFilesToCreate(String... artifacts)
    {
        getBuild().addFilesToCreate(artifacts);
    }

    protected void addExpectedFile(String dependency)
    {
        getBuild().addExpectedFile(dependency);
    }

    protected void addExpectedFiles(String... dependencies)
    {
        getBuild().addExpectedFiles(dependencies);
    }

    protected void addNotExpectedFile(String file)
    {
        getBuild().addNotExpectedFile(file);
    }

    private AntBuildConfiguration getBuild()
    {
        if (build == null)
        {
            build = new AntBuildConfiguration();
        }
        return build;
    }

    /**
     * Contains the configuration details to be passed through to the ant build
     * to a) produce the specified artifacts for each stage and b) to assert the
     * existance of the specified artifacts
     */
    private class AntBuildConfiguration
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
