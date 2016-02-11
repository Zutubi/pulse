// dependency: ./namespace.js
// dependency: zutubi/config/package.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        BOUND = "bound",
        DATABOUND = "dataBound",
        DRAG = "drag",
        DROP = "drop",
        NODESELECT = "nodeselect",
        SELECT = "select",
        BEFORE_UNLOAD = "beforeunload";

    Zutubi.admin.HierarchyTree = TreeView.extend({
        init: function(element, options)
        {
            TreeView.fn.init.call(this, element, options);

            this.bound = false;

            // jQuery has issues with beforeunload handling, so we don't use it for this binding.
            this.unloadHandler = jQuery.proxy(this._beforeUnload, this);
            window.addEventListener(BEFORE_UNLOAD, this.unloadHandler);

            if (options && options.scope)
            {
                this.setScope(options.scope);
            }
        },

        events: [
            BOUND,
            DATABOUND,
            DRAG,
            DROP,
            NODESELECT,
            SELECT
        ],

        options: {
            name: "ZaHierarchyTree",
            dataTextField: "name",
            dragAndDrop: true,
            loadOnDemand: false,
            dataBound: function(e)
            {
                // This callback is invoked for every level, but only once with a null node.
                if (!this.bound && !e.node)
                {
                    kendo.ui.progress(this.element, false);
                    this.expand(this._getRoot());
                    this.bound = true;
                    this._updateSelected();
                    this.trigger(BOUND);
                }
            },
            drag: function(e)
            {
                var targetNode;

                if (e.statusClass === "add")
                {
                    targetNode = $(e.dropTarget).closest("li");
                    if (this._isValidDropTarget(e.sourceNode, targetNode))
                    {
                        return;
                    }
                }

                // Non-valid cases fall through to here, setting the class to indicate dropping is not allowed.
                e.setStatusClass("k-denied");
            },
            drop: function(e)
            {
                if (e.valid && this._isValidDropTarget(e.sourceNode, e.destinationNode))
                {
                    e.preventDefault();
                    this._previewMove(this.dataItem(e.sourceNode).name, this.dataItem(e.destinationNode).name);
                }
            },
            select: function(e)
            {
                var that = this;
                that.trigger(NODESELECT, {name: that.dataItem(e.node).name});
            }
        },

        destroy: function()
        {
            window.removeEventListener(BEFORE_UNLOAD, this.unloadHandler);
            this._saveState();
            TreeView.fn.destroy.call(this);
        },

        _beforeUnload: function()
        {
            this._saveState();
        },

        _stateKey: function()
        {
            return this.options.name + "_" + this.options.namespace + "_" + this.scope;
        },

        _saveState: function()
        {
            var that = this,
                expandedItems = [];

            if (that.bound)
            {
                that.element.find(".k-item").each(function () {
                    var item = that.dataItem(this);
                    if (item.expanded)
                    {
                        expandedItems.push(item.handle);
                    }
                });

                localStorage[that._stateKey()] = JSON.stringify(expandedItems);
            }
        },

        _isValidDropTarget: function(sourceNode, targetNode)
        {
            var targetItem = this.dataItem(targetNode),
                currentParentNode = this.parent(sourceNode);

            return targetItem && !targetItem.concrete && targetNode[0] !== currentParentNode[0];
        },

        _previewMove: function(name, newParentName)
        {
            var that = this;

            Zutubi.core.ajax({
                type: "POST",
                url: "/api/hierarchy/previewMove",
                data: {
                    scope: that.scope,
                    key: name,
                    newParentKey: newParentName
                },
                maskAll: true,
                success: function (model)
                {
                    that._confirmMove(model);
                },
                error: function (jqXHR)
                {
                    Zutubi.core.reportError("Could not preview move: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        },

        _confirmMove: function(model)
        {
            var that = this,
                messageHTML,
                i,
                window;

            messageHTML = '<p>Are you sure you&apos;d like to move &apos;' + kendo.htmlEncode(model.key) + '&apos; to the new template parent &apos;' + kendo.htmlEncode(model.newParentKey) + '&apos;?</p>';
            if (model.pathsToDelete && model.pathsToDelete.length > 0)
            {
                messageHTML += '<p>Note that some paths are incompatible with the new parent, and will be deleted (this is not reversible):</p><ul>';
                for (i = 0; i < model.pathsToDelete.length; i++)
                {
                    messageHTML += '<li>' + kendo.htmlEncode(model.pathsToDelete[i]) + '</li>';
                }
                messageHTML += '</ul>';
            }

            window = new Zutubi.core.PromptWindow({
                title: "Confirm Move",
                messageHTML: messageHTML,
                width: 640,
                buttons: [{
                    label: "move",
                    value: true,
                    spriteCssClass: "fa fa-check-circle"
                }, {
                    label: "cancel",
                    value: false,
                    spriteCssClass: "fa fa-times-circle"
                }],
                select: function(value)
                {
                    if (value)
                    {
                        // Small optimisation so these are not needlessly POSTed back the server.
                        model.pathsToDelete = null;

                        Zutubi.core.ajax({
                            type: "POST",
                            maskAll: true,
                            url: "/api/hierarchy/move",
                            data: model,
                            success: function(model)
                            {
                                // Since we cancel the drop event (because we don't know if the move will be confirmed
                                // and succeed) we now need to manually move the node.
                                that._moveNode(model.key, model.newParentKey);
                            },
                            error: function(jqXHR)
                            {
                                Zutubi.core.reportError("Could not move to new parent: " + Zutubi.core.ajaxError(jqXHR));
                            }
                        });
                    }
                }
            });

            window.show();
        },

        _moveNode: function(name, newParentName)
        {
            var node = this.findByText(name),
                item = this.dataItem(node),
                newParentNode = this.findByText(newParentName),
                newParentItem = this.dataItem(newParentNode),
                dataSource,
                data,
                i;

            if (node && newParentNode)
            {
                this.expand(newParentNode);
                this.remove(node);
                dataSource = newParentItem.children;
                data = dataSource.data();
                for (i = 0; i < data.length; i++)
                {
                    if (data[i].name > name)
                    {
                        break;
                    }
                }

                dataSource.insert(i, item);
                if (this.item !== name)
                {
                    this.selectItem(name);
                    this.trigger(NODESELECT, {name: name});
                }
            }
        },

        setScope: function(scope)
        {
            this._setScope(scope);
        },

        _setScope: function (scope)
        {
            var expanded, dataSource;

            this.scope = scope;
            this.bound = false;

            expanded = localStorage[this._stateKey()];
            if (expanded)
            {
                expanded = JSON.parse(expanded);
            }
            else
            {
                expanded = [];
            }

            dataSource = new kendo.data.HierarchicalDataSource({
                transport: {
                    read: {
                        url: window.baseUrl + "/api/template/" + scope,
                        dataType: "json",
                        headers: {
                            Accept: "application/json; charset=utf-8",
                            "Content-Type": "application/json; charset=utf-8"
                        }
                    }
                },
                schema: {
                    model: {
                        children: "nested"
                    },
                    parse: function parseItems(items)
                    {
                        return jQuery.map(items, function(item)
                        {
                            item.spriteCssClass = item.concrete ? "fa fa-circle" : "fa fa-circle-thin";
                            item.expanded = expanded.indexOf(item.handle) >= 0;
                            if (item.nested)
                            {
                                parseItems(item.nested);
                            }
                            return item;
                        });
                    }
                }
            });

            dataSource.bind('error', function(e)
            {
                Zutubi.core.reportError('Could not load hierarchy tree: ' + Zutubi.core.ajaxError(e.xhr));
            });

            kendo.ui.progress(this.element, true);
            this.setDataSource(dataSource);
        },

        _getRoot: function()
        {
            return this.wrapper.find(".k-item:first");
        },

        getRootName: function()
        {
            return this.dataSource.at(0).name;
        },

        selectItem: function(name)
        {
            this.item = name;
            if (this.bound)
            {
                this._updateSelected();
            }
        },

        _updateSelected: function()
        {
            var that = this,
                root = that._getRoot(),
                node;

            if (that.item)
            {
                node = that.findByText(that.item);
            }

            if (node)
            {
                if (node !== root)
                {
                    that.expandTo(that.dataItem(node));
                }

                that.select(node);
            }
        },

        prefixFilter: function(s)
        {
            if (s)
            {
                this._prefixFilterSubtree(this.root.children("li").first(), this.dataSource.at(0), s.toLowerCase());
            }
            else
            {
                this.root.find("li").css("display", "");
            }
        },

        _prefixFilterSubtree: function(root, dataItem, text)
        {
            var visible = root.find(".k-in").first().text().toLowerCase().indexOf(text) === 0,
                nested,
                childVisible = false,
                children,
                items,
                child,
                item,
                i;

            nested = root.children("ul");
            if (nested.length > 0)
            {
                children = dataItem.children.data();
                items = nested.children("li");

                for (i = 0; i < items.length; i++)
                {
                    item = items.eq(i);
                    child = children[i];

                    childVisible = this._prefixFilterSubtree(item, child, text) || childVisible;
                }

                if (childVisible && !dataItem.expanded)
                {
                    this._toggle(root, dataItem, true);
                }

                visible = visible || childVisible;
            }

            root.css("display", visible ? "" : "none");
            return visible;
        },

        applyDelta: function(delta)
        {
            var i, path;

            // If there is an addition or rename that applies to us, we just do a full reload (overkill but easy).
            // Deletes are easy to handle directly.
            if (delta.addedPaths)
            {
                for (i = 0; i < delta.addedPaths.length; i++)
                {
                    path = delta.addedPaths[i];
                    if (this.scope === Zutubi.config.parentPath(path))
                    {
                        this.reload();
                        return;
                    }
                }
            }

            if (delta.renamedPaths)
            {
                for (path in delta.renamedPaths)
                {
                    if (delta.renamedPaths.hasOwnProperty(path) && this.scope === Zutubi.config.parentPath(path))
                    {
                        this.reload();
                        return;
                    }
                }
            }

            if (delta.deletedPaths)
            {
                for (i = 0; i < delta.deletedPaths.length; i++)
                {
                    path = delta.deletedPaths[i];
                    if (this.scope === Zutubi.config.parentPath(path))
                    {
                        this.remove(this.findByText(Zutubi.config.subPath(path, 1, 2)));
                    }
                }
            }
        },

        reload: function()
        {
            this._setScope(this.scope);
        }
    });

    ui.plugin(Zutubi.admin.HierarchyTree);
}(jQuery));
