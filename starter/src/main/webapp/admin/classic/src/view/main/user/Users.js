Ext.define('admin.view.main.user.Users', {
    extend: 'Ext.grid.Panel',
    xtype: 'users',

    controller: 'user',
    viewModel: 'user',
    bind: {
        title: '{listTitle}',
        store :  '{users}'
    },
        columns: [
        { text: 'UserId',width: 200, flex : 1, dataIndex: 'userId' },
        { text: 'UserName',width: 200, flex : 1, dataIndex: 'userName' },
        { text: 'CreatedBy',width: 130, dataIndex: 'createdBy' },
        { text: 'CreatedOn',width: 150, dataIndex: 'createdOn', xtype: 'datecolumn',   format:'Y-m-d H:i:s' },
        { text: 'LastUpdatedBy',width: 130, dataIndex: 'lastupdatedBy' },
        { text: 'LastUpdatedOn',width: 150, dataIndex: 'lastupdatedOn', xtype: 'datecolumn',   format:'Y-m-d H:i:s' }
    ],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button', text: 'Add', handler: 'openCreateWin' },
            { xtype: 'button', text: 'Delete',handler: 'deleteUser'}
        ]
    }
    //    ,{
    //    xtype : 'slider',
    //    bind: {
    //        value: '{size}',
    //        fieldLabel: '显示数量' + '{size}'
    //    }
    //    //value: 10,
    //
    //}
        ,{
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        bind: {
            store :  '{users}'
        },
        displayInfo: true,
        displayMsg: 'Displaying Users {0} - {1} of {2}',
        emptyMsg: "No Users to display"
    }],
    listeners:{
        "rowdblclick" : 'openModifyWin'
    }
});