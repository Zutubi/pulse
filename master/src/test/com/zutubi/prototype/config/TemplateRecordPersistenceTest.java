package com.zutubi.prototype.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TemplatedMapType;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Tests that the records returned by the CTM are template records with
 * expected inheritance properties.
 */
public class TemplateRecordPersistenceTest extends AbstractConfigurationSystemTestCase
{
    private CompositeType projectType;
    private CompositeType propertyType;
    private CompositeType stageType;
    private static final String GLOBAL_PROJECT = "global";

    protected void setUp() throws Exception
    {
        super.setUp();

        projectType = typeRegistry.register(Project.class);
        propertyType = typeRegistry.getType(Property.class);
        stageType = typeRegistry.getType(Stage.class);
        TemplatedMapType top = new TemplatedMapType(configurationTemplateManager);
        top.setTypeRegistry(typeRegistry);
        top.setCollectionType(projectType);

        configurationPersistenceManager.register("project", top);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSingleRecord()
    {
        MutableRecord global = createGlobal();
        configurationTemplateManager.insertRecord("project", global);

        TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord("project/global");
        assertEquals(GLOBAL_PROJECT, record.getOwner());
        assertNull(record.getParent());

        assertEquals(GLOBAL_PROJECT, record.get("name"));
        assertEquals(GLOBAL_PROJECT, record.getOwner("name"));

        assertEquals("this is the daddy of them all", record.get("description"));
        assertEquals(GLOBAL_PROJECT, record.getOwner("description"));

        assertNull(record.get("url"));
        assertNull(record.getOwner("url"));

        assertNull(record.get("property"));
        assertNull(record.getOwner("property"));

        assertEmptyCollection(record, "properties");

        assertDefaultStages((TemplateRecord) record.get("stages"));

        assertEmptyCollection(record, "propertiesList");

        String [] cool = (String[]) record.get("coolnesses");
        assertEquals(GLOBAL_PROJECT, record.getOwner("coolnesses"));
        assertEquals(0, cool.length);
    }

    public void testComposite()
    {
        MutableRecord global = createGlobal();
        global.put("property", createProperty("p", "v"));
        configurationTemplateManager.insertRecord("project", global);

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
        MutableRecord global = createGlobal();
        addNamed((MutableRecord) global.get("stages"), createStage("stagename", "p1", "v1"));
        configurationTemplateManager.insertRecord("project", global);

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
        MutableRecord child = createProject("child", "my own way baby!");
        configurationTemplateManager.setParentTemplate(child, configurationTemplateManager.getRecord("project/global").getHandle());
        configurationTemplateManager.insertRecord("project", child);

        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        TemplateRecord parent = childTemplate.getParent();
        assertEquals(GLOBAL_PROJECT, parent.getOwner());
        assertEquals(GLOBAL_PROJECT, parent.get("name"));
        assertEquals(GLOBAL_PROJECT, parent.getOwner("name"));
        assertNull(parent.getParent());

        assertEquals("child", childTemplate.getOwner());
        assertEquals("child", childTemplate.get("name"));
        assertEquals("inherited url", childTemplate.get("url"));
        assertEquals(GLOBAL_PROJECT, childTemplate.getOwner("url"));
    }

    public void testSimpleOverride()
    {
        MutableRecord global = createGlobal();
        configurationTemplateManager.insertRecord("project", global);
        MutableRecord child = createProject("child", "my own way baby!");
        child.put("url", "override url");
        configurationTemplateManager.setParentTemplate(child, configurationTemplateManager.getRecord("project/global").getHandle());
        configurationTemplateManager.insertRecord("project", child);

        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals("child", childTemplate.getOwner());
        assertEquals("child", childTemplate.get("name"));
        assertEquals("override url", childTemplate.get("url"));
        assertEquals("child", childTemplate.getOwner("url"));
    }

    public void testInheritedMap()
    {
        MutableRecord global = createGlobal();
        configurationTemplateManager.insertRecord("project", global);
        MutableRecord child = createProject("child", "my own way baby!");
        configurationTemplateManager.setParentTemplate(child, configurationTemplateManager.getRecord("project/global").getHandle());
        configurationTemplateManager.insertRecord("project", child);

        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals("child", childTemplate.getOwner());
        assertEquals(GLOBAL_PROJECT, childTemplate.getOwner("stages"));

        assertDefaultStages((TemplateRecord) childTemplate.get("stages"));
        assertDefaultStages((TemplateRecord) configurationTemplateManager.getRecord("project/child/stages"));
    }

    public void testInheritedList()
    {
        MutableRecord global = createGlobal();
        configurationTemplateManager.insertRecord("project", global);
        MutableRecord child = createProject("child", "my own way baby!");
        configurationTemplateManager.setParentTemplate(child, configurationTemplateManager.getRecord("project/global").getHandle());
        configurationTemplateManager.insertRecord("project", child);

        TemplateRecord childTemplate = (TemplateRecord) configurationTemplateManager.getRecord("project/child");
        assertEquals("child", childTemplate.getOwner());
        assertEquals(GLOBAL_PROJECT, childTemplate.getOwner("stages"));

    }

    private MutableRecord createGlobal()
    {
        return createProject(GLOBAL_PROJECT, "this is the daddy of them all");
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

    private MutableRecord createStage(String name, String... properties)
    {
        MutableRecord record = stageType.createNewRecord(true);
        record.put("name", name);
        MutableRecord propertiesRecord = (MutableRecord) record.get("properties");
        for(int i = 0; i < properties.length - 1; i += 2)
        {
            addNamed(propertiesRecord, createProperty(properties[i], properties[i + 1]));
        }

        return record;
    }

    private void addNamed(MutableRecord record, MutableRecord named)
    {
        record.put((String) named.get("name"), named);
    }

    private void assertEmptyCollection(TemplateRecord record, String property)
    {
        TemplateRecord collection = (TemplateRecord) record.get(property);
        assertEquals(record.getOwner(), record.getOwner(property));
        assertEquals(0, collection.size());
    }

    private void assertDefaultStages(TemplateRecord stages)
    {
        assertEquals(1, stages.size());
        assertEquals(GLOBAL_PROJECT, stages.getOwner());
        assertEquals(GLOBAL_PROJECT, stages.getOwner("default"));

        TemplateRecord stage = (TemplateRecord) stages.get("default");
        assertEquals(GLOBAL_PROJECT, stage.getOwner());
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
        private String description;
        private String url;
        private Property property;
        private Map<String, Property> properties = new HashMap<String, Property>();
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

        public Stage()
        {
        }

        public Stage(String name)
        {
            super(name);
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
