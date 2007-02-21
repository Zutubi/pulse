package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.pulse.core.PulseRuntimeException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the wizard interface that generates a simple 2 step wizard for types that have
 * extensions.
 *
 * The first step displayed to the UI is a selection for which of the extensions is being selected. The second
 * step is the configuration form for the selected type.
 *
 */
public class ExtendedTypeWizard implements Wizard
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private TypeRegistry typeRegistry;

    private String path;

    private SelectState selectState;

    private WizardState currentState;

    private Map<String, WizardState> stateCache = new HashMap<String, WizardState>();

    public ExtendedTypeWizard(String path)
    {
        this.path = path;
    }

    public WizardState getCurrentState()
    {
        return currentState;
    }

    /**
     * This is a simple two step wizard. Either we are in the selection state, in which case the NEXT and CANCEL
     * transitions are available, or we are in the data form state, in which case PREVIOUS, FINISH and CANCEL
     * are available.
     *
     * @return the list of wizard transitions available for the current state.
     */
    public List<WizardTransition> getAvailableActions()
    {
        if (currentState == selectState)
        {
            return Arrays.asList(WizardTransition.NEXT, WizardTransition.CANCEL);
        }
        return Arrays.asList(WizardTransition.PREVIOUS, WizardTransition.FINISH, WizardTransition.CANCEL);
    }

    public void doFinish()
    {
        // conver the data into a record based on its type, and then record that record.
        try
        {
            configurationPersistenceManager.setInstance(path, currentState.getData());
        }
        catch (TypeException e)
        {
            throw new PulseRuntimeException(e);
        }
    }

    public WizardState doNext()
    {
        try
        {
            if (currentState == selectState)
            {
                String currentSymbolicName = selectState.getSelection();
                if (!stateCache.containsKey(currentSymbolicName))
                {
                    CompositeType type = (CompositeType) typeRegistry.getType(currentSymbolicName);
                    Object state = type.getClazz().newInstance();
                    stateCache.put(currentSymbolicName, new ConfigurationState(state));
                }
                currentState = stateCache.get(currentSymbolicName);
            }
            return currentState;
        }
        catch (Exception e)
        {
            throw new PulseRuntimeException(e);
        }
    }

    public WizardState doPrevious()
    {
        currentState = selectState;
        return currentState;
    }

    public void doCancel()
    {
        // cleanup any resources.
    }

    public void initialise()
    {
        Type type = (Type) configurationPersistenceManager.getType(path);
        if (type instanceof CollectionType)
        {
            type = ((CollectionType)type).getCollectionType();
        }
        if (!(type instanceof CompositeType))
        {
            throw new RuntimeException("Can not initialise ExtendedTypeWizard for a non-composite type.");
        }

        CompositeType compositeType = (CompositeType) type;

        List<String> extensions = compositeType.getExtensions();
        if (extensions.size() == 0)
        {
            throw new RuntimeException("No extension definitions available for the path: " + path);
        }

        if (extensions.size() > 1)
        {
            selectState = new SelectState(extensions);
            currentState = selectState;
        }
        else
        {
            try
            {
                CompositeType availableType = (CompositeType) typeRegistry.getType(extensions.get(0));
                Object instance = availableType.getClazz().newInstance();
                currentState = new ConfigurationState(instance);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public WizardState doRestart()
    {
        if (selectState != null)
        {
            // ... clear out the cache ?
            currentState = selectState;
        }
        return currentState;
    }

    /**
     * Required resource
     *
     * @param typeRegistry instance
     */
    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Required resource
     *
     * @param persistenceManager instance
     */
    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager persistenceManager)
    {
        this.configurationPersistenceManager = persistenceManager;
    }

    public class SelectState implements WizardState
    {
        private List<String> options;

        public SelectState(List<String> options)
        {
            this.options = options;
        }

        public Object getData()
        {
            return this;
        }

        public String name()
        {
            return getClass().getName();
        }

        public Form getForm(Object data)
        {
            Form form = new Form();
            form.setId(getClass().getName());
            Field field = new Field();
            field.addParameter("name", "selection");
            field.addParameter("type", "select");
            field.addParameter("label", "selection");
            field.addParameter("list", options);
            if (selection != null)
            {
                field.addParameter("value", selection);
            }
            field.setTabindex(1);
            form.add(field);
            field = new Field();
            field.addParameter("name", "state");
            field.addParameter("type", "hidden");
            field.addParameter("value", name());
            form.add(field);
            form.setActions(Arrays.asList("next"));
            return form;
        }

        private String selection;

        public String getSelection()
        {
            return selection;
        }

        public void setSelection(String selection)
        {
            this.selection = selection;
        }
    }

    public class ConfigurationState implements WizardState
    {
        private Object dataObject;

        public ConfigurationState(Object obj)
        {
            this.dataObject = obj;
        }

        public String name()
        {
            return dataObject.getClass().getSimpleName();
        }

        public Object getData()
        {
            return dataObject;
        }

        public Form getForm(Object data)
        {
            Type stateType = typeRegistry.getType(dataObject.getClass());

            FormDescriptorFactory formFactory = new FormDescriptorFactory();
            formFactory.setTypeRegistry(typeRegistry);
            FormDescriptor formDescriptor = formFactory.createDescriptor(stateType);

            // where do we get the data from?
            Form form = formDescriptor.instantiate(data);

            List<String> actions = new LinkedList<String>();
            for (WizardTransition transition : getAvailableActions())
            {
                actions.add(transition.name().toLowerCase());   
            }

            Field state = new Field();
            state.addParameter("name", "state");
            state.addParameter("type", "hidden");
            state.addParameter("value", name());
            form.add(state);
            
            form.setActions(actions);
            return form;
        }

    }
}
