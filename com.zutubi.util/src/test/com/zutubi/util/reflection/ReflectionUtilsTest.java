package com.zutubi.util.reflection;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import com.zutubi.util.bean.BeanException;
import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.junit.ZutubiTestCase;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionUtilsTest extends ZutubiTestCase
{
    private Method noParams;
    private Method oneParam;
    private Method twoParams;
    private Method parentParam;
    private Method returnsList;
    private Method returnsStringList;

    protected void setUp() throws Exception
    {
        noParams = Methods.class.getMethod("noParams");
        oneParam = Methods.class.getMethod("oneParam", Foo.class);
        twoParams = Methods.class.getMethod("twoParams", Foo.class, Bar.class);
        parentParam = Methods.class.getMethod("parentParam", Parent.class);
        returnsList = Methods.class.getMethod("returnsList");
        returnsStringList = Methods.class.getMethod("returnsStringList");
    }

    public void testGetSuperclassesNoParent()
    {
        superclassesHelper(SCGrandparent.class, null, true, Object.class);
    }

    public void testGetSuperclassesOneParent()
    {
        superclassesHelper(SCParent.class, null, true, SCGrandparent.class, Object.class);
    }

    public void testGetSuperclassesTwoParents()
    {
        superclassesHelper(SCChild.class, null, true, SCParent.class, SCGrandparent.class, Object.class);
    }

    public void testGetSuperclassesNonStrictNoParent()
    {
        superclassesHelper(SCGrandparent.class, null, false, SCGrandparent.class, Object.class);
    }

    public void testGetSuperclassesNonStrictParent()
    {
        superclassesHelper(SCParent.class, null, false, SCParent.class, SCGrandparent.class, Object.class);
    }

    public void testGetSuperclassesStopObjectNoParent()
    {
        superclassesHelper(SCGrandparent.class, Object.class, true);
    }

    public void testGetSuperclassesStopObjectParent()
    {
        superclassesHelper(SCParent.class, Object.class, true, SCGrandparent.class);
    }

    public void testGetSuperclassesStopObjectNonStrictNoParent()
    {
        superclassesHelper(SCGrandparent.class, Object.class, false, SCGrandparent.class);
    }

    public void testGetSuperclassesStopObjectNonStrictParent()
    {
        superclassesHelper(SCParent.class, Object.class, false, SCParent.class, SCGrandparent.class);
    }

    public void testGetSuperclassesStopParent()
    {
        superclassesHelper(SCParent.class, SCGrandparent.class, true);
    }

    public void testGetSuperclassesStopParentNonStrict()
    {
        superclassesHelper(SCParent.class, SCGrandparent.class, false, SCParent.class);
    }

    public void testGetSuperclassesStopSelfNoParent()
    {
        superclassesHelper(SCGrandparent.class, SCGrandparent.class, true);
    }

    public void testGetSuperclassesStopSelfParent()
    {
        superclassesHelper(SCParent.class, SCParent.class, true);
    }

    public void testGetSuperclassesStopSelfNonStrictNoParent()
    {
        superclassesHelper(SCGrandparent.class, SCGrandparent.class, false);
    }

    public void testGetSuperclassesStopSelfNonStrictParent()
    {
        superclassesHelper(SCParent.class, SCParent.class, false);
    }

    private void superclassesHelper(Class clazz, Class stopClazz, boolean strict, Class... expectedClazzes)
    {
        assertEquals(Arrays.asList(expectedClazzes), ReflectionUtils.getSuperclasses(clazz, stopClazz, strict));
    }

    public void testGetSuperTypesClassNoParent()
    {
        supertypesHelper(SCGrandparent.class, null, true, Object.class);
    }

    public void testGetSuperTypesClassParent()
    {
        supertypesHelper(SCParent.class, null, true, SCGrandparent.class, Object.class);
    }

    public void testGetSuperTypesClassNonStrict()
    {
        supertypesHelper(SCParent.class, null, false, SCParent.class, SCGrandparent.class, Object.class);
    }

    public void testGetSuperTypesClassStopClass()
    {
        supertypesHelper(SCParent.class, SCGrandparent.class, true);
    }

    public void testGetSuperTypesInterfaceNoSuper()
    {
        supertypesHelper(IGrandparent.class, null, true);
    }

    public void testGetSuperTypesInterfaceSuper()
    {
        supertypesHelper(IParent.class, null, true, IGrandparent.class);
    }

    public void testGetSuperTypesInterfaceTwoSupers()
    {
        supertypesHelper(IChild.class, null, true, IGrandparent.class, IParent.class);
    }

    public void testGetSuperTypesClassDirectImpl()
    {
        supertypesHelper(DirectImpl.class, null, true, I.class, Object.class);
    }

    public void testGetSuperTypesClassDirectImplNonStrict()
    {
        supertypesHelper(DirectImpl.class, null, false, DirectImpl.class, I.class, Object.class);
    }

    public void testGetSuperTypesClassDirectImplStopSelf()
    {
        supertypesHelper(DirectImpl.class, DirectImpl.class, true);
    }

    public void testGetSuperTypesClassDirectImplStopSelfNonStrict()
    {
        supertypesHelper(DirectImpl.class, DirectImpl.class, false);
    }

    public void testGetSuperTypesClassInheritedImpl()
    {
        supertypesHelper(InheritedImpl.class, null, true, DirectImpl.class, I.class, Object.class);
    }

    public void testGetSuperTypesClassInheritedImplNonStrict()
    {
        supertypesHelper(InheritedImpl.class, null, false, InheritedImpl.class, DirectImpl.class, I.class, Object.class);
    }

    public void testGetSuperTypesClassInheritedImplStopParent()
    {
        supertypesHelper(InheritedImpl.class, DirectImpl.class, true);
    }

    public void testGetSuperTypesClassInheritedImplStopSelf()
    {
        supertypesHelper(InheritedImpl.class, InheritedImpl.class, true);
    }

    public void testGetSuperTypesClassInheritedImplStopParentNonStrict()
    {
        supertypesHelper(InheritedImpl.class, DirectImpl.class, false, InheritedImpl.class);
    }

    public void testGetSuperTypesClassInheritedImplStopSelfNonStrict()
    {
        supertypesHelper(InheritedImpl.class, InheritedImpl.class, false);
    }

    private void supertypesHelper(Class clazz, Class stopClazz, boolean strict, Class... expectedClazzes)
    {
        assertEquals(new HashSet<Class>(Arrays.asList(expectedClazzes)), ReflectionUtils.getSupertypes(clazz, stopClazz, strict));
    }

    public void testGetImplementedInterfacesInterfaceNoParent()
    {
        interfacesHelper(IGrandparent.class, null, true);
    }

    public void testGetImplementedInterfacesInterfaceParent()
    {
        interfacesHelper(IParent.class, null, true, IGrandparent.class);
    }

    public void testGetImplementedInterfacesInterfaceTwoParents()
    {
        interfacesHelper(IChild.class, null, true, IParent.class, IGrandparent.class);
    }

    public void testGetImplementedInterfacesInterfaceNoParentNonStrict()
    {
        interfacesHelper(IGrandparent.class, null, false, IGrandparent.class);
    }

    public void testGetImplementedInterfacesInterfaceParentNonStrict()
    {
        interfacesHelper(IParent.class, null, false, IParent.class, IGrandparent.class);
    }

    public void testGetImplementedInterfacesClassNoParent()
    {
        interfacesHelper(SCGrandparent.class, null, true);
    }

    public void testGetImplementedInterfacesClassParent()
    {
        interfacesHelper(SCParent.class, null, true);
    }

    public void testGetImplementedInterfacesClassNoParentNonStrict()
    {
        interfacesHelper(SCGrandparent.class, null, false);
    }

    public void testGetImplementedInterfacesClassParentNonStrict()
    {
        interfacesHelper(SCParent.class, null, false);
    }

    public void testGetImplementedInterfacesClassDirectImpl()
    {
        interfacesHelper(DirectImpl.class, null, true, I.class);
    }

    public void testGetImplementedInterfacesClassDirectImplStopSelf()
    {
        interfacesHelper(DirectImpl.class, DirectImpl.class, true);
    }

    public void testGetImplementedInterfacesClassInheritedImpl()
    {
        interfacesHelper(InheritedImpl.class, null, true, I.class);
    }

    public void testGetImplementedInterfacesClassInheritedImplStopParent()
    {
        interfacesHelper(InheritedImpl.class, DirectImpl.class, true);
    }

    public void testGetImplementedInterfacesClassInheritedImplStopSelf()
    {
        interfacesHelper(InheritedImpl.class, InheritedImpl.class, true);
    }

    public void testGetImplementedInterfacesClassChildImpl()
    {
        interfacesHelper(ChildImpl.class, null, true, IChild.class, IParent.class, IGrandparent.class);
    }

    public void testGetImplementedInterfacesInheritedChildImpl()
    {
        interfacesHelper(InheritedChildImpl.class, null, true, IChild.class, IParent.class, IGrandparent.class);
    }

    public void testGetImplementedInterfacesInheritedChildImplStopParent()
    {
        interfacesHelper(InheritedChildImpl.class, ChildImpl.class, true);
    }

    private void interfacesHelper(Class clazz, Class stopClazz, boolean strict, Class... expectedClazzes)
    {
        assertEquals(new HashSet<Class>(Arrays.asList(expectedClazzes)), ReflectionUtils.getImplementedInterfaces(clazz, stopClazz, strict));
    }

    public void testAcceptsParametersNoParams() throws Exception
    {
        assertTrue(ReflectionUtils.acceptsParameters(noParams));
        assertFalse(ReflectionUtils.acceptsParameters(oneParam));
    }

    public void testAcceptsParametersOneParam() throws Exception
    {
        assertFalse(ReflectionUtils.acceptsParameters(noParams, Foo.class));
        assertTrue(ReflectionUtils.acceptsParameters(oneParam, Foo.class));
    }

    public void testAcceptsParametersTwoParams() throws Exception
    {
        assertFalse(ReflectionUtils.acceptsParameters(noParams, Foo.class, Bar.class));
        assertFalse(ReflectionUtils.acceptsParameters(oneParam, Foo.class, Bar.class));
        assertTrue(ReflectionUtils.acceptsParameters(twoParams, Foo.class, Bar.class));
    }

    public void testAcceptsParametersInheritedType() throws Exception
    {
        assertTrue(ReflectionUtils.acceptsParameters(parentParam, Parent.class));
        assertTrue(ReflectionUtils.acceptsParameters(parentParam, Child.class));
    }

    public void testReturnsListMatch() throws Exception
    {
        assertTrue(ReflectionUtils.returnsType(returnsList, List.class));
    }

    public void testReturnsListSuperclass() throws Exception
    {
        assertTrue(ReflectionUtils.returnsType(returnsList, Collection.class));
    }

    public void testReturnsListMismatch() throws Exception
    {
        assertFalse(ReflectionUtils.returnsType(returnsList, Set.class));
    }

    public void testReturnsStringList() throws Exception
    {
        assertFalse(ReflectionUtils.returnsType(returnsList, List.class, String.class));
        assertFalse(ReflectionUtils.returnsType(returnsStringList, List.class, Foo.class));
        assertFalse(ReflectionUtils.returnsType(returnsStringList, Foo.class, String.class));
        assertTrue(ReflectionUtils.returnsType(returnsStringList, List.class, String.class));
    }

    public void testReturnsStringListCollection() throws Exception
    {
        assertTrue(ReflectionUtils.returnsType(returnsStringList, Collection.class, String.class));
    }

    public static class Methods
    {
        public void noParams()
        {
        }

        public void oneParam(Foo f)
        {
        }

        public void twoParams(Foo f, Bar b)
        {
        }

        public void parentParam(Parent c)
        {
        }

        public List returnsList()
        {
            return null;
        }

        public List<String> returnsStringList()
        {
            return null;
        }
    }

    public static class Foo
    {
    }

    public static class Bar
    {
    }

    public static class Parent
    {
    }

    public static class Child extends Parent
    {
    }

    public static interface IGrandparent
    {
    }

    public static interface IParent extends IGrandparent
    {
    }

    public static interface IChild extends IParent
    {
    }

    public static class SCGrandparent
    {
    }

    public static class SCParent extends SCGrandparent
    {
    }

    public static class SCChild extends SCParent
    {
    }

    public static interface I
    {
    }

    public static class DirectImpl implements I
    {
    }

    public static class InheritedImpl extends DirectImpl
    {
    }

    public static class ChildImpl implements IChild
    {
    }

    public static class InheritedChildImpl extends ChildImpl
    {
    }

    public void testGetQualifiedNameDirectField() throws NoSuchFieldException
    {
        getQualifiedNameFieldHelper(GetQualifiedName.class, "directInt");
    }

    public void testGetQualifiedNameInheritedField() throws NoSuchFieldException
    {
        getQualifiedNameFieldHelper(SuperGetQualifiedName.class, "inheritedInt");
    }

    public void testGetQualifiedNameInterfaceField() throws NoSuchFieldException
    {
        getQualifiedNameFieldHelper(IGetQualifiedName.class, "interfaceInt");
    }

    public void testGetQualifiedNameDirectMethod() throws NoSuchMethodException
    {
        getQualifiedNameMethodHelper(GetQualifiedName.class, "directMethod");
    }

    public void testGetQualifiedNameInheritedMethod() throws NoSuchMethodException
    {
        getQualifiedNameMethodHelper(SuperGetQualifiedName.class, "inheritedMethod");
    }

    public void testGetQualifiedNameOverriddenMethod() throws NoSuchMethodException
    {
        getQualifiedNameMethodHelper(GetQualifiedName.class, "overriddenMethod");
    }

    public void testGetQualifiedNameInterfaceMethod() throws NoSuchMethodException
    {
        getQualifiedNameMethodHelper(GetQualifiedName.class, "interfaceMethod");
    }

    private void getQualifiedNameFieldHelper(Class<?> expectedQualifyingClass, String fieldName) throws NoSuchFieldException
    {
        assertEquals(expectedQualifyingClass.getName() + "." + fieldName, ReflectionUtils.getQualifiedName(GetQualifiedName.class.getField(fieldName)));
    }

    private void getQualifiedNameMethodHelper(Class<?> expectedQualifyingClass, String methodName) throws NoSuchMethodException
    {
        assertEquals(expectedQualifyingClass.getName() + "." + methodName, ReflectionUtils.getQualifiedName(GetQualifiedName.class.getMethod(methodName)));
    }

    public static interface IGetQualifiedName
    {
        int interfaceInt = 0;

        void interfaceMethod();
    }

    public static class SuperGetQualifiedName
    {
        public int inheritedInt = 0;

        public void inheritedMethod()
        {

        }

        public void overriddenMethod()
        {

        }
    }

    public static class GetQualifiedName extends SuperGetQualifiedName implements IGetQualifiedName
    {
        public int directInt;

        public void directMethod()
        {
        }

        public void interfaceMethod()
        {
        }


        public void overriddenMethod()
        {
        }
    }

    public void testIsFinalStaticNonFinalField() throws NoSuchFieldException
    {
        isFinalFieldHelper(false, "staticNonFinalInt");
    }

    public void testIsFinalStaticFinalField() throws NoSuchFieldException
    {
        isFinalFieldHelper(true, "staticFinalInt");
    }

    public void testIsFinalNonFinalField() throws NoSuchFieldException
    {
        isFinalFieldHelper(false, "nonFinalInt");
    }

    public void testIsFinalFinalField() throws NoSuchFieldException
    {
        isFinalFieldHelper(true, "finalInt");
    }

    private void isFinalFieldHelper(boolean expectedResult, String fieldName) throws NoSuchFieldException
    {
        assertEquals(expectedResult, ReflectionUtils.isFinal(IsFinal.class.getField(fieldName)));
    }

    public void testIsFinalFinalMethod() throws NoSuchMethodException
    {
        isFinalMethodHelper(true, "finalMethod");
    }

    public void testIsFinalNonFinalMethod() throws NoSuchMethodException
    {
        isFinalMethodHelper(false, "nonFinalMethod");
    }

    private void isFinalMethodHelper(boolean expectedResult, String methodName) throws NoSuchMethodException
    {
        assertEquals(expectedResult, ReflectionUtils.isFinal(IsFinal.class.getMethod(methodName)));
    }

    public static class IsFinal
    {
        public static int staticNonFinalInt = 1;
        public static final int staticFinalInt = 1;

        public int nonFinalInt = 1;
        public final int finalInt = 1;

        public void nonFinalMethod()
        {
        }

        public final void finalMethod()
        {
        }
    }

    public void testSetFieldPublicField() throws NoSuchFieldException, BeanException
    {
        setFieldHelper(SetFieldValue.class.getDeclaredField("publicField"));
    }

    public void testSetFieldProtectedField() throws NoSuchFieldException, BeanException
    {
        setFieldHelper(SetFieldValue.class.getDeclaredField("protectedField"));
    }

    public void testSetFieldPrivateField() throws NoSuchFieldException, BeanException
    {
        setFieldHelper(SetFieldValue.class.getDeclaredField("privateField"));
    }

    public void testSetFieldInheritedPublicField() throws NoSuchFieldException, BeanException
    {
        setFieldHelper(SuperSetFieldValue.class.getDeclaredField("inheritedPublicField"));
    }

    public void testSetFieldInheritedProtectedField() throws NoSuchFieldException, BeanException
    {
        setFieldHelper(SuperSetFieldValue.class.getDeclaredField("inheritedProtectedField"));
    }

    public void testSetFieldInheritedPrivateField() throws NoSuchFieldException, BeanException
    {
        setFieldHelper(SuperSetFieldValue.class.getDeclaredField("inheritedPrivateField"));
    }

    public void testSetFieldFinalField() throws NoSuchFieldException
    {
        try
        {
            ReflectionUtils.setFieldValue(new SetFieldValue(), SetFieldValue.class.getDeclaredField("publicFinalField"), new Object());
            fail("Should not be able to set final field");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Cannot set final field 'com.zutubi.util.reflection.ReflectionUtilsTest$SetFieldValue.publicFinalField', even if it succeeds it may have no effect due to compiler optimisations", e.getMessage());
        }
    }

    public void testSetFieldPreservesAccessibility() throws NoSuchFieldException, BeanException
    {
        Field field = SetFieldValue.class.getDeclaredField("privateField");
        assertFalse(field.isAccessible());
        ReflectionUtils.setFieldValue(new SetFieldValue(), field, new Object());
        assertFalse(field.isAccessible());
    }

    private void setFieldHelper(Field field) throws BeanException
    {
        SetFieldValue instance = new SetFieldValue();
        Object value = new Object();
        ReflectionUtils.setFieldValue(instance, field, value);
        assertSame(value, BeanUtils.getProperty(field.getName(), instance));
    }

    public static class SuperSetFieldValue
    {
        private Object inheritedPrivateField = null;
        protected Object inheritedProtectedField;
        public Object inheritedPublicField;

        public Object getInheritedPrivateField()
        {
            return inheritedPrivateField;
        }

        public Object getInheritedProtectedField()
        {
            return inheritedProtectedField;
        }

        public Object getInheritedPublicField()
        {
            return inheritedPublicField;
        }
    }

    public static class SetFieldValue extends SuperSetFieldValue
    {
        private Object privateField = null;
        protected Object protectedField;
        public Object publicField;

        public final Object publicFinalField = new Object();

        public Object getPrivateField()
        {
            return privateField;
        }

        public Object getProtectedField()
        {
            return protectedField;
        }

        public Object getPublicField()
        {
            return publicField;
        }
    }

    public void testGetBeanPropertiesSimple() throws IntrospectionException
    {
        beanPropertiesHelper(BeanSimple.class, "simpleProperty");
    }

    public void testGetBeanPropertiesExtendsSimple() throws IntrospectionException
    {
        beanPropertiesHelper(BeanExtendsSimple.class, "extendsSimpleProperty", "simpleProperty");
    }

    public void testGetBeanPropertiesInterface() throws IntrospectionException
    {
        beanPropertiesHelper(BeanInterface.class, "interfaceProperty");
    }

    public void testGetBeanPropertiesImplements() throws IntrospectionException
    {
        beanPropertiesHelper(BeanImplements.class, "implementsProperty", "interfaceProperty");
    }

    public void testGetBeanPropertiesExtendsImplements() throws IntrospectionException
    {
        beanPropertiesHelper(BeanExtendsImplements.class, "extendsImplementsProperty", "implementsProperty", "interfaceProperty");
    }

    public void testGetBeanPropertiesSubInterface() throws IntrospectionException
    {
        beanPropertiesHelper(BeanSubInterface.class, "interfaceProperty", "subInterfaceProperty");
    }

    public void testGetBeanPropertiesImplementsSub() throws IntrospectionException
    {
        beanPropertiesHelper(BeanImplementsSub.class, "implementsSubProperty", "interfaceProperty", "subInterfaceProperty");
    }

    public void testGetBeanPropertiesExtendsImplementsSub() throws IntrospectionException
    {
        beanPropertiesHelper(BeanExtendsImplementsSub.class, "extendsImplementsSubProperty", "implementsSubProperty", "interfaceProperty", "subInterfaceProperty");
    }

    public void testGetBeanPropertiesExtendsAndImplements() throws IntrospectionException
    {
        beanPropertiesHelper(BeanExtendsAndImplements.class, "extendsAndImplementsProperty", "interfaceProperty", "simpleProperty");
    }

    public void testGetBeanPropertiesDeclaringClassOverrides() throws IntrospectionException
    {
        PropertyDescriptor[] properties = ReflectionUtils.getBeanProperties(BeanOverridesGetter.class);
        assertEquals(1, properties.length);
        assertEquals(BeanOverridesGetter.class, properties[0].getReadMethod().getDeclaringClass());
    }

    public void testGetBeanPropertiesDeclaringClassImplements() throws IntrospectionException
    {
        PropertyDescriptor[] properties = ReflectionUtils.getBeanProperties(BeanImplementsGetterAndSetter.class);
        assertEquals(1, properties.length);
        assertEquals(BeanImplementsGetterAndSetter.class, properties[0].getReadMethod().getDeclaringClass());
        assertEquals(BeanImplementsGetterAndSetter.class, properties[0].getWriteMethod().getDeclaringClass());
    }

    private void beanPropertiesHelper(final Class<?> clazz, String... expectedNames) throws IntrospectionException
    {
        PropertyDescriptor[] properties = ReflectionUtils.getBeanProperties(clazz);
        String[] names = CollectionUtils.mapToArray(properties, new Mapping<PropertyDescriptor, String>()
        {
            public String map(PropertyDescriptor propertyDescriptor)
            {
                return propertyDescriptor.getName();
            }
        }, new String[properties.length]);

        assertEquals(expectedNames.length, properties.length);

        Sort.StringComparator stringComparator = new Sort.StringComparator();
        Arrays.sort(expectedNames, stringComparator);
        Arrays.sort(names, stringComparator);
        assertTrue(Arrays.equals(expectedNames, names));
    }

    public static class PropertyComparator implements Comparator<PropertyDescriptor>
    {
        private static final Comparator<String> cmp = new Sort.StringComparator();

        public int compare(PropertyDescriptor o1, PropertyDescriptor o2)
        {
            return cmp.compare(o1.getName(), o2.getName());
        }
    }

    public static class BeanSimple
    {
        private int simpleProperty;

        public int getSimpleProperty()
        {
            return simpleProperty;
        }

        public void setSimpleProperty(int simpleProperty)
        {
            this.simpleProperty = simpleProperty;
        }
    }

    public static class BeanExtendsSimple extends BeanSimple
    {
        private int extendsSimpleProperty;

        public int getExtendsSimpleProperty()
        {
            return extendsSimpleProperty;
        }

        public void setExtendsSimpleProperty(int extendsSimpleProperty)
        {
            this.extendsSimpleProperty = extendsSimpleProperty;
        }
    }

    public static class BeanOverridesGetter extends BeanSimple
    {
        @Override
        public int getSimpleProperty()
        {
            return super.getSimpleProperty();
        }
    }

    public static interface BeanInterface
    {
        int getInterfaceProperty();
        void setInterfaceProperty(int i);
    }

    public static class BeanImplementsGetterAndSetter implements BeanInterface
    {
        private int interfaceProperty;

        public int getInterfaceProperty()
        {
            return interfaceProperty;
        }

        public void setInterfaceProperty(int interfaceProperty)
        {
            this.interfaceProperty = interfaceProperty;
        }
    }

    public abstract static class BeanImplements implements BeanInterface
    {
        private int implementsProperty;

        public int getImplementsProperty()
        {
            return implementsProperty;
        }

        public void setImplementsProperty(int implementsProperty)
        {
            this.implementsProperty = implementsProperty;
        }
    }

    public static abstract class BeanExtendsImplements extends BeanImplements
    {
        private int extendsImplementsProperty;

        public int getExtendsImplementsProperty()
        {
            return extendsImplementsProperty;
        }

        public void setExtendsImplementsProperty(int extendsImplementsProperty)
        {
            this.extendsImplementsProperty = extendsImplementsProperty;
        }
    }

    public static interface BeanSubInterface extends BeanInterface
    {
        int getSubInterfaceProperty();
        void setSubInterfaceProperty(int i);
    }

    public abstract static class BeanImplementsSub implements BeanSubInterface
    {
        private int implementsSubProperty;

        public int getImplementsSubProperty()
        {
            return implementsSubProperty;
        }

        public void setImplementsSubProperty(int implementsSubProperty)
        {
            this.implementsSubProperty = implementsSubProperty;
        }
    }

    public static abstract class BeanExtendsImplementsSub extends BeanImplementsSub
    {
        private int extendsImplementsSubProperty;

        public int getExtendsImplementsSubProperty()
        {
            return extendsImplementsSubProperty;
        }

        public void setExtendsImplementsSubProperty(int extendsImplementsSubProperty)
        {
            this.extendsImplementsSubProperty = extendsImplementsSubProperty;
        }
    }

    public static abstract class BeanExtendsAndImplements extends BeanSimple implements BeanInterface
    {
        private int extendsAndImplementsProperty;

        public int getExtendsAndImplementsProperty()
        {
            return extendsAndImplementsProperty;
        }

        public void setExtendsAndImplementsProperty(int extendsAndImplementsProperty)
        {
            this.extendsAndImplementsProperty = extendsAndImplementsProperty;
        }
    }

    public void testInvokeMethodDeclaredOnClass()
    {
        Object target = new InvokeMethod();
        assertEquals("public", ReflectionUtils.invoke(target, "publicNoArgMethod"));
        assertEquals("package", ReflectionUtils.invoke(target, "packageNoArgMethod"));
        assertEquals("protected", ReflectionUtils.invoke(target, "protectedNoArgMethod"));
        assertEquals("private", ReflectionUtils.invoke(target, "privateNoArgMethod"));
        assertEquals("arg", ReflectionUtils.invoke(target, "oneArgMethod", "arg"));
        Object obj = new Object();
        assertEquals(obj, ReflectionUtils.invoke(target, "twoArgMethod", obj, "arg"));
    }

    public void testInvokeMethodDeclaredOnSuperclass()
    {
        Object target = new ExtensionOfInvokeMethod();
        assertEquals("public", ReflectionUtils.invoke(target, "publicNoArgMethod"));
        assertEquals("package", ReflectionUtils.invoke(target, "packageNoArgMethod"));
        assertEquals("protected", ReflectionUtils.invoke(target, "protectedNoArgMethod"));
        assertEquals("private", ReflectionUtils.invoke(target, "privateNoArgMethod"));
        assertEquals("arg", ReflectionUtils.invoke(target, "oneArgMethod", "arg"));
        Object obj = new Object();
        assertEquals(obj, ReflectionUtils.invoke(target, "twoArgMethod", obj, "arg"));
    }

    public class InvokeMethod
    {
        public String protectedNoArgMethod()
        {
            return "protected";
        }

        public String publicNoArgMethod()
        {
            return "public";
        }

        String packageNoArgMethod()
        {
            return "package";
        }

        private String privateNoArgMethod()
        {
            return "private";
        }

        public String oneArgMethod(String arg)
        {
            return arg;
        }

        public Object twoArgMethod(Object a, String b)
        {
            return a;
        }
    }

    public class ExtensionOfInvokeMethod extends InvokeMethod
    {

    }
}
