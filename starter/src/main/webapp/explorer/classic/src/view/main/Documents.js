Ext.define('explorer.view.main.Documents', {
    extend: 'Ext.grid.Panel',
    xtype: 'documents',

    controller: 'document',
    //bind: {
    //    store :  '{documents}'
    //},
    //session: true,
    //store: {type: 'documents'},
    selType: 'checkboxmodel',
    //initComponent: function () {
        //var query = this.getView().query;
        //var params = {};
        //if (this.getView().limit && this.getView().limit != '') {
        //
        //    params = {
        //        highlight: true,
        //        query: query,
        //        limit: this.getView().limit
        //    };
        //} else {
        //    params = {
        //        highlight: false,
        //        query: query,
        //        types: this.getView().qType
        //    };
        //}
        //var store = Ext.create('explorer.store.Documents', {
        //    autoLoad: true
        //});
        //store.getProxy().extraParams = params;
        //e.bindStore(store);
        //this.down('pagingtoolbar').bindStore(store);

        //Ext.apply(this, {
        //    store: store,
        //    dockedItems: [{
        //        xtype: 'toolbar',
        //        dock: 'top',
        //        items: ['->',
        //            {xtype: 'button', text: '详细信息', handler: 'detail'},
        //            {xtype: 'button', text: '删除', handler: 'deleteDoc'}
        //        ]
        //    }, {
        //        xtype: 'pagingtoolbar',
        //        dock: 'bottom',
        //        displayInfo: true,
        //        displayMsg: 'Displaying Contents {0} - {1} of {2}',
        //        emptyMsg: "No Contents to display"
        //    }]
        //});
    //    this.callParent();
    //},
    columns: [{
        header: 'Name',
        dataIndex: 'name'
    }, {
        header: 'Type',
        dataIndex: '_type',
        flex: 1
    }, {
        header: 'CreatedOn',
        dataIndex: 'createdOn',
        xtype: 'datecolumn',
        format: 'Y-m-d H:i:s',
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
        format: 'Y-m-d H:i:s'
    }, {
        header: 'LastUpdatedBy',
        dataIndex: 'lastupdatedBy',
        flex: 1
    }],

    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            {xtype: 'button', text: '详细信息', handler: 'detail'},
            {xtype: 'button', text: '删除', handler: 'deleteDoc'}
        ]
    }, {
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        displayInfo: true,
        displayMsg: 'Displaying Contents {0} - {1} of {2}',
        emptyMsg: "No Contents to display"
    }],

    listeners: {
        afterrender: 'loadData',
        itemdblclick: 'showImage'
    }
});
