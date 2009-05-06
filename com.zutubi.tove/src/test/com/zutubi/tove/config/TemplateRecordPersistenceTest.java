package com.zutubi.tove.config;

import com.zutubi.events.Event;
import com.zutubi.tove.annotations.NoInherit;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.events.*;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;

import java.util.*;

/**
 * Tests that the records returned by the CTM are template records with
 * expected inheritance properties.
 */
public class TemplateRecordPersistenceTest extends AbstractConfigurationSystemTestCase
{
    private static final String GLOBAL_PROJECT = "global";
    private static final String GLOBAL_DESCRIPTION = "this is the daddy of them all";
    private static final String CHILD_PROJECT = "child";
    private static final String CHILD_DESCRIPTION = "my own way baby!";
    private static final String GRANDCHILD_PROJECT = "grandchild";
    private static final String GRANDCHILD_DESCRIPTION = "nkotb";

    private static final String PROPERTY_VALUE = "wow!";

    private CompositeType projectType;
    private CompositeType propertyType;
    private CompositeType stageType;

    protected void setUp() throws Exception
    {
        super.setUp();

        projectType = typeRegistry.register(Project.class);
        propertyType = typeRegistry.getType(Property.class);
        stageType = typeRegistry.getType(Stage.class);
        TemplatedMapType templatedMapType = new TemplatedMapType(projectType, typeRegistry);

        MapType mapType = new MapType(projectType, typeRegistry);

        configurationPersistenceManager.register("project", templatedMapType);
        configurationPersistenceManager.register("nproject", mapType);
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

    public void testInsertPathAlreadyInDescendent()
    {
        insertGlobal();
        MutableRecord child = createChild();
        MutableRecord childStages = (MutableRecord) child.get("stages");
        String stageName = "test";
        childStages.put(stageName, createStage(stageName));
        configurationTemplateManager.insertRecord("project", child);

        failedInsertHelper("project/global/stages", createStage(stageName), "Unable to insert record with name 'test' into path 'project/global/stages': a record with this name already exists in descendents [child]");
    }

    public void testInsertPathAlreadyHidden()
    {
        insertGlobal();
        insertChild();
        configurationTemplateManager.delete("project/child/stages/default");

        failedInsertHelper("project/child/stages", createStage("default"), "Unable to insert record with name 'default' into path 'project/child/stages': a record with this name already exists in ancestor 'global'");
    }

    public void testInsertPathHiddenInAncestor()
    {
        insertToGrandchild();
        configurationTemplateManager.delete("project/child/stages/default");

        failedInsertHelper("project/grandchild/stages", createStage("default"), "Unable to insert record with name 'default' into path 'project/grandchild/stages': a record with this name already exists in ancestor 'global'");
    }

    public void testInsertNestedPathHiddenInAncestor()
    {
        insertGlobal();
        insertTemplateChild();
        configurationTemplateManager.delete("project/child/stages/default");

        MutableRecord grandchild = createGrandchild();
        MutableRecord stages = (MutableRecord) grandchild.get("stages");
        stages.put("default", createStage("default"));
        
        failedInsertHelper("project", grandchild, "Cannot insert record: nested item 'stages/default' conflicts with hidden ancestor path 'project/global/stages/default'");
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

        MutableRecord overridingProperty = childProperty.flatten();
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

        MutableRecord overridingProperty = childProperty.flatten();
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

    public void testSaveOptimisedAway()
    {
        insertGlobal();
        insertChild();

        String path = "project/child";
        Project instance = configurationTemplateManager.getInstance(path, Project.class);
        Listener listener = registerListener();
        configurationTemplateManager.saveRecord(path, recordManager.select(path).copy(false));
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
        MutableRecord record = recordManager.select(path).copy(false);
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
        MutableRecord record = projectType.unstantiate(instance);
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
        MutableRecord record = child.flatten();
        
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
        MutableRecord record = template.flatten();
        record.put("name", "newname");
        configurationTemplateManager.saveRecord("project/global", record);

        assertNull(configurationTemplateManager.getRecord("project/global"));
        template = (TemplateRecord) configurationTemplateManager.getRecord("project/newname");
        assertEquals("newname", template.getOwner());
        assertEquals("newname", template.get("name"));
    }

    public void testRenameProjectLeavesDescendents()
    {
        insertGlobal();
        insertChild();

        TemplateRecord template = (TemplateRecord) configurationTemplateManager.getRecord("project/global");
        MutableRecord record = template.flatten();
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
        MutableRecord record = stageTemplate.flatten();
        record.put("name", "newname");
        String newPath = configurationTemplateManager.saveRecord("project/global/stages/default", record);
        assertEquals("project/global/stages/newname", newPath);

        stageTemplate = (TemplateRecord) configurationTemplateManager.getRecord(newPath);
        assertEquals(GLOBAL_PROJECT, stageTemplate.getOwner("name"));
        assertEquals("newname", stageTemplate.get("name"));

        assertNull(configurationTemplateManager.getRecord("project/child/stages/default"));

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

        MutableRecord property = configurationTemplateManager.getRecord(propertyPath).copy(true);
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
        String firstStagePath = PathUtils.getPath(stagesPath, newOrder.get(0));
        MutableRecord property = configurationTemplateManager.getRecord(firstStagePath).copy(true);
        property.put("name", "renamed");
        configurationTemplateManager.saveRecord(firstStagePath, property);

        // Verify order is unchanged
        Collections.replaceAll(newOrder, PathUtils.getBaseName(firstStagePath), "renamed");
        assertEquals(newOrder, getOrder(stagesPath));
    }

    public void testRenameOrderedInDescendent()
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
        String firstStagePath = PathUtils.getPath(stagesPath, newOrder.get(0));
        MutableRecord property = configurationTemplateManager.getRecord(firstStagePath).copy(true);
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

    public void testDeleteRemovesDescendents()
    {
        insertGlobal();
        insertChild();
        configurationTemplateManager.delete("project/global");
        assertNull(configurationTemplateManager.getRecord("project/global"));
        assertNull(configurationTemplateManager.getRecord("project/child"));
    }

    public void testDeleteCollectionItemRemovesDescendents()
    {
        insertGlobal();
        insertChild();
        configurationTemplateManager.delete("project/global/stages/default");
        assertNull(configurationTemplateManager.getRecord("project/global/stages/default"));
        assertNull(configurationTemplateManager.getRecord("project/child/stages/default"));
    }

    public void testInsertPerformance()
    {
        insertGlobal();
        long globalHandle = configurationTemplateManager.getRecord("project/global").getHandle();
        for(int i = 0; i < 10; i++)
        {
            insertLargeProject("project" + i, globalHandle, 5, 5);
        }
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

        clone = configurationTemplateManager.deepClone(child);
        clone.getPropertiesList().add(new Property("newer", "value"));
        configurationTemplateManager.save(clone);

        child = (Project) configurationTemplateManager.getInstance("project/child");
        property = child.getPropertiesList().get(0);
        assertNotNull(property);
        assertEquals("gp1", property.getName());
    }

    public void testHideInherited()
    {
        insertGlobal();
        insertChild();

        hideStageAndAssertEvents("project/child/stages/default");
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

        String stagePath = "project/child/stages/default";
        Stage stage = configurationTemplateManager.getInstance(stagePath, Stage.class);
        stage.setRecipe("over");
        configurationTemplateManager.save(stage);

        hideStageAndAssertEvents(stagePath);
    }

    public void testHideOverriddenNestedItem()
    {
        insertGlobal();
        insertChild();

        String propertyPath = "project/child/stages/default/properties/p1";
        Property property = configurationTemplateManager.getInstance(propertyPath, Property.class);
        property.setValue("over");
        configurationTemplateManager.save(property);

        hideStageAndAssertEvents("project/child/stages/default");
    }

    public void testHideOverriddenNewNestedItem()
    {
        insertGlobal();
        insertChild();

        String stagePath = "project/child/stages/default";
        Stage stage = configurationTemplateManager.getInstance(stagePath, Stage.class);
        stage.addProperty("new", "item");
        configurationTemplateManager.save(stage);

        Listener listener = registerListener();
        hideStage(stagePath);
        listener.assertEvents(new DeleteEventSpec(stagePath, false),
                              new DeleteEventSpec(stagePath + "/properties/p1", true),
                              new DeleteEventSpec(stagePath + "/properties/new", true),
                              new PostDeleteEventSpec(stagePath, false),
                              new PostDeleteEventSpec(stagePath + "/properties/p1", true),
                              new PostDeleteEventSpec(stagePath + "/properties/new", true));
    }

    public void testHideOverriddenAlreadyHiddenNestedItem()
    {
        insertGlobal();
        insertChild();

        String stagePath = "project/child/stages/default";
        configurationTemplateManager.delete(stagePath + "/properties/p1");

        Listener listener = registerListener();
        hideStage(stagePath);
        listener.assertEvents(new DeleteEventSpec(stagePath, false), new PostDeleteEventSpec(stagePath, false));
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

        hideStageAndAssertEvents("project/grandchild/stages/default");
    }

    public void testHideInheritedWithDescendent()
    {
        insertToGrandchild();

        String gcStagePath = "project/grandchild/stages/default";
        hideStageAndAssertEvents("project/child/stages/default", gcStagePath);
        assertDeletedStage(gcStagePath);
    }

    public void testHideDescendentOverridesSimpleProperty()
    {
        insertToGrandchild();

        String gcStagePath = "project/grandchild/stages/default";
        Stage stage = configurationTemplateManager.getInstance(gcStagePath, Stage.class);
        stage.setRecipe("over");
        configurationTemplateManager.save(stage);

        hideStageAndAssertEvents("project/child/stages/default", gcStagePath);
        assertDeletedStage(gcStagePath);
    }

    public void testHideDescendentOverridesNestedItem()
    {
        insertToGrandchild();

        String propertyPath = "project/grandchild/stages/default/properties/p1";
        Property property = configurationTemplateManager.getInstance(propertyPath, Property.class);
        property.setValue("over");
        configurationTemplateManager.save(property);

        hideStageAndAssertEvents("project/child/stages/default", "project/grandchild/stages/default");
    }

    public void testHideDescendentAddsNewNestedItem()
    {
        insertToGrandchild();

        String gcStagePath = "project/grandchild/stages/default";
        Stage stage = configurationTemplateManager.getInstance(gcStagePath, Stage.class);
        stage.addProperty("new", "item");
        configurationTemplateManager.save(stage);

        Listener listener = registerListener();
        hideStage("project/child/stages/default");
        listener.assertEvents(new DeleteEventSpec(gcStagePath, false),
                              new DeleteEventSpec(gcStagePath + "/properties/p1", true),
                              new DeleteEventSpec(gcStagePath + "/properties/new", true),
                              new PostDeleteEventSpec(gcStagePath, false),
                              new PostDeleteEventSpec(gcStagePath + "/properties/p1", true),
                              new PostDeleteEventSpec(gcStagePath + "/properties/new", true));
        assertDeletedStage(gcStagePath);
    }

    public void testHideDescendentHidesNestedItem()
    {
        insertToGrandchild();

        String gcStagePath = "project/grandchild/stages/default";
        configurationTemplateManager.delete(gcStagePath + "/properties/p1");

        Listener listener = registerListener();
        hideStage("project/child/stages/default");
        listener.assertEvents(new DeleteEventSpec(gcStagePath, false), new PostDeleteEventSpec(gcStagePath, false));
        assertDeletedStage(gcStagePath);
    }

    public void testHideDescendentAlreadyHidden()
    {
        insertToGrandchild();

        String gcStagePath = "project/grandchild/stages/default";
        configurationTemplateManager.delete(gcStagePath);

        Listener listener = registerListener();
        hideStage("project/child/stages/default");
        listener.assertEvents();
        assertDeletedStage(gcStagePath);
    }

    public void testHideInheritedWithIndirectDescendent()
    {
        insertGlobal();
        insertTemplateChild();
        insertTemplateGrandchild();

        MutableRecord greatGrandchild = createProject("greatgrandchild", "omg");
        configurationTemplateManager.setParentTemplate(greatGrandchild, configurationTemplateManager.getRecord("project/grandchild").getHandle());
        configurationTemplateManager.insertRecord("project", greatGrandchild);

        String ggcStagePath = "project/greatgrandchild/stages/default";
        hideStageAndAssertEvents("project/child/stages/default", ggcStagePath);
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
        failedRestoreHelper("project/child/stages/default", "Invalid path 'project/child/stages/default': not hidden");
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

        String path = "project/child/stages/default";
        configurationTemplateManager.delete(path);
        Listener listener = registerListener();
        configurationTemplateManager.restore(path);
        listener.assertEvents(new InsertEventSpec(path, false), new PostInsertEventSpec(path, false), new InsertEventSpec(path + "/properties/p1", true), new PostInsertEventSpec(path + "/properties/p1", true));
        assertStage(path);
        TemplateRecord stagesRecord = (TemplateRecord) configurationTemplateManager.getRecord(PathUtils.getParentPath(path));
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

        String path = "project/child/stages/default";
        configurationTemplateManager.delete(path);
        configurationTemplateManager.restore(path);
        hideStageAndAssertEvents(path);
    }

    public void testModifyAfterRestore()
    {
        insertGlobal();
        insertChild();

        String path = "project/child/stages/default";
        configurationTemplateManager.delete(path);
        configurationTemplateManager.restore(path);
        Stage stage = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(path, Stage.class));
        stage.setRecipe("edited");

        Listener listener = registerListener();
        configurationTemplateManager.save(stage);
        listener.assertEvents(new SaveEventSpec(path), new PostSaveEventSpec(path));

        assertEquals("edited", configurationTemplateManager.getInstance(path, Stage.class).getRecipe());
    }

    public void testInsertChildAfterRestore()
    {
        insertGlobal();
        insertChild();

        String path = "project/child/stages/default";
        configurationTemplateManager.delete(path);
        configurationTemplateManager.restore(path);
        Stage stage = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(path, Stage.class));
        stage.addProperty("new", "value");

        Listener listener = registerListener();
        configurationTemplateManager.save(stage);
        listener.assertEvents(new InsertEventSpec(path + "/properties/new", false), new PostInsertEventSpec(path + "/properties/new", false));

        assertEquals("value", configurationTemplateManager.getInstance(path, Stage.class).getProperties().get("new").getValue());
    }

    public void testRestoreWithDescendent()
    {
        insertToGrandchild();

        String path = "project/child/stages/default";
        configurationTemplateManager.delete(path);
        Listener listener = registerListener();
        configurationTemplateManager.restore(path);
        String gcStagePath = "project/grandchild/stages/default";
        listener.assertEvents(new InsertEventSpec(gcStagePath, false), new PostInsertEventSpec(gcStagePath, false), new InsertEventSpec(gcStagePath + "/properties/p1", true), new PostInsertEventSpec(gcStagePath + "/properties/p1", true));
        assertStage(gcStagePath);
    }

    public void testRestoreAfterHideWithDescendentAlreadyHidden()
    {
        insertToGrandchild();

        String gcStagePath = "project/grandchild/stages/default";
        configurationTemplateManager.delete(gcStagePath);
        String cStagePath = "project/child/stages/default";
        configurationTemplateManager.delete(cStagePath);

        Listener listener = registerListener();
        configurationTemplateManager.restore(cStagePath);
        listener.assertEvents(new InsertEventSpec(gcStagePath, false), new PostInsertEventSpec(gcStagePath, false), new PostInsertEventSpec(gcStagePath + "/properties/p1", true), new PostInsertEventSpec(gcStagePath + "/properties/p1", true));
        assertStage(gcStagePath);
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

        configurationTemplateManager.delete("project/child/stages/default");

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
        configurationTemplateManager.insertRecord("nproject", project);
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
        configurationTemplateManager.insertRecord("nproject", project);
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
        configurationTemplateManager.insertRecord("nproject", project);
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
        configurationTemplateManager.insertRecord("nproject", project);
        String stagesPath = "nproject/test/stages";
        insertStage(stagesPath, "one", 0);
        insertStage(stagesPath, "two", 0);
        insertStage(stagesPath, "three", 0);

        configurationTemplateManager.setOrder(stagesPath, Arrays.asList("three", "two", "one"));
        assertEquals(Arrays.asList("three", "two", "one"), getOrder(stagesPath));
        configurationTemplateManager.delete(PathUtils.getPath(stagesPath, "three"));
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

    public void testDeleteItemInDescendentOrder()
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
        configurationTemplateManager.delete(PathUtils.getPath(parentStagesPath, "default"));
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
        String hidePath = PathUtils.getPath(childStagesPath, "default");
        configurationTemplateManager.delete(hidePath);
        assertEquals(Arrays.asList("default2", "childs", "default3"), getOrder(childStagesPath));

        // Restore it and it should now appear at the end
        configurationTemplateManager.restore(hidePath);
        assertEquals(Arrays.asList("default2", "childs", "default3", "default"), getOrder(childStagesPath));
    }

    public void testHideItemInDescendentOrder()
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
            return CollectionUtils.<Configuration, String>map(instance, new Mapping<Configuration, String>()
            {
                public String map(Configuration o)
                {
                    return PathUtils.getBaseName(o.getConfigurationPath());
                }
            });
        }
        else
        {
            fail();
            return null;
        }
    }

    private Listener registerListener()
    {
        Listener listener = new Listener();
        eventManager.register(listener);
        return listener;
    }

    private String insertGlobal()
    {
        MutableRecord global = createGlobal();
        return configurationTemplateManager.insertRecord("project", global);
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
        return configurationTemplateManager.insertRecord("project", child);
    }

    private void insertTemplateChild()
    {
        MutableRecord child = createChild();
        configurationTemplateManager.markAsTemplate(child);
        configurationTemplateManager.insertRecord("project", child);
    }

    private MutableRecord createChild()
    {
        MutableRecord child = createProject(CHILD_PROJECT, CHILD_DESCRIPTION);
        configurationTemplateManager.setParentTemplate(child, configurationTemplateManager.getRecord("project/global").getHandle());
        return child;
    }

    private void insertGrandchild()
    {
        MutableRecord grandchild = createGrandchild();
        configurationTemplateManager.insertRecord("project", grandchild);
    }

    private void insertTemplateGrandchild()
    {
        MutableRecord grandchild = createGrandchild();
        configurationTemplateManager.markAsTemplate(grandchild);
        configurationTemplateManager.insertRecord("project", grandchild);
    }

    private MutableRecord createGrandchild()
    {
        MutableRecord child = createProject(GRANDCHILD_PROJECT, GRANDCHILD_DESCRIPTION);
        configurationTemplateManager.setParentTemplate(child, configurationTemplateManager.getRecord("project/child").getHandle());
        return child;
    }

    private void insertLargeProject(String name, long parent, int stages, int propertiesPerStage)
    {
        MutableRecord project = createProject(name, "fake");
        configurationTemplateManager.setParentTemplate(project, parent);
        configurationTemplateManager.insertRecord("project", project);
        String stagePath = PathUtils.getPath("project", name, "stages");
        for(int i = 0; i < stages; i++)
        {
            insertStage(stagePath, "stage " + i, propertiesPerStage);
        }
    }

    private void insertStage(String path, String name, int properties)
    {
        MutableRecord stage = stageType.createNewRecord(false);
        stage.put("name", name);
        configurationTemplateManager.insertRecord(path, stage);
        String propertiesPath = PathUtils.getPath(path, name, "properties");
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

    public static class Listener implements com.zutubi.events.EventListener
    {
        private List<ConfigurationEvent> events = new LinkedList<ConfigurationEvent>();

        public List<ConfigurationEvent> getEvents()
        {
            return events;
        }

        public void clearEvents()
        {
            events.clear();
        }

        public void assertEvents(EventSpec... expectedEvents)
        {
            assertEquals(expectedEvents.length, events.size());
            for(final EventSpec spec: expectedEvents)
            {
                ConfigurationEvent matchingEvent = CollectionUtils.find(events, new Predicate<ConfigurationEvent>()
                {
                    public boolean satisfied(ConfigurationEvent event)
                    {
                        return spec.matches(event);
                    }
                });

                assertNotNull("Expected event '" + spec.toString() + "' missing", matchingEvent);
            }
        }

        public void handleEvent(Event evt)
        {
            events.add((ConfigurationEvent) evt);
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{ConfigurationEvent.class};
        }
    }

    public static abstract class EventSpec
    {
        private String path;
        private boolean cascaded;
        private Class<? extends ConfigurationEvent> eventClass;

        protected EventSpec(String path, Class<? extends ConfigurationEvent> eventClass)
        {
            this(path, false, eventClass);
        }

        protected EventSpec(String path, boolean cascaded, Class<? extends ConfigurationEvent> eventClass)
        {
            this.path = path;
            this.cascaded = cascaded;
            this.eventClass = eventClass;
        }

        public boolean matches(ConfigurationEvent event)
        {
            if(event instanceof CascadableEvent)
            {
                if(cascaded != ((CascadableEvent)event).isCascaded())
                {
                    return false;
                }
            }

            return eventClass.isInstance(event) && event.getInstance().getConfigurationPath().equals(path);
        }

        public String toString()
        {
            return eventClass.getSimpleName() + ": " + path;
        }
    }

    public static class InsertEventSpec extends EventSpec
    {
        public InsertEventSpec(String path, boolean cascaded)
        {
            super(path, cascaded, InsertEvent.class);
        }
    }

    public static class DeleteEventSpec extends EventSpec
    {
        public DeleteEventSpec(String path, boolean cascaded)
        {
            super(path, cascaded, DeleteEvent.class);
        }
    }

    public static class SaveEventSpec extends EventSpec
    {
        public SaveEventSpec(String path)
        {
            super(path, SaveEvent.class);
        }
    }

    public static class PostInsertEventSpec extends EventSpec
    {
        public PostInsertEventSpec(String path, boolean cascaded)
        {
            super(path, cascaded, PostInsertEvent.class);
        }
    }

    public static class PostDeleteEventSpec extends EventSpec
    {
        public PostDeleteEventSpec(String path, boolean cascaded)
        {
            super(path, cascaded, PostDeleteEvent.class);
        }
    }

    public static class PostSaveEventSpec extends EventSpec
    {
        public PostSaveEventSpec(String path)
        {
            super(path, PostSaveEvent.class);
        }
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
}
