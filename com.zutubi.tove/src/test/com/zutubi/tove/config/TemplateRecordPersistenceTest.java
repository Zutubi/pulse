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

import com.google.common.base.Function;
import com.zutubi.tove.annotations.NoInherit;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;

import java.util.*;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.zutubi.tove.type.record.PathUtils.getPath;

/**
 * Tests that the records returned by the CTM are template records with
 * expected inheritance properties.
 */
public class TemplateRecordPersistenceTest extends AbstractConfigurationSystemTestCase
{
    private static final String SCOPE_PROJECT = "project";
    private static final String SCOPE_NON_TEMPLATED_PROJECT = "nproject";

    private static final String GLOBAL_PROJECT = "global";
    private static final String GLOBAL_DESCRIPTION = "this is the daddy of them all";
    private static final String CHILD_PROJECT = "child";
    private static final String CHILD_DESCRIPTION = "my own way baby!";
    private static final String GRANDCHILD_PROJECT = "grandchild";
    private static final String GRANDCHILD_DESCRIPTION = "nkotb";

    private static final String PATH_DEFAULT_STAGE = "stages/default";
    private static final String PATH_CHILD_DEFAULT_STAGE = getPath(SCOPE_PROJECT, CHILD_PROJECT, PATH_DEFAULT_STAGE);
    private static final String PATH_GRANDCHILD_DEFAULT_STAGE = getPath(SCOPE_PROJECT, GRANDCHILD_PROJECT, PATH_DEFAULT_STAGE);

    private static final String PROPERTY_VALUE = "wow!";

    private CompositeType projectType;
    private CompositeType propertyType;
    private CompositeType stageType;
    private CompositeType specialStageType;

    protected void setUp() throws Exception
    {
        super.setUp();

        projectType = typeRegistry.register(Project.class);
        propertyType = typeRegistry.getType(Property.class);
        stageType = typeRegistry.getType(Stage.class);
        specialStageType = typeRegistry.register(SpecialStage.class);
        TemplatedMapType templatedMapType = new TemplatedMapType(projectType, typeRegistry);

        MapType mapType = new MapType(projectType, typeRegistry);

        configurationPersistenceManager.register(SCOPE_PROJECT, templatedMapType);
        configurationPersistenceManager.register(SCOPE_NON_TEMPLATED_PROJECT, mapType);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSingleRecord()
    {
        insertGlobal();

        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord("project/global");
        assertEquals(GLOBAL_PROJECT, record.getOwner());
        assertNull(record.getParent());

        assertEquals(GLOBAL_PROJECT, record.get("name"));
        assertEquals(GLOBAL_PROJECT, record.getOwner("name"));

        assertEquals(GLOBAL_DESCRIPTION, record.get("description"));
        assertEquals(GLOBAL_PROJECT, record.getOwner("description"));

        assertNull(record.get("url"));
        assertEquals(GLOBAL_PROJECT, record.getOwner("url"));

        assertNull(record.get("property"));
        assertEquals(GLOBAL_PROJECT, record.getOwner("property"));

        assertEmptyCollection(record, "properties");

        assertDefaultStages((TemplateRecord) record.get("stages"), GLOBAL_PROJECT);

        assertEmptyCollection(record, "propertiesList");

        String [] cool = (String[]) record.get("coolnesses");
        assertEquals(GLOBAL_PROJECT, record.getOwner("coolnesses"));
        assertEquals(0, cool.length);
    }

    public void testComposite()
    {
        insertGlobal();
        configurationTemplateManager.insertRecord("project/global/property", createProperty("p", "v"));

        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord("project/global");
        // Test both retrieving the property and asking for it directly by path
        assertComposite((TemplateRecord) record.get("property"));
        assertComposite((TemplateRecord) configurationTemplateManager.getRecord("project/global/property"));
    }

    private void assertComposite(TemplateRecord composite)
    {
        assertEquals(GLOBAL_PROJECT, composite.getOwner());
        assertNull(composite.getParent());

        assertEquals("v", composite.get("value"));
        assertEquals(GLOBAL_PROJECT, composite.getOwner("value"));
    }

    public void testNestedCollection()
    {
        insertGlobal();
        configurationTemplateManager.insertRecord("project/global/stages", createStage("stagename"));
        configurationTemplateManager.insertRecord("project/global/stages/stagename/properties", createProperty("p1", "v1"));

        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord("project/global");
        TemplateRecord stages = (TemplateRecord) record.get("stages");
        assertEquals(GLOBAL_PROJECT, stages.getOwner());
        assertEquals(2, stages.size());
        assertNull(stages.getParent());

        TemplateRecord stage = (TemplateRecord) stages.get("stagename");
        assertEquals(GLOBAL_PROJECT, stage.getOwner());
        assertNull(stage.getParent());

        TemplateRecord properties = (TemplateRecord) stage.get("properties");
        assertEquals(1, properties.size());
        assertEquals(GLOBAL_PROJECT, properties.getOwner());
        assertNull(properties.getParent());

        assertPropertyOfNestedCollection((TemplateRecord) properties.get("p1"));
        assertPropertyOfNestedCollection((TemplateRecord) configurationTemplateManager.getRecord("project/global/stages/stagename/properties/p1"));
    }

    private void assertPropertyOfNestedCollection(TemplateRecord property)
    {
        assertEquals(GLOBAL_PROJECT, property.getOwner());
        assertNull(property.getParent());
        assertEquals("v1", property.get("value"));
        assertEquals(GLOBAL_PROJECT, property.getOwner("value"));
    }

    public void testSimpleInheritance()
    {
        MutableRecord global = createGlobal();
        global.put("url", "inherited url");
        configurationTemplateManager.insertRecord("project", global);
        insertChild();

        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        TemplateRecord parent = childTemplate.getParent();
        assertEquals(GLOBAL_PROJECT, parent.getOwner());
        assertEquals(GLOBAL_PROJECT, parent.get("name"));
        assertEquals(GLOBAL_PROJECT, parent.getOwner("name"));
        assertNull(parent.getParent());

        assertEquals(CHILD_PROJECT, childTemplate.getOwner());
        assertEquals(CHILD_PROJECT, childTemplate.get("name"));
        assertEquals("inherited url", childTemplate.get("url"));
        assertEquals(GLOBAL_PROJECT, childTemplate.getOwner("url"));
    }

    public void testScrubOnInsert()
    {
        MutableRecord record = createGlobal();
        record.put("url", "some url");
        configurationTemplateManager.insertRecord("project", record);

        record = createChild();
        record.put("url", "some url");
        configurationTemplateManager.insertRecord("project", record);

        TemplateRecord template = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals("some url", template.get("url"));
        assertEquals(GLOBAL_PROJECT, template.getOwner("url"));
        assertNull(template.getMoi().get("url"));
    }

    public void testScrubArrayOnInsert()
    {
        MutableRecord record = createGlobal();
        record.put("coolnesses", new String[]{Coolness.PULSE.toString()});
        configurationTemplateManager.insertRecord("project", record);

        record = createChild();
        record.put("coolnesses", new String[]{Coolness.PULSE.toString()});
        configurationTemplateManager.insertRecord("project", record);

        TemplateRecord template = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertTrue(Arrays.equals(new String[]{Coolness.PULSE.toString()}, (String[]) template.get("coolnesses")));
        assertEquals(GLOBAL_PROJECT, template.getOwner("coolnesses"));
        assertNull(template.getMoi().get("coolnesses"));
    }

    public void testScrubOnInsertDeep()
    {
        MutableRecord record = createGlobal();
        configurationTemplateManager.insertRecord("project", record);
        configurationTemplateManager.insertRecord("project/global/properties", createProperty("foo", "bar"));

        record = createChild();
        ((MutableRecord) record.get("properties")).put("foo", createProperty("foo", "bar"));
        configurationTemplateManager.insertRecord("project", record);

        TemplateRecord property = (TemplateRecord) configurationTemplateManager.getRecord("project/child/properties/foo");
        assertEquals("foo", property.get("name"));
        assertEquals(GLOBAL_PROJECT, property.getOwner("name"));
        assertNull(property.getMoi().get("name"));
        assertEquals("bar", property.get("value"));
        assertEquals(GLOBAL_PROJECT, property.getOwner("value"));
        assertNull(property.getMoi().get("value"));
    }

    public void testInsertChildOfInvalid()
    {
        insertGlobal();
        insertChild();

        MutableRecord grandchild = createProject("grand", "test");
        configurationTemplateManager.setParentTemplate(grandchild, 9999999);
        failedInsertHelper("project", grandchild, "Invalid parent handle '9999999'");
    }

    public void testInsertChildOfConcrete()
    {
        insertGlobal();
        insertChild();

        MutableRecord grandchild = createProject("grand", "test");
        configurationTemplateManager.setParentTemplate(grandchild, configurationTemplateManager.getInstance("project/child").getHandle());
        failedInsertHelper("project", grandchild, "Cannot inherit from concrete path 'project/child'");
    }

    public void testInsertPathAlreadyInDescendant()
    {
        insertGlobal();
        MutableRecord child = createChild();
        MutableRecord childStages = (MutableRecord) child.get("stages");
        String stageName = "test";
        childStages.put(stageName, createStage(stageName));
        configurationTemplateManager.insertRecord("project", child);

        failedInsertHelper("project/global/stages", createStage(stageName), "Unable to insert record with name 'test' into path 'project/global/stages': a record with this name already exists in descendants [child]");
    }

    public void testInsertPathAlreadyHidden()
    {
        insertGlobal();
        insertChild();
        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);

        failedInsertHelper("project/child/stages", createStage("default"), "Unable to insert record with name 'default' into path 'project/child/stages': a record with this name already exists in ancestor 'global'");
    }

    public void testInsertPathHiddenInAncestor()
    {
        insertToGrandchild();
        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);

        failedInsertHelper("project/grandchild/stages", createStage("default"), "Unable to insert record with name 'default' into path 'project/grandchild/stages': a record with this name already exists in ancestor 'global'");
    }

    public void testInsertNestedPathHiddenInAncestor()
    {
        insertGlobal();
        insertTemplateChild();
        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);

        MutableRecord grandchild = createGrandchild();
        MutableRecord stages = (MutableRecord) grandchild.get("stages");
        stages.put("default", createStage("default"));
        
        failedInsertHelper("project", grandchild, "Cannot insert record: nested item 'stages/default' conflicts with hidden ancestor path 'project/global/stages/default'");
    }

    public void testInsertNestedPathDifferentTypeInParent()
    {
        insertGlobal();
        insertTemplateChild();

        MutableRecord grandchild = createGrandchild();
        MutableRecord stages = (MutableRecord) grandchild.get("stages");
        MutableRecord stage = specialStageType.createNewRecord(true);
        stage.put("name", "default");
        stages.put("default", stage);
        
        failedInsertHelper("project", grandchild, "Cannot inserted record: nested item 'stages/default' of type 'specialStage' conflicts with type in parent 'stage'");
    }

    private void failedInsertHelper(String path, MutableRecord record, String message)
    {
        try
        {
            configurationTemplateManager.insertRecord(path, record);
            fail("Expected insert at path '" + path + "' to fail");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(message, e.getMessage());
        }
    }

    public void testSimpleOverride()
    {
        insertGlobal();
        MutableRecord child = createProject(CHILD_PROJECT, CHILD_DESCRIPTION);
        child.put("url", "override url");
        configurationTemplateManager.setParentTemplate(child, configurationTemplateManager.getRecord("project/global").getHandle());
        configurationTemplateManager.insertRecord("project", child);

        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals(CHILD_PROJECT, childTemplate.getOwner());
        assertEquals(CHILD_PROJECT, childTemplate.get("name"));
        assertEquals("override url", childTemplate.get("url"));
        assertEquals(CHILD_PROJECT, childTemplate.getOwner("url"));
    }

    public void testInheritedMap()
    {
        insertGlobal();
        insertChild();

        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals(CHILD_PROJECT, childTemplate.getOwner());
        assertEquals(GLOBAL_PROJECT, childTemplate.getOwner("stages"));

        assertDefaultStages((TemplateRecord) childTemplate.get("stages"), CHILD_PROJECT);
        assertDefaultStages((TemplateRecord) configurationTemplateManager.getRecord("project/child/stages"), CHILD_PROJECT);
    }

    public void testInheritedList()
    {
        insertGlobal();
        insertChild();

        configurationTemplateManager.insertRecord("project/global/propertiesList", createProperty("gp1", "gv1"));

        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals(CHILD_PROJECT, childTemplate.getOwner());
        assertEquals(GLOBAL_PROJECT, childTemplate.getOwner("propertiesList"));

        assertInheritedList((TemplateRecord) childTemplate.get("propertiesList"));
        assertInheritedList((TemplateRecord) configurationTemplateManager.getRecord("project/child/propertiesList"));
    }

    private void assertInheritedList(TemplateRecord propertiesList)
    {
        assertEquals(1, propertiesList.size());
        String key = propertiesList.keySet().iterator().next();
        assertEquals(CHILD_PROJECT, propertiesList.getOwner());
        assertEquals(GLOBAL_PROJECT, propertiesList.getOwner(key));

        TemplateRecord property = (TemplateRecord) propertiesList.get(key);
        assertEquals(CHILD_PROJECT, property.getOwner());
        assertEquals(GLOBAL_PROJECT, property.getOwner("name"));
        assertEquals("gp1", property.get("name"));
    }

    public void testSaveInherited()
    {
        // Test saving a record to a path where there is no existing record,
        // but there *is* an inherited record (i.e. it is valid to save to
        // this path only when templating is taken into account).
        insertGlobal();
        configurationTemplateManager.insertRecord("project/global/property", createProperty("p", "v"));
        insertChild();
        assertInheritedProperty();
    }

    public void testSaveInheritedAddChildFirst()
    {
        // Like testSaveInherited, but insert the child record before
        // creating the inherited property.
        insertGlobal();
        insertChild();
        configurationTemplateManager.insertRecord("project/global/property", createProperty("p", "v"));
        assertInheritedProperty();
    }

    private void assertInheritedProperty()
    {
        TemplateRecord childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertNotNull(childProperty);
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("name"));
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("value"));

        MutableRecord overridingProperty = childProperty.flatten(false);
        overridingProperty.put("value", "cv");

        configurationTemplateManager.saveRecord("project/child/property", overridingProperty);
        childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("name"));
        assertEquals(CHILD_PROJECT, childProperty.getOwner("value"));
    }

    public void testSaveInheritedNested()
    {
        // Like testSaveInherited except that the path saved to is nested
        // within a collection
        insertGlobal();
        configurationTemplateManager.insertRecord("project/global/properties", createProperty("p", "v"));
        insertChild();
        TemplateRecord childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/properties/p");
        assertNotNull(childProperty);
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("name"));
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("value"));

        MutableRecord overridingProperty = childProperty.flatten(false);
        overridingProperty.put("value", "cv");

        configurationTemplateManager.saveRecord("project/child/properties/p", overridingProperty);
        childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/properties/p");
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("name"));
        assertEquals(CHILD_PROJECT, childProperty.getOwner("value"));
    }

    public void testSaveRevertingToParentValue()
    {
        // Save once overriding a field, and then save again restoring it to
        // the parent value, ensuring that the field ownership reverts to the
        // parent.
        insertGlobal();
        configurationTemplateManager.insertRecord("project/global/property", createProperty("p", "v"));
        insertChild();
        assertNotNull(configurationTemplateManager.getRecord("project/child/property"));

        MutableRecord overridingProperty = propertyType.createNewRecord(false);
        overridingProperty.put("name", "cp");
        configurationTemplateManager.saveRecord("project/child/property", overridingProperty);
        overridingProperty.put("name", "p");
        configurationTemplateManager.saveRecord("project/child/property", overridingProperty);

        TemplateRecord childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertNotNull(childProperty);
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("name"));
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("value"));
    }

    public void testSaveRevertingToChildValue()
    {
        // Save once overriding a field, and then save the same value to that
        // field in the parent, ensuring that the field ownership reverts to
        // the parent.
        insertGlobal();
        configurationTemplateManager.insertRecord("project/global/property", createProperty("p", "v"));
        insertChild();
        assertNotNull(configurationTemplateManager.getRecord("project/child/property"));

        MutableRecord overridingProperty = propertyType.createNewRecord(false);
        overridingProperty.put("name", "cp");
        configurationTemplateManager.saveRecord("project/child/property", overridingProperty);

        TemplateRecord childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertNotNull(childProperty);
        assertEquals(CHILD_PROJECT, childProperty.getOwner("name"));
        assertEquals("cp", childProperty.get("name"));

        configurationTemplateManager.saveRecord("project/global/property", overridingProperty);

        childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertNotNull(childProperty);
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("name"));
        assertEquals("cp", childProperty.get("name"));
    }

    public void testSaveRevertingToGrandchildValue()
    {
        // Save once overriding a field in a grandchild, and then save the same
        // value to that field in the grandparent, ensuring that the field
        // ownership reverts to the grandparent.
        insertToGrandchild();
        configurationTemplateManager.insertRecord("project/global/property", createProperty("p", "v"));
        assertNotNull(configurationTemplateManager.getRecord("project/child/property"));

        MutableRecord overridingProperty = propertyType.createNewRecord(false);
        overridingProperty.put("name", "gp");
        configurationTemplateManager.saveRecord("project/grandchild/property", overridingProperty);

        TemplateRecord childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("name"));
        assertEquals("p", childProperty.get("name"));
        TemplateRecord grandchildProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/grandchild/property");
        assertEquals(GRANDCHILD_PROJECT, grandchildProperty.getOwner("name"));
        assertEquals("gp", grandchildProperty.get("name"));

        configurationTemplateManager.saveRecord("project/global/property", overridingProperty);

        childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertEquals(GLOBAL_PROJECT, childProperty.getOwner("name"));
        assertEquals("gp", childProperty.get("name"));
        grandchildProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/grandchild/property");
        assertEquals(GLOBAL_PROJECT, grandchildProperty.getOwner("name"));
        assertEquals("gp", grandchildProperty.get("name"));
    }

    public void testSaveRevertingToGrandchildValueOverrideInChild()
    {
        // Save to override a field in a child, then save to override with
        // another value in the grandchild.  Finally save the grandchild value
        // in the parent, and make sure the field is still overridden all the
        // way down.
        insertToGrandchild();
        configurationTemplateManager.insertRecord("project/global/property", createProperty("p", "v"));
        assertNotNull(configurationTemplateManager.getRecord("project/child/property"));

        MutableRecord overridingProperty = propertyType.createNewRecord(false);
        overridingProperty.put("name", "cp");
        configurationTemplateManager.saveRecord("project/child/property", overridingProperty);
        overridingProperty.put("name", "gp");
        configurationTemplateManager.saveRecord("project/grandchild/property", overridingProperty);

        TemplateRecord childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertEquals(CHILD_PROJECT, childProperty.getOwner("name"));
        assertEquals("cp", childProperty.get("name"));
        TemplateRecord grandchildProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/grandchild/property");
        assertEquals(GRANDCHILD_PROJECT, grandchildProperty.getOwner("name"));
        assertEquals("gp", grandchildProperty.get("name"));

        configurationTemplateManager.saveRecord("project/global/property", overridingProperty);

        childProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/property");
        assertEquals(CHILD_PROJECT, childProperty.getOwner("name"));
        assertEquals("cp", childProperty.get("name"));
        grandchildProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/grandchild/property");
        assertEquals(GRANDCHILD_PROJECT, grandchildProperty.getOwner("name"));
        assertEquals("gp", grandchildProperty.get("name"));
    }

    public void testSaveOptimisedAway()
    {
        insertGlobal();
        insertChild();

        String path = "project/child";
        Project instance = configurationTemplateManager.getInstance(path, Project.class);
        Listener listener = registerListener();
        configurationTemplateManager.saveRecord(path, recordManager.select(path).copy(false, true));
        listener.assertEvents();
        assertSame(instance, configurationTemplateManager.getInstance(path, Project.class));
    }

    public void testShallowSaveOptimisedAwayDespiteNestedChange()
    {
        insertGlobal();
        insertChild();

        String path = "project/child";
        Project instance = configurationTemplateManager.getInstance(path, Project.class);
        Listener listener = registerListener();
        MutableRecord record = recordManager.select(path).copy(false, true);
        record.put("property", createProperty("foo", "bar"));
        configurationTemplateManager.saveRecord(path, record);

        listener.assertEvents();
        assertSame(instance, configurationTemplateManager.getInstance(path, Project.class));
    }

    public void testDeepSaveNotOptimisedAwayWhenNestedChange() throws TypeException
    {
        insertGlobal();
        insertChild();

        String path = "project/child";
        Project instance = configurationTemplateManager.getInstance(path, Project.class);
        Listener listener = registerListener();
        MutableRecord record = unstantiate(instance);
        record.put("property", createProperty("foo", "bar"));
        configurationTemplateManager.saveRecord(path, record, true);

        listener.assertEvents(new InsertEventSpec(path + "/property", false), new PostInsertEventSpec(path + "/property", false));
        Project newInstance = configurationTemplateManager.getInstance(path, Project.class);
        assertNotSame(instance, newInstance);
        assertEquals("bar", newInstance.getProperty().getValue());
    }

    public void testInstanceSaveOptimisedAway()
    {
        insertGlobal();
        insertChild();

        String path = "project/child";
        Project instance = configurationTemplateManager.getInstance(path, Project.class);
        Listener listener = registerListener();
        configurationTemplateManager.save(instance);
        listener.assertEvents();
        assertSame(instance, configurationTemplateManager.getInstance(path, Project.class));
    }

    public void testInstanceSaveNotOptimisedAwayWhenNestedChange() throws TypeException
    {
        insertGlobal();
        insertChild();

        String path = "project/child";
        Project instance = configurationTemplateManager.getInstance(path, Project.class);
        Listener listener = registerListener();
        Project clone = configurationTemplateManager.deepClone(instance);
        clone.setProperty(new Property("foo", "bar"));
        configurationTemplateManager.save(clone);

        listener.assertEvents(new InsertEventSpec(path + "/property", false), new PostInsertEventSpec(path + "/property", false));
        Project newInstance = configurationTemplateManager.getInstance(path, Project.class);
        assertNotSame(instance, newInstance);
        assertEquals("bar", newInstance.getProperty().getValue());
    }

    public void testNoInheritValuesAreNotScrubbed()
    {
        // If a we save a value to a child NoInherit property that is the
        // same as the value in the parent, it should *not* be scrubbed.
        insertGlobal();
        insertChild();

        TemplateRecord child = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals(CHILD_PROJECT, child.getOwner("description"));
        assertEquals(CHILD_DESCRIPTION, child.get("description"));
        MutableRecord record = child.flatten(false);
        
        record.put("description", GLOBAL_DESCRIPTION);
        configurationTemplateManager.saveRecord("project/child", record);

        child = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals(CHILD_PROJECT, child.getOwner("description"));
        assertEquals(GLOBAL_DESCRIPTION, child.get("description"));
        assertEquals(GLOBAL_DESCRIPTION, child.getMoi().get("description"));
    }

    public void testRenameProject()
    {
        insertGlobal();

        TemplateRecord template = (TemplateRecord) configurationTemplateManager.getRecord("project/global");
        MutableRecord record = template.flatten(false);
        record.put("name", "newname");
        configurationTemplateManager.saveRecord("project/global", record);

        assertNull(configurationTemplateManager.getRecord("project/global"));
        template = (TemplateRecord) configurationTemplateManager.getRecord("project/newname");
        assertEquals("newname", template.getOwner());
        assertEquals("newname", template.get("name"));
    }

    public void testRenameProjectLeavesDescendants()
    {
        insertGlobal();
        insertChild();

        TemplateRecord template = (TemplateRecord) configurationTemplateManager.getRecord("project/global");
        MutableRecord record = template.flatten(false);
        record.put("name", "newname");
        configurationTemplateManager.saveRecord("project/global", record);

        assertNull(configurationTemplateManager.getRecord("project/global"));
        template = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals(CHILD_PROJECT, template.getOwner());
        assertEquals(CHILD_PROJECT, template.get("name"));
        assertEquals("newname", template.getOwner("url"));
    }

    public void testRenameInherited()
    {
        // If we rename something that is inherited, the inherited value
        // needs to be renamed too.
        insertGlobal();
        insertChild();
        configurationTemplateManager.insertRecord("project/child/stages/default/properties", createProperty("foo", "bar"));

        TemplateRecord stageTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/global/stages/default");
        MutableRecord record = stageTemplate.flatten(false);
        record.put("name", "newname");
        String newPath = configurationTemplateManager.saveRecord("project/global/stages/default", record);
        assertEquals("project/global/stages/newname", newPath);

        stageTemplate = (TemplateRecord) configurationTemplateManager.getRecord(newPath);
        assertEquals(GLOBAL_PROJECT, stageTemplate.getOwner("name"));
        assertEquals("newname", stageTemplate.get("name"));

        assertNull(configurationTemplateManager.getRecord(PATH_CHILD_DEFAULT_STAGE));

        stageTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child/stages/newname");
        assertEquals(GLOBAL_PROJECT, stageTemplate.getOwner("name"));
        assertEquals("newname", stageTemplate.get("name"));

        TemplateRecord movedProperty = (TemplateRecord) configurationTemplateManager.getRecord("project/child/stages/newname/properties/foo");
        assertEquals("foo", movedProperty.get("name"));
    }

    public void testRenameHidden()
    {
        // If we rename something that is hidden, then the hidden key needs
        // to be updated to the new key.
        insertGlobal();
        insertChild();
        String propertyPath = configurationTemplateManager.insertRecord("project/global/stages/default/properties", createProperty("foo", "bar"));

        String inheritedPropertyPath = "project/child/stages/default/properties/foo";
        configurationTemplateManager.delete(inheritedPropertyPath);
        assertNull(configurationTemplateManager.getRecord(inheritedPropertyPath));

        MutableRecord property = configurationTemplateManager.getRecord(propertyPath).copy(true, true);
        property.put("name", "newfoo");
        configurationTemplateManager.saveRecord(propertyPath, property);
        String newInheritedPropertyPath = "project/child/stages/default/properties/newfoo";
        assertNull(configurationTemplateManager.getRecord(newInheritedPropertyPath));
        configurationTemplateManager.restore(newInheritedPropertyPath);
        assertNotNull(configurationTemplateManager.getRecord(newInheritedPropertyPath));
    }

    public void testRenameOrdered()
    {
        // If we rename something that is in an explicit order, then the
        // order key needs to be updated to the new key.
        insertGlobal();
        String stagesPath = "project/global/stages";
        configurationTemplateManager.insertRecord(stagesPath, createStage("another"));

        // Reorder - independent of the default order (i.e. swap whatever the
        // default is).
        Project global = (Project) configurationTemplateManager.getInstance("project/global");
        List<String> originalOrder = new LinkedList<String>(global.getStages().keySet());
        List<String> newOrder = new LinkedList<String>(originalOrder);
        Collections.reverse(newOrder);
        configurationTemplateManager.setOrder(stagesPath, newOrder);

        // Check new order
        assertEquals(newOrder, getOrder(stagesPath));

        // Rename the first property (if the order is not fixed this property
        // will then fall to the end).
        String firstStagePath = getPath(stagesPath, newOrder.get(0));
        MutableRecord property = configurationTemplateManager.getRecord(firstStagePath).copy(true, true);
        property.put("name", "renamed");
        configurationTemplateManager.saveRecord(firstStagePath, property);

        // Verify order is unchanged
        Collections.replaceAll(newOrder, PathUtils.getBaseName(firstStagePath), "renamed");
        assertEquals(newOrder, getOrder(stagesPath));
    }

    public void testRenameOrderedInDescendant()
    {
        insertGlobal();
        insertChild();
        String stagesPath = "project/global/stages";
        configurationTemplateManager.insertRecord(stagesPath, createStage("another"));

        // Reorder - independent of the default order (i.e. swap whatever the
        // default is).
        Project child = (Project) configurationTemplateManager.getInstance("project/child");
        List<String> originalOrder = new LinkedList<String>(child.getStages().keySet());
        List<String> newOrder = new LinkedList<String>(originalOrder);
        Collections.reverse(newOrder);
        String inheritedStagesPath = "project/child/stages";
        configurationTemplateManager.setOrder(inheritedStagesPath, newOrder);

        // Check new order
        assertEquals(newOrder, getOrder(inheritedStagesPath));

        // Rename the first property (if the order is not fixed this property
        // will then fall to the end).
        String firstStagePath = getPath(stagesPath, newOrder.get(0));
        MutableRecord property = configurationTemplateManager.getRecord(firstStagePath).copy(true, true);
        property.put("name", "renamed");
        configurationTemplateManager.saveRecord(firstStagePath, property);

        // Verify order is unchanged
        Collections.replaceAll(newOrder, PathUtils.getBaseName(firstStagePath), "renamed");
        assertEquals(newOrder, getOrder(inheritedStagesPath));
    }

    public void testDelete()
    {
        insertGlobal();
        configurationTemplateManager.delete("project/global");
        assertNull(configurationTemplateManager.getRecord("project/global"));
    }

    public void testDeleteRemovesDescendants()
    {
        insertGlobal();
        insertChild();
        configurationTemplateManager.delete("project/global");
        assertNull(configurationTemplateManager.getRecord("project/global"));
        assertNull(configurationTemplateManager.getRecord("project/child"));
    }

    public void testDeleteCollectionItemRemovesDescendants()
    {
        insertGlobal();
        insertChild();
        configurationTemplateManager.delete("project/global/stages/default");
        assertNull(configurationTemplateManager.getRecord("project/global/stages/default"));
        assertNull(configurationTemplateManager.getRecord(PATH_CHILD_DEFAULT_STAGE));
    }

    public void testDeepClonePreservesInherited()
    {
        insertGlobal();
        insertChild();

        Project child = (Project) configurationTemplateManager.getInstance("project/child");
        Project clone = configurationTemplateManager.deepClone(child);

        assertEquals("default", clone.getStages().get("default").getName());
    }

    public void testDeepCloneAndSavePreservesInheritedListItems()
    {
        insertGlobal();
        insertChild();

        configurationTemplateManager.insertRecord("project/global/propertiesList", createProperty("gp1", "gv1"));

        Project child = (Project) configurationTemplateManager.getInstance("project/child");
        Project clone = configurationTemplateManager.deepClone(child);
        clone.getPropertiesList().add(new Property("new", "value"));
        configurationTemplateManager.save(clone);
        
        child = (Project) configurationTemplateManager.getInstance("project/child");
        Property property = child.getPropertiesList().get(0);
        assertNotNull(property);
        assertEquals("gp1", property.getName());
        long expectedHandle = property.getHandle();

        clone = configurationTemplateManager.deepClone(child);
        clone.getPropertiesList().add(new Property("newer", "value"));
        configurationTemplateManager.save(clone);

        child = (Project) configurationTemplateManager.getInstance("project/child");
        property = child.getPropertiesList().get(0);
        assertNotNull(property);
        assertEquals("gp1", property.getName());
        // Verify the handle to make sure this is not a new property that looks the same.
        assertEquals(expectedHandle, property.getHandle());
    }

    public void testHideInherited()
    {
        insertGlobal();
        insertChild();

        hideStageAndAssertEvents(PATH_CHILD_DEFAULT_STAGE);
    }

    public void testHideListItem()
    {
        insertGlobal();
        insertChild();

        String path = configurationTemplateManager.insertRecord("project/global/propertiesList", createProperty("foo", "bar"));
        path = path.replace("global", "child");

        Listener listener = registerListener();
        configurationTemplateManager.delete(path);
        listener.assertEvents(new DeleteEventSpec(path, false), new PostDeleteEventSpec(path, false));
        assertHiddenItem(path);
    }

    public void testHideCompositeProperty()
    {
        MutableRecord global = createGlobal();
        String propertyName = "prop";
        global.put("property", createProperty(propertyName, "value"));
        configurationTemplateManager.insertRecord("project", global);
        insertChild();

        String path = "project/child/property";
        try
        {
            configurationTemplateManager.delete(path);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("Invalid path '" + path + "': cannot delete an inherited composite property", e.getMessage());
        }
    }

    public void testHideOverriddenSimpleProperty()
    {
        insertGlobal();
        insertChild();

        Stage stage = configurationTemplateManager.getInstance(PATH_CHILD_DEFAULT_STAGE, Stage.class);
        stage.setRecipe("over");
        configurationTemplateManager.save(stage);

        hideStageAndAssertEvents(PATH_CHILD_DEFAULT_STAGE);
    }

    public void testHideOverriddenNestedItem()
    {
        insertGlobal();
        insertChild();

        String propertyPath = "project/child/stages/default/properties/p1";
        Property property = configurationTemplateManager.getInstance(propertyPath, Property.class);
        property.setValue("over");
        configurationTemplateManager.save(property);

        hideStageAndAssertEvents(PATH_CHILD_DEFAULT_STAGE);
    }

    public void testHideOverriddenNewNestedItem()
    {
        insertGlobal();
        insertChild();

        Stage stage = configurationTemplateManager.getInstance(PATH_CHILD_DEFAULT_STAGE, Stage.class);
        stage.addProperty("new", "item");
        configurationTemplateManager.save(stage);

        Listener listener = registerListener();
        hideStage(PATH_CHILD_DEFAULT_STAGE);
        listener.assertEvents(new DeleteEventSpec(PATH_CHILD_DEFAULT_STAGE, false),
                              new DeleteEventSpec(PATH_CHILD_DEFAULT_STAGE + "/properties/p1", true),
                              new DeleteEventSpec(PATH_CHILD_DEFAULT_STAGE + "/properties/new", true),
                              new PostDeleteEventSpec(PATH_CHILD_DEFAULT_STAGE, false),
                              new PostDeleteEventSpec(PATH_CHILD_DEFAULT_STAGE + "/properties/p1", true),
                              new PostDeleteEventSpec(PATH_CHILD_DEFAULT_STAGE + "/properties/new", true));
    }

    public void testHideOverriddenAlreadyHiddenNestedItem()
    {
        insertGlobal();
        insertChild();

        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE + "/properties/p1");

        Listener listener = registerListener();
        hideStage(PATH_CHILD_DEFAULT_STAGE);
        listener.assertEvents(new DeleteEventSpec(PATH_CHILD_DEFAULT_STAGE, false), new PostDeleteEventSpec(PATH_CHILD_DEFAULT_STAGE, false));
    }

    public void testAddItemToHidden()
    {
        String parentPath = insertGlobal();
        String childPath = insertChild();

        String stagePath = "/stages/default";
        hideStage(childPath + stagePath);

        Listener listener = registerListener();
        insertProperty(parentPath + stagePath + "/properties", "newproperty");
        assertEquals(0, listener.getEvents().size());
        
        // On restore the new property should be in the child
        configurationTemplateManager.restore(childPath + stagePath);
        Property property = configurationTemplateManager.getInstance(childPath + stagePath + "/properties/newproperty", Property.class);
        assertNotNull(property);
        assertEquals(PROPERTY_VALUE, property.getValue());
    }

    public void testDeleteItemFromHidden()
    {
        String parentPath = insertGlobal();
        String childPath = insertChild();

        String stagePath = "/stages/default";
        String childPropertyPath = childPath + stagePath + "/properties/p1";
        assertTrue(configurationTemplateManager.pathExists(childPropertyPath));

        hideStage(childPath + stagePath);

        Listener listener = registerListener();
        configurationTemplateManager.delete(parentPath + stagePath + "/properties/p1");
        assertEquals(0, listener.getEvents().size());

        // On restore the new property should not be in the child
        configurationTemplateManager.restore(childPath + stagePath);
        assertFalse(configurationTemplateManager.pathExists(childPropertyPath));
    }

    public void testHideIndirectlyInherited()
    {
        insertToGrandchild();

        hideStageAndAssertEvents(PATH_GRANDCHILD_DEFAULT_STAGE);
    }

    public void testHideInheritedWithDescendant()
    {
        insertToGrandchild();

        hideStageAndAssertEvents(PATH_CHILD_DEFAULT_STAGE, PATH_GRANDCHILD_DEFAULT_STAGE);
        assertDeletedStage(PATH_GRANDCHILD_DEFAULT_STAGE);
    }

    public void testHideDescendantOverridesSimpleProperty()
    {
        insertToGrandchild();

        Stage stage = configurationTemplateManager.getInstance(PATH_GRANDCHILD_DEFAULT_STAGE, Stage.class);
        stage.setRecipe("over");
        configurationTemplateManager.save(stage);

        hideStageAndAssertEvents(PATH_CHILD_DEFAULT_STAGE, PATH_GRANDCHILD_DEFAULT_STAGE);
        assertDeletedStage(PATH_GRANDCHILD_DEFAULT_STAGE);
    }

    public void testHideDescendantOverridesNestedItem()
    {
        insertToGrandchild();

        String propertyPath = "project/grandchild/stages/default/properties/p1";
        Property property = configurationTemplateManager.getInstance(propertyPath, Property.class);
        property.setValue("over");
        configurationTemplateManager.save(property);

        hideStageAndAssertEvents(PATH_CHILD_DEFAULT_STAGE, PATH_GRANDCHILD_DEFAULT_STAGE);
    }

    public void testHideDescendantAddsNewNestedItem()
    {
        insertToGrandchild();

        Stage stage = configurationTemplateManager.getInstance(PATH_GRANDCHILD_DEFAULT_STAGE, Stage.class);
        stage.addProperty("new", "item");
        configurationTemplateManager.save(stage);

        Listener listener = registerListener();
        hideStage(PATH_CHILD_DEFAULT_STAGE);
        listener.assertEvents(new DeleteEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE, false),
                              new DeleteEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE + "/properties/p1", true),
                              new DeleteEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE + "/properties/new", true),
                              new PostDeleteEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE, false),
                              new PostDeleteEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE + "/properties/p1", true),
                              new PostDeleteEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE + "/properties/new", true));
        assertDeletedStage(PATH_GRANDCHILD_DEFAULT_STAGE);
    }

    public void testHideDescendantHidesNestedItem()
    {
        insertToGrandchild();

        configurationTemplateManager.delete(PATH_GRANDCHILD_DEFAULT_STAGE + "/properties/p1");

        Listener listener = registerListener();
        hideStage(PATH_CHILD_DEFAULT_STAGE);
        listener.assertEvents(new DeleteEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE, false), new PostDeleteEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE, false));
        assertDeletedStage(PATH_GRANDCHILD_DEFAULT_STAGE);
    }

    public void testHideDescendantAlreadyHidden()
    {
        insertToGrandchild();

        String gcStagePath = PATH_GRANDCHILD_DEFAULT_STAGE;
        configurationTemplateManager.delete(gcStagePath);

        Listener listener = registerListener();
        hideStage(PATH_CHILD_DEFAULT_STAGE);
        listener.assertEvents();
        assertDeletedStage(gcStagePath);
    }

    public void testHideInheritedWithIndirectDescendant()
    {
        insertGlobal();
        insertTemplateChild();
        insertTemplateGrandchild();

        MutableRecord greatGrandchild = createProject("greatgrandchild", "omg");
        configurationTemplateManager.setParentTemplate(greatGrandchild, configurationTemplateManager.getRecord("project/grandchild").getHandle());
        configurationTemplateManager.insertRecord("project", greatGrandchild);

        String ggcStagePath = "project/greatgrandchild/stages/default";
        hideStageAndAssertEvents(PATH_CHILD_DEFAULT_STAGE, ggcStagePath);
        assertDeletedStage(ggcStagePath);
    }

    public void testRestoreEmptyPath()
    {
        failedRestoreHelper("", "Invalid path: path is empty");
    }

    public void testRestoreUnknownScope()
    {
        failedRestoreHelper("unknown", "Invalid path 'unknown': references non-existant root scope 'unknown'");
    }

    public void testRestoreShortPath()
    {
        failedRestoreHelper("project/foo", "Invalid path 'project/foo': only records nested within a template can have been hidden");
    }

    public void testRestoreNonTemplatedScope()
    {
        failedRestoreHelper("nproject/foo/bar", "Invalid path 'nproject/foo/bar': not a templated scope");
    }

    public void testRestoreNoSuchPath()
    {
        failedRestoreHelper("project/foo/bar", "Invalid path 'project/foo/bar': parent does not exist");
    }

    public void testRestoreRootNotHidden()
    {
        insertGlobal();
        failedRestoreHelper("project/global/stages/default", "Invalid path 'project/global/stages/default': not hidden");
    }

    public void testRestoreNotHidden()
    {
        insertGlobal();
        insertChild();
        failedRestoreHelper(PATH_CHILD_DEFAULT_STAGE, "Invalid path 'project/child/stages/default': not hidden");
    }

    private void failedRestoreHelper(String path, String message)
    {
        try
        {
            configurationTemplateManager.restore(path);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals(message, e.getMessage());
        }
    }

    public void testRestoreInherited()
    {
        insertGlobal();
        insertChild();

        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);
        Listener listener = registerListener();
        configurationTemplateManager.restore(PATH_CHILD_DEFAULT_STAGE);
        listener.assertEvents(new InsertEventSpec(PATH_CHILD_DEFAULT_STAGE, false), new PostInsertEventSpec(PATH_CHILD_DEFAULT_STAGE, false), new InsertEventSpec(PATH_CHILD_DEFAULT_STAGE + "/properties/p1", true), new PostInsertEventSpec(PATH_CHILD_DEFAULT_STAGE + "/properties/p1", true));
        assertStage(PATH_CHILD_DEFAULT_STAGE);
        TemplateRecord stagesRecord = (TemplateRecord) configurationTemplateManager.getRecord(PathUtils.getParentPath(PATH_CHILD_DEFAULT_STAGE));
        assertEquals("global", stagesRecord.getOwner("default"));
    }

    public void testRestoreListItem()
    {
        insertGlobal();
        insertChild();

        String path = configurationTemplateManager.insertRecord("project/global/propertiesList", createProperty("foo", "bar"));
        path = path.replace("global", "child");

        configurationTemplateManager.delete(path);
        Listener listener = registerListener();
        configurationTemplateManager.restore(path);
        listener.assertEvents(new InsertEventSpec(path, false), new PostInsertEventSpec(path, false));
        assertTrue(configurationTemplateManager.pathExists(path));
        assertEquals("foo", configurationTemplateManager.getRecord(path).get("name"));
    }

    public void testHideAfterRestore()
    {
        insertGlobal();
        insertChild();

        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);
        configurationTemplateManager.restore(PATH_CHILD_DEFAULT_STAGE);
        hideStageAndAssertEvents(PATH_CHILD_DEFAULT_STAGE);
    }

    public void testModifyAfterRestore()
    {
        insertGlobal();
        insertChild();

        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);
        configurationTemplateManager.restore(PATH_CHILD_DEFAULT_STAGE);
        Stage stage = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(PATH_CHILD_DEFAULT_STAGE, Stage.class));
        stage.setRecipe("edited");

        Listener listener = registerListener();
        configurationTemplateManager.save(stage);
        listener.assertEvents(new SaveEventSpec(PATH_CHILD_DEFAULT_STAGE), new PostSaveEventSpec(PATH_CHILD_DEFAULT_STAGE));

        assertEquals("edited", configurationTemplateManager.getInstance(PATH_CHILD_DEFAULT_STAGE, Stage.class).getRecipe());
    }

    public void testInsertChildAfterRestore()
    {
        insertGlobal();
        insertChild();

        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);
        configurationTemplateManager.restore(PATH_CHILD_DEFAULT_STAGE);
        Stage stage = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(PATH_CHILD_DEFAULT_STAGE, Stage.class));
        stage.addProperty("new", "value");

        Listener listener = registerListener();
        configurationTemplateManager.save(stage);
        listener.assertEvents(new InsertEventSpec(PATH_CHILD_DEFAULT_STAGE + "/properties/new", false), new PostInsertEventSpec(PATH_CHILD_DEFAULT_STAGE + "/properties/new", false));

        assertEquals("value", configurationTemplateManager.getInstance(PATH_CHILD_DEFAULT_STAGE, Stage.class).getProperties().get("new").getValue());
    }

    public void testRestoreWithDescendant()
    {
        insertToGrandchild();

        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);
        Listener listener = registerListener();
        configurationTemplateManager.restore(PATH_CHILD_DEFAULT_STAGE);
        String gcStagePath = PATH_GRANDCHILD_DEFAULT_STAGE;
        listener.assertEvents(new InsertEventSpec(gcStagePath, false), new PostInsertEventSpec(gcStagePath, false), new InsertEventSpec(gcStagePath + "/properties/p1", true), new PostInsertEventSpec(gcStagePath + "/properties/p1", true));
        assertStage(gcStagePath);
    }

    public void testRestoreAfterHideWithDescendantAlreadyHidden()
    {
        insertToGrandchild();

        configurationTemplateManager.delete(PATH_GRANDCHILD_DEFAULT_STAGE);
        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);

        Listener listener = registerListener();
        configurationTemplateManager.restore(PATH_CHILD_DEFAULT_STAGE);
        listener.assertEvents(new InsertEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE, false), new PostInsertEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE, false), new PostInsertEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE + "/properties/p1", true), new PostInsertEventSpec(PATH_GRANDCHILD_DEFAULT_STAGE + "/properties/p1", true));
        assertStage(PATH_GRANDCHILD_DEFAULT_STAGE);
    }

    public void testRestoreWithHiddenInSiblingTemplate()
    {
        final String PROJECT_SIBLING_CHILD = "child2";
        final String PROJECT_SIBLING_GRANDCHILD = "grandchild2";

        insertToGrandchild();

        MutableRecord record = createProject(PROJECT_SIBLING_CHILD, "");
        configurationTemplateManager.markAsTemplate(record);
        setParent(record, GLOBAL_PROJECT);
        String siblingChildPath = configurationTemplateManager.insertRecord(SCOPE_PROJECT, record);
        String siblingChildStagePath = getPath(siblingChildPath, PATH_DEFAULT_STAGE);

        record = createProject(PROJECT_SIBLING_GRANDCHILD, "");
        setParent(record, PROJECT_SIBLING_CHILD);
        String siblingGrandchildPath = configurationTemplateManager.insertRecord(SCOPE_PROJECT, record);
        String siblingGrandchildStagePath = getPath(siblingGrandchildPath, PATH_DEFAULT_STAGE);

        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);
        configurationTemplateManager.delete(siblingChildStagePath);
        assertSiblingStageHidden(siblingChildStagePath, siblingGrandchildStagePath);

        configurationTemplateManager.restore(PATH_CHILD_DEFAULT_STAGE);
        assertSiblingStageHidden(siblingChildStagePath, siblingGrandchildStagePath);
    }

    private void assertSiblingStageHidden(String siblingChildStagePath, String siblingGrandchildStagePath)
    {
        assertHiddenItem(siblingChildStagePath);
        // These checks are to verify no skeletons exist (CIB-1973).
        assertNull(recordManager.select(siblingChildStagePath));
        assertNull(recordManager.select(siblingGrandchildStagePath));
    }

    private void insertToGrandchild()
    {
        insertGlobal();
        insertTemplateChild();
        insertGrandchild();
    }

    private void hideStage(String stagePath)
    {
        configurationTemplateManager.delete(stagePath);
        assertFalse(configurationTemplateManager.pathExists(stagePath));
        assertHiddenItem(stagePath);
    }

    private void assertStage(String stagePath)
    {
        assertTrue(configurationTemplateManager.pathExists(stagePath));
        
        String stagesPath = PathUtils.getParentPath(stagePath);
        TemplateRecord parent = (TemplateRecord) configurationTemplateManager.getRecord(stagesPath);
        assertEquals(1, parent.size());
        assertEquals(0, TemplateRecord.getHiddenKeys(parent).size());
        TemplateRecord record = (TemplateRecord) parent.get("default");
        TemplateRecord properties = (TemplateRecord) record.get("properties");
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey("p1"));
    }

    private void assertHiddenItem(String path)
    {
        String parentPath = PathUtils.getParentPath(path);
        assertTrue(configurationTemplateManager.pathExists(parentPath));
        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord(parentPath);
        assertEquals(0, record.keySet().size());
        Set<String> hidden = TemplateRecord.getHiddenKeys(record);
        assertEquals(1, hidden.size());
        assertTrue(hidden.contains(PathUtils.getBaseName(path)));
    }

    private void assertDeletedStage(String stagePath)
    {
        assertFalse(configurationTemplateManager.pathExists(stagePath));
        String stagesPath = PathUtils.getParentPath(stagePath);
        assertTrue(configurationTemplateManager.pathExists(stagesPath));
        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord(stagesPath);
        assertEquals(0, record.keySet().size());
        Set<String> hidden = TemplateRecord.getHiddenKeys(record);
        assertEquals(0, hidden.size());
    }

    private void hideStageAndAssertEvents(String stagePath)
    {
        hideStageAndAssertEvents(stagePath, stagePath);
    }

    private void hideStageAndAssertEvents(String deletePath, String concretePath)
    {
        Listener listener = registerListener();
        hideStage(deletePath);
        listener.assertEvents(new DeleteEventSpec(concretePath, false), new DeleteEventSpec(concretePath + "/properties/p1", true), new PostDeleteEventSpec(concretePath, false), new PostDeleteEventSpec(concretePath + "/properties/p1", true));
    }

    public void testSetOrderEmptyPath()
    {
        failedSetOrderHelper("", Collections.<String>emptyList(), "Invalid path: path is empty");
    }

    public void testSetOrderNoSuchPath()
    {
        failedSetOrderHelper("project/path", Collections.<String>emptyList(), "Invalid path 'project/path': references unknown child 'path' of collection");
    }

    public void testSetOrderNotACollection()
    {
        insertGlobal();
        failedSetOrderHelper("project/global/property", Collections.<String>emptyList(), "Invalid path 'project/global/property': does not refer to a collection");
    }

    public void testSetOrderNonOrderedCollection()
    {
        insertGlobal();
        failedSetOrderHelper("project/global/properties", Collections.<String>emptyList(), "Invalid path 'project/global/properties': collection is not ordered");
    }

    public void testSetOrderInvalidKey()
    {
        insertGlobal();
        failedSetOrderHelper("project/global/stages", Arrays.asList("foo"), "Invalid order: item 'foo' does not exist in collection at path 'project/global/stages'");
    }

    public void testSetOrderHiddenKey()
    {
        insertGlobal();
        insertChild();

        // Should work now
        configurationTemplateManager.setOrder("project/child/stages", Arrays.asList("default"));

        configurationTemplateManager.delete(PATH_CHILD_DEFAULT_STAGE);

        // And fail now
        failedSetOrderHelper("project/child/stages", Arrays.asList("default"), "Invalid order: item 'default' does not exist in collection at path 'project/child/stages'");
    }

    private void failedSetOrderHelper(String path, List<String> order, String message)
    {
        try
        {
            configurationTemplateManager.setOrder(path, order);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals(message, e.getMessage());
        }
    }

    public void testSetOrder()
    {
        MutableRecord project = createProject("test", "desc");
        configurationTemplateManager.insertRecord(SCOPE_NON_TEMPLATED_PROJECT, project);
        String stagesPath = "nproject/test/stages";
        insertStage(stagesPath, "one", 0);
        insertStage(stagesPath, "two", 0);
        assertEquals(Arrays.asList("one", "two"), getOrder(stagesPath));

        Listener listener = registerListener();
        configurationTemplateManager.setOrder(stagesPath, Arrays.asList("two", "one"));
        listener.assertEvents(new SaveEventSpec("nproject/test"), new PostSaveEventSpec("nproject/test"));
        assertEquals(Arrays.asList("two", "one"), getOrder(stagesPath));
    }

    public void testSetOrderIncomplete()
    {
        MutableRecord project = createProject("test", "desc");
        configurationTemplateManager.insertRecord(SCOPE_NON_TEMPLATED_PROJECT, project);
        String stagesPath = "nproject/test/stages";
        insertStage(stagesPath, "one", 0);
        insertStage(stagesPath, "two", 0);
        insertStage(stagesPath, "three", 0);
        
        configurationTemplateManager.setOrder(stagesPath, Arrays.asList("two", "three"));
        assertEquals(Arrays.asList("two", "three", "one"), getOrder(stagesPath));
    }

    public void testAddAfterSettingOrder()
    {
        MutableRecord project = createProject("test", "desc");
        configurationTemplateManager.insertRecord(SCOPE_NON_TEMPLATED_PROJECT, project);
        String stagesPath = "nproject/test/stages";
        insertStage(stagesPath, "one", 0);
        insertStage(stagesPath, "two", 0);
        
        configurationTemplateManager.setOrder(stagesPath, Arrays.asList("two", "one"));
        assertEquals(Arrays.asList("two", "one"), getOrder(stagesPath));
        insertStage(stagesPath, "three", 0);
        assertEquals(Arrays.asList("two", "one", "three"), getOrder(stagesPath));
    }

    public void testDeleteAfterSettingOrder()
    {
        MutableRecord project = createProject("test", "desc");
        configurationTemplateManager.insertRecord(SCOPE_NON_TEMPLATED_PROJECT, project);
        String stagesPath = "nproject/test/stages";
        insertStage(stagesPath, "one", 0);
        insertStage(stagesPath, "two", 0);
        insertStage(stagesPath, "three", 0);

        configurationTemplateManager.setOrder(stagesPath, Arrays.asList("three", "two", "one"));
        assertEquals(Arrays.asList("three", "two", "one"), getOrder(stagesPath));
        configurationTemplateManager.delete(getPath(stagesPath, "three"));
        assertEquals(Arrays.asList("two", "one"), getOrder(stagesPath));

        // Re-insert to also check that it lands at the end again (i.e. order
        // was actually cleaned).
        insertStage(stagesPath, "three", 0);
        assertEquals(Arrays.asList("two", "one", "three"), getOrder(stagesPath));
    }

    public void testSetOrderTemplated()
    {
        insertGlobal();
        insertChild();
        String stagesPath = "project/global/stages";
        insertStage(stagesPath, "default2", 0);
        assertEquals(Arrays.asList("default", "default2"), getOrder(stagesPath));

        Listener listener = registerListener();
        configurationTemplateManager.setOrder(stagesPath, Arrays.asList("default2", "default"));
        listener.assertEvents(new SaveEventSpec("project/child"), new PostSaveEventSpec("project/child"));
        assertEquals(Arrays.asList("default2", "default"), getOrder(stagesPath));
    }

    public void testSetOrderInheritedItems()
    {
        insertGlobal();
        insertChild();

        String stagesPath = "project/child/stages";
        insertStage(stagesPath, "childs", 0);
        assertEquals(Arrays.asList("default", "childs"), getOrder(stagesPath));

        configurationTemplateManager.setOrder(stagesPath, Arrays.asList("childs", "default"));
        assertEquals(Arrays.asList("childs", "default"), getOrder(stagesPath));
    }

    public void testInheritOrder()
    {
        insertGlobal();
        insertChild();

        String parentStagesPath = "project/global/stages";
        insertStage(parentStagesPath, "default2", 0);

        String childStagesPath = "project/child/stages";
        insertStage(childStagesPath, "childs", 0);

        assertEquals(Arrays.asList("default", "default2", "childs"), getOrder(childStagesPath));

        configurationTemplateManager.setOrder(parentStagesPath, Arrays.asList("default2", "default"));
        assertEquals(Arrays.asList("default2", "default", "childs"), getOrder(childStagesPath));
        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord(childStagesPath);
        assertEquals("global", childTemplate.getMetaOwner(CollectionType.ORDER_KEY));
    }

    public void testOverrideOrder()
    {
        insertGlobal();
        insertChild();

        String parentStagesPath = "project/global/stages";
        insertStage(parentStagesPath, "default2", 0);

        String childStagesPath = "project/child/stages";
        insertStage(childStagesPath, "childs", 0);

        configurationTemplateManager.setOrder(parentStagesPath, Arrays.asList("default2", "default"));
        configurationTemplateManager.setOrder(childStagesPath, Arrays.asList("default", "childs", "default2"));
        assertEquals(Arrays.asList("default", "childs", "default2"), getOrder(childStagesPath));
        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord(childStagesPath);
        assertEquals("child", childTemplate.getMetaOwner(CollectionType.ORDER_KEY));
    }

    public void testOverrideOrderWithSame()
    {
        insertGlobal();
        insertChild();

        String parentStagesPath = "project/global/stages";
        insertStage(parentStagesPath, "default2", 0);

        String childStagesPath = "project/child/stages";
        insertStage(childStagesPath, "childs", 0);

        List<String> order = Arrays.asList("default2", "default");
        configurationTemplateManager.setOrder(parentStagesPath, order);
        configurationTemplateManager.setOrder(childStagesPath, order);
        assertEquals(Arrays.asList("default2", "default", "childs"), getOrder(childStagesPath));
        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord(childStagesPath);
        assertEquals("global", childTemplate.getMetaOwner(CollectionType.ORDER_KEY));
    }

    public void testInheritedOrderRefersToHidden()
    {
        insertGlobal();
        insertChild();

        String parentStagesPath = "project/global/stages";
        insertStage(parentStagesPath, "default2", 0);
        insertStage(parentStagesPath, "default3", 0);

        String childStagesPath = "project/child/stages";
        insertStage(childStagesPath, "childs", 0);

        String hidePath = "project/child/stages/default2";
        configurationTemplateManager.delete(hidePath);
        configurationTemplateManager.setOrder(parentStagesPath, Arrays.asList("default2", "default", "default3"));
        assertEquals(Arrays.asList("default", "default3", "childs"), getOrder(childStagesPath));
        configurationTemplateManager.restore(hidePath);
        assertEquals(Arrays.asList("default2", "default", "default3", "childs"), getOrder(childStagesPath));
    }

    public void testHideItemInInheritedOrder()
    {
        insertGlobal();
        insertChild();

        String parentStagesPath = "project/global/stages";
        insertStage(parentStagesPath, "default2", 0);
        insertStage(parentStagesPath, "default3", 0);

        String childStagesPath = "project/child/stages";
        insertStage(childStagesPath, "childs", 0);

        configurationTemplateManager.setOrder(parentStagesPath, Arrays.asList("default2", "default", "default3"));
        assertEquals(Arrays.asList("default2", "default", "default3", "childs"), getOrder(childStagesPath));

        String hidePath = "project/child/stages/default2";
        configurationTemplateManager.delete(hidePath);
        assertEquals(Arrays.asList("default", "default3", "childs"), getOrder(childStagesPath));

        configurationTemplateManager.restore(hidePath);
        assertEquals(Arrays.asList("default2", "default", "default3", "childs"), getOrder(childStagesPath));
    }

    public void testDeleteItemInDescendantOrder()
    {
        insertGlobal();
        insertChild();

        String parentStagesPath = "project/global/stages";
        insertStage(parentStagesPath, "default2", 0);
        insertStage(parentStagesPath, "default3", 0);

        String childStagesPath = "project/child/stages";
        insertStage(childStagesPath, "childs", 0);

        configurationTemplateManager.setOrder(childStagesPath, Arrays.asList("default2", "default", "childs", "default3"));
        assertEquals(Arrays.asList("default2", "default", "childs", "default3"), getOrder(childStagesPath));
        configurationTemplateManager.delete(getPath(parentStagesPath, "default"));
        assertEquals(Arrays.asList("default2", "childs", "default3"), getOrder(childStagesPath));

        // Insert again and it should now appear at the end
        insertStage(parentStagesPath, "default", 0);
        assertEquals(Arrays.asList("default2", "childs", "default3", "default"), getOrder(childStagesPath));
    }

    public void testHideAfterSettingOrder()
    {
        insertGlobal();
        insertChild();

        String parentStagesPath = "project/global/stages";
        insertStage(parentStagesPath, "default2", 0);
        insertStage(parentStagesPath, "default3", 0);

        String childStagesPath = "project/child/stages";
        insertStage(childStagesPath, "childs", 0);

        configurationTemplateManager.setOrder(childStagesPath, Arrays.asList("default2", "default", "childs", "default3"));
        assertEquals(Arrays.asList("default2", "default", "childs", "default3"), getOrder(childStagesPath));
        String hidePath = getPath(childStagesPath, "default");
        configurationTemplateManager.delete(hidePath);
        assertEquals(Arrays.asList("default2", "childs", "default3"), getOrder(childStagesPath));

        // Restore it and it should now appear at the end
        configurationTemplateManager.restore(hidePath);
        assertEquals(Arrays.asList("default2", "childs", "default3", "default"), getOrder(childStagesPath));
    }

    public void testHideItemInDescendantOrder()
    {
        insertToGrandchild();

        String parentStagesPath = "project/global/stages";
        insertStage(parentStagesPath, "default2", 0);

        String gcStagesPath = "project/grandchild/stages";
        insertStage(gcStagesPath, "gcs", 0);

        configurationTemplateManager.setOrder(gcStagesPath, Arrays.asList("default2", "default", "gcs"));
        assertEquals(Arrays.asList("default2", "default", "gcs"), getOrder(gcStagesPath));
        String hidePath = "project/child/stages/default2";
        configurationTemplateManager.delete(hidePath);
        assertEquals(Arrays.asList("default", "gcs"), getOrder(gcStagesPath));

        // Restore it and it should now appear at the end
        configurationTemplateManager.restore(hidePath);
        assertEquals(Arrays.asList("default", "gcs", "default2"), getOrder(gcStagesPath));
    }

    @SuppressWarnings({"unchecked"})
    private List<String> getOrder(String path)
    {
        // Determine the order by iterating over an instance - this verifies
        // the full end-to-end application of ordering.
        CollectionType type = configurationTemplateManager.getType(path, CollectionType.class);
        if(type instanceof MapType)
        {
            ConfigurationMap instance = configurationTemplateManager.getInstance(path, ConfigurationMap.class);
            return new LinkedList<String>(instance.keySet());
        }
        else if(type instanceof ListType)
        {
            ConfigurationList instance = configurationTemplateManager.getInstance(path, ConfigurationList.class);
            return newArrayList(transform(instance, new Function<Configuration, String>()
            {
                public String apply(Configuration o)
                {
                    return PathUtils.getBaseName(o.getConfigurationPath());
                }
            }));
        }
        else
        {
            fail();
            return null;
        }
    }

    private String insertGlobal()
    {
        MutableRecord global = createGlobal();
        return configurationTemplateManager.insertRecord(SCOPE_PROJECT, global);
    }

    private MutableRecord createGlobal()
    {
        MutableRecord record = createProject(GLOBAL_PROJECT, GLOBAL_DESCRIPTION);
        configurationTemplateManager.markAsTemplate(record);
        return record;
    }

    private String insertChild()
    {
        MutableRecord child = createChild();
        return configurationTemplateManager.insertRecord(SCOPE_PROJECT, child);
    }

    private void insertTemplateChild()
    {
        MutableRecord child = createChild();
        configurationTemplateManager.markAsTemplate(child);
        configurationTemplateManager.insertRecord(SCOPE_PROJECT, child);
    }

    private MutableRecord createChild()
    {
        MutableRecord child = createProject(CHILD_PROJECT, CHILD_DESCRIPTION);
        setParent(child, GLOBAL_PROJECT);
        return child;
    }

    private void insertGrandchild()
    {
        MutableRecord grandchild = createGrandchild();
        configurationTemplateManager.insertRecord(SCOPE_PROJECT, grandchild);
    }

    private void insertTemplateGrandchild()
    {
        MutableRecord grandchild = createGrandchild();
        configurationTemplateManager.markAsTemplate(grandchild);
        configurationTemplateManager.insertRecord(SCOPE_PROJECT, grandchild);
    }

    private MutableRecord createGrandchild()
    {
        MutableRecord child = createProject(GRANDCHILD_PROJECT, GRANDCHILD_DESCRIPTION);
        setParent(child, CHILD_PROJECT);
        return child;
    }

    private void setParent(MutableRecord projectRecord, String parentName)
    {
        Record parentRecord = configurationTemplateManager.getRecord(getPath(SCOPE_PROJECT, parentName));
        configurationTemplateManager.setParentTemplate(projectRecord, parentRecord.getHandle());
    }

    private void insertStage(String path, String name, int properties)
    {
        MutableRecord stage = stageType.createNewRecord(false);
        stage.put("name", name);
        configurationTemplateManager.insertRecord(path, stage);
        String propertiesPath = getPath(path, name, "properties");
        for(int i = 0; i < properties; i++)
        {
            insertProperty(propertiesPath, "property " + i);
        }
    }

    private void insertProperty(String path, String name)
    {
        MutableRecord property = propertyType.createNewRecord(false);
        property.put("name", name);
        property.put("value", PROPERTY_VALUE);
        configurationTemplateManager.insertRecord(path, property);
    }

    private MutableRecord createProject(String name, String description)
    {
        // Only apply defaults to the global project
        MutableRecord record = projectType.createNewRecord(name.equals(GLOBAL_PROJECT));
        record.put("name", name);
        record.put("description", description);
        return record;
    }

    private MutableRecord createProperty(String name, String value)
    {
        MutableRecord record = propertyType.createNewRecord(true);
        record.put("name", name);
        record.put("value", value);
        return record;
    }

    private MutableRecord createStage(String name)
    {
        MutableRecord record = stageType.createNewRecord(true);
        record.put("name", name);
        return record;
    }

    private void assertEmptyCollection(TemplateRecord record, String property)
    {
        TemplateRecord collection = (TemplateRecord) record.get(property);
        assertEquals(0, collection.size());
    }

    private void assertDefaultStages(TemplateRecord stages, String owner)
    {
        assertEquals(1, stages.size());
        assertEquals(owner, stages.getOwner());
        assertEquals(GLOBAL_PROJECT, stages.getOwner("default"));
        assertDefaultStage((TemplateRecord) stages.get("default"), owner);
    }

    private void assertDefaultStage(TemplateRecord stage, String owner)
    {
        assertEquals(owner, stage.getOwner());
        assertEquals(GLOBAL_PROJECT, stage.getOwner("name"));
        assertEquals("default", stage.get("name"));
    }

    public static enum Coolness
    {
        JOHN_HOWARD,
        LAME,
        OK_I_GUESS,
        COOL,
        AWESOME,
        PULSE
    }

    @SymbolicName("project")
    public static class Project extends AbstractNamedConfiguration
    {
        @NoInherit
        private String description;
        private String url;
        private Property property;
        private Map<String, Property> properties = new HashMap<String, Property>();
        @Ordered
        private Map<String, Stage> stages = new HashMap<String, Stage>();
        private List<Coolness> coolnesses = new LinkedList<Coolness>();
        private List<Property> propertiesList = new LinkedList<Property>();

        public Project()
        {
            Stage stage = new Stage("default");
            stage.addProperty("p1", "v1");
            stages.put(stage.getName(), stage);
        }

        public Project(String name, String description)
        {
            super(name);
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        public Property getProperty()
        {
            return property;
        }

        public void setProperty(Property property)
        {
            this.property = property;
        }

        public Map<String, Property> getProperties()
        {
            return properties;
        }

        public void setProperties(Map<String, Property> properties)
        {
            this.properties = properties;
        }

        public Map<String, Stage> getStages()
        {
            return stages;
        }

        public void setStages(Map<String, Stage> stages)
        {
            this.stages = stages;
        }

        public List<Coolness> getCoolnesses()
        {
            return coolnesses;
        }

        public void setCoolnesses(List<Coolness> coolnesses)
        {
            this.coolnesses = coolnesses;
        }

        public List<Property> getPropertiesList()
        {
            return propertiesList;
        }

        public void setPropertiesList(List<Property> propertiesList)
        {
            this.propertiesList = propertiesList;
        }
    }

    @SymbolicName("property")
    public static class Property extends AbstractNamedConfiguration
    {
        private String value;

        public Property()
        {
        }

        public Property(String name, String value)
        {
            super(name);
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }

    @SymbolicName("stage")
    public static class Stage extends AbstractNamedConfiguration
    {
        private Map<String, Property> properties = new HashMap<String, Property>();
        private String recipe;

        public Stage()
        {
        }

        public Stage(String name)
        {
            super(name);
        }

        public String getRecipe()
        {
            return recipe;
        }

        public void setRecipe(String recipe)
        {
            this.recipe = recipe;
        }

        public Map<String, Property> getProperties()
        {
            return properties;
        }

        public void setProperties(Map<String, Property> properties)
        {
            this.properties = properties;
        }

        public void addProperty(String name, String value)
        {
            properties.put(name, new Property(name, value));
        }
    }

    @SymbolicName("specialStage")
    public static class SpecialStage extends Stage
    {
        public SpecialStage()
        {
        }

        public SpecialStage(String name)
        {
            super(name);
        }
    }
}
