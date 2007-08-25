package com.zutubi.prototype.config;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.UnaryFunction;

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
    private Entry root = new Entry(null);

    public boolean hasInstancesUnder(String path)
    {
        if(path.length() == 0)
        {
            return root.size() > 0;
        }
        else
        {
            return getEntry(path, null) != null;
        }
    }

    public void markInvalid(String path)
    {
        getEntry(path, new UnaryFunction<Entry>()
        {
            public void process(Entry entry)
            {
                entry.markInvalid();
            }
        });
    }

    public boolean isValid(String path)
    {
        DefaultInstanceCache.Entry entry = getEntry(path, null);
        return entry != null && entry.isValid();
    }

    public Configuration get(String path)
    {
        Entry entry = getEntry(path, null);
        return entry == null ? null : entry.getInstance();
    }

    private Entry getEntry(String path, UnaryFunction<Entry> f)
    {
        return getEntry(root, PathUtils.getPathElements(path), 0, f);
    }

    private Entry getEntry(Entry entry, String[] elements, int index, UnaryFunction<Entry> f)
    {
        if (f != null)
        {
            f.process(entry);
        }

        if (index == elements.length)
        {
            return entry;
        }

        entry = entry.getChild(elements[index]);
        return entry == null ? null : getEntry(entry, elements, index + 1, f);
    }

    public Collection<Configuration> getAllDescendents(String path)
    {
        Collection<Configuration> result = new LinkedList<Configuration>();
        Entry entry = getEntry(path, null);
        if (entry != null)
        {
            entry.getAllDescendents(result);
        }
        return result;
    }

    public void getAllMatchingPathPattern(String path, Collection<Configuration> result)
    {
        getAll(root, PathUtils.getPathElements(path), 0, result);
    }

    private void getAll(Entry entry, String[] elements, int index, Collection<Configuration> result)
    {
        if (index == elements.length)
        {
            Configuration instance = entry.getInstance();
            if (instance != null)
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
                    getAll(child.getValue(), elements, index + 1, result);
                }
            }
        }
    }

    public void put(String path, Configuration instance)
    {
        put(instance, root, PathUtils.getPathElements(path), 0);
    }

    public void forAllInstances(InstanceHandler handler)
    {
        root.forAllInstances(null, "", handler);
    }

    private void put(Configuration instance, Entry entry, String[] elements, int index)
    {
        if (index == elements.length)
        {
            entry.setInstance(instance);
            return;
        }

        put(instance, entry.getOrCreateChild(elements[index]), elements, index + 1);
    }

    public void clear()
    {
        root = new Entry(null);
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

        public Entry(Configuration instance)
        {
            this.instance = instance;
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

        public Entry getOrCreateChild(String element)
        {
            Entry child = getChild(element);
            if (child == null)
            {
                child = new Entry(null);
                addChild(element, child);
            }

            return child;
        }

        public void getAllDescendents(Collection<Configuration> result)
        {
            if(instance != null)
            {
                result.add(instance);
            }

            if (children != null)
            {
                for(Entry child: children.values())
                {
                    child.getAllDescendents(result);
                }
            }
        }

        public void forAllInstances(Configuration parentInstance, String path, InstanceHandler handler)
        {
            if(instance != null)
            {
                handler.handle(instance, path, parentInstance);
            }

            if (children != null)
            {
                for(Map.Entry<String,Entry> childEntry: children.entrySet())
                {
                    childEntry.getValue().forAllInstances(instance, PathUtils.getPath(path, childEntry.getKey()), handler);
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
    }
}
