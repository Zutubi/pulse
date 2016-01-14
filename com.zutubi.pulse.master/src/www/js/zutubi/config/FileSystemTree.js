// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        SELECT = "select";

    Zutubi.config.FileSystemTree = TreeView.extend({
        init: function(element, options)
        {
            this.bound = false;

            TreeView.fn.init.call(this, element, options);

            if (options && options.fs)
            {
                this.setBase(options.fs, options.basePath);
            }
        },

        events: [
            "dataBound"
        ],

        options: {
            name: "ZaFileSystemTree",
            dataTextField: "name",
            showFiles: true,
            loadOnDemand: true,
            dataBound: function(e)
            {
                // This callback is invoked for every level, but only once with a null node. It is also called when we
                // later update the datasource, so once we've bound we don't trigger again.
                if (!this.bound && !e.node)
                {
                    kendo.ui.progress(this.element, false);
                    this.bound = true;
                    if (this.lazyPath)
                    {
                        this.selectPath(this.lazyPath, this.lazyComplete);
                        this.lazyPath = this.lazyComplete = null;
                    }
                }
            }
        },

        _doLoad: function ()
        {
            var that = this,
                dataSource;

            that.bound = false;

            dataSource = new kendo.data.HierarchicalDataSource({
                transport: {
                    read: function (options) {
                        // By default Kendo wants to specify the node with its id as a query param.
                        // Instead we want to use the node's path in the URL.  The simplest way I
                        // have found to do this is take over the read function as below, and make
                        // sure it gets the data it needs by using the path as the id (only the id
                        // field appears to be available to the read function, this would be
                        // simpler if we could just access the node).  So we don't need to
                        // wastefully pass the path from the server, we construct it client side in
                        // our success callback.
                        var url = "/api/fs/" + that.fs + "/" + Zutubi.config.encodePath(that.basePath),
                            path = null;

                        if (options.data && options.data.path)
                        {
                            path = options.data.path;
                            url += "/" + Zutubi.config.encodePath(path);
                            delete options.data.path;
                        }

                        url += "?showFiles=" + that.options.showFiles;

                        Zutubi.core.ajax({
                            url: url,
                            success: function (result) {
                                var i, name;

                                for (i = 0; i < result.length; i++)
                                {
                                    name = result[i].name;
                                    if (path)
                                    {
                                        if (path === "/")
                                        {
                                            result[i].path = path + name;
                                        }
                                        else
                                        {
                                            result[i].path = path + "/" + name;
                                        }
                                    }
                                    else
                                    {
                                        result[i].path = name;
                                    }
                                }
                                options.success(result);
                            },
                            error: function (result) {
                                options.error(result);
                            }
                        });
                    }
                },
                schema: {
                    model: {
                        hasChildren: "directory",
                        id: "path"
                    }
                }
            });

            dataSource.bind('error', function (e)
            {
                Zutubi.core.reportError('Could not load file system tree: ' + Zutubi.core.ajaxError(e.xhr));
            });

            kendo.ui.progress(that.element, true);
            that.setDataSource(dataSource);
        },

        reload: function()
        {
            this._doLoad();
        },

        setBase: function(fs, basePath)
        {
            if (this.fs === fs && this.basePath === basePath)
            {
                return;
            }

            this.fs = fs;
            this.basePath = basePath;
            this._doLoad();
        },

        _findItem: function(dataSource, name)
        {
            var i,
                data = dataSource.data;

            for (i = 0; i < data.length; i++)
            {
                if (data[i].name === name)
                {
                    return data[i];
                }
            }

            return null;
        },

        selectPath: function(path, complete)
        {
            var that = this,
                metaPath = [],
                root = that.wrapper.find(".k-item:first"),
                prefix = "",
                elements,
                end;

            if (!that.bound)
            {
                that.lazyPath = path;
                that.lazyComplete = complete;
                return;
            }

            // Two notes:
            // 1) This implementation looks a bit odd because a Kendo tree "path" is an array of
            //    ids, whereas we set the id of each node to its file system path (see the data
            //    source config for why we do this).  So to convert from a file system path to a
            //    Kendo path we construct the meta path array, easiest to understand from the
            //    example: "this/is/a/path" -> ["this", "this/is", "this/is/a", "this/is/a/path"].
            // 2) We need to special case handling of paths that start with a slash, as the slash
            //    is used as the name of the root node in that case (and are included in all
            //    child paths too).

            if (path.indexOf("/") === 0 && this.text(root) === "/")
            {
                metaPath.push("/");
                prefix = "/";
            }

            path = Zutubi.config.normalisedPath(path);
            elements = path.split("/");
            for (end = 1; end <= elements.length; end++)
            {
                metaPath.push(prefix + elements.slice(0, end).join("/"))
            }

            that.expandPath(metaPath, function()
            {
                var i,
                    dataSource = that.dataSource,
                    item = null,
                    node = null;

                for (i = 0; i < metaPath.length; i++)
                {
                    item = dataSource.get(metaPath[i]);
                    dataSource = item.children;
                }

                if (item)
                {
                    node = that.findByUid(item.uid);
                    if (node)
                    {
                        node.get(0).scrollIntoView();
                        that.select(node);
                        that.trigger(SELECT, {node: node});
                    }
                }

                complete(node);
            });
        }
    });

    ui.plugin(Zutubi.config.FileSystemTree);
}(jQuery));
