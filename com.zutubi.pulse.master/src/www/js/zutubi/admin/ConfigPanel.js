// dependency: ./namespace.js
// dependency: ./ConfigTree.js
// dependency: ./Form.js

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
                                                   '<p>Main pane.</p>' +
                                               '</div>' +
                                           '</div>' +
                                           '<div id="right-pane">' +
                                               '<div class="pane-content">' +
                                                   '<p>Help pane.</p>' +
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

            jQuery.ajax({
                type: "GET",
                url: window.baseUrl + "/api/config/" + path + "?depth=-1",
                dataType: "json",
                headers: {
                    Accept: "application/json; charset=utf-8",
                    "Content-Type": "application/json; charset=utf-8"
                },
                success: function (data)
                {
                    if (data.length === 1)
                    {
                        that.showContent(data[0]);
                    }
                    else
                    {
                        zaReportError("Unexpected result for config lookup, length = " + data.length);
                    }
                },
                error: function (jqXHR, textStatus)
                {
                    if (jqXHR.status === 401)
                    {
                        showLoginForm();
                    }

                    zaReportError("Could not load configuration: " + zaAjaxError(jqXHR));
                }
            });
        },

        showContent: function(data)
        {
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

        _showComposite: function(data)
        {
            var that = this;

            console.log("composite");
            console.dir(data);

            that.form = $("#center-pane-content").kendoZaForm({
                structure: data.form,
                values: data.properties
            }).data("kendoZaForm");

            that.form.bind("submit", function(e)
            {
                that._saveComposite(that.form.getValues());
            });
        },

        _showCollection: function(data)
        {
            console.log("collection");
            console.dir(data);
        },

        _showTypeSelection: function(data)
        {
            console.log("type select");
            console.dir(data);
        },

        _saveComposite: function(properties)
        {
            var that = this;

            that._coerce(properties);

            jQuery.ajax({
                type: "PUT",
                url: window.baseUrl + "/api/config/" + that.path + "?depth=-1",
                dataType: "json",
                headers: {
                    Accept: "application/json; charset=utf-8",
                    "Content-Type": "application/json; charset=utf-8"
                },
                data: JSON.stringify({properties: properties}),
                success: function (data)
                {
                    console.log('save succcess');
                    console.dir(data);
                },
                error: function (jqXHR, textStatus)
                {
                    var details;

                    if (jqXHR.status === 401)
                    {
                        showLoginForm();
                    }
                    else if (jqXHR.status == 422)
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

        _coerce: function(properties)
        {
            var i,
                propertyTypes = this.data.type.simpleProperties,
                propertyType;

            for (i = 0; i < propertyTypes.length; i++)
            {
                propertyType = propertyTypes[i];
                if (propertyType.shortType === "int")
                {
                    this._coerceInt(properties, propertyType.name);
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
        }
    });
}(jQuery));
