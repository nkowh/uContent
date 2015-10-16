Ext.define('admin.view.main.ReIndexController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.reIndex',
    reIndex : function(e){
        var me = this;
        var form = this.getView().getForm();
        if (!form.isValid())return;
        Ext.Ajax.request({
            url: '/svc/_reindex',
            method : 'POST',
            params : form.getValues(),
                callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                if(response.responseText&&response.responseText!=''){
                    var result = Ext.decode(response.responseText);
                    if(result.operationId&&result.operationId!=''){
                        me.getViewModel().set({'operationId':result.operationId});
                        me.getViewModel().set({'srcIndex':result.srcIndex});
                        me.getViewModel().set({'targetIndex':result.targetIndex});
                        me.refresh();
                    }
                    me.getView().up('window').close();
                }
            }
        });
    },
    onAxisLabelRender :  function (axis, label, layoutContext) {
        return layoutContext.renderer(label) + '%';
    },
    onSeriesTooltipRender : function(tooltip, record, item){
        tooltip.setHtml(record.get('finished') + ': ' + record.get('total') );
    },
    openWin : function(){
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '重建索引条件表单',
            items: [{
                xtype: 'indexCondition'
            }]
        }).show();
        this.getView().getReferences().reIndexButton.setDisabled(true);
        return;
    },
    onTimeChartRendered : function(){
        this.timeChartTask = Ext.TaskManager.start({
            run: this.loadData,
            interval: 5000,
            scope: this
        });
    },
    onTimeChartDestroy : function(){
        if (this.timeChartTask) {
            Ext.TaskManager.stop(this.timeChartTask);
        }
    },
    loadData : function(callback){
        var me = this;
        Ext.Ajax.request({
            url: '/svc/_reindex/_status',
            callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                if(response.responseText&&response.responseText!=''){
                    var result = Ext.decode(response.responseText);
                    if(result.operationId&&result.operationId!=''){
                        me.getViewModel().set({'operationId':result.operationId});
                        me.getViewModel().set({'srcIndex':result.srcIndex});
                        me.getViewModel().set({'targetIndex':result.targetIndex});
                        me.refresh();
                    }
                    if(result.isFinished){
                        me.getView().getReferences().reIndexButton.setDisabled(false);
                    }else{
                        me.getView().getReferences().reIndexButton.setDisabled(true);
                    }
                }
            }
        });
        return true;
    },
    refresh : function(){
        var me = this;
        var operationId =  me.getViewModel().get('operationId');
        var store = me.getViewModel().getStore('reIndexs');
        if(operationId&&operationId!=''){
            store.load({
                params : {'operationId':operationId}
            });
        }else{
            me.onTimeChartDestroy();
        }
    }

});
