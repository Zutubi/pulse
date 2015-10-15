// dependency: ./namespace.js
// dependency: ./ajax.js
// dependency: ./ActionWindow.js
// dependency: ./ConfigTree.js
// dependency: ./ContextPanel.js
// dependency: ./CollectionPanel.js
// dependency: ./CompositePanel.js
// dependency: ./DeleteWindow.js
// dependency: ./Table.js
// dependency: ./TypeSelectPanel.js
// dependency: ./WizardWindow.js

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

            that.contentEl = $("#center-pane-content");

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

        getRootPath: function()
        {
            return this.configTree.getRootPath();
        },

        setPaths: function(rootPath, configPath)
        {
            var path = rootPath;
            if (configPath && configPath.length > 0)
            {
                path += "/" + configPath;
            }

            this.configTree.setRootPath(rootPath);
            this.configTree.selectConfig(configPath);
            this.loadContentPanes(path);
        },

        /**
         * Opens a new path, within our root.  Observers are notified.
         *
         * @param path new path to open
         * @param model (optional) data for the new path (if we already know it), if not specified
         *              the data will be loaded
         * @private
         */
        _openPath: function(path, model)
        {
            this.configTree.selectAbsolutePath(path);
            this.trigger(PATHSELECT, {path: path});
            if (model)
            {
                this._showContent(model);
            }
            else
            {
                this.loadContentPanes(path);
            }
        },

        applyDelta: function(delta)
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

        beginNavigation: function()
        {
            this._clearContent();
            kendo.ui.progress(this.contentEl, true);
        },

        endNavigation: function(error)
        {
            kendo.ui.progress(this.contentEl, false);
            if (error)
            {
                $('<p class="nav-error"></p>').appendTo(this.contentEl).text(error);
            }
        },

        loadContentPanes: function(path)
        {
            var that = this;

            that.path = path;

            Zutubi.admin.navigate("/api/config/" + path + "?depth=-1", [that, that.contextPanel], function(data)
            {
                if (data.length === 1)
                {
                    return that._showContent(data[0]);
                }
                else
                {
                    return "Unexpected result for config lookup, length = " + data.length;
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
                return "Unrecognised config kind: " + data.kind;
            }

            this.contextPanel.setData(this.path, this.data);
            return null;
        },

        _clearContent: function()
        {
            if (this.contentPanel)
            {
                this.contentPanel.destroy();
                this.contentPanel = null;
            }

            kendo.destroy(this.contentEl);
            this.contentEl.empty();
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
                that.applyDelta(e.delta);
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
                that._showWizard(data.type.targetType);
            });

            that.contentPanel.bind("action", jQuery.proxy(that._doAction, that));

            that.contentPanel.bind("reordered", function(e)
            {
                that.applyDelta(e.delta);
            });
        },

        _showTypeSelection: function(data)
        {
            var that = this;

            that.contentPanel = new Zutubi.admin.TypeSelectPanel({
                containerSelector: "#center-pane-content",
                type: data,
                path: that.path
            });

            that.contentPanel.bind("configure", function()
            {
                that._showWizard(data);
            });
        },

        _showWizard: function(type)
        {
            var that = this,
                window;

            window = new Zutubi.admin.WizardWindow({
                path: that.path,
                label: type.label,
                success: jQuery.proxy(that._wizardFinished, that)
            });

            window.show();
        },

        _wizardFinished: function(delta)
        {
            this.applyDelta(delta);
            this.loadContentPanes(this.path);
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
                executed: jQuery.proxy(that._handleActionResult, that, path)
            });

            actionWindow.show();
        },

        _executeDescendantAction: function(path, action)
        {

        },

        _executeAction: function(path, action)
        {
            var that = this;

            Zutubi.admin.ajax({
                type: "POST",
                maskAll: true,
                url: "/api/action/single/" + action.action + "/" + path,
                success: jQuery.proxy(that._handleActionResult, that, path),
                error: function (jqXHR)
                {
                    Zutubi.admin.reportError("Could not perform action: " + Zutubi.admin.ajaxError(jqXHR));
                }
            });
        },

        _handleActionResult: function(path, data)
        {
            if (data.success)
            {
                Zutubi.admin.reportSuccess(data.message);
            }
            else
            {
                Zutubi.admin.reportError(data.message);
            }

            if (data.newPath)
            {
                this.configTree.updatePath(data.newPath, data.model);
                if (data.newPath === this.path)
                {
                    this._showContent(data.model);
                }
                else
                {
                    this._openPath(data.newPath, data.model);
                }
            }
            else
            {
                this.configTree.updatePath(path, data.model);

                if (path === this.path)
                {
                    if (data.model)
                    {
                        this._showContent(data.model);
                    }
                    else
                    {
                        // The model was removed as part of the action.
                        this._openPath(this.configTree.longestMatchingSubpath(path));
                    }
                }
                else if (Zutubi.admin.parentPath(path) === this.path)
                {
                    // We are showing this item in a collection.
                    this.contentPanel.updateItem(Zutubi.admin.baseName(path), data.model);
                }
            }
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
                            that.applyDelta(delta);
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
