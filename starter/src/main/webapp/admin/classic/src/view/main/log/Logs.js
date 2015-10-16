Ext.define('admin.view.main.log.Logs', {
    extend: 'Ext.grid.Panel',
    xtype: 'logs',

    controller: 'log',
    viewModel: 'log',
    bind: {
        title: '{listTitle}',
        store :  '{logs}'
    },
    columns: [
        { text: 'UserName',width: 200, flex : 1, dataIndex: 'userName' },
        { text: 'StartTime',width: 150, flex : 1, dataIndex: 'timeInfo',
            renderer: function(v, meta){
                return v.start_format;
            } },
        { text: 'EndTime',width: 150, dataIndex: 'timeInfo' ,
            renderer: function(v, meta){
                return v.end_format;
            }},
        { text: 'Consume',width: 100, dataIndex: 'timeInfo',
            renderer: function(v, meta){
                return v.consume_format;
            } },
        { text: 'IpAddress',width: 200, dataIndex: 'requestInfo',
            renderer: function(v, meta){
                return v.ipAddress;
            } },
        { text: 'Method',width: 130, dataIndex: 'requestInfo',
            renderer: function(v, meta){
                return v.method;
            } }
    ],
    dockedItems: [{
        dock: 'top',
        xtype: 'toolbar',
        items: [{
            xtype: 'textfield',
            name: 'query',
            fieldLabel: 'query'
        },{ xtype: 'button',
            text: 'search',
            handler: 'searchLog' }]
    },{
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        bind: {
            store :  '{logs}'
        }}],
    listeners:{
        "rowdblclick" : 'showWin'
    }
});