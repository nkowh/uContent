Ext.define('starter.view.main.system.log.Log', {
    extend: 'Ext.form.Panel',
    xtype: 'log',
    controller: 'log',
    viewModel: 'log',
    buttons: [{
        text: 'Close',
        handler: function() {
            this.up('window').close();
        }
    }],
    bodyPadding: 5,
    width: 880,
    scrollable : true,
    height : 600,
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [
        {
            xtype: 'container',
            layout: 'hbox',
            defaultType : 'textfield',
            items: [
                {
                    fieldLabel: 'User Name',
                    name: 'userName'
                },{
                    fieldLabel: 'Start Time',
                    name: 'timeInfo.start_format'
                },{
                    fieldLabel: 'End Time',
                    name: 'timeInfo.end_format'
                }
            ]
        }, {
            xtype: 'container',
            layout: 'hbox',
            defaultType : 'textfield',
            items: [
                {
                    fieldLabel: 'Consume',
                    name: 'timeInfo.consume'
                }, {
                    fieldLabel: 'Request IpAddress',
                    name: 'requestInfo.ipAddress'
                },{
                    fieldLabel: 'Request Url',
                        name: 'requestInfo.url'
                }
            ]
        },{
            xtype: 'container',
            layout: 'hbox',
            defaultType : 'textfield',
            items: [
               {
                    fieldLabel: 'Request Method',
                    name: 'requestInfo.method'
                },{
                    fieldLabel: 'Response StatusCode',
                    name: 'responseInfo.statusCode'
                },{
                    fieldLabel: 'Exception StatusCode',
                    name: 'exceptionInfo.statusCode'
                }
            ]
        },{
        xtype     : 'textfield',
        fieldLabel: 'Request Params',
        name: 'requestInfo.params'
    },{
        xtype     : 'textareafield',
        grow      : true,
        name      : 'requestInfo.header',
        fieldLabel: 'Request Header',
        scrollable : 'y',
        maxHeight : 100,
        anchor    : '100%'
    },{
        xtype     : 'textareafield',
        grow      : true,
        name      : 'responseInfo.header',
        fieldLabel: 'Response Header',
        scrollable : 'y',
        maxHeight : 100,
        anchor    : '100%'
    },{
        xtype     : 'textareafield',
        grow      : true,
        name      : 'responseInfo.result',
        fieldLabel: 'Response Result',
        maxHeight : 100,
        anchor    : '100%'
    },{
        xtype     : 'textareafield',
        grow      : true,
        name      : 'exceptionInfo.msg',
        maxHeight : 100,
        scrollable : 'y',
        fieldLabel: 'Exception Message',
        anchor    : '100%'
    },{
        xtype     : 'textareafield',
        grow      : true,
            maxHeight : 100,
            scrollable : 'y',
        name      : 'exceptionInfo.stackTrace',
        fieldLabel: 'Exception Result',
        anchor    : '100%'
    },
        {xtype     : 'textfield',
        fieldLabel: 'LogDate',
        name: 'logDate'
    }],
    listeners: {
        afterrender : 'loadData'
    }
});