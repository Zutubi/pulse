// dependency: ./namespace.js
// dependency: zutubi/core/package.js
// dependency: zutubi/config/package.js
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
        DELTA ='delta',
        NAVIGATE = 'navigate',
        PATHSELECT = 'pathselect';

    Zutubi.admin.ConfigPanel = Observable.extend({
        init: function (container)
        {
            var that = this;

            Observable.fn.init.call(this);

            that.view = new kendo.View('<div class="k-outer-split" style="height: 100%; width: 100%">' +
                                           '<div>' +
                                               '<div class="k-pane-content">' +
                                                   '<div class="k-view-switch"><button class="k-left"><span class="fa fa-angle-left"></span> hierarchy</button></div>' +
                                                   '<div class="k-config-tree"></div>' +
                                               '</div>' +
                                           '</div>' +
                                           '<div>' +
                                               '<div class="k-center-pane-content k-pane-content">' +
                                               '</div>' +
                                           '</div>' +
                                           '<div>' +
                                               '<div class="k-right-pane-content k-pane-content">' +
                                               '</div>' +
                                           '</div>' +
                                       '</div>', {wrap: false});

            that.view.render(container);

            that.view.element.kendoSplitter({
                panes: [
                    { collapsible: true, size: "350px" },
                    { collapsible: false },
                    { collapsible: true, size: "250px" }
                ]
            });

            that.hierarchyButton = that.view.element.find(".k-view-switch button").kendoButton({
                click: jQuery.proxy(this._openHierarchy, this)
            });
            that.configTree = that.view.element.find(".k-config-tree").kendoZaConfigTree().data("kendoZaConfigTree");
            that.configTree.bind("pathselect", function(e)
            {
                that.trigger(PATHSELECT, {path: e.path});
                that.loadContentPanes(e.path);
            });

            that.contentEl = that.view.element.find(".k-center-pane-content");

            that.contextPanel = that.view.element.find(".k-right-pane-content").kendoZaContextPanel().data("kendoZaContextPanel");
            that.contextPanel.bind("action", jQuery.proxy(that._doAction, that));
        },

        events: [
            DELTA,
            NAVIGATE,
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

        getConfigPath: function()
        {
            return this.configTree.getConfigPath();
        },

        setPaths: function(rootPath, configPath, lazy)
        {
            var that = this,
                path = rootPath;

            if (configPath && configPath.length > 0)
            {
                path += "/" + configPath;
            }

            that.configTree.setRootPath(rootPath);

            if (lazy)
            {
                that.beginNavigation();
                that.contextPanel.beginNavigation();
                that.configTree.one("ready", function()
                {
                    path = that.configTree.longestMatchingSubpath(path);
                    that.configTree.selectAbsolutePath(path);
                    Zutubi.admin.replaceConfigPath(path);
                    that.loadContentPanes(path);
                });
            }
            else
            {
                that.configTree.selectConfig(configPath);
                that.loadContentPanes(path);
            }
        },

        _openHierarchy: function()
        {
            Zutubi.admin.openHierarchyPath(this.configTree.getRootPath());
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
                this.path = path;
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
            this.contextPanel.applyDelta(delta);
            if (delta.renamedPaths && delta.renamedPaths.hasOwnProperty(this.path))
            {
                newPath = delta.renamedPaths[this.path];
                Zutubi.admin.replaceConfigPath(newPath);
                this.path = newPath;
            }

            this.trigger(DELTA, {delta: delta});
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
                $('<p class="k-nav-error"></p>').appendTo(this.contentEl).text(error);
            }
        },

        loadContentPanes: function(path)
        {
            var that = this;

            that.path = path;

            Zutubi.admin.navigate("/api/config/" + Zutubi.config.encodePath(path) + "?depth=-1", [that, that.contextPanel], function(data)
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
                container: that.contentEl,
                composite: data,
                path: that.path
            });

            that.contentPanel.bind("cancelled", function(e)
            {
                that._openPath(that.configTree.longestMatchingSubpath(Zutubi.config.parentPath(that.path)));
            });

            that.contentPanel.bind("saved", function(e)
            {
                that.applyDelta(e.delta);
                that._openPath(that.configTree.longestMatchingSubpath(Zutubi.config.parentPath(that.path)));
            });

            that.contentPanel.bind("applied", function(e)
            {
                that.applyDelta(e.delta);
                that._clearContent();
                that._showComposite(e.delta.models[that.path]);
            });

            that.contentPanel.bind("navigate", function(e)
            {
                that.trigger(NAVIGATE, e);
            });

            if (that.contentPanel.collapsedCollectionPanel)
            {
                that._bindCollectionHandlers(that.contentPanel.collapsedCollectionPanel, data.nested[0]);
            }
        },

        _showCollection: function(data)
        {
            var that = this;

            that.contentPanel = new Zutubi.admin.CollectionPanel({
                container: that.contentEl,
                collection: data,
                path: that.path
            });

            that._bindCollectionHandlers(that.contentPanel, data);
        },

        _bindCollectionHandlers: function(panel, collection)
        {
            var that = this;

            panel.bind("add", function()
            {
                that._showWizard(panel.options.path, collection.type.targetLabel, collection.concrete);
            });

            panel.bind("action", jQuery.proxy(that._doAction, that));

            panel.bind("reordered", function(e)
            {
                that.applyDelta(e.delta);
            });
        },

        _showTypeSelection: function(data)
        {
            var that = this;

            that.contentPanel = new Zutubi.admin.TypeSelectPanel({
                container: that.contentEl,
                type: data,
                path: that.path
            });

            that.contentPanel.bind("configure", function()
            {
                that._showWizard(that.path, data.label, data.concrete);
            });
        },

        _showWizard: function(path, label, markRequired)
        {
            var that = this,
                window;

            window = new Zutubi.admin.WizardWindow({
                path: path,
                label: label,
                markRequired: markRequired,
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

            if (action.action === "view" || action.action === "write")
            {
                this._openPath(e.path);
            }
            else if (action.action === "delete")
            {
                this._deleteConfig(e.path, action.label);
            }
            else if (action.action === "restore")
            {
                this._restoreConfig(e.path);
            }
            else if (e.descendant)
            {
                this._executeAction(e.path, e.action, true);
            }
            else if (action.inputRequired)
            {
                this._doActionWithInput(e.path, e.action);
            }
            else
            {
                this._executeAction(e.path, e.action, false);
            }
        },

        _doActionWithInput: function(path, action)
        {
            var that = this,
                actionWindow;

            actionWindow = new Zutubi.config.ActionWindow({
                path: path,
                action: action,
                executed: jQuery.proxy(that._handleActionResult, that, path)
            });

            actionWindow.show();
        },

        _executeAction: function(path, action, descendant)
        {
            var that = this,
                actionPart = action.action;

            if (action.variant)
            {
                actionPart += ":" + action.variant;
            }

            Zutubi.core.ajax({
                type: "POST",
                maskAll: true,
                url: "/api/action/" + (descendant ? "descendant" : "single") + "/" + Zutubi.config.encodePath(actionPart + "/" + path),
                success: jQuery.proxy(that._handleActionResult, that, path),
                error: function (jqXHR)
                {
                    Zutubi.core.reportError("Could not perform action: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        },

        _handleActionResult: function(path, data)
        {
            if (data.success)
            {
                Zutubi.core.reportSuccess(data.message);
            }
            else
            {
                Zutubi.core.reportError(data.message);
            }

            if (data.newPath)
            {
                if (data.newPath.indexOf(this.getRootPath()) === 0)
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
                    Zutubi.admin.openConfigPath(data.newPath);
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
                else if (Zutubi.config.parentPath(path) === this.path)
                {
                    // We are showing this item in a collection.
                    this.contentPanel.updateItem(Zutubi.config.baseName(path), data.model);
                }
            }
        },

        _deleteConfig: function(path, label)
        {
            var that = this,
                deleteWindow;

            deleteWindow = new Zutubi.admin.DeleteWindow({
                path: path,
                label: label,
                confirm: function()
                {
                    Zutubi.core.ajax({
                        type: "DELETE",
                        url: "/api/config/" + Zutubi.config.encodePath(path),
                        maskAll: true,
                        success: function (delta)
                        {
                            var rootPath = that.getRootPath();

                            if (delta.deletedPaths && delta.deletedPaths.indexOf(rootPath) >= 0)
                            {
                                that.trigger(DELTA, {delta: delta});
                                Zutubi.admin.openConfigPath(Zutubi.config.parentPath(rootPath));
                            }
                            else
                            {
                                that.applyDelta(delta);
                                that._openPath(that.configTree.longestMatchingSubpath(path));
                            }
                        },
                        error: function (jqXHR)
                        {
                            Zutubi.core.reportError("Could not delete configuration: " + Zutubi.core.ajaxError(jqXHR));
                        }
                    });
                }
            });

            deleteWindow.show();
        },

        _restoreConfig: function(path)
        {
            var that = this;

            Zutubi.core.ajax({
                type: "POST",
                maskAll: true,
                url: "/api/action/restore/" + Zutubi.config.encodePath(path),
                success: jQuery.proxy(that._handleRestoreResult, that),
                error: function (jqXHR)
                {
                    Zutubi.core.reportError("Could not perform action: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        },

        _handleRestoreResult: function(data)
        {
            this.configTree.updatePath(this.path, data);
            this._showContent(data);
        }
    });
}(jQuery));
