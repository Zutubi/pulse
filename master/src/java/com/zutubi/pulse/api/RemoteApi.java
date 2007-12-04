package com.zutubi.pulse.api;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.committransformers.CommitMessageTransformerManager;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.system.SystemStartedEvent;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.scm.SCMConfiguration;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMServerUtils;
import com.zutubi.pulse.util.OgnlUtils;
import com.zutubi.pulse.util.StringUtils;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.util.UnaryFunction;
import com.zutubi.pulse.validation.PulseValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import ognl.Ognl;
import ognl.OgnlException;
import org.acegisecurity.AuthenticationManager;

import java.util.*;

/**
 * Implements a simple API for remote monitoring and control.
 */
public class RemoteApi implements com.zutubi.pulse.events.EventListener
{
    private TokenManager tokenManager;

    private ShutdownManager shutdownManager;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private UserManager userManager;
    private AgentManager agentManager;
    private AuthenticationManager authenticationManager;
    private EventManager eventManager;
    private MasterConfigurationManager configurationManager;

    private ValidationManager validationManager;

    //---( Define the properties that are visible in structs in the remote api. )---
    private static final Map<Class, String[]> structDefs = new HashMap<Class, String[]>();
    private CommitMessageTransformerManager transformerManager;

    {
        structDefs.put(Project.class, new String[]{"id", "name", "description", "url"});
        structDefs.put(Cvs.class, new String[]{"root", "module", "password", "branch", "quietPeriod", "monitor", "pollingInterval"});
        structDefs.put(Svn.class, new String[]{"url", "username", "password", "keyfile", "passphrase", "monitor", "pollingInterval"});
        structDefs.put(P4.class, new String[]{"port", "user", "password", "client", "monitor", "pollingInterval"});
        structDefs.put(AntPulseFileDetails.class, new String[]{"buildFile", "targets", "arguments", "workingDir"});
        structDefs.put(ExecutablePulseFileDetails.class, new String[]{"executable", "arguments", "workingDir"});
        structDefs.put(MavenPulseFileDetails.class, new String[]{"targets", "workingDir", "arguments"});
        structDefs.put(Maven2PulseFileDetails.class, new String[]{"goals", "workingDir", "arguments"});
        structDefs.put(MakePulseFileDetails.class, new String[]{"makefile", "targets", "arguments", "workingDir"});
        structDefs.put(XCodePulseFileDetails.class, new String[]{"workingDir", "project", "config", "target", "action", "settings"});
        structDefs.put(CustomPulseFileDetails.class, new String[]{"pulseFile"});
        structDefs.put(VersionedPulseFileDetails.class, new String[]{"pulseFileName"});
    }

    public RemoteApi()
    {
        // can remove this call when we sort out autowiring from the XmlRpcServlet.
        ComponentContext.autowire(this);
    }

    public int getVersion()
    {
        Version v = Version.getVersion();
        return v.getBuildNumberAsInt();
    }

    public String login(String username, String password) throws AuthenticationException
    {
        return tokenManager.login(username, password);
    }

    public boolean logout(String token)
    {
        return tokenManager.logout(token);
    }

    public String ping()
    {
        return "pong";
    }

    public Vector<String> getAllUserLogins(String token) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);
        List<User> users = userManager.getAllUsers();
        Vector<String> result = new Vector<String>(users.size());
        for (User user : users)
        {
            result.add(user.getLogin());
        }

        return result;
    }

    public Vector<String> getAllProjectNames(String token) throws AuthenticationException
    {
        //@Secured({"ROLE_USER"})
        tokenManager.verifyUser(token);

        List<Project> projects = projectManager.getAllProjects();
        return getNames(projects);
    }

    public Vector<String> getAllProjectGroups(String token) throws AuthenticationException
    {
        tokenManager.verifyUser(token);

        List<ProjectGroup> groups = projectManager.getAllProjectGroups();
        Vector<String> result = new Vector<String>(groups.size());
        for (ProjectGroup g : groups)
        {
            result.add(g.getName());
        }

        return result;
    }

    public Hashtable<String, Object> getProjectGroup(String token, String name) throws AuthenticationException, IllegalArgumentException
    {
        tokenManager.verifyUser(token);

        ProjectGroup group = projectManager.getProjectGroup(name);
        if(group == null)
        {
            throw new IllegalArgumentException(String.format("Unknown project group: '%s'", name));
        }

        Hashtable<String, Object> result = new Hashtable<String, Object>();
        result.put("name", group.getName());
        result.put("projects", getNames(group.getProjects()));
        return result;
    }

    public boolean createProjectGroup(String token, String name, Vector<String> projects) throws AuthenticationException, IllegalArgumentException
    {
        tokenManager.verifyAdmin(token);

        if(!TextUtils.stringSet(name))
        {
            throw new IllegalArgumentException("Name is required");
        }

        if(projectManager.getProjectGroup(name) != null)
        {
            throw new IllegalArgumentException(String.format("A project group with name '%s' already exists", name));
        }

        List<Project> members = getProjectList(projects);

        ProjectGroup group = new ProjectGroup(name);
        group.setProjects(members);

        try
        {
            tokenManager.loginUser(token);
            projectManager.save(group);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean editProjectGroup(String token, String name, String newName, Vector<String> projects) throws AuthenticationException, IllegalArgumentException
    {
        tokenManager.verifyAdmin(token);

        ProjectGroup group = projectManager.getProjectGroup(name);
        if(group == null)
        {
            throw new IllegalArgumentException(String.format("Unknown project group '%s'", name));
        }

        if(!TextUtils.stringSet(newName))
        {
            throw new IllegalArgumentException("Name is required");
        }

        if(!newName.equals(name) && projectManager.getProjectGroup(newName) != null)
        {
            throw new IllegalArgumentException(String.format("A project group with name '%s' already exists", newName));
        }

        group.setName(newName);
        List<Project> members = getProjectList(projects);
        group.setProjects(members);

        try
        {
            tokenManager.loginUser(token);
            projectManager.save(group);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean deleteProjectGroup(String token, String name) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        ProjectGroup group = projectManager.getProjectGroup(name);
        if(group == null)
        {
            return false;
        }
        else
        {
            projectManager.delete(group);
            return true;
        }
    }

    public Vector<String> getMyProjectNames(String token) throws AuthenticationException
    {
        User user = tokenManager.verifyUser(token);
        List<Project> projects = new LinkedList<Project>();

        if (user != null)
        {
            projects.addAll(userManager.getUserProjects(user, projectManager));
        }

        return getNames(projects);
    }

    public Vector<Hashtable<String, Object>> getBuild(String token, String projectName, int id) throws AuthenticationException
    {
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(1);

        tokenManager.verifyUser(token);
        Project project = internalGetProject(projectName);
        BuildResult build = buildManager.getByProjectAndNumber(project, id);
        if (build == null)
        {
            return result;
        }

        result.add(convertResult(build));
        return result;
    }

    public boolean deleteBuild(String token, String projectName, int id) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName);
            projectManager.checkWrite(project);
            BuildResult build = buildManager.getByProjectAndNumber(project, id);
            if (build == null)
            {
                return false;
            }

            buildManager.delete(build);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();   
        }
    }

    public Vector<Hashtable<String, Object>> queryBuildsForProject(String token, String projectName, String[] resultStates, String[] buildSpecifications, int firstResult, int maxResults, boolean mostRecentFirst) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        Project project = internalGetProject(projectName);

        List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, mapStates(resultStates), mapSpecs(buildSpecifications, project), -1, -1, null, firstResult, maxResults, mostRecentFirst);
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for (BuildResult build : builds)
        {
            Hashtable<String, Object> buildDetails = convertResult(build);
            result.add(buildDetails);
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getBuildRange(String token, String projectName, String buildSpecification, int afterBuild, int toBuild) throws AuthenticationException
    {
        tokenManager.verifyUser(token);

        Project project = internalGetProject(projectName);
        BuildSpecification spec = getBuildSpecification(project, buildSpecification);
        List<BuildResult> buildRange = buildManager.querySpecificationBuilds(project, spec.getPname(), ResultState.getCompletedStates(), afterBuild + 1, toBuild, 0, -1, false, false);
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(buildRange.size());
        for(BuildResult r: buildRange)
        {
            result.add(convertResult(r));
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getPreviousBuild(String token, String projectName, int id) throws AuthenticationException
    {
        tokenManager.verifyUser(token);

        Project project = internalGetProject(projectName);
        BuildResult buildResult = internalGetBuild(project, id);
        buildResult = buildManager.getPreviousBuildResult(buildResult);
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();
        if(buildResult != null)
        {
            result.add(convertResult(buildResult));
        }

        return result;
    }

    private PersistentName[] mapSpecs(String[] buildSpecifications, Project project)
    {
        if (buildSpecifications.length > 0)
        {
            PersistentName[] specs = new PersistentName[buildSpecifications.length];
            for(int i = 0; i < buildSpecifications.length; i++)
            {
                specs[i] = getBuildSpecification(project, buildSpecifications[i]).getPname();
            }

            return specs;
        }
        else
        {
            return null;
        }
    }

    private ResultState[] mapStates(String[] stateNames)
    {
        if(stateNames.length > 0)
        {
            ResultState[] states = new ResultState[stateNames.length];
            for(int i = 0; i < stateNames.length; i++)
            {
                states[i] = ResultState.fromPrettyString(stateNames[i]);
            }

            return states;
        }
        else
        {
            return null;
        }
    }

    public Vector<Hashtable<String, Object>> getLatestBuildsForProject(String token, String projectName, String buildSpecification, boolean completedOnly, int maxResults) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        Project project = internalGetProject(projectName);

        PersistentName[] specs = null;
        if (TextUtils.stringSet(buildSpecification))
        {
            BuildSpecification spec = getBuildSpecification(project, buildSpecification);
            specs = new PersistentName[]{ spec.getPname() };
        }

        ResultState[] states = null;
        if (completedOnly)
        {
            states = ResultState.getCompletedStates();
        }

        List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, states, specs, -1, -1, null, 0, maxResults, true);
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for (BuildResult build : builds)
        {
            Hashtable<String, Object> buildDetails = convertResult(build);
            result.add(buildDetails);
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestBuildForProject(String token, String projectName, String buildSpecification, boolean completedOnly) throws AuthenticationException
    {
        return getLatestBuildsForProject(token, projectName, buildSpecification, completedOnly, 1);
    }

    public Vector<Hashtable<String, Object>> getPersonalBuild(String token, int id) throws AuthenticationException
    {
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(1);

        User user = tokenManager.verifyUser(token);
        BuildResult build = buildManager.getByUserAndNumber(user, id);
        if (build == null)
        {
            return result;
        }

        result.add(convertResult(build));
        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestPersonalBuilds(String token, boolean completedOnly, int maxResults) throws AuthenticationException
    {
        User user = tokenManager.verifyUser(token);

        List<BuildResult> builds = buildManager.getPersonalBuilds(user);
        if (completedOnly)
        {
            Iterator<BuildResult> it = builds.iterator();
            while (it.hasNext())
            {
                BuildResult b = it.next();
                if (!b.completed())
                {
                    it.remove();
                }
            }
        }

        if (maxResults >= 0 && builds.size() > maxResults)
        {
            builds = builds.subList(0, maxResults);
        }

        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for (BuildResult build : builds)
        {
            Hashtable<String, Object> buildDetails = convertResult(build);
            result.add(buildDetails);
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestPersonalBuild(String token, boolean completedOnly) throws AuthenticationException
    {
        return getLatestPersonalBuilds(token, completedOnly, 1);
    }

    private Hashtable<String, Object> convertResult(BuildResult build)
    {
        Hashtable<String, Object> buildDetails = new Hashtable<String, Object>();
        buildDetails.put("id", (int) build.getNumber());
        buildDetails.put("project", build.getProject().getName());
        buildDetails.put("specification", build.getBuildSpecification());
        buildDetails.put("status", build.getState().getPrettyString());
        buildDetails.put("revision", getBuildRevision(build));
        buildDetails.put("completed", build.completed());
        buildDetails.put("succeeded", build.succeeded());

        TimeStamps timeStamps = build.getStamps();
        buildDetails.put("startTime", new Date(timeStamps.getStartTime()));
        buildDetails.put("endTime", new Date(timeStamps.getEndTime()));
        if (timeStamps.hasEstimatedTimeRemaining())
        {
            buildDetails.put("progress", timeStamps.getEstimatedPercentComplete());
        }
        else
        {
            buildDetails.put("progress", -1);
        }

        return buildDetails;
    }

    private String getBuildRevision(BuildResult build)
    {
        BuildScmDetails details = build.getScmDetails();
        if(details != null)
        {
            Revision revision = details.getRevision();
            if(revision != null)
            {
                return revision.getRevisionString();
            }
        }

        return "";
    }

    public Vector<Hashtable<String, Object>> getChangesInBuild(String token, String projectName, int id) throws AuthenticationException
    {
        tokenManager.verifyUser(token);

        Project project = internalGetProject(projectName);
        final BuildResult build = internalGetBuild(project, id);
        final Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();

        buildManager.executeInTransaction(new Runnable()
        {
            public void run()
            {
                List<Changelist> changelists = buildManager.getChangesForBuild(build);
                for(Changelist change: changelists)
                {
                    result.add(convertChangelist(change));
                }
            }
        });

        return result;
    }

    private Hashtable<String, Object> convertChangelist(Changelist change)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        if(change.getRevision() != null && change.getRevision().getRevisionString() != null)
        {
            result.put("revision", change.getRevision().getRevisionString());
        }
        if(change.getUser() != null)
        {
            result.put("author", change.getUser());
        }
        if(change.getDate() != null)
        {
            result.put("date", change.getDate());
        }
        if(change.getComment() != null)
        {
            result.put("comment", change.getComment());
        }

        Vector<Hashtable<String, Object>> files = new Vector<Hashtable<String, Object>>(change.getChanges().size());
        for(Change file: change.getChanges())
        {
            files.add(convertChange(file));
        }
        result.put("files", files);

        return result;
    }

    private Hashtable<String, Object> convertChange(Change change)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        if(change.getFilename() != null)
        {
            result.put("file", change.getFilename());
        }
        if(change.getRevision() != null)
        {
            result.put("revision", change.getRevision().getRevisionString());
        }
        if(change.getAction() != null)
        {
            result.put("action", change.getAction().toString().toLowerCase());
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getArtifactsInBuild(String token, final String projectName, final int id) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        final Project project = internalGetProject(projectName);
        final Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();

        buildManager.executeInTransaction(new Runnable()
        {
            public void run()
            {
                final BuildResult build = internalGetBuild(project, id);

                build.getRoot().forEachNode(new UnaryFunction<RecipeResultNode>()
                {
                    public void process(RecipeResultNode recipeResultNode)
                    {
                        RecipeResult recipeResult = recipeResultNode.getResult();
                        if(recipeResult != null)
                        {
                            String stage = recipeResultNode.getStage();
                            for(CommandResult commandResult: recipeResult.getCommandResults())
                            {
                                String command = commandResult.getCommandName();
                                for(StoredArtifact artifact: commandResult.getArtifacts())
                                {
                                    result.add(convertArtifact(artifact, projectName, build, stage, command));
                                }
                            }
                        }
                    }
                });
            }
        });

        return result;
    }

    private Hashtable<String, Object> convertArtifact(StoredArtifact artifact, String project, BuildResult build, String stage, String command)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        result.put("stage", stage);
        result.put("command", command);
        result.put("name", artifact.getName());
        result.put("permalink", StringUtils.join("/", "display/projects",
                                                 StringUtils.uriComponentEncode(project),
                                                 "builds", Long.toString(build.getNumber()),
                                                 StringUtils.uriComponentEncode(stage),
                                                 StringUtils.uriComponentEncode(command),
                                                 StringUtils.uriComponentEncode(artifact.getName())));
        return result;
    }

    public Vector<Hashtable<String, String>> getMessagesInBuild(String token, String projectName, final int id) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        final Project project = internalGetProject(projectName);
        final Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();
        
        buildManager.executeInTransaction(new Runnable()
        {
            public void run()
            {
                final BuildResult build = internalGetBuild(project, id);
                build.loadFeatures(configurationManager.getDataDirectory());
                for(Feature f: build.getFeatures())
                {
                    result.add(convertFeature(null, null, null, null, f));
                }
                
                build.getRoot().forEachNode(new UnaryFunction<RecipeResultNode>()
                {
                    public void process(RecipeResultNode recipeResultNode)
                    {
                        RecipeResult recipeResult = recipeResultNode.getResult();
                        if(recipeResult != null)
                        {
                            String stage = recipeResultNode.getStage();
                            for(Feature f: recipeResult.getFeatures())
                            {
                                result.add(convertFeature(stage, null, null, null, f));
                            }

                            for(CommandResult commandResult: recipeResult.getCommandResults())
                            {
                                String command = commandResult.getCommandName();
                                for(Feature f: commandResult.getFeatures())
                                {
                                    result.add(convertFeature(stage, command, null, null, f));
                                }

                                for(StoredArtifact artifact: commandResult.getArtifacts())
                                {
                                    String artifactName = artifact.getName();
                                    for(StoredFileArtifact fileArtifact: artifact.getChildren())
                                    {
                                        String artifactPath = fileArtifact.getPath();
                                        for(Feature f: fileArtifact.getFeatures())
                                        {
                                            result.add(convertFeature(stage, command, artifactName, artifactPath, f));
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        return result;
    }

    public Vector<Hashtable<String, String>> getErrorMessagesInBuild(String token, String projectName, final int id) throws AuthenticationException
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.ERROR);
    }

    public Vector<Hashtable<String, String>> getWarningMessagesInBuild(String token, String projectName, final int id) throws AuthenticationException
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.WARNING);
    }

    public Vector<Hashtable<String, String>> getInfoMessagesInBuild(String token, String projectName, final int id) throws AuthenticationException
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.INFO);
    }

    private Vector<Hashtable<String, String>> getMessagesOfLevel(String token, String projectName, int id, Feature.Level level) throws AuthenticationException
    {
        Vector<Hashtable<String, String>> result = getMessagesInBuild(token, projectName, id);
        Iterator<Hashtable<String, String>> it = result.iterator();
        String levelString = level.getPrettyString();
        while(it.hasNext())
        {
            Hashtable<String, String> feature = it.next();
            if(!levelString.equals(feature.get("level")))
            {
                it.remove();
            }
        }

        return result;
    }

    private Hashtable<String, String> convertFeature(String stageName, String commandName, String artifactName, String artifactPath, Feature feature)
    {
        Hashtable<String, String> result = new Hashtable<String, String>();
        if(stageName != null)
        {
            result.put("stage", stageName);
        }

        if(commandName != null)
        {
            result.put("command", commandName);
        }

        if(artifactName != null)
        {
            result.put("artifact", artifactName);
        }

        if(artifactPath != null)
        {
            result.put("path", artifactPath);
        }

        result.put("level", feature.getLevel().getPrettyString());
        result.put("message", feature.getSummary());
        return result;
    }

    public boolean triggerBuild(String token, String projectName, String buildSpecification) throws AuthenticationException
    {
        return triggerBuild(token, projectName, buildSpecification, null);
    }

    public boolean triggerBuild(String token, String projectName, String buildSpecification, String revision) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = internalGetProject(projectName);
            getBuildSpecification(project, buildSpecification);

            Revision r = null;
            if(TextUtils.stringSet(revision))
            {
                SCMServer scm = null;
                try
                {
                    scm = project.getScm().createServer();
                    r = scm.getRevision(revision);
                }
                catch (SCMException e)
                {
                    throw new IllegalArgumentException("Unable to verify revision: " + e.getMessage());
                }
                finally
                {
                    SCMServerUtils.close(scm);
                }
            }

            projectManager.triggerBuild(project, buildSpecification, new RemoteTriggerBuildReason(), r, true);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getProjectBuildSpecifications(String token, String projectName) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        Project project = internalGetProject(projectName);
        return getNames(project.getBuildSpecifications());
    }

    public String getProjectState(String token, String projectName) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        Project project = internalGetProject(projectName);
        return project.getState().toString().toLowerCase();
    }

    public Hashtable<String, String> preparePersonalBuild(String token, String projectName, String buildSpecification) throws AuthenticationException
    {
        tokenManager.verifyRoleIn(token, GrantedAuthority.PERSONAL);
        Project project = internalGetProject(projectName);
        getBuildSpecification(project, buildSpecification);

        Hashtable<String, String> scmDetails = new Hashtable<String, String>();
        scmDetails.put(SCMConfiguration.PROPERTY_TYPE, project.getScm().getType());
        scmDetails.putAll(project.getScm().getRepositoryProperties());
        return scmDetails;
    }

    public boolean pauseProject(String token, String projectName) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = internalGetProject(projectName);
            if (project.isPaused())
            {
                return false;
            }
            else
            {
                projectManager.pauseProject(project);
                return true;
            }
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean resumeProject(String token, String projectName) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = internalGetProject(projectName);
            if (project.isPaused())
            {
                projectManager.resumeProject(project);
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean createAgent(String token, String name, String host, int port) throws AuthenticationException, LicenseException
    {
        tokenManager.verifyAdmin(token);
        LicenseHolder.ensureAuthorization(LicenseHolder.AUTH_ADD_AGENT);

        Slave newSlave = new Slave(name, host, port);
        validate(newSlave);

        if (!TextUtils.stringSet(name) || agentManager.agentExists(name))
        {
            throw new IllegalArgumentException(String.format("An agent by the name '%s' already exists. Please select a different name.", name));
        }

        agentManager.addSlave(new Slave(name, host, port));
        return true;
    }

/* Refactor DeleteAgentAction, but first, understand what is going on with agents and slaves. 
    public boolean deleteAgent(String token, String name) throws AuthenticationException
    {
        try
        {
            AcegiUtils.loginAs(tokenManager.verifyAdmin(token));
            if (!agentManager.agentExists(name))
            {
                throw new IllegalArgumentException(String.format("No agent by the name '%s' exists. Please select a different name.", name));
            }

        }
        finally
        {
            AcegiUtils.logout();
        }
    }
*/

    public String getAgentStatus(String token, String name) throws AuthenticationException
    {
        tokenManager.verifyUser(token);

        Agent agent = agentManager.getAgent(name);
        if (agent == null)
        {
            return "";
        }

        return agent.getStatus().getPrettyString();
    }

    public boolean enableAgent(String token, String name) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        Agent agent = agentManager.getAgent(name);
        if (agent == null)
        {
            throw new IllegalArgumentException("Unknown agent '" + name + "'");
        }

        if(agent.isSlave())
        {
            agentManager.enableSlave(agent.getId());
        }
        else
        {
            agentManager.enableMasterAgent();
        }

        return true;
    }

    public boolean disableAgent(String token, String name) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        Agent agent = agentManager.getAgent(name);
        if (agent == null)
        {
            throw new IllegalArgumentException("Unknown agent '" + name + "'");
        }

        if(agent.isSlave())
        {
            agentManager.disableSlave(agent.getId());
        }
        else
        {
            agentManager.disableMasterAgent();
        }

        return true;
    }

    public boolean shutdown(String token, boolean force, boolean exitJvm) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        shutdownManager.delayedShutdown(force, exitJvm);
        return true;
    }

    public boolean stopService(String token) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);
        shutdownManager.delayedStop();
        return true;
    }

    /**
     * Updates the specified users password.
     *
     * @param token    used to authenticate the request.
     * @param login    name identifying the user whose password is being set.
     * @param password is the new password.
     * @return true if the request was successful, false otherwise.
     * @throws AuthenticationException if the token does not authorise administrator access.
     */
    public boolean setPassword(String token, String login, String password) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        User user = userManager.getUser(login);
        if (user == null)
        {
            throw new IllegalArgumentException(String.format("Unknown username '%s'", login));
        }
        userManager.setPassword(user, password);
        userManager.save(user);
        return true;
    }

    /**
     * Create a new user.
     *
     * @param user      is a map of containing the users details.
     * @param token     used to authenticate the request
     *
     * @return true if the request is successful
     *
     * @throws AuthenticationException if you are not authorised to execute this action.
     * @throws LicenseException        if you are not licensed to execute this action.
     */
    public boolean createUser(String token, Hashtable<String, Object> user) throws AuthenticationException, LicenseException
    {
        tokenManager.verifyAdmin(token);
        LicenseHolder.ensureAuthorization(LicenseHolder.AUTH_ADD_USER);

        // validate the user details.
        String login = (String) user.get("login");
        User existingUser = userManager.getUser(login);
        if (existingUser != null)
        {
            throw new IllegalArgumentException(String.format("A user with the login '%s' already exists. Please select a different login.", login));
        }

        User instance = new User();
        instance.setLogin(login);
        instance.setName((String) user.get("name"));

        userManager.addUser(instance, false, false);
        return true;
    }

    /**
     * Delete the specified user.
     *
     * @param token used to authenticate the request.
     * @param login identifies the user to be deleted.
     * @return true if the request is successful, false otherwise.
     * @throws AuthenticationException is you are not authorised to execute this request.
     *
     * @throws IllegalArgumentException if no user with the specified login exists.
     */
    public boolean deleteUser(String token, String login) throws AuthenticationException, IllegalArgumentException
    {
        tokenManager.verifyAdmin(token);

        User user = userManager.getUser(login);
        if (user == null)
        {
            throw new IllegalArgumentException(String.format("Unknown user login: '%s'", login));
        }
        userManager.delete(user);
        return true;
    }

    /**
     * Create a new project.
     *
     * @param token     used to authenticate the request.
     * @param project   the project details
     * @param scm       the scm details
     * @param type      the project type details
     *
     * @return true if the request is successful, false otherwise.
     * 
     * @throws AuthenticationException  if you are not authorised to execute this action.
     * @throws LicenseException         if you are not licensed to execute this action.
     * @throws IllegalArgumentException      if a validation error is detected.
     */
    public boolean createProject(String token, Hashtable<String, Object> project, Hashtable<String, Object> scm, Hashtable<String, Object> type) throws AuthenticationException, LicenseException, IllegalArgumentException
    {
        try
        {
            tokenManager.loginUser(token);

            String name = (String) project.get("name");
            Project existingProject = projectManager.getProject(name);
            if (existingProject != null)
            {
                throw new IllegalArgumentException(String.format("A project with the name '%s' already exists. Please use a different name.", name));
            }

            Project newProject = new Project();
            setProperties(project, newProject);
            validate(newProject);

            // lookup scm.
            Scm newScm = createScm(scm);
            validate(newScm);
            newProject.setScm(newScm);

            // set the details.
            PulseFileDetails newType = createFileDetails(type);
            validate(newType);
            newProject.setPulseFileDetails(newType);

            // set the details.
            projectManager.create(newProject);

            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Delete the specified project.
     *
     * @param token used to authenticate the request.
     * @param name  the name of the project to be deleted.
     * @return true if the request is successful, false otherwise.
     *
     * @throws AuthenticationException if you are not authorised to execute this request.
     *
     * @throws IllegalArgumentException if no project with the specified name exists.
     */
    public boolean deleteProject(String token, String name) throws AuthenticationException, IllegalArgumentException
    {
        try
        {
            tokenManager.loginUser(token);
            
            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", name));
            }

            projectManager.delete(project);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean editProject(String token, String name, Hashtable<String, Object> projectDetails) throws AuthenticationException, IllegalArgumentException
    {
        try
        {
            tokenManager.loginUser(token);
            
            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", name));
            }
            
            // are we changing the name of the project? if so, then we need to check that the new name is not already in use.
            if (projectDetails.containsKey("name"))
            {
                String newName = (String) projectDetails.get("name");
                if (!name.equals(newName) && projectManager.getProject(newName) != null)
                {
                    throw new IllegalArgumentException(String.format("The name '%s' is already in use by another project. Please select a different name.", newName));
                }
            }

            setProperties(projectDetails, project);
            validate(project);

            projectManager.save(project);

            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Get the details for the specified project.
     *
     * @param name is the name of the project to be retrieved.
     * @param token used to authenticate the request.
     *
     * @return a mapping of the projects details.
     *
     * @throws IllegalArgumentException if the specified name does not reference a project.
     * @throws AuthenticationException if you are not authorised to execute this request.
     */
    public Hashtable<String, Object> getProject(String token, String name) throws IllegalArgumentException, AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            
            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", name));
            }

            Hashtable<String, Object> details = extractDetails(project);
            // add the scm and type details.
            details.put("scm", project.getScm().getType());
            details.put("type", project.getPulseFileDetails().getType());

            return details;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Hashtable<String, Object> cloneProject(String token, String name, String cloneName, String cloneDescription) throws IllegalArgumentException, AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);

            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", name));
            }

            if(projectManager.getProject(cloneName) != null)
            {
                throw new IllegalArgumentException(String.format("The name '%s' is already in use by another project. Please select a different name.", name));
            }
            
            Project clone = projectManager.cloneProject(project, cloneName, cloneDescription);
            Hashtable<String, Object> details = extractDetails(clone);
            // add the scm and type details.
            details.put("scm", clone.getScm().getType());
            details.put("type", clone.getPulseFileDetails().getType());

            return details;
        }
        finally
        {
            tokenManager.logoutUser();
        }

    }

    /**
     * Retrieve the scm details for the specified project.
     *
     * @param token         used to authorise this request.
     * @param projectName   the name of the project for which the scm details are being retrieved.
     *
     * @return a scm structure. The contents of this are specific to the type of scm.
     *
     * @throws AuthenticationException if you are not authorised to execute this request.
     */
    public Hashtable<String, Object> getScm(String token, String projectName) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);

            Project project = projectManager.getProject(projectName);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", projectName));
            }

            return extractDetails(project.getScm());
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean editScm(String token, String name, Hashtable<String, Object> scmDetails) throws AuthenticationException, IllegalArgumentException
    {
        try
        {
            tokenManager.loginUser(token);

            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", name));
            }

            setProperties(scmDetails, project.getScm());
            validate(project.getScm());

            projectManager.save(project);

            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Hashtable<String, Object> getProjectType(String token, String name) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);

            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", name));
            }

            return extractDetails(project.getPulseFileDetails());
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean editProjectType(String token, String name, Hashtable<String, Object> specifics) throws AuthenticationException, IllegalArgumentException
    {
        try
        {
            tokenManager.loginUser(token);

            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", name));
            }

            setProperties(specifics, project.getPulseFileDetails());
            validate(project.getPulseFileDetails());

            projectManager.save(project);

            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Deletes all commit message links, primarily for testing purposes.
     *
     * @param token used to authenticate the request
     * @return the number of commit message links deleted
     * @throws AuthenticationException if the token does not authorise administrator access
     */
    public int deleteAllCommitMessageLinks(String token) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);
        List<CommitMessageTransformer> transformers = transformerManager.getCommitMessageTransformers();
        int result = transformers.size();
        for (CommitMessageTransformer t : transformers)
        {
            transformerManager.delete(t);
        }
        return result;
    }

    private <T extends NamedEntity> Vector<String> getNames(Collection<T> entities)
    {
        Vector<String> result = new Vector<String>(entities.size());
        for (NamedEntity t: entities)
        {
            result.add(t.getName());
        }
        return result;
    }

    private Hashtable<String, Object> extractDetails(Object obj)
    {
        if (!structDefs.containsKey(obj.getClass()))
        {
            throw new RuntimeException(String.format("Object of type '%s' is not supported by the remote interface.", obj.getClass().getName()));
        }
        
        Hashtable<String, Object> details = new Hashtable<String, Object>();
        for (String property : structDefs.get(obj.getClass()))
        {
            try
            {
                Object value = Ognl.getValue(property, obj);
                if (value != null)
                {
                    TypeSqueezer squeezer = Squeezers.findSqueezer(value.getClass());
                    details.put(property, squeezer.squeeze(value));
                }
            }
            catch (OgnlException e)
            {
                e.printStackTrace();
            }
            catch (SqueezeException e)
            {
                e.printStackTrace();
            }
        }
        return details;
    }

    private void validate(Object o) throws IllegalArgumentException
    {
        ValidationContext ctx = new PulseValidationContext(o);
        try
        {
            validationManager.validate(o, ctx);
        }
        catch (ValidationException e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }
        if (ctx.hasErrors())
        {
            if (ctx.hasFieldErrors())
            {
                String field = ctx.getFieldErrors().keySet().iterator().next();
                String message = ctx.getFieldErrors(field).iterator().next();
                throw new IllegalArgumentException(String.format("Field %s is invalid. Reason: %s", field, message));
            }
            if (ctx.hasActionErrors())
            {
                String message = ctx.getActionErrors().iterator().next();
                throw new IllegalArgumentException(String.format("The following error occured validating your request: %s", message));
            }
        }
    }

    private PulseFileDetails createFileDetails(Hashtable<String, Object> type) throws IllegalArgumentException
    {
        //TODO: This goes into the project type manager.
        String projectType = (String) type.remove("type");

        PulseFileDetails details;
        if ("ant".equals(projectType))
        {
            details = new AntPulseFileDetails();
        }
        else if ("executable".equals(projectType))
        {
            details = new ExecutablePulseFileDetails();
        }
        else if ("maven".equals(projectType))
        {
            details = new MavenPulseFileDetails();
        }
        else if ("maven2".equals(projectType))
        {
            details = new Maven2PulseFileDetails();
        }
        else if ("xcode".equals(projectType))
        {
            details = new XCodePulseFileDetails();
        }
        else if ("custom".equals(projectType))
        {
            details = new CustomPulseFileDetails();
        }
        else if ("versioned".equals(projectType))
        {
            details = new VersionedPulseFileDetails();
        }
        else
        {
            throw new IllegalArgumentException("Unknown project type: " + type);
        }
        setProperties(type, details);
        return details;
    }

    private Scm createScm(Hashtable<String, Object> details) throws IllegalArgumentException
    {
        //TODO: This goes into the ScmManager.
        String type = (String) details.remove("type");

        Scm scm;
        if ("cvs".equals(type))
        {
            scm = new Cvs();
        }
        else if ("svn".equals(type))
        {
            scm = new Svn();
        }
        else if ("perforce".equals(type))
        {
            scm = new P4();
        }
        else
        {
            throw new IllegalArgumentException("Unknown scm type: " + type);
        }

        setProperties(details, scm);

        return scm;
    }

    private void setProperties(Hashtable<String, Object> scmDetails, Object object)
    {
        OgnlUtils.setProperties(scmDetails, object);
    }

    private Project internalGetProject(String projectName)
    {
        Project project = projectManager.getProject(projectName);
        if (project == null)
        {
            throw new IllegalArgumentException("Unknown project '" + projectName + "'");
        }
        return project;
    }

    private BuildResult internalGetBuild(Project project, int id)
    {
        BuildResult build = buildManager.getByProjectAndNumber(project, id);
        if (build == null)
        {
            throw new IllegalArgumentException("Unknown build '" + id + "' for project '" + project.getName() + "'");
        }

        return build;
    }

    private List<Project> getProjectList(Vector<String> projects)
    {
        List<Project> members = new ArrayList<Project>(projects.size());
        for(String s: projects)
        {
            members.add(internalGetProject(s));
        }
        return members;
    }

    private BuildSpecification getBuildSpecification(Project project, String buildSpecification)
    {
        BuildSpecification spec;
        if(TextUtils.stringSet(buildSpecification))
        {
             spec = project.getBuildSpecification(buildSpecification);
        }
        else
        {
            spec = project.getDefaultSpecification();
        }
        
        if (spec == null)
        {
            throw new IllegalArgumentException("Unknown build specification '" + buildSpecification + "'");
        }

        return spec;
    }

    /**
     * Required resource.
     *
     * @param tokenManager instance
     */
    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    /**
     * Required resource.
     *
     * @param shutdownManager instance
     */
    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    /**
     * Required resource.
     *
     * @param userManager instance
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setCommitMessageTransformerManager(CommitMessageTransformerManager manager)
    {
        this.transformerManager = manager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager)
    {
        this.authenticationManager = authenticationManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(this);
    }

    public void handleEvent(Event evt)
    {
        // Rewire on startup to get the full token manager.
        ComponentContext.autowire(this);
        eventManager.unregister(this);
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { SystemStartedEvent.class } ;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
