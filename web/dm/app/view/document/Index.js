Ext.define('dm.view.document.Index', {
    extend: 'Ext.form.Panel',
    bodyPadding: 10,
    scrollable: true,

    layout: 'center',
    defaults: {
        anchor: '100%',
        width: '75%',
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


    buttons: [{
        text: 'Upload',
        handler: function () {
            var me = this.up('form');

            var type = me.down('combo[name=type]').getValue();
            var aclset = me.down('container[itemId=acl]');

            var acl = [];
            aclset.items.each(function (aceset) {
                var principals = Ext.Array.map(aceset.items.getAt(0).getValueRecords(), function (item) {
                    return item.get('_id');
                });
                var permission = Ext.Array.map(aceset.items.getAt(1).getValueRecords(), function (item) {
                    return item.get('field1');
                });
                acl.push({principals: principals, permission: permission});
            });

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

    addAce: function () {
        var me = this.up('form');
        var aclcontainer = this.up('container[itemId=acl]');
        aclcontainer.add({
            xtype: 'container',
            title: 'ace',
            layout: 'hbox',
            items: [
                {
                    fieldLabel: 'ACE',
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
                }, {
                    xtype: 'button',
                    text: '-',
                    handler: me.removeAce
                }
            ]
        });
    },

    removeAce: function () {
        var me = this.up('form');
        var aclcontainer = this.up('container[itemId=acl]');
        var acecontainer = this.up('container[title=ace]');
        aclcontainer.remove(acecontainer);

    },

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
    }
    ,

    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            items: [
                {
                    xtype: 'fieldset',
                    title: 'Index',
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
                            xtype: 'container',
                            itemId: 'acl',
                            layout: 'vbox',
                            items: [
                                {
                                    xtype: 'container',
                                    title: 'ace',
                                    layout: 'hbox',
                                    items: [
                                        {
                                            fieldLabel: 'ACE',
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
                                        }, {
                                            xtype: 'button',
                                            text: '+',
                                            handler: me.addAce
                                        }
                                    ]
                                }]
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
                    ]
                }
            ]
        });


        me.callParent();

    }

})
;