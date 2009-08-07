package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task that refactors the dependency publication, from being explicitly
 * defined in the original implementation, to piggy backing on the existing
 * artifact configurations.
 */
public class RefactorDependencyPublicationUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        RemoveBuildStagePublications removeBuildStagePublications = new RemoveBuildStagePublications();
        removeBuildStagePublications.setRecordManager(recordManager);
        removeBuildStagePublications.execute();

        RemoveProjectDependenciesPublications removeProjectDependenciesPublications = new RemoveProjectDependenciesPublications();
        removeProjectDependenciesPublications.setRecordManager(recordManager);
        removeProjectDependenciesPublications.execute();

        AddPublishToFileSystemArtifactConfiguration addPublishToFileSystemArtifactConfiguration = new AddPublishToFileSystemArtifactConfiguration();
        addPublishToFileSystemArtifactConfiguration.setRecordManager(recordManager);
        addPublishToFileSystemArtifactConfiguration.execute();
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    private class RemoveBuildStagePublications extends AbstractRecordPropertiesUpgradeTask
    {
        protected RecordLocator getRecordLocator()
        {
            return RecordLocators.newPathPattern("projects/*/stages/*");
        }

        protected List<RecordUpgrader> getRecordUpgraders()
        {
            return Arrays.asList(RecordUpgraders.newDeleteProperty("publications"));
        }

        public boolean haltOnFailure()
        {
            return false;
        }
    }

    private class RemoveProjectDependenciesPublications extends AbstractRecordPropertiesUpgradeTask
    {
        protected RecordLocator getRecordLocator()
        {
            return RecordLocators.newPathPattern("projects/*/dependencies");
        }

        protected List<RecordUpgrader> getRecordUpgraders()
        {
            return Arrays.asList(RecordUpgraders.newDeleteProperty("publications"),
                    RecordUpgraders.newDeleteProperty("publicationPattern"));
        }

        public boolean haltOnFailure()
        {
            return true;
        }
    }

    private class AddPublishToFileSystemArtifactConfiguration extends AbstractRecordPropertiesUpgradeTask
    {
        protected RecordLocator getRecordLocator()
        {
            return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("projects/*/type/recipes/*/commands/*/artifacts/*"),
                "zutubi.fileSystemArtifactConfigSupport"
            ) ;
        }

        protected List<RecordUpgrader> getRecordUpgraders()
        {
            return Arrays.asList(RecordUpgraders.newAddProperty("publish", false),
                    RecordUpgraders.newAddProperty("artifactPattern", "(.+)\\.(.+)"));
        }

        public boolean haltOnFailure()
        {
            return true;
        }
    }
}
