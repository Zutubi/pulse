package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.Password;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.ConfigurationValidatorProvider;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.junit.ZutubiTestCase;
import static org.mockito.Mockito.mock;

import java.util.List;

public class FormDescriptorFactoryTest extends ZutubiTestCase
{
    private TypeRegistry typeRegistry;
    private FormDescriptorFactory descriptorFactory;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        descriptorFactory = new FormDescriptorFactory();
        descriptorFactory.setConfigurationValidatorProvider(mock(ConfigurationValidatorProvider.class));
    }

    public void testPasswordField() throws TypeException
    {
        typeRegistry.register(PasswordConfig.class);
        FormDescriptor formDescriptor = descriptorFactory.createDescriptor("path", "basename", typeRegistry.getType(PasswordConfig.class), true, "form");

        List<FieldDescriptor> fieldDescriptors = formDescriptor.getFieldDescriptors();
        assertEquals(1, fieldDescriptors.size());
        assertEquals("password", fieldDescriptors.get(0).getType());
    }

    public void testTextField() throws TypeException
    {
        typeRegistry.register(TextConfig.class);
        FormDescriptor formDescriptor = descriptorFactory.createDescriptor("path", "basename", typeRegistry.getType(TextConfig.class), true, "form");

        List<FieldDescriptor> fieldDescriptors = formDescriptor.getFieldDescriptors();
        assertEquals(1, fieldDescriptors.size());
        assertEquals("text", fieldDescriptors.get(0).getType());
    }

    @SymbolicName("text")
    public static class TextConfig extends AbstractConfiguration
    {
        private String text;

        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }
    }

    @SymbolicName("password")
    public static class PasswordConfig extends AbstractConfiguration
    {
        @Password
        private String password;

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }
    }
}
