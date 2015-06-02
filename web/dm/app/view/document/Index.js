Ext.define('dm.view.document.Index', {
    extend: 'Ext.form.Panel',
    bodyPadding: 10,
    scrollable: true,

    defaults: {
        anchor: '100%',
        allowBlank: false,
        msgTarget: 'side',
        labelWidth: 50
    },

    listeners: {
        afterrender: function () {
            var me = this;
            Ext.Ajax.request({
                url: Ext.util.Cookies.get('service') + '/documents/_mappings',
                callback: function (options, success, response) {
                    if (!success)return;
                    var mappings = Ext.decode(response.responseText).documents.mappings;
                    me.down('combo[name=type]').bindStore(Ext.create('Ext.data.Store', {
                        fields: ['name'], data: Ext.Array.map(Ext.Object.getAllKeys(mappings), function (key) {
                            return [key];
                        })
                    }));
                }
            });
        }
    },

    items: [
        {
            xtype: 'combo',
            fieldLabel: 'Type',
            name: 'type',
            displayField: 'name',
            valueField: 'name',
            allowBlank: false,
            forceSelection: true
        }, {
            xtype: 'filefield',
            emptyText: 'Select an File',
            fieldLabel: 'Image',
            name: 'File',
            buttonText: '',
            buttonConfig: {
                glyph: 0xf0c6
            },
            listeners: {
                afterrender: function (file, eOpts) {
                    var el = file.getEl();
                    file.fileInputEl.set({multiple: 'multiple'});
                }
            }
        }, {
            xtype: 'fieldset',
            title: 'ACL',
            layout: 'hbox',
            items: [
                {
                    xtype: 'tagfield',
                    allowBlank: false,
                    store: Ext.create('dm.store.system.Users'),
                    displayField: '_id',
                    valueField: '_id',
                    forceSelection: true
                }, {
                    xtype: 'tagfield',
                    allowBlank: false,
                    store: ['read', 'write'],
                    forceSelection: true
                },
            ]
        }, {
            xtype: 'textareafield',
            grow: true,
            allowBlank: true,
            name: 'meta',
            fieldLabel: 'Meta',
            anchor: '100%'
        }, {
            xtype: 'hiddenfield',
            name: 'metadata'
        }
    ],
    //errorReader: Ext.create('dm.data.reader.SubmitResultReader', {}),

    buttons: [{
        text: 'Upload',
        handler: function () {
            var me = this.up('form');

            var type = me.down('combo[name=type]').getValue();
            var aclset = me.down('fieldset[title=ACL]');
            var principals = Ext.Array.map(aclset.items.getAt(0).getValueRecords(), function (item) {
                return item.get('_id');
            });
            var permission = Ext.Array.map(aclset.items.getAt(1).getValueRecords(), function (item) {
                return item.get('field1');
            });
            var acl = [];
            acl.push({principals: principals, permission: permission});

            var metadataField = me.down('hiddenfield[name=metadata]');
            var metaText = me.down('textareafield[name=meta]').getValue();
            var meta = {};
            try {
                meta = Ext.decode(metaText);
            } catch (e) {

            }
            meta['_acl'] = acl;
            metadataField.setValue(Ext.encode(meta));
            var form = me.getForm();
            var uuid = Ext.data.identifier.Uuid.createRandom()();
            var documentUrl = Ext.util.Cookies.get('service') + '/documents/' + type + '/' + uuid;
            if (form.isValid()) {
                form.submit({
                    url: documentUrl,
                    waitMsg: 'uploading ...',
                    failure: function (fp, o) {
                        me.checkResult(documentUrl);
                    },
                    success: function (fp, o) {
                        me.checkResult(documentUrl);
                    }
                });
            }
        }
    }, {
        text: 'Reset',
        handler: function () {
            this.up('form').getForm().reset();
        }
    }],


    checkResult: function (url) {
        Ext.Ajax.request({
            url: url,
            success: function (response, opts) {
                Ext.toast({
                    html: 'success',
                    closable: false,
                    align: 't',
                    slideInDuration: 400,
                    minWidth: 400
                });
            },
            failure: function (response, opts) {
                Ext.toast({
                    html: 'failure',
                    closable: false,
                    align: 't',
                    slideInDuration: 400,
                    minWidth: 400
                });
            }
        });
    },

    initComponent: function () {

        this.callParent();

    }

});