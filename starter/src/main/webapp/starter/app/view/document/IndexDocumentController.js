Ext.define('starter.view.document.IndexDocumentController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.indexdocument',

    changeType: function (combo, newValue, oldValue, eOpts) {
        var me = this;
        if(newValue&&newValue!=oldValue){
            var form = this.getView();
            var fieldset = form.down('fieldset[itemId="propertyList"]');
            fieldset.removeAll(true);
            var type = newValue;
            Ext.Ajax.request({
                url: '/svc/types/'+type,
                callback: function (options, success, response) {
                    if(!success){
                        return ;
                    }
                    if(response.responseText!=''){
                        var properties = Ext.decode(response.responseText);
                        Ext.Array.each(properties.properties, function(property, index, countriesItSelf) {
                            fieldset.add({
                                fieldLabel: property.name,
                                name: property.name
                            });
                        });

                    }
                }
            });
        }

    },
    drawPeopertyField : function(type,property){
        var field = {};
        if(type=='string'){
            field = {
                fieldLabel: property.name,
                name: property.name
            };
        }
        if(type=='integer'){
            field = {
                xtype: 'numberfield',
                fieldLabel: property.name,
                minValue: -2147483647,
                maxValue: 2147483647,
                name: property.name
            };
        }
        if(type=='float'){

        }
        if(type=='boolean'){

        }
        if(type=='date'){

        }
    },
    addAcl : function(){
        var me = this;
        var aclcontainer = this.getView().down('fieldset[itemId=aclList]');
        var index = aclcontainer.items.length;
        aclcontainer.add({
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
                    text: '-',
                    handler:  function (e) {
                        var acl = e.up('container');
                        var aclcontainer = me.getView().down('fieldset[itemId=aclList]');
                        aclcontainer.remove(acl);

                    }
                }
            ]
        });

        this.loadAclOperationObj(index);
    },
    save : function(){
        var form = this.getView().getForm();
        if (form.isValid()) {

        }
    },
    loadAclData : function(){
        this.loadAclOperationObj(0);
    },
    loadAclOperationObj : function(index){
        var me = this;
        var userResult = [];
        var groupResult = [];
        var data = [];
        Ext.Ajax.request({
            url: '/svc/users?limit=100000',
            callback: function (options, success, response) {
                if(!success){
                    return ;
                }
                if(response.responseText!=''){
                    var users = Ext.decode(response.responseText);
                    userResult = Ext.Array.map(users.users,function(item,index){
                        return {'id':item._id,'name':item.userName,'isUser':true,'isGroup':false};
                    });
                }
                Ext.Ajax.request({
                    url: '/svc/groups?limit=100000',
                    callback: function (options, success, response) {
                        if(!success){
                            return ;
                        }
                        if(response.responseText!=''){
                            var groups = Ext.decode(response.responseText);
                            groupResult = Ext.Array.map(groups.groups,function(item,index){
                                return {'id':item._id,'name':item.groupName,'isUser':false,'isGroup':true};
                            });
                        }
                        data = Ext.Array.merge( userResult, groupResult) ;
                        me.getView().query('tagfield[name="operationObj"]')[index].bindStore(
                            Ext.create('Ext.data.Store', {
                            fields : ['id','name','isUser','isGroup'],
                            data : data
                        }));
                    }
                });
            }
        });
    }
});
