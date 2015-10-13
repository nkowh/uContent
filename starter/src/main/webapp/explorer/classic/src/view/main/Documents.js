Ext.define('explorer.view.main.Documents', {
    extend: 'Ext.grid.Panel',
    xtype: 'documents',

    controller: 'document',
    //bind: {
    //    store :  '{documents}'
    //},
    //session: true,
    store: {type: 'documents'},
    selType: 'checkboxmodel',
    columns: [{
        header: 'Name',
        dataIndex: 'name'
    }, {
        header: 'Type',
        dataIndex: '_type',
        flex: 1
    }, {
        header: 'CreatedOn',
        dataIndex: 'createdOn',
        xtype: 'datecolumn',
        format: 'Y-m-d',
        flex: 1
    }, {
        header: 'CreatedBy',
        dataIndex: 'createdBy',
        flex: 1
    }, {
        header: 'LastUpdatedOn',
        dataIndex: 'lastupdatedOn',
        xtype: 'datecolumn',
        flex: 1,
        format: 'Y-m-d'
    }, {
        header: 'LastUpdatedBy',
        dataIndex: 'lastupdatedBy',
        flex: 1
    }],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            {xtype: 'button', text: '详细信息', handler: 'detail'},
            {xtype: 'button', text: '删除', handler: 'deleteDoc'}
        ]
    }, {
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        displayInfo: true,
        store: {type: 'documents'},
        displayMsg: 'Displaying Contents {0} - {1} of {2}',
        emptyMsg: "No Contents to display"
    }],
    listeners: {
        afterrender: 'loadData'
    }
});
