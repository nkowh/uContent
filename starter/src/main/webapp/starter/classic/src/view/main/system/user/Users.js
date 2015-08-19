Ext.define('starter.view.main.system.user.Users', {
    extend: 'Ext.grid.Panel',
    xtype: 'users',

    controller: 'user',
    viewModel: 'user',
    bind: {
        title: '{listTitle}',
        store :  '{users}'
    },
    columns: [
        { text: 'Name',width: 200, flex : 1, dataIndex: 'Name' },
        { text: 'CreateBy',width: 130, dataIndex: 'CreateBy' },
        { text: 'CreatedOn',width: 150, dataIndex: 'CreatedOn'},
        { text: 'LastUpdatedBy',width: 130, dataIndex: 'LastUpdatedBy' },
        { text: 'LastUpdatedOn',width: 150, dataIndex: 'LastUpdatedOn' }
    ],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button', text: 'Add', handler: 'openCreateWin' },
            { xtype: 'button', text: 'Delete',handler: 'deleteUser'}
        ]
    },{
        xtype : 'slider',
        bind: {
            value: '{size}',
            fieldLabel: '显示数量' + '{size}'
        }
        //value: 10,

    },{
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