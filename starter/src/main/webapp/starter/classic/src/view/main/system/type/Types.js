Ext.define('starter.view.main.system.type.Types', {
    extend: 'Ext.grid.Panel',
    xtype: 'types',

    controller: 'type',
    viewModel: 'type',
    bind: {
        title: '{listTitle}',
        store :  '{types}'
    },
    columns: [
        { text: 'Name',width: 200, flex : 1, dataIndex: 'name' },
        { text: 'DisplayName',width: 200, flex : 1, dataIndex: 'displayName' },
        { text: 'Description',width: 130, dataIndex: 'description' }
    ],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button', text: 'Refresh', handler: 'refreshType' },
            { xtype: 'button', text: 'Add', handler: 'openCreateWin' },
            { xtype: 'button', text: 'Delete',handler: 'deleteType'}
        ]
    }],
    listeners:{
        "rowdblclick" : 'openModifyWin'
    }
});