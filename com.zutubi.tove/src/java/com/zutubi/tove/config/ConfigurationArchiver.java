package com.zutubi.tove.config;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.health.ConfigurationHealthChecker;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.NullaryFunction;

import java.io.File;
import java.util.*;

/**
 * Allows records to be archived to external files, then later restored into a configuration system.
 */
public class ConfigurationArchiver
{
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationHealthChecker configurationHealthChecker;

    public void archive(File file, ArchiveMode mode, String version, String scope, String... items)
    {
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        ArchiveRecord archive;
        if (file.exists() && mode == ArchiveMode.MODE_APPEND)
        {
            MutableRecord record = serialiser.deserialise(file);
            archive = new ArchiveRecord(record);
            if (!archive.getVersion().equals(version))
            {
                throw new ConfigurationException("Cannot append to an archive with version '" + archive.getVersion() + "' (this is version '" + version + "')");
            }
        }
        else
        {
            archive = new ArchiveRecord(version);
        }

        for (String item : items)
        {
            Record record = configurationTemplateManager.getRecord(PathUtils.getPath(scope, item));
            MutableRecord mutableRecord;
            if (record instanceof TemplateRecord)
            {
                mutableRecord = ((TemplateRecord) record).flatten(true);
            }
            else
            {
                mutableRecord = record.copy(true, true);
            }

            archive.addItem(scope, item, mutableRecord);
        }

        serialiser.serialise(file, archive.getRecord(), true);
    }

    public void restore(final File file, VersionChecker versionChecker)
    {
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        MutableRecord record = serialiser.deserialise(file);
        final ArchiveRecord archiveRecord = new ArchiveRecord(record);
        versionChecker.checkVersion(archiveRecord.getVersion());

        // References are tricky.  Some may refer to things that are not in the archive, and can be discarded.  But
        // we want to preserve any that refer to other things within the archive.  The tricky part is they use
        // handles that are rewritten as we insert the records here.  So we need to pull out internal references,
        // store them as paths, then later add them back in by looking for the paths.
        final Multimap<String, Long> pathToReferencedHandles = ArrayListMultimap.create();
        final Map<Long, String> handleToPath = new HashMap<Long, String>();

        extractReferences(file, archiveRecord, pathToReferencedHandles, handleToPath);

        configurationTemplateManager.executeInsideTransaction(new NullaryFunction<Void>()
        {
            public Void process()
            {
                List<String> insertedPaths = new ArrayList<String>();
                for (String scope : archiveRecord.getScopes())
                {
                    Record scopeRecord = archiveRecord.getScope(scope);
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
                        for (String name : scopeRecord.nestedKeySet())
                        {
                            MutableRecord item = (MutableRecord) scopeRecord.get(name);
                            long parentHandle = getTemplateParentHandle(item);
                            if (!handles.contains(parentHandle))
                            {
                                importItemAndChildren(scope, scopeRecord, item, rootHandle, insertedPaths);
                            }
                        }
                    }
                    else
                    {
                        for (String name : scopeRecord.nestedKeySet())
                        {
                            Record item = (Record) scopeRecord.get(name);
                            insertedPaths.add(configurationTemplateManager.insertRecord(scope, item));
                        }
                    }
                }

                restoreReferences(pathToReferencedHandles, handleToPath);

                for (String insertedPath : insertedPaths)
                {
                    configurationHealthChecker.healPath(insertedPath);
                }

                return null;
            }
        });
    }

    private void extractReferences(File file, ArchiveRecord archiveRecord, Multimap<String, Long> pathToReferencedHandles, Map<Long, String> handleToPath)
    {
        for (String scope : archiveRecord.getScopes())
        {
            if (!configurationTemplateManager.getPersistentScopes().contains(scope))
            {
                throw new ToveRuntimeException("Archive '" + file.getAbsolutePath() + "' contains unrecognised scope '" + scope + "'");
            }

            ComplexType type = configurationTemplateManager.getType(scope);
            if (!(type instanceof CollectionType))
            {
                throw new ToveRuntimeException("Archive '" + file.getAbsolutePath() + "' contains invalid scope '" + scope + "' (not a collection)");
            }

            Record scopeRecord = archiveRecord.getScope(scope);
            for (String name : scopeRecord.nestedKeySet())
            {
                MutableRecord item = (MutableRecord) scopeRecord.get(name);
                ReferenceExtractingFunction fn = new ReferenceExtractingFunction((ComplexType) type.getTargetType(), item, PathUtils.getPath(scope, name), pathToReferencedHandles, handleToPath);
                fn.process(item);
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

    private void importItemAndChildren(String scope, Record scopeRecord, MutableRecord item, long templateParentHandle, List<String> insertedPaths)
    {
        long originalHandle = item.getHandle();

        item.putMeta(TemplateRecord.PARENT_KEY, Long.toString(templateParentHandle));
        String path = configurationTemplateManager.insertRecord(scope, item);
        insertedPaths.add(path);
        long newHandle = configurationTemplateManager.getRecord(path).getHandle();

        for (String name : scopeRecord.nestedKeySet())
        {
            MutableRecord otherItem = (MutableRecord) scopeRecord.get(name);
            long otherParentHandle = getTemplateParentHandle(otherItem);
            if (otherParentHandle == originalHandle)
            {
                importItemAndChildren(scope, scopeRecord, otherItem, newHandle, insertedPaths);
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

            if (!newRefereeHandles.isEmpty())
            {
                String toUpdatePath = PathUtils.getParentPath(refererPath);
                MutableRecord toUpdate = configurationTemplateManager.getRecord(toUpdatePath).copy(false, true);
                CompositeType type = configurationTemplateManager.getType(toUpdatePath, CompositeType.class);
                TypeProperty property = type.getProperty(PathUtils.getBaseName(refererPath));
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
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
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
        MODE_APPEND,
        MODE_OVERWRITE,
    }

    /**
     * Pluggable verification of archive version compatibility.
     */
    public interface VersionChecker
    {
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
            super.handleReference(path, record, property, value);
        }

        @Override
        protected String updateReference(String value)
        {
            return null;
        }
    }
}
