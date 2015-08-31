Ext.define('starter.view.main.document.AdvancedSearch', {
    extend: 'Ext.panel.Panel',
    xtype: 'advancedsearch',
    title: 'AdvancedSearch',
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
            width: 130
        }, {
            header: 'Type',
            dataIndex: 'type',
            width: 100
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
