Ext.define('explorer.store.Views', {
    extend: 'Ext.data.Store',

    alias: 'store.views',

    fields: [
        'id', 'name', 'value'
    ],

    data: [
        { id: '1', name: "视图一", query: "{\"bool\":{\"must\":[{\"term\":{\"name\":\"testtest\"}}]}}",type:['newCt'] },
        { id: '2', name: "视图二", query: "{\"bool\":{\"must\":[{\"term\":{\"name\":\"mmmmm\"}}]}}",type:['testCt','newCt'] },
        { id: '3', name: "视图三", query: "{\"bool\":{\"must\":[{\"term\":{\"name\":\"mmmmm\"}}]}}",type:[] }
    ]

    //proxy: {
    //    type: 'memory',
    //    reader: {
    //        type: 'json',
    //        rootProperty: 'items'
    //    }
    //}
});
