package com.zutubi.validation.providers;

import com.zutubi.validation.Validator;
import com.zutubi.validation.annotations.*;
import com.zutubi.validation.mock.*;
import com.zutubi.validation.validators.*;
import junit.framework.TestCase;

import java.util.List;

/**
 * <class-comment/>
 */
public class AnnotationValidatorProviderTest extends TestCase
{
    private AnnotationValidatorProvider provider;

    protected void setUp() throws Exception
    {
        super.setUp();

        provider = new AnnotationValidatorProvider();
    }

    protected void tearDown() throws Exception
    {
        provider = null;

        super.tearDown();
    }

    public void testPropertyConstraint()
    {
        MockAnimal animal = new MockAnimal();
        List<Validator> validators = provider.getValidators(animal, null);
        assertNotNull(validators);
        assertEquals(1, validators.size());

        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("head", r.getFieldName());
    }

    public void testPropertyConstriantWithParameters()
    {
        MockWall wall = new MockWall();
        List<Validator> validators = provider.getValidators(wall, null);
        assertNotNull(validators);
        assertEquals(1, validators.size());

        NumericValidator n = (NumericValidator) validators.get(0);
        assertEquals("height", n.getFieldName());
        assertEquals(20, n.getMax());
    }

    public void testCustomMessageKeyDefinedOnConstraint()
    {
        MockAnimal animal = new MockAnimal();
        List<Validator> validators = provider.getValidators(animal, null);
        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("animal.head.required", r.getMessageKey());
    }

    public void testNestedValidation()
    {
        MockHouse house = new MockHouse();
        List<Validator> validators = provider.getValidators(house, null);

        assertEquals(1, validators.size());

        DelegateValidator d = (DelegateValidator) validators.get(0);
        assertEquals("door", d.getFieldName());
    }

    public void testValidationDefinedInSuperClass()
    {
        MockMouse mouse = new MockMouse();
        List<Validator> validators = provider.getValidators(mouse, null);

        assertEquals(1, validators.size());
        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("head", r.getFieldName());
    }

    public void testValidationDefinedInImplementedInterface()
    {
        // the book implementes the readable interface with its validation.
        MockBook book = new MockBook();
        List<Validator> validators = provider.getValidators(book, null);

        assertEquals(2, validators.size());
        validators.get(0);

        // ensure that the readable validator is picked up only once, even through the interface is
        // implemented multiple times.
        MockNiceBook niceBook = new MockNiceBook();
        validators = provider.getValidators(niceBook, null);

        assertEquals(2, validators.size());
        validators.get(0);
    }

    public void testValidatorOrdering()
    {
        MockStraightJacket jacket = new MockStraightJacket();
        List<Validator> validators = provider.getValidators(jacket, null);
        assertEquals(4, validators.size());
        assertTrue(validators.get(0) instanceof RequiredValidator);
        assertTrue(validators.get(1) instanceof EmailValidator);
        assertTrue(validators.get(2) instanceof NameValidator);
        assertTrue(validators.get(3) instanceof RegexValidator);
    }

    public void testIndirectConstraintsOnField()
    {
        MockIndirectConstraintsOnField mock = new MockIndirectConstraintsOnField();
        List<Validator> validators = provider.getValidators(mock, null);
        assertEquals(4, validators.size());
        assertTrue(validators.get(0) instanceof RequiredValidator);
        assertTrue(validators.get(1) instanceof EmailValidator);
        assertTrue(validators.get(2) instanceof NameValidator);
        assertTrue(validators.get(3) instanceof RegexValidator);
    }

    public void testDirectConstraintsOnField()
    {
        MockDirectConstraintsOnField mock = new MockDirectConstraintsOnField();
        List<Validator> validators = provider.getValidators(mock, null);
        assertEquals(2, validators.size());
        assertTrue(validators.get(0) instanceof RequiredValidator);
        assertTrue(validators.get(1) instanceof EmailValidator);
    }

    public class MockIndirectConstraintsOnField
    {
        @Required @Email @Name @Regex(pattern = ".")
        private String field;

        public String getField()
        {
            return field;
        }

        public void setField(String field)
        {
            this.field = field;
        }
    }

    public class MockDirectConstraintsOnField
    {
        @Constraint({"com.zutubi.validation.validators.RequiredValidator", "com.zutubi.validation.validators.EmailValidator"})
        private String field;

        public String getField()
        {
            return field;
        }

        public void setField(String field)
        {
            this.field = field;
        }
    }
}
