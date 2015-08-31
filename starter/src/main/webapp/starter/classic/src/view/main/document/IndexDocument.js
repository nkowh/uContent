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
    height: 650,
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
                    },
                    listeners: {
                        change:"changeType"
                    }
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
                                xtype: 'tagfield',
                                store: ['READ', 'WRITE','UPDATE','DELETE'],
                                forceSelection: true
                            }, {
                                xtype: 'button',
                                text: '+',
                                handler: 'addAcl'
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
                xtype: 'filefield',
                name: 'file',
                fieldLabel: 'Files',
                labelWidth: 50,
                msgTarget: 'side',
                anchor: '100%',
                buttonText: 'Select Photo...',
                listeners : {
                    afterrender: function (file, eOpts) {
                        var el = file.getEl();
                        file.fileInputEl.set({multiple: 'multiple'});
                    }
                }
            }]
        }];
        me.callParent();
    },
    buttons: [ {
        text: 'Save',
        handler : 'save'
    }],
    listeners: {
        beforerender: 'loadAclData'
    }

});