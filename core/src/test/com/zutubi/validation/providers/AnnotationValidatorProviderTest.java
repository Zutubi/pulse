package com.zutubi.validation.providers;

import junit.framework.TestCase;
import com.zutubi.validation.bean.DefaultObjectFactory;
import com.zutubi.validation.Validator;
import com.zutubi.validation.mock.*;
import com.zutubi.validation.validators.*;

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
        List<Validator> validators = provider.getValidators(animal);
        assertNotNull(validators);
        assertEquals(1, validators.size());

        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("head", r.getFieldName());
    }

    public void testPropertyConstriantWithParameters()
    {
        MockWall wall = new MockWall();
        List<Validator> validators = provider.getValidators(wall);
        assertNotNull(validators);
        assertEquals(1, validators.size());

        NumericValidator n = (NumericValidator) validators.get(0);
        assertEquals("height", n.getFieldName());
        assertEquals(20, n.getMax());
    }

    public void testCustomMessageKeyDefinedOnConstraint()
    {
        MockAnimal animal = new MockAnimal();
        List<Validator> validators = provider.getValidators(animal);
        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("animal.head.required", r.getMessageKey());
    }

    public void testNestedValidation()
    {
        MockHouse house = new MockHouse();
        List<Validator> validators = provider.getValidators(house);

        assertEquals(1, validators.size());

        DelegateValidator d = (DelegateValidator) validators.get(0);
        assertEquals("door", d.getFieldName());
    }

    public void testValidationDefinedInSuperClass()
    {
        MockMouse mouse = new MockMouse();
        List<Validator> validators = provider.getValidators(mouse);

        assertEquals(1, validators.size());
        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("head", r.getFieldName());
    }

    public void testValidationDefinedInImplementedInterface()
    {
        // the book implementes the readable interface with its validation.
        MockBook book = new MockBook();
        List<Validator> validators = provider.getValidators(book);

        assertEquals(2, validators.size());
        RequiredValidator r = (RequiredValidator) validators.get(0);

        // ensure that the readable validator is picked up only once, even through the interface is
        // implemented multiple times.
        MockNiceBook niceBook = new MockNiceBook();
        validators = provider.getValidators(niceBook);

        assertEquals(2, validators.size());
        r = (RequiredValidator) validators.get(0);
    }

    public void testValidatorOrdering()
    {
        MockStraightJacket jacket = new MockStraightJacket();
        List<Validator> validators = provider.getValidators(jacket);
        assertEquals(4, validators.size());
        assertTrue(validators.get(0) instanceof RequiredValidator);
        assertTrue(validators.get(1) instanceof EmailFieldValidator);
        assertTrue(validators.get(2) instanceof NameValidator);
        assertTrue(validators.get(3) instanceof RegexValidator);
    }
}
