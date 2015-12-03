// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable,
        CONTINUE = "continue",
        SECOND = 1000,
        MINUTE = 60000;

    Zutubi.setup.ProgressPanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                progress = options.progress;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="progress-view">' +
                    '<h1>#: title #</h1>' +
                    '<table><tbody id="progress-overall"></tbody></table>' +
                    '<div id="task-wrapper" style="display:none">' +
                        '<h2>current task</h2>' +
                        '<table><tbody id="progress-task"></tbody></table>' +
                    '</div>' +
                    '<div id="continue-wrapper" style="display:none"><button id="continue-button" type="button" value="continue">continue</button></div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        baseUrl: window.baseUrl,
                        title: options.type + " progress"
                    }
                });

            that.view.render($(options.containerSelector));
            that.overallTable = $("#progress-overall");
            that.taskWrapper = $("#task-wrapper");
            that.taskTable = $("#progress-task");
            that.continueWrapper = $("#continue-wrapper");

            $("#continue-button").kendoZaButton({
                click: jQuery.proxy(that.trigger, that, CONTINUE)
            });
        },

        events: [
            CONTINUE
        ],

        destroy: function()
        {
            this.view.destroy();
        },

        setProgress: function(progress)
        {
            var template;

            this.overallTable.find("tr").remove();

            template = kendo.template(
                '<tr><th>status</th><td><span class="fa #= statusIconCls #"></span> #: status #</td></tr>' +
                '<tr><th>elapsed time</th><td>#: elapsed #</td></tr>');

            this.overallTable.html(template({
                status: progress.status,
                statusIconCls: this._getStatusIconCls(progress.status),
                elapsed: this._getElapsed(progress.elapsedMillis)
            }));

            this.taskWrapper.hide();

            if (progress.status === "success")
            {
                this.continueWrapper.show();
            }
            else
            {
                this.continueWrapper.hide();
            }
        },

        _getStatusIconCls: function(status)
        {
            if (status === "success")
            {
                return "fa-check";
            }
            else if (status === "failure")
            {
                return "fa-exclamation-triangle";
            }
            else
            {
                return "fa-spinner fa-spin";
            }
        },

        _getElapsed: function(millis)
        {
            var result, remainder;

            if (millis < 0)
            {
                result = "n/a";
            }
            else if (millis < SECOND)
            {
                result = '< 1 sec';
            }
            else if (millis < MINUTE)
            {
                result = Math.floor(millis / SECOND) + ' sec';
            }
            else
            {
                result = Math.floor(millis / MINUTE) + ' min';
                remainder = millis % MINUTE;
                if (remainder > SECOND)
                {
                    result += ' ' + Math.floor(remainder / SECOND) + ' sec';
                }
            }

            return result;
        }
    });
}(jQuery));
