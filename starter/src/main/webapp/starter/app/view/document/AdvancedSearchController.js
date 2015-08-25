Ext.define('starter.document.AdvancedSearchController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.advancedsearch',
    loadConditionField : function(aPanel,type,index){
        var me = this;
            Ext.Ajax.request({
                url: '/svc/types/'+type,
                callback: function (options, success, response) {
                    if(!success){
                        return ;
                    }
                    if(response.responseText!=''){
                        var properties = Ext.decode(response.responseText);
                        aPanel.query('combobox[name="property"]')[index].bindStore(
                            Ext.create('Ext.data.Store', {
                                model : Ext.create('starter.model.Property'),
                                data: properties.properties
                            })
                        );
                    }
                }
            });
    },
    //loadPropertyField : function(){
    //    var combo = this.getView().child('combobox[name="type"]');
    //    var type = this.getViewModel().getStore('types').getAt(0).get('name');
    //    combo.setValue(type);
    //    this.loadConditionField(type,0);
    //},
    changeType : function( e, newValue, oldValue, eOpts ){
        if(newValue&&newValue!=oldValue){
            var advPanel = this.getView();
            if(oldValue&&oldValue!=''){
                this.initCondition();
                this.drawCondition(advPanel,newValue);
            }
            this.loadConditionField(advPanel,newValue,0);
        }
    },
    initCondition : function(){
        var advPanel =  this.getView();
        var fieldset =  advPanel.child('fieldset');
        fieldset.removeAll();
    },
    changeOperator: function( e, newValue, oldValue, eOpts ){
        var condition = this.getView();
        var items = condition.items.items;
        var cLength = condition.items.length;
        var removeList = [];
        if(newValue!=''&&newValue!=oldValue){
            for(var i =3 ;i<=cLength;i++){
                var obj = items[i];
                if(obj){
                    if(!obj.isXType('container')){
                        removeList.push(obj);
                    }
                }
            }
            Ext.Array.each(removeList, function(r, index, countriesItSelf) {
                condition.remove(r);
            });
            if(newValue=='term'){
                condition.insert(3,{
                    name: 'value'
                });
            }
            if(newValue=='range'){
                condition.insert(3,[{
                        xtype: 'combobox',
                        name : 'startOperator',
                        displayField: 'name',
                        maxWidth : 100,
                        valueField: 'value',
                        store: Ext.create("Ext.data.Store", {
                            fields: ["name", "value"],
                            data: [
                                { name: "from", value: "from" },
                                { name: "gt", value: "gt" },
                                { name: "gte", value: "gte" }
                            ]
                        })
                    },
                    {
                        name: 'startValue'
                    },{
                        xtype: 'combobox',
                        name : 'endOperator',
                        displayField: 'name',
                        maxWidth : 70,
                        valueField: 'value',
                        store: Ext.create("Ext.data.Store", {
                            fields: ["name", "value"],
                            data: [
                                { name: "to", value: "to" },
                                { name: "lt", value: "lt" },
                                { name: "lte", value: "lte" }
                            ]
                        })
                    },
                    {
                        name: 'endValue'
                    }]);
            }
            if(newValue=='fuzzy'){
                condition.insert(3,[{
                    name: 'value'
                },{
                    xtype: 'combobox',
                        name : 'setting',
                        displayField: 'name',
                        valueField: 'value',
                        maxWidth : 150,
                        store: Ext.create("Ext.data.Store", {
                        fields: ["name", "value"],
                        data: [
                            { name: "max_expansions", value: "max_expansions" },
                            { name: "min_similarity", value: "min_similarity" }
                        ]
                    })
                },
                {
                    name: 'sValue'
                }]);
            }
        }

    },
    drawCondition : function(aPanel,type){

        var items = aPanel.child('fieldset');
        var index = items.items.length;
        items.add(Ext.create('starter.view.main.document.SearchCondition'));
        this.loadConditionField(aPanel,type,index);
    },
    changeQuery : function(e, newValue, oldValue, eOpts){

    },
    onAddButton : function(e){
        var aPanel = this.getView().up('searchForm');
        var type = aPanel.child('combo').getValue();
        if(type&&type!=''){
            this.drawCondition(aPanel,type);
        }
    },
    onDeleteButton : function(e){
        if(this.getView().items.length==1){
            return ;
        }
        e.up('searchcondition').removeAll();
    },
    search : function(){
        var form = this.getView().getForm();
        var conditions = this.getView().query('searchcondition');
        if (form.isValid()) {
            var qmust = [];
            var qmust_not = [];
            var qshould = [];
            var type = form.findField('type').getValue();
            Ext.Array.each(conditions, function(condition, index, countriesItSelf) {
                var queryItem ={};
                var query = condition.child('combobox[name="query"]').getValue();
                var property = condition.child('combobox[name="property"]').getValue();
                var operator = condition.child('combobox[name="operator"]').getValue();
                if(operator=='range'){
                    queryItem.range =   { };
                    var startValue = condition.child('textfield[name="startValue"]').getValue();
                    var endValue = condition.child('textfield[name="endValue"]').getValue();
                    if(startValue&&startValue!=''){
                        queryItem.range[property] =  {'from':startValue};
                    }
                    if(endValue&&endValue!=''){
                        if(queryItem.range[property]){
                            queryItem.range[property].to =  endValue;
                        }else{
                            queryItem.range[property] =  {'to':endValue};
                        }
                    }
                }
                if(operator=='term'){
                    var pValue= condition.child('textfield[name="value"]').getValue();
                    queryItem.term ={};
                    queryItem.term[property] =  pValue;
                }
                if(operator=='fuzzy'){
                    queryItem.fuzzy =   { };
                    var pValue= condition.child('textfield[name="value"]').getValue();
                    if(pValue&&pValue!=''){
                        queryItem.fuzzy[property] =  {'value': pValue} ;
                        var fkey = condition.child('combobox[name="setting"]').getValue();
                        var fvalue = condition.child('textfield[name="sValue"]').getValue();
                        if(fvalue&&fvalue!=''&&fvalue&&fvalue!=''){
                            queryItem.fuzzy[property][fkey] = fvalue;
                        }
                    }

                 }
                if(query=='must'){
                    if(queryItem){
                        qmust.push(queryItem);
                    }
                }
                if(query=='must_not'){
                    if(queryItem) {
                        qmust_not.push(queryItem);
                    }
                }
                if(query=='should'){
                    if(queryItem) {
                        qshould.push(queryItem);
                    }
                }
    });
            var query = {"bool" : {}};
            if(qmust.length>0){
                query.bool.must =  qmust;
            }
            if(qmust_not.length>0){
                query.bool.must_not = qmust_not;
            }
            if(qshould.length>0){
                query.bool.should =  qshould;
            }
            if(qshould&&qshould.length>0){
                query.bool.minimum_should_match = form.findField( 'minimum_should_match').getValue();
            }
            Ext.JSON.encode(query);
            var store = Ext.create('Ext.data.Store', {
                model: 'starter.model.Document',
                sorters  : [{
                    property : "createdOn",
                    direction: "DESC"
                }],
                remoteSort : true,
                proxy: {
                    type: 'ajax',
                    url: '/svc/'+type,
                    extraParams : {'query':Ext.JSON.encode(query)},
                    reader: {
                        type: 'json',
                        rootProperty: 'documents'
                    }
                },
             autoLoad: true
            });
            var grid = this.getView().up('advancedsearch').down('grid');
            grid.bindStore(store);
            grid.down('pagingtoolbar').bindStore(store);
            //store.load();
        }
    }

});
