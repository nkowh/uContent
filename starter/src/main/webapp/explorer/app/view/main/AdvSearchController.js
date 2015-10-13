Ext.define('explorer.view.main.AdvSearchController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.advsearch',
    loadConditionField: function (aPanel, type,isMulti, index) {
        var me = this;
        var data = [{
            name: "name",
            type: "string"
        },{
            name: "createdBy",
            type: "string"
        },{
            name: "lastUpdatedBy",
            type: "string"
        },{
            name: "createdOn",
            type: "date"
        },{
            name: "lastUpdatedOn",
            type: "date"
        }];
        if(isMulti){
            aPanel.query('combobox[name="property"]')[index].bindStore(
                Ext.create('Ext.data.Store', {
                    model: Ext.create('explorer.model.Property'),
                    data: data
                })
            );
        }else{
            Ext.Ajax.request({
                url: '/svc/types/' + type,
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    if (response.responseText != '') {
                        var properties = Ext.decode(response.responseText);
                        data = Ext.Array.insert(properties.properties, 0, data);
                        aPanel.query('combobox[name="property"]')[index].bindStore(
                            Ext.create('Ext.data.Store', {
                                model: Ext.create('explorer.model.Property'),
                                data: data
                            })
                        );
                    }
                }
            });
        }

    },
    changeType: function (e, newValue, oldValue, eOpts) {
        var isMulti = false;
        if (newValue && newValue != oldValue) {
            if(newValue.length>1){
                isMulti = true;
            }
            var advPanel = this.getView();
            if (oldValue && oldValue != '') {
                this.initCondition();
                this.drawCondition(advPanel, newValue,isMulti);
            }
            this.loadConditionField(advPanel, newValue,isMulti, 0);
        }
    },
    initCondition: function () {
        var advPanel = this.getView();
        var fieldset = advPanel.child('fieldset');
        fieldset.removeAll();
    },
    changeOperator: function (e, newValue, oldValue, eOpts) {
        var condition = this.getView();
        var items = condition.items.items;
        var cLength = condition.items.length;
        var removeList = [];
        if (newValue != '' && newValue != oldValue) {
            for (var i = 3; i <= cLength; i++) {
                var obj = items[i];
                if (obj) {
                    if (!obj.isXType('container')) {
                        removeList.push(obj);
                    }
                }
            }
            Ext.Array.each(removeList, function (r, index, countriesItSelf) {
                condition.remove(r);
            });
            if (newValue == 'term' || newValue == 'wildcard') {
                condition.insert(3, {
                    name: 'value'
                });
            }
            if (newValue == 'range') {
                condition.insert(3, [{
                    xtype: 'combobox',
                    name: 'startOperator',
                    displayField: 'name',
                    maxWidth: 100,
                    valueField: 'value',
                    store: Ext.create("Ext.data.Store", {
                        fields: ["name", "value"],
                        data: [
                            {name: "from", value: "from"},
                            {name: "gt", value: "gt"},
                            {name: "gte", value: "gte"}
                        ]
                    })
                },
                    {
                        name: 'startValue'
                    }, {
                        xtype: 'combobox',
                        name: 'endOperator',
                        displayField: 'name',
                        maxWidth: 70,
                        valueField: 'value',
                        store: Ext.create("Ext.data.Store", {
                            fields: ["name", "value"],
                            data: [
                                {name: "to", value: "to"},
                                {name: "lt", value: "lt"},
                                {name: "lte", value: "lte"}
                            ]
                        })
                    },
                    {
                        name: 'endValue'
                    }]);
            }
            if (newValue == 'fuzzy') {
                condition.insert(3, [{
                    name: 'value'
                }, {
                    xtype: 'combobox',
                    name: 'setting',
                    displayField: 'name',
                    valueField: 'value',
                    maxWidth: 150,
                    store: Ext.create("Ext.data.Store", {
                        fields: ["name", "value"],
                        data: [
                            {name: "max_expansions", value: "max_expansions"},
                            {name: "min_similarity", value: "min_similarity"}
                        ]
                    })
                },
                    {
                        name: 'sValue'
                    }]);
            }
        }

    },
    drawCondition: function (aPanel, type,isMulti) {

        var items = aPanel.child('fieldset');
        var index = items.items.length;
        items.add(Ext.create('explorer.view.main.SearchCondition'));
        this.loadConditionField(aPanel, type,isMulti, index);
    },
    onAddButton: function (e) {
        var aPanel = this.getView().up('advancedsearch');
        var type = aPanel.child('tagfield').getValue();
        var isMulti = false;
        if (type && type != '') {
            if(type.length>1)
                isMulti = true;
            this.drawCondition(aPanel, type,isMulti);
        }
    },
    onDeleteButton: function (e) {
        var condtion = this.getView();
        var conditionContainer = this.getView().up('fieldset[title=Condition]');
        conditionContainer.remove(condtion);

    },
    search: function (bt,e) {
        var form = this.getView().getForm();
        var conditions = this.getView().query('searchcondition');
        if (form.isValid()) {
            var qmust = [];
            var qmust_not = [];
            var qshould = [];
            var type = form.findField('type').getValue();
            Ext.Array.each(conditions, function (condition, index, countriesItSelf) {
                var queryItem = {};
                var query = condition.child('combobox[name="query"]').getValue();
                var property = condition.child('combobox[name="property"]').getValue();
                var operator = condition.child('combobox[name="operator"]').getValue();
                if (operator == 'range') {
                    queryItem.range = {};
                    var startValue = condition.child('textfield[name="startValue"]').getValue();
                    var endValue = condition.child('textfield[name="endValue"]').getValue();
                    var startOperator = condition.child('textfield[name="startOperator"]').getValue();
                    var endOperator = condition.child('textfield[name="endOperator"]').getValue();
                    queryItem.range[property] = {};
                    if (startValue && startValue != '') {
                        if(!startOperator){
                            startOperator = 'from';
                        }
                        queryItem.range[property][startOperator] =startValue;
                    }
                    if (endValue && endValue != '') {
                        if(!endOperator){
                            endOperator = 'to';
                        }
                        queryItem.range[property][endOperator] = endValue;

                    }
                }
                if (operator == 'term') {
                    var pValue = condition.child('textfield[name="value"]').getValue();
                    queryItem.term = {};
                    queryItem.term[property] = encodeURIComponent(pValue);
                }
                if (operator == 'wildcard') {
                    var pValue = condition.child('textfield[name="value"]').getValue();
                    queryItem.wildcard = {};
                    queryItem.wildcard[property] = encodeURIComponent(pValue);
                }
                if (operator == 'fuzzy') {
                    queryItem.fuzzy = {};
                    var pValue = condition.child('textfield[name="value"]').getValue();
                    if (pValue && pValue != '') {
                        queryItem.fuzzy[property] = {'value': encodeURIComponent(pValue)};
                        var fkey = condition.child('combobox[name="setting"]').getValue();
                        var fvalue = condition.child('textfield[name="sValue"]').getValue();
                        if (fvalue && fvalue != '' && fvalue && fvalue != '') {
                            queryItem.fuzzy[property][fkey] = fvalue;
                        }
                    }
                }
                if (query == 'must') {
                    if (queryItem) {
                        qmust.push(queryItem);
                    }
                }
                if (query == 'must_not') {
                    if (queryItem) {
                        qmust_not.push(queryItem);
                    }
                }
                if (query == 'should') {
                    if (queryItem) {
                        qshould.push(queryItem);
                    }
                }
            });
            var query = {"bool": {}};
            if (qmust.length > 0) {
                query.bool.must = qmust;
            }
            if (qmust_not.length > 0) {
                query.bool.must_not = qmust_not;
            }
            if (qshould.length > 0) {
                query.bool.should = qshould;
            }
            if (qshould && qshould.length > 0) {
                query.bool.minimum_should_match = form.findField('minimum_should_match').getValue();
            }

            var tabPanel = this.getViewModel().getView().down('tabpanel');
            var index = tabPanel.items.length;
            tabPanel.add({
                title:this.getViewModel().get('advQueryTitle'),
                xtype:'documents',
                query : Ext.JSON.encode(query),
                qType :type ,
                index : index
            });
            this.getViewModel().setData({queryCondition: Ext.JSON.encode(query),qType :type});
            tabPanel.setActiveTab(index);
            this.getView().up('window').close();
        }
    },
    deleteDoc : function(bt,e){
        var me = this;
        var records = this.getView().getSelectionModel().getSelection();
        if (records && records.length > 0) {
            var result = [];
            Ext.Msg.confirm("Title", "Are you sure to delete this Document？", function (r) {
                Ext.Array.each(records, function(record, index, countriesItSelf) {
                    result.push({"type":record.get("_type"),"id":record.get("_id")});
                });
                Ext.Ajax.request({
                    method: 'DELETE',
                    url: '/svc',
                    headers: {'Content-Type': 'application/json;charset=utf-8'},
                    params: JSON.stringify(result),
                    callback: function (options, success, response) {
                        if (!success) {
                            return;
                        }
                        Ext.toast({
                            html: 'Delete successful',
                            title: 'message',
                            width: 200,
                            align: 't'
                        });
                        me.getView().store.load();
                    }
                });
            });

        } else {
            Ext.Msg.alert('message', 'Please select one item at least.');
            return;
        }
    }
});
