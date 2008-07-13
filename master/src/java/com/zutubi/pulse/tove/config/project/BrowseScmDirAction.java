package com.zutubi.pulse.tove.config.project;

import com.zutubi.config.annotations.FieldAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@FieldAction(filterClass = "com.zutubi.pulse.tove.config.project.ScmBrowsablePredicate", template = "actions/browse-scm-dir")
public @interface BrowseScmDirAction
{
}
