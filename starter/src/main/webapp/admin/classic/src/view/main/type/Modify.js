Ext.define('admin.view.main.type.Modify', {
    extend: 'Ext.window.Window',
    xtype: 'modifytype',
    controller: 'type',
    viewModel: 'type',
    title:'修改类型',
    width: 1300,
    height: 600,
    layout: {
        type: 'border',
        padding: 5
    },
    items:[{
        region: 'north',
        xtype: 'modifyTypeInfo'
    },{
        region: 'center',
        xtype: 'grid',
        bind: {
            title: '{propertyTitle}',
            store : '{properties}'
        },
        columns: [{
            header: 'Name',
            dataIndex: 'name',
            width: 150
        }, {
            header: 'Type',
            dataIndex: 'type',
            width: 100
        },  {
            header: 'DefaultValue',
            dataIndex: 'defaultValue',
            width: 130
        }, {
            header: 'Pattern',
            dataIndex: 'pattern',
            width: 200
        }, {
            header: 'PromptMessage',
            dataIndex: 'promptMessage',
            width: 220
        }, {
            header: 'Required',
            dataIndex: 'required',
            width: 80

        }, {
            header: 'Index',
            dataIndex: 'index',
            width: 100
        },{
            header: 'IndexAnalyzer',
            dataIndex: 'indexAnalyzer',
            width: 100
        },{
            header: 'SearchAnalyzer',
            dataIndex: 'searchAnalyzer',
            width: 100
        },  {
            header: 'Order',
            dataIndex: 'order',
            width: 80
        }],
        dockedItems: [{
            xtype: 'toolbar',
            dock: 'top',
            items: ['->',
                { xtype: 'button', text: 'Add', handler: 'addProperty' },
                { xtype: 'button', text: 'Delete',handler: 'deleteProperty'}
            ]
        }],
        listeners: {
            itemdblclick: 'modifyProperty'
        }
    }],
    buttons: [{
        text: 'Close',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: 'Submit',
        handler : 'modifySave'
    }],
    listeners: {
        afterrender:'loadModifyData'
    }

});