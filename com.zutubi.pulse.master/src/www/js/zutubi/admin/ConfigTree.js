// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        PATHSELECT = "pathselect";

    Zutubi.admin.ConfigTree = TreeView.extend({
        init: function(element, options)
        {
            TreeView.fn.init.call(this, element, options);

            this.bound = false;

            if (options && options.rootPath)
            {
                this.setRootPath(options.rootPath);
            }
        },

        events: [
            // FIXME kendo: to subscribe to our own select event we need to have this here, is there a better way?
            "dataBound",
            "select",
            PATHSELECT
        ],

        options: {
            name: "ZaConfigTree",
            dataTextField: "label",
            loadOnDemand: false,
            dataBound: function(e)
            {
                // This callback is invoked for every level, but only once with a null node. It is also called when we
                // later update the datasource, so once we've bound we don't trigger again.
                if (!this.bound && !e.node)
                {
                    this.bound = true;
                    this.selectConfigNode();
                }
            },
            select: function(e)
            {
                var that = this;
                that.configPath = that.pathForNode(e.node, true);
                that.trigger(PATHSELECT, {path: that.pathForNode(e.node)});
            }
        },

        pathForNode: function(node, relative)
        {
            var nodeData = this.dataItem(node),
                path = nodeData.key,
                parent = this.parent(node),
                parentPath;

            if (parent.length)
            {
                parentPath = this.pathForNode(parent, relative);
                if (parentPath.length > 0)
                {
                    parentPath += '/';
                }

                path =  parentPath + path;
            }
            else if (relative)
            {
                path = '';
            }
            else
            {
                path = this.rootPath;
            }

            return path;
        },

        setRootPath: function(rootPath)
        {
            var dataSource;

            if (this.rootPath && this.rootPath === rootPath)
            {
                return;
            }

            this.rootPath = rootPath;
            this.bound = false;

            dataSource = new kendo.data.HierarchicalDataSource({
                transport: {
                    read: {
                        url: window.baseUrl + "/api/config/" + rootPath + "?depth=-1&filter=nested&filter=type",
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
                    }
                }
            });

            dataSource.bind('error', function(e) {
                Zutubi.admin.reportError('Could not load configuration tree: ' + Zutubi.admin.ajaxError(e.xhr));
            });

            this.setDataSource(dataSource);
        },

        _dataItemForConfigPath: function(configPath)
        {
            var that = this,
                root = that.wrapper.find(".k-item:first"),
                dataItem = that.dataItem(root),
                keys = [],
                i, j,
                key,
                children;

            if (configPath)
            {
                keys = configPath.split("/");
            }

            for (i = 0; i < keys.length; i++)
            {
                key = keys[i];

                children = dataItem.children.data();
                for (j = 0; j < children.length; j++)
                {
                    if (children[j].key === key)
                    {
                        break;
                    }
                }

                if (j === children.length)
                {
                    return null;
                }

                dataItem = children[j];
            }

            return dataItem;
        },

        selectConfigNode: function()
        {
            var that = this,
                root = that.wrapper.find(".k-item:first"),
                dataItem = that._dataItemForConfigPath(that.configPath);

            that.expand(root);
            if (dataItem)
            {
                that.expandTo(dataItem);
                that.select(that.findByUid(dataItem.uid));
            }
            else
            {
                that.select(root);
            }
        },

        selectConfig: function(configPath)
        {
            this.configPath = configPath;
            if (this.bound)
            {
                this.selectConfigNode();
            }
        },

        selectAbsolutePath: function(path)
        {
            this.selectConfig(this._absoluteToConfigPath(path));
        },

        longestMatchingSubpath: function(path)
        {
            var result = this.rootPath,
                configPath = this._absoluteToConfigPath(path),
                root = this.wrapper.find(".k-item:first"),
                dataItem = this.dataItem(root),
                keys = [],
                i, j,
                key,
                children;

            if (configPath)
            {
                keys = configPath.split("/");
            }

            for (i = 0; i < keys.length; i++)
            {
                key = keys[i];

                children = dataItem.children.data();
                for (j = 0; j < children.length; j++)
                {
                    if (children[j].key === key)
                    {
                        break;
                    }
                }

                if (j === children.length)
                {
                    break;
                }

                dataItem = children[j];
                result = result + "/" + key;
            }

            return result;
        },

        _absoluteToConfigPath: function(path)
        {
            if (path.indexOf(this.rootPath) === 0)
            {
                path = path.substring(this.rootPath.length);
            }

            if (path.length > 0 && path[0] === "/")
            {
                path = path.substring(1);
            }

            return path;
        },

        _addModel: function(model, parentDataItem, index)
        {
            var i, data, item;

            if (!parentDataItem.type.ordered)
            {
                // Order by label.
                data = parentDataItem.children.data();
                for (i = 0; i < data.length; i++)
                {
                    item = data[i];
                    if (item.label.localeCompare(model.label) > 0)
                    {
                        break;
                    }
                }

                index = i;
            }
            else if (index === undefined || index < 0)
            {
                index = parentDataItem.children.data().length;
            }

            parentDataItem.children.insert(index, model);
        },

        updatePath: function(path, data)
        {
            var item;

            path = this._absoluteToConfigPath(path);
            item = this._dataItemForConfigPath(path);

            if (item)
            {
                if (data)
                {
                    this.updateItem(path, item, data);
                }
                else
                {
                    this.dataSource.remove(item);
                }
            }
        },

        updateItem: function(path, item, data)
        {
            var parentItem = item.parentNode(),
                dataSource,
                index;

            if (parentItem)
            {
                dataSource = parentItem.children;
            }
            else
            {
                dataSource = this.dataSource;
            }

            index = dataSource.indexOf(item);
            if (index >= 0)
            {
                dataSource.remove(item);
                // FIXME kendo it would be nicer to maintain all expansions, not just this one.
                data.expanded = item.expanded;
                dataSource.insert(index, data);
                if (path === this.configPath)
                {
                    this.selectConfig(path);
                }
            }
        },

        applyDelta: function(delta)
        {
            var that = this,
                i,
                model,
                item,
                path,
                index = -1;

            if (delta.addedPaths)
            {
                for (i = 0; i < delta.addedPaths.length; i++)
                {
                    path = that._absoluteToConfigPath(delta.addedPaths[i]);
                    item = that._dataItemForConfigPath(Zutubi.admin.parentPath(path));
                    if (item)
                    {
                        that._addModel(delta.models[delta.addedPaths[i]], item);
                    }
                }
            }

            if (delta.deletedPaths)
            {
                for (i = 0; i < delta.deletedPaths.length; i++)
                {
                    path = that._absoluteToConfigPath(delta.deletedPaths[i]);
                    item = that._dataItemForConfigPath(path);
                    if (item)
                    {
                        that.dataSource.remove(item);
                    }
                }
            }

            if (delta.renamedPaths)
            {
                jQuery.each(delta.renamedPaths, function(oldPath, newPath)
                {
                    model = delta.models[newPath];
                    oldPath = that._absoluteToConfigPath(oldPath);
                    newPath = that._absoluteToConfigPath(newPath);

                    item = that._dataItemForConfigPath(oldPath);
                    if (item)
                    {
                        index = item.parentNode().children.indexOf(item);
                        that.dataSource.remove(item);
                    }

                    item = that._dataItemForConfigPath(Zutubi.admin.parentPath(newPath));
                    if (item)
                    {
                        that._addModel(model, item, index);
                    }

                    if (oldPath === that.configPath)
                    {
                        that.selectConfig(newPath);
                    }
                });
            }

            if (delta.updatedPaths)
            {
                for (i = 0; i < delta.updatedPaths.length; i++)
                {
                    path = delta.updatedPaths[i];
                    model = delta.models[path];
                    path = that._absoluteToConfigPath(path);
                    item = that._dataItemForConfigPath(path);
                    if (item && item.children && item.children.data().length > 0)
                    {
                        // Update to a node, e.g. collection reorder. Refresh.
                        that.updateItem(path, item, model);
                    }
                }
            }
        }
    });

    ui.plugin(Zutubi.admin.ConfigTree);
}(jQuery));
