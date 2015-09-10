Ext.define('starter.view.system.TypeController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.type',

    openCreateWin: function (sender, record) {
        var me = this;
        Ext.create('starter.view.main.system.type.Create').show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        var me = this;
        Ext.create('starter.view.main.system.type.Modify',{ record : record}).show();
        return ;
    },
    refreshType: function (sender, record) {
        var me = this;
        var store = this.getViewModel().getStore('types');
        store.load();
        return ;
    },
    loadModifyData: function() {
        var me = this;
        var record =  this.getView().record;
        this.getView().down('form').loadRecord(record);
        var type = record.get('name');
        Ext.Ajax.request({
            url: '/svc/types/'+type,
            callback: function (options, success, response) {
                if(!success){
                    return ;
                }
                if(response.responseText!=''){
                    var properties = Ext.decode(response.responseText);
                    //Ext.Array.each(properties.properties, function(property, index, countriesItSelf) {
                    //    if(property.index=='analyzed'){
                    //        property.isFullIndex = true;
                    //    }
                    //    if(property.index=='not_analyzed'){
                    //        property.isFullIndex = false;
                    //    }
                    //});
                    me.getView().down('grid').bindStore(
                        Ext.create('Ext.data.Store', {
                            model: 'starter.model.Property',
                            data : properties.properties,
                            initData : properties.properties
                        }));

                }
            }
        });
    },
    deleteType : function(e){
        var me = this;
        var record = this.getView().getSelectionModel().getSelection();
        var name = record[0].get('name');
        if(record&&record.length>0){
            Ext.Msg.confirm("Title","Are you sure to delete this Type？",function(r) {
                me.getViewModel().getStore('types').remove(record[0]);
            });

        }else{
            Ext.Msg.alert('message', 'Please select one item at least.');
            return ;
        }
    },
    addProperty : function(e){
        var store = this.getViewModel().getStore('properties');
        var order = store.data.length+1;
        var record = store.getAt(0);
        // Create a model instance
        if(store.getCount()==0||record.isValid()){
        var r = Ext.create('starter.model.Property', {
            name: '',
            type: 'string',
            pattern: '',
            promptMessage: '',
            index : 'analyzed',
            defaultValue: '',
            required: false,
            order :order
        });

        store.insert(0, r);
        }
    },
    validateByType :function(type,obj){
        if(type=='integer'){
            obj.regex =/^\d+$/;
            obj.regexText = '请输入整形';
        }
        if(type=='string'){
            obj.regex ='';
            obj.regexText = '';
        }
        if(type=='float'){
            obj.regex =/^(-?\d+)(\.\d+)?$/;
            obj.regexText = '请输入浮点型';
        }
        if(type=='boolean'){
            obj.regex = /0|1/;;
            obj.regexText = '请输入布尔型';
        }
        if(type=='date'){
            obj.regex =/^[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))$/;
            obj.regexText = '请输入日期型，例如（2004-04-31）';
        }
    },
    validateProperty : function(editor, e){
        var type = e.record.get('type');
        var defaultValue = e.record.get('defaultValue');
        var pattern = e.record.get('pattern');
        var promptMessage = e.record.get('promptMessage');
        if(e.field=='name'||e.field=='type'||e.field=='index'){
            if(e.grid.store.initData){
                if(Ext.Array.contains(e.grid.store.initData, e.record.data)){
                    e.cancel = true;
                }
            }
        }
        if(e.field=='defaultValue') {
            if (type != '') {
                this.validateByType(type, e.column.field);
            }
            if (pattern != '') {
                e.column.field.regex = pattern;
            }
            if (promptMessage != '') {
                e.column.field.regexText = promptMessage;
            }
        }

    },
    deleteProperty : function(e){
        var grid =  this.getView();
        var store = this.getViewModel().getStore('properties');
        var sm = grid.getSelectionModel();
        store.remove(sm.getSelection());
        if (store.getCount() > 0) {
            sm.select(0);
        }
    },
    deleteModifyProperty : function(e){
        var grid =  this.getView();
        var store = grid.store;
        var sm = grid.getSelectionModel();
        var record = sm.getSelection()[0];

        if(record){
            var isOld = Ext.Array.contains(store.initData, record.data);
            if(isOld){
                Ext.Msg.alert('message', 'You cannot delete the property.');
                return ;
            }else
            store.remove(record);
        }
        if (store.getCount() > 0) {
            sm.select(0);
        }
    },
    addModifyProperty : function(e){
        var grid =  this.getView();
        var store = grid.store;
        var order = store.data.length+1;
        var record = store.getAt(0);
        // Create a model instance
        if(store.getCount()==0||record.isValid()){
            var r = Ext.create('starter.model.Property', {
                name: '',
                type: 'string',
                pattern: '',
                promptMessage: '',
                defaultValue: '',
                index : 'analyzed',
                required: false,
                order :order
            });

            store.insert(0, r);
        }
    },

    createSave : function(e){
        var me = this;
        var form = e.up('window').down('form');
        var grid =  e.up('window').down('grid');
        var store = me.getViewModel().getStore('types');
        if (form.isValid()) {
            var type = form.getValues();
            var gstore = grid.store;
            var record = gstore.getAt(0);
            if(record&&!record.isValid()&&record.get('name')!=''){
                return;
            }
            var properties = []
            var size = gstore.getCount();
            for(var i=0;i<size;i++){
                var pRecord = gstore.getAt(i);
                if(pRecord.get('type')=='boolean'){
                    pRecord.set('index','not_analyzed');
                }
                properties.push(pRecord.getData());
            }
            type.properties = properties;
            //type.set('properties',properties);
            Ext.Ajax.request({
                method: 'POST',
                headers : {'Content-Type':'application/json;charset=utf-8'},
                url: '/svc/types',
                params : Ext.JSON.encode(type),
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    Ext.toast({
                        html: 'Save successful',
                        title: 'message',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                    me.getView().close();
                }
            });
        }

    },

    modifySave : function(e){
        var me = this;
        var form = e.up('window').down('form');
        var grid =  e.up('window').down('grid');
        var store = me.getViewModel().getStore('types');
        if (form.isValid()) {
            var type = form.getValues();
            var gstore = grid.store;
            var record = gstore.getAt(0);
            if(record&&!record.isValid()&&record.get('name')!=''){
                return;
            }
            var properties = []
            var size = gstore.getCount();
            for(var i=0;i<size;i++){
                var pRecord = gstore.getAt(i);
                //if(pRecord.get('type')!='boolean'){
                //    if(pRecord.get('isFullIndex')){
                //        pRecord.set('index','analyzed');
                //    }else{
                //        pRecord.set('index','not_analyzed');
                //
                //    }
                //}else{
                //    pRecord.set('index','not_analyzed');
                //}
                properties.push(pRecord.getData());
            }
            type.properties = properties;
            //type.set('properties',properties);
            Ext.Ajax.request({
                method: 'PATCH',
                headers : {'Content-Type':'application/json;charset=utf-8'},
                url: '/svc/types/' + type.name,
                params : Ext.JSON.encode(type),
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    Ext.toast({
                        html: 'Save successful',
                        title: 'message',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                    me.getView().close();
                }
            });
        }

    }
});
