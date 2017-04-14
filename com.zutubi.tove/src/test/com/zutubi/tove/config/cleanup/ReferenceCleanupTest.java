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

package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

public class ReferenceCleanupTest extends AbstractConfigurationSystemTestCase
{
    private static final String SCOPE_TEMPLATED = "templated";

    private static final String PROJECT_GLOBAL = "global";
    private static final String PROJECT_REFEREE1 = "referee1";
    private static final String PROJECT_REFEREE2 = "referee2";
    private static final String PROJECT_REFEREE3 = "referee3";
    private static final String PROJECT_REFEREE4 = "referee4";

    private static final String PROPERTY_PROJECT_REF = "projectRef";

    private String globalPath;
    private RefProject global;
    private RefProject referee1;
    private RefProject referee2;
    private RefProject referee3;
    private RefProject referee4;
    private Listener listener;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        CompositeType projectType = typeRegistry.register(RefProject.class);
        MapType templatedMap = new TemplatedMapType(projectType, typeRegistry);
        configurationPersistenceManager.register(SCOPE_TEMPLATED, templatedMap);

        RefProject global = new RefProject(PROJECT_GLOBAL);
        global.putMeta(TemplateRecord.TEMPLATE_KEY, "true");
        globalPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, global);
        this.global = configurationTemplateManager.getInstance(globalPath, RefProject.class);

        RefProject referee = createProject(PROJECT_REFEREE1);
        String refereePath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referee);
        referee1 = configurationTemplateManager.getInstance(refereePath, RefProject.class);

        referee = createProject(PROJECT_REFEREE2);
        refereePath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referee);
        referee2 = configurationTemplateManager.getInstance(refereePath, RefProject.class);

        referee = createProject(PROJECT_REFEREE3);
        refereePath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referee);
        referee3 = configurationTemplateManager.getInstance(refereePath, RefProject.class);

        referee = createProject(PROJECT_REFEREE4);
        refereePath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referee);
        referee4 = configurationTemplateManager.getInstance(refereePath, RefProject.class);

        listener = registerListener();
    }

    public void testNullOut()
    {
        RefProject referer = createProject("referer");
        referer.setProjectRef(referee1);
        String referrerPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);


        listener.clearEvents();
        configurationTemplateManager.delete(referee1.getConfigurationPath());


        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        assertNull(referer.getProjectRef());

        Record refererRecord = configurationTemplateManager.getRecord(referrerPath);
        assertEquals("0", refererRecord.get(PROPERTY_PROJECT_REF));
        
        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee1.getConfigurationPath(), false), new SaveEventSpec(referrerPath)));
    }

    public void testNullOutInherited()
    {
        RefProject parent = createProject("parent", globalPath, true);
        parent.setProjectRef(referee1);
        String parentPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, parent);
        
        RefProject referer = createProject("referer", parentPath, false);
        referer.setProjectRef(referee1);
        String referrerPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);

        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord(referrerPath);
        assertEquals("parent", record.getOwner(PROPERTY_PROJECT_REF));

        
        listener.clearEvents();
        configurationTemplateManager.delete(referee1.getConfigurationPath());


        global = configurationTemplateManager.getInstance(globalPath, RefProject.class);
        assertNull(global.getProjectRef());

        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        assertNull(referer.getProjectRef());
        
        record = (TemplateRecord) configurationTemplateManager.getRecord(referrerPath);
        assertEquals("0", record.get(PROPERTY_PROJECT_REF));
        // Make sure the global template still owns the reference path -- the
        // referrer itself should just inherit its null now..
        assertEquals(PROJECT_GLOBAL, record.getOwner(PROPERTY_PROJECT_REF));
        
        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee1.getConfigurationPath(), false), new SaveEventSpec(referrerPath)));
    }

    public void testRemoveFromList()
    {
        RefProject referer = createProject("referer");
        referer.getProjectRefList().add(referee1);
        String referrerPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);


        listener.clearEvents();
        configurationTemplateManager.delete(referee1.getConfigurationPath());


        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        assertEquals(0, referer.getProjectRefList().size());
        
        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee1.getConfigurationPath(), false), new SaveEventSpec(referrerPath)));
    }

    public void testRemoveFromListInherited()
    {
        RefProject parent = createProject("parent", globalPath, true);
        parent.getProjectRefList().add(referee1);
        String parentPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, parent);
        
        RefProject referer = createProject("referer", parentPath, false);
        referer.setProjectRefList(null);
        String referrerPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);

        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord(referrerPath);
        assertEquals("parent", record.getOwner("projectRefList"));


        listener.clearEvents();
        configurationTemplateManager.delete(referee1.getConfigurationPath());


        global = configurationTemplateManager.getInstance(globalPath, RefProject.class);
        assertEquals(0, global.getProjectRefList().size());

        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        assertEquals(0, referer.getProjectRefList().size());
        
        record = (TemplateRecord) configurationTemplateManager.getRecord(referrerPath);
        assertEquals(PROJECT_GLOBAL, record.getOwner("projectRefList"));

        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee1.getConfigurationPath(), false), new SaveEventSpec(referrerPath)));
    }

    public void testRemoveFromListMultipleItems()
    {
        RefProject referer = createProject("referer");
        List<RefProject> list = referer.getProjectRefList();
        list.add(referee1);
        list.add(referee2);
        list.add(referee3);
        String referrerPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);


        listener.clearEvents();
        configurationTemplateManager.delete(referee2.getConfigurationPath());


        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        list = referer.getProjectRefList();
        assertEquals(2, list.size());
        assertEquals(PROJECT_REFEREE1, list.get(0).getName());
        assertEquals(PROJECT_REFEREE3, list.get(1).getName());

        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee2.getConfigurationPath(), false), new SaveEventSpec(referrerPath)));
    }

    public void testRemoveFromListMultipleItemsSomeInherited()
    {
        RefProject parent = createProject("parent", globalPath, true);
        parent.getProjectRefList().add(referee1);
        parent.getProjectRefList().add(referee2);
        String parentPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, parent);

        RefProject referer = createProject("referer", parentPath, false);
        referer.setProjectRefList(null);
        String referrerPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);

        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        referer.getProjectRefList().add(referee3);
        referer.getProjectRefList().add(referee4);
        configurationTemplateManager.save(referer);


        listener.clearEvents();
        configurationTemplateManager.delete(referee3.getConfigurationPath());


        parent = configurationTemplateManager.getInstance(parentPath, RefProject.class);
        assertEquals(2, parent.getProjectRefList().size());

        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        assertEquals(3, referer.getProjectRefList().size());
        
        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee3.getConfigurationPath(), false), new SaveEventSpec(referrerPath)));
    }
    
    public void testRemoveFromListInheritedMultipleItems()
    {
        RefProject parent = createProject("parent", globalPath, true);
        parent.getProjectRefList().add(referee1);
        parent.getProjectRefList().add(referee2);
        String parentPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, parent);

        RefProject referer = createProject("referer", parentPath, false);
        referer.setProjectRefList(null);
        String referrerPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);

        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        referer.getProjectRefList().add(referee3);
        referer.getProjectRefList().add(referee4);
        configurationTemplateManager.save(referer);


        listener.clearEvents();
        configurationTemplateManager.delete(referee2.getConfigurationPath());


        parent = configurationTemplateManager.getInstance(parentPath, RefProject.class);
        assertEquals(1, parent.getProjectRefList().size());

        referer = configurationTemplateManager.getInstance(referrerPath, RefProject.class);
        assertEquals(3, referer.getProjectRefList().size());
        
        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee2.getConfigurationPath(), false), new SaveEventSpec(referrerPath)));        
    }
    
    public void testRemoveParentWhenRequired()
    {
        RefProject referer = createProject("referer");
        Holder holder = new Holder();
        holder.setRequiredProjectRef(referee1);
        referer.setHolder(holder);
        String refererPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);


        listener.clearEvents();
        configurationTemplateManager.delete(referee1.getConfigurationPath());


        referer = configurationTemplateManager.getInstance(refererPath, RefProject.class);
        assertNull(referer.getHolder());
        
        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee1.getConfigurationPath(), false), new DeleteEventSpec(getPath(refererPath, "holder"), false)));
    }

    public void testRemoveParentWhenRequiredInherited()
    {
        Holder holder = new Holder();
        holder.setRequiredProjectRef(referee1);
        RefProject parent = createProject("parent", globalPath, true);
        parent.setHolder(holder);
        String parentPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, parent);

        RefProject referer = createProject("referer", parentPath, false);
        String refererPath = configurationTemplateManager.insertInstance(SCOPE_TEMPLATED, referer);


        listener.clearEvents();
        configurationTemplateManager.delete(referee1.getConfigurationPath());


        parent = configurationTemplateManager.getInstance(parentPath, RefProject.class);
        assertNull(parent.getHolder());

        referer = configurationTemplateManager.getInstance(refererPath, RefProject.class);
        assertNull(referer.getHolder());
        
        listener.assertEvents(addPostEvents(new DeleteEventSpec(referee1.getConfigurationPath(), false), new DeleteEventSpec(getPath(refererPath, "holder"), false)));
    }

    private RefProject createProject(String name)
    {
        return createProject(name, globalPath, false);
    }

    private RefProject createProject(String name, String parentPath, boolean template)
    {
        RefProject project = new RefProject(name);
        long parentHandle = configurationTemplateManager.getRecord(parentPath).getHandle();
        project.putMeta(TemplateRecord.PARENT_KEY, Long.toString(parentHandle));
        if (template)
        {
            project.putMeta(TemplateRecord.TEMPLATE_KEY, "true");
        }
        return project;
    }

    @SymbolicName("refProject")
    public static class RefProject extends AbstractNamedConfiguration
    {
        @Reference
        private RefProject projectRef;
        @Reference
        private List<RefProject> projectRefList = new LinkedList<RefProject>();
        private Holder holder;

        public RefProject()
        {
        }

        public RefProject(String name)
        {
            super(name);
        }

        public RefProject getProjectRef()
        {
            return projectRef;
        }

        public void setProjectRef(RefProject projectRef)
        {
            this.projectRef = projectRef;
        }

        public List<RefProject> getProjectRefList()
        {
            return projectRefList;
        }

        public void setProjectRefList(List<RefProject> projectRefList)
        {
            this.projectRefList = projectRefList;
        }

        public Holder getHolder()
        {
            return holder;
        }

        public void setHolder(Holder holder)
        {
            this.holder = holder;
        }
    }

    @SymbolicName("holder")
    public static class Holder extends AbstractConfiguration
    {
        @Required @Reference
        private RefProject requiredProjectRef;

        public RefProject getRequiredProjectRef()
        {
            return requiredProjectRef;
        }

        public void setRequiredProjectRef(RefProject requiredProjectRef)
        {
            this.requiredProjectRef = requiredProjectRef;
        }
    }
}
