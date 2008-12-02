package com.zutubi.pulse.master.api;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.scm.ScmLocation;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.FatController;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.AgentDisableRequestedEvent;
import com.zutubi.pulse.master.events.AgentEnableRequestedEvent;
import com.zutubi.pulse.master.model.*;
import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.api.AuthenticationException;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.*;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.*;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.AccessDeniedException;

import java.util.*;

/**
 * Implements a simple API for remote monitoring and control.
 */
public class RemoteApi
{
    private static final Logger LOG = Logger.getLogger(RemoteApi.class);

    private TokenManager tokenManager;
    private AccessManager accessManager;
    private EventManager eventManager;
    private ShutdownManager shutdownManager;
    private MasterConfigurationManager configurationManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationRefactoringManager configurationRefactoringManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationProvider configurationProvider;
    private TypeRegistry typeRegistry;
    private RecordManager recordManager;

    private ActionManager actionManager;
    private AgentManager agentManager;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private UserManager userManager;
    private ScmManager scmManager;
    private FatController fatController;

    public RemoteApi()
    {
    }

    /**
     * Retrieves the version number of this Pulse installation.  The number is of the form:
     * <p/>
     *     &lt;major&gt;&lt;minor&gt;&lt;build&gt;&lt;patch&gt;
     * <p/>
     * where &lt;major&gt; and &lt;minor&gt; are two digits and &lt;build&gt; and &lt;patch&gt; are
     * three digits.  The value of &lt;patch&gt; will always be 000 in regular builds.  For example,
     * version 2.0.12 would have build number 0200012000, so this method would return 200012000.
     *
     * @return the version of this Pulse installation as a build number.
     */
    public int getVersion()
    {
        Version v = Version.getVersion();
        return v.getBuildNumberAsInt();
    }

    /**
     * Authenticates as the given user and returns a token that can be used
     * as credentials for other method calls.  The returned token is valid
     * for 30 minutes or until explicitly invalidated by a call to
     * {@link #logout(String)}.
     *
     * @see #logout(String)
     *
     * @param username login of the user to authenticate as
     * @param password password of the user
     * @return an authentication token that can be used as credentials for
     *         other method calls
     * @throws AuthenticationException if the user does not exist or the
     *         password does not match
     */
    public String login(String username, String password)
    {
        return tokenManager.login(username, password);
    }

    /**
     * Explicitly invalidates the given authentication token, such that it
     * cannot be used for further method calls.
     *
     * @see #login(String, String)
     * 
     * @param token the token to invalidate
     * @return true if the given token was valid before this call
     */
    public boolean logout(String token)
    {
        return tokenManager.logout(token);
    }

    /**
     * A trivial ping method that can be useful for testing connectivity.
     *
     * @return the value "pong"
     */
    public String ping()
    {
        return "pong";
    }

    /**
     * Tests whether the given configuration path exists and is visible to
     * the logged in user.
     *
     * @param token authentication token (see {@link #login})
     * @param path the path to test, e.g. "projects/my project"
     * @return true iff the given configuration path exists and is visible to
     *         the logged in user
     * @access available to all users
     */
    public boolean configPathExists(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            return configurationTemplateManager.pathExists(path) && configurationSecurityManager.hasPermission(path, AccessManager.ACTION_VIEW);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Returns a list of sub paths that are nested under the given
     * configuration path.  For example, if the path "projects" is given, a
     * list of all project names will be returned. Paths not visible to the
     * logged in user are filtered out.
     *
     * @param token authentication token (see {@link #login})
     * @param path the path to list the sub paths of
     * @return all sub paths of the given path that are visible to the logged
     *         in user
     * @throws IllegalArgumentException if the given path is invalid
     * @access available to all users (paths not visible to the user are filtered)
     */
    public Vector<String> getConfigListing(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            Vector<String> result;

            if (path.length() == 0)
            {
                result = new Vector<String>(configurationTemplateManager.getRootListing());
            }
            else
            {
                Type type = configurationTemplateManager.getType(path);
                if (type instanceof CollectionType)
                {
                    CollectionType collectionType = (CollectionType) type;
                    if (collectionType.getCollectionType() instanceof ComplexType)
                    {
                        Record record = configurationTemplateManager.getRecord(path);
                        result = new Vector<String>(collectionType.getOrder(record));
                    }
                    else
                    {
                        throw new IllegalArgumentException("Path refers to simple collection");
                    }
                }
                else
                {
                    CompositeType compositeType = (CompositeType) type;
                    result = new Vector<String>(compositeType.getNestedPropertyNames());
                }
            }

            configurationSecurityManager.filterPaths(path, result, AccessManager.ACTION_VIEW);
            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Returns the name of the template parent for the configuration at the
     * given path.  The path mist refer to an element of a templated
     * collection, e.g. a project.
     *
     * @param token authentication token (see {@link #login})
     * @param path  path of the template to retrieve the parent for
     * @return the key of the template parent for the given path or the empty
     *         string if the path refers to the template root
     * @throws IllegalArgumentException if the given path does not refer to
     *         an element of a templated collection
     * @access available to users with view permission for the given path
     */
    public String getTemplateParent(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            TemplateNode node = getTemplateNode(path);
            TemplateNode parent = node.getParent();
            return parent == null ? "" : parent.getId();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Returns the names of all template children of the template at the
     * given path.  The path must refer to an element of a templated
     * collection, e.g. a project.
     *
     * @param token authentication token (see {@link #login})
     * @param path  path of the template to retrieve the children for
     * @return the keys of all template children for the given path that are
     *         visible to the logged-in user
     * @throws IllegalArgumentException if the given path does not refer to
     *         an element of a templated collection
     * @access available to users with view permission for the path; paths
     *         not visible to the user are filtered out of the result
     */
    public Vector<String> getTemplateChildren(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            TemplateNode node = getTemplateNode(path);

            List<String> names = CollectionUtils.map(node.getChildren(), new Mapping<TemplateNode, String>()
            {
                public String map(TemplateNode templateNode)
                {
                    return templateNode.getId();
                }
            });

            configurationSecurityManager.filterPaths(ConfigurationRegistry.PROJECTS_SCOPE, names, AccessManager.ACTION_VIEW);
            return new Vector<String>(names);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private TemplateNode getTemplateNode(String path)
    {
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);
        TemplateNode node = configurationTemplateManager.getTemplateNode(path);
        if(node == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': path does not refer to a member of a templated collection");
        }
        return node;
    }

    /**
     * Creates a default configuration object of the given type.  This object
     * will not necessarily be valid - some fields may be incomplete.
     *
     * @param token        authentication token (see {@link #login})
     * @param symbolicName symbolic name of the configuration type to create
     *                     an instance of, e.g. "zutubi.projectConfig"
     * @return {@xtype struct<[config|Remote API Configuration Objects]>}
     *         a default configuration object of the given type
     * @throws IllegalArgumentException if the given symbolic name is invalid
     * @throws TypeException if there is an error constructing the object
     * @access available to all users
     */
    public Hashtable<String, Object> createDefaultConfig(String token, String symbolicName) throws TypeException
    {
        tokenManager.verifyUser(token);
        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            throw new IllegalArgumentException("Unrecognised symbolic name '" + symbolicName + "'");
        }

        MutableRecord record = type.createNewRecord(true);
        return type.toXmlRpc(record);
    }

    /**
     * Returns the configuration object at the given path.  Configuration paths use forward slashes
     * as a separator, and are split into a few main scopes:
     * <ul>
     *   <li>projects</li>
     *   <li>agents</li>
     *   <li>users</li>
     *   <li>groups</li>
     *   <li>settings</li>
     * </ul>
     * So, for example, to retrieve the SCM for a project named "my project", you would use the path
     * "projects/my project/scm".  To discover other paths, use
     * {@link #getConfigListing(String, String)}.
     * <p/>
     * If the path refers to a templated object, this function will return the full configuration
     * including values inherited from template ancestors.
     *
     * @param token authentication token (see {@link #login})
     * @param path  the configuration path to look up (e.g. "projects/my project")
     * @return {@xtype struct<[config|Remote API Configuration Objects]>} the object at the given
     *         path
     * @throws IllegalArgumentException if the given path does not exist
     * @throws TypeException if there is an error processing the configuration object
     * @access available to users with view access for the path
     * @see #getConfigListing(String, String)
     * @see #getRawConfig(String, String) 
     */
    public Object getConfig(String token, String path) throws TypeException
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationProvider.get(path, Configuration.class);
            if (instance == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);

            Type t = configurationTemplateManager.getType(path);
            return t.toXmlRpc(t.unstantiate(instance));
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Returns the unique handle for the configuration object at the given path.  Handles are
     * constant, unique identifiers assigned to each configuration objects when they are created.
     *
     * @param token authentication token (see {@link #login})
     * @param path  the path to retrieve the handle for
     * @return the handle for the given path, as a 64-bit integer in string format
     * @throws IllegalArgumentException if the given path is invalid
     * @access available to users with view permission for the given path
     */
    public String getConfigHandle(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);

            Record record = configurationTemplateManager.getRecord(path);
            if (record == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            return Long.toString(record.getHandle());
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Retrieves the raw configuration object for the given path.  This may differ from the full
     * configuration returned by {@link #getConfig(String, String)} when the path refers to an
     * object in a templated scope.  No values inherited from template ancestors are included in the
     *  result, only values defined directly at this path.  This information can be used to
     * determine which values are inherited, overridden or introduced at this level in the template
     * hierarchy.
     *
     * @param token authentication token (see {@link #login})
     * @param path  the path to retrieve the aw configuration for
     * @return {@xtype struct<[config|Remote API Configuration Objects]>} the raw configuration
     *         (i.e. with no inherited values) at the given path
     * @throws IllegalArgumentException if the given path does not exist
     * @throws TypeException if there is an error converting the object
     * @access available to users with view access to the given path
     * @see #getConfig(String, String) 
     */
    public Object getRawConfig(String token, String path) throws TypeException
    {
        tokenManager.loginUser(token);
        try
        {
            Record record = recordManager.select(path);
            if (record == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);
            Type t = configurationTemplateManager.getType(path);
            return t.toXmlRpc(record);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Indicates if the configuration object at the given path is marked as permanent.  Permanent
     * objects may not be cloned or deleted.
     *
     * @param token authentication token (see {@link #login})
     * @param path  the configuration path to test
     * @throws IllegalArgumentException if the given path does not exist
     * @return true if the given configuration object is marked permanent
     * @access available to users with view access for the given path
     */
    public boolean isConfigPermanent(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationProvider.get(path, Configuration.class);
            if(instance == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);
            return instance.isPermanent();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Tests if the configuration object at the given path is valid, i.e. that all fields on the
     * object pass validation rules.  Nested objects are not included in this check.
     *
     * @param token authentication token (see {@link #login})
     * @param path  the configuration path to check the validity of
     * @return true if the fields on the given configuration object are all valid, false otherwise
     * @throws IllegalArgumentException if the given path is invalid
     * @access available to users with view permission for the given path
     */
    public boolean isConfigValid(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationProvider.get(path, Configuration.class);
            if(instance == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);
            return instance.isValid();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Adds a new configuration object to this server.  Configuration objects can be inserted into
     * collection paths (e.g. the project properties collecction), and also to paths that expect a
     * single complex object but are not yet configured (e.g. a project scm).  Objects that already
     * exist may not be updated using this function, use {@link #saveConfig(String, String, java.util.Hashtable, boolean)}
     * instead.
     * <p/>
     * The inserted object will be checked for type-correctness and validated before it is inserted.
     * A fault will be thrown if either check fails, and the path will remain unchanged.
     * <p/>
     * Properties not set in the passed in config will be given default values (where a default
     * exists).
     * <p/>
     * This function cannot be used to insert projects or agents, where more information regarding
     * the template hiearchy is required (see {@link #insertTemplatedConfig(String, String, java.util.Hashtable, boolean)}).
     * 
     * @param token  authentication token (see {@link #login})
     * @param path   the path to insert into, either a collection (e.g. "projects/my project/properties"
     *               or the path of a not-yet-configured singular nested object (e.g.
     *               "projects/my project/scm")
     * @param config {@xtype struct<[config|Remote API Configuration Objects]>} the configuration
     *               object to insert
     * @return the path of the inserted configuration
     * @throws IllegalArgumentException if the given path does not refer to a path the may be
     *         inserted into, or the the type of the given config does not match
     * @throws TypeException if the given config is malformed
     * @throws ValidationException if the given config fails validation
     * @access available to users with write permission for the given path
     * @see #insertTemplatedConfig(String, String, java.util.Hashtable, boolean)
     * @see #saveConfig(String, String, java.util.Hashtable, boolean)
     * @see #deleteConfig(String, String)
     */
    public String insertConfig(String token, String path, Hashtable config) throws TypeException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            ComplexType pathType = configurationTemplateManager.getType(path);
            CompositeType expectedType = (CompositeType) pathType.getTargetType();

            String parentPath;
            String baseName;
            if (pathType instanceof CollectionType)
            {
                parentPath = path;
                baseName = null;
            }
            else
            {
                parentPath = PathUtils.getParentPath(path);
                baseName = PathUtils.getBaseName(path);
            }

            if (configurationTemplateManager.isTemplatedCollection(parentPath))
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': use insertTemplatedConfig to insert into templated collections");
            }

            String symbolicName = CompositeType.getTypeFromXmlRpc(config);
            CompositeType type = configurationTemplateManager.typeCheck(expectedType, symbolicName);
            MutableRecord record = type.fromXmlRpc(config);

            Configuration instance = configurationTemplateManager.validate(parentPath, baseName, record, true, true);
            if (!type.isValid(instance))
            {
                throw new ValidationException(type, instance);
            }

            return configurationTemplateManager.insertRecord(path, record);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Adds a new configuration object to a templated collection on this server.  Currently two
     * templated collections are available, projects and agents.  the object may be either a
     * template or a concrete instance.  Objects that already exist may not be updated using this
     * function, see {@link #saveConfig(String, String, java.util.Hashtable, boolean)}.
     * <p/>
     * The inserted object will be type-checked and validated prior to being inserted.  A fault will
     * be thrown if either check fails and the configuration will remain unaffected.
     * <p/>
     * Properties not set in the passed in config will be given default values (where a default
     * exists).
     * <p/>
     * This function is only used for inserting into the top level of a templated collection.  To
     * insert at any other path, use {@link #insertConfig(String, String, java.util.Hashtable)}.
     *
     * @param token              authentication token (see {@link #login})
     * @param templateParentPath the path of the template parent that this new config should inherit
     *                           from, this also determines which templated collection the config is
     *                           being inserted into
     * @param config             {@xtype struct<[config|Remote API Configuration Objects]>} the
     *                           configuration object to insert
     * @param template           if true, the object will be inserted as a template, if false it
     *                           will be inserted as a concrete instance
     * @return the path of the inserted configuration
     * @throws IllegalArgumentException if the given template parent path does not refer to an
     *         existing template, or the given config is of a different type
     * @throws TypeException if the given config is malformed
     * @throws ValidationException if the given config fails validation
     * @access requires the server create permission for either projects or agents as appropriate
     * @see #insertConfig(String, String, java.util.Hashtable)
     * @see #saveConfig(String, String, java.util.Hashtable, boolean)
     * @see #deleteConfig(String, String)
     */
    public String insertTemplatedConfig(String token, String templateParentPath, Hashtable config, boolean template) throws TypeException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            String insertPath = PathUtils.getParentPath(templateParentPath);
            if (insertPath == null)
            {
                throw new IllegalArgumentException("Invalid templateParentPath '" + templateParentPath + "': no parent path");
            }

            if (!configurationTemplateManager.isTemplatedCollection(insertPath))
            {
                throw new IllegalArgumentException("Invalid templateParentPath '" + templateParentPath + "': parent path '" + insertPath + "' is not a templated collection, use insertConfig instead");
            }

            TemplateRecord templateParent = (TemplateRecord) configurationTemplateManager.getRecord(templateParentPath);
            if (templateParent == null)
            {
                throw new IllegalArgumentException("Invalid templateParentPath '" + templateParentPath + "': template parent does not exist");
            }

            if (configurationTemplateManager.isConcrete(insertPath, templateParent))
            {
                throw new IllegalArgumentException("Invalid templateParentPath '" + templateParentPath + "': template parent is concrete and thus cannot be inherited from");
            }

            CompositeType expectedType = (CompositeType) templateParent.getType();

            String symbolicName = CompositeType.getTypeFromXmlRpc(config);
            CompositeType type = configurationTemplateManager.typeCheck(expectedType, symbolicName);
            MutableRecord record = type.fromXmlRpc(config);
            configurationTemplateManager.setParentTemplate(record, templateParent.getHandle());
            if (template)
            {
                configurationTemplateManager.markAsTemplate(record);
            }

            Configuration instance = configurationTemplateManager.validate(insertPath, null, record, !template, true);
            if (!type.isValid(instance))
            {
                throw new ValidationException(type, instance);
            }

            return configurationTemplateManager.insertRecord(insertPath, record);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Updates the configuration object at the given path to match the given object.  A config
     * object must already exist at the given path, to add new configuration use
     * {@link #insertConfig(String, String, java.util.Hashtable)}.
     * <p/>
     * The given config object must be of exactly the same type as the existing config and will be
     * validated before being saved.  If the type check or validation fails a fault is raised and
     * the configuration is unchanged.
     * <p/>
     * If deep is true, then objects nested as complex properties of the given object will also be
     * updated.  During this process new configuration objects may be inserted, and existing ones
     * updated or deleted.  For example, if the given config contains a nested collection, the items
     * of the existing collection will be synchronised to match by inserting and deleting as
     * necessary.
     * <p/>
     * Properties not set in the passed in config will be given default values (where a default
     * exists).
     * <p/>
     * Collections may not be directly updated by saving to the collection path, rather their items
     * should be updated using {@link #insertConfig(String, String, java.util.Hashtable)} and
     * {@link #deleteConfig(String, String)}.
     *
     * @param token  authentication token (see {@link #login})
     * @param path   path to be updated with the new configuration
     * @param config {@xtype struct<[config|Remote API Configuration Objects]>} the new
     *               configuration
     * @param deep   if true, nested complex objects are updated recursively using the passed in
     *               configuration, if false, nested complex objects are unaffected by this call
     * @return the path of the saved configuration (may differ from the path passed in if the
     *         configuration is renamed)
     * @throws IllegalArgumentException if the path does not exist, the path refers to a collection,
     *         or the type of the given config does not match
     * @throws TypeException if the given config is malformed
     * @throws ValidationException if the given config fails validation
     * @access requires write permission for the given path
     * @see #insertConfig(String, String, java.util.Hashtable)
     * @see #deleteConfig(String, String)
     */
    public String saveConfig(String token, String path, Hashtable config, boolean deep) throws TypeException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            Record existingRecord = configurationTemplateManager.getRecord(path);
            if (existingRecord == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': no existing record found (use insert to create new records)");
            }

            String existingSymbolicName = existingRecord.getSymbolicName();
            if (existingSymbolicName == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': path refers to a collection (manipulate collections using insert and delete)");
            }

            String symbolicName = CompositeType.getTypeFromXmlRpc(config);
            if (!existingSymbolicName.equals(symbolicName))
            {
                throw new IllegalArgumentException("Expecting type '" + existingSymbolicName + "', found '" + symbolicName + "' (type cannot be changed by saving)");
            }

            CompositeType type = typeRegistry.getType(existingSymbolicName);
            MutableRecord record = type.fromXmlRpc(config);

            Configuration instance = configurationTemplateManager.validate(PathUtils.getParentPath(path), PathUtils.getBaseName(path), record, configurationTemplateManager.isConcrete(path), deep);
            if ((deep && !type.isValid(instance)) || !instance.isValid())
            {
                throw new ValidationException(type, instance);
            }

            return configurationTemplateManager.saveRecord(path, record, deep);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Tests whether the given configuration path can be cloned.  Only map elements (generally all
     * named configuration objects) that are not the root of a template hierarchy may be cloned.
     * This method does <b>not</b> verify whether the user actually has permission to perform the
     * clone: only that the path is cloneable.
     *
     * @param token authentication token (see {@link #login})
     * @param path  path to test
     * @return true if the given path exists and is cloneable
     * @access requires view permission for the given path
     * @see #cloneConfig(String, String, java.util.Hashtable)
     */
    public boolean canCloneConfig(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);
            return configurationRefactoringManager.canClone(path);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Clones elements of a map, producing exact replicas with the exception
     * of the keys which are changed.  The map can be a top-level map
     * (including templated collections) or a map nested anywhere in a
     * persistent scope.  The clone operation performs similarly in both
     * cases with only the parent references in templates being treated
     * specially (see below).
     * <p/>
     * Note that this method allows multiple elements of the same map to be
     * cloned in a single operation.  Multiple elements should be cloned
     * together when it is desirable to update references between them
     * (including parent references) to point to the new clones.  For
     * example, take two items map/a and map/b.  Say path map/a/ref is a
     * reference that points to map/b/foo.  If map/a and map/b are cloned
     * separately to form map/clonea and map/cloneb, then map/clonea/ref will
     * continue to point to map/b/foo.  If these two operations were done in
     * a single call to this method, however, then map/clonea/ref will point
     * to map/cloneb/foo.  Similarly, to clone a parent and child in a
     * template hierarchy and have the cloned parent be the parent of the
     * cloned child, they must be cloned in a single operation.  A member of
     * a template collection that is cloned without its parent being involved
     * in the same operation will result in a clone that has the original
     * parent.
     * <p/>
     * The keys are the names of the objects to clone, so for example the key
     * for a property name "ant.bin" is simply "ant.bin".  Each original key
     * must refer to an existing item in the map.
     * <p/>
     * Each new clone key must be unique in its template hierarchy and also
     * in the map itself.  For this reason no duplicate new keys are allowed.
     * <p/>
     * The root of a template hierarchy cannot be cloned as each hierarchy
     * can only have one root.
     *
     * @param token      authentication token (see {@link #login})
     * @param parentPath path of the map to clone elements of
     * @param keyMap     {@xtype struct<string:string>} map from original keys
     *                   (denoting the elements to clone) to clone keys (the
     *                   new key for each clone)
     * @return true
     * @throws IllegalArgumentException if a given path or key is invalid
     * @access requires write permission for the map path, or server create
     *         permission for projects or agents when cloning in those scopes
     * @see #canCloneConfig(String, String)
     */
    public boolean cloneConfig(String token, String parentPath, Hashtable<String, String> keyMap)
    {
        tokenManager.loginUser(token);
        try
        {
            configurationRefactoringManager.clone(parentPath, keyMap);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Deletes the configuration object at the given path, if one exists.  If the object is complex,
     * all nested objects will be deleted with it.  Further cleanup actions (e.g. deleting all build
     * results for a project) may also be triggered by a delete.  These actions are not reversible.
     * <p/>
     * The path must refer to either a collection item or an already-configured singular nested
     * object.  It is not possible to update simple properties this way (use
     * {@link #saveConfig(String, String, java.util.Hashtable, boolean)} on the parent path) or to
     * delete collections.
     * <p/>
     * If the path refers to a collection item that is inherited from an ancestor template, the item
     * will actually be <em>hidden</em>.  This has the same effect as deleting it from this path
     * (and all descendent paths), but the item will still exist in the template ancestor and may
     * later be restored using {@link #restoreConfig(String, String)}.  Note that in this case
     * cleanup actions may still be run, and those actions are not undone by restoration.
     * 
     * @param token authentication token (see {@link #login})
     * @param path  path of the configuration object to delete
     * @return true if the path existed and was deleted, false of the path did not exist
     * @access requires write access to the parent path, or the server delete permission for
     *         projects or agents when deleting those objects
     * @see #insertConfig(String, String, java.util.Hashtable)
     * @see #saveConfig(String, String, java.util.Hashtable, boolean)
     * @see #restoreConfig(String, String)
     * @see #deleteAllConfigs(String, String) 
     */
    public boolean deleteConfig(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            // Check view permission before giving information about the existence of a path
            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);
            if (configurationTemplateManager.getRecord(path) == null)
            {
                return false;
            }

            configurationTemplateManager.delete(path);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Deletes all objects that match a certain path pattern.  The pattern may include the character
     * '&#42;' as a wildcard to mean "any path element".  For example, to delete all properties in
     * project "my project", you could use path:
     * <p/>
     * projects/my project/properties/&#42;
     * <p/>
     * To delete all properties for all projects:
     * <p/>
     * projects/&#42;/properties/&#42;
     * <p/>
     * The wildcard may <strong>not</strong> be used to match a partial path element (hence
     * projects/my project/properties/foo&#42; is <strong>not</strong> valid).
     * <p/>
     * Any configuration objects matched by the path which may not be deleted (e.g. permanent
     * objects, or those which the user does not have permission to delete) will be left unchanged.
     * <p/>
     * Note that if the configuration
     *
     * @param token       authentication token (see {@link #login})
     * @param pathPattern pattern used to match paths to delete, may include the &#42; wildcard
     * @return the number of configuration objects delete (not counting nested objects)
     * @access available to all users, objects that may not be deleted by the user are ignored
     * @see #deleteConfig(String, String)
     */
    public int deleteAllConfigs(String token, String pathPattern)
    {
        tokenManager.loginUser(token);
        try
        {
            return configurationTemplateManager.deleteAll(pathPattern);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Restores a hidden inherited collection item at the given path.  The item must have previously
     * been hidden (perhaps by deleting the path using this API).  Restoring a hidden item has a
     * similar effect to adding a new item at the path, where the item inherits all details from an
     * existing item in a template ancestor.
     *
     * @param token authentication token (see {@link #login})
     * @param path  path of the item to restore
     * @return true
     * @throws IllegalArgumentException if the given path does not refer to a hidden collection item
     * @access requires write permission for the parent path
     * @see #deleteConfig(String, String)
     */
    public boolean restoreConfig(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            configurationTemplateManager.restore(path);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Sets the order of items in a collection.  The path given must refer to an existing ordered
     * collection.  The order is expressed as an array of keys, where keys match the names of
     * the collection items for maps or handles of collection items for lists.  Any items not
     * mentioned in the order will appear at the end of the collection in an arbitrary order.
     *
     * @param token authentication token (see {@link #login})
     * @param path  path of the collection to set the order of
     * @param order array of item keys in the desired order
     * @return true
     * @throws IllegalArgumentException if the path does not refer to an ordered collection, or any
     *         of the keys is invalid
     * @access requires write access to the given path
     * @see #getConfigHandle(String, String)
     * @see #getConfigListing(String, String)
     */
    public boolean setConfigOrder(String token, String path, Vector<String> order)
    {
        tokenManager.loginUser(token);
        try
        {
            configurationTemplateManager.setOrder(path, order);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Returns all available "actions" for a given configuration path.  Actions are dependent on the
     * configuration type.  For example, projects support a "trigger" action and agents a "ping"
     * action.  Only actions that the user has permission to perform are returned.
     * <p/>
     * Available actions can depend on the state of the configuration, for example only a disabled
     * agent may be enabled.
     * 
     * @param token authentication token (see {@link #login})
     * @param path  path of the configuration to retrieve the actions for
     * @return available actions for the given configuration path
     * @access available to all users, but the returned actions are filtered based on the actions
     *         the user has permission to perform
     * @see #doConfigAction(String, String, String)
     * @see #doConfigActionWithArgument(String, String, String, java.util.Hashtable)  
     */
    public Vector<String> getConfigActions(String token, String path)
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationProvider.get(path, Configuration.class);
            return new Vector<String>(actionManager.getActions(instance, false));
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean doConfigAction(String token, String path, String action)
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationProvider.get(path, Configuration.class);
            actionManager.execute(action, instance, null);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }

    }

    /**
     *
     * @param token
     * @param path
     * @param action
     * @param argument {@xtype struct<[config|Remote API Configuration Objects]>}
     * @return
     */
    public boolean doConfigActionWithArgument(String token, String path, String action, Hashtable argument) throws TypeException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationProvider.get(path, Configuration.class);

            String symbolicName = CompositeType.getTypeFromXmlRpc(argument);
            CompositeType type = typeRegistry.getType(symbolicName);
            MutableRecord record = type.fromXmlRpc(argument);
            Configuration arg = configurationTemplateManager.validate(null, null, record, true, true);
            if(!type.isValid(arg))
            {
                throw new ValidationException(type, arg);
            }


            actionManager.execute(action, instance, arg);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }

    }

    /**
     * @internal
     * Writes an error message to the log for testing.
     *
     * @param token   authentication token
     * @param message message to write
     * @return true
     */
    public boolean logError(String token, String message)
    {
        tokenManager.verifyAdmin(token);
        LOG.severe(message);
        return true;
    }

    /**
     * @internal
     * Writes a warning message to the log for testing.
     *
     * @param token   authentication token
     * @param message message to write
     * @return true
     */
    public boolean logWarning(String token, String message)
    {
        tokenManager.verifyAdmin(token);
        LOG.warning(message);
        return true;
    }

    /**
     * Indicates the number of users configured on this server.
     *
     * @param token authentication token, see {@link #login}
     * @return the total number of users
     * @throws AuthenticationException if the given token is invalid
     */
    public int getUserCount(String token)
    {
        tokenManager.loginUser(token);
        try
        {
            return userManager.getUserCount();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getAllUserLogins(String token)
    {
        tokenManager.loginUser(token);
        try
        {
            Collection<User> users = userManager.getAllUsers();
            Vector<String> result = new Vector<String>(users.size());
            for (User user : users)
            {
                result.add(user.getConfig().getLogin());
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Indicates the number of concrete projects configured on this server.
     *
     * @param token authentication token, see {@link #login}
     * @return the total number of concrete projects
     * @throws AuthenticationException if the given token is invalid
     */
    public int getProjectCount(String token)
    {
        tokenManager.loginUser(token);
        try
        {
            return projectManager.getProjectCount();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getAllProjectNames(String token)
    {
        tokenManager.loginUser(token);
        try
        {
            List<Project> projects = projectManager.getProjects(true);
            return getNames(projects);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getMyProjectNames(String token)
    {
        User user = tokenManager.loginAndReturnUser(token);
        try
        {
            List<Project> projects = new LinkedList<Project>();
            if (user != null)
            {
                projects.addAll(userManager.getUserProjects(user, projectManager));
            }

            return getNames(projects);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getAllProjectGroups(String token)
    {
        tokenManager.loginUser(token);
        try
        {
            Collection<ProjectGroup> groups = projectManager.getAllProjectGroups();
            Vector<String> result = new Vector<String>(groups.size());
            for (ProjectGroup g : groups)
            {
                result.add(g.getName());
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Hashtable<String, Object> getProjectGroup(String token, String name) throws IllegalArgumentException
    {
        tokenManager.loginUser(token);
        try
        {
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
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private Vector<String> getNames(Collection<Project> projects)
    {
        Vector<String> result = new Vector<String>(projects.size());
        CollectionUtils.map(projects, new Mapping<Project, String>()
        {
            public String map(Project project)
            {
                return project.getName();
            }
        }, result);

        return result;
    }

    /**
     * Indicates the number of concrete agents configured on this server.
     *
     * @param token authentication token, see {@link #login}
     * @return the total number of concrete agents
     * @throws AuthenticationException if the given token is invalid
     */
    public int getAgentCount(String token)
    {
        tokenManager.loginUser(token);
        try
        {
            return agentManager.getAgentCount();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getAllAgentNames(String token)
    {
        tokenManager.loginUser(token);
        try
        {
            List<Agent> agents = agentManager.getAllAgents();
            Vector<String> result = new Vector<String>(agents.size());
            CollectionUtils.map(agents, new Mapping<Agent, String>()
            {
                public String map(Agent agent)
                {
                    return agent.getConfig().getName();
                }
            }, result);

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     *
     * @param token
     * @param projectName
     * @param id
     * @return {@xtype array<[RemoteApi.BuildResult]>}
     */
    public Vector<Hashtable<String, Object>> getBuild(String token, String projectName, int id)
    {
        tokenManager.loginUser(token);
        try
        {
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(1);
            Project project = internalGetProject(projectName, true);
            BuildResult build = buildManager.getByProjectAndNumber(project, id);
            if (build == null)
            {
                return result;
            }

            result.add(convertResult(build));
            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean deleteBuild(String token, String projectName, int id)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
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

    public Vector<Hashtable<String, Object>> queryBuildsForProject(String token, String projectName, Vector<String> resultStates, int firstResult, int maxResults, boolean mostRecentFirst)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);

            List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, mapStates(resultStates), -1, -1, null, firstResult, maxResults, mostRecentFirst);
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
            for (BuildResult build : builds)
            {
                Hashtable<String, Object> buildDetails = convertResult(build);
                result.add(buildDetails);
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getBuildRange(String token, String projectName, int afterBuild, int toBuild)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            List<BuildResult> buildRange = buildManager.queryBuilds(project, ResultState.getCompletedStates(), afterBuild + 1, toBuild, 0, -1, false, false);
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(buildRange.size());
            for(BuildResult r: buildRange)
            {
                result.add(convertResult(r));
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getPreviousBuild(String token, String projectName, int id)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            BuildResult buildResult = internalGetBuild(project, id);
            buildResult = buildManager.getPreviousBuildResult(buildResult);
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();
            if(buildResult != null)
            {
                result.add(convertResult(buildResult));
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private ResultState[] mapStates(Vector<String> stateNames)
    {
        if(stateNames.size() > 0)
        {
            ResultState[] states = new ResultState[stateNames.size()];
            int i = 0;
            for(String stateName: stateNames)
            {
                states[i++] = ResultState.fromPrettyString(stateName);
            }

            return states;
        }
        else
        {
            return null;
        }
    }

    public int getNextBuildNumber(String token, String projectName)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            return (int) project.getNextBuildNumber();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getLatestBuildsForProject(String token, String projectName, boolean completedOnly, int maxResults)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);

            ResultState[] states = null;
            if (completedOnly)
            {
                states = ResultState.getCompletedStates();
            }

            List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, states, -1, -1, null, 0, maxResults, true);
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
            for (BuildResult build : builds)
            {
                Hashtable<String, Object> buildDetails = convertResult(build);
                result.add(buildDetails);
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getLatestBuildForProject(String token, String projectName, boolean completedOnly)
    {
        return getLatestBuildsForProject(token, projectName, completedOnly, 1);
    }

    public Vector<Hashtable<String, Object>> getLatestBuildsWithWarnings(String token, String projectName, int maxResults)
    {
        tokenManager.verifyUser(token);

        Project project = internalGetProject(projectName, true);
        List<BuildResult> builds = buildManager.queryBuildsWithMessages(new Project[]{project}, Feature.Level.WARNING, maxResults);
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for (BuildResult build : builds)
        {
            Hashtable<String, Object> buildDetails = convertResult(build);
            result.add(buildDetails);
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestBuildWithWarnings(String token, String projectName)
    {
        return getLatestBuildsWithWarnings(token, projectName, 1);
    }

    public Vector<Hashtable<String, Object>> getPersonalBuild(String token, int id)
    {
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(1);

        User user = tokenManager.loginAndReturnUser(token);
        try
        {
            BuildResult build = buildManager.getByUserAndNumber(user, id);
            if (build == null)
            {
                return result;
            }

            result.add(convertResult(build));
            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getLatestPersonalBuilds(String token, boolean completedOnly, int maxResults)
    {
        User user = tokenManager.loginAndReturnUser(token);
        try
        {
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
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getLatestPersonalBuild(String token, boolean completedOnly)
    {
        return getLatestPersonalBuilds(token, completedOnly, 1);
    }

    private Hashtable<String, Object> convertResult(BuildResult build)
    {
        Hashtable<String, Object> buildDetails = new Hashtable<String, Object>();
        buildDetails.put("id", (int) build.getNumber());
        buildDetails.put("project", build.getProject().getName());
        buildDetails.put("revision", getBuildRevision(build));
        addResultFields(build, buildDetails);

        Vector<Hashtable<String, Object>> stages = new Vector<Hashtable<String, Object>>();
        for(RecipeResultNode rrn: build.getRoot().getChildren())
        {
            stages.add(convertStage(rrn));
        }
        buildDetails.put("stages", stages);

        return buildDetails;
    }

    private String getBuildRevision(BuildResult build)
 	{
        Revision revision = build.getRevision();
        if(revision != null)
        {
            return revision.getRevisionString();
        }

         return "";
 	}

    private Hashtable<String, Object> convertStage(RecipeResultNode recipeResultNode)
    {
        Hashtable<String, Object> stage = new Hashtable<String, Object>();
        stage.put("name", recipeResultNode.getStageName());
        stage.put("agent", recipeResultNode.getHostSafe());
        addResultFields(recipeResultNode.getResult(), stage);
        return stage;
    }

    private void addResultFields(Result result, Hashtable<String, Object> buildDetails)
    {
        buildDetails.put("status", result.getState().getPrettyString());
        buildDetails.put("completed", result.completed());
        buildDetails.put("succeeded", result.succeeded());

        TimeStamps timeStamps = result.getStamps();
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
    }

    public Vector<Hashtable<String, Object>> getChangesInBuild(String token, String projectName, int id)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            final BuildResult build = buildManager.getByProjectAndNumber(project, id);
            if (build == null)
            {
                throw new IllegalArgumentException("Unknown build '" + id + "' for project '" + projectName + "'");
            }

            final Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();
            buildManager.executeInTransaction(new Runnable()
            {
                public void run()
                {
                    List<PersistentChangelist> changelists = buildManager.getChangesForBuild(build);
                    for(PersistentChangelist change: changelists)
                    {
                        result.add(convertChangelist(change));
                    }
                }
            });

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private Hashtable<String, Object> convertChangelist(PersistentChangelist change)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        if(change.getRevision() != null && change.getRevision().getRevisionString() != null)
        {
            result.put("revision", change.getRevision().getRevisionString());
        }
        if(change.getAuthor() != null)
        {
            result.put("author", change.getAuthor());
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
        for(PersistentFileChange file: change.getChanges())
        {
            files.add(convertChange(file));
        }
        result.put("files", files);

        return result;
    }

    private Hashtable<String, Object> convertChange(PersistentFileChange change)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        if(change.getFilename() != null)
        {
            result.put("file", change.getFilename());
        }
        if(change.getRevisionString() != null)
        {
            result.put("revision", change.getRevisionString());
        }
        if(change.getActionName() != null)
        {
            result.put("action", change.getActionName().toLowerCase());
        }

        return result;
    }

    /**
     * Returns an array of all artifacts captured in a given build.  Artifacts from all stages are
     * returned.  The returned structures indicate the context (stage and command) in which each
     * artifact was captured.
     *
     * @param token       authentication token, see {@link #login(String, String)}
     * @param projectName name of the project that owns the build
     * @param id          id of the build to retrieve the artifacts for
     * @return {@xtype array<[RemoteApi.Artifact]>} all artifacts captured in the given build
     * @throws IllegalArgumentException if the given project name or build is invalid
     * @access requires view permission for th given project
     */
    public Vector<Hashtable<String, Object>> getArtifactsInBuild(String token, final String projectName, final int id)
    {
        tokenManager.loginUser(token);
        try
        {
            final Project project = internalGetProject(projectName, true);
            final Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();

            buildManager.executeInTransaction(new Runnable()
            {
                public void run()
                {
                    final BuildResult build = internalGetBuild(project, id);

                    build.getRoot().forEachNode(new UnaryProcedure<RecipeResultNode>()
                    {
                        public void process(RecipeResultNode recipeResultNode)
                        {
                            RecipeResult recipeResult = recipeResultNode.getResult();
                            if(recipeResult != null)
                            {
                                String stage = recipeResultNode.getStageName();
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
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private Hashtable<String, Object> convertArtifact(StoredArtifact artifact, String project, BuildResult build, String stage, String command)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        result.put("id", Long.toString(artifact.getId()));
        result.put("stage", stage);
        result.put("command", command);
        result.put("name", artifact.getName());
        result.put("permalink", Urls.getBaselessInstance().commandDownload(project, Long.toString(build.getNumber()), stage, command, artifact.getName()));
        return result;
    }

    /**
     * Returns an array of all messages (errors, warnings and information) in a build.  Messages are
     * gathered at all levels: build, stage, command and artifact.  The returned structures contain
     * information about the context where the message was found.
     *
     * @param token       authentication token, see {@link #login(String, String)}
     * @param projectName the name of the project owning the build
     * @param id          id of the build to get the messages for
     * @return {@xtype array<[RemoteApi.Feature]>} all messages found for the given build
     * @throws IllegalArgumentException if the given project or build does not exist
     * @access requires view permission for the given project
     * @see #getInfoMessagesInBuild(String, String, int)
     * @see #getWarningMessagesInBuild(String, String, int)
     * @see #getErrorMessagesInBuild(String, String, int)
     */
    public Vector<Hashtable<String, String>> getMessagesInBuild(String token, String projectName, final int id)
    {
        tokenManager.loginUser(token);
        try
        {
            final Project project = internalGetProject(projectName, true);
            final Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();

            buildManager.executeInTransaction(new Runnable()
            {
                public void run()
                {
                    final BuildResult build = internalGetBuild(project, id);
                    build.loadFeatures(configurationManager.getDataDirectory());
                    for(PersistentFeature f: build.getFeatures())
                    {
                        result.add(convertFeature(null, null, null, null, f));
                    }

                    build.getRoot().forEachNode(new UnaryProcedure<RecipeResultNode>()
                    {
                        public void process(RecipeResultNode recipeResultNode)
                        {
                            RecipeResult recipeResult = recipeResultNode.getResult();
                            if(recipeResult != null)
                            {
                                String stage = recipeResultNode.getStageName();
                                for(PersistentFeature f: recipeResult.getFeatures())
                                {
                                    result.add(convertFeature(stage, null, null, null, f));
                                }

                                for(CommandResult commandResult: recipeResult.getCommandResults())
                                {
                                    String command = commandResult.getCommandName();
                                    for(PersistentFeature f: commandResult.getFeatures())
                                    {
                                        result.add(convertFeature(stage, command, null, null, f));
                                    }

                                    for(StoredArtifact artifact: commandResult.getArtifacts())
                                    {
                                        String artifactName = artifact.getName();
                                        for(StoredFileArtifact fileArtifact: artifact.getChildren())
                                        {
                                            String artifactPath = fileArtifact.getPath();
                                            for(PersistentFeature f: fileArtifact.getFeatures())
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
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Returns an array of all <strong>error</strong> messages in a build.  Messages are
     * gathered at all levels: build, stage, command and artifact.  The returned structures contain
     * information about the context where the message was found.
     *
     * @param token       authentication token, see {@link #login(String, String)}
     * @param projectName the name of the project owning the build
     * @param id          id of the build to get the messages for
     * @return {@xtype array<[RemoteApi.Feature]>} all error messages found for the given
     *         build
     * @throws IllegalArgumentException if the given project or build does not exist
     * @access requires view permission for the given project
     * @see #getMessagesInBuild(String, String, int)
     * @see #getInfoMessagesInBuild(String, String, int)
     * @see #getWarningMessagesInBuild(String, String, int)
     */
    public Vector<Hashtable<String, String>> getErrorMessagesInBuild(String token, String projectName, final int id)
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.ERROR);
    }

    /**
     * Returns an array of all <strong>warning</strong> messages in a build.  Messages are
     * gathered at all levels: build, stage, command and artifact.  The returned structures contain
     * information about the context where the message was found.
     *
     * @param token       authentication token, see {@link #login(String, String)}
     * @param projectName the name of the project owning the build
     * @param id          id of the build to get the messages for
     * @return {@xtype array<[RemoteApi.Feature]>} all warning messages found for the given
     *         build
     * @throws IllegalArgumentException if the given project or build does not exist
     * @access requires view permission for the given project
     * @see #getMessagesInBuild(String, String, int)
     * @see #getInfoMessagesInBuild(String, String, int)
     * @see #getErrorMessagesInBuild(String, String, int)
     */
    public Vector<Hashtable<String, String>> getWarningMessagesInBuild(String token, String projectName, final int id)
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.WARNING);
    }

    /**
     * Returns an array of all <strong>information</strong> messages in a build.  Messages are
     * gathered at all levels: build, stage, command and artifact.  The returned structures contain
     * information about the context where the message was found.
     *
     * @param token       authentication token, see {@link #login(String, String)}
     * @param projectName the name of the project owning the build
     * @param id          id of the build to get the messages for
     * @return {@xtype array<[RemoteApi.Feature]>} all information messages found for the given
     *         build
     * @throws IllegalArgumentException if the given project or build does not exist
     * @access requires view permission for the given project
     * @see #getMessagesInBuild(String, String, int)
     * @see #getWarningMessagesInBuild(String, String, int)
     * @see #getErrorMessagesInBuild(String, String, int)
     */
    public Vector<Hashtable<String, String>> getInfoMessagesInBuild(String token, String projectName, final int id)
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.INFO);
    }

    private Vector<Hashtable<String, String>> getMessagesOfLevel(String token, String projectName, int id, Feature.Level level)
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

    private Hashtable<String, String> convertFeature(String stageName, String commandName, String artifactName, String artifactPath, PersistentFeature feature)
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

    /**
     * Triggers a build of the given project at a floating revision.  The revision will be fixed as
     * laate as possible.  This function returns as soon as the request has been made.
     *
     * @param token       authentication token, see {@link #login(String, String)}
     * @param projectName the name of the project to trigger
     * @return true
     * @access requires trigger permission for the given project
     * @see #triggerBuild(String, String, String) 
     */
    public boolean triggerBuild(String token, String projectName)
    {
        return triggerBuild(token, projectName, null);
    }

    /**
     * Triggers a build of the given project at the given revision.  The revision will be verified
     * before requesting the build.  This function returns as soon as the request has been made.
     * 
     * @param token       authentication token, see {@link #login(String, String)}
     * @param projectName the name of the project to trigger
     * @param revision    the revision to build, in SCM-specific format (e.g. a revision number)
     * @return true
     * @access requires trigger permission for the given project
     * @see #triggerBuild(String, String) 
     */
    public boolean triggerBuild(String token, String projectName, final String revision)
    {
        tokenManager.loginUser(token);
        try
        {
            final Project project = internalGetProject(projectName, false);

            Revision r = null;
            if(TextUtils.stringSet(revision))
            {
                try
                {
                    r = withScmClient(project.getConfig(), scmManager, new ScmContextualAction<Revision>()
                    {
                        public Revision process(ScmClient client, ScmContext context) throws ScmException
                        {
                            if(client.getCapabilities(project.isInitialised()).contains(ScmCapability.REVISIONS))
                            {
                                return client.parseRevision(context, revision);
                            }
                            else
                            {
                                throw new IllegalArgumentException("Attempt to specify a revision to build when SCM does not support revisions");
                            }
                        }
                    });
                }
                catch (ScmException e)
                {
                    throw new IllegalArgumentException("Unable to verify revision: " + e.getMessage());
                }
            }

            projectManager.triggerBuild(project.getConfig(), new RemoteTriggerBuildReason(), r, "remote api", false, true);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Request that the given active build is cancelled.  This function returns when the request is
     * made, which is likely to be before the build is cancelled (if indeed it is cancelled).
     *
     * @param token       authentication token, see {@link #login(String, String)}
     * @param projectName the name of the project that is building
     * @param id          the id of the build to cancel
     * @return true if cancellation was requested, false if the build was not found or was not in
     *         progress
     * @throws IllegalArgumentException if the given project name is invalid
     * @access requires cancel build permission for the given project
     */
    public boolean cancelBuild(String token, String projectName, int id)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            BuildResult build = buildManager.getByProjectAndNumber(project, id);
            if (build == null || !build.inProgress())
            {
                return false;
            }
            else
            {
                fatController.terminateBuild(build, false);
                return true;
            }
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Retrieves the state of the given project.  Possible states include:
     * <ul>
     *   <li>initialising</li>
     *   <li>idle</li>
     *   <li>building</li>
     *   <li>paused</li>
     *   <li>initialise on idle</li>
     *   <li>pause on idle</li>
     * </ul>
     *
     * @param token        authentication token (see {@link #login})
     * @param projectName  name of the project to retrieve the state for
     * @return the current project state
     * @throws IllegalArgumentException if the project name is invalid
     * @access requires view permission for the project
     */
    public String getProjectState(String token, String projectName)
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            return project.getState().toString();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * @internal
     * Performs checks before a personal build is requested.
     *
     * @param token       authentication token (see {@link #login})
     * @param projectName name of the projec that the user wishes to run a personal build of
     * @return SCM configuration details for the project
     * @throws ScmException if there is an error retrieving SCM details
     */
    public Hashtable<String, String> preparePersonalBuild(String token, String projectName) throws ScmException
    {
        User user = tokenManager.loginAndReturnUser(token);
        if(!accessManager.hasPermission(userManager.getPrinciple(user), ServerPermission.PERSONAL_BUILD.toString(), null))
        {
            throw new AccessDeniedException("User does not have authority to submit personal build requests.");
        }

        try
        {
            final ProjectConfiguration projectConfig = internalGetProject(projectName, false).getConfig();
            return withScmClient(projectConfig, scmManager, new ScmContextualAction<Hashtable<String, String>>()
            {
                public Hashtable<String, String> process(ScmClient client, ScmContext context) throws ScmException
                {
                    Hashtable<String, String> scmDetails = new Hashtable<String, String>();
                    scmDetails.put(ScmLocation.TYPE, projectConfig.getScm().getType());
                    scmDetails.put(ScmLocation.LOCATION, client.getLocation());
                    return scmDetails;
                }
            });
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Pauses the given project.  When a project is paused, new triggers are ignored.  If the
     * project is currently building, it will be marked to pause on idle.  If it is currently
     * initialising, this request is ignored.
     *
     * @param token       authentication token (see {@link #login})
     * @param projectName name of the project to pause
     * @return true if the project was paused (or marked for pause on idle), false if pausing is not
     *         possible in the current project state
     * @throws IllegalArgumentException if the project name is invalid
     * @access available to users with pause permission for the project
     * @see #resumeProject(String, String)
     */
    public boolean pauseProject(String token, String projectName)
    {
        return doProjectStateTransition(token, projectName, Project.Transition.PAUSE);
    }

    /**
     * Resumes the given project if it is currently paused.  If the project is currently marked to
     * pause on idle, it will return to the building state.  If the project is currently
     * initialising, this request is ignored.
     *
     * @param token       authentication token (see {@link #login})
     * @param projectName name of the project to resume
     * @return true if the project was resumed, false if resuming is not possible in the current
     *         project state
     * @throws IllegalArgumentException if the project name is invalid
     * @access available to users with pause permission for the project
     * @see #pauseProject(String, String)
     */
    public boolean resumeProject(String token, String projectName)
    {
        return doProjectStateTransition(token, projectName, Project.Transition.RESUME);
    }

    /**
     * Requests initialisation of the given project.  This will force the
     * project to repeat the initialisation cycle from the beginning, even if
     * it was already successfully initialised.  If the project is currently
     * building, it will be marked to initialise on idle.
     *
     * @param token       authentication token (see {@link #login})
     * @param projectName name of the project to initialise
     * @return true if initialisation was requested for the project (or it was
     *         marked initialise on idle), false if initialising is not
     *         possible in the current project state
     * @throws IllegalArgumentException if the project name is invalid
     * @access available to users with write permission for the given project
     */
    public boolean initialiseProject(String token, String projectName)
    {
        return doProjectStateTransition(token, projectName, Project.Transition.INITIALISE);
    }

    private boolean doProjectStateTransition(String token, String projectName, Project.Transition transition)
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = internalGetProject(projectName, true);
            return projectManager.makeStateTransition(project.getId(), transition);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Returns the state of the given agent as a simple string.  Possible states are:
     * <ul>
     *   <li>awaiting ping</li>
     *   <li>building</li>
     *   <li>building invalid</li>
     *   <li>disabled</li>
     *   <li>idle</li>
     *   <li>initial</li>
     *   <li>invalid master</li>
     *   <li>offline</li>
     *   <li>post recipe</li>
     *   <li>recipe assigned</li>
     *   <li>token mismatch</li>
     *   <li>version mismatch</li>
     * </ul>
     *
     * @param token authentication token (see {@link #login})
     * @param name the name of the agent to retrieve the state for
     * @return the current state of the given agent
     * @throws IllegalArgumentException if the given agent does not exist
     * @access available to users with view permission for the given agent
     * @see #disableAgent(String, String)
     * @see #enableAgent(String, String)
     */
    public String getAgentStatus(String token, String name)
    {
        tokenManager.loginUser(token);
        try
        {
            Agent agent = agentManager.getAgent(name);
            if (agent == null)
            {
                throw new IllegalArgumentException("Unknown agent '" + name + "'");
            }

            return agent.getStatus().getPrettyString();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Enables a given disabled agent, allowing it to once more begin processing builds.  If the
     * agent is curently marked to disable on idle, it will be returned to the building
     * state.
     *
     * @param token authentication token (see {@link #login})
     * @param name  the name of the agent to enable
     * @return true
     * @throws IllegalArgumentException if the given agent does not exist
     * @access available to users with disable permission for the given agent
     * @see #disableAgent(String, String)
     * @see #getAgentStatus(String, String)
     */
    public boolean enableAgent(String token, String name)
    {
        tokenManager.loginUser(token);
        try
        {
            Agent agent = agentManager.getAgent(name);
            if (agent == null)
            {
                throw new IllegalArgumentException("Unknown agent '" + name + "'");
            }

            eventManager.publish(new AgentEnableRequestedEvent(this, agent));
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Disables the given agent, preventing it from processing builds until it is reenabled.  If the
     * agent is currently running a build, it will be marked to disable on idle.  If the agent is
     * already marked to disable on idle, it will be forcably disabled (terminating the build with
     * an error).
     *
     * @param token authentication token (see {@link #login})
     * @param name  the name of the agent to disable
     * @return true
     * @throws IllegalArgumentException if the given agent does not exist
     * @access available to users with disable permission for the given agent
     * @see #enableAgent(String, String)
     * @see #getAgentStatus(String, String)
     */
    public boolean disableAgent(String token, String name)
    {
        tokenManager.loginUser(token);
        try
        {
            Agent agent = agentManager.getAgent(name);
            if (agent == null)
            {
                throw new IllegalArgumentException("Unknown agent '" + name + "'");
            }

            eventManager.publish(new AgentDisableRequestedEvent(this, agent));
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Request that this server shut down.  This method will return after the request is made, which
     * is likely to be before the shutdown is complete.
     *
     * @param token   authentication token (see {@link #login})
     * @param force   if true, running builds will be terminated to force a faster shutdown, if
     *                false running builds will be allowed to complete
     * @param exitJvm if true, the JVM will be explicitly exited (rather than being allowed to exit
     *                when there are no more non-daemon threads)
     * @return true
     */
    public boolean shutdown(String token, boolean force, boolean exitJvm)
    {
        tokenManager.verifyAdmin(token);

        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        shutdownManager.delayedShutdown(force, exitJvm);
        return true;
    }

    /**
     * @internal
     * Shutdown function used by the service wrapper.
     *
     * @param token authentication token (see {@link #login})
     * @return true
     */
    public boolean stopService(String token)
    {
        tokenManager.verifyAdmin(token);
        shutdownManager.delayedStop();
        return true;
    }

    private Project internalGetProject(String projectName, boolean allownIvalid)
    {
        Project project = projectManager.getProject(projectName, allownIvalid);
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

    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                // Rewire on startup to get the full token manager.  Maybe there is a way to delay
                // the creation of this instance until after the context is fully initialised and
                // hence the objectFactory.buildBean(RemoteApi.class) will return a fully wired instance?.
                SpringComponentContext.autowire(RemoteApi.this);
            }
        });
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }
}
