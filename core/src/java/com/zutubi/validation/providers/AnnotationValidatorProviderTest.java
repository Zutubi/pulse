package com.zutubi.validation.providers;

import junit.framework.TestCase;
import com.zutubi.validation.bean.DefaultObjectFactory;
import com.zutubi.validation.Validator;
import com.zutubi.validation.validators.RequiredValidator;
import com.zutubi.validation.validators.NumericValidator;

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
        provider.setObjectFactory(new DefaultObjectFactory());
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
}
