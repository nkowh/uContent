Ext.define('admin.view.main.ViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.view',
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
                        Ext.Array.each(me.getView().query('tagfield[name="permissionObj"]'), function(obj, index, countriesItSelf) {
                            obj.bindStore(
                                Ext.create('Ext.data.Store', {
                                    fields: ['id', 'name', 'isUser', 'isGroup'],
                                    data: data
                                }));
                        });
                        if(acl){
                            me.getView().query('tagfield[name="permissionObj"]')[0].setValue(acl);
                        }
                    }
                });
            }
        });
    },
    openCreateWin: function () {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            width : 800,
            title: '新建视图',
            items: [{
                xtype: 'createView',
                store: this.getView().getStore()
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            width : 800,
            title: '修改视图',
            items: [{
                xtype: 'modifyView',
                record: record
            }]
        }).show();
        return;
    },
    loadModifyData: function (e, eOpts) {
        var me = this;
        var record = this.getView().record;
        var queryContext = Ext.JSON.decode(record.get('queryContext'));
        var query = queryContext.query;
        var users = [];
        var groups = [];
        if(record.get('users')&&record.get('users')!=''){
            var users = record.get('users').split(',');
        }

        if(record.get('groups')&&record.get('groups')!=''){
            var groups = record.get('groups').split(',');
        }

        var pData = Ext.Array.merge(users, groups);
        this.loadAclOperationObj(pData);
        this.getView().getForm().loadRecord(record);
        this.getView().getForm().findField('type').setValue(queryContext.type);
        var isMulti = false;
        if (queryContext.type&&queryContext.type.length>1) {
            isMulti = true;
        }
        if(query.bool){
            Ext.Object.each(query.bool, function(key, value, myself) {
                if(value){
                    var conditionValue = {'query':key};

                    if(Ext.isArray(value)){
                        Ext.Array.each(value, function(c, index, countriesItSelf) {
                           var k =  Ext.Object.getKeys(c)[0];
                            conditionValue.operator = k;
                            conditionValue.operatorValue = c[k];
                            me.organizationConditionData(conditionValue);
                            me.drawCondition(me.getView(), queryContext.type,isMulti,conditionValue);
                        });
                    }
                }
            });
            me.getView().down('textfield[name=minimum_should_match]').setValue(query.bool.minimum_should_match);
        }
    },
    organizationConditionData : function(conditionValue){
        var operator = conditionValue.operator;
        var operatorValue = conditionValue.operatorValue;
        var key = '';
        var value = '';
        if(operatorValue){
            key =  Ext.Object.getKeys(operatorValue)[0];
            value = operatorValue[key];
        }
        if(operator=='range'){
            conditionValue.property = key;
            var rkeys =  Ext.Object.getKeys(value);
            var sOperator = rkeys[0];

            var hasFrom = Ext.Array.contains(['from','gt','gte'],rOperator);
            if(hasFrom){
                conditionValue.startOperator = rkeys[0];
                conditionValue.startValue = value[rkeys[0]];
            }else{
                conditionValue.endOperator = rkeys[0];
                conditionValue.endValue = value[rkeys[0]];
            }
            if(rkeys.length>1){
                var eOperator = rkeys[1];
                var hasEnd = Ext.Array.contains(['to','lt','lte'],eOperator);
                if(hasEnd){
                    conditionValue.endOperator = rkeys[1];
                    conditionValue.endValue =value[rkeys[1]];
                }
            }

        }
        if(operator=='fuzzy'){
            conditionValue.property = key;
            conditionValue.value = value.value;
            if(value.max_expansions){
                conditionValue.setting = 'max_expansions';
                conditionValue.sValue =value.max_expansions;
            }
            if(value.min_similarity){
                conditionValue.setting = 'min_similarity';
                conditionValue.sValue =value.min_similarity;
            }

        }
        if(operator=='term'||operator=='wildcard'){
            conditionValue.property = key;
            conditionValue.value = value;
        }
    },
    loadConditionField: function (aPanel, type,isMulti, index,conditionValue) {
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
            if(aPanel.query('combobox[name="property"]')[index]){
                aPanel.query('combobox[name="property"]')[index].bindStore(
                    Ext.create('Ext.data.Store', {
                        model: Ext.create('admin.model.Property'),
                        data: data
                    })
                );
                if(conditionValue&&conditionValue.property!=''){
                    aPanel.query('combobox[name="property"]')[index].setValue(conditionValue.property);
                }
            }
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
                        if(aPanel.query('combobox[name="property"]')[index]){
                            aPanel.query('combobox[name="property"]')[index].bindStore(
                                Ext.create('Ext.data.Store', {
                                    model: Ext.create('admin.model.Property'),
                                    data: data
                                })
                            );
                            if(conditionValue&&conditionValue.property!=''){
                                aPanel.query('combobox[name="property"]')[index].setValue(conditionValue.property);
                            }
                        }
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
    drawCondition: function (aPanel, type,isMulti,conditionValue) {

        var items = aPanel.child('fieldset');
        var index = items.items.length;
        var sc =  items.add(Ext.create('admin.view.main.view.SearchCondition'));
        if(conditionValue&&conditionValue.query){
            sc.down('combobox[name=query]').setValue(conditionValue.query);
            sc.down('combobox[name=operator]').setValue(conditionValue.operator);
            this.loadQueryValues(conditionValue,sc);
        }
        this.loadConditionField(aPanel, type,isMulti, index,conditionValue);
    },
    loadQueryValues : function(conditionValue,obj){
        var operator = conditionValue.operator;
        if(operator=='range'){
            obj.down('combobox[name=startOperator]').setValue(conditionValue.startOperator);
            obj.down('textfield[name=startValue]').setValue(conditionValue.startValue);
            obj.down('textfield[name=endValue]').setValue(conditionValue.endValue);
            obj.down('combobox[name=endOperator]').setValue(conditionValue.endOperator);

        }
        if(operator=='fuzzy'){
            obj.down('combobox[name=setting]').setValue(conditionValue.setting);
            obj.down('textfield[name=value]').setValue(conditionValue.value);
            obj.down('textfield[name=sValue]').setValue(conditionValue.sValue);

        }
        if(operator=='term'||operator=='wildcard'){
            obj.down('textfield[name=value]').setValue(conditionValue.value);
        }
    },
    onAddButton: function (e) {
        var aPanel = this.getView().up('form');
        var type = aPanel.child('tagfield[name=type]').getValue();
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
    save : function(bt,e) {
        var me = this;
        var form = this.getView().getForm();
        var store = this.getView().store;
        var conditions = this.getView().query('searchcondition');
        if (form.isValid()) {
            var qmust = [];
            var qmust_not = [];
            var qshould = [];
            var type = form.findField('type').getValue();
            var viewObj = form.getValues();
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
                        if (!startOperator) {
                            startOperator = 'from';
                        }
                        queryItem.range[property][startOperator] = startValue;
                    }
                    if (endValue && endValue != '') {
                        if (!endOperator) {
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
            var queryContext = {'query':query,'type':type}
            viewObj.queryContext = Ext.JSON.encode(queryContext);
            var permissionObj = form.findField('permissionObj').getValueRecords();
            viewObj.users = [];
            viewObj.groups = [];
            Ext.Array.each(permissionObj, function (permiss, index, countriesItSelf) {
                if (permiss.get('isUser')) {
                    viewObj.users.push(permiss.get('id'));
                }
                if (permiss.get('isGroup')) {
                    viewObj.groups.push(permiss.get('id'));
                }

            });
            var url = '';
            var msg = "";
            var method = "";
            if( this.getView().down('hiddenfield[name=_id]')&&this.getView().down('hiddenfield[name=_id]').getValue()!=''){
                url = '/svc/views/'+this.getView().down('hiddenfield[name=_id]').getValue();
                msg ='Update';
                method = 'PATCH';
            }else{
                url = '/svc/views';
                msg ='Create';
                method = 'POST';
            }
            Ext.Ajax.request({
                method: method,
                headers: {'Content-Type': 'application/json;charset=utf-8'},
                url: url,
                params: Ext.JSON.encode(viewObj),
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    Ext.toast({
                        html: msg+' successful',
                        title: 'message',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                    me.getView().up('window').close();
                }
            });
        }
    }, refreshView : function(){
        var me = this;
        var store = this.getViewModel().getStore('views');
        store.load();
        return;
    },
    deleteView: function (e) {
        var me = this;
        var record = this.getView().getSelectionModel().getSelection();
        if (!record || record.length == 0) {
            Ext.Msg.alert('message', 'Please select one item at least.');
            return;
        }

        Ext.Msg.confirm("Title", "Are you sure to delete this View ?", function (r) {
            if(r=='yes'){
                var store = record[0].store;
                store.remove(record[0]);
            }
        });
    }

});
