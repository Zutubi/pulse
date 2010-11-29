package com.zutubi.pulse.master.license;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Java 5 annotation for describing service layer licensing attributes.
 *
 * <p>
 * The <code>Licensed</code> annotation is used to define a list of licensing authorisations
 * attributes for business methods.
 * </p>
 * <p>
 * For example:
 * <pre>
 *     &#64;Licensed ({"canAddProject"})
 *     public void addProject(Project p);
 *
 *     &#64;Licensed ({"canAddUser"})
 *     public void newUser(String userName, String password);
 * </pre>
 * </p>
 *
 * @see LicenseHolder for the list of supported authorisations.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Licensed
{
    public String[] value();
}