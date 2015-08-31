Ext.define('starter.view.main.system.group.Groups', {
    extend: 'Ext.grid.Panel',
    xtype: 'groups',

    controller: 'group',
    viewModel: 'group',
    bind: {
        title: '{listTitle}',
        store :  '{groups}'
    },
    columns: [
        { text: 'Name',width: 200, flex : 1, dataIndex: 'groupName' },
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
            { xtype: 'button', text: 'Delete',handler: 'deleteGroup'}
        ]
    },{
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        bind: {
            store :  '{groups}'
        },
        displayInfo: true,
        displayMsg: 'Displaying Groups {0} - {1} of {2}',
        emptyMsg: "No Groups to display"
    }],
    listeners:{
        "rowdblclick" : 'openModifyWin'
    }
});