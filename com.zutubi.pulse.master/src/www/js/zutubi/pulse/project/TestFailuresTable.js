// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/ContentTable.js

/**
 * A table summarising test failures for a build.
 *
 * [{
 *     name: 'linux full',
 *     safeName: 'linux', (guaranteed to be id-safe),
 *     testsUrl: '/url/of/stage/tests/page',
 *     recipe: 'full tests',
 *     agent: 'linux',
 *     testSummary: '5 of 10 broken',
 *     excessFailureCount: 0,
 *     tests: [{
 *         cases: TestCaseModel[],
 *         suites: TestSuiteModel[]
 *     }, ... ]
 * }, ... ]
 *
 * @cfg {String} id        Id to use for this component.
 * @cfg {String} buildsUrl Url for builds of the project these tests were part of.
 */
Zutubi.pulse.project.TestFailuresTable = Ext.extend(Zutubi.table.ContentTable, {
    columnCount: 5,
    
    stageRowTemplate: new Ext.XTemplate(
        '<tr id="stage-{safeName}-failed">' +
            '<td class="leftmost">' +
                '<a href="{testsUrl}">build stage {name:htmlEncode}</a>' +
            '</td>' +
            '<td><img src="{[window.baseUrl]}/images/exclamation.gif"/></td>' +
            '<td class="content">{testSummary}</td>' +
            '<td>{recipe:htmlEncode}@{agent:htmlEncode}</td>' +
            '<td class="rightmost">&nbsp;</td>' +
        '</tr>'
    ),

    suiteRowTemplate: new Ext.XTemplate(
        '<tr>' +
            '<td class="test-failure content-nowrap leftmost">' +
                 '{indent}<a href="{url}">{name:htmlEncode}</a>' +
            '</td>' +
            '<td>&nbsp;</td>' +
            '<td>&nbsp;</td>' +
            '<td>&nbsp;</td>' +
            '<td class="content rightmost" width="10%">' +
                '{duration}' +
            '</td>' +
        '</tr>'
    ),

    caseRowTemplate: new Ext.XTemplate(
        '<tr>' +
            '<td class="test-failure nowrap leftmost">' +
                '{indent}{name:htmlEncode}' +
            '</td>' +
            '<td class="test-failure fit-width">' +
                '<img alt="broken test" src="{[window.baseUrl]}/images/exclamation' + +
                '<tpl if="brokenNumber &gt; 0">-bw</tpl>' +
                '.gif"/>' +
            '</td>' +
            '<td class="test-failure">' +
                '{status}' +
                '<tpl if="brokenNumber &gt; 0">' +
                    '<br/>(<a href="{buildsUrl}{brokenNumber}/">since build {brokenNumber}</a>)' +
                '</tpl>' +
            '</td>' +
            '<td class="wrap">' +
                '{message:plainToHtml}' +
            '</td>' +
            '<td class="rightmost" width="10%">' +
                '{duration}' +
            '</td>' +
        '</tr>'            
    ),

    noteRowTemplate: new Ext.XTemplate(
        '<tr><th class="note" colspan="5">{text}</th></tr>'
    ),

    indent: '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',

    renderFixed: function() {
        this.tbodyEl.insertHtml('beforeEnd', '<tr><th>name</th><th colspan="2">status</th><th>details</th><th>time</th></tr>');
    },

    renderDynamic: function(data)
    {
        for (var i = 0, l = this.data.length; i < l; i++)
        {
            var stage = this.data[i];
            this.stageRowTemplate.append(this.tbodyEl, stage);
            if (stage.excessFailureCount > 0)
            {
                this.noteRowTemplate.append(this.tbodyEl, {
                    text: 'Too many test failures to summarise.  This recipe has ' + stage.excessFailureCount + ' further test failures, see the full test report for details.'
                });
            }

            if (stage.tests)
            {
                this.renderSuiteContents(stage.tests, stage.testsUrl, this.indent);
            }
            else
            {
                this.noteRowTemplate.append(this.tbodyEl, {
                    text: 'Unable to load details on test failures.  Loading the details has failed, likely because they have been cleaned up.'
                });
            }
        }
    },

    renderSuite: function(suite, url, indent)
    {
        this.suiteRowTemplate.append(this.tbodyEl, {
            indent: indent,
            url: url,
            name: suite.name,
            duration: suite.duration
        });

        this.renderSuiteContents(suite, url, indent + this.indent);
    },

    renderSuiteContents: function(suite, url, indent)
    {
        for (var i = 0, l = suite.suites.length; i < l; i++)
        {
            var child = suite.suites[i];
            this.renderSuite(child, url + encodeURIComponent(child.name) + '/', indent);
        }

        for (i = 0, l = suite.cases.length; i < l; i++)
        {
            this.renderCase(suite.cases[i], indent);
        }
    },

    renderCase: function(testCase, indent)
    {
        this.caseRowTemplate.append(this.tbodyEl, {
            indent: indent,
            name: testCase.name,
            brokenNumber: testCase.brokenNumber,
            buildsUrl: this.buildsUrl,
            status: testCase.status,
            message: testCase.message || ' ',
            duration: testCase.duration || '&nbsp;'
        });
    }
});

Ext.reg('xztestfailurestable', Zutubi.pulse.project.TestFailuresTable);
