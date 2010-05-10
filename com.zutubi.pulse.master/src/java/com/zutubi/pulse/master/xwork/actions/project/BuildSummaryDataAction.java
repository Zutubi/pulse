package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.model.*;
import static com.zutubi.pulse.master.committransformers.CommitMessageBuilder.processSubstitution;
import com.zutubi.pulse.master.committransformers.LinkSubstitution;
import com.zutubi.pulse.master.committransformers.Substitution;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.config.NamedConfigurationComparator;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;
import org.apache.velocity.context.Context;

import java.io.File;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Action to provide data for the build summary tab.
 */
public class BuildSummaryDataAction extends BuildStatusActionBase
{
    private static final Logger LOG = Logger.getLogger(BuildSummaryDataAction.class);
    
    private List<PersistentChangelist> changelists;
    private String responsibleOwner;
    private String responsibleComment;
    private boolean canClearResponsible = false;
    private List<ActionLink> actions = new LinkedList<ActionLink>();
    private List<RelatedLink> relatedLinks = new LinkedList<RelatedLink>();
    private Map<String, List<FeaturedArtifact>> featuredArtifacts = new LinkedHashMap<String, List<FeaturedArtifact>>();
    private List<BuildHookConfiguration> hooks;

    private SummaryData data;
    
    private SystemPaths systemPaths;

    public SummaryData getData()
    {
        return data;
    }

    public String getResponsibleOwner()
    {
        return responsibleOwner;
    }

    public String getResponsibleComment()
    {
        return responsibleComment;
    }

    public boolean isCanClearResponsible()
    {
        return canClearResponsible;
    }

    public List<PersistentChangelist> getChangelists()
    {
        return changelists;
    }

    public boolean canDeleteComment(final long id)
    {
        Comment comment = CollectionUtils.find(getBuildResult().getComments(), new EntityWithIdPredicate<Comment>(id));
        return comment != null && accessManager.hasPermission(AccessManager.ACTION_DELETE, comment);
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public List<RelatedLink> getRelatedLinks()
    {
        return relatedLinks;
    }

    public Map<String, List<FeaturedArtifact>> getFeaturedArtifacts()
    {
        return featuredArtifacts;
    }

    public List<BuildHookConfiguration> getHooks()
    {
        return hooks;
    }

    public String execute()
    {
        super.execute();

        final BuildResult result = getRequiredBuildResult();
        Project project = result.getProject();
        boolean canWrite = accessManager.hasPermission(AccessManager.ACTION_WRITE, project);
        if (canWrite)
        {
            ProjectConfiguration projectConfig = getRequiredProject().getConfig();
            hooks = CollectionUtils.filter(projectConfig.getBuildHooks().values(), new Predicate<BuildHookConfiguration>()
            {
                public boolean satisfied(BuildHookConfiguration hookConfiguration)
                {
                    return hookConfiguration.canManuallyTriggerFor(result);
                }
            });
            Collections.sort(hooks, new NamedConfigurationComparator());
        }
        else
        {
            hooks = Collections.emptyList();
        }

        Messages messages = Messages.getInstance(BuildResult.class);
        File contentRoot = systemPaths.getContentRoot();
        if (result.completed())
        {
            if (canWrite)
            {
                actions.add(ToveUtils.getActionLink(AccessManager.ACTION_DELETE, messages, contentRoot));
            }
        }
        else
        {
            if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, result))
            {
                actions.add(ToveUtils.getActionLink(BuildResult.ACTION_CANCEL, messages, contentRoot));
            }
        }

        ProjectResponsibility projectResponsibility = project.getResponsibility();
        if (projectResponsibility == null && accessManager.hasPermission(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY, project))
        {
            actions.add(ToveUtils.getActionLink(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY, messages, contentRoot));
        }

        if (projectResponsibility != null)
        {
            responsibleOwner = projectResponsibility.getMessage(getLoggedInUser());
            responsibleComment = projectResponsibility.getComment();

            if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY, project))
            {
                canClearResponsible = true;
                actions.add(ToveUtils.getActionLink(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY, messages, contentRoot));
            }
        }

        if (getLoggedInUser() != null)
        {
            actions.add(ToveUtils.getActionLink(BuildResult.ACTION_ADD_COMMENT, messages, contentRoot));
        }
        
        gatherRelatedLinks(result);
        gatherFeaturedArtifacts(result);

        try
        {
            ActionContext actionContext = ActionContext.getContext();
            VelocityManager velocityManager = VelocityManager.getInstance();
            Context context = velocityManager.createContext(actionContext.getValueStack(), ServletActionContext.getRequest(), ServletActionContext.getResponse());
            String mainPanel = renderTemplate("ajax/build-summary-main.vm", context, velocityManager);
            String rightPanel = renderTemplate("ajax/build-summary-right.vm", context, velocityManager);
            data = new SummaryData(mainPanel, rightPanel);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return ERROR;
        }

        return SUCCESS;
    }

    private void gatherRelatedLinks(BuildResult buildResult)
    {
        changelists = buildManager.getChangesForBuild(buildResult, true);

        List<LinkSubstitution> substitutions = gatherLinkSubstitutions(buildResult.getProject().getConfig());
        relatedLinks = new LinkedList<RelatedLink>();
        for (LinkSubstitution substitution: substitutions)
        {
            try
            {
                Pattern pattern = Pattern.compile(substitution.getExpression());
                for (PersistentChangelist changelist: changelists)
                {
                    Matcher matcher = pattern.matcher(changelist.getComment());
                    while (matcher.find())
                    {
                        relatedLinks.add(new RelatedLink(processSubstitution(substitution.getLinkUrl(), matcher), processSubstitution(substitution.getLinkText(), matcher)));
                    }
                }
            }
            catch (Exception e)
            {
                // Soldier on.
            }
        }
    }

    private List<LinkSubstitution> gatherLinkSubstitutions(ProjectConfiguration projectConfig)
    {
        List<LinkSubstitution> result = new LinkedList<LinkSubstitution>();
        for (CommitMessageTransformerConfiguration transformerConfig: projectConfig.getCommitMessageTransformers().values())
        {
            List<Substitution> substitutions = transformerConfig.substitutions();
            for (Substitution substitution: substitutions)
            {
                if (substitution instanceof LinkSubstitution)
                {
                    LinkSubstitution linkSubstitution = (LinkSubstitution) substitution;
                    if( !result.contains(linkSubstitution))
                    {
                        result.add(linkSubstitution);
                    }
                }
            }
        }

        return result;
    }

    private void gatherFeaturedArtifacts(final BuildResult result)
    {
        for (final RecipeResultNode node: result.getRoot().getChildren())
        {
            if (node.getResult().completed())
            {
                List<FeaturedArtifact> stageFeaturedArtifacts = new LinkedList<FeaturedArtifact>();
                for (final CommandResult commandResult: node.getResult().getCommandResults())
                {
                    List<StoredArtifact> commandFeaturedArtifacts = CollectionUtils.filter(commandResult.getArtifacts(), new Predicate<StoredArtifact>()
                    {
                        public boolean satisfied(StoredArtifact storedArtifact)
                        {
                            return storedArtifact.isFeatured();
                        }
                    });

                    final String baseUrl = configurationManager.getSystemConfig().getContextPathNormalised();
                    final Urls urls = new Urls(baseUrl);
                    CollectionUtils.map(commandFeaturedArtifacts, new Mapping<StoredArtifact, FeaturedArtifact>()
                    {
                        public FeaturedArtifact map(StoredArtifact artifact)
                        {
                            String icon;
                            String url;

                            if (artifact.isLink())
                            {
                                icon = "link";
                                url = artifact.getUrl();
                            }
                            else if (artifact.isSingleFile())
                            {
                                StoredFileArtifact file = artifact.getFile();
                                if (file.canDecorate())
                                {
                                    icon = "decorate";
                                    url = urls.commandArtifacts(result, commandResult) + file.getPathUrl();
                                }
                                else
                                {
                                    icon = "download";
                                    url = urls.commandDownload(result, commandResult, file.getPath());
                                }
                            }
                            else if (artifact.hasIndexFile())
                            {
                                icon = "view";
                                url = urls.fileFileArtifact(artifact, artifact.findFileBase(artifact.findIndexFile()));
                            }
                            else
                            {
                                icon = "archive";
                                url = baseUrl + "/zip.action?path=pulse:///projects/" + result.getProject().getId() + "/builds/" + result.getId() + "/artifacts/" + node.getResult().getId() + "/" + commandResult.getId() + "/" + artifact.getId() + "/";
                            }

                            return new FeaturedArtifact(icon, url, artifact.getName());
                        }
                    }, stageFeaturedArtifacts);
                }

                if (stageFeaturedArtifacts.size() > 0)
                {
                    Collections.sort(stageFeaturedArtifacts);
                    featuredArtifacts.put(node.getStageName(), stageFeaturedArtifacts);
                }
            }
        }
    }

    private String renderTemplate(String template, Context context, VelocityManager velocityManager) throws Exception
    {
        StringWriter writer = new StringWriter();
        velocityManager.getVelocityEngine().mergeTemplate(template, context, writer);
        return writer.toString();
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }
    
    public static class SummaryData
    {
        private String mainPanel;
        private String rightPanel;

        public SummaryData(String mainPanel, String rightPanel)
        {
            this.mainPanel = mainPanel;
            this.rightPanel = rightPanel;
        }

        public String getMainPanel()
        {
            return mainPanel;
        }

        public String getRightPanel()
        {
            return rightPanel;
        }
    }

    public static class FeaturedArtifact implements Comparable
    {
        private static final Sort.StringComparator COMPARATOR = new Sort.StringComparator();

        private String icon;
        private String url;
        private String name;

        public FeaturedArtifact(String icon, String url, String name)
        {
            this.icon = icon;
            this.url = url;
            this.name = name;
        }

        public String getIcon()
        {
            return icon;
        }

        public String getUrl()
        {
            return url;
        }

        public String getName()
        {
            return name;
        }

        public int compareTo(Object o)
        {
            FeaturedArtifact other = (FeaturedArtifact) o;
            return COMPARATOR.compare(name, other.name);
        }
    }

    public static class RelatedLink
    {
        private String url;
        private String text;

        public RelatedLink(String url, String text)
        {
            this.url = url;
            this.text = text;
        }

        public String getUrl()
        {
            return url;
        }

        public String getText()
        {
            return text;
        }
    }
}
