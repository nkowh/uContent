Ext.define('starter.view.document.IndexDocumentController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.indexdocument',

    changeType: function (combo, newValue, oldValue, eOpts) {
        var me = this;
        if (newValue && newValue != oldValue) {
            var form = this.getView();
            var fieldset = form.down('fieldset[itemId="propertyList"]');
            fieldset.removeAll(true);
            var type = newValue;
            Ext.Ajax.request({
                url: '/svc/types/' + type,
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    if (response.responseText != '') {
                        var properties = Ext.decode(response.responseText);
                        Ext.Array.each(properties.properties, function (property, index, countriesItSelf) {
                            var field = me.drawPeopertyField(property);
                            fieldset.add(field);
                        });

                    }
                }
            });
        }

    },
    drawPeopertyField: function (property) {
        var type = property.type;
        var field = {};
        if (type == 'string') {
            field = {
                fieldLabel: property.name,
                name: property.name,
                value: property.defaultValue
            };
        }
        if (type == 'integer' || type == 'float') {
            field = {
                xtype: 'numberfield',
                fieldLabel: property.name,
                minValue: -2147483647,
                maxValue: 2147483647,
                name: property.name,
                value: property.defaultValue
            };
        }
        if (type == 'boolean') {
            field = {
                xtype: 'combobox',
                name: property.name,
                fieldLabel: property.name,
                value: property.defaultValue,
                minWidth: 100,
                store: [true, false]
            }
        }
        if (type == 'date') {
            field = {
                xtype: 'datefield',
                fieldLabel: property.name,
                anchor: '100%',
                format: 'Y-m-d',
                name: property.name,
                value: property.defaultValue
            };
        }
        if (property.required) {
            field.allowBlank = false;
        }
        return field;
    },
    addAcl: function () {
        var me = this;
        var aclcontainer = this.getView().down('fieldset[itemId=aclList]');
        var index = aclcontainer.items.length;
        aclcontainer.add({
            xtype: 'container',
            title: 'acl',
            layout: 'hbox',
            margin: '2 5 2 5',
            items: [
                {
                    fieldLabel: 'ACE',
                    xtype: 'tagfield',
                    name: 'operationObj',
                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true
                }, {
                    xtype: 'tagfield',
                    name: 'permission',
                    store: ['READ', 'WRITE'],
                    forceSelection: true
                }, {
                    xtype: 'button',
                    text: '-',
                    handler: function (e) {
                        var acl = e.up('container');
                        var aclcontainer = me.getView().down('fieldset[itemId=aclList]');
                        aclcontainer.remove(acl);

                    }
                }
            ]
        });

        this.loadAclOperationObj(index);
    },
    save: function () {
        var form = this.getView().getForm();
        if (form.isValid()) {
            var type = this.getView().down('combo[name=type]');
            var name = this.getView().down('textfield[itemId=documentName]').getValue();
            var aclcontainer = this.getView().down('fieldset[itemId=aclList]');
            var aclItems = aclcontainer.items;
            if (aclItems) {
                var _acl = {
                    read: {users: [], groups: []},
                    write: {users: [], groups: []}
                };
                Ext.Array.each(aclItems.items, function (aclItem) {
                    var operationObjs = aclItem.child('tagfield[name="operationObj"]').getValueRecords();
                    var permissions = aclItem.child('tagfield[name="permission"]').getValue();
                    Ext.Array.each(permissions, function (permission) {
                        Ext.Array.each(operationObjs, function (operationObj) {
                            if (operationObj.get('isUser'))
                                _acl[permission.toLowerCase()].users.push(operationObj.get('name'));
                            if (operationObj.get('isGroup'))
                                _acl[permission.toLowerCase()].groups.push(operationObj.get('name'));
                        });
                    });
                });
                if (_acl.read.users.length === 0 && _acl.read.groups.length === 0
                    && _acl.write.groups.length === 0 && _acl.write.users.length === 0)
                    _acl = undefined;
                this.getView().down('hiddenfield[name=_acl]').setValue(Ext.encode(_acl));
            }

            form.submit({
                url: '/svc/' + type.getValue(),
                waitMsg: 'uploading ...',
                success: function (form, action) {
                    Ext.toast({
                        html: 'Docuemnt Saved',
                        title: 'message',
                        width: 200,
                        align: 't'
                    });
                    form.reset();
                },
                failure: function (form, action) {
                    Ext.toast({
                        html: 'error',
                        title: 'message',
                        width: 200,
                        align: 't'
                    });
                }
            });

        }
    },
    loadAclData: function () {
        this.loadAclOperationObj(0);
    },
    loadAclOperationObj: function (index) {
        var me = this;
        var userResult = [];
        var groupResult = [];
        var data = [];
        Ext.Ajax.request({
            url: '/svc/users?limit=100000',
            callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                if (response.responseText != '') {
                    var users = Ext.decode(response.responseText);
                    userResult = Ext.Array.map(users.users, function (item, index) {
                        return {'id': item.userId, 'name': item.userName, 'isUser': true, 'isGroup': false};
                    });
                }
                Ext.Ajax.request({
                    url: '/svc/groups?limit=100000',
                    callback: function (options, success, response) {
                        if (!success) {
                            return;
                        }
                        if (response.responseText != '') {
                            var groups = Ext.decode(response.responseText);
                            groupResult = Ext.Array.map(groups.groups, function (item, index) {
                                return {'id': item.groupId, 'name': item.groupName, 'isUser': false, 'isGroup': true};
                            });
                        }
                        data = Ext.Array.merge(userResult, groupResult);
                        me.getView().query('tagfield[name="operationObj"]')[index].bindStore(
                            Ext.create('Ext.data.Store', {
                                fields: ['id', 'name', 'isUser', 'isGroup'],
                                data: data
                            }));
                    }
                });
            }
        });
    }
});
