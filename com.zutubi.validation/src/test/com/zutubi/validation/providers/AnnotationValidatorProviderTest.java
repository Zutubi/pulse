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

package com.zutubi.validation.providers;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.Validator;
import com.zutubi.validation.annotations.*;
import com.zutubi.validation.types.*;
import com.zutubi.validation.validators.*;

import java.util.List;

public class AnnotationValidatorProviderTest extends ZutubiTestCase
{
    private AnnotationValidatorProvider provider;

    protected void setUp() throws Exception
    {
        super.setUp();
        provider = new AnnotationValidatorProvider();
    }

    public void testPropertyConstraint()
    {
        TestAnimal animal = new TestAnimal();
        List<Validator> validators = provider.getValidators(animal.getClass());
        assertNotNull(validators);
        assertEquals(1, validators.size());

        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("head", r.getFieldName());
    }

    public void testPropertyConstriantWithParameters()
    {
        TestWall wall = new TestWall();
        List<Validator> validators = provider.getValidators(wall.getClass());
        assertNotNull(validators);
        assertEquals(1, validators.size());

        NumericValidator n = (NumericValidator) validators.get(0);
        assertEquals("height", n.getFieldName());
        assertEquals(20, n.getMax());
    }

    public void testCustomKeySuffixDefinedOnConstraint()
    {
        TestAnimal animal = new TestAnimal();
        List<Validator> validators = provider.getValidators(animal.getClass());
        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("myrequired", r.getDefaultKeySuffix());
    }

    public void testNestedValidation()
    {
        TestHouse house = new TestHouse();
        List<Validator> validators = provider.getValidators(house.getClass());

        assertEquals(1, validators.size());

        DelegateValidator d = (DelegateValidator) validators.get(0);
        assertEquals("door", d.getFieldName());
    }

    public void testValidationDefinedInSuperClass()
    {
        TestMouse mouse = new TestMouse();
        List<Validator> validators = provider.getValidators(mouse.getClass());

        assertEquals(1, validators.size());
        RequiredValidator r = (RequiredValidator) validators.get(0);
        assertEquals("head", r.getFieldName());
    }

    public void testValidationDefinedInImplementedInterface()
    {
        // the book implementes the readable interface with its validation.
        TestBook book = new TestBook();
        List<Validator> validators = provider.getValidators(book.getClass());

        assertEquals(2, validators.size());

        // ensure that the readable validator is picked up only once, even through the interface is
        // implemented multiple times.
        TestNiceBook niceBook = new TestNiceBook();
        validators = provider.getValidators(niceBook.getClass());

        assertEquals(2, validators.size());
    }

    public void testValidatorOrdering()
    {
        typedValidatorHelper(new TestStraightJacket(), RequiredValidator.class, EmailValidator.class, RegexValidator.class);
    }

    public void testIndirectConstraintsOnField()
    {
        typedValidatorHelper(new IndirectConstraintsOnField(), RequiredValidator.class, EmailValidator.class, RegexValidator.class);
    }

    public static class IndirectConstraintsOnField
    {
        @Required @Email @Regex(pattern = ".")
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

    public void testDirectConstraintsOnField()
    {
        typedValidatorHelper(new DirectConstraintsOnField(), RequiredValidator.class, EmailValidator.class);
    }

    public static class DirectConstraintsOnField
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

    public void testIndirectInheritedFromSuperClass()
    {
        typedValidatorHelper(new IndirectInheritedFromSuperClassSub(), NumericValidator.class);
    }

    public static class IndirectInheritedFromSuperClassSuper
    {
        @Min(0)
        private int f;

        public int getF()
        {
            return f;
        }

        public void setF(int f)
        {
            this.f = f;
        }
    }

    public static class IndirectInheritedFromSuperClassSub extends IndirectInheritedFromSuperClassSuper
    {
    }

    public void testConstraintPropertyApplied()
    {
        List<Validator> validators = typedValidatorHelper(new ConstraintPropertyApplied(), NumericValidator.class);
        NumericValidator numericValidator = (NumericValidator) validators.get(0);
        assertEquals(11, numericValidator.getMin());
    }

    public static class ConstraintPropertyApplied
    {
        @Min(11)
        private int i;

        public int getI()
        {
            return i;
        }

        public void setI(int i)
        {
            this.i = i;
        }
    }

    /**
     * Helper for tests that just check the number, order and type of
     * validators returned for an instance.
     *
     * @param instance      instance to get the validators for
     * @param expectedTypes expected classes for the returned validators, in
     * @return the found validators
     */
    private List<Validator> typedValidatorHelper(Object instance, Class<? extends Validator>... expectedTypes)
    {
        List<Validator> validators = provider.getValidators(instance.getClass());
        assertEquals(expectedTypes.length, validators.size());
        int i = 0;
        for (Validator validator: validators)
        {
            assertSame(validator.getClass(), expectedTypes[i]);
            i++;
        }

        return validators;
    }
}
