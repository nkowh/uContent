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
                {
                    xtype: 'combo',
                    fieldLabel: 'Type',
                    name: 'type',
                    displayField: 'displayName',
                    allowBlank: false,
                    valueField: 'name',
                    bind: {
                        store :  '{types}'
                        //value : '{tValue}'
                    },
                    listeners: {
                        change:"changeType"
                    }
                }
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