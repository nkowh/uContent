Ext.define('explorer.view.main.IndexDocController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.indexdoc',
    indexDoc : function(bt,e){
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '新建文档',
            width : 1000,
            height : 600,
            items: [{
                xtype: 'indexdocument'
            }]
        }).show();
    },
    changeTypeForIndex: function (combo, newValue, oldValue, eOpts) {
        var me = this;
        if (newValue && newValue != oldValue) {

            var type = newValue;
            me.drawPeopertyByType(type);
        }

    },
    drawPeopertyByType : function(type,callback){
        var me = this;
        var form = this.getView();
        var fieldset = form.down('fieldset[itemId="propertyList"]');
        fieldset.removeAll(true);
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
                    if(callback){
                        callback();
                    }
                }
            }
        });
    },
    drawPeopertyField: function (property) {
        var type = property.type;
        var field = {};
        if (type == 'string') {
            field = {
                fieldLabel: property.name,
                name: property.name,
                value: property.defaultValue,
                regex : property.pattern,
                regexText : property.promptMessage
            };
        }
        if (type == 'integer' || type == 'float') {
            field = {
                xtype: 'numberfield',
                fieldLabel: property.name,
                minValue: -2147483647,
                maxValue: 2147483647,
                name: property.name,
                value: property.defaultValue,
                regex : property.pattern,
                regexText : property.promptMessage
            };
        }
        if (type == 'boolean') {
            field = {
                xtype: 'combobox',
                name: property.name,
                fieldLabel: property.name,
                value: property.defaultValue,
                minWidth: 100,
                store: [true, false],
                regex : property.pattern,
                regexText : property.promptMessage
            }
        }
        if (type == 'date') {
            field = {
                xtype: 'datefield',
                fieldLabel: property.name,
                anchor: '100%',
                altFormats : 'c',
                format : 'Y-m-d',
                name: property.name,
                regex : property.pattern,
                regexText : property.promptMessage
            };
            if(property.defaultValue!=''){
                field.value = new Date(property.defaultValue);
            }
        }
        if (property.required) {
            field.allowBlank = false;
        }
        return field;
    },
    save: function () {
        var me = this;
        var form = this.getView().getForm();
        if (form.isValid()) {
            var type = this.getView().down('combo[name=type]');
            var name = this.getView().down('textfield[itemId=documentName]').getValue();
            var aclcontainer = this.getView().down('fieldset[itemId=aclList]');
            var aclItems = aclcontainer.items;
            if (aclItems) {
                var flag = false;
                var _acl = {"read":{"users":[],"groups":[]},"write":{"users":[],"groups":[]}};
                    var readOperationObj = aclItems.items[0].child('tagfield[name="operationObj"]').getValueRecords();
                    var writeOperationObj = aclItems.items[1].child('tagfield[name="operationObj"]').getValueRecords();

                    Ext.Array.each(readOperationObj, function (operationObj, index, countriesItSelf) {
                            if (operationObj.get('isUser')) {
                                _acl.read.users.push(operationObj.get('id'));
                                flag = true;
                            }
                            if (operationObj.get('isGroup')) {
                                _acl.read.groups.push(operationObj.get('id'));
                                flag = true;
                            }

                    });
                Ext.Array.each(writeOperationObj, function (operationObj, index, countriesItSelf) {
                        if (operationObj.get('isUser')) {
                            _acl.write.users.push(operationObj.get('id'));
                            flag = true;
                        }
                        if (operationObj.get('isGroup')) {
                            _acl.write.groups.push(operationObj.get('id'));
                            flag = true;
                        }
                });
                if(flag){
                    this.getView().down('hiddenfield[name=_acl]').setValue(Ext.encode(_acl));
                }else{
                    this.getView().down('hiddenfield[name=_acl]').setValue('');
                }
            }
            var url = '';
            var msg = "";
            if( this.getView().down('hiddenfield[name=_id]')&&this.getView().down('hiddenfield[name=_id]').getValue()!=''){
                url = '/svc/' +type.getValue()+'/'+this.getView().down('hiddenfield[name=_id]').getValue();
                msg ='Update';
            }else{
                url = '/svc/'+type.getValue();
                msg ='Create';
            }
            form.submit({
                url: url,
                waitMsg: 'uploading ...',
                success: function (form, action) {
                    me.getView().up('window').close();
                    Ext.toast({
                        html: msg+'successful',
                        title: 'message',
                        width: 200,
                        align: 't'
                    });

                },
                failure: function (form, action) {
                    if (action.response.status === 200) {
                        me.getView().up('window').close();
                        Ext.toast({
                            html: msg+'successful',
                            title: 'message',
                            width: 200,
                            align: 't'
                        });
                    } else {

                        var error = Ext.decode(action.responseText);
                        Ext.toast({
                            html: msg+' Error!<br />'+error.status+':'+error.reason,
                            title: 'message',
                            width: 200,
                            align: 't'
                        });

                    }
                }
            });

        }
    },
    loadAclOperationObj: function (acl) {
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
                        Ext.Array.each(me.getView().query('tagfield[name="operationObj"]'), function(obj, index, countriesItSelf) {
                            obj.bindStore(
                                Ext.create('Ext.data.Store', {
                                    fields: ['id', 'name', 'isUser', 'isGroup'],
                                    data: data
                                }));
                        });
                        if(acl&&acl.read){
                            me.getView().query('tagfield[name="operationObj"]')[0].setValue(acl.read);
                        }
                        if(acl&&acl.write){
                            me.getView().query('tagfield[name="operationObj"]')[1].setValue(acl.write);
                        }

                    }
                });
            }
        });
    },
    loadData : function(){
        var data = this.getView().docData;
        var form = this.getView().getForm();
        var type = data.get('_type');
        form.findField('type').setValue(type);
        form.findField('tag').setValue(data.get('tag'));
        var streams = data.get('_streams');
        var _acl = data.get('_acl');
        var acl = {read:[],write:[]};
        Ext.Object.each(_acl, function(key, value, myself) {
            if (key === 'read') {
                if(value){
                    Ext.Object.each(value, function(k, val, myself) {
                        if(val){
                            acl.read.push(val);
                        }
                    });
                }
            }
            if (key === 'write') {
                if(value){
                    Ext.Object.each(value, function(k, val, myself) {
                        if(val){
                            acl.write.push(val);
                        }
                    });
                }
            }
        });
        this.loadAclOperationObj(acl);
        this.drawStreamTable(streams);
        this.drawPeopertyByType(type,function(){
            form.loadRecord(data);
        });

    },
    closeWin : function(){
        this.getView().up('window').close();
    },
    drawStreamTable : function(streams){
        var me = this;
        var streamcontainer = this.getView().down('fieldset[itemId=stream]');
        var removeStreamIds = this.getView().down('hiddenfield[itemId=removeStreamIds]');
        Ext.Array.each(streams, function(stream, index, countriesItSelf) {
            if(stream&&stream.streamName!=''){
                streamcontainer.insert(index,{
                    xtype: 'container',
                    layout: 'hbox',
                    items: [
                        {
                            fieldLabel :'StreamName',
                            xtype: 'textfield',
                            name : 'streamName',
                            value : stream.streamName,
                            readOnly : true
                        },
                        {
                            xtype: 'button',
                            text: '-',
                            fieldReference: 'fieldInterval',
                            style: {
                                'margin-left': '10px'
                            },
                            streamId : stream.streamId,
                            listeners: {
                                click: function(bt,e){
                                    var stream = bt.up('container');
                                    var streamcontainer = me.getView().down('fieldset[itemId=stream]');
                                    var streamId = bt.streamId;
                                    var vals =[];
                                    if(removeStreamIds.getValue()&&removeStreamIds.getValue().length>0){
                                        vals =  removeStreamIds.getValue().split(',');
                                    }
                                    vals.push(streamId);
                                    removeStreamIds.setValue(vals);
                                    streamcontainer.remove(stream);
                                }
                            }
                        }
                    ]
                });
            }
        });
    }
});
