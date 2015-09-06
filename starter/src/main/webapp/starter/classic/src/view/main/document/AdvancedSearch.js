Ext.define('starter.view.main.document.AdvancedSearch', {
    extend: 'Ext.panel.Panel',
    xtype: 'advancedsearch',
    controller: 'advancedsearch',
    viewModel: 'advancedsearch',
    layout: {
        type: 'vbox',
        pack: 'start',
        align: 'stretch'
    },

    items: [
        {xtype: 'searchForm', title: '高级搜索'},
        {
            itemId: 'searchGrid',
            xtype: 'grid',
            listeners: {
                itemdblclick: 'showImage'
            },
            columns: [{
                header: 'Name',
                dataIndex: 'name',
                flex: 1
            }, {
                header: 'Type',
                dataIndex: '_type',
                flex: 1
            }, {
                header: 'CreatedOn',
                dataIndex: 'createdOn',
                xtype: 'datecolumn',
                format: 'Y-m-d',
                flex: 1
            }, {
                header: 'CreatedBy',
                dataIndex: 'createdBy',
                flex: 1
            }, {
                header: 'LastUpdatedOn',
                dataIndex: 'lastupdatedOn',
                xtype: 'datecolumn',
                flex: 1,
                format: 'Y-m-d'
            }, {
                header: 'LastUpdatedBy',
                dataIndex: 'lastupdatedBy',
                flex: 1
            }],
            dockedItems: [{
                xtype: 'pagingtoolbar',
                dock: 'bottom',
                displayInfo: true,
                displayMsg: 'Displaying Contents {0} - {1} of {2}',
                emptyMsg: "No Contents to display"
            }]
        }]

});
