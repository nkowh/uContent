Ext.define('dm.view.main.FullTextSearch', {
    extend: 'Ext.grid.Panel',
    xtype: 'fulltextsearch',

    controller: 'fulltextsearch',
    viewModel: 'fulltextsearch',


    bind: {
        title: '{title}',
        store:'{documents}'
    },
    tbar: [
        {
            text: 'save',
            handler: 'save'
        }
    ],
    bbar: [
        {
            xtype: 'slider',
            minValue: 1,
            width: 300,
            bind: {
                fieldLabel: '显示数量' + '{pageSize}',
                value: '{pageSize}'
            }
        }
    ],
    columns: [
        {text: 'Name', dataIndex: 'Name', flex: 1},
        {text: 'Id', dataIndex: 'Id', flex: 1},
        {text: 'CreateBy', dataIndex: 'CreateBy', flex: 1},
        {text: 'LastUpdatedBy', dataIndex: 'LastUpdatedBy', flex: 1},
        {text: 'CreatedOn', xtype: 'datecolumn', format: 'C', dataIndex: 'CreatedOn', flex: 1},
        {text: 'LastUpdatedOn', xtype: 'datecolumn', format: 'Y-m-d H:i:sT', dataIndex: 'LastUpdatedOn', flex: 1},
        {
            xtype: 'actioncolumn',
            items: [{
                iconCls: 'fa fa-times',
                tooltip: '删除',
                handler: 'delete'
            }]
        }
    ],

    listeners: {
        select: 'onDocumentSelected'
    }
});
