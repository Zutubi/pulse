// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./Table.js
// dependency: ./TemplateIcon.js

(function($)
{
    var Observable = kendo.Observable,
        CANCELLED = "cancelled",
        APPLIED = "applied",
        SAVED = "saved",
        NAVIGATE = "navigate",
        ns = ".kendoCompositePanel",
        CLICK = "click" + ns;

    Zutubi.admin.CompositePanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                composite = options.composite,
                writable = that._canWrite(composite),
                el;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#: id #" class="k-composite-panel">' +
                    '<span class="k-template-icon-wrapper" style="display: none"></span>' +
                    '<button class="k-composite-help-button"></button>' +
                    '<h1>#: label #</h1>' +
                    '<div style="display:none" class="k-state-wrapper">' +
                    '</div>' +
                    '<div id="#: id #-form"></div>' +
                    '<div style="display:none" id="#: id #-checkwrapper" class="k-check-wrapper">' +
                        '<h1>check configuration</h1>' +
                        '<p>click <em>check</em> below to test your configuration</p>' +
                        '<div id="#: id #-checkform">' +
                        '</div>' +
                    '</div>' +
                    '<div style="display:none" class="k-collapsed-collection-wrapper">' +
                    '</div>' +
                    '<div style="display:none" id="#: id #-upwrapper" class="k-up-wrapper">' +
                        '<a><span class="fa fa-chevron-left"/> return to list</a>' +
                    '</div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: "composite",
                        label: composite.label
                    }
                });

            that.view.render(options.container);

            if (composite.templateOwner)
            {
                that._renderTemplateIcon();
            }

            if (composite.state && composite.state.fields)
            {
                el = that.view.element.find(".k-state-wrapper");
                el.kendoZaPropertyTable({
                    id: "composite-state",
                    title: composite.state.label,
                    data: composite.state.fields
                }).data("kendoZaPropertyTable");
                el.show();
            }

            that.form = $("#composite-form").kendoZaForm({
                parentPath: Zutubi.config.parentPath(options.path),
                baseName: Zutubi.config.baseName(options.path),
                symbolicName: composite.type.symbolicName,
                structure: composite.type.form,
                values: composite.properties,
                dirtyChecking: composite.keyed,
                markRequired: composite.concrete,
                readOnly: !writable,
                submits: composite.keyed ? ["apply", "reset"] : ["save", "cancel"],
                docs: composite.type.docs
            }).data("kendoZaForm");

            that.helpButton = that.view.element.find(".k-composite-help-button").kendoZaHelpButton({
                form: that.form
            }).data("kendoZaHelpButton");

            if (composite.validationErrors)
            {
                that.form.showValidationErrors(composite.validationErrors);
            }

            that.form.bind("buttonClicked", jQuery.proxy(that._submitClicked, that));

            if (writable && composite.type.checkType)
            {
                $("#composite-checkwrapper").show();

                that.checkForm = $("#composite-checkform").kendoZaForm({
                    formName: "check",
                    symbolicName: composite.type.checkType.symbolicName,
                    structure: composite.type.checkType.form,
                    values: [],
                    submits: ["check"]
                }).data("kendoZaForm");

                that.checkForm.bind("buttonClicked", jQuery.proxy(that._checkClicked, that));
            }

            if (Zutubi.admin.hasCollapsedCollection(composite))
            {
                el = that.view.element.find(".k-collapsed-collection-wrapper");
                el.show();

                that.collapsedCollectionPanel = new Zutubi.admin.CollectionPanel({
                    container: el,
                    collection: composite.nested[0],
                    path: that.options.path + "/" + composite.nested[0].key
                });
            }

            if (!writable && !composite.keyed)
            {
                el = $("#composite-upwrapper");
                el.show();
                el.find("a").on(CLICK, function()
                {
                    that.trigger(CANCELLED);
                });
            }
        },

        events: [
            CANCELLED,
            APPLIED,
            SAVED,
            NAVIGATE
        ],

        destroy: function()
        {
            // FIXME moar destruction?
            $("#composite-upwrapper").find("a").off(ns);
            if (this.templateIcon)
            {
                this.templateIcon.destroy();
                this.templateIcon = null;
            }
            this.view.destroy();
        },

        _canWrite: function(composite)
        {
            var i;

            if (composite.actions)
            {
                for (i = 0; i < composite.actions.length; i++)
                {
                    if (composite.actions[i].action === "write")
                    {
                        return true;
                    }
                }
            }

            return false;
        },

        _renderTemplateIcon: function()
        {
            var that = this,
                composite = that.options.composite,
                templateIconWrapperEl = that.view.element.find(".k-template-icon-wrapper");

            templateIconWrapperEl.show();
            if (composite.templateOwner === composite.templateOriginator)
            {
                that.templateIcon = templateIconWrapperEl.kendoZaTemplateIcon({
                    spriteCssClass: "fa fa-arrow-circle-left",
                    items: [{
                        text: "first defined at this level of the hierarchy"
                    }]
                }).data("kendoZaTemplateIcon");
            }
            else
            {
                that.templateIcon = templateIconWrapperEl.kendoZaTemplateIcon({
                    spriteCssClass: "fa fa-arrow-circle-up",
                    items: [{
                        text: "inherited from " + kendo.htmlEncode(composite.templateOriginator),
                        action: "navigate",
                        owner: composite.templateOriginator
                    }],
                    select: function(e)
                    {
                        that.trigger(NAVIGATE, {owner: e.item.owner})
                    }
                }).data("kendoZaTemplateIcon");
            }
        },

        _submitClicked: function(e)
        {
            var that = this,
                composite,
                type,
                properties,
                hookFn;

            if (e.value === "cancel")
            {
                that.trigger(CANCELLED);
            }
            else
            {
                composite = that.options.composite;
                type = composite.type;
                properties = that.form.getValues();
                Zutubi.config.coerceProperties(properties, type.simpleProperties);

                hookFn = Zutubi.config.saveHookForType(type.symbolicName);
                hookFn({
                    path: that.options.path,
                    composite: composite,
                    properties: properties,
                    success: function(delta)
                    {
                        that.trigger(composite.keyed ? APPLIED : SAVED, {delta: delta});
                    },
                    invalid: function(validationErrors)
                    {
                        that.form.showValidationErrors(validationErrors);
                    },
                    cancel: function()
                    {
                        if (composite.keyed)
                        {
                            that.form.resetValues();
                        }
                        else
                        {
                            that.trigger(CANCELLED);
                        }
                    }
                });
            }
        },

        _checkClicked: function()
        {
            Zutubi.config.checkConfig(this.options.path, this.options.composite.type, this.form, this.checkForm);
        }
    });
}(jQuery));
