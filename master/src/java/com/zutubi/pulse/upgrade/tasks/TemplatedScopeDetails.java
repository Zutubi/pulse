package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Holds information about a templated configuration scope which may be used
 * within upgrade tasks.
 */
public class TemplatedScopeDetails extends ScopeDetails
{
    static final String KEY_PARENT_HANDLE = "parentHandle";

    private ScopeHierarchy hierarchy;
    private RecordManager recordManager;

    public TemplatedScopeDetails(String name, RecordManager recordManager)
    {
        super(name);
        this.recordManager = recordManager;
    }

    public boolean hasAncestor(String path)
    {
        PathInfo pathInfo = new PathInfo(path);
        ScopeHierarchy.Node node = getHierarchy().findNodeById(pathInfo.getOwner());
        if (node != null)
        {
            if (node.getParent() == null)
            {
                return false;
            }

            if (pathInfo.getRemainder() == null)
            {
                return true;
            }

            String parentPath = PathUtils.getPath(pathInfo.getScope(), node.getParent().getId(), pathInfo.getRemainder());
            return recordManager.containsRecord(parentPath) || hasAncestor(parentPath);
        }

        return false;
    }

    ScopeHierarchy getHierarchy()
    {
        // The hierarchy is lazily-initialised as it is not always required
        // and takes some time to compute.
        if (hierarchy == null)
        {
            initHierarchy();
        }
        return hierarchy;
    }

    private void initHierarchy()
    {
        Map<String, Record> allMembers = recordManager.selectAll(PathUtils.getPath(getName(), PathUtils.WILDCARD_ANY_ELEMENT));
        Map<Long, List<String>> idByParentHandle = new HashMap<Long, List<String>>();
        Map<String, Record> recordById = new HashMap<String, Record>();
        for(Map.Entry<String, Record> entry: allMembers.entrySet())
        {
            final String id = PathUtils.getPath(1, PathUtils.getPathElements(entry.getKey()));

            recordById.put(id, entry.getValue());

            String parentHandleString = entry.getValue().getMeta(KEY_PARENT_HANDLE);
            long parentHandle = 0;
            if(parentHandleString != null)
            {
                parentHandle = Long.parseLong(parentHandleString);
            }

            List<String> children = idByParentHandle.get(parentHandle);
            if (children == null)
            {
                children = new LinkedList<String>();
                idByParentHandle.put(parentHandle, children);
            }

            children.add(id);
        }

        hierarchy = createHierarchy(getName(), recordById, idByParentHandle);
    }

    private ScopeHierarchy createHierarchy(String scope, Map<String, Record> recordById, Map<Long, List<String>> idByParentHandle)
        {
        List<String> rootIds = idByParentHandle.get(0L);
        if (rootIds == null)
        {
            throw new PulseRuntimeException("Scope '" + scope + "' has no root element");
        }
        else if(rootIds.size() != 1)
        {
            throw new PulseRuntimeException("Scope '" + scope + "' has multiple root elements");
        }

        ScopeHierarchy.Node root = createNode(rootIds.get(0), recordById, idByParentHandle);
        return new ScopeHierarchy(root);
    }

    private ScopeHierarchy.Node createNode(String id, Map<String, Record> recordById, Map<Long, List<String>> idByParentHandle)
    {
        Record record = recordById.get(id);
        ScopeHierarchy.Node result = new ScopeHierarchy.Node(id);

        long handle = record.getHandle();
        List<String> children = idByParentHandle.get(handle);
        if (children != null)
        {
            for(String childId: children)
            {
                result.addChild(createNode(childId, recordById, idByParentHandle));
            }
        }

        return result;
    }

    private static class PathInfo
    {
        private String scope;
        private String owner;
        private String remainder;

        public PathInfo(String path)
        {
            String[] elements = PathUtils.getPathElements(path);
            scope = elements[0];
            if (elements.length > 1)
            {
                owner = elements[1];

                if (elements.length > 2)
                {
                    remainder = PathUtils.getPath(2, elements);
                }
            }
        }

        public String getScope()
        {
            return scope;
        }

        public String getOwner()
        {
            return owner;
        }

        public String getRemainder()
        {
            return remainder;
        }
    }
}
