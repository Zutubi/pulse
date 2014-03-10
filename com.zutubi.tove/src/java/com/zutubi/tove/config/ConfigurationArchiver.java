package com.zutubi.tove.config;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.health.ConfigurationHealthChecker;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.NullaryFunction;
import com.zutubi.validation.annotations.Required;

import java.io.File;
import java.util.*;

/**
 * Allows records to be archived to external files, then later restored into a configuration system.
 */
public class ConfigurationArchiver
{
    private RecordManager recordManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private ConfigurationHealthChecker configurationHealthChecker;

    /**
     * Archives records from the configuration system to a file.  The records may later be restored into a compatible
     * configuration system.  Note that references, including to template parents, are conserved within a set of records
     * that are archived together in a single call to this method.  References not within the set are removed and
     * therefore cannot be restored.
     *
     * @param file the file to archive the records in
     * @param mode determines how an existing file is handled
     * @param version version string to store in the archive, for verification at restore time
     * @param paths configuration paths to export from - each must refer to an item in a top-level map
     * @throws ToveRuntimeException on error writing to the given file
     */
    public void archive(File file, ArchiveMode mode, String version, String... paths)
    {
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        ArchiveRecord archive;
        if (file.exists() && mode == ArchiveMode.MODE_APPEND)
        {
            MutableRecord record = serialiser.deserialise(file);
            archive = new ArchiveRecord(record);
            if (!archive.getVersion().equals(version))
            {
                throw new ToveRuntimeException("Cannot append to an archive with version '" + archive.getVersion() + "' (this is version '" + version + "')");
            }
        }
        else
        {
            archive = new ArchiveRecord(version);
        }

        Multimap<String, String> scopeToItems = convertPaths(paths);
        for (String scope : scopeToItems.keys())
        {
            MapType mapType = (MapType) configurationTemplateManager.getType(scope);
            for (String item : scopeToItems.get(scope))
            {
                String path = PathUtils.getPath(scope, item);
                Record record = configurationTemplateManager.getRecord(path);
                MutableRecord mutableRecord;
                if (record instanceof TemplateRecord)
                {
                    mutableRecord = ((TemplateRecord) record).flatten(true);
                }
                else
                {
                    mutableRecord = record.copy(true, true);
                }

                ExternalStateRemovalFunction fn = new ExternalStateRemovalFunction(mapType.getTargetType(), mutableRecord, path);
                mutableRecord.forEach(fn);

                DecanonicaliseReferencesFunction decon = new DecanonicaliseReferencesFunction(mapType.getTargetType(), mutableRecord, path, recordManager, configurationReferenceManager);
                mutableRecord.forEach(decon);

                archive.addItem(scope, item, mutableRecord);
            }

            serialiser.serialise(file, archive.getRecord(), true);
        }
    }

    private Multimap<String, String> convertPaths(String... paths)
    {
        Multimap<String, String> result = ArrayListMultimap.create();
        for (String path : paths)
        {
            if (!configurationTemplateManager.pathExists(path))
            {
                throw new ToveRuntimeException("Invalid path '" + path + "': path does not exist");
            }

            String[] components = PathUtils.getPathElements(path);
            if (components.length != 2 || !(configurationTemplateManager.getType(components[0]) instanceof MapType))
            {
                throw new ToveRuntimeException("Invalid path '" + path + "': does not refer to a top-level map item");
            }

            result.put(components[0], components[1]);
        }

        return result;
    }

    /**
     * Checks a given archive file could be imported into this configuration system, and if so return the configuration
     * paths that it contains.
     *
     * @param file the archive file to check
     * @param versionChecker pluggable verification of the archive version before restoration
     * @return a list of paths included in the archive file
     * @throws ToveRuntimeException on error reading from the given file or verifying it can be imported
     */
    public List<String> checkArchive(final File file, VersionChecker versionChecker)
    {
        final ArchiveRecord archiveRecord = unpackAndVerifyArchive(file, versionChecker);
        List<String> paths = new ArrayList<String>();
        for (String scope : archiveRecord.getScopes())
        {
            Record scopeRecord = archiveRecord.getScope(scope);
            for (String item : scopeRecord.nestedKeySet())
            {
                paths.add(PathUtils.getPath(scope, item));
            }
        }

        return paths;
    }

    /**
     * Restores all records from an archive file, if version-compatible with this configuration system.  If the records
     * belong to a templated scope, any trees within the archive are restored as trees under the root template in that
     * scope (i.e. if the archive contains the template parent of an item that item will be restored under the restored
     * parent, else the item will be restored under the root).
     *
     * @param file the file to restore from
     * @param versionChecker pluggable verification of the archive version before restoration
     * @return a list of paths that were restored from the archive
     * @throws ToveRuntimeException on error reading from the given file or restoring the items
     */
    public List<String> restore(final File file, VersionChecker versionChecker)
    {
        final ArchiveRecord archiveRecord = unpackAndVerifyArchive(file, versionChecker);

        // References are tricky.  Some may refer to things that are not in the archive, and can be discarded.  But
        // we want to preserve any that refer to other things within the archive.  The tricky part is they use
        // handles that are rewritten as we insert the records here.  So we need to pull out internal references,
        // store them as paths, then later add them back in by looking for the paths.
        final Multimap<String, Long> pathToReferencedHandles = ArrayListMultimap.create();
        final Map<Long, String> handleToPath = new HashMap<Long, String>();

        extractReferences(archiveRecord, pathToReferencedHandles, handleToPath);

        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<List<String>>()
        {
            public List<String> process()
            {
                List<String> insertedPaths = new ArrayList<String>();
                for (String scope : archiveRecord.getScopes())
                {
                    MapType itemType = (MapType) configurationTemplateManager.getType(scope);
                    Set<String> existingItems = new HashSet<String>(itemType.getOrder(configurationTemplateManager.getRecord(scope)));
                    Record scopeRecord = archiveRecord.getScope(scope);
                    try
                    {
                        if (configurationTemplateManager.isTemplatedPath(scope))
                        {
                            // Import multiple items in inheritance order.  Find all within the archive that have no parent, and
                            // import them (in any order) and their children recursively.
                            Set<Long> handles = new HashSet<Long>();
                            for (String name : scopeRecord.nestedKeySet())
                            {
                                Record item = (Record) scopeRecord.get(name);
                                handles.add(item.getHandle());
                            }

                            long rootHandle = configurationTemplateManager.getRootInstance(scope, Configuration.class).getHandle();

                            configurationTemplateManager.suspendInstanceCache();
                            for (String name : scopeRecord.nestedKeySet())
                            {
                                MutableRecord item = (MutableRecord) scopeRecord.get(name);
                                long parentHandle = getTemplateParentHandle(item);
                                if (!handles.contains(parentHandle))
                                {
                                    importItemAndChildren(scope, itemType, existingItems, scopeRecord, item, rootHandle, insertedPaths);
                                }
                            }
                        }
                        else
                        {
                            configurationTemplateManager.suspendInstanceCache();
                            for (String name : scopeRecord.nestedKeySet())
                            {
                                MutableRecord item = (MutableRecord) scopeRecord.get(name);
                                ensureItemNameUnique(itemType, existingItems, item);
                                insertedPaths.add(configurationTemplateManager.insertRecord(scope, item, false));
                            }
                        }

                        restoreReferences(pathToReferencedHandles, handleToPath);

                        for (String insertedPath : insertedPaths)
                        {
                            configurationHealthChecker.healPath(insertedPath);
                        }
                    }
                    finally
                    {
                        configurationTemplateManager.resumeInstanceCache();
                    }

                    configurationTemplateManager.raiseInsertEvents(insertedPaths);
                }

                return insertedPaths;
            }
        });
    }

    private ArchiveRecord unpackAndVerifyArchive(File file, VersionChecker versionChecker)
    {
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        MutableRecord record = serialiser.deserialise(file);
        final ArchiveRecord archiveRecord = new ArchiveRecord(record);
        versionChecker.checkVersion(archiveRecord.getVersion());
        verifyScopes(file, archiveRecord);
        return archiveRecord;
    }

    private void ensureItemNameUnique(MapType type, Set<String> existingItems, MutableRecord item)
    {
        String itemKey = type.getItemKey(null, item);
        while (existingItems.contains(itemKey))
        {
            itemKey += " (restored)";
        }

        existingItems.add(itemKey);
        item.put(type.getKeyProperty(), itemKey);
    }

    private void verifyScopes(File file, ArchiveRecord archiveRecord)
    {
        for (String scope : archiveRecord.getScopes())
        {
            if (!configurationTemplateManager.getPersistentScopes().contains(scope))
            {
                throw new ToveRuntimeException("Archive '" + file.getAbsolutePath() + "' contains unrecognised scope '" + scope + "'");
            }

            ComplexType type = configurationTemplateManager.getType(scope);
            if (!(type instanceof MapType))
            {
                throw new ToveRuntimeException("Archive '" + file.getAbsolutePath() + "' contains invalid scope '" + scope + "' (not a map)");
            }
        }
    }

    private void extractReferences(ArchiveRecord archiveRecord, Multimap<String, Long> pathToReferencedHandles, Map<Long, String> handleToPath)
    {
        for (String scope : archiveRecord.getScopes())
        {
            MapType type = (MapType) configurationTemplateManager.getType(scope);
            Record scopeRecord = archiveRecord.getScope(scope);
            for (String name : scopeRecord.nestedKeySet())
            {
                MutableRecord item = (MutableRecord) scopeRecord.get(name);
                ReferenceExtractingFunction fn = new ReferenceExtractingFunction(type.getTargetType(), item, PathUtils.getPath(scope, name), pathToReferencedHandles, handleToPath);
                item.forEach(fn);
            }
        }
    }

    private long getTemplateParentHandle(Record item)
    {
        String handleString = item.getMeta(TemplateRecord.PARENT_KEY);
        long parentHandle = 0;
        if (handleString != null)
        {
            try
            {
                parentHandle = Long.parseLong(handleString);
            }
            catch (NumberFormatException e)
            {
                // Noop
            }
        }
        return parentHandle;
    }

    private void importItemAndChildren(String scope, MapType type, Set<String> existingItems, Record scopeRecord, MutableRecord item, long templateParentHandle, List<String> insertedPaths)
    {
        // Shallow copy so we don't modify the version in the archive (it confuses later processing).
        item = item.copy(false, true);
        long originalHandle = item.getHandle();

        ensureItemNameUnique(type, existingItems, item);
        item.putMeta(TemplateRecord.PARENT_KEY, Long.toString(templateParentHandle));
        String path = configurationTemplateManager.insertRecord(scope, item, false);
        insertedPaths.add(path);
        long newHandle = configurationTemplateManager.getRecord(path).getHandle();

        for (String name : scopeRecord.nestedKeySet())
        {
            MutableRecord otherItem = (MutableRecord) scopeRecord.get(name);
            long otherParentHandle = getTemplateParentHandle(otherItem);
            if (otherParentHandle == originalHandle)
            {
                importItemAndChildren(scope, type, existingItems, scopeRecord, otherItem, newHandle, insertedPaths);
            }
        }
    }

    private void restoreReferences(Multimap<String, Long> pathToReferencedHandles, Map<Long, String> handleToPath)
    {
        for (String refererPath : pathToReferencedHandles.keys())
        {
            Collection<Long> refereeHandles = pathToReferencedHandles.get(refererPath);
            List<String> newRefereeHandles = new ArrayList<String>(refereeHandles.size());
            for (Long refereeHandle : refereeHandles)
            {
                String refereePath = handleToPath.get(refereeHandle);
                if (refereePath != null)
                {
                    Record referee = configurationTemplateManager.getRecord(refereePath);
                    if (referee != null)
                    {
                        newRefereeHandles.add(Long.toString(referee.getHandle()));
                    }
                }
            }

            String toUpdatePath = PathUtils.getParentPath(refererPath);
            CompositeType type = configurationTemplateManager.getType(toUpdatePath, CompositeType.class);
            TypeProperty property = type.getProperty(PathUtils.getBaseName(refererPath));
            if (!newRefereeHandles.isEmpty())
            {
                MutableRecord toUpdate = configurationTemplateManager.getRecord(toUpdatePath).copy(false, true);
                if (property.getType() instanceof CollectionType)
                {
                    toUpdate.put(property.getName(), newRefereeHandles.toArray(new String[newRefereeHandles.size()]));
                }
                else
                {
                    toUpdate.put(property.getName(), newRefereeHandles.get(0));
                }

                configurationTemplateManager.saveRecord(toUpdatePath, toUpdate);
            }
            else if (property.getAnnotation(Required.class) != null)
            {
                // Rather than just removing the reference, remove the whole record (as it is
                // invalid without this reference anyway).
                configurationTemplateManager.delete(toUpdatePath);
            }
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setConfigurationHealthChecker(ConfigurationHealthChecker configurationHealthChecker)
    {
        this.configurationHealthChecker = configurationHealthChecker;
    }

    /**
     * Flags that indicate how to treat an existing file when archiving.
     */
    public enum ArchiveMode
    {
        /**
         * Add new records to the existing archive.  Note if records with the same path exist in the archive they will
         * be replaced.
         */
        MODE_APPEND,
        /**
         * Replace the existing archive with the new records.
         */
        MODE_OVERWRITE,
    }

    /**
     * Pluggable verification of archive version compatibility.
     */
    public interface VersionChecker
    {
        /**
         * Called back to verify an archive version before restoration.  Should throw {@link ToveRuntimeException} if
         * the version is not acceptable.
         *
         * @param version the version in the archive
         * @throws ToveRuntimeException if this configuration system cannot restore from the given archive version
         */
        public void checkVersion(String version) throws ToveRuntimeException;
    }

    /**
     * Uses a generic record to lay out an archive.  This record looks like:
     * <pre>
     * {
     *     version: &lt;version the archive was created by&gt;
     *     &lt;scope&gt;: {
     *         &lt;item name&gt;: &lt;item record&gt;
     *         ...
     *     }
     *     ...
     * }
     * </pre>
     */
    private static class ArchiveRecord
    {
        private static final String VERSION_KEY = "version";

        private MutableRecord record;
        private String version;

        public ArchiveRecord(String version)
        {
            this.version = version;
            record = new MutableRecordImpl();
            record.put(VERSION_KEY, version);
        }

        public ArchiveRecord(Record record)
        {
            Object o = record.get(VERSION_KEY);
            if (o == null)
            {
                throw new ToveRuntimeException("Invalid archive: version missing");
            }

            if (!(o instanceof String))
            {
                throw new ToveRuntimeException("Invalid archive: version has class '" + o.getClass() + "'");
            }

            this.version = (String) o;
            this.record = record.copy(true, true);
        }

        private MutableRecord getRecord()
        {
            return record;
        }

        public String getVersion()
        {
            return version;
        }

        public Set<String> getScopes()
        {
            return record.nestedKeySet();
        }

        public Record getScope(String scope)
        {
            return (Record) record.get(scope);
        }

        public void addItem(String scope, String name, Record item)
        {
            MutableRecord scopeRecord = (MutableRecord) getScope(scope);
            if (scopeRecord == null)
            {
                scopeRecord = new MutableRecordImpl();
                record.put(scope, scopeRecord);
            }

            scopeRecord.put(name, item);
        }
    }

    private class ExternalStateRemovalFunction extends TypeAwareRecordWalkingFunction
    {
        public ExternalStateRemovalFunction(ComplexType type, Record record, String path)
        {
            super(type, record, path);
        }

        @Override
        protected void process(String path, Record record, Type type)
        {
            if (type instanceof CompositeType)
            {
                TypeProperty externalStateProperty = ((CompositeType) type).getExternalStateProperty();
                if (externalStateProperty != null)
                {
                    ((MutableRecord) record).remove(externalStateProperty.getName());
                }
            }
        }
    }

    private class ReferenceExtractingFunction extends ReferenceUpdatingFunction
    {
        private Multimap<String, Long> pathToReferencedHandles;
        private Map<Long, String> handleToPath;

        public ReferenceExtractingFunction(ComplexType type, MutableRecord record, String path, Multimap<String, Long> pathToReferencedHandles, Map<Long, String> handleToPath)
        {
            super(type, record, path);
            this.pathToReferencedHandles = pathToReferencedHandles;
            this.handleToPath = handleToPath;
        }

        @Override
        public void process(String path, Record record, Type type)
        {
            handleToPath.put(record.getHandle(), path);
            super.process(path, record, type);
        }

        @Override
        protected void handleReferenceList(String path, Record record, TypeProperty property, String[] value)
        {
            String fullPath = PathUtils.getPath(path, property.getName());
            for (String handleString: value)
            {
                pathToReferencedHandles.put(fullPath, Long.parseLong(handleString));
            }

            super.handleReferenceList(path, record, property, value);
        }

        @Override
        protected void handleReference(String path, Record record, TypeProperty property, String value)
        {
            String fullPath = PathUtils.getPath(path, property.getName());
            pathToReferencedHandles.put(fullPath, Long.parseLong(value));
            super.handleReference(path, record, property, value);
        }

        @Override
        protected String updateReference(String value)
        {
            return null;
        }
    }
}
