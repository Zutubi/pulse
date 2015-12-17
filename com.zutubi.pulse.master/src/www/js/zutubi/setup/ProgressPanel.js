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
                    '<div id="verbose-message"></div>' +
                    '<div id="continue-wrapper" style="display:none"><button id="continue-button" type="button" value="continue">continue</button></div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        baseUrl: window.baseUrl,
                        title: options.type + " progress"
                    }
                });

            that.view.render(options.container);
            that.overallTable = $("#progress-overall");
            that.taskWrapper = $("#task-wrapper");
            that.taskTable = $("#progress-task");
            that.verboseMessage = $("#verbose-message");
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
            var template, verbose = null, task = null;

            this.overallTable.find("tr").remove();

            template = kendo.template(
                '<tr><th>status</th><td><span class="fa #= statusIconCls #"></span> #: status #</td></tr>' +
                '<tr><th>elapsed time</th><td>#: elapsed #</td></tr>');

            this.overallTable.html(template({
                status: progress.status,
                statusIconCls: this._getStatusIconCls(progress.status),
                elapsed: this._getElapsed(progress.elapsedMillis)
            }));

            if (progress.status === "failed")
            {
                task = this._getTaskWithStatus(progress.tasks, "failed");
                verbose = this.options.failureVerbose;
            }
            else if (progress.status === "success")
            {
                this.continueWrapper.show();
                verbose = this.options.successVerbose;
            }
            else
            {
                task = this._getTaskWithStatus(progress.tasks, "running");
                this.continueWrapper.hide();
            }

            if (verbose)
            {
                this.verboseMessage.html(verbose);
                this.verboseMessage.show();
            }
            else
            {
                this.verboseMessage.hide();
            }

            this._renderTask(task);
        },

        _getTaskWithStatus: function(tasks, status)
        {
            var i;

            for (i = 0; i < tasks.length; i++)
            {
                if (tasks[i].status === status)
                {
                    return tasks[i];
                }
            }

            return null;
        },

        _renderTask: function(task)
        {
            var template, message;

            if (task) {
                template = kendo.template(
                    '<tr><th>name</th><td>#: name #</td></tr>' +
                    '<tr><th>description</th><td>#: description #</td></tr>' +
                    '<tr><th>elapsed time</th><td>#: elapsed #</td></tr>');

                task.elapsed = this._getElapsed(task.elapsedMillis);

                this.taskTable.find("tr").remove();
                this.taskTable.html(template(task));

                if (task.status === "running")
                {
                    this.taskTable.append('<tr><th>progress</th><td><div id="task-progress"></div></td></tr>');
                    $("#task-progress").kendoProgressBar({
                        type: "percent",
                        value: task.percentComplete < 0 ? false : task.percentComplete
                    });
                }
                else if (task.status === "failed")
                {
                    message = "Task Failed: " + (task.statusMessage || "unknown reason");
                    this.taskTable.append('<tr><th>message</th><td>' + kendo.htmlEncode(message) + '</td></tr>');
                }

                this.taskWrapper.show();
            }
            else
            {
                this.taskWrapper.hide();
            }
        },

        _getStatusIconCls: function(status)
        {
            if (status === "success")
            {
                return "fa-check";
            }
            else if (status === "failed")
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
