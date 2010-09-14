package com.zutubi.pulse.core.plugins.repository;

import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginDependency;
import com.zutubi.pulse.core.plugins.PluginVersion;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import nu.xom.Document;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PluginListTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";
    
    private static final PluginInfo INFO_CORE = new PluginInfo("test.core", "1.0.0", PluginRepository.Scope.CORE);
    private static final PluginInfo INFO_SERVER = new PluginInfo("test.server", "2.2.0", PluginRepository.Scope.SERVER);
    private static final PluginInfo INFO_MASTER = new PluginInfo("test.master", "1.0.1", PluginRepository.Scope.MASTER);

    public void testToInfos()
    {
        Plugin plugin = makePluginWithDependencies();
        doReturn(INFO_CORE.getId()).when(plugin).getId();
        doReturn(new PluginVersion(INFO_CORE.getVersion())).when(plugin).getVersion();
        
        assertEquals(asList(INFO_CORE), PluginList.toInfos(asList(plugin)));
    }
    
    public void testPluginsToHashes()
    {
        Plugin plugin = makePluginWithDependencies();
        doReturn(INFO_CORE.getId()).when(plugin).getId();
        doReturn(new PluginVersion(INFO_CORE.getVersion())).when(plugin).getVersion();
        
        List<Hashtable<String, Object>> hashes = PluginList.pluginsToHashes(asList(plugin));
        assertEquals(1, hashes.size());
        Hashtable<String, Object> expected = new Hashtable<String, Object>();
        expected.put("id", INFO_CORE.getId());
        expected.put("version", INFO_CORE.getVersion());
        expected.put("scope", INFO_CORE.getScope().toString());
        assertEquals(expected, hashes.get(0));
    }
    
    public void testInfosToFromHashes()
    {
        List<PluginInfo> infos = asList(INFO_CORE, INFO_SERVER, INFO_MASTER);
        assertEquals(infos, PluginList.infosFromHashes(PluginList.infosToHashes(infos)));
    }
    
    public void testGetScopeNoDependencies()
    {
        assertEquals(PluginRepository.Scope.CORE, PluginList.getScope(makePluginWithDependencies()));
    }

    public void testGetScopeCoreDependency()
    {
        assertEquals(PluginRepository.Scope.CORE, PluginList.getScope(makePluginWithDependencies(PluginRepository.Scope.CORE.getDependencyId())));
    }

    public void testGetScopeServerDependency()
    {
        assertEquals(PluginRepository.Scope.SERVER, PluginList.getScope(makePluginWithDependencies(PluginRepository.Scope.SERVER.getDependencyId())));
    }
    
    public void testGetScopeMasterDependency()
    {
        assertEquals(PluginRepository.Scope.MASTER, PluginList.getScope(makePluginWithDependencies(PluginRepository.Scope.MASTER.getDependencyId())));
    }

    public void testGetScopeMultipleDependencies()
    {
        assertEquals(PluginRepository.Scope.MASTER, PluginList.getScope(makePluginWithDependencies(
                PluginRepository.Scope.CORE.getDependencyId(),
                PluginRepository.Scope.MASTER.getDependencyId(),
                PluginRepository.Scope.SERVER.getDependencyId()
        )));
    }
    
    public void testReadSimpleList() throws IOException
    {
        readAndCheck(INFO_CORE, INFO_SERVER, INFO_MASTER);
    }

    public void testReadIncompleteEntry() throws IOException
    {
        readAndCheck(INFO_CORE, INFO_MASTER);
    }

    public void testReadBadScope() throws IOException
    {
        readAndCheck(INFO_CORE, INFO_MASTER);
    }
    
    public void testRoundTripSingleEntry()
    {
        roundTrip(INFO_CORE);
    }

    public void testRoundMultipleSingleEntry()
    {
        roundTrip(INFO_CORE, INFO_SERVER, INFO_MASTER);
    }

    private Plugin makePluginWithDependencies(String... dependencyIds)
    {
        List<PluginDependency> dependencies = CollectionUtils.map(dependencyIds, new Mapping<String, PluginDependency>()
        {
            public PluginDependency map(String id)
            {
                return new PluginDependency(id, null, null);
            }
        });
        
        Plugin plugin = mock(Plugin.class);
        doReturn(dependencies).when(plugin).getRequiredPlugins();
        return plugin;
    }

    private void readAndCheck(PluginInfo... expected) throws IOException
    {
        List<PluginInfo> infoList = PluginList.read(getInput(EXTENSION_XML));
        assertEquals(asList(expected), infoList);
    }

    private void roundTrip(PluginInfo... infos)
    {
        List<PluginInfo> original = asList(infos);
        Document doc = PluginList.toXML(original);
        assertEquals(original, PluginList.fromXML(doc));
    }
}
