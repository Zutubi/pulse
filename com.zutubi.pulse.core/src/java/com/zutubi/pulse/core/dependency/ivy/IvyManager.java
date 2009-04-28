package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.dependency.DependencyManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import org.apache.ivy.core.module.status.Status;
import org.apache.ivy.core.module.status.StatusManager;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.Message;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

/**
 * The ivy implementation of the dependency manager interface.
 */
public class IvyManager implements DependencyManager
{
    // These status's are the default status's configured in ivy.  If / when we allow
    // customisation of the status's, these static fields will need to be revisited.
    // See org.apache.ivy.core.module.status.StatusManager for details.
    public static String STATUS_INTEGRATION   = "integration";
    public static String STATUS_MILESTONE     = "milestone";
    public static String STATUS_RELEASE       = "release";

    private DefaultIvyClientFactory ivyClientFactory = new DefaultIvyClientFactory();

    private IvySettings defaultSettings;

    public void init() throws IOException, ParseException
    {
        // redirect the default logging to our own logging system.
        Message.setDefaultLogger(new IvyMessageLoggerAdapter());

        defaultSettings = loadDefaultSettings();
    }

    private IvySettings loadDefaultSettings() throws ParseException, IOException
    {
        IvySettings settings = new IvySettings();
        settings.load(getClass().getResource("ivysettings.xml"));
        return settings;
    }

    /**
     * Get the default ivy settings.
     *
     * @return the default ivy settings instance.
     */
    public IvySettings getDefaultSettings()
    {
        return defaultSettings;
    }

    /**
     * Get the list of statuses available for use with builds.
     *
     * @return a list of strings representing valid statuses.
     */
    public List<String> getStatuses()
    {
        @SuppressWarnings("unchecked")
        List<Status> statues = (List<Status>) StatusManager.getCurrent().getStatuses();
        return CollectionUtils.map(statues, new Mapping<Status, String>()
        {
            public String map(Status s)
            {
                return s.getName();
            }
        });
    }

    /**
     * Get the default status.
     *
     * @return the default status.
     */
    public String getDefaultStatus()
    {
        return StatusManager.getCurrent().getDefaultStatus();
    }

    /**
     * @param repositoryBase    defines the base path to the internal pulse repository.  The
     * format of this field must be a valid uri.
     * @return  a new configured ivy support instance.
     *
     * @throws Exception on error.
     */
    public IvyClient createIvyClient(String repositoryBase) throws Exception
    {
        // validate the repository base parameter and fail early if there are any problems.
        new URI(repositoryBase);

        Map<String, String> variables = new HashMap<String, String>();
        variables.put(DefaultIvyClientFactory.VARIABLE_REPOSITORY_BASE, repositoryBase);
        return ivyClientFactory.createClient(variables);
    }

}
