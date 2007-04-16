package com.zutubi.prototype.config;

import com.zutubi.prototype.type.record.PathUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * A cache for configuration instances, supporting addressing by path,
 * including paths with wildcards.
 */
class InstanceCache
{
    private Entry root = new Entry(null);

    public Object get(String path)
    {
        return get(root, PathUtils.getPathElements(path), 0);
    }

    private Object get(Entry entry, String[] elements, int index)
    {
        if(index == elements.length)
        {
            return entry.getInstance();
        }

        entry = entry.getChild(elements[index]);
        return entry == null ? null : get(entry, elements, index + 1);
    }

    public void getAll(String path, List result)
    {
        getAll(root, PathUtils.getPathElements(path), 0, result);
    }

    @SuppressWarnings({"unchecked"})
    private void getAll(Entry entry, String[] elements, int index, List result)
    {
        if(index == elements.length)
        {
            Object instance = entry.getInstance();
            if(instance != null)
            {
                result.add(instance);
            }
            
            return;
        }

        String pattern = elements[index];
        Map<String, Entry> children = entry.children;
        if(children != null)
        {
            for(Map.Entry<String, Entry> child: children.entrySet())
            {
                if(PathUtils.matches(pattern, child.getKey()))
                {
                    getAll(child.getValue(), elements, index + 1, result);
                }
            }
        }
    }

    public void put(String path, Object instance)
    {
        put(instance, root, PathUtils.getPathElements(path), 0);
    }

    private void put(Object instance, Entry entry, String[] elements, int index)
    {
        if(index == elements.length)
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
        private Object instance;
        /**
         * Created on demand to prevent wastage for the large numbers of
         * leaf instances.
         */
        private Map<String, Entry> children;

        public Entry(Object instance)
        {
            this.instance = instance;
        }

        public Object getInstance()
        {
            return instance;
        }

        public void setInstance(Object instance)
        {
            this.instance = instance;
        }

        public void addChild(String element, Entry entry)
        {
            if(children == null)
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
            if(child == null)
            {
                child = new Entry(null);
                addChild(element, child);
            }

            return child;
        }
    }
}
