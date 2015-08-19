// dependency: ./namespace.js
// dependency: ./ajax.js
// dependency: ./ConfigTree.js
// dependency: ./Form.js
// dependency: ./Table.js

(function($)
{
    var Observable = kendo.Observable,
        PATHSELECT = 'pathselect';

    Zutubi.admin.ConfigPanel = Observable.extend({
        init: function (containerSelector)
        {
            var that = this;

            Observable.fn.init.call(this);

            that.view = new kendo.View('<div id="outer-split" style="height: 100%; width: 100%">' +
                                           '<div id="left-pane">' +
                                               '<div id="config-tree" class="pane-content">' +
                                               '</div>' +
                                           '</div>' +
                                           '<div id="center-pane">' +
                                               '<div id="center-pane-content" class="pane-content">' +
                                               '</div>' +
                                           '</div>' +
                                           '<div id="right-pane">' +
                                               '<div class="pane-content">' +
                                               '</div>' +
                                           '</div>' +
                                       '</div>', {wrap: false});

            that.view.render($(containerSelector));

            $("#outer-split").kendoSplitter({
                panes: [
                    { collapsible: true, size: "250px" },
                    { collapsible: false },
                    { collapsible: true, size: "250px" }
                ]
            });

            that.configTree = $("#config-tree").kendoZaConfigTree().data("kendoZaConfigTree");
            that.configTree.bind("pathselect", function(e)
            {
                that.trigger(PATHSELECT, {path: e.path});
                that.loadContentPanes(e.path);
            });
        },

        events: [
            PATHSELECT
        ],

        setPath: function(path)
        {
            this.configTree.setRootPath(subpath(path, 0, 2));
            this.configTree.selectConfig(subpath(path, 2));
            this.loadContentPanes(path);
        },

        loadContentPanes: function(path)
        {
            var that = this;

            this.path = path;

            Zutubi.admin.ajax({
                type: "GET",
                url: "/api/config/" + path + "?depth=-1",
                success: function (data)
                {
                    if (data.length === 1)
                    {
                        that._showContent(data[0]);
                    }
                    else
                    {
                        zaReportError("Unexpected result for config lookup, length = " + data.length);
                    }
                },
                error: function (jqXHR)
                {
                    zaReportError("Could not load configuration: " + zaAjaxError(jqXHR));
                }
            });
        },

        _showContent: function(data)
        {
            this._clearContent();

            this.data = data;

            if (data.kind === "composite")
            {
                this._showComposite(data);
            }
            else if (data.kind === "collection")
            {
                this._showCollection(data);
            }
            else if (data.kind === "type-selection")
            {
                this._showTypeSelection(data);
            }
            else
            {
                zaReportError("Unrecognised config kind: " + data.kind);
            }
        },

        _clearContent: function()
        {
            var contentEl = $("#center-pane-content");

            this.form = null;
            this.checkForm = null;
            this.table = null;
            kendo.destroy(contentEl);
            contentEl.empty();
        },

        _showComposite: function(data)
        {
            var that = this,
                contentEl = $("#center-pane-content"),
                formEl = $("<div></div>"),
                checkFormEl;

            console.log("composite");
            console.dir(data);

            contentEl.append(formEl);
            that.form = formEl.kendoZaForm({
                structure: data.type.form,
                values: data.properties
            }).data("kendoZaForm");

            that.form.bind("submit", function(e)
            {
                that._saveComposite(that.form.getValues());
            });

            // FIXME kendo if the composite is not writable, don't show this
            if (data.type.checkType)
            {
                checkFormEl = $("<div></div>");

                contentEl.append("<h1>check</h1>");
                contentEl.append(checkFormEl);
                that.checkForm = checkFormEl.kendoZaForm({
                    formName: "check",
                    structure: data.type.checkType.form,
                    values: [],
                    submits: ["check"]
                }).data("kendoZaForm");

                that.checkForm.bind("submit", function(e)
                {
                    that._checkComposite(that.form.getValues(), that.checkForm.getValues());
                });
            }
        },

        _showCollection: function(data)
        {
            var that = this;

            console.log("collection");
            console.dir(data);

            that.table = $("#center-pane-content").kendoZaTable({
                structure: data.table,
                items: data.nested,
                allowSorting: data.type.ordered && jQuery.inArray("write", data.allowedActions)
            }).data("kendoZaTable");

            that.table.bind("reorder", function(e)
            {
                that._setCollectionOrder(that.table.getOrder());
            });
        },

        _showTypeSelection: function(data)
        {
            console.log("type select");
            console.dir(data);
        },

        _saveComposite: function(properties)
        {
            var that = this;

            that._coerce(properties, this.data.type.simpleProperties);

            Zutubi.admin.ajax({
                type: "PUT",
                url: "/api/config/" + that.path + "?depth=1",
                data: {kind: "composite", properties: properties},
                success: function (data)
                {
                    console.log('save succcess');
                    console.dir(data);
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
                                that.form.showValidationErrors(details);
                                return;
                            }
                        }
                        catch(e)
                        {
                            // Do nothing.
                        }
                    }

                    zaReportError("Could not save configuration: " + zaAjaxError(jqXHR));
                }
            });
        },

        _checkComposite: function(properties, checkProperties)
        {
            var that = this,
                type = this.data.type;

            that._coerce(properties, type.simpleProperties);
            that._coerce(checkProperties, type.checkType.simpleProperties);

            Zutubi.admin.ajax({
                type: "POST",
                url: "/api/action/check/" + that.path,
                data: {
                    main: {kind: "composite", properties: properties},
                    check: {kind: "composite", properties: checkProperties}
                },
                success: function (data)
                {
                    // FIXME kendo better to display these near the check button
                    if (data.success)
                    {
                        zaReportSuccess("configuration ok");
                    }
                    else
                    {
                        zaReportError(data.message || "check failed");
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

                    zaReportError("Could not check configuration: " + zaAjaxError(jqXHR));
                }
            });

        },

        _coerce: function(properties, propertyTypes)
        {
            var i,
                propertyType;

            if (propertyTypes)
            {
                for (i = 0; i < propertyTypes.length; i++)
                {
                    propertyType = propertyTypes[i];
                    if (propertyType.shortType === "int")
                    {
                        this._coerceInt(properties, propertyType.name);
                    }
                }
            }
        },

        _coerceInt: function(properties, name)
        {
            var value, newValue;
            if (properties.hasOwnProperty(name))
            {
                value = properties[name];
                if (value === "")
                {
                    newValue = null;
                }
                else
                {
                    newValue = Number(value);
                }

                properties[name] = newValue;
            }
        },

        _setCollectionOrder: function(order)
        {
            var that = this;

            Zutubi.admin.ajax({
                type: "PUT",
                url: "/api/config/" + that.path + "?depth=-1",
                data: {kind: "collection", nested: jQuery.map(order, function(key)
                {
                    return {kind: "composite", key: key};
                })},
                success: function (data)
                {
                    console.log('set order succcess');
                    console.dir(data);
                },
                error: function (jqXHR)
                {
                    zaReportError("Could not save order: " + zaAjaxError(jqXHR));
                }
            });
        }
    });
}(jQuery));
