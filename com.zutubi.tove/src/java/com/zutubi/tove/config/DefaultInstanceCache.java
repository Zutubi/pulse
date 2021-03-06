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

package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.inmemory.InMemoryMapStateWrapper;
import com.zutubi.tove.transaction.inmemory.InMemoryStateWrapper;
import com.zutubi.tove.transaction.inmemory.InMemoryTransactionResource;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.UnaryProcedure;

import java.util.*;

/**
 * A cache for configuration instances, supporting addressing by path,
 * including paths with wildcards.
 */
class DefaultInstanceCache implements InstanceCache
{
    private InMemoryTransactionResource<Map<String, Set<String>>> referenceIndexState;
    private InMemoryTransactionResource<Entry> entryState;

    private TransactionManager transactionManager;

    public void init()
    {
        referenceIndexState = new InMemoryTransactionResource<Map<String, Set<String>>>(new InMemoryMapStateWrapper<String, Set<String>>(new HashMap<String, Set<String>>()));
        referenceIndexState.setTransactionManager(transactionManager);

        entryState = new InMemoryTransactionResource<Entry>(new InMemoryEntryStateWrapper(new Entry(true)));
        entryState.setTransactionManager(transactionManager);
    }

    public void reset()
    {
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                entryState.get(true).reset();
                referenceIndexState.get(true).clear();
            }
        });
    }

    public boolean hasInstancesUnder(String path)
    {
        if (path.length() == 0)
        {
            return entryState.get(false).size() > 0;
        }
        else
        {
            return walkToEntry(path, true, null, false) != null;
        }
    }

    public void markInvalid(final String path)
    {
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                walkToEntry(path, true, new UnaryProcedure<Entry>()
                {
                    public void run(Entry entry)
                    {
                        entry.markInvalid();
                    }
                }, true);
            }
        });
    }

    public boolean isValid(String path, boolean allowIncomplete)
    {
        DefaultInstanceCache.Entry entry = walkToEntry(path, allowIncomplete, null, false);
        return entry != null && entry.isValid();
    }

    public Configuration get(String path, boolean allowIncomplete)
    {
        Entry entry = walkToEntry(path, allowIncomplete, null, false);
        return entry == null ? null : entry.getInstance();
    }

    private Entry walkToEntry(String path, boolean allowIncomplete, UnaryProcedure<Entry> f, boolean writable)
    {
        return walkToEntry(entryState.get(writable), PathUtils.getPathElements(path), 0, allowIncomplete, f);
    }

    private Entry walkToEntry(Entry entry, String[] elements, int index, boolean allowIncomplete, UnaryProcedure<Entry> f)
    {
        if (!allowIncomplete && !entry.complete)
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
        return entry == null ? null : walkToEntry(entry, elements, index + 1, allowIncomplete, f);
    }

    public Collection<Configuration> getAllDescendants(String path, boolean allowIncomplete)
    {
        Collection<Configuration> result = new LinkedList<Configuration>();
        Entry entry = walkToEntry(path, allowIncomplete, null, false);
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
        getAll(entryState.get(false), PathUtils.getPathElements(path), 0, clazz, result, allowIncomplete);
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

    public void put(final String path, final Configuration instance, final boolean complete)
    {
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                put(instance, complete, entryState.get(true), PathUtils.getPathElements(path), 0);
            }
        });
    }

    public void forAllInstances(final InstanceHandler handler, final boolean allowIncomplete, final boolean writable)
    {
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                entryState.get(writable).forAllInstances(null, allowIncomplete, "", handler);
            }
        });
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
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                entryState.get(true).prune(referenceIndexState.get(true));
            }
        });
    }

    public Set<String> getInstancePathsReferencing(String path)
    {
        Set<String> instancesReferencing = referenceIndexState.get(false).get(path);
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
        Set<String> instancesReferencing = referenceIndexState.get(false).get(path);
        if (instancesReferencing == null)
        {
            return Collections.emptySet();
        }
        else
        {
            Set<String> result = new HashSet<String>();
            for (String instancePath: instancesReferencing)
            {
                Entry entry = walkToEntry(instancePath, true, null, false);
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

    public void indexReference(final String fromPropertyPath, final String toPath)
    {
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                String propertyPath = PathUtils.getBaseName(fromPropertyPath);
                String instancePath = PathUtils.getParentPath(fromPropertyPath);
                Entry entry = walkToEntry(instancePath, true, null, true);
                if (entry == null)
                {
                    propertyPath = PathUtils.getPath(PathUtils.getBaseName(instancePath), propertyPath);
                    instancePath = PathUtils.getParentPath(instancePath);
                    entry = walkToEntry(instancePath, true, null, true);
                }

                addToReferenceIndex(instancePath, toPath);
                entry.addReference(propertyPath, toPath);
            }
        });
    }

    private void addToReferenceIndex(String fromPath, String toPath)
    {
        Map<String, Set<String>> referenceIndex = referenceIndexState.get(true);
        Set<String> index = referenceIndex.get(toPath);
        if (index == null)
        {
            index = new HashSet<String>();
            referenceIndex.put(toPath, index);
        }

        index.add(fromPath);
    }

    public boolean markDirty(final String path)
    {
        return transactionManager.runInTransaction(new NullaryFunction<Boolean>()
        {
            public Boolean process()
            {
                Entry entry = walkToEntry(path, true, null, true);
                if (entry != null && !entry.isDirty())
                {
                    entry.markDirty();
                    return true;
                }
                return false;
            }
        });
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
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
            if (instance != null && (complete || allowIncomplete))
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

        public void forAllInstances(Configuration parentInstance, boolean allowIncomplete, String baseName, InstanceHandler handler)
        {
            if (instance != null && (complete || allowIncomplete))
            {
                handler.handle(instance, baseName, complete, parentInstance);
            }

            if (children != null)
            {
                for(Map.Entry<String,Entry> childEntry: children.entrySet())
                {
                    childEntry.getValue().forAllInstances(instance, allowIncomplete, childEntry.getKey(), handler);
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

        public void reset()
        {
            this.instance = null;
            this.children = null;
            this.dirty = false;
            this.complete = true;
            this.valid = true;
            this.references = null;
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

    private class InMemoryEntryStateWrapper extends InMemoryStateWrapper<Entry>
    {
        private InMemoryEntryStateWrapper(Entry state)
        {
            super(state);
        }

        protected InMemoryStateWrapper<Entry> copy()
        {
            return new InMemoryEntryStateWrapper(get().copyStructure());
        }
    }
}
