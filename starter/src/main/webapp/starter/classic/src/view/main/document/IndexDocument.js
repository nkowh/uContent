Ext.define('starter.view.main.document.IndexDocument', {
    extend: 'Ext.form.Panel',
    xtype: 'indexdocument',

    controller: 'indexdocument',
    viewModel: 'indexdocument',

    title:{
        bind: {
            text: '{title}'
        }
    },

    fieldDefaults: {
        labelAlign: 'right',
        labelWidth: 115,
        msgTarget: 'side'
    },
    listeners:{
        afterrender:'loadEntityTypes'
    },

    initComponent: function () {
        var me = this;
        this.items = [{
            xtype: 'fieldset',
            title: 'User Info',
            defaultType: 'textfield',
            defaults: {
                anchor: '100%'
            },
            items: [
                Ext.create('Ext.form.ComboBox', {
                    fieldLabel: '文档类型',
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'entityType',
                    listeners: {
                        change:"changeType"
                    }
                })
            ]
        }, {
            xtype: 'fieldset',
            itemId: 'documenttype',
            title: 'Contact Information',

            defaultType: 'textfield',
            defaults: {
                anchor: '100%'
            },

            items: []
        }];
        me.callParent();
    }

});