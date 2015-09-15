Ext.define('starter.view.main.document.FullTextSearch', {
    extend: 'Ext.grid.Panel',
    xtype: 'fulltextsearch',

    controller: 'fulltextsearch',
    viewModel: 'fulltextsearch',


    bind: {
        store: '{fulltext}'
    },
    tbar: [
        {
            xtype: 'textfield',
            name: 'keytext'
        }, {
            text: '搜索',
            handler: 'search'
        }, {
            xtype: 'slider',
            minValue: 1,
            width: 300,
            bind: {
                fieldLabel: '显示数量' + '{pageSize}',
                value: '{pageSize}'
            }
        }
    ],

    initComponent: function () {
        //var store = Ext.create('KitchenSink.store.ForumThreads');

        Ext.apply(this, {
            store: [],
            features: [{
                ftype: 'rowbody',
                setupRowData: function (record, rowIndex, rowValues) {
                    var headerCt = this.view.headerCt,
                        colspan = headerCt.getColumnCount();
                   var html=''
                    Ext.Object.each(record.get("highlight"), function (key, value) {
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

            columns: [
                {text: 'Score', dataIndex: 'score', xtype: 'numbercolumn', flex: 1, sortable: false},
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

    //afterRender: function(){
    //    this.callParent(arguments);
    //    this.getStore().loadPage(1);
    //},

});
