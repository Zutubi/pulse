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
 *     tests: TestSuiteModel (name and duration should be ignored)
 * }, ... ]
 *
 * where a TestSuiteModel looks like:
 *
 * {
 *     name: 'SomeClassTest',
 *     duration: '5 mins 1 sec',
 *     cases: TestCaseModel[],
 *     suites: TestSuiteModel[],
 * }
 *
 * and a TestCaseModel:
 *
 * {
 *     name: 'testSomeCase',
 *     status: 'failed',
 *     brokenNumber: 12 (greater than zero if the test has been broken since an earlier build)
 *     message: 'Assertion failed ...',
 *     duration: '1 min 23 secs'
 * }
 *
 * @cfg {String} id        Id to use for this component.
 * @cfg {String} title     Title for the table.
 * @cfg {String} buildsUrl Url for builds of the project these tests were part of.
 */
Zutubi.pulse.project.TestFailuresTable = Ext.extend(Zutubi.table.ContentTable, {
    columnCount: 5,
    
    stageRowTemplate: new Ext.XTemplate(
        '<tr id="stage-{safeName}-failed" class="' + Zutubi.table.CLASS_DYNAMIC + '">' +
            '<td class="leftmost">' +
                '<a href="{testsUrl}">build stage {name:htmlEncode}</a>' +
            '</td>' +
            '<td><img src="{[window.baseUrl]}/images/exclamation.gif"/></td>' +
            '<td class="content">{testSummary}</td>' +
            '<td>{recipeName:htmlEncode}@{agentName:htmlEncode}</td>' +
            '<td class="rightmost">&nbsp;</td>' +
        '</tr>'
    ),

    suiteRowTemplate: new Ext.XTemplate(
        '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '">' +
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
        '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '">' +
            '<td class="test-failure nowrap leftmost">' +
                '{indent}{name:htmlEncode}' +
            '</td>' +
            '<td class="test-failure fit-width">' +
                '<img alt="broken test" src="{[window.baseUrl]}/images/exclamation' +
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
        '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '"><th class="note leftmost rightmost" colspan="5">{indent}{text}</th></tr>'
    ),

    indent: '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',

    renderFixed: function() {
        this.tbodyEl.insertHtml('beforeEnd', '<tr><th class="leftmost">name</th><th colspan="2">status</th><th>details</th><th class="rightmost">time</th></tr>');
    },

    renderData: function()
    {
        for (var i = 0, l = this.data.length; i < l; i++)
        {
            var stage = this.data[i];
            this.stageRowTemplate.append(this.tbodyEl, stage);

            if (stage.tests)
            {
                if (stage.excessFailureCount > 0)
                {
                    this.noteRowTemplate.append(this.tbodyEl, {
                        indent: this.indent,
                        text: 'Too many test failures to summarise.  This stage has ' + stage.excessFailureCount + ' further test failures, see the full test report for details.'
                    });
                }

                this.renderSuiteContents(stage.tests, stage.testsUrl, this.indent);
            }
            else
            {
                this.noteRowTemplate.append(this.tbodyEl, {
                    indent: this.indent,
                    text: 'Unable to load test failures for this stage.  Test results may have been cleaned up.'
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
            duration: suite.duration || '&nbsp;'
        });

        this.renderSuiteContents(suite, url, indent + this.indent);
    },

    renderSuiteContents: function(suite, url, indent)
    {
        if (suite.suites)
        {
            for (var i = 0, l = suite.suites.length; i < l; i++)
            {
                var child = suite.suites[i];
                this.renderSuite(child, url + encodeURIComponent(child.name) + '/', indent);
            }
        }
        
        if (suite.cases)
        {
            for (i = 0, l = suite.cases.length; i < l; i++)
            {
                this.renderCase(suite.cases[i], indent);
            }
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
