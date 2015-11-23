// dependency: ./namespace.js
// dependency: ./Form.js

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
                    symbolicName: type.type.symbolicName,
                    form: type.type.form,
                    checkType: type.type.checkType,
                    simpleProperties: type.type.simpleProperties,
                    filter: type.filter
                };
            });

            this.valuesByType = jQuery.map(config.types, function(type)
            {
                return type.type.simplePropertyDefaults || {};
            });
        }
        else
        {
            Zutubi.admin.reportWarning("Unknown wizard step type '" + config.kind + "'");
        }
    };

    WizardStep.prototype = {
        applyDefaults: function(composite)
        {
            var i;

            for (i = 0; i < this.types.length; i++)
            {
                if (this.types[i].symbolicName === composite.type.symbolicName)
                {
                    break;
                }
            }

            if (i < this.types.length)
            {
                this.types = [this.types[i]];
                this.valuesByType = [this.valuesByType[i]];
            }
        },

        getValue: function()
        {
            var type = this.types[this.selectedTypeIndex],
                value = {
                    kind: "composite",
                    properties: this.valuesByType[this.selectedTypeIndex]
                };

            Zutubi.admin.coerceProperties(value.properties, type.simpleProperties);

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
            var index = 0;

            jQuery.each(this.types, function(i, type)
            {
                if (type.label === label)
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
                      '<div class="k-wizard-type-select">Select type: <input></div>' +
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

            that.typeSelectWrapper = that.element.find(".k-wizard-type-select");
            that.typeSelectDropDown = that.typeSelectWrapper.children("input").kendoDropDownList({
                change: jQuery.proxy(that._typeSelected, that)
            }).data("kendoDropDownList");

            that.formWrapper = that.element.find(".k-wizard-form");
            that.checkWrapper = that.element.find(".k-wizard-check");
            that.checkFormWrapper = that.element.find(".k-wizard-check-form");

            that._showStepAtIndex(0);
        },

        _renderStepIndex: function()
        {
            var i = 0;

            this.stepIndexElement.empty();
            if (this.steps.length === 1)
            {
                this.stepIndexElement.hide();
            }
            else
            {
                for (i = 0; i < this.steps.length; i++)
                {
                    this.stepIndexElement.append(this.stepIndexTemplate(this.steps[i]));
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

        showValidationErrors: function(details, stepIndex)
        {
            var that = this;

            if (stepIndex === -1)
            {
                jQuery.each(that.steps, function (index, step)
                {
                    if (step.key === details.key)
                    {
                        stepIndex = index;
                        return false;
                    }
                });
            }

            if (stepIndex === -1)
            {
                Zutubi.admin.reportError("Unrecognisable validation failure in wizard");
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

        _showStepAtIndex: function(index)
        {
            var that = this,
                step = that.steps[index],
                items = that.stepIndexElement.children("li"),
                validTypeLabels;

            this._stashValuesAndCleanupForm();

            items.removeClass("active");
            items.eq(index).addClass("active");

            this.currentStepIndex = index;
            validTypeLabels = step.validTypeLabels(that._typesByKey());
            if (jQuery.inArray(step.types[step.selectedTypeIndex].label, validTypeLabels) < 0)
            {
                step.selectedTypeIndex = step.indexOfTypeLabel(validTypeLabels[0]);
            }

            this._updateTypeSelect(validTypeLabels);
            this._showTypeAtIndex(step.selectedTypeIndex);
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
                structure: that._filterFields(type.form),
                markRequired: that._shouldMarkRequired(),
                values: step.valuesByType[index],
                submits: submits,
                defaultSubmit: defaultSubmit
            }).data("kendoZaForm");

            that.form.bind("buttonClicked", jQuery.proxy(that._formSubmitted, that));

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

        _ignoredFieldNames: function(formStructure)
        {
            return jQuery.map(jQuery.grep(formStructure.fields, function(field)
            {
                return field.parameters.hasOwnProperty("wizardIgnore");
            }), function(field)
            {
                return field.name;
            });
        },

        _filterFields: function(formStructure)
        {
            var ignoreFieldNames = this._ignoredFieldNames(formStructure);
            return jQuery.extend({}, formStructure, {
                fields: jQuery.grep(formStructure.fields, function(field)
                {
                    return ignoreFieldNames.indexOf(field.name) < 0;
                })
            });
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
                this._showStepAtIndex(this.currentStepIndex - 1);
            }
        },

        _mask: function(mask)
        {
            kendo.ui.progress(this.element.closest(".k-widget"), mask);
        },

        _handleError: function(jqXHR, stepIndex)
        {
            var details;

            if (jqXHR.status === 422)
            {
                try
                {
                    details = JSON.parse(jqXHR.responseText);
                    if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                    {
                        this.showValidationErrors(details, stepIndex);
                        return;
                    }
                }
                catch(e)
                {
                    // Do nothing.
                }
            }

            Zutubi.admin.reportError("Error stepping forward: " + Zutubi.admin.ajaxError(jqXHR));
        },

        _findNestedComposite: function(parent, key)
        {
            var i, nested;

            for (i = 0; i < parent.nested.length; i++)
            {
                nested = parent.nested[i];
                if (nested.key === key && nested.kind === "composite")
                {
                    return nested;
                }
            }

            return null;
        },

        _applyParentTemplate: function(parent)
        {
            var i, step, stepParent;

            for (i = 0; i < this.steps.length; i++)
            {
                step = this.steps[i];
                if (step.key === "")
                {
                    step.applyDefaults(parent);
                }
                else
                {
                    stepParent = this._findNestedComposite(parent, step.key);
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

                Zutubi.admin.ajax({
                    type: "GET",
                    url: "/api/config/" + Zutubi.admin.encodePath(Zutubi.admin.parentPath(that.options.path) + "/" + step.getValue().properties.parentTemplate) + "?depth=1",
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

                Zutubi.admin.ajax({
                    type: "POST",
                    url: "/api/action/validate/" + Zutubi.admin.encodePath(that.options.path),
                    data: {
                        ignoredFields: that._ignoredFieldNames(step.types[step.selectedTypeIndex].form),
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

            Zutubi.admin.ajax({
                type: "POST",
                url: "/api/wizard/" + Zutubi.admin.encodePath(that.options.path),
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
            Zutubi.admin.checkConfig(this.options.path, this.type, this.form, this.checkForm);
        }
    });

    ui.plugin(Zutubi.admin.Wizard);
}(jQuery));
