// dependency: ./namespace.js
// dependency: ./Table.js

(function($)
{
    var Observable = kendo.Observable,
        CANCELLED = "cancelled",
        SAVED = "saved";

    Zutubi.admin.CompositePanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                composite = options.composite;

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
                parentPath: Zutubi.admin.parentPath(options.path),
                baseName: Zutubi.admin.baseName(options.path),
                symbolicName: composite.type.symbolicName,
                structure: composite.type.form,
                values: composite.properties,
                dirtyChecking: composite.keyed,
                submits: composite.keyed ? ["apply", "reset"] : ["save", "cancel"]
            }).data("kendoZaForm");

            that.form.bind("submit", jQuery.proxy(that._submitClicked, that));

            // FIXME kendo if the composite is not writable, don't show this
            if (composite.type.checkType)
            {
                $("#composite-checkwrapper").show();

                that.checkForm = $("#composite-checkform").kendoZaForm({
                    formName: "check",
                    symbolicName: composite.type.checkType.symbolicName,
                    structure: composite.type.checkType.form,
                    values: [],
                    submits: ["check"]
                }).data("kendoZaForm");

                that.checkForm.bind("submit", jQuery.proxy(that._checkClicked, that));
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
        },

        events: [
            CANCELLED,
            SAVED
        ],

        destroy: function()
        {
            // FIXME moar destruction?
            this.view.destroy();
        },

        _submitClicked: function(e)
        {
            var that = this,
                properties;

            if (e.value === "cancel")
            {
                that.trigger(CANCELLED);
            }
            else
            {
                properties = that.form.getValues();
                Zutubi.admin.coerceProperties(properties, that.options.composite.type.simpleProperties);

                Zutubi.admin.ajax({
                    type: "PUT",
                    maskAll: true,
                    url: "/api/config/" + Zutubi.admin.encodePath(that.options.path) + "?depth=1",
                    data: {kind: "composite", properties: properties},
                    success: function(data)
                    {
                        console.log('save succcess');
                        console.dir(data);

                        that.trigger(SAVED, {delta: data});
                    },
                    error: function(jqXHR)
                    {
                        var details;

                        if (jqXHR.status === 422)
                        {
                            try
                            {
                                details = JSON.parse(jqXHR.responseText);
                                if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                                {
                                    that.form.showValidationErrors(details);
                                    return;
                                }
                            }
                            catch (e)
                            {
                                // Do nothing.
                            }
                        }

                        Zutubi.admin.reportError("Could not save configuration: " + Zutubi.admin.ajaxError(jqXHR));
                    }
                });
            }
        },

        _checkClicked: function()
        {
            var that = this,
                type = that.options.composite.type,
                properties = that.form.getValues(),
                checkProperties = that.checkForm.getValues();

            Zutubi.admin.coerceProperties(properties, type.simpleProperties);
            Zutubi.admin.coerceProperties(checkProperties, type.checkType.simpleProperties);

            Zutubi.admin.ajax({
                type: "POST",
                maskAll: true,
                url: "/api/action/check/" + Zutubi.admin.encodePath(that.options.path),
                data: {
                    main: {kind: "composite", properties: properties},
                    check: {kind: "composite", properties: checkProperties}
                },
                success: function (data)
                {
                    // FIXME kendo better to display these near the check button
                    if (data.success)
                    {
                        Zutubi.admin.reportSuccess("configuration ok");
                    }
                    else
                    {
                        Zutubi.admin.reportError(data.message || "check failed");
                    }
                },
                error: function (jqXHR)
                {
                    var details;

                    if (jqXHR.status === 422)
                    {
                        try
                        {
                            details = JSON.parse(jqXHR.responseText);
                            if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                            {
                                if (details.key === "main")
                                {
                                    that.form.showValidationErrors(details);
                                }
                                else
                                {
                                    that.checkForm.showValidationErrors(details);
                                }
                                return;
                            }
                        }
                        catch(e)
                        {
                            // Do nothing.
                        }
                    }

                    Zutubi.admin.reportError("Could not check configuration: " + Zutubi.admin.ajaxError(jqXHR));
                }
            });

        }
    });
}(jQuery));
