Ext.define('explorer.view.main.IndexDocument', {
    extend: 'Ext.form.Panel',
    xtype: 'indexdocument',

    controller: 'indexdoc',
    scrollable : 'y',
    fieldDefaults: {
        labelAlign: 'right',
        labelWidth: 115,
        msgTarget: 'side'
    },

    initComponent: function () {
        var me = this;
        this.items = [{
            xtype: 'fieldset',
            title: 'Document Info',
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
                    },
                    listeners: {
                        change:"changeTypeForIndex"
                    }
                },{
                    name : 'name',
                    itemId: 'documentName',
                    fieldLabel: 'Name',
                    allowBlank: false
                },{
                    name : 'tag',
                    xtype: 'tagfield',
                    fieldLabel: 'Tag',
                    displayField: 'tagContext',
                    valueField: '_id',
                    forceSelection: true,
                    store: Ext.create('Ext.data.Store', {
                        fields: ['_id','tagContext'],
                        proxy: {
                            type: 'ajax',
                            url: '/svc/tags',
                            reader: {
                                type: 'json',
                                rootProperty: 'tags'
                            }
                        },
                        autoLoad: true
                    })
                },{
                    xtype: 'hiddenfield',
                    name: '_acl'
                }
            ]
        }, {
            xtype: 'fieldset',
            itemId: 'propertyList',
            title: 'Contact Information',

            defaultType: 'textfield',
            defaults: {
                anchor: '100%'
            },

            items: []
        }, {
            xtype: 'fieldset',
            itemId: 'aclList',
            title: 'Acl Information',

            defaults: {
                anchor: '100%'
            },

            items: [{
                xtype: 'container',
                title: 'acl',
                layout: 'hbox',
                margin :  '2 5 2 5',
                items: [
                    {
                        fieldLabel: 'ACE',
                        xtype: 'tagfield',
                        name : 'operationObj',
                        displayField: 'name',
                        valueField: 'id',
                        forceSelection: true
                    }, {
                        xtype: 'textfield',
                        name : 'permission',
                        value : 'read',
                        readOnly : true
                    }
                ]
            },{
                xtype: 'container',
                title: 'acl',
                layout: 'hbox',
                margin :  '2 5 2 5',
                items: [
                    {
                        fieldLabel: 'ACE',
                        xtype: 'tagfield',
                        name : 'operationObj',
                        displayField: 'name',
                        valueField: 'id',
                        forceSelection: true
                    }, {
                        xtype: 'textfield',
                        name : 'permission',
                        value : 'write',
                        readOnly : true
                    }
                ]
            }]
        }, {
            xtype: 'fieldset',
            itemId: 'stream',
            title: 'Stream Information',

            defaults: {
                anchor: '100%'
            },

            items: [{
                xtype: 'multifile',
                name: 'file',
                fieldLabel: 'Files',
                msgTarget: 'side',
                anchor: '100%',
                buttonText: 'Select Photo...'
            }]
        }];
        me.callParent();
    },

    buttons: [ {
        text: 'Save',
        handler : 'save'
    },{
        text: 'Close',
        handler : 'closeWin'
    }],

    listeners: {
        beforerender: 'loadAclOperationObj'
    }

});