Ext.define('admin.view.main.view.Views', {
    extend: 'Ext.grid.Panel',
    xtype: 'views',

    controller: 'view',
    viewModel: 'view',
    bind: {
        title: '{listTitle}',
        store :  '{views}'
    },
    columns: [
        { text: 'ViewName', flex : 1, dataIndex: 'viewName' },
        { text: 'Groups',width: 200, dataIndex: 'groups' },
        { text: 'Users',width: 200,  dataIndex: 'users' }
    ],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button', text: 'Refresh', handler: 'refreshView' },
            { xtype: 'button', text: 'Add', handler: 'openCreateWin' },
            { xtype: 'button', text: 'Delete',handler: 'deleteView'}
        ]
    }
       ],
    listeners:{
        "rowdblclick" : 'openModifyWin'
    }
});