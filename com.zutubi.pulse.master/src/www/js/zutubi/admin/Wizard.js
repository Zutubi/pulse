// dependency: ./namespace.js
// dependency: zutubi/config/package.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        PREVIOUS = "previous",
        NEXT = "next",
        CANCEL = "cancel",
        FINISH = "finish",
        POSTING = "posting",
        POSTED = "posted",
        FINISHED = "finished",
        CANCELLED = "cancelled",
        KEY_HIERARCHY = "meta.hierarchy",
        PARAMETER_IGNORE = "wizardIgnore",
        PARAMETER_NO_INHERIT = "noInherit",

    WizardStep = function(config)
    {
        this.kind = config.kind;
        this.key = config.key;
        this.label = config.label;
        this.selectedTypeIndex = 0;

        if (config.kind === "custom")
        {
            this.types = [{
                label: "",
                form: config.form,
                docs: config.docs,
                simpleProperties: []
            }];

            this.valuesByType = [config.formDefaults];
        }
        else if(config.kind === "typed")
        {
            config.types.sort(Zutubi.admin.labelCompare);

            this.types = jQuery.map(config.types, function(type)
            {
                return {
                    label: type.label,
                    help: type.help,
                    symbolicName: type.type.symbolicName,
                    form: type.type.form,
                    checkType: type.type.checkType,
                    docs: type.type.docs,
                    simpleProperties: type.type.simpleProperties,
                    filter: type.filter
                };
            });

            if (config.defaultType)
            {
                this.selectedTypeIndex = this.indexOfType(config.defaultType);
            }

            this.valuesByType = jQuery.map(config.types, function(type)
            {
                return type.type.simplePropertyDefaults || {};
            });
        }
        else
        {
            Zutubi.core.reportWarning("Unknown wizard step type '" + config.kind + "'");
        }
    };

    WizardStep.prototype = {
        applyDefaults: function(composite)
        {
            var i,
                type,
                fields,
                field,
                values = {};

            for (i = 0; i < this.types.length; i++)
            {
                type = this.types[i];
                if (type.symbolicName === composite.type.symbolicName)
                {
                    values = this.valuesByType[i];
                    break;
                }
            }

            if (i < this.types.length)
            {
                this.types = [type];
                this.selectedTypeIndex = 0;
                fields = type.form.fields;
                for (i = 0; i < fields.length; i++)
                {
                    field = fields[i];
                    if (composite.properties.hasOwnProperty(field.name) && this._isInheritable(field))
                    {
                        values[field.name] = composite.properties[field.name];
                    }
                }
                this.valuesByType = [values];
            }
        },

        _isInheritable: function(field)
        {
            return !field.parameters.hasOwnProperty(PARAMETER_IGNORE) && !field.parameters.hasOwnProperty(PARAMETER_NO_INHERIT);
        },

        getValue: function()
        {
            var type = this.types[this.selectedTypeIndex],
                value = {
                    kind: "composite",
                    properties: this.valuesByType[this.selectedTypeIndex]
                };

            Zutubi.config.coerceProperties(value.properties, type.simpleProperties);

            if (type.symbolicName)
            {
                value.type = {symbolicName: type.symbolicName};
            }

            return value;
        },

        requiresValidation: function()
        {
            return this.kind === "typed";
        },

        indexOfTypeLabel: function(label)
        {
            return this.indexOfTypeProperty("label", label);
        },

        indexOfType: function(symbolicName)
        {
            return this.indexOfTypeProperty("symbolicName", symbolicName);
        },

        indexOfTypeProperty: function(name, value)
        {
            var index = 0;

            jQuery.each(this.types, function(i, type)
            {
                if (type[name] === value)
                {
                    index = i;
                    return false;
                }
            });

            return index;
        },

        validTypeLabels: function(typesByKey)
        {
            var validTypes = jQuery.grep(this.types, function(type)
            {
                if (type.filter && typesByKey.hasOwnProperty(type.filter.stepKey))
                {
                    return jQuery.inArray(typesByKey[type.filter.stepKey], type.filter.compatibleTypes) >= 0;
                }
                else
                {
                    return true;
                }
            });

            return jQuery.map(validTypes, function(type)
            {
                return type.label;
            });
        },

        requiresConfig: function(typesByKey)
        {
            var validTypes = this.validTypeLabels(typesByKey),
                formStructure,
                i;

            if (validTypes.length === 0)
            {
                return false;
            }
            else if (validTypes.length === 1)
            {
                formStructure = this.types[this.indexOfTypeLabel(validTypes[0])].form;
                for (i = 0; i < formStructure.fields.length; i++)
                {
                    if (!formStructure.fields[i].parameters.hasOwnProperty(PARAMETER_IGNORE))
                    {
                        return true;
                    }
                }

                return false;
            }
            else
            {
                return true;
            }
        },

        ignoredFieldNames: function()
        {
            var formStructure = this.types[this.selectedTypeIndex].form;

            return jQuery.map(jQuery.grep(formStructure.fields, function(field)
            {
                return field.parameters.hasOwnProperty(PARAMETER_IGNORE);
            }), function(field)
            {
                return field.name;
            });
        },

        filteredForm: function()
        {
            var formStructure = this.types[this.selectedTypeIndex].form,
                ignoreFieldNames = this.ignoredFieldNames();

            return jQuery.extend({}, formStructure, {
                fields: jQuery.grep(formStructure.fields, function(field)
                {
                    return ignoreFieldNames.indexOf(field.name) < 0;
                })
            });
        }
    };

    Zutubi.admin.Wizard = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        events: [
            POSTING,
            POSTED,
            FINISHED
        ],

        options: {
            name: "ZaWizard",
            template: '<ul class="k-wizard-step-index"></ul>' +
                      '<div class="k-wizard-type-select">' +
                          'Select type: <input>' +
                          '<div class="k-wizard-type-help"></div>' +
                      '</div>' +
                      '<div class="k-wizard-form"></div>' +
                      '<div class="k-wizard-check">' +
                          '<p>click <em>check</em> below to test your configuration</p>' +
                          '<div class="k-wizard-check-form"></div>' +
                      '</div>',
            stepIndexTemplate: '<li>#: label #</li>',
            markRequired: true
        },

        _create: function()
        {
            var that = this,
                structure = that.options.structure,
                steps = structure.steps;

            that.steps = jQuery.map(steps, function(step)
            {
                return new WizardStep(step);
            });

            that.template = kendo.template(that.options.template);
            that.stepIndexTemplate = kendo.template(that.options.stepIndexTemplate);

            that.element.html(that.template({}));
            that.stepIndexElement = that.element.find(".k-wizard-step-index");
            that._renderStepIndex();

            that._renderHelpButton();

            that.typeSelectWrapper = that.element.find(".k-wizard-type-select");
            that.typeSelectDropDown = that.typeSelectWrapper.children("input").kendoDropDownList({
                change: jQuery.proxy(that._typeSelected, that)
            }).data("kendoDropDownList");
            that.typeSelectHelp = that.typeSelectWrapper.children(".k-wizard-type-help");

            that.formWrapper = that.element.find(".k-wizard-form");
            that.checkWrapper = that.element.find(".k-wizard-check");
            that.checkFormWrapper = that.element.find(".k-wizard-check-form");

            that._showStepAtIndex(0);
        },

        _renderHelpButton: function()
        {
            var window = this.element.closest(".k-window"),
                button;

            if (window.length > 0)
            {
                button = $('<button class="k-window-help-button"></button>');
                window.append(button);
                this.helpButton = button.kendoZaHelpButton({}).data("kendoZaHelpButton");
            }
        },

        _renderStepIndex: function()
        {
            var typesByKey = this._typesByKey(),
                i,
                step,
                stepElement;

            this.stepIndexElement.empty();
            if (this.steps.length === 1)
            {
                this.stepIndexElement.hide();
            }
            else
            {
                for (i = 0; i < this.steps.length; i++)
                {
                    step = this.steps[i];
                    stepElement = $(this.stepIndexTemplate(step));
                    this.stepIndexElement.append(stepElement);

                    if (!step.requiresConfig(typesByKey))
                    {
                        stepElement.hide();
                    }
                }
            }
        },

        destroy: function()
        {
            var that = this;

            Widget.fn.destroy.call(that);
            kendo.destroy(that.element);

            that.element = null;
        },

        getValue: function()
        {
            var that = this,
                result = {};

            jQuery.each(that.steps, function(index, step)
            {
                result[step.key] = step.getValue();
            });

            return result;
        },

        _indexOfStepWithKey: function(key)
        {
            var stepIndex = -1;

            jQuery.each(this.steps, function (index, step)
            {
                if (step.key === key)
                {
                    stepIndex = index;
                    return false;
                }
            });

            return stepIndex;
        },

        showValidationErrors: function(details, stepIndex)
        {
            var that = this;

            if (stepIndex === -1)
            {
                stepIndex = that._indexOfStepWithKey(details.key);
            }

            if (stepIndex === -1)
            {
                Zutubi.core.reportError("Unrecognisable validation failure in wizard");
            }
            else
            {
                that._showStepAtIndex(stepIndex);
                that.form.showValidationErrors(details.validationErrors);
            }
        },

        _stashValuesAndCleanupForm: function()
        {
            var form = this.form,
                checkForm = this.checkForm,
                step;

            if (form)
            {
                step = this.steps[this.currentStepIndex];
                step.valuesByType[step.selectedTypeIndex] = form.getValues();

                form.element.empty();
                form.destroy();
                this.form = null;
                if (this.helpButton)
                {
                    this.helpButton.setForm(null);
                }
            }

            if (checkForm)
            {
                checkForm.element.empty();
                checkForm.destroy();
                this.checkForm = null;
            }
        },

        _typesByKey: function()
        {
            var i,
                step,
                types = {};

            for (i = 0; i < this.currentStepIndex; i++)
            {
                step = this.steps[i];
                if (step.kind === "typed")
                {
                    types[step.key] = step.types[step.selectedTypeIndex].symbolicName;
                }
            }

            return types;
        },

        _showStepAtIndex: function(index, back)
        {
            var that = this,
                step = that.steps[index],
                items,
                typesByKey,
                validTypeLabels;

            this._stashValuesAndCleanupForm();

            this.currentStepIndex = index;

            this._renderStepIndex();
            items = that.stepIndexElement.children("li");
            items.removeClass("active");
            items.eq(index).addClass("active");

            typesByKey = that._typesByKey();

            if (step.key === "defaults" && this.steps[this._indexOfStepWithKey("")].types[0].symbolicName === "zutubi.projectConfig")
            {
                // This is a pure hack to detect one special case.  If we have more special
                // cases it would be worth generalising.
                this._filterProjectDefaultsStep(step);
            }

            if (step.requiresConfig(typesByKey))
            {
                validTypeLabels = step.validTypeLabels(typesByKey);
                if (jQuery.inArray(step.types[step.selectedTypeIndex].label, validTypeLabels) < 0) {
                    step.selectedTypeIndex = step.indexOfTypeLabel(validTypeLabels[0]);
                }

                this._updateTypeSelect(validTypeLabels);
                this._showTypeAtIndex(step.selectedTypeIndex);
            }
            else if (back)
            {
                this._back();
            }
            else
            {
                this._forward();
            }
        },

        _updateTypeSelect: function(labels)
        {
            var dropDown = this.typeSelectDropDown;

            if (labels.length > 1)
            {
                dropDown.setDataSource(labels);
                this.typeSelectWrapper.show();
            }
            else
            {
                this.typeSelectWrapper.hide();
            }
        },

        _typeSelected: function()
        {
            var step = this.steps[this.currentStepIndex],
                typeLabel = this.typeSelectDropDown.value();

            this._showTypeAtIndex(step.indexOfTypeLabel(typeLabel));
        },

        _shouldMarkRequired: function()
        {
            if (this.steps[0].key === KEY_HIERARCHY)
            {
                return this.steps[0].getValue().properties.isTemplate === false;
            }
            else
            {
                return this.options.markRequired;
            }
        },

        _showTypeAtIndex: function(index)
        {
            var that = this,
                stepIndex = that.currentStepIndex,
                step = that.steps[stepIndex],
                type = step.types[index],
                lastIndex = that.steps.length - 1,
                submits = [],
                defaultSubmit;

            this._stashValuesAndCleanupForm();

            step.selectedTypeIndex = index;
            this.typeSelectDropDown.value(step.types[index].label);
            if (type.help)
            {
                that.typeSelectHelp.html(kendo.htmlEncode(type.help));
                that.typeSelectHelp.show();
            }
            else
            {
                that.typeSelectHelp.empty();
                that.typeSelectHelp.hide();
            }

            if (stepIndex !== 0)
            {
                submits.push(PREVIOUS);
            }

            if (stepIndex !== lastIndex)
            {
                submits.push(NEXT);
                defaultSubmit = NEXT;
            }
            else
            {
                submits.push(FINISH);
                defaultSubmit = FINISH;
            }

            submits.push(CANCEL);

            that.form = that.formWrapper.kendoZaForm({
                parentPath: that.options.path,
                symbolicName: type.symbolicName,
                structure: step.filteredForm(),
                markRequired: that._shouldMarkRequired(),
                values: step.valuesByType[index],
                submits: submits,
                defaultSubmit: defaultSubmit,
                docs: type.docs
            }).data("kendoZaForm");

            that.form.bind("buttonClicked", jQuery.proxy(that._formSubmitted, that));
            if (that.helpButton)
            {
                that.helpButton.setForm(that.form);
            }

            if (type.checkType)
            {
                that.checkForm = that.checkFormWrapper.kendoZaForm({
                    formName: "check",
                    symbolicName: type.checkType.symbolicName,
                    structure: type.checkType.form,
                    values: [],
                    submits: ["check"]
                }).data("kendoZaForm");

                that.checkForm.bind("buttonClicked", jQuery.proxy(that._checkClicked, that));

                that.checkWrapper.show();
            }
            else
            {
                that.checkWrapper.hide();
            }

            that.type = type;
        },

        _formSubmitted: function(e)
        {
            var submit = e.value,
                step = this.steps[this.currentStepIndex];

            if (submit === CANCEL)
            {
                this.trigger(CANCELLED);
            }
            else
            {
                step.valuesByType[step.selectedTypeIndex] = this.form.getValues();

                if (submit === PREVIOUS)
                {
                    this._back();
                }
                else
                {
                    this._forward();
                }
            }
        },

        _back: function()
        {
            if (this.currentStepIndex > 0)
            {
                this._showStepAtIndex(this.currentStepIndex - 1, true);
            }
        },

        _mask: function(mask)
        {
            kendo.ui.progress(this.element.closest(".k-widget"), mask);
        },

        _handleError: function(jqXHR, stepIndex)
        {
            var details = Zutubi.config.getValidationErrors(jqXHR);

            if (details)
            {
                this.showValidationErrors(details, stepIndex);
            }
            else
            {
                Zutubi.core.reportError("Error stepping forward: " + Zutubi.core.ajaxError(jqXHR));
            }
        },

        _findNested: function(parent, key)
        {
            var i, nested;

            for (i = 0; i < parent.nested.length; i++)
            {
                nested = parent.nested[i];
                if (nested.key === key)
                {
                    return nested;
                }
            }

            return null;
        },

        _applyParentTemplate: function(parent)
        {
            var i, step, stepParent;

            this.parentTemplate = parent;

            for (i = 0; i < this.steps.length; i++)
            {
                step = this.steps[i];
                if (step.key === "")
                {
                    step.applyDefaults(parent);
                }
                else
                {
                    stepParent = this._findNested(parent, step.key);
                    if (stepParent)
                    {
                        step.applyDefaults(stepParent);
                    }
                }
            }
        },

        _forward: function()
        {
            var that = this,
                step = that.steps[that.currentStepIndex];

            if (that.currentStepIndex === that.steps.length - 1)
            {
                that._finish();
            }
            else if (step.key === KEY_HIERARCHY)
            {
                // We need to fetch the template parent to apply restrictions/defaults from it.
                that._mask(true);

                Zutubi.core.ajax({
                    type: "GET",
                    url: "/api/config/" + Zutubi.config.encodePath(Zutubi.config.parentPath(that.options.path) + "/" + step.getValue().properties.parentTemplate) + "?depth=2",
                    success: function (data)
                    {
                        that._mask(false);
                        if (data.length === 1)
                        {
                            that._applyParentTemplate(data[0]);
                        }

                        that._showStepAtIndex(that.currentStepIndex + 1);
                    },
                    error: function (jqXHR)
                    {
                        that._mask(false);
                        that._handleError(jqXHR, that.currentStepIndex);
                    }
                });
            }
            else if (step.requiresValidation())
            {
                that._mask(true);

                Zutubi.core.ajax({
                    type: "POST",
                    url: "/api/action/validate/" + Zutubi.config.encodePath(that.options.path),
                    data: {
                        ignoredFields: step.ignoredFieldNames(),
                        composite: step.getValue(),
                        concrete: that._shouldMarkRequired()
                    },
                    success: function ()
                    {
                        that._mask(false);
                        that._showStepAtIndex(that.currentStepIndex + 1);
                    },
                    error: function (jqXHR)
                    {
                        that._mask(false);
                        that._handleError(jqXHR, that.currentStepIndex);
                    }
                });
            }
            else
            {
                that._showStepAtIndex(that.currentStepIndex + 1);
            }
        },

        _finish: function()
        {
            var that = this,
                wizardData = that.getValue();

            that.trigger(POSTING);

            Zutubi.core.ajax({
                type: "POST",
                url: "/api/wizard/" + Zutubi.config.encodePath(that.options.path),
                data: wizardData,
                success: function (data)
                {
                    that.trigger(POSTED);
                    that.trigger(FINISHED, {delta: data});
                },
                error: function (jqXHR)
                {
                    that.trigger(POSTED);
                    that._handleError(jqXHR, -1);
                }
            });
        },

        _checkClicked: function()
        {
            Zutubi.config.checkConfig(this.options.path, this.type, this.form, this.checkForm);
        },

        // From here down lies project wizard specific hacks.

        _hasTriggerOfType: function(project, symbolicName)
        {
            var triggers = this._findNested(project, "triggers"),
                i;

            if (triggers && triggers.nested)
            {
                triggers = triggers.nested;
                for (i = 0; i < triggers.length; i++)
                {
                    if (triggers[i].type.symbolicName === symbolicName)
                    {
                        return true;
                    }
                }
            }

            return false;
        },

        _ignoreFieldNamed: function(form, fieldName)
        {
            var i,
                field;

            for (i = 0; i < form.fields.length; i++)
            {
                field = form.fields[i];
                if (field.name === fieldName)
                {
                    if (field.parameters)
                    {
                        field.parameters = {};
                    }

                    field.parameters[PARAMETER_IGNORE] = true;
                }
            }
        },

        _filterProjectDefaultsStep: function(step)
        {
            var parentProject = this.parentTemplate,
                form = step.types[0].form,
                values = step.valuesByType[0],
                concrete = this.steps[0].getValue().properties.isTemplate === false,
                ignoreDefaultRecipe = true,
                projectType,
                typeStep,
                recipes,
                stages;

            if (this._hasTriggerOfType(parentProject, "zutubi.scmTriggerConfig"))
            {
                this._ignoreFieldNamed(form, "addScmTrigger");
            }

            if (this._hasTriggerOfType(parentProject, "zutubi.dependentBuildTriggerConfig"))
            {
                this._ignoreFieldNamed(form, "addDependenciesTrigger");
            }

            projectType = this._findNested(parentProject, "type");
            if (projectType && projectType.type.symbolicName === "zutubi.multiRecipeTypeConfig")
            {
                recipes = this._findNested(projectType, "recipes");
                if (!recipes || recipes.length === 0)
                {
                    ignoreDefaultRecipe = false;
                }
            }
            else
            {
                typeStep = this.steps[this._indexOfStepWithKey("type")];
                if (typeStep.getValue().type.symbolicName === "zutubi.multiRecipeTypeConfig")
                {
                    ignoreDefaultRecipe = false;
                }
            }

            if (ignoreDefaultRecipe)
            {
                this._ignoreFieldNamed(form, "addDefaultRecipe");
                this._ignoreFieldNamed(form, "recipeName");
            }
            else
            {
                values.addDefaultRecipe = concrete;
            }

            stages = this._findNested(parentProject, "stages");
            if (stages && stages.nested && stages.nested.length > 0)
            {
                this._ignoreFieldNamed(form, "addDefaultStage");
                this._ignoreFieldNamed(form, "stageName");
            }
            else
            {
                values.addDefaultStage = concrete;
            }
        }
    });

    ui.plugin(Zutubi.admin.Wizard);
}(jQuery));
