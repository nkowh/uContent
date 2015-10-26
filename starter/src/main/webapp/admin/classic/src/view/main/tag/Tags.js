Ext.define('admin.view.main.tag.Tags', {
    extend: 'Ext.grid.Panel',
    xtype: 'tags',

    controller: 'tag',
    viewModel: 'tag',
    bind: {
        title: '{listTitle}',
        store :  '{tags}'
    },
    columns: [
        { text: 'tagContext', flex : 1, dataIndex: 'tagContext' },
        { text: 'description', flex : 1, dataIndex: 'description' }],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button', text: 'Add', handler: 'openCreateWin' },
            { xtype: 'button', text: 'Delete',handler: 'deleteTag'}
        ]
    }
        ,{
            xtype: 'pagingtoolbar',
            dock: 'bottom',
            bind: {
                store :  '{tags}'
            },
            displayInfo: true,
            displayMsg: 'Displaying Users {0} - {1} of {2}',
            emptyMsg: "No Users to display"
        }],
    listeners:{
        "rowdblclick" : 'openModifyWin'
    }
});