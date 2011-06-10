package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.master.xwork.actions.DefaultAction;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Numeric;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Nested composite to store user preferences under their own path.
 */
@SymbolicName("zutubi.userPreferencesConfig")
@Classification(single = "settings")
@Listing(order = {"dashboard", "browseView", "contacts", "subscriptions"})
@Form(labelWidth = 250, fieldOrder = {"aliases", "defaultAction", "myBuildsCount", "refreshingEnabled", "refreshInterval", "tailLines", "tailRefreshInterval"})
public class UserPreferencesConfiguration extends AbstractConfiguration
{
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_PROJECT = "project";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_REASON = "reason";
    public static final String COLUMN_REVISION = "revision";
    public static final String COLUMN_TESTS = "tests";
    public static final String COLUMN_WHEN = "when";
    public static final String COLUMN_ELAPSED = "elapsed";
    
    @StringList
    private List<String> aliases = new LinkedList<String>();
    @Select(optionProvider = "DefaultActionOptionProvider")
    private String defaultAction = DefaultAction.WELCOME_ACTION;
    @ControllingCheckbox(checkedFields = "refreshInterval")
    boolean refreshingEnabled = true;
    /**
     * Number of seconds between refreshes of "live" content, or 0 if the
     * user disables refreshing.
     */
    @Numeric(min = 1)
    private int refreshInterval = 60;
    @Numeric(min = 1)
    private int tailRefreshInterval = 60;
    @Numeric(min = 1)
    private int tailLines = 30;
    @Numeric(min = 1)
    private int myBuildsCount = 5;

    @Internal
    private String myBuildsColumns = defaultMyBuildsColumns();
    @Internal
    private String projectRecentColumns = defaultProjectColumns();
    @Internal
    private String projectHistoryColumns = defaultProjectColumns();
    @Internal
    private String serverHistoryColumns = defaultGlobalColumns();
    @Internal
    private String agentHistoryColumns = defaultGlobalColumns();

    @Internal
    private ProjectDependencyGraphBuilder.TransitiveMode dependencyTransitiveMode = ProjectDependencyGraphBuilder.TransitiveMode.FULL;

    private Map<String, ContactConfiguration> contacts = new HashMap<String, ContactConfiguration>();
    private Map<String, SubscriptionConfiguration> subscriptions = new HashMap<String, SubscriptionConfiguration>();
    private DashboardConfiguration dashboard = new DashboardConfiguration();
    private BrowseViewConfiguration browseView = new BrowseViewConfiguration();

    public UserPreferencesConfiguration()
    {
        setPermanent(true);
    }

    public List<String> getAliases()
    {
        return aliases;
    }

    public void setAliases(List<String> aliases)
    {
        this.aliases = aliases;
    }

    public String getDefaultAction()
    {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction)
    {
        this.defaultAction = defaultAction;
    }

    public boolean isRefreshingEnabled()
    {
        return refreshingEnabled;
    }

    public void setRefreshingEnabled(boolean refreshingEnabled)
    {
        this.refreshingEnabled = refreshingEnabled;
    }

    public int getRefreshInterval()
    {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval)
    {
        this.refreshInterval = refreshInterval;
    }

    public int getTailRefreshInterval()
    {
        return tailRefreshInterval;
    }

    public void setTailRefreshInterval(int tailRefreshInterval)
    {
        this.tailRefreshInterval = tailRefreshInterval;
    }

    public int getTailLines()
    {
        return tailLines;
    }

    public void setTailLines(int tailLines)
    {
        this.tailLines = tailLines;
    }

    public int getMyBuildsCount()
    {
        return myBuildsCount;
    }

    public void setMyBuildsCount(int myBuildsCount)
    {
        this.myBuildsCount = myBuildsCount;
    }

    public String getMyBuildsColumns()
    {
        return myBuildsColumns;
    }

    public void setMyBuildsColumns(String myBuildsColumns)
    {
        this.myBuildsColumns = myBuildsColumns;
    }

    public String getProjectRecentColumns()
    {
        return projectRecentColumns;
    }

    public void setProjectRecentColumns(String projectRecentColumns)
    {
        this.projectRecentColumns = projectRecentColumns;
    }

    public String getProjectHistoryColumns()
    {
        return projectHistoryColumns;
    }

    public void setProjectHistoryColumns(String projectHistoryColumns)
    {
        this.projectHistoryColumns = projectHistoryColumns;
    }

    public String getServerHistoryColumns()
    {
        return serverHistoryColumns;
    }

    public void setServerHistoryColumns(String serverHistoryColumns)
    {
        this.serverHistoryColumns = serverHistoryColumns;
    }

    public String getAgentHistoryColumns()
    {
        return agentHistoryColumns;
    }

    public void setAgentHistoryColumns(String agentHistoryColumns)
    {
        this.agentHistoryColumns = agentHistoryColumns;
    }

    public ProjectDependencyGraphBuilder.TransitiveMode getDependencyTransitiveMode()
    {
        return dependencyTransitiveMode;
    }

    public void setDependencyTransitiveMode(ProjectDependencyGraphBuilder.TransitiveMode dependencyTransitiveMode)
    {
        this.dependencyTransitiveMode = dependencyTransitiveMode;
    }

    public Map<String, ContactConfiguration> getContacts()
    {
        return contacts;
    }

    public void addContact(ContactConfiguration contactConfiguration)
    {
        contacts.put(contactConfiguration.getName(), contactConfiguration);
    }

    public void setContacts(Map<String, ContactConfiguration> contacts)
    {
        this.contacts = contacts;
    }

    public Map<String, SubscriptionConfiguration> getSubscriptions()
    {
        return subscriptions;
    }

    public void setSubscriptions(Map<String, SubscriptionConfiguration> subscriptions)
    {
        this.subscriptions = subscriptions;
    }

    public DashboardConfiguration getDashboard()
    {
        return dashboard;
    }

    public void setDashboard(DashboardConfiguration dashboard)
    {
        this.dashboard = dashboard;
    }

    public BrowseViewConfiguration getBrowseView()
    {
        return browseView;
    }

    public void setBrowseView(BrowseViewConfiguration browseView)
    {
        this.browseView = browseView;
    }

    public static String defaultMyBuildsColumns()
    {
        return StringUtils.join(",", COLUMN_NUMBER, COLUMN_PROJECT, COLUMN_STATUS, COLUMN_REASON, COLUMN_TESTS, COLUMN_WHEN, COLUMN_ELAPSED);
    }
    
    public static String defaultGlobalColumns()
    {
        return StringUtils.join(",", COLUMN_PROJECT, COLUMN_NUMBER, COLUMN_REVISION, COLUMN_STATUS, COLUMN_REASON, COLUMN_TESTS, COLUMN_WHEN, COLUMN_ELAPSED);
    }

    public static String defaultProjectColumns()
    {
        return StringUtils.join(",", COLUMN_NUMBER, COLUMN_REVISION, COLUMN_STATUS, COLUMN_REASON, COLUMN_TESTS, COLUMN_WHEN, COLUMN_ELAPSED);
    }
}
