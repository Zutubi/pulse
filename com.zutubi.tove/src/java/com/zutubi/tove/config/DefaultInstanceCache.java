package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.UnaryProcedure;

import java.util.*;

/**
 * A cache for configuration instances, supporting addressing by path,
 * including paths with wildcards.
 */
class DefaultInstanceCache implements InstanceCache
{
    private Entry root = new Entry(true);
    private Map<String, Set<String>> referenceIndex = new HashMap<String, Set<String>>();

    public DefaultInstanceCache copyStructure()
    {
        DefaultInstanceCache copy = new DefaultInstanceCache();
        copy.root = root.copyStructure();

        copy.referenceIndex = new HashMap<String, Set<String>>();
        for (Map.Entry<String, Set<String>> entry: referenceIndex.entrySet())
        {
            copy.referenceIndex.put(entry.getKey(), new HashSet<String>(entry.getValue()));
        }

        return copy;
    }

    public boolean hasInstancesUnder(String path)
    {
        if(path.length() == 0)
        {
            return root.size() > 0;
        }
        else
        {
            return getEntry(path, true, null) != null;
        }
    }

    public void markInvalid(String path)
    {
        getEntry(path, true, new UnaryProcedure<Entry>()
        {
            public void run(Entry entry)
            {
                entry.markInvalid();
            }
        });
    }

    public boolean isValid(String path, boolean allowIncomplete)
    {
        DefaultInstanceCache.Entry entry = getEntry(path, allowIncomplete, null);
        return entry != null && entry.isValid();
    }

    public Configuration get(String path, boolean allowIncomplete)
    {
        Entry entry = getEntry(path, allowIncomplete, null);
        return entry == null ? null : entry.getInstance();
    }

    private Entry getEntry(String path, boolean allowIncomplete, UnaryProcedure<Entry> f)
    {
        return getEntry(root, PathUtils.getPathElements(path), 0, allowIncomplete, f);
    }

    private Entry getEntry(Entry entry, String[] elements, int index, boolean allowIncomplete, UnaryProcedure<Entry> f)
    {
        if(!allowIncomplete && !entry.complete)
        {
            return null;

        }
        if (f != null)
        {
            f.run(entry);
        }

        if (index == elements.length)
        {
            return entry;
        }

        entry = entry.getChild(elements[index]);
        return entry == null ? null : getEntry(entry, elements, index + 1, allowIncomplete, f);
    }

    public Collection<Configuration> getAllDescendants(String path, boolean allowIncomplete)
    {
        Collection<Configuration> result = new LinkedList<Configuration>();
        Entry entry = getEntry(path, allowIncomplete, null);
        if (entry != null)
        {
            entry.getAllDescendants(result, allowIncomplete);
        }
        return result;
    }

    public void getAllMatchingPathPattern(String path, Collection<Configuration> result, boolean allowIncomplete)
    {
        getAllMatchingPathPattern(path, Configuration.class, result, allowIncomplete);
    }

    public <T extends Configuration> void getAllMatchingPathPattern(String path, Class<T> clazz, Collection<T> result, boolean allowIncomplete)
    {
        getAll(root, PathUtils.getPathElements(path), 0, clazz, result, allowIncomplete);
    }

    private <T extends Configuration> void getAll(Entry entry, String[] elements, int index, Class<T> clazz, Collection<T> result, boolean allowIncomplete)
    {
        if (index == elements.length)
        {
            T instance = clazz.cast(entry.getInstance());
            if (instance != null && (entry.isComplete() || allowIncomplete))
            {
                result.add(instance);
            }

            return;
        }

        String pattern = elements[index];
        Map<String, Entry> children = entry.children;
        if (children != null)
        {
            for (Map.Entry<String, Entry> child : children.entrySet())
            {
                if (PathUtils.elementMatches(pattern, child.getKey()))
                {
                    getAll(child.getValue(), elements, index + 1, clazz, result, allowIncomplete);
                }
            }
        }
    }

    public void put(String path, Configuration instance, boolean complete)
    {
        put(instance, complete, root, PathUtils.getPathElements(path), 0);
    }

    public void forAllInstances(InstanceHandler handler, boolean allowIncomplete)
    {
        root.forAllInstances(null, allowIncomplete, "", handler);
    }

    private void put(Configuration instance, boolean complete, Entry entry, String[] elements, int index)
    {
        if (index == elements.length)
        {
            entry.setInstance(instance, complete);
            return;
        }

        put(instance, complete, entry.getOrCreateChild(elements[index], complete), elements, index + 1);
    }

    public void clearDirty()
    {
        root.prune(referenceIndex);
    }

    public Set<String> getInstancePathsReferencing(String path)
    {
        Set<String> instancesReferencing = referenceIndex.get(path);
        if (instancesReferencing == null)
        {
            return Collections.emptySet();
        }
        else
        {
            return instancesReferencing;
        }
    }

    public Set<String> getPropertyPathsReferencing(String path)
    {
        Set<String> instancesReferencing = referenceIndex.get(path);
        if (instancesReferencing == null)
        {
            return Collections.emptySet();
        }
        else
        {
            Set<String> result = new HashSet<String>();
            for (String instancePath: instancesReferencing)
            {
                Entry entry = getEntry(instancePath, true, null);
                for (Map.Entry<String, String> propertyReference: entry.getReferences().entrySet())
                {
                    if (propertyReference.getValue().equals(path))
                    {
                        result.add(PathUtils.getPath(instancePath, propertyReference.getKey()));
                    }
                }
            }

            return result;
        }
    }

    public void indexReference(String fromPropertyPath, String toPath)
    {
        String propertyPath = PathUtils.getBaseName(fromPropertyPath);
        String instancePath = PathUtils.getParentPath(fromPropertyPath);
        Entry entry = getEntry(instancePath, true, null);
        if (entry == null)
        {
            propertyPath = PathUtils.getPath(PathUtils.getBaseName(instancePath), propertyPath);
            instancePath = PathUtils.getParentPath(instancePath);
            entry = getEntry(instancePath, true, null);
        }

        addToReferenceIndex(instancePath, toPath);
        entry.addReference(propertyPath, toPath);
    }

    private void addToReferenceIndex(String fromPath, String toPath)
    {
        Set<String> index = referenceIndex.get(toPath);
        if (index == null)
        {
            index = new HashSet<String>();
            referenceIndex.put(toPath, index);
        }

        index.add(fromPath);
    }

    public boolean markDirty(String path)
    {
        Entry entry = getEntry(path, true, null);
        if (entry != null && !entry.isDirty())
        {
            entry.markDirty();
            return true;
        }

        return false;
    }

    private static class Entry
    {
        /**
         * Cached instance at this path.
         */
        private Configuration instance;
        /**
         * Created on demand to prevent wastage for the large numbers of
         * leaf instances.
         */
        private Map<String, Entry> children;
        /**
         * True if this entry and all children are valid.
         */
        private boolean valid = true;
        /**
         * True if this entry is complete.
         */
        private boolean complete;

        /**
         * True if this entry is dirty - i.e. the record underlying the
         * instance or some instance it reaches has been modified.
         */
        private boolean dirty = false;
        private Map<String, String> references;

        public Entry(boolean complete)
        {
            this.complete = complete;
        }

        public Configuration getInstance()
        {
            return instance;
        }

        public void setInstance(Configuration instance, boolean complete)
        {
            this.instance = instance;
            this.complete = complete;
        }

        public void addChild(String element, Entry entry)
        {
            if (children == null)
            {
                children = new HashMap<String, Entry>();
            }
            children.put(element, entry);
        }

        public Entry getChild(String element)
        {
            return children == null ? null : children.get(element);
        }

        public Entry getOrCreateChild(String element, boolean complete)
        {
            Entry child = getChild(element);
            if (child == null)
            {
                child = new Entry(complete);
                addChild(element, child);
            }

            return child;
        }

        public void getAllDescendants(Collection<Configuration> result, boolean allowIncomplete)
        {
            if(instance != null && (complete || allowIncomplete))
            {
                result.add(instance);
            }

            if (children != null)
            {
                for(Entry child: children.values())
                {
                    child.getAllDescendants(result, allowIncomplete);
                }
            }
        }

        public void forAllInstances(Configuration parentInstance, boolean allowIncomplete, String path, InstanceHandler handler)
        {
            if(instance != null && (complete || allowIncomplete))
            {
                handler.handle(instance, path, complete, parentInstance);
            }

            if (children != null)
            {
                for(Map.Entry<String,Entry> childEntry: children.entrySet())
                {
                    childEntry.getValue().forAllInstances(instance, allowIncomplete, PathUtils.getPath(path, childEntry.getKey()), handler);
                }
            }
        }

        public int size()
        {
            return children == null ? 0 : children.size();
        }

        public boolean isValid()
        {
            return valid;
        }

        public void markInvalid()
        {
            valid = false;
        }

        public boolean isComplete()
        {
            return complete;
        }

        public boolean isDirty()
        {
            return dirty;
        }

        public void markDirty()
        {
            this.dirty = true;
        }

        public Map<String, String> getReferences()
        {
            return references;
        }
        
        private void addReference(String relativePropertyPath, String configurationPath)
        {
            if (references == null)
            {
                references = new HashMap<String, String>();
            }

            references.put(relativePropertyPath, configurationPath);
        }

        public Entry copyStructure()
        {
            Entry copy = new Entry(complete);
            copy.instance = instance;
            copy.valid = valid;
            copy.dirty = dirty;
            if (children != null)
            {
                for (Map.Entry<String,Entry> child: children.entrySet())
                {
                    copy.addChild(child.getKey(), child.getValue().copyStructure());
                }
            }

            if (references != null)
            {
                copy.references = new HashMap<String, String>(references);
            }

            return copy;
        }

        /**
         * Processes dirty flags in this subtree, discarding empty entries
         * and resetting flags.
         *
         * @param referenceIndex current reference index, cleared of any cases
         *                       of pruned entries
         * @return true if this whole subtree is empty after the prune
         */
        public boolean prune(Map<String, Set<String>> referenceIndex)
        {
            if (dirty)
            {
                if (references != null)
                {
                    for (String referencedPath: references.values())
                    {
                        referenceIndex.get(referencedPath).remove(instance.getConfigurationPath());
                    }
                }

                instance = null;
                dirty = false;
                valid = true;
                references = null;
            }

            if (children != null)
            {
                Iterator<Map.Entry<String,Entry>> it = children.entrySet().iterator();
                while (it.hasNext())
                {
                    if (it.next().getValue().prune(referenceIndex))
                    {
                        // The child can be ditched.
                        it.remove();
                    }
                }

                if (children.size() == 0)
                {
                    children = null;
                }
            }

            return children == null && instance == null;
        }
    }
}
