package com.zutubi.util;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;

/**
 */
public class ReflectionUtilsTest extends TestCase
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

    public void testReturnsList() throws Exception
    {
        assertFalse(ReflectionUtils.returnsParameterisedType(returnsList, List.class));
    }

    public void testReturnsStringList() throws Exception
    {
        assertFalse(ReflectionUtils.returnsParameterisedType(returnsList, List.class, String.class));
        assertFalse(ReflectionUtils.returnsParameterisedType(returnsStringList, List.class, Foo.class));
        assertFalse(ReflectionUtils.returnsParameterisedType(returnsStringList, Foo.class, String.class));
        assertTrue(ReflectionUtils.returnsParameterisedType(returnsStringList, List.class, String.class));
    }

    public void testReturnsStringListCollection() throws Exception
    {
        assertTrue(ReflectionUtils.returnsParameterisedType(returnsStringList, Collection.class, String.class));
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
}
