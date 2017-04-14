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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.bean.ObjectFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Upgrade task that refactors the dependency publication, from being explicitly
 * defined in the original implementation, to piggy backing on the existing
 * artifact configurations.
 */
public class RefactorDependencyPublicationUpgradeTask extends AbstractUpgradeTask
{
    private ObjectFactory objectFactory;
    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        deleteAll("projects/*/stages/*/publications");
        deleteAll("projects/*/dependencies/publications");

        RemoveProjectDependenciesPublicationPattern removeProjectDependenciesPublicationPattern = objectFactory.buildBean(RemoveProjectDependenciesPublicationPattern.class);
        removeProjectDependenciesPublicationPattern.execute();

        AddPublishToFileSystemArtifactConfiguration addPublishToFileSystemArtifactConfiguration = objectFactory.buildBean(AddPublishToFileSystemArtifactConfiguration.class);
        addPublishToFileSystemArtifactConfiguration.execute();
    }

    private void deleteAll(String pathPattern)
    {
        Map<String,Record> records = recordManager.selectAll(pathPattern);
        for (String path: records.keySet())
        {
            recordManager.delete(path);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public static class RemoveProjectDependenciesPublicationPattern extends AbstractRecordPropertiesUpgradeTask
    {
        protected RecordLocator getRecordLocator()
        {
            return RecordLocators.newPathPattern("projects/*/dependencies");
        }

        protected List<? extends RecordUpgrader> getRecordUpgraders()
        {
            return Arrays.asList(RecordUpgraders.newDeleteProperty("publicationPattern"));
        }

        public boolean haltOnFailure()
        {
            return true;
        }
    }

    public static class AddPublishToFileSystemArtifactConfiguration extends AbstractRecordPropertiesUpgradeTask
    {
        protected RecordLocator getRecordLocator()
        {
            return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("projects/*/type/recipes/*/commands/*/artifacts/*"),
                "zutubi.fileSystemArtifactConfigSupport"
            ) ;
        }

        protected List<? extends RecordUpgrader> getRecordUpgraders()
        {
            return Arrays.asList(RecordUpgraders.newAddProperty("publish", "false"),
                    RecordUpgraders.newAddProperty("artifactPattern", "(.+)\\.(.+)"));
        }

        public boolean haltOnFailure()
        {
            return true;
        }
    }
}
