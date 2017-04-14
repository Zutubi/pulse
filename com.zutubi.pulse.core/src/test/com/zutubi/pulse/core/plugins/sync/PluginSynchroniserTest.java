/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.plugins.sync;

import com.google.common.base.Function;
import com.zutubi.pulse.core.plugins.*;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.PluginRepositoryException;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class PluginSynchroniserTest extends PulseTestCase
{
    private static final String ID_1 = "plugin.1";
    private static final String ID_2 = "plugin.2";
    private static final String ID_3 = "plugin.3";
    private static final String VERSION_1 = "1.0.0";
    private static final String VERSION_2 = "2.0.0";

    private static final PluginInfo INFO_1_VERSION_1 = new PluginInfo(ID_1, VERSION_1, PluginRepository.Scope.CORE);
    private static final PluginInfo INFO_1_VERSION_2 = new PluginInfo(ID_1, VERSION_2, PluginRepository.Scope.CORE);
    private static final PluginInfo INFO_2_VERSION_1 = new PluginInfo(ID_2, VERSION_1, PluginRepository.Scope.CORE);

    private TestPluginRepository pluginRepository;
    private PluginSynchroniser pluginSynchroniser;
    
    private List<Plugin> plugins = new LinkedList<Plugin>();
    
    private List<String> installs = new LinkedList<String>();
    private List<String> installRequests = new LinkedList<String>();
    private List<String> upgrades = new LinkedList<String>();
    private List<String> uninstalls = new LinkedList<String>();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // The mock plugin manager simply records plugin action requests in
        // the lists defined above.  It returns plugins that do similarly.
        PluginManager pluginManager = mock(PluginManager.class);
        doReturn(plugins).when(pluginManager).getPlugins();
        
        doAnswer(new Answer<Plugin>()
        {
            public Plugin answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return find(plugins, new PluginIdPredicate((String) invocationOnMock.getArguments()[0]), null);
            }
        }).when(pluginManager).getPlugin(Mockito.anyString());
        
        doAnswer(new Answer<Plugin>()
        {
            public Plugin answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                @SuppressWarnings({"unchecked"})
                List<URI> uris = (List<URI>) invocationOnMock.getArguments()[0];
                for (URI uri: uris)
                {
                    installs.add(uri.getPath());
                }
                return null;
            }
        }).when(pluginManager).installAll(Mockito.<List<URI>>anyObject());

        doAnswer(new Answer<Plugin>()
        {
            public Plugin answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                URI uri = (URI) invocationOnMock.getArguments()[0];
                installRequests.add(uri.getPath());
                return null;
            }
        }).when(pluginManager).requestInstall(Mockito.<URI>anyObject());
        
        pluginRepository = new TestPluginRepository();
        pluginSynchroniser = new PluginSynchroniser();
        pluginSynchroniser.setPluginManager(pluginManager);
    }

    private void addInstalledPlugin(final String id, final String version, boolean running) throws PluginException
    {
        Plugin plugin = mock(Plugin.class);
        
        doReturn(id).when(plugin).getId();
        doReturn(new PluginVersion(version)).when(plugin).getVersion();
        doReturn(running).when(plugin).isRunning();
        
        doAnswer(new Answer<Plugin>()
        {
            public Plugin answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                upgrades.add(id);
                return null;
            }
        }).when(plugin).upgrade(Mockito.<URI>anyObject());

        doAnswer(new Answer<Plugin>()
        {
            public Plugin answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                uninstalls.add(id);
                return null;
            }
        }).when(plugin).uninstall();
        
        plugins.add(plugin);
    }

    public void testNoPluginsAnywhere() throws PluginException
    {
        SynchronisationActions actions = pluginSynchroniser.determineRequiredActions(pluginRepository.getAvailablePlugins(PluginRepository.Scope.CORE));
        assertFalse(actions.isSyncRequired());
        assertFalse(actions.isRebootRequired());
        assertEquals(0, actions.getToInstall().size());
        assertEquals(0, actions.getToUpgrade().size());
        assertEquals(0, actions.getToUninstall().size());
        
        pluginSynchroniser.synchronise(pluginRepository, actions);
        assertActionsTaken(actions);
    }

    public void testInstallRequired() throws PluginException
    {
        pluginRepository.addPlugin(INFO_1_VERSION_1);
        
        SynchronisationActions actions = pluginSynchroniser.determineRequiredActions(pluginRepository.getAvailablePlugins(PluginRepository.Scope.CORE));
        assertTrue(actions.isSyncRequired());
        assertFalse(actions.isRebootRequired());
        assertEquals(asList(INFO_1_VERSION_1), actions.getToInstall());
        assertEquals(0, actions.getToUpgrade().size());
        assertEquals(0, actions.getToUninstall().size());
        
        pluginSynchroniser.synchronise(pluginRepository, actions);
        assertActionsTaken(actions);
    }

    public void testUpgradeRequired() throws PluginException
    {
        addInstalledPlugin(ID_1, VERSION_1, true);
        pluginRepository.addPlugin(INFO_1_VERSION_2);
        
        SynchronisationActions actions = pluginSynchroniser.determineRequiredActions(pluginRepository.getAvailablePlugins(PluginRepository.Scope.CORE));
        assertTrue(actions.isSyncRequired());
        assertTrue(actions.isRebootRequired());
        assertEquals(0, actions.getToInstall().size());
        assertEquals(asList(INFO_1_VERSION_2), actions.getToUpgrade());
        assertEquals(0, actions.getToUninstall().size());

        pluginSynchroniser.synchronise(pluginRepository, actions);
        assertActionsTaken(actions);
    }

    public void testUninstallRequired() throws PluginException
    {
        addInstalledPlugin(ID_1, VERSION_1, true);
        
        SynchronisationActions actions = pluginSynchroniser.determineRequiredActions(pluginRepository.getAvailablePlugins(PluginRepository.Scope.CORE));
        assertTrue(actions.isSyncRequired());
        assertTrue(actions.isRebootRequired());
        assertEquals(0, actions.getToInstall().size());
        assertEquals(0, actions.getToUpgrade().size());
        assertEquals(asList(ID_1), actions.getToUninstall());

        pluginSynchroniser.synchronise(pluginRepository, actions);
        assertActionsTaken(actions);
    }

    public void testMultipleActionsRequired() throws PluginException
    {
        addInstalledPlugin(ID_1, VERSION_1, true);
        addInstalledPlugin(ID_3, VERSION_1, true);
        
        pluginRepository.addPlugin(INFO_1_VERSION_2);
        pluginRepository.addPlugin(INFO_2_VERSION_1);
        
        SynchronisationActions actions = pluginSynchroniser.determineRequiredActions(pluginRepository.getAvailablePlugins(PluginRepository.Scope.CORE));
        assertTrue(actions.isSyncRequired());
        assertTrue(actions.isRebootRequired());
        assertEquals(asList(INFO_1_VERSION_2), actions.getToUpgrade());
        assertEquals(asList(INFO_2_VERSION_1), actions.getToInstall());
        assertEquals(asList(ID_3), actions.getToUninstall());

        pluginSynchroniser.synchronise(pluginRepository, actions);
        assertActionsTaken(actions);
    }
    
    public void testScopeRespected() throws PluginException
    {
        PluginInfo coreInfo = new PluginInfo(ID_1, VERSION_1, PluginRepository.Scope.CORE);
        PluginInfo serverInfo = new PluginInfo(ID_2, VERSION_1, PluginRepository.Scope.SERVER);
        PluginInfo masterInfo = new PluginInfo(ID_3, VERSION_1, PluginRepository.Scope.MASTER);
        pluginRepository.addPlugin(coreInfo);
        pluginRepository.addPlugin(serverInfo);
        pluginRepository.addPlugin(masterInfo);
        
        pluginSynchroniser.synchroniseWithRepository(pluginRepository, PluginRepository.Scope.SERVER);
        
        SynchronisationActions actions = new SynchronisationActions();
        actions.addInstall(coreInfo);
        actions.addInstall(serverInfo);
        assertActionsTaken(actions);
    }

    public void testNonRunningPluginsIgnored() throws PluginException
    {
        addInstalledPlugin(ID_1, VERSION_1, false);

        SynchronisationActions actions = pluginSynchroniser.determineRequiredActions(Collections.<PluginInfo>emptyList());
        assertFalse(actions.isSyncRequired());
    }
    
    private void assertActionsTaken(SynchronisationActions actions)
    {
        if (actions.isRebootRequired())
        {
            assertEquals(0, installs.size());
            assertEquals(transform(actions.getToInstall(), new PluginInfoToIdFunction()), installRequests);
            assertEquals(transform(actions.getToUpgrade(), new PluginInfoToIdFunction()), upgrades);
            assertEquals(actions.getToUninstall(), uninstalls);
        }
        else
        {
            assertEquals(transform(actions.getToInstall(), new PluginInfoToIdFunction()), installs);
            assertEquals(0, installRequests.size());
            assertEquals(0, upgrades.size());
            assertEquals(0, uninstalls.size());
        }
    }
    
    /**
     * A test plugin repository that allows infos to be directly added.  URIs
     * are returned with the path as the plugin id and the fragment as the
     * plugin version for convenience.
     */
    private static class TestPluginRepository implements PluginRepository
    {
        private Map<Scope, List<PluginInfo>> plugins = new HashMap<Scope, List<PluginInfo>>();
        
        public List<PluginInfo> getAvailablePlugins(Scope scope) throws PluginRepositoryException
        {
            List<PluginInfo> result = new LinkedList<PluginInfo>();
            for (Scope s: Scope.values())
            {
                if (s.ordinal() > scope.ordinal())
                {
                    break;
                }
                
                result.addAll(getPluginsDirectlyInScope(s));
            }
            
            return result;
        }

        public void addPlugin(PluginInfo info)
        {
            getPluginsDirectlyInScope(info.getScope()).add(info);
        }

        private List<PluginInfo> getPluginsDirectlyInScope(Scope scope)
        {
            List<PluginInfo> pluginInfos = plugins.get(scope);
            if (pluginInfos == null)
            {
                pluginInfos = new LinkedList<PluginInfo>();
                plugins.put(scope, pluginInfos);
            }
            return pluginInfos;
        }

        public URI getPluginLocation(PluginInfo pluginInfo)
        {
            try
            {
                return new URI(null, null, pluginInfo.getId(), pluginInfo.getVersion());
            }
            catch (URISyntaxException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static class PluginInfoToIdFunction implements Function<PluginInfo, String>
    {
        public String apply(PluginInfo pluginInfo)
        {
            return pluginInfo.getId();
        }
    }
}
