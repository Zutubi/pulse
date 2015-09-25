// dependency: ./namespace.js
// dependency: ./ajax.js
// dependency: ./ConfigTree.js
// dependency: ./CollectionPanel.js
// dependency: ./CompositePanel.js
// dependency: ./DeleteWindow.js
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

        _openPath: function(path)
        {
            this.configTree.selectAbsolutePath(path);
            this.trigger(PATHSELECT, {path: path});
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
                        Zutubi.admin.reportError("Unexpected result for config lookup, length = " + data.length);
                    }
                },
                error: function (jqXHR)
                {
                    Zutubi.admin.reportError("Could not load configuration: " + Zutubi.admin.ajaxError(jqXHR));
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
                Zutubi.admin.reportError("Unrecognised config kind: " + data.kind);
            }
        },

        _clearContent: function()
        {
            var contentEl = $("#center-pane-content");

            if (this.contentPanel)
            {
                this.contentPanel.destroy();
                this.contentPanel = null;
            }

            kendo.destroy(contentEl);
            contentEl.empty();
        },

        _showComposite: function(data)
        {
            var that = this;

            console.log("composite");
            console.dir(data);

            that.contentPanel = new Zutubi.admin.CompositePanel({
                containerSelector: "#center-pane-content",
                composite: data,
                path: that.path
            });

            that.contentPanel.bind("save", function(e)
            {
                that._saveComposite(e.values);
            });

            that.contentPanel.bind("check", function(e)
            {
                that._checkComposite(e.values, e.checkValues);
            });
        },

        _showCollection: function(data)
        {
            var that = this;

            console.log("collection");
            console.dir(data);

            that.contentPanel = new Zutubi.admin.CollectionPanel({
                containerSelector: "#center-pane-content",
                collection: data
            });

            that.contentPanel.bind("add", function()
            {
                that._showWizard();
            });

            that.contentPanel.bind("action", function(e)
            {
                var path = that.path + "/" + e.key;

                if (e.action === "view")
                {
                    that._openPath(path);
                }
                else if (e.action === "delete")
                {
                    that._deleteConfig(path);
                }
            });

            that.contentPanel.bind("reorder", function(e)
            {
                that._setCollectionOrder(e.order);
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
            var newPath;

            this.configTree.applyDelta(delta);
            if (delta.renamedPaths && delta.renamedPaths.hasOwnProperty(this.path))
            {
                newPath = delta.renamedPaths[this.path];
                Zutubi.admin.replaceConfigPath(newPath);
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
                                that.contentPanel.showValidationErrors(details);
                                return;
                            }
                        }
                        catch(e)
                        {
                            // Do nothing.
                        }
                    }

                    Zutubi.admin.reportError("Could not save configuration: " + Zutubi.admin.ajaxError(jqXHR));
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
                                    that.contentPanel.showValidationErrors(details);
                                }
                                else
                                {
                                    that.contentPanel.showCheckValidationErrors(details);
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
                    Zutubi.admin.reportError("Could not save order: " + Zutubi.admin.ajaxError(jqXHR));
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
                    Zutubi.admin.reportError("Could not get wizard information: " + Zutubi.admin.ajaxError(jqXHR));
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
            that.contentPanel = wizardEl.kendoZaWizard({
                structure: data
            }).data("kendoZaWizard");

            that.contentPanel.bind("finish", function()
            {
                var wizardData = that.contentPanel.getValue();
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
                                    that.contentPanel.showValidationErrors(details);
                                    return;
                                }
                            }
                            catch(e)
                            {
                                // Do nothing.
                                console.dir(e);
                            }
                        }

                        Zutubi.admin.reportError("Could not finish wizard: " + Zutubi.admin.ajaxError(jqXHR));
                    }
                });
            });

            that.contentPanel.bind("cancel", function()
            {
                that.loadContentPanes(that.path);
            });
        },

        _deleteConfig: function(path)
        {
            var that = this,
                deleteWindow;

            deleteWindow = new Zutubi.admin.DeleteWindow({
                path: path,
                confirm: function()
                {
                    Zutubi.admin.ajax({
                        type: "DELETE",
                        url: "/api/config/" + path,
                        success: function (delta)
                        {
                            that._applyDelta(delta);
                            that._openPath(that.configTree.longestMatchingSubpath(path));
                        },
                        error: function (jqXHR)
                        {
                            Zutubi.admin.reportError("Could not delete configuration: " + Zutubi.admin.ajaxError(jqXHR));
                        }
                    });
                }
            });

            deleteWindow.show();
        }
    });
}(jQuery));
