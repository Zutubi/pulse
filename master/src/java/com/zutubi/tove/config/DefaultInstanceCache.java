package com.zutubi.tove.config;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.UnaryProcedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A cache for configuration instances, supporting addressing by path,
 * including paths with wildcards.
 */
class DefaultInstanceCache implements InstanceCache
{
    private Entry root = new Entry(null, true);

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
            public void process(Entry entry)
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
            f.process(entry);
        }

        if (index == elements.length)
        {
            return entry;
        }

        entry = entry.getChild(elements[index]);
        return entry == null ? null : getEntry(entry, elements, index + 1, allowIncomplete, f);
    }

    public Collection<Configuration> getAllDescendents(String path, boolean allowIncomplete)
    {
        Collection<Configuration> result = new LinkedList<Configuration>();
        Entry entry = getEntry(path, allowIncomplete, null);
        if (entry != null)
        {
            entry.getAllDescendents(result, allowIncomplete);
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
            entry.setInstance(instance);
            return;
        }

        put(instance, complete, entry.getOrCreateChild(elements[index], complete), elements, index + 1);
    }

    public void clear()
    {
        root = new Entry(null, true);
    }

    private class Entry
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

        public Entry(Configuration instance, boolean complete)
        {
            this.instance = instance;
            this.complete = complete;
        }

        public Configuration getInstance()
        {
            return instance;
        }

        public void setInstance(Configuration instance)
        {
            this.instance = instance;
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
                child = new Entry(null, complete);
                addChild(element, child);
            }

            return child;
        }

        public void getAllDescendents(Collection<Configuration> result, boolean allowIncomplete)
        {
            if(instance != null && (complete || allowIncomplete))
            {
                result.add(instance);
            }

            if (children != null)
            {
                for(Entry child: children.values())
                {
                    child.getAllDescendents(result, allowIncomplete);
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
    }
}
