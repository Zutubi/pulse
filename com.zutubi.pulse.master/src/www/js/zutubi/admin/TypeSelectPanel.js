// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable,
        ns = ".kendoTypeSelectPanel",
        CLICK = "click" + ns,
        CONFIGURE = "configure";

    Zutubi.admin.TypeSelectPanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                type = options.type,
                configured = type.configuredDescendants && type.configuredDescendants.length > 0;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#: id #" class="k-type-select-panel">' +
                    '<h1>#: label #</h1>' +
                    '<div id="#: id #-content"></div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: "type-select",
                        label: type.label
                    }
                });

            that.view.render($(options.containerSelector));

            that.contentElement = $("#type-select-content");
            if (configured)
            {
                that._showConfiguredDescendants(type.configuredDescendants);
            }
            else
            {
                that._showConfigureButton(type);
            }
        },

        events: [
            CONFIGURE
        ],

        destroy: function()
        {
            if (this.links)
            {
                this.links.off(ns);
            }

            if (this.createButton)
            {
                this.createButton.destroy();
            }

            this.view.destroy();
        },

        _showConfiguredDescendants: function(descendants)
        {
            var i,
                descendant,
                lastIndent = 0,
                template = kendo.template('<li><a>#: second #</a></li>'),
                list = $('<ul></ul>'),
                currentList = list,
                lastItem= $(),
                el;

            for (i = 0; i < descendants.length; i++)
            {
                descendant = descendants[i];
                if (descendant.first > lastIndent)
                {
                    currentList = $('<ul></ul>').appendTo(lastItem);
                }
                else if (descendant.first < lastIndent)
                {
                    currentList = currentList.parent().parent();
                }

                lastIndent = descendant.first;

                lastItem = $(template(descendant));
                currentList.append(lastItem);
            }

            el = $('<div class="configured-descendants"></div>').appendTo(this.contentElement);
            el.append('<p>This path is already configured in ' + descendants.length + ' descendant' +
                (descendants.length > 1 ? 's' : '') +
                ', thus cannot be configured here:</p>');
            el.append(list);

            this.links = el.find("a");
            this.links.on(CLICK, jQuery.proxy(this._linkClicked, this));
        },

        _linkClicked: function(e)
        {
            var targetOwner = $(e.target).text();
            Zutubi.admin.openConfigPath(Zutubi.admin.subPath(this.options.path, 0, 1) + "/" + targetOwner + "/" + Zutubi.admin.subPath(this.options.path, 2));
        },

        _showConfigureButton: function(type)
        {
            var that = this,
                template = kendo.template('<button><span class="k-sprite"></span> configure #: label #</button>'),
                html = template(type),
                element = $(html);

            that.createButton = element.kendoButton({spriteCssClass: "fa fa-plus-circle"}).data("kendoButton");
            that.createButton.bind("click", function()
            {
                that.trigger(CONFIGURE);
            });
            that.contentElement.append(element);
        }
    });
}(jQuery));
