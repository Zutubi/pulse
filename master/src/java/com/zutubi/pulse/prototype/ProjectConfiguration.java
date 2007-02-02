package com.zutubi.pulse.prototype;

import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The top-level model for all project configuration data.
 */
public class ProjectConfiguration
{
    public static final int TIMEOUT_NEVER = 0;

    /**
     * The checkout scheme defines the maner in which a projects source is bootstrapped.
     */
    public enum CheckoutScheme
    {
        /**
         * Always checkout a fresh copy of the project to the base directory.
         */
        CLEAN_CHECKOUT,

        /**
         * Keep a local copy of the project, update it to the required
         * revision and copy to a clean base directory to build.
         */
        CLEAN_UPDATE,

        /**
         * Keep a copy of the project, update to the required revision and
         * build in place.
         */
        INCREMENTAL_UPDATE
    }

    // Configuration

    private PulseFileDetails pulseFileDetails;
    // Will change...
    private Scm scm;
    private ChangeViewer changeViewer;
    private Map<String, CommitMessageTransformer> commitMessageTransformers;
    private Map<String, PostBuildAction> postBuildActions;
    private Map<String, CleanupRule> cleanupRules;

    // Build stages

    private Map<String, Stage> stages;

    // Build options

    private CheckoutScheme checkoutScheme = CheckoutScheme.CLEAN_CHECKOUT;
    private boolean isolateChangelists = false;
    private boolean retainWorkingCopy = false;
    private int timeout = TIMEOUT_NEVER;
    private boolean forceClean;

    // Properties

    private List<ResourceProperty> properties = new LinkedList<ResourceProperty>();
    private boolean prompt = false;

    // Security

    private List<ProjectAclEntry> projectAclEntries;

}
