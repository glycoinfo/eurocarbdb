/**
*
*/
function draw_taxonomy_tree( element, json ) 
{
    var st_left = 999, st_right = -999, st_top = 999, st_bottom = -999;
    // var json_${glycan.glycanSequenceId?c} = <@ecdb.jit_json_tree graph=biological_context_graph />; 
    var st = new $jit.ST({  
        // injectInto: 'glycan_${glycan.glycanSequenceId?c}_taxa',  
        injectInto: element,
        duration: 0,  
        width: 1000, 
        height: 1000,
        background: false,
        orientation: 'top',
        // orientation: 'left',
        levelDistance: 25,
        levelsToShow: 20,  
        constrained: false,
        // Navigation: {
        //   enable:true,
        //   panning:true,
        //   zooming: true
        // },
        Node: {  
            height: 25,  
            width: 90,  
            // color:'#eee',  
            color:'#fff',  
            lineWidth: 0,  
            align:"center",  
            type: 'rectangle',
            overridable: true  
        },  
        Edge: {  
            type: 'bezier',  
            lineWidth: 2,  
            color:'#446',  
            overridable: true 
        },
        // Tips: {
        //     enable: true,
        //     onShow: function(tip,node) {
        //         tip.innerHTML = 'View detail page for ' + node.name;
        //     }
        // },
        onCreateLabel: function(label, node) {  
            label.id = node.id;              
            label.innerHTML = '<a href="' + node.data.detail + '">' + node.name + '</a>';  
            // label.onclick = function(){  
                // st.onClick(node.id);  
                // st.select(node.id);  
                // window.top.location = node.data.detail;
                // window.open( node.data.detail );
            // };  
            //set label styles  
            var style = label.style;
            style.width = 90 + 'px';
            style.height = 25 + 'px';            
            // style.cursor = 'pointer';
            style.color = '#333';
            style.fontSize = '0.8em';
            style.textAlign= 'center';
            style.textDecoration = 'underline';  
            // style.paddingTop = '3px';  
        },
        onAfterCompute: function(){  
            var graph_nodes = st.graph.nodes;
            var x, y;
            for ( var i in graph_nodes )
            {
                x = graph_nodes[i].pos.x;
                y = graph_nodes[i].pos.y;
                if ( x < st_left )
                    st_left = x;
                if ( x > st_right )
                    st_right = x;
                if ( y < st_top )
                    st_top = y;
                if ( y > st_bottom )
                    st_bottom = y;
            }
            // console.log( 'onAfterCompute: left=' + st_left + ',right=' + st_right + ',top=' + st_top + ',bottom=' + st_bottom );
        },
        // onBeforePlotLine: function(adj){ 
        //     if ( adj.nodeFrom.data.type != adj.nodeTo.data.type ) {  
        //         adj.setData('color', '#f00');  
        //         adj.type = 'hyperline';
        //     }  
        // }, 
        // onBeforePlotNode: function(node){  
        //     if ( node.data.type == 'tissue_taxonomy' ) {  
        //         // node.data.$type = 'circle';  
        //         node.data.$color = '#fee';  
        //     }  
        //     // console.log("node " + node.id + ": x=" + node.pos.x + ", y=" + node.pos.y );
        // }
    });  
    st.loadJSON( json );
    st.compute();  
    st.onClick( st.root, {
        onComplete: function() {
            st.canvas.resize( st_right - st_left + 100, st_bottom - st_top + 100 );
            st.canvas.translate( -((st_left + st_right) / 2), -((st_top + st_bottom) / 2) );
        }
    });  
    
    return st;
}

