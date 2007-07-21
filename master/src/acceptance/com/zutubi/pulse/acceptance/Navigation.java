package com.zutubi.pulse.acceptance;

/**
 * Constants for navigating around the Pulse web UI.
 */
public class Navigation
{
    public static final String LOCATION_LOGIN = "login.action";
    
    public static final String LOCATION_ADMINISTRATION = "admin";
    public static final String LOCATION_PROJECT_CONFIG = LOCATION_ADMINISTRATION + "/projects/";

    public static final String TAB_ADMINISTRATION = "tab.administration";
    public static final String TAB_PROJECTS = "tab.projects";
    public static final String TAB_DASHBOARD = "tab.dashboard";
    public static final String TAB_QUEUES = "tab.queues";
    public static final String TAB_AGENTS = "tab.agents";

    public static final String LINK_LOGOUT = "logout";
    public static final String LINK_DASHBOARD_CONFIGURE = "configure.dashboard";

    public static class Projects
    {
        public static final String LINK_ADD_PROJECT = "project.add";

        public static final String TAB_CONFIGURATION = "tab.project.configuration";

        public static final String LINK_EDIT_CHANGE_VIEWER = "project.change.viewer.edit";
        public static final String ACTION_PROJECT_HOME = "/currentBuild.action";
        public static final String ACTION_PROJECT_CONFIG = "/configureProject.action";
    }

    public static class Agents
    {
        public static final String LINK_ADD_AGENTS = "agent.add";
    }

    public static class Administration
    {
        public static final String TAB_USERS = "tab.administration.users";
        public static final String TAB_GROUPS = "tab.administration.groups";

        public static final String LINK_EDIT_GENERAL = "general.edit";
        public static final String LINK_RESET_GENERAL = "general.reset";

        public static final String LINK_EDIT_JABBER = "jabber.edit";
        public static final String LINK_RESET_JABBER = "jabber.reset";

        public static final String LINK_EDIT_LDAP = "ldap.edit";
        public static final String LINK_RESET_LDAP = "ldap.reset";
    }
}
