package com.zutubi.tove.annotations;

/**
 * Defines constants naming annotation handler types.
 */
public interface DefaultAnnotationHandlers
{
    String HANDLER_PACKAGE    = "com.zutubi.tove.ui.forms";

    String FIELD_ACTION       = HANDLER_PACKAGE + ".FieldActionAnnotationHandler";
    String FIELD_SCRIPT       = HANDLER_PACKAGE + ".FieldScriptAnnotationHandler";
    String FIELD              = HANDLER_PACKAGE + ".FieldAnnotationHandler";
    String OPTION             = HANDLER_PACKAGE + ".OptionAnnotationHandler";
    String REFERENCE          = HANDLER_PACKAGE + ".ReferenceAnnotationHandler";
    String WIZARD_IGNORE      = HANDLER_PACKAGE + ".WizardIgnoreAnnotationHandler";
}
