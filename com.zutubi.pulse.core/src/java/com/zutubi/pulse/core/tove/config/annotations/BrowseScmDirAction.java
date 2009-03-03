package com.zutubi.pulse.core.tove.config.annotations;

import com.zutubi.tove.annotations.FieldAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An action that allows a text field to be configured by browsing for a
 * directory in a project's SCM.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@FieldAction(filterClass = "com.zutubi.pulse.master.tove.config.project.ScmBrowsablePredicate", template = "actions/browse-scm-dir")
public @interface BrowseScmDirAction
{
}
