Ext.define('explorer.view.main.ModifyDocument', {
    extend: 'Ext.form.Panel',
    xtype: 'modifydocument',

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
                anchor: '100%',
                readOnly : true
            },
            items: [
                {
                    fieldLabel: 'Type',
                    name: 'type'
                },{
                    name : 'name',
                    itemId: 'documentName',
                    fieldLabel: 'Name',
                    allowBlank: false,
                    readOnly : false
                },{
                    name : 'tag',
                    xtype: 'tagfield',
                    fieldLabel: 'Tag',
                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    store: Ext.create("Ext.data.Store", {
                        fields: ["name", "id"],
                        data: [
                            { name: "music", id: "music" },
                            { name: "sports", id: "sports" }
                        ]
                    }),
                    readOnly : false
                }, {
                    fieldLabel: 'CreatedBy',
                    name: 'createdBy'
                },{
                    fieldLabel: 'CreatedOn',
                    format : 'Y-m-d H:i:s',
                    xtype: 'datefield',
                    name: 'createdOn'
                },{
                    fieldLabel: 'LastUpdatedBy',
                    name: 'lastUpdatedBy'
                },{
                    fieldLabel: 'LastUpdatedOn',
                    format : 'Y-m-d H:i:s',
                    xtype: 'datefield',
                    name: 'lastUpdatedOn'
                },{
                    xtype: 'hiddenfield',
                    name: '_acl'
                },{
                    xtype: 'hiddenfield',
                    name: '_id'
                },{
                    xtype:'hidden',
                    name:'_method',
                    value:'PUT'
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
            },{
                xtype: 'hiddenfield',
                name: '_removeStreamIds',
                itemId: 'removeStreamIds'
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
        beforerender: 'loadData'
    }

});