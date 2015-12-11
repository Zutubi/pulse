// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./Table.js

(function($)
{
    var Observable = kendo.Observable,
        CANCELLED = "cancelled",
        APPLIED = "applied",
        SAVED = "saved",
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
                    '<h1>#: label #</h1>' +
                    '<div id="#: id #-form"></div>' +
                    '<div style="display:none" id="#: id #-checkwrapper" class="k-check-wrapper">' +
                        '<h1>check configuration</h1>' +
                        '<p>click <em>check</em> below to test your configuration</p>' +
                        '<div id="#: id #-checkform">' +
                        '</div>' +
                    '</div>' +
                    '<div style="display:none" id="#: id #-collapsed-collection" class="k-collapsed-collection-wrapper">' +
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

            that.view.render($(options.containerSelector));

            that.form = $("#composite-form").kendoZaForm({
                parentPath: Zutubi.config.parentPath(options.path),
                baseName: Zutubi.config.baseName(options.path),
                symbolicName: composite.type.symbolicName,
                structure: composite.type.form,
                values: composite.properties,
                dirtyChecking: composite.keyed,
                markRequired: composite.concrete,
                readOnly: !writable,
                submits: composite.keyed ? ["apply", "reset"] : ["save", "cancel"]
            }).data("kendoZaForm");

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
                $("#composite-collapsed-collection").show();

                that.collapsedCollectionPanel = new Zutubi.admin.CollectionPanel({
                    containerSelector: "#composite-collapsed-collection",
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
            SAVED
        ],

        destroy: function()
        {
            // FIXME moar destruction?
            $("#composite-upwrapper").find("a").off(ns);
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
