package com.zutubi.pulse.core.tove.config.annotations;

import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.tove.annotations.Handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An action that allows a text field to be configured by browsing for a
 * file in a project's SCM.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@FieldAction(filterClass = "com.zutubi.pulse.master.tove.config.project.ScmBrowsablePredicate", template = "actions/browse-scm-file")
@Handler(className = "com.zutubi.pulse.master.tove.config.project.BrowseScmFileAnnotationHandler")
public @interface BrowseScmFileAction
{
    /**
     * Indicates an (optional) property that should be used as the base
     * directory for browsing (i.e. if another field indicates a working
     * directory, the file path should be browsed from that base rather
     * than the SCM root).
     *
     * @return the name of the field that contains the base directory
     *         to browse from, if any
     */
    String baseDirField() default "";
}
