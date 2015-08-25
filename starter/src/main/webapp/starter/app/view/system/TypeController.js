Ext.define('starter.system.TypeController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.type',

    openCreateWin: function (sender, record) {
        var me = this;
        Ext.create('Ext.window.Window', {
            title:'新建类型',
            width: 1050,
            header: {
                titlePosition: 2,
                titleAlign: 'center'
            },
            height: 600,
            layout: {
                type: 'border',
                padding: 5
            },
            items:[{
                region: 'north',
                xtype: 'createTypeInfo'
            },{
                region: 'center',
                xtype: 'createProperty'
            }],
            buttons: [{
                text: 'Close',
                handler: function() {
                    this.up('window').close();
                }
            }, {
                text: 'Submit',
                handler : function(e){
                    var form = e.up('window').down('form');
                    var grid =  e.up('window').down('grid');
                    if (form.isValid()) {
                        var type = Ext.create('starter.model.Type', form.getValues());
                        var gstore = grid.store;
                        var record = gstore.getAt(0);
                        if(record&&!record.isValid()&&record.get('name')!=''){
                            return;
                        }
                        var properties = []
                        var size = gstore.getCount();
                        for(var i=0;i<size;i++){
                            var pRecord = gstore.getAt(i);
                            properties.push(pRecord.getData());
                        }
                        type.set('properties',properties);
                        type.phantom =true;
                        var store = me.getViewModel().getStore('types');
                        store.add(type);
                        e.up('window').close();
                    }
                }
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: {
                type: 'border',
                padding: 5
            },
            title:'修改类型',
            items:[{
                xtype: 'modifyType',
                record : record
            }]
        }).show();
        return ;
    },
    loadModifyData: function (e, eOpts) {
        var record = this.getView().record;
        this.getView().getForm().loadRecord(record);
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
            isFullTextIndex: false,
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
    }
});
