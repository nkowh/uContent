Ext.define('dm.view.system.Initialization', {
    extend: 'Ext.panel.Panel',

    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            items: [
                {
                    xtype: 'button',
                    scale: 'large',
                    iconAlign: 'top',
                    text: '重建索引',
                    glyph: 0xf1c0,
                    handler: me.reindexing
                    //html : '<div id="processBarDiv" style="width:50%;">ddddddddddddd</div>'
                },
                {
                    xtype : 'panel',
                    id : 'processBarDiv',
                    html : '<div id="processBarDiv" style="width:50%;">xx</div>'
                }
            ]
        });




        me.callParent();
    },

    updateProgress1: function (progress) {
        alert(progress);
        //if (progress >= 1) {
        //    Ext.MessageBox.updateProgress(1, "处理完成");
        //    return;
        //}
        //
        //Ext.MessageBox.updateProgress(progress, Math.round(progress * 100) + "%");
        //Ext.defer(function () {
        //    updateProgress(progress + 0.1);
        //}, 500);
    },



    reindexing: function () {
        //alert(Ext.fly("processBarDiv").getHtml());
        //Ext.MessageBox.progress("进度", "正在处理，请稍候...", "0%");
        this.updateProgress1(0);



        //Ext.ux.Ajax
        //    .put(Ext.util.Cookies.get('service').replace('/dm','/my_index_v1'))
        //    .then(Ext.ux.Ajax.put(Ext.util.Cookies.get('service').replace('/dm','/my_index_v1')), errorHandler)
        //    .then(Ext.ux.Ajax.request('data3.json'), errorHandler)
        //    .then(function (data3) {
        //    alert("Ah! That's much better ;)");
        //}, errorHandler);


        ////加载进度条
        //var processBar = Ext.create("Ext.ProgressBar", {
        //    id: "reindexProcess",
        //    text: '准备中...',
        //    renderTo: 'processBarDiv'
        //});
        //
        ////模拟加载环境
        //var f = function (v) {
        //    return function () {
        //        var i = v / 12;
        //        processBar.updateProgress(i, '进度：' + v + '/12');
        //        if (v == 12) {
        //            Ext.Msg.alert("提示", "加载完毕！");
        //            processBar.reset();   //复位进度条
        //            processBar.updateText("完成。");
        //        }
        //    };
        //};
        //for (var i = 1; i < 13; i++) {
        //    setTimeout(f(i), i * 200);
        //}
        //
        //
        //var p = Ext.create('Ext.ProgressBar', {
        //    renderTo: 'processBarDiv',
        //    width: 300
        //});
        //
        //p.wait({
        //    interval: 500, //bar will move fast!
        //    duration: 50000,
        //    increment: 15,
        //    text: 'Updating...',
        //    scope: this,
        //    fn: function(){
        //        p.updateText('Done!');
        //    }
        //});


    }


});