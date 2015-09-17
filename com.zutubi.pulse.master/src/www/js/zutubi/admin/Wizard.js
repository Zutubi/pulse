// dependency: ./namespace.js
// dependency: ./Form.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        PREVIOUS = "previous",
        NEXT = "next",
        FINISH = "finish",
        CANCEL = "cancel";

    Zutubi.admin.Wizard = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        events: [
            FINISH
        ],

        options: {
            name: "ZaWizard",
            template: '<ul class="k-wizard-step-index"></ul>' +
                      '<div class="k-wizard-type-select"><input></div>' +
                      '<div class="k-wizard-form"></div>',
            stepIndexTemplate: '<li>#: label #</li>'
        },

        _create: function()
        {
            var that = this,
                structure = that.options.structure,
                steps = structure.steps,
                i;

            that.steps = jQuery.map(steps, function(step)
            {
                return that._generateStep(step);
            });

            that.template = kendo.template(that.options.template);
            that.stepIndexTemplate = kendo.template(that.options.stepIndexTemplate);

            that.element.html(that.template({}));
            that.stepIndexElement = that.element.find(".k-wizard-step-index");
            if (steps.length === 1)
            {
                that.stepIndexElement.hide();
            }
            else
            {
                for (i = 0; i < steps.length; i++)
                {
                    that.stepIndexElement.append(that.stepIndexTemplate(steps[i]));
                }
            }

            that.typeSelectWrapper = that.element.find(".k-wizard-type-select");
            that.typeSelectDropDown = that.typeSelectWrapper.children("input").kendoDropDownList({
                change: jQuery.proxy(that._typeSelected, that)
            }).data("kendoDropDownList");

            that.formWrapper = that.element.find(".k-wizard-form");

            that._showStepAtIndex(0);
        },

        _generateStep: function(structure)
        {
            return {
                structure: structure,
                selectedTypeIndex: 0,
                valuesByType: jQuery.map(structure.types, function() { return {}; })
            };
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
                result[step.structure.key] = {
                    properties: step.valuesByType[step.selectedTypeIndex],
                    type: step.structure.types[step.selectedTypeIndex].type
                };
            });

            return result;
        },

        showValidationErrors: function(details)
        {
            var that = this,
                stepIndex = -1;

            jQuery.each(that.steps, function(index, step)
            {
                if (step.structure.key === details.key)
                {
                    stepIndex = index;
                    return false;
                }
            });

            if (stepIndex === -1)
            {
                Zutubi.admin.reportError("Unrecognisable validation failure in wizard");
            }
            else
            {
                that._showStepAtIndex(stepIndex);
                that.form.showValidationErrors(details);
            }
        },

        _showStepAtIndex: function(index)
        {
            var that = this,
                step = that.steps[index];

            this.currentStepIndex = index;
            this._updateTypeSelect(step.structure.types);
            this._showTypeAtIndex(step.selectedTypeIndex);
        },

        _updateTypeSelect: function(types)
        {
            var labels,
                dropDown = this.typeSelectDropDown,
                width;

            if (types.length > 1)
            {
                labels = jQuery.map(types, function(type) { return type.label; });
                dropDown.setDataSource(labels);
                width = dropDown.list.width() + 40;

                dropDown.list.width(width - 1);
                dropDown.element.closest(".k-widget").width(width);
                this.typeSelectWrapper.show();
            }
            else
            {
                this.typeSelectWrapper.hide();
            }
        },

        _typeSelected: function()
        {
            var that = this,
                step = that.steps[that.currentStepIndex],
                typeLabel = that.typeSelectDropDown.value();

            jQuery.each(step.structure.types, function(i, type)
            {
                if (type.label === typeLabel)
                {
                    that._showTypeAtIndex(i);
                    return false;
                }
            });
        },

        _showTypeAtIndex: function(index)
        {
            var that = this,
                stepIndex = that.currentStepIndex,
                step = that.steps[stepIndex],
                type = step.structure.types[index],
                lastIndex = that.steps.length - 1,
                submits = [];

            step.selectedTypeIndex = index;

            if (that.form)
            {
                that.form.element.empty();
                that.form.destroy();
            }

            if (stepIndex !== 0)
            {
                submits.push(PREVIOUS);
            }

            if (stepIndex !== lastIndex)
            {
                submits.push(NEXT);
            }
            else
            {
                submits.push(FINISH);
            }

            submits.push(CANCEL);

            that.form = that.formWrapper.kendoZaForm({
                structure: that._filterFields(type.type.form),
                values: step.valuesByType[index],
                submits: submits
            }).data("kendoZaForm");

            that.form.bind("submit", jQuery.proxy(that._formSubmitted, that));
        },

        _filterFields: function(formStructure)
        {
            return jQuery.extend({}, formStructure, {
                fields: jQuery.grep(formStructure.fields, function(field)
                {
                    return !field.parameters.hasOwnProperty("wizardIgnore");
                })
            });
        },

        _formSubmitted: function(e)
        {
            var submit = e.value,
                step = this.steps[this.currentStepIndex];

            if (submit === CANCEL)
            {
                this.trigger(CANCEL);
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

        _forward: function()
        {
            // FIXME kendo validate before stepping forward.
            if (this.currentStepIndex === this.steps.length - 1)
            {
                this.trigger(FINISH);
            }
            else
            {
                this._showStepAtIndex(this.currentStepIndex + 1);
            }
        }
    });

    ui.plugin(Zutubi.admin.Wizard);
}(jQuery));
