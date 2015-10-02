// dependency: ./namespace.js
// dependency: ./ajax.js
// dependency: ./ActionWindow.js
// dependency: ./ConfigTree.js
// dependency: ./ContextPanel.js
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
                                               '<div id="right-pane-content" class="pane-content">' +
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

            that.contextPanel = $("#right-pane-content").kendoZaContextPanel().data("kendoZaContextPanel");
            that.contextPanel.bind("action", jQuery.proxy(that._doAction, that));
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

            this.contextPanel.setData(this.path, this.data);
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

            that.contentPanel = new Zutubi.admin.CompositePanel({
                containerSelector: "#center-pane-content",
                composite: data,
                path: that.path
            });

            that.contentPanel.bind("saved", function(e)
            {
                that._applyDelta(e.delta);
                that._clearContent();
                that._showComposite(e.delta.models[that.path]);
            });
        },

        _showCollection: function(data)
        {
            var that = this;

            that.contentPanel = new Zutubi.admin.CollectionPanel({
                containerSelector: "#center-pane-content",
                collection: data,
                path: that.path
            });

            that.contentPanel.bind("add", function()
            {
                that._showWizard();
            });

            that.contentPanel.bind("action", jQuery.proxy(that._doAction, that));

            that.contentPanel.bind("reordered", function(e)
            {
                that._applyDelta(e.delta);
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

        _showWizard: function()
        {
            var that = this;

            Zutubi.admin.ajax({
                type: "GET",
                url: "/api/wizard/" + that.path,
                success: function (data)
                {
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
                structure: data,
                path: that.path
            }).data("kendoZaWizard");

            that.contentPanel.bind("finished", function(e)
            {
                that.loadContentPanes(that.path);
            });

            that.contentPanel.bind("cancelled", function()
            {
                that.loadContentPanes(that.path);
            });
        },

        _doAction: function(e)
        {
            var action = e.action;

            if (action.action === "view")
            {
                this._openPath(e.path);
            }
            else if (action.action === "delete")
            {
                this._deleteConfig(e.path);
            }
            else if (action.descendant)
            {
                this._executeDescendantAction(e.path, action);
            }
            else if (action.inputRequired)
            {
                this._doActionWithInput(e.path, e.action);
            }
            else
            {
                this._executeAction(e.path, e.action);
            }
        },

        _doActionWithInput: function(path, action)
        {
            var that = this,
                actionWindow;

            actionWindow = new Zutubi.admin.ActionWindow({
                path: path,
                action: action,
                executed: function(data)
                {
                    if (data.success)
                    {
                        Zutubi.admin.reportSuccess(data.message);
                    }
                    else
                    {
                        Zutubi.admin.reportError(data.message);
                    }

                    if (data.delta)
                    {
                        that._applyDelta(data.delta)
                    }
                }
            });

            actionWindow.show();
        },

        _executeDescendantAction: function(path, action)
        {

        },

        _executeAction: function(path, action)
        {

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
