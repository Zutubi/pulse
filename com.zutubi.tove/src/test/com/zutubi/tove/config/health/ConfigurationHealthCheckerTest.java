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

package com.zutubi.tove.config.health;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.annotations.ExternalState;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.ReferenceType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.*;

public class ConfigurationHealthCheckerTest extends AbstractConfigurationSystemTestCase
{
    private static final Messages I18N = Messages.getInstance(ConfigurationHealthChecker.class);
    
    private static final String SCOPE_NORMAL    = "normal";
    private static final String SCOPE_TEMPLATED = "templated";
    
    private ConfigurationHealthChecker configurationHealthChecker;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        disableHealthCheckOnTeardown();

        CompositeType projectType = typeRegistry.register(Project.class);
        typeRegistry.register(Subversion.class);
        typeRegistry.register(Perforce.class);
        configurationPersistenceManager.register(SCOPE_NORMAL, new MapType(projectType, typeRegistry));
        configurationPersistenceManager.register(SCOPE_TEMPLATED, new TemplatedMapType(projectType, typeRegistry));
        
        configurationHealthChecker = objectFactory.buildBean(ConfigurationHealthChecker.class);
    }
    
    public void testCheckAllEmpty()
    {
        checkAllTest();
    }
    
    public void testCheckAllValidData()
    {
        Project normalProject = new Project("i am normal");
        normalProject.addLabel(new Label("groupy"));
        normalProject.addStage(new Stage("default"));
        normalProject.addHook(new Hook("a hook"));
        configurationTemplateManager.insertInstance(SCOPE_NORMAL, normalProject);

        Project global = new Project("global template");
        global.addStage(new Stage("default"));
        String globalPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, global, null, true);

        Project child = createChildProject(globalPath, "child");
        child.addStage(new Stage("another stage"));
        configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, child, globalPath, false);
        
        checkAllTest();
    }
    
    public void testCheckAllRootContainsSimpleKey()
    {
        Record rootRecord = recordManager.select();
        MutableRecord mutable = rootRecord.copy(false, true);
        mutable.put("simple", "value");
        recordManager.update("", mutable);

        checkAllTest(new UnexpectedSimpleValueProblem("", I18N.format("root.simple.key", "simple"), "simple"));
    }
    
    public void testCheckAllRootContainsUnknownScope()
    {
        recordManager.insert("unknown", new MutableRecordImpl());

        checkAllTest(new UnexpectedNestedRecordProblem("", I18N.format("root.unexpected.scope", "unknown"), "unknown"));
    }

    public void testCheckAllCollectionWithSymbolicName()
    {
        String projectPath = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("pro"));
        String stagesPath = getPath(projectPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        stagesRecord.setSymbolicName("atype");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new UnsolvableHealthProblem(stagesPath, I18N.format("type.mismatch.expected.collection", "atype")));
    }

    public void testCheckAllCollectionWithSimpleKey()
    {
        String projectPath = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("pro"));
        String stagesPath = getPath(projectPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        stagesRecord.put("simple", "value");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new UnexpectedSimpleValueProblem(stagesPath, I18N.format("unexpected.simple.key", "simple"), "simple"));
    }

    public void testCheckAllCollectionWithBadOrder()
    {
        String projectPath = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("pro"));
        String stagesPath = getPath(projectPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        MapType.setOrder(stagesRecord, asList("bad"));
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new InvalidOrderKeyProblem(stagesPath, I18N.format("order.key.invalid", "bad"), "bad"));
    }
    
    public void testCheckAllCollectionWithOrderReferringToSimple()
    {
        String projectPath = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("pro"));
        String stagesPath = getPath(projectPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        MapType.setOrder(stagesRecord, asList("simple"));
        stagesRecord.put("simple", "value");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new UnexpectedSimpleValueProblem(stagesPath, I18N.format("unexpected.simple.key", "simple"), "simple"),
                new InvalidOrderKeyProblem(stagesPath, I18N.format("order.key.refers.to.simple", "simple"), "simple"));
    }
    
    public void testCheckAllInheritedOrderRefersToHiddenItem()
    {
        String rootPath = insertRootProjectWithStages("s1", "s2");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, createChildProject(rootPath, "child"), rootPath, false);
        
        configurationTemplateManager.setOrder(getPath(rootPath, "stages"), asList("s2", "s1"));
        configurationTemplateManager.delete(getPath(childPath, "stages", "s2"));

        checkAllTest();
    }

    public void testCheckAllRootCollectionWithHiddenKeys()
    {
        String rootPath = insertRootProjectWithStages("s1", "s2");

        String stagesPath = getPath(rootPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        TemplateRecord.hideItem(stagesRecord, "invalid");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new UnexpectedHiddenKeysProblem(stagesPath, I18N.format("hidden.keys.unexpected", asList("invalid"))));
    }

    public void testCheckAllCollectionHiddenItemExists()
    {
        String rootPath = insertRootProjectWithStages("s1", "s2");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, createChildProject(rootPath, "child"), rootPath, false);

        String stagesPath = getPath(childPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        TemplateRecord.hideItem(stagesRecord, "s1");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new InvalidHiddenKeyProblem(stagesPath, I18N.format("hidden.key.exists", "s1"), "s1"));
    }
    
    public void testCheckAllCollectionHiddenItemNotInParent()
    {
        String rootPath = insertRootProjectWithStages("s1", "s2");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, createChildProject(rootPath, "child"), rootPath, false);

        String stagesPath = getPath(childPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        TemplateRecord.hideItem(stagesRecord, "invalid");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new InvalidHiddenKeyProblem(stagesPath, I18N.format("hidden.key.not.in.parent", "invalid"), "invalid"));
    }

    public void testCheckAllCollectionHiddenItemRefersToSimple()
    {
        String rootPath = insertRootProjectWithStages("s1", "s2");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, createChildProject(rootPath, "child"), rootPath, false);

        String rootStagesPath = getPath(rootPath, "stages");
        MutableRecord rootStagesRecord = recordManager.select(rootStagesPath).copy(false, true);
        rootStagesRecord.put("simple", "value");
        recordManager.update(rootStagesPath, rootStagesRecord);
        
        String childStagesPath = getPath(childPath, "stages");
        MutableRecord childStagesRecord = recordManager.select(childStagesPath).copy(false, true);
        TemplateRecord.hideItem(childStagesRecord, "simple");
        recordManager.update(childStagesPath, childStagesRecord);

        checkAllTest(new InvalidHiddenKeyProblem(childStagesPath, I18N.format("hidden.key.refers.to.simple", "simple"), "simple"),
                new UnexpectedSimpleValueProblem(rootStagesPath, I18N.format("unexpected.simple.key", "simple"), "simple"));
    }
    
    public void testCheckAllCollectionItemTypeInvalid()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.setSymbolicName("invalid");
        String path = getPath(SCOPE_NORMAL, "item");
        recordManager.insert(path, record);
        
        checkAllTest(new UnsolvableHealthProblem(path, I18N.format("type.invalid", "invalid")));
    }

    public void testCheckAllCollectionItemTypeMismatch()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.setSymbolicName("stage");
        String path = getPath(SCOPE_NORMAL, "item");
        recordManager.insert(path, record);
        
        checkAllTest(new UnsolvableHealthProblem(path, I18N.format("type.mismatch.composites", "project", "stage")));
    }

    public void testCheckAllCompositeUnrecognisedSimpleProperty()
    {
        String path = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("p"));
        MutableRecord record = configurationTemplateManager.getRecord(path).copy(false, true);
        record.put("unknown", "value");
        recordManager.update(path, record);
        
        checkAllTest(new UnexpectedSimpleValueProblem(path, I18N.format("unexpected.simple.key", "unknown"), "unknown"));
    }
    
    public void testCheckAllCompositeUnrecognisedComplexProperty()
    {
        String path = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("p"));
        recordManager.insert(getPath(path, "unknown"), new MutableRecordImpl());
        
        checkAllTest(new UnexpectedNestedRecordProblem(path, I18N.format("unexpected.nested.record", "unknown"), "unknown"));
    }
    
    public void testCheckAllCompositeCompositePropertySimple()
    {
        String path = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("p"));
        MutableRecord record = configurationTemplateManager.getRecord(path).copy(false, true);
        record.put("options", "value");
        recordManager.update(path, record);
        
        checkAllTest(new UnsolvableHealthProblem(path, I18N.format("complex.property.simple.value", "options")));
    }
    
    public void testCheckAllCompositeCollectionPropertySimple()
    {
        String path = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("p"));
        MutableRecord record = configurationTemplateManager.getRecord(path).copy(false, true);
        record.put("stages", "value");
        recordManager.update(path, record);
        
        checkAllTest(new UnsolvableHealthProblem(path, I18N.format("complex.property.simple.value", "stages")));
    }
    
    public void testCheckAllCompositeCollectionPropertyMissing()
    {
        String rootPath = insertRootProjectWithStages();
        String stagesPath = getPath(rootPath, "stages");
        recordManager.delete(stagesPath);
        
        checkAllTest(new MissingCollectionProblem(rootPath, I18N.format("collection.missing", "stages"), "stages"));
    }
    
    public void testCheckAllCompositeSimplePropertyCollection()
    {
        String path = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("p"));
        recordManager.insert(getPath(path, "description"), new MutableRecordImpl());
        
        checkAllTest(new UnsolvableHealthProblem(path, I18N.format("simple.property.complex.value", "description")));
    }
    
    public void testCheckAllReferenceHandleInvalid()
    {
        Project project = new Project("p");
        project.addHook(new Hook("h"));
        String projectPath = configurationTemplateManager.insertInstance(SCOPE_NORMAL, project);
        String hookPath = getPath(projectPath, "hooks", "h");
        MutableRecord hookRecord = recordManager.select(hookPath).copy(false, true);
        hookRecord.put("stage", "invalid");
        recordManager.update(hookPath, hookRecord);
        
        checkAllTest(new InvalidReferenceProblem(hookPath, I18N.format("reference.handle.invalid", "stage"), "stage", "invalid"));
    }

    public void testCheckAllReferenceHandleUnknown()
    {
        Project project = new Project("p");
        project.addHook(new Hook("h"));
        String projectPath = configurationTemplateManager.insertInstance(SCOPE_NORMAL, project);
        String hookPath = getPath(projectPath, "hooks", "h");
        MutableRecord hookRecord = recordManager.select(hookPath).copy(false, true);
        hookRecord.put("stage", "388765");
        recordManager.update(hookPath, hookRecord);
        
        checkAllTest(new InvalidReferenceProblem(hookPath, I18N.format("reference.handle.unknown", "stage"), "stage", "388765"));
    }

    public void testCheckAllCollectionReferenceHandleNull()
    {
        Artifact artifact = new Artifact("a");
        Command command = new Command("c");
        command.addArtifact(artifact);
        Recipe recipe = new Recipe("r");
        recipe.addCommand(command);
        Project project = new Project("p");
        project.addRecipe(recipe);
        String projectPath = configurationTemplateManager.insertInstance(SCOPE_NORMAL, project);
        String artifactPath = getPath(projectPath, "recipes", "r", "commands", "c", "artifacts", "a");
        MutableRecord artifactRecord = recordManager.select(artifactPath).copy(false, true);
        artifactRecord.put("processors", new String[]{ReferenceType.NULL_REFERENCE});
        recordManager.update(artifactPath, artifactRecord);

        checkAllTest(new NullReferenceInCollectionProblem(artifactPath, I18N.format("collection.reference.handle.null", "processors"), "processors"));
    }

    public void testCheckAllReferenceHandleInvalidInChild()
    {
        String rootPath = insertRootProjectWithStages("default");
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);
        configurationTemplateManager.delete(getPath(childPath, "stages", "default"));

        Project rootProject = configurationTemplateManager.getInstance(rootPath, Project.class);
        rootProject.addHook(new Hook("h", rootProject.getStages().get("default")));
        configurationTemplateManager.save(rootProject);

        checkAllTest(new UnsolvableHealthProblem(getPath(childPath, "hooks", "h"), I18N.format("reference.cannot.push.down", "stage")));
    }

    public void testCheckAllReferenceHandleNotPulledUp()
    {
        String rootPath = insertRootProjectWithStages("default");
        Project rootProject = configurationTemplateManager.getInstance(rootPath, Project.class);
        rootProject.addHook(new Hook("h", rootProject.getStages().get("default")));
        configurationTemplateManager.save(rootProject);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        String childHookPath = getPath(childPath, "hooks", "h");
        long nonCanonicalHandle = recordManager.select(getPath(childPath, "stages", "default")).getHandle();
        MutableRecord childHookRecord = recordManager.select(childHookPath).copy(false, true);
        String nonCanonicalHandleString = Long.toString(nonCanonicalHandle);
        childHookRecord.put("stage", nonCanonicalHandleString);
        recordManager.update(childHookPath, childHookRecord);
        
        long canonicalHandle = recordManager.select(getPath(rootPath, "stages", "default")).getHandle();
        checkAllTest(new NonCanonicalReferenceProblem(childHookPath, I18N.format("reference.not.canonical", "stage"), "stage", nonCanonicalHandleString, Long.toString(canonicalHandle)));
    }
    
    public void testCheckAllInheritanceTypeMismatch()
    {
        Project rootProject = new Project("root");
        rootProject.setScm(new Perforce());
        String rootPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        MutableRecordImpl childScmRecord = new MutableRecordImpl();
        childScmRecord.setSymbolicName("subversion");
        String childScmPath = getPath(childPath, "scm");
        recordManager.delete(childScmPath);
        recordManager.insert(childScmPath, childScmRecord);
        
        checkAllTest(new IncompatibleOverrideOfComplexProblem(childPath, I18N.format("inherited.type.mismatch", "subversion", "perforce"), "scm", rootPath));
    }
    
    public void testCheckAllSimpleOverridesRecord()
    {
        Project rootProject = new Project("root");
        rootProject.setScm(new Perforce());
        String rootPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        recordManager.delete(getPath(childPath, "scm"));
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.put("scm", "value");
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new SimpleOverrideOfComplexProblem(childPath, I18N.format("inherited.record.simple.in.child", "scm"), "scm", rootPath));
    }

    public void testCheckAllMissingSkeleton()
    {
        Project rootProject = new Project("root");
        rootProject.setScm(new Perforce());
        String rootPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        recordManager.delete(getPath(childPath, "scm"));
        
        checkAllTest(new MissingSkeletonsProblem(childPath, I18N.format("inherited.missing.skeletons", "scm"), "scm", rootPath));
    }

    public void testCheckAllInheritedSimpleValueNotScrubbed()
    {
        Project rootProject = new Project("root");
        rootProject.setDescription("mundane");
        String rootPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.put("description", "mundane");
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new NonScrubbedSimpleValueProblem(childPath, I18N.format("simple.value.not.scrubbed", "description"), "description", "mundane"));
    }

    public void testCheckAllEmptySimpleValueNotInParentNotMarkedForScrub()
    {
        Project rootProject = new Project("root");
        String rootPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, rootProject, null, true);

        Project childProject = createChildProject(rootPath, "child");
        Perforce scm = new Perforce();
        scm.setClient("");
        childProject.setScm(scm);
        configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        checkAllTest();
    }

    public void testCheckAllExternalStateInTemplate()
    {
        final String TRIGGER_NAME = "pooh";
        
        Project rootProject = new Project("root");
        rootProject.addTrigger(new Trigger(TRIGGER_NAME));
        String rootPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, rootProject, null, true);
        
        // Also add a concrete child to make sure its trigger id is fine.
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        String rootTriggerPath = addExternalStateToTrigger(rootPath, TRIGGER_NAME);
        addExternalStateToTrigger(childPath, TRIGGER_NAME);
        
        checkAllTest(new ExternalStateInTemplateProblem(rootTriggerPath, I18N.format("external.state.in.template", "triggerId"), "triggerId"));
    }

    private String addExternalStateToTrigger(String projectPath, String triggerName)
    {
        String triggerPath = getPath(projectPath, "triggers", triggerName);
        MutableRecord triggerRecord = recordManager.select(triggerPath).copy(false, true);
        triggerRecord.put("triggerId", "123");
        recordManager.update(triggerPath, triggerRecord);
        return triggerPath;
    }

    public void testCheckAllTemplateParentInvalid()
    {
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);
        
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.putMeta(TemplateRecord.PARENT_KEY, "invalid");
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new UnsolvableHealthProblem(childPath, I18N.format("parent.handle.illegal", "invalid")));
    }
    
    public void testCheckAllTemplateParentUnknown()
    {
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);
        
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.putMeta(TemplateRecord.PARENT_KEY, "33115599");
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new UnsolvableHealthProblem(childPath, I18N.format("parent.handle.unknown", 33115599L)));
    }

    public void testCheckAllTemplateParentNotItemOfTemplateCollection()
    {
        String normalPath = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("norm"));
        
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);
        
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        long normalHandle = recordManager.select(normalPath).getHandle();
        childRecord.putMeta(TemplateRecord.PARENT_KEY, Long.toString(normalHandle));
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new UnsolvableHealthProblem(childPath, I18N.format("parent.not.in.collection", "normal/norm")));
    }

    public void testCheckAllTemplateParentNotTemplate()
    {
        String rootPath = insertRootProjectWithStages();
        Project child1Project = createChildProject(rootPath, "child1");
        Project child2Project = createChildProject(rootPath, "child2");
        String child1Path = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, child1Project, rootPath, false);
        String child2Path = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, child2Project, rootPath, false);
        
        MutableRecord child1Record = recordManager.select(child1Path).copy(false, true);
        long child2Handle = recordManager.select(child2Path).getHandle();
        child1Record.putMeta(TemplateRecord.PARENT_KEY, Long.toString(child2Handle));
        recordManager.update(child1Path, child1Record);
        
        checkAllTest(new UnsolvableHealthProblem(child1Path, I18N.format("parent.not.template", "templated/child2")));
    }
    
    public void testCheckAllScopeHasMultipleRoots()
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName("project");
        configurationTemplateManager.markAsTemplate(record);
        recordManager.insert(getPath(SCOPE_TEMPLATED, "1"), record);
        recordManager.insert(getPath(SCOPE_TEMPLATED, "2"), record);
        
        checkAllTest(new UnsolvableHealthProblem(SCOPE_TEMPLATED, I18N.format("scope.multiple.roots", "1", "2")));
    }
    
    public void testCheckAllScopeHasRootNotMarkedAsTemplate()
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName("project");
        String path = getPath(SCOPE_TEMPLATED, "root");
        recordManager.insert(path, record);
        
        checkAllTest(new UnsolvableHealthProblem(path, I18N.format("scope.root.not.template", "root")));
    }
    
    public void testCheckPathEmpty()
    {
        breakBothScopes();
        assertEquals(configurationHealthChecker.checkAll(), configurationHealthChecker.checkPath(""));
    }

    public void testCheckPathScope()
    {
        breakScope(SCOPE_NORMAL);
        checkPathTest(SCOPE_NORMAL, new UnexpectedSimpleValueProblem(SCOPE_NORMAL, I18N.format("unexpected.simple.key", "simple"), "simple"));
        checkPathTest(SCOPE_TEMPLATED);
    }

    public void testCheckPathTemplatedCollectionItem()
    {
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        checkPathTest(childPath);
        
        // Now break the parent pointer and ensure it is detected.
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.putMeta(TemplateRecord.PARENT_KEY, "invalid");
        recordManager.update(childPath, childRecord);

        checkPathTest(childPath, new UnsolvableHealthProblem(childPath, I18N.format("parent.handle.illegal", "invalid")));
    }

    public void testCheckPathTemplatedCollectionItemTypeMismatch()
    {
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);
        
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.setSymbolicName("stage");
        recordManager.update(childPath, childRecord);

        checkPathTest(childPath, new UnsolvableHealthProblem(childPath, I18N.format("templated.item.type.mismatch", "project", "stage")));
    }
    
    public void testCheckPathWithinTemplatedCollectionItem()
    {
        Project rootProject = new Project("root");
        rootProject.setScm(new Perforce());
        String rootPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, childProject, rootPath, false);

        checkPathTest(childPath);

        // Now mess up the structure and ensure it is detected.
        recordManager.delete(getPath(childPath, "scm"));

        checkPathTest(childPath, new MissingSkeletonsProblem(childPath, I18N.format("inherited.missing.skeletons", "scm"), "scm", rootPath));
    }

    public void testCheckPathNormalPath()
    {
        String path = configurationTemplateManager.insertInstance(SCOPE_NORMAL, new Project("p"));

        checkPathTest(path);

        // Now mess up a proeprty and ensure it is detected.
        MutableRecord record = configurationTemplateManager.getRecord(path).copy(false, true);
        record.put("stages", "value");
        recordManager.update(path, record);

        checkPathTest(path, new UnsolvableHealthProblem(path, I18N.format("complex.property.simple.value", "stages")));
    }
    
    public void testCheckPathPathInvalid()
    {
        try
        {
            configurationHealthChecker.checkPath("invalid");
            fail("Should not be able to check invalid path");
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage(), containsString("Path 'invalid' does not exist"));
        }
    }
    
    public void testHealAllDeleteInvalidReferenceThenScrub()
    {
        // A healing process that we know takes multiple steps.  A reference is
        // zeroed out, then the zero needs scrubbing.
        Project rootProject = new Project("root");
        rootProject.addHook(new Hook("h"));
        String rootPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, rootProject, null, true);

        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, createChildProject(rootPath, "child"), rootPath, false);
        String hookPath = getPath(childPath, "hooks", "h");
        MutableRecord hookRecord = recordManager.select(hookPath).copy(false, true);
        hookRecord.put("stage", "invalid");
        recordManager.update(hookPath, hookRecord);
        
        checkAllTest(new InvalidReferenceProblem(hookPath, I18N.format("reference.handle.invalid", "stage"), "stage", "invalid"));
    }
    
    private void breakBothScopes()
    {
        breakScope(SCOPE_NORMAL);
        breakScope(SCOPE_TEMPLATED);
    }

    private void breakScope(String scope)
    {
        MutableRecord normalRecord = recordManager.select(scope).copy(false, true);
        normalRecord.put("simple", "value");
        recordManager.update(scope, normalRecord);
    }

    private String insertRootProjectWithStages(String... stages)
    {
        Project root = new Project("root");
        for (String stage: stages)
        {
            root.addStage(new Stage(stage));
        }
        return configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, root, null, true);
    }

    private Project createChildProject(String parentPath, String name)
    {
        Project child = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(parentPath, Project.class));
        child.setName(name);
        child.setConcrete(true);
        child.putMeta(TemplateRecord.TEMPLATE_KEY, "");
        child.putMeta(TemplateRecord.PARENT_KEY, "");
        return child;
    }

    private void checkAllTest(HealthProblem... expectedProblems)
    {
        ConfigurationHealthReport expectedReport = new ConfigurationHealthReport(expectedProblems);
        ConfigurationHealthReport gotReport = configurationHealthChecker.checkAll();
        assertEquals(expectedReport, gotReport);

        ConfigurationHealthReport healedReport = configurationHealthChecker.healAll();
        assertEquals(expectedReport.isSolvable(), healedReport.isHealthy());
    }

    private void checkPathTest(String path, HealthProblem... expectedProblems)
    {
        ConfigurationHealthReport expectedReport = new ConfigurationHealthReport(expectedProblems);
        assertEquals(expectedReport, configurationHealthChecker.checkPath(path));

        ConfigurationHealthReport healedReport = configurationHealthChecker.healPath(path);
        assertEquals(expectedReport.isSolvable(), healedReport.isHealthy());
    }

    @SymbolicName("project")
    public static class Project extends AbstractNamedConfiguration
    {
        private String description;
        private Options options;
        private Scm scm;
        private List<Label> labels = new LinkedList<Label>();
        private Map<String, Processor> processors = new HashMap<String, Processor>();
        private Map<String, Recipe> recipes = new HashMap<String, Recipe>();
        private Map<String, Trigger> triggers = new HashMap<String, Trigger>();
        @Ordered
        private Map<String, Stage> stages = new HashMap<String, Stage>();
        private Map<String, Hook> hooks = new HashMap<String, Hook>();

        public Project()
        {
        }

        public Project(String name)
        {
            super(name);
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public Options getOptions()
        {
            return options;
        }

        public void setOptions(Options options)
        {
            this.options = options;
        }

        public Scm getScm()
        {
            return scm;
        }

        public void setScm(Scm scm)
        {
            this.scm = scm;
        }

        public List<Label> getLabels()
        {
            return labels;
        }

        public void setLabels(List<Label> labels)
        {
            this.labels = labels;
        }

        public void addLabel(Label label)
        {
            labels.add(label);
        }

        public Map<String, Processor> getProcessors()
        {
            return processors;
        }

        public void setProcessors(Map<String, Processor> processors)
        {
            this.processors = processors;
        }

        public void addProcessor(Processor processor)
        {
            processors.put(processor.getName(), processor);
        }

        public Map<String, Recipe> getRecipes()
        {
            return recipes;
        }

        public void setRecipes(Map<String, Recipe> recipes)
        {
            this.recipes = recipes;
        }

        public void addRecipe(Recipe recipe)
        {
            recipes.put(recipe.getName(), recipe);
        }

        public Map<String, Trigger> getTriggers()
        {
            return triggers;
        }

        public void setTriggers(Map<String, Trigger> triggers)
        {
            this.triggers = triggers;
        }

        public void addTrigger(Trigger trigger)
        {
            triggers.put(trigger.getName(), trigger);
        }

        public Map<String, Stage> getStages()
        {
            return stages;
        }

        public void setStages(Map<String, Stage> stages)
        {
            this.stages = stages;
        }

        public void addStage(Stage stage)
        {
            stages.put(stage.getName(), stage);
        }

        public Map<String, Hook> getHooks()
        {
            return hooks;
        }

        public void setHooks(Map<String, Hook> hooks)
        {
            this.hooks = hooks;
        }

        public void addHook(Hook hook)
        {
            hooks.put(hook.getName(), hook);
        }
    }
    
    @SymbolicName("options")
    public static class Options extends AbstractConfiguration
    {
        private boolean magic;

        public boolean isMagic()
        {
            return magic;
        }

        public void setMagic(boolean magic)
        {
            this.magic = magic;
        }
    }

    @SymbolicName("scm")
    public static abstract class Scm extends AbstractConfiguration
    {
        private String common;

        public String getCommon()
        {
            return common;
        }

        public void setCommon(String common)
        {
            this.common = common;
        }
    }
    
    @SymbolicName("subversion")
    public static class Subversion extends Scm
    {
        private String url;

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }
    }
    
    @SymbolicName("perforce")
    public static class Perforce extends Scm
    {
        private String client;

        public String getClient()
        {
            return client;
        }

        public void setClient(String client)
        {
            this.client = client;
        }
    }

    @SymbolicName("processor")
    public static class Processor extends AbstractNamedConfiguration
    {
        public Processor()
        {
        }

        public Processor(String name)
        {
            super(name);
        }
    }

    @SymbolicName("recipe")
    public static class Recipe extends AbstractNamedConfiguration
    {
        @Ordered
        private Map<String, Command> commands = new LinkedHashMap<String, Command>();

        public Recipe()
        {
        }

        public Recipe(String name)
        {
            super(name);
        }

        public Map<String, Command> getCommands()
        {
            return commands;
        }

        public void setCommands(Map<String, Command> commands)
        {
            this.commands = commands;
        }

        public void addCommand(Command command)
        {
            commands.put(command.getName(), command);
        }
    }

    @SymbolicName("command")
    public static class Command extends AbstractNamedConfiguration
    {
        @Ordered
        private Map<String, Artifact> artifacts = new LinkedHashMap<String, Artifact>();

        public Command()
        {
        }

        public Command(String name)
        {
            super(name);
        }

        public Map<String, Artifact> getArtifacts()
        {
            return artifacts;
        }

        public void setArtifacts(Map<String, Artifact> artifacts)
        {
            this.artifacts = artifacts;
        }

        public void addArtifact(Artifact artifact)
        {
            artifacts.put(artifact.getName(), artifact);
        }
    }

    @SymbolicName("artifact")
    public static class Artifact extends AbstractNamedConfiguration
    {
        @Reference
        private List<Processor> processors = new LinkedList<Processor>();

        public Artifact()
        {
        }

        public Artifact(String name)
        {
            super(name);
        }

        public List<Processor> getProcessors()
        {
            return processors;
        }

        public void setProcessors(List<Processor> processors)
        {
            this.processors = processors;
        }

        public void addProcessor(Processor processor)
        {
            processors.add(processor);
        }
    }

    @SymbolicName("label")
    public static class Label extends AbstractConfiguration
    {
        private String label;

        public Label()
        {
        }

        public Label(String label)
        {
            this.label = label;
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }
    }
    
    @SymbolicName("trigger")
    public static class Trigger extends AbstractNamedConfiguration
    {
        @ExternalState
        private long triggerId;

        public Trigger()
        {
        }

        public Trigger(String name)
        {
            super(name);
        }

        public long getTriggerId()
        {
            return triggerId;
        }

        public void setTriggerId(long triggerId)
        {
            this.triggerId = triggerId;
        }
    }
    
    @SymbolicName("stage")
    public static class Stage extends AbstractNamedConfiguration
    {
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
    }
    
    @SymbolicName("hook")
    public static class Hook extends AbstractNamedConfiguration
    {
        @Reference
        private Stage stage;

        public Hook()
        {
        }

        public Hook(String name)
        {
            super(name);
        }

        public Hook(String name, Stage stage)
        {
            this(name);
            this.stage = stage;
        }

        public Stage getStage()
        {
            return stage;
        }

        public void setStage(Stage stage)
        {
            this.stage = stage;
        }
    }
}
