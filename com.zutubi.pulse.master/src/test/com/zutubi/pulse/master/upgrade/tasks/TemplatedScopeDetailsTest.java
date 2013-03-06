package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.events.DefaultEventManager;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.test.EqualityAssertions;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.record.*;
import com.zutubi.tove.type.record.store.InMemoryRecordStore;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

public class TemplatedScopeDetailsTest extends PulseTestCase
{
    private static final String SCOPE_EMPTY    = "empty";
    private static final String SCOPE_PROJECTS = "projects";

    private static final String PROJECT_ROOT            = "root";
    private static final String PROJECT_SIMPLE_CHILD    = "simple child";
    private static final String PROJECT_TEMPLATE        = "template";
    private static final String PROJECT_TEMPLATE_CHILD1 = "template child1";
    private static final String PROJECT_TEMPLATE_CHILD2 = "template child2";

    private static final String TYPE_PROJECT = "zutubi.project";
    private static final String TYPE_SCM     = "zutubi.scm";

    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_SCM  = "scm";
    private static final String PROPERTY_URL  = "url";

    private RecordManager recordManager;
    private TemplatedScopeDetails emptyDetails;
    private TemplatedScopeDetails projectsDetails;

    protected void setUp() throws Exception
    {
        super.setUp();

        TransactionManager transactionManager = new TransactionManager();

        InMemoryRecordStore recordStore = new InMemoryRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.init();

        recordManager = new RecordManager();
        recordManager.setRecordStore(recordStore);
        recordManager.setTransactionManager(transactionManager);
        recordManager.setEventManager(new DefaultEventManager());
        recordManager.init();

        // Create an empty scope: just one record
        recordManager.insert(SCOPE_EMPTY, new MutableRecordImpl());

        // Now a scope with a template hierarchy.  Top level is a collection,
        // next level is "projects" which have a name and a nested "scm".
        MutableRecord projectCollection = new MutableRecordImpl();
        recordManager.insert(SCOPE_PROJECTS, projectCollection);

        addProject(null, PROJECT_ROOT);
        addProject(PROJECT_ROOT, PROJECT_SIMPLE_CHILD);
        addProject(PROJECT_ROOT, PROJECT_TEMPLATE);
        addProject(PROJECT_TEMPLATE, PROJECT_TEMPLATE_CHILD1);
        addProject(PROJECT_TEMPLATE, PROJECT_TEMPLATE_CHILD2);

        emptyDetails = new TemplatedScopeDetails(SCOPE_EMPTY, recordManager);
        projectsDetails = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
    }

    public void testEmptyScope()
    {
        try
        {
            emptyDetails.getHierarchy();
            fail("Should not be able to get hierarchy for empty scope: all scopes need a root");
        }
        catch(PulseRuntimeException e)
        {
            assertTrue(e.getMessage().contains("no root element"));
        }
    }

    public void testMultipleRootsScope()
    {
        addProject(null, "uh-oh, a second root");
        try
        {
            projectsDetails.getHierarchy();
            fail("Should not be able to get hierarchy for a scope with multiple roots");
        }
        catch(PulseRuntimeException e)
        {
            assertTrue(e.getMessage().contains("multiple root elements"));
        }
    }

    public void testProjectsHierarchy()
    {
        ScopeHierarchy hierarchy = projectsDetails.getHierarchy();
        assertNode(hierarchy.getRoot(), PROJECT_ROOT, null, PROJECT_SIMPLE_CHILD, PROJECT_TEMPLATE);
        assertNode(hierarchy.findNodeById(PROJECT_SIMPLE_CHILD), PROJECT_SIMPLE_CHILD, PROJECT_ROOT);
        assertNode(hierarchy.findNodeById(PROJECT_TEMPLATE), PROJECT_TEMPLATE, PROJECT_ROOT, PROJECT_TEMPLATE_CHILD1, PROJECT_TEMPLATE_CHILD2);
        assertNode(hierarchy.findNodeById(PROJECT_TEMPLATE_CHILD1), PROJECT_TEMPLATE_CHILD1, PROJECT_TEMPLATE);
        assertNode(hierarchy.findNodeById(PROJECT_TEMPLATE_CHILD2), PROJECT_TEMPLATE_CHILD2, PROJECT_TEMPLATE);
    }

    public void testGetAncestorPathInvalidPath()
    {
        assertNull(projectsDetails.getAncestorPath("garbage"));
    }

    public void testGetAncestorPathRootProject()
    {
        assertNull(projectsDetails.getAncestorPath(getProjectPath(PROJECT_ROOT)));
    }

    public void testGetAncestorPathSimpleChildProject()
    {
        assertEquals(getProjectPath(PROJECT_ROOT), projectsDetails.getAncestorPath(getProjectPath(PROJECT_SIMPLE_CHILD)));
    }

    public void testGetAncestorPathTemplateProject()
    {
        assertEquals(getProjectPath(PROJECT_ROOT), projectsDetails.getAncestorPath(getProjectPath(PROJECT_TEMPLATE)));
    }

    public void testGetAncestorPathTemplateChildProject()
    {
        assertEquals(getProjectPath(PROJECT_TEMPLATE), projectsDetails.getAncestorPath(getProjectPath(PROJECT_TEMPLATE_CHILD1)));
    }

    public void testGetAncestorPathRootScm()
    {
        String rootScmPath = addScm(PROJECT_ROOT);
        assertNull(projectsDetails.getAncestorPath(rootScmPath));
    }

    public void testGetAncestorPathSimpleChildScm()
    {
        String childScmPath = addScm(PROJECT_SIMPLE_CHILD);
        assertNull(projectsDetails.getAncestorPath(childScmPath));

        // Now add a parent, and make sure the result changes.
        String rootScmPath = addScm(PROJECT_ROOT);
        assertEquals(rootScmPath, projectsDetails.getAncestorPath(childScmPath));
    }

    public void testGetAncestorPathTemplateChildScm()
    {
        String childScmPath = addScm(PROJECT_TEMPLATE_CHILD1);
        assertNull(projectsDetails.getAncestorPath(childScmPath));

        String templateScmPath = addScm(PROJECT_TEMPLATE);
        assertEquals(templateScmPath, projectsDetails.getAncestorPath(childScmPath));
    }

    public void testGetAncestorPathIndirectTemplateChildScm()
    {
        String childScmPath = addScm(PROJECT_TEMPLATE_CHILD1);
        assertNull(projectsDetails.getAncestorPath(childScmPath));

        String rootScmPath = addScm(PROJECT_ROOT);
        assertEquals(rootScmPath, projectsDetails.getAncestorPath(childScmPath));
    }

    public void testHasAncestorInvalidPath()
    {
        assertFalse(projectsDetails.hasAncestor("garbage"));
    }

    public void testHasAncestorRootProject()
    {
        assertFalse(projectsDetails.hasAncestor(getProjectPath(PROJECT_ROOT)));
    }

    public void testHasAncestorSimpleChildProject()
    {
        assertTrue(projectsDetails.hasAncestor(getProjectPath(PROJECT_SIMPLE_CHILD)));
    }

    public void testHasAncestorTemplateProject()
    {
        assertTrue(projectsDetails.hasAncestor(getProjectPath(PROJECT_TEMPLATE)));
    }

    public void testHasAncestorTemplateChildProject()
    {
        assertTrue(projectsDetails.hasAncestor(getProjectPath(PROJECT_TEMPLATE_CHILD1)));
    }

    public void testHasAncestorRootScm()
    {
        String rootScmPath = addScm(PROJECT_ROOT);
        assertFalse(projectsDetails.hasAncestor(rootScmPath));
    }

    public void testHasAncestorSimpleChildScm()
    {
        String childScmPath = addScm(PROJECT_SIMPLE_CHILD);
        assertFalse(projectsDetails.hasAncestor(childScmPath));

        // Now add a parent, and make sure the result changes.
        addScm(PROJECT_ROOT);
        assertTrue(projectsDetails.hasAncestor(childScmPath));
    }

    public void testHasAncestorTemplateChildScm()
    {
        String childScmPath = addScm(PROJECT_TEMPLATE_CHILD1);
        assertFalse(projectsDetails.hasAncestor(childScmPath));

        addScm(PROJECT_TEMPLATE);
        assertTrue(projectsDetails.hasAncestor(childScmPath));
    }

    public void testHasAncestorIndirectTemplateChildScm()
    {
        String childScmPath = addScm(PROJECT_TEMPLATE_CHILD1);
        assertFalse(projectsDetails.hasAncestor(childScmPath));

        addScm(PROJECT_ROOT);
        assertTrue(projectsDetails.hasAncestor(childScmPath));
    }

    private String getProjectPath(String... elements)
    {
        return PathUtils.getPath(SCOPE_PROJECTS, PathUtils.getPath(elements));
    }

    private void addProject(String parentName, String name)
    {
        MutableRecord projectRecord = new MutableRecordImpl();
        projectRecord.setSymbolicName(TYPE_PROJECT);
        projectRecord.put(PROPERTY_NAME, name);

        if (parentName != null)
        {
            Record parent = recordManager.select(getProjectPath(parentName));
            long parentHandle = parent.getHandle();
            projectRecord.putMeta(TemplatedScopeDetails.KEY_PARENT_HANDLE, Long.toString(parentHandle));
        }

        recordManager.insert(getProjectPath(name), projectRecord);
    }

    private String addScm(String project)
    {
        MutableRecord scmRecord = new MutableRecordImpl();
        scmRecord.setSymbolicName(TYPE_SCM);
        scmRecord.put(PROPERTY_URL, "some url");

        String scmPath = getProjectPath(project, PROPERTY_SCM);
        recordManager.insert(scmPath, scmRecord);
        return scmPath;
    }

    private void assertNode(ScopeHierarchy.Node node, String expectedId, String expectedParentId, String... expectedChildIds)
    {
        assertEquals(expectedId, node.getId());
        if (expectedParentId == null)
        {
            assertNull(node.getParent());
        }
        else
        {
            assertEquals(expectedParentId, node.getParent().getId());
        }

        List<String> gotChildIds = newArrayList(transform(node.getChildren(), new Function<ScopeHierarchy.Node, String>()
        {
            public String apply(ScopeHierarchy.Node node)
            {
                return node.getId();
            }
        }));

        Collections.sort(gotChildIds, new Sort.StringComparator());
        EqualityAssertions.assertListEquals(gotChildIds, expectedChildIds);
    }

}
