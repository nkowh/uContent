Ext.define('starter.view.main.document.AdvancedSearch', {
    extend: 'Ext.panel.Panel',
    xtype: 'advancedsearch',
    title: '',
    controller: 'advancedsearch',
    viewModel: 'advancedsearch',
    layout: {
        type: 'vbox',
        pack: 'start',
        align: 'stretch'
    },

    defaults: {
        frame: true,
        bodyPadding: 10
    },
    fieldDefaults: {
        labelAlign: 'left',
        labelWidth: 150,
        msgTarget: 'side'
    },

    items: [Ext.create('starter.view.main.document.SearchForm'),{
        title: 'Result List',
        collapsible: true,
        itemId : 'searchGrid',
        xtype : 'grid',
        columns: [{
            header: 'Name',
            dataIndex: 'name',
            flex : 1,
            width: 200
        }, {
            header: 'Type',
            dataIndex: '_type',
            width: 150
        }, {
            header: 'CreatedOn',
            dataIndex: 'createdOn',
            xtype: 'datecolumn',
            width: 150,   format:'Y-m-d'

        }, {
            header: 'CreatedBy',
            dataIndex: 'createdBy',
            width: 150
        }, {
            header: 'LastUpdatedOn',
            dataIndex: 'lastupdatedOn',
            xtype: 'datecolumn',
            width: 150,   format:'Y-m-d'
        }, {
            header: 'LastUpdatedBy',
            dataIndex: 'lastupdatedBy',
            width: 150
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
