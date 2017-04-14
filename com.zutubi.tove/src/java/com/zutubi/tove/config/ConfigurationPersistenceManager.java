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
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.*;

/**
 *
 *
 */
public class ConfigurationPersistenceManager
{
    private TypeRegistry typeRegistry;
    private RecordManager recordManager;

    private Map<String, ConfigurationScopeInfo> rootScopes = new HashMap<String, ConfigurationScopeInfo>();
    
    /**
     * An index mapping from composite types to all paths where records of that
     * type (or a subtype) may reside.  Paths will include wildcards to navigate
     * collection members.
     */
    private Map<CompositeType, List<String>> compositeTypePathIndex = new HashMap<CompositeType, List<String>>();

    public void register(String scope, ComplexType type)
    {
        register(scope, type, true);
    }

    /**
     * Register the root scope definitions, from which all of the other definitions will be
     * derived.
     *
     * @param scope      name of the scope
     * @param type       type of the object stored in this scope
     * @param persistent if true, records under this scope will be persisted
     */
    public void register(String scope, ComplexType type, boolean persistent)
    {
        validateConfiguration(type, scope);
        rootScopes.put(scope, new ConfigurationScopeInfo(scope, type, persistent));
        if (persistent)
        {
            if(recordManager == null)
            {
                throw new IllegalArgumentException("Attempt to register persistent scope '" + scope + "' before persistence system is initialised");
            }

            if(!recordManager.containsRecord(scope))
            {
                recordManager.insert(scope, type.createNewRecord(true));
            }
        }

        if (type instanceof CompositeType)
        {
            updateIndex(scope, (CompositeType) type);
        }
        else
        {
            if (type instanceof CollectionType)
            {
                updateIndex(scope, (CollectionType) type);
            }
        }
    }

    public Collection<ConfigurationScopeInfo> getScopes()
    {
        return rootScopes.values();
    }

    public ConfigurationScopeInfo getScopeInfo(String scope)
    {
        return rootScopes.get(scope);
    }

    private void validateConfiguration(ComplexType type, String scope)
    {
        validateConfiguration(type, new HashSet<ComplexType>(), scope);
    }

    private void validateConfiguration(ComplexType type, Set<ComplexType> seenTypes, String path)
    {
        if (seenTypes.contains(type))
        {
            throw new IllegalArgumentException("Cycle detected in type definition at path '" + path + "': type '" + type.getClazz().getName() + "' has already appeared in this path");
        }

        if(type instanceof CollectionType)
        {
            Type targetType = type.getTargetType();
            if (targetType instanceof ComplexType)
            {
                validateConfiguration((ComplexType) targetType, seenTypes, PathUtils.getPath(path, PathUtils.WILDCARD_ANY_ELEMENT));
            }
        }
        else
        {
            CompositeType compositeType = (CompositeType) type;
            if (!Configuration.class.isAssignableFrom(compositeType.getClazz()))
            {
                throw new IllegalArgumentException("Attempt to register persistent configuration of type '" + compositeType.getClazz() + "': which does not implement Configuration");
            }

            seenTypes.add(type);
            for (TypeProperty property : compositeType.getProperties(ComplexType.class))
            {
                validateConfiguration((ComplexType) property.getType(), new HashSet<ComplexType>(seenTypes), PathUtils.getPath(path, property.getName()));
            }
        }
    }

    private void updateIndex(String path, CompositeType type)
    {
        // Add an entry at the current path, and analyse properties
        addToIndex(type, path);
        for (TypeProperty property : type.getProperties(CompositeType.class))
        {
            String childPath = PathUtils.getPath(path, property.getName());
            updateIndex(childPath, (CompositeType) property.getType());
        }
        for (TypeProperty property : type.getProperties(CollectionType.class))
        {
            String childPath = PathUtils.getPath(path, property.getName());
            updateIndex(childPath, (CollectionType) property.getType());
        }
    }

    private void updateIndex(String path, CollectionType type)
    {
        // If the collection itself holds a complex type, add a wildcard
        // to the path and traverse down.
        Type targetType = type.getCollectionType();
        if (targetType instanceof CompositeType)
        {
            updateIndex(PathUtils.getPath(path, PathUtils.WILDCARD_ANY_ELEMENT), (CompositeType) targetType);
        }
    }

    private void addToIndex(CompositeType composite, String path)
    {
        ensureConfigurationPaths(composite).add(path);
    }

    private List<String> ensureConfigurationPaths(CompositeType type)
    {
        List<String> l = compositeTypePathIndex.get(type);
        if (l == null)
        {
            l = new ArrayList<String>();
            compositeTypePathIndex.put(type, l);
        }
        return l;
    }

    String getClosestMatchingScope(CompositeType type, String path)
    {
        List<String> patterns = compositeTypePathIndex.get(type);
        if (patterns != null)
        {
            // Find the closest by starting at our path and working up the
            // ancestry until one hits.
            path = PathUtils.normalisePath(path);
            while (path != null)
            {
                for (String candidatePattern : patterns)
                {
                    if (PathUtils.prefixMatchesPathPattern(candidatePattern, path))
                    {
                        return path;
                    }
                }
                path = PathUtils.getParentPath(path);
            }
        }
        return null;
    }

    String getClosestOwningScope(CompositeType type, String path)
    {
        String scope = getClosestMatchingScope(type, path);
        if (scope != null)
        {
            String parent = PathUtils.getParentPath(scope);
            ComplexType parentType = getType(parent);
            if(parentType instanceof CollectionType && parentType.getTargetType().equals(type))
            {
                scope = parent;
            }
        }
        return scope;
    }

    List<String> getOwningPaths(CompositeType type, String prefix)
    {
        List<String> paths = compositeTypePathIndex.get(type);
        List<String> result = new LinkedList<String>();
        if (paths != null)
        {
            if (prefix == null)
            {
                // Include all
                result.addAll(paths);
            }
            else
            {
                // Restrict to those under prefix
                for (String owningPath : paths)
                {
                    if (PathUtils.prefixMatchesPathPattern(owningPath, prefix))
                    {
                        result.add(PathUtils.getPath(prefix, PathUtils.stripMatchingPrefix(owningPath, prefix)));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Retrieve the type definition for the specified path.
     *
     * @param path the path to retrieve the type of
     * @return the type definition, or null if none exists.
     */
    ComplexType getType(String path)
    {
        String[] pathElements = PathUtils.getPathElements(path);
        String[] parentElements = PathUtils.getParentPathElements(pathElements);
        if (parentElements == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': no parent");
        }

        ConfigurationScopeInfo info = rootScopes.get(pathElements[0]);
        if (info == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': references non-existent root scope '" + pathElements[0] + "'");
        }

        if (parentElements.length == 0)
        {
            // Parent is the base, special case this as the base is currently
            // like a composite without a registered type :/.
            return info.getType();
        }
        else
        {
            String lastElement = pathElements[pathElements.length - 1];
            if(info.isPersistent())
            {
                return lookupPersistentType(path, parentElements, lastElement);
            }
            else
            {
                return lookupTransientType(path, pathElements, info);
            }
        }
    }

    private CompositeType lookupTransientType(String path, String[] pathElements, ConfigurationScopeInfo info)
    {
        CompositeType type = (CompositeType) info.getType();
        for(int i = 1; i < pathElements.length; i++)
        {
            TypeProperty property = type.getProperty(pathElements[i]);
            if(property == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': references non-existant property '" + pathElements[i] + "' of class '" + type.getClazz().getName() + "'");
            }

            Type propertyType = property.getType();
            if(!(propertyType instanceof CompositeType))
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': references non-composite property '" + pathElements[i] + "' of class '" + type.getClazz().getName() + "'");
            }

            type = (CompositeType) propertyType;
        }

        return type;
    }

    private ComplexType lookupPersistentType(String path, String[] parentElements, String lastElement)
    {
        Record parentRecord = recordManager.select(PathUtils.getPath(parentElements));
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': parent does not exist");
        }

        Object value = parentRecord.get(lastElement);
        if (parentRecord.isCollection())
        {
            // Last segment of path must refer to an existing child
            // composite record.
            if (value == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': references unknown child '" + lastElement + "' of collection");
            }
            // TODO: validate that collections must not contain collections
            return extractRecordType(value, path);
        }
        else
        {
            // Parent is a composite, see if the field exists.
            String parentSymbolicName = parentRecord.getSymbolicName();
            CompositeType parentType = typeRegistry.getType(parentSymbolicName);
            TypeProperty typeProperty = parentType.getProperty(lastElement);
            if (typeProperty == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + ": references non-existant field '" + lastElement + "' of type '" + parentSymbolicName + "'");
            }

            Type type = typeProperty.getType();
            if (value == null || type instanceof CollectionType)
            {
                return (ComplexType) type;
            }
            else
            {
                // Return the type of the actual value.
                return extractRecordType(value, path);
            }
        }
    }

    private CompositeType extractRecordType(Object value, String path)
    {
        if (!(value instanceof Record))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': does not reference a complex type");
        }

        Record record = (Record) value;
        return typeRegistry.getType(record.getSymbolicName());
    }

    public List<String> getConfigurationPaths(CompositeType type)
    {
        return compositeTypePathIndex.get(type);
    }

    public boolean isPersistent(String path)
    {
        String[] parts = PathUtils.getPathElements(path);
        if(parts.length > 0)
        {
            ConfigurationScopeInfo rootScope = rootScopes.get(parts[0]);
            if(rootScope == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': references non-existant root scope '" + parts[0] + "'");
            }

            return rootScope.isPersistent();
        }
        else
        {
            throw new IllegalArgumentException("Invalid path: path is empty");
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
