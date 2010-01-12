package com.zutubi.pulse.master.model;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.events.RecordUpdatedEvent;
import com.zutubi.util.StringUtils;

import java.util.List;

/**
 * Listens for renames of recipes and changes references to that name to match.
 * Implemented at this low level as changes can happen in templates, so higher-
 * level methods that deal with instances may not even be raised.
 *
 * Normally such manual fixing is not required as these things would be
 * modelled as references which Just Work.  In this case, though, the recipes
 * may or may not be in a Pulse file, so we can't use references.
 *
 * See CIB-2255.
 */
public class RecipeRenameListener implements EventListener
{
    private static final String SYMBOLIC_NAME = RecipeConfiguration.class.getAnnotation(SymbolicName.class).value();
    private static final String PROPERTY_NAME = "name";

    private ConfigurationTemplateManager configurationTemplateManager;

    public void handleEvent(Event event)
    {
        RecordUpdatedEvent rue = (RecordUpdatedEvent) event;
        if (SYMBOLIC_NAME.equals(rue.getNewRecord().getSymbolicName()))
        {
            Record originalRecord = rue.getOriginalRecord();
            Record newRecord = rue.getNewRecord();
            String originalName = (String) originalRecord.get(PROPERTY_NAME);
            String newName = (String) newRecord.get(PROPERTY_NAME);

            if (StringUtils.stringSet(originalName) && StringUtils.stringSet(newName) && !originalName.equals(newName))
            {
                handleRename(rue.getPath(), originalName, newName);
            }
        }
    }

    private void handleRename(String path, String originalName, String newName)
    {
        String owningProjectPath = PathUtils.getPath(0, 2, PathUtils.getPathElements(path));
        List<String> allProjectPaths = configurationTemplateManager.getDescendantPaths(owningProjectPath, false, false, false);
        for (String projectPath: allProjectPaths)
        {
            handleRenameInProject(projectPath, originalName, newName);
        }
    }

    private void handleRenameInProject(String projectPath, String originalName, String newName)
    {
        ProjectConfiguration projectConfig = configurationTemplateManager.getInstance(projectPath, ProjectConfiguration.class);
        TypeConfiguration typeConfig = projectConfig.getType();
        if (typeConfig instanceof MultiRecipeTypeConfiguration)
        {
            MultiRecipeTypeConfiguration multiTypeConfig = (MultiRecipeTypeConfiguration) typeConfig;
            if (originalName.equals(multiTypeConfig.getDefaultRecipe()))
            {
                multiTypeConfig = configurationTemplateManager.deepClone(multiTypeConfig);
                multiTypeConfig.setDefaultRecipe(newName);
                configurationTemplateManager.save(multiTypeConfig);
            }

            for (BuildStageConfiguration stage: projectConfig.getStages().values())
            {
                if (originalName.equals(stage.getRecipe()))
                {
                    stage = configurationTemplateManager.deepClone(stage);
                    stage.setRecipe(newName);
                    configurationTemplateManager.save(stage);
                }
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecordUpdatedEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
