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
            { xtype: 'button', text: 'Delete',handler: 'deleteGroup'}
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