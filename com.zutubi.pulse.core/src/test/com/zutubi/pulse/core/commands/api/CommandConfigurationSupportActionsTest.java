package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.config.ConfigurationProvider;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.List;

public class CommandConfigurationSupportActionsTest extends PulseTestCase
{
    private CommandConfigurationSupportActions actions;
    private ConfigurationProvider configurationProvider;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        configurationProvider = mock(ConfigurationProvider.class);
        
        actions = new CommandConfigurationSupportActions();
        actions.setConfigurationProvider(configurationProvider);
    }

    public void testGetActions()
    {
        CommandConfiguration config = new NoopCommandConfiguration();
        config.setEnabled(true);

        List<String> availableActions = actions.getActions(config);
        assertTrue(availableActions.contains(CommandConfigurationSupportActions.ACTION_DISABLE));
        assertFalse(availableActions.contains(CommandConfigurationSupportActions.ACTION_ENABLE));

        config.setEnabled(false);

        availableActions = actions.getActions(config);
        assertFalse(availableActions.contains(CommandConfigurationSupportActions.ACTION_DISABLE));
        assertTrue(availableActions.contains(CommandConfigurationSupportActions.ACTION_ENABLE));
    }

    public void testDoEnable()
    {
        CommandConfiguration config = new NoopCommandConfiguration();
        config.setHandle(1);
        config.setEnabled(false);
        
        actions.doEnable(config);
        
        assertTrue(config.isEnabled());
        verify(configurationProvider, times(1)).save(config);
    }

    public void testDoDisable()
    {
        CommandConfiguration config = new NoopCommandConfiguration();
        config.setHandle(1);

        actions.doDisable(config);

        assertFalse(config.isEnabled());
        verify(configurationProvider, times(1)).save(config);
    }

    private class NoopCommandConfiguration extends CommandConfigurationSupport
    {
        private NoopCommandConfiguration()
        {
            super(NoopCommand.class);
        }
    }

    private class NoopCommand extends CommandSupport
    {
        private NoopCommand(CommandConfigurationSupport config)
        {
            super(config);
        }

        public void execute(CommandContext commandContext)
        {
            // noop.
        }
    }
}
