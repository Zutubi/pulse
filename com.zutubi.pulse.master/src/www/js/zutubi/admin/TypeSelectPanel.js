// dependency: ./namespace.js
// dependency: zutubi/config/package.js

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
                '<div class="k-type-select-panel">' +
                    '<h1>#: label #</h1>' +
                    '<div class="k-type-select-content"></div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        label: type.label
                    }
                });

            that.view.render(options.container);

            that.contentElement = that.view.element.find(".k-type-select-content");
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
            var i, j,
                descendant,
                template = kendo.template('<li><a>#= levels # #: name #</a></li>'),
                list = $('<ul></ul>'),
                levels,
                el;

            for (i = 0; i < descendants.length; i++)
            {
                descendant = descendants[i];
                levels = ' ';
                for (j = 0; j < descendant.first; j++)
                {
                    levels += '<span class="fa fa-caret-right"></span> ';
                }

                list.append($(template({
                    levels: levels,
                    name: descendant.second
                })));
            }

            el = $('<div class="k-configured-descendants"></div>').appendTo(this.contentElement);
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
            Zutubi.admin.openConfigPath(Zutubi.config.subPath(this.options.path, 0, 1) + "/" + targetOwner + "/" + Zutubi.config.subPath(this.options.path, 2));
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
