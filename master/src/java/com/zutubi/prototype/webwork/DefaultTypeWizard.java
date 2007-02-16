package com.zutubi.prototype.webwork;

import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.pulse.core.PulseRuntimeException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;

/**
 * An implementation of the wizard interface based on the type details specified by a given configuration path.
 * 
 *
 */
public class DefaultTypeWizard implements Wizard
{
    private ConfigurationRegistry configurationRegistry;
    private TypeRegistry typeRegistry;
    private RecordManager recordManager;

    private String path;

    private SelectState selectState;

    private WizardState currentState;
    private String currentSymbolicName;

    private Map<String, WizardState> stateCache = new HashMap<String, WizardState>();

    public DefaultTypeWizard(String path)
    {
        this.path = path;
    }

    public WizardState getCurrentState()
    {
        return currentState;
    }

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
            Type type = typeRegistry.getType(currentSymbolicName);
            Object obj = currentState.data();
            Record record = (Record) type.unstantiate(obj);
            recordManager.store(path, record);
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
                currentSymbolicName = selectState.getSelection();
                if (!stateCache.containsKey(currentSymbolicName))
                {
                    CompositeType type = typeRegistry.getType(currentSymbolicName);
                    Object state = type.getClazz().newInstance();
                    stateCache.put(currentSymbolicName, new ConfigurationState(state, type));
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
    }

    public void initialise()
    {
        String symbolicName = configurationRegistry.getSymbolicName(path);
        CompositeType type = typeRegistry.getType(symbolicName);

        selectState = new SelectState(type.getExtensions());
        selectState.setType(type);
        
        currentState = selectState;
    }

    public WizardState doRestart()
    {
        currentState = selectState;

        return currentState;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public class SelectState implements WizardState
    {
        private List<String> options;

        private Type type;

        public SelectState(List<String> options)
        {
            this.options = options;
        }

        public Object data()
        {
            return this;
        }

        public CompositeType type()
        {
            try
            {
                CompositeType type = typeRegistry.getType(getClass());
                if (type == null)
                {
                    type = typeRegistry.registerAnonymous(getClass());
                }
                return type;
            }
            catch (TypeException e)
            {
                throw new PulseRuntimeException(e);
            }
        }

        public String name()
        {
            return getClass().getName();
        }

        public Type getType()
        {
            return type;
        }

        public void setType(Type type)
        {
            this.type = type;
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
        private CompositeType type;

        public ConfigurationState(Object obj, CompositeType type)
        {
            this.dataObject = obj;
            this.type = type;
        }

        public String name()
        {
            return dataObject.getClass().getSimpleName();
        }

        public Object data()
        {
            return dataObject;
        }

        public CompositeType type()
        {
            return type;
        }

        public Form getForm(Object data)
        {
            CompositeType stateType = typeRegistry.getType(dataObject.getClass());

            FormDescriptorFactory formFactory = new FormDescriptorFactory();
            formFactory.setTypeRegistry(typeRegistry);
            FormDescriptor formDescriptor = formFactory.createDescriptor(stateType);

            // where do we get the data from?
            Record record = new Record();
            Form form = formDescriptor.instantiate(record);

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
