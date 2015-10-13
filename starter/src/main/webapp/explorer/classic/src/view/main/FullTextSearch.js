Ext.define('explorer.view.main.FullTextSearch', {
    extend: 'Ext.grid.Panel',
    xtype: 'fulltext',

    controller: 'mainheader',
    bind: {
        store: '{fulltext}'
    },
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            {xtype: 'button', text: '详细信息', handler: 'detail'},
            {xtype: 'button', text: '删除', handler: 'deleteDoc'}
        ]
    }, {
        xtype: 'toolbar',
        dock: 'bottom',
        items: [{
            xtype: 'slider',
            minValue: 1,
            width: 300,
            bind: {
                fieldLabel: '显示数量' + '{pageSize}',
                value: '{pageSize}'
            }
        }]
    }],

    initComponent: function () {
        Ext.apply(this, {
            store: [],
            features: [{
                ftype: 'rowbody',
                setupRowData: function (record, rowIndex, rowValues) {
                    var headerCt = this.view.headerCt,
                        colspan = headerCt.getColumnCount();
                    var html=''
                    Ext.Object.each(record.get("_highlight"), function (key, value) {
                        html+='<div style="padding: 1em">' + value + '</div>'
                    });
                    Ext.apply(rowValues, {
                        rowBody: html,
                        rowBodyColspan: colspan
                    });
                }
            }],
            viewConfig: {
                trackOver: false,
                stripeRows: false
            },
            selType: 'checkboxmodel',
            columns: [
                {text: 'Score', dataIndex: '_score', xtype: 'numbercolumn', flex: 1, sortable: false},
                {text: 'Name', dataIndex: 'name', flex: 1, sortable: false},
                {text: 'CreateBy', dataIndex: 'createdBy', flex: 1, sortable: false},
                {
                    text: 'CreatedOn',
                    xtype: 'datecolumn',
                    format: 'Y-m-d H:i:s',
                    dataIndex: 'createdOn',
                    flex: 1,
                    sortable: false
                },
                {text: 'LastUpdatedBy', dataIndex: 'lastUpdatedBy', flex: 1, sortable: false},
                {
                    text: 'LastUpdatedOn',
                    xtype: 'datecolumn',
                    format: 'Y-m-d H:i:s',
                    dataIndex: 'lastUpdatedOn',
                    flex: 1, sortable: false
                }
            ]

        });
        this.callParent();
    },

    listeners: {
        itemdblclick: 'showImage'
    }
});
