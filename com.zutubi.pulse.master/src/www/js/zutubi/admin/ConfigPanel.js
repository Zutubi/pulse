// dependency: ./namespace.js
// dependency: ./ajax.js
// dependency: ./ConfigTree.js
// dependency: ./CollectionPanel.js
// dependency: ./Form.js
// dependency: ./Table.js
// dependency: ./Wizard.js

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
                    { collapsible: true, size: "350px" },
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

        destroy: function()
        {
            // FIXME kendo need we do more?
            this.view.destroy();
        },

        setPaths: function(rootPath, configPath)
        {
            this.configTree.setRootPath(rootPath);
            this.configTree.selectConfig(configPath);
            this.loadContentPanes(rootPath + "/" + configPath);
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

            if (this.collection)
            {
                this.collection.destroy();
                this.collection = null;
            }

            if (this.wizard)
            {
                this.wizard.destroy();
                this.wizard = null;
            }

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

            that.collection = new Zutubi.admin.CollectionPanel({
                containerSelector: "#center-pane-content",
                collection: data
            });

            that.collection.bind("add", function()
            {
                that._showWizard();
            });

            that.collection.bind("reorder", function()
            {
                that._setCollectionOrder(that.collection.table.getOrder());
            });
        },

        _showTypeSelection: function(data)
        {
            var that = this,
                link = $('<a>create a new thing</a>');
            link.on("click", function(e)
            {
                e.preventDefault();
                that._showWizard();
            });

            $("#center-pane-content").append(link);
        },

        _applyDelta: function(delta)
        {
            this.configTree.applyDelta(delta);
            if (delta.renamedPaths && delta.renamedPaths.hasOwnProperty(this.path))
            {
                newPath = delta.renamedPaths[this.path];
                replaceConfigPath(newPath);
                this.path = newPath;
            }
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
                    var newPath;

                    console.log('save succcess');
                    console.dir(data);

                    that._applyDelta(data);
                    that._clearContent();
                    that._showComposite(data.models[that.path]);
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
                data: {
                    kind: "collection",
                    nested: jQuery.map(order, function(key)
                            {
                                return {
                                    kind: "composite",
                                    key: key
                                };
                            })
                },
                success: function (data)
                {
                    console.log('set order succcess');
                    console.dir(data);
                    that._applyDelta(data);
                },
                error: function (jqXHR)
                {
                    zaReportError("Could not save order: " + zaAjaxError(jqXHR));
                }
            });
        },

        _showWizard: function()
        {
            var that = this;

            Zutubi.admin.ajax({
                type: "GET",
                url: "/api/wizard/" + that.path,
                success: function (data)
                {
                    console.log('wizard');
                    console.dir(data);

                    that._renderWizard(data);
                },
                error: function (jqXHR)
                {
                    zaReportError("Could not get wizard information: " + zaAjaxError(jqXHR));
                }
            });
        },

        _renderWizard: function(data)
        {
            var that = this,
                contentEl = $("#center-pane-content"),
                wizardEl = $("<div></div>");

            that._clearContent();

            contentEl.append(wizardEl);
            that.wizard = wizardEl.kendoZaWizard({
                structure: data
            }).data("kendoZaWizard");

            that.wizard.bind("finish", function()
            {
                var wizardData = that.wizard.getValue();
                jQuery.each(wizardData, function(property, data)
                {
                    data.kind = "composite";
                    that._coerce(data.properties, data.type.simpleProperties);
                    data.type = { symbolicName: data.type.symbolicName };
                });

                Zutubi.admin.ajax({
                    type: "POST",
                    url: "/api/wizard/" + that.path,
                    data: wizardData,
                    success: function (data)
                    {
                        console.log('wizard posted');
                        that.loadContentPanes(that.path);
                    },
                    error: function (jqXHR)
                    {
                        var details;

                        if (jqXHR.status === 422)
                        {
                            try
                            {
                                details = JSON.parse(jqXHR.responseText);
                                console.dir(details);
                                if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                                {
                                    that.wizard.showValidationErrors(details);
                                    return;
                                }
                            }
                            catch(e)
                            {
                                // Do nothing.
                                console.dir(e);
                            }
                        }

                        zaReportError("Could not finish wizard: " + zaAjaxError(jqXHR));
                    }
                });
            });

            that.wizard.bind("cancel", function()
            {
                that.loadContentPanes(that.path);
            });
        }
    });
}(jQuery));
