package com.zutubi.pulse.core.engine.api;

import com.zutubi.tove.annotations.Transient;

/**
 * Types that define the reference interface can be referenced anywhere within the
 * pulse file using the syntax ${name}, where name is the value returned by the getName
 * method.
 *
 * Examples:
 *
 * The property object implements reference, so it can be defined at the top of the file,
 * and then used later on. In this example, propertyA is defined as "someValue". Later,
 * propertyB is initialised with the value of propertyA. So, both propertyA and propertyB
 * end up with the same value - "someValue":
 *
 *  <pre>{@code <property name="propertyA" value="someValue"/>
 *
 *  <snip/>....
 *
 *  <property name="propertyB" value="${propertyA}"/>}</pre>
 *
 * This is a rather simple example that does not show the full benefits of references, namely
 * that anything can be a reference, not just a property with a string value.
 */
public interface Reference
{
    String getName();

    @Transient
    Object referenceValue();
}
