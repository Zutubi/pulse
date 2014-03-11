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

        final RestoreContext context = new RestoreContext();

        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<List<String>>()
        {
            public List<String> process()
            {
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
                                    Record rootRecord = configurationTemplateManager.getRecord(recordManager.getPathForHandle(rootHandle));
                                    importItemAndChildren(context, scope, itemType, existingItems, scopeRecord, item, rootHandle, rootRecord);
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
                                ReferenceExtractingFunction fn = new ReferenceExtractingFunction(itemType.getTargetType(), item, PathUtils.getPath(scope, name), context);
                                item.forEach(fn);
                                context.insertedPaths.add(configurationTemplateManager.insertRecord(scope, item, false));
                            }
                        }

                        restoreReferences(context);

                        for (String insertedPath : context.insertedPaths)
                        {
                            configurationHealthChecker.healPath(insertedPath);
                        }
                    }
                    finally
                    {
                        configurationTemplateManager.resumeInstanceCache();
                    }

                    configurationTemplateManager.raiseInsertEvents(context.insertedPaths);
                }

                return context.insertedPaths;
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

    private String ensureItemNameUnique(MapType type, Set<String> existingItems, MutableRecord item)
    {
        String itemKey = type.getItemKey(null, item);
        while (existingItems.contains(itemKey))
        {
            itemKey += " (restored)";
        }

        existingItems.add(itemKey);
        item.put(type.getKeyProperty(), itemKey);
        return itemKey;
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

    private void importItemAndChildren(RestoreContext restoreContext, String scope, MapType type, Set<String> existingItems, Record scopeRecord, MutableRecord item, long templateParentHandle, Record existingTemplateParent)
    {
        // Shallow copy so we don't modify the version in the archive (it confuses later processing).
        item = item.copy(false, true);
        long originalHandle = item.getHandle();

        String name = ensureItemNameUnique(type, existingItems, item);
        item.putMeta(TemplateRecord.PARENT_KEY, Long.toString(templateParentHandle));

        ListItemRenamingFunction listRenameFn = new ListItemRenamingFunction(type.getTargetType(), item, PathUtils.getPath(scope, name), existingTemplateParent, restoreContext.listKeyRenames);
        item.forEach(listRenameFn);

        ReferenceExtractingFunction fn = new ReferenceExtractingFunction(type.getTargetType(), item, PathUtils.getPath(scope, name), restoreContext);
        item.forEach(fn);

        String path = configurationTemplateManager.insertRecord(scope, item, false);
        restoreContext.insertedPaths.add(path);
        long newHandle = configurationTemplateManager.getRecord(path).getHandle();

        for (String nested : scopeRecord.nestedKeySet())
        {
            MutableRecord otherItem = (MutableRecord) scopeRecord.get(nested);
            long otherParentHandle = getTemplateParentHandle(otherItem);
            if (otherParentHandle == originalHandle)
            {
                // We deliberately don't pass the item we just inserted as the
                // existingTemplateParent here, it's not desirable as we can identify inherited
                // list items within the archive directly based on their keys (this is why the
                // context carries around the map of previous list key renames).
                importItemAndChildren(restoreContext, scope, type, existingItems, scopeRecord, otherItem, newHandle, null);
            }
        }
    }

    private void restoreReferences(RestoreContext context)
    {
        for (String refererPath : context.pathToReferencedHandles.keys())
        {
            String toUpdatePath = PathUtils.getParentPath(refererPath);
            // The path may have already been removed if we deleted the equivalent from a template
            // parent because the required reference was missing.
            if (configurationTemplateManager.pathExists(toUpdatePath))
            {
                Collection<Long> refereeHandles = context.pathToReferencedHandles.get(refererPath);
                List<String> newRefereeHandles = new ArrayList<String>(refereeHandles.size());
                for (Long refereeHandle : refereeHandles)
                {
                    String refereePath = context.handleToPath.get(refereeHandle);
                    if (refereePath != null)
                    {
                        Record referee = configurationTemplateManager.getRecord(refereePath);
                        if (referee != null)
                        {
                            newRefereeHandles.add(Long.toString(referee.getHandle()));
                        }
                    }
                }

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

    /**
     * Holds state that is passed around throughout a restore.
     */
    private static class RestoreContext
    {
        private final List<String> insertedPaths;
        private final Map<String, String> listKeyRenames;
        private final Multimap<String, Long> pathToReferencedHandles;
        private final Map<Long, String> handleToPath;

        private RestoreContext()
        {
            insertedPaths = new ArrayList<String>();
            pathToReferencedHandles = ArrayListMultimap.create();
            handleToPath = new HashMap<Long, String>();
            listKeyRenames = new HashMap<String, String>();
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

    /**
     * List items are keyed by what should be unique numbers that increase over time.  Two list
     * items should only have the same key if they have a direct inheritance relationship. When
     * restoring lists from (potentially) another server, we need to somehow determine what items
     * should inherit from existing items and make sure they do by setting their key appropriately.
     * We also need to avoid accidental inheritance relationships (from key clashes) by rewriting
     * other keys to something unique.
     * <p/>
     * We don't have a foolproof way to determine which items should inherit, but if two items
     * are equal they almost certainly should, so we guess based on that.
     */
    private class ListItemRenamingFunction extends TypeAwareRecordWalkingFunction
    {
        private Map<String, String> previousRenames;
        private Record existingTemplateParent;

        public ListItemRenamingFunction(ComplexType type, Record record, String path, Record existingTemplateParent, Map<String, String> previousRenames)
        {
            super(type, record, path);
            this.existingTemplateParent = existingTemplateParent;
            this.previousRenames = previousRenames;
        }

        @Override
        protected void process(String path, Record record, Type type)
        {
            if (type instanceof ListType)
            {
                MutableRecord mutableRecord = (MutableRecord) record;
                Map<String, String> renames = new HashMap<String, String>();
                for (String key: mutableRecord.keySet())
                {
                    String newKey = previousRenames.get(key);
                    if (newKey == null)
                    {
                        newKey = getKeyOfInheritedEqualItem((ListType)type, path, mutableRecord, key);
                        if (newKey == null)
                        {
                            newKey = Long.toString(recordManager.allocateHandle());
                        }

                        previousRenames.put(key, newKey);
                    }

                    renames.put(key, newKey);
                }

                for (Map.Entry<String, String> entry: renames.entrySet())
                {
                    mutableRecord.put(entry.getValue(), mutableRecord.remove(entry.getKey()));
                }
            }
        }

        private String getKeyOfInheritedEqualItem(ListType listType, String path, MutableRecord record, String key)
        {
            if (existingTemplateParent != null)
            {
                for (TypeProperty property: ((CompositeType)listType.getTargetType()).getProperties())
                {
                    if (property.getType().getTargetType() instanceof ReferenceType)
                    {
                        // Don't do equality tests for items that include references, as they are
                        // meaningless and we handle references specially already.
                        return null;
                    }
                }

                Record existingTemplateParentList = existingTemplateParent.getPath(PathUtils.getSuffix(path, 2));
                if (existingTemplateParentList != null)
                {
                    Record value = (Record) record.get(key);
                    for (String existingKey: existingTemplateParentList.nestedKeySet())
                    {
                        Record existingValue = (Record) existingTemplateParentList.get(existingKey);
                        if (value.simpleEquals(existingValue))
                        {
                            return existingKey;
                        }
                    }
                }
            }

            return null;
        }
    }

    /**
     * References are tricky.  Some may refer to things that are not in the archive, and can be
     * discarded.  But we want to preserve any that refer to other things within the archive.  The
     * tricky part is they use handles that are rewritten as we insert the records (and potentially
     * rewrite list keys).  So we need rewrite list keys first, then pull out internal references,
     * store them as paths, then later add them back in by looking for the paths.
     */
    private class ReferenceExtractingFunction extends ReferenceUpdatingFunction
    {
        private RestoreContext restoreContext;

        public ReferenceExtractingFunction(ComplexType type, MutableRecord record, String path, RestoreContext restoreContext)
        {
            super(type, record, path);
            this.restoreContext = restoreContext;
        }

        @Override
        public void process(String path, Record record, Type type)
        {
            restoreContext.handleToPath.put(record.getHandle(), path);
            super.process(path, record, type);
        }

        @Override
        protected void handleReferenceList(String path, Record record, TypeProperty property, String[] value)
        {
            String fullPath = PathUtils.getPath(path, property.getName());
            for (String handleString: value)
            {
                restoreContext.pathToReferencedHandles.put(fullPath, Long.parseLong(handleString));
            }

            super.handleReferenceList(path, record, property, value);
        }

        @Override
        protected void handleReference(String path, Record record, TypeProperty property, String value)
        {
            String fullPath = PathUtils.getPath(path, property.getName());
            restoreContext.pathToReferencedHandles.put(fullPath, Long.parseLong(value));
            super.handleReference(path, record, property, value);
        }

        @Override
        protected String updateReference(String value)
        {
            return null;
        }
    }
}
