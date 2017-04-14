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

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository;
import com.zutubi.pulse.master.servlet.PluginRepositoryServlet;
import static java.util.Arrays.asList;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SynchroniseCommandAcceptanceTest extends DevToolsTestBase
{
    public void testSyncNewInstall() throws Exception
    {
        File pluginDir = devPaths.getPluginStorageDir();

        assertFalse(pluginDir.exists());
        runPluginSync();
        assertTrue(pluginDir.exists());

        HttpPluginRepository repository = new HttpPluginRepository(AcceptanceTestUtils.getPulseUrl() + "/" + PluginRepositoryServlet.PATH_REPOSITORY);
        Collection<PluginInfo> corePlugins = repository.getAvailablePlugins(PluginRepository.Scope.CORE);
        assertEquals(corePlugins.size(), pluginDir.list(new SuffixFileFilter(".jar")).length);
    }

    public void testSecondSyncDoesNothing() throws Exception
    {
        runPluginSync();

        File pluginDir = devPaths.getPluginStorageDir();
        Set<String> pluginListing = new HashSet<String>(asList(pluginDir.list()));

        String output = runPluginSync();
        assertThat(output, containsString("Plugins are already up-to-date."));
        assertEquals(pluginListing, new HashSet<String>(asList(pluginDir.list())));
    }
}
