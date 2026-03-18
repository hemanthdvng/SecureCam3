const WebSocket=require('ws');const http=require('http');const PORT=process.env.PORT||8080;
const server=http.createServer((req,res)=>{res.writeHead(200);res.end('SecureCam OK\n')});
const wss=new WebSocket.Server({server});const rooms={};
function room(c){if(!rooms[c])rooms[c]={camera:null,viewer:null,receiver:null};return rooms[c]}
function send(ws,o){if(ws&&ws.readyState===WebSocket.OPEN)ws.send(typeof o==='string'?o:JSON.stringify(o))}
wss.on('connection',(ws)=>{
  ws.on('message',(data)=>{
    if(Buffer.isBuffer(data)&&ws._role==='sender'){const r=rooms[ws._room];if(r&&r.receiver)send(r.receiver,data);return}
    let msg;try{msg=JSON.parse(data.toString())}catch{return}
    const{type,room:rc,role}=msg;
    if(type==='join'){ws._room=rc;ws._role=role;const r=room(rc);if(role==='camera'){r.camera=ws;if(r.viewer){send(r.camera,{type:'peer_joined'});send(r.viewer,{type:'peer_joined'})}}else{r.viewer=ws;if(r.camera){send(r.camera,{type:'peer_joined'});send(r.viewer,{type:'peer_joined'})}}}
    else if(type==='join_stream'){ws._room=rc;ws._role=role;const r=room(rc);if(role==='sender'){r.camera=ws;if(r.receiver){send(r.camera,{type:'peer_joined'});send(r.receiver,{type:'peer_joined'})}}else{r.receiver=ws;r.viewer=ws;if(r.camera){send(r.camera,{type:'peer_joined'});send(r.receiver,{type:'peer_joined'})}}}
    else if(type==='offer'){const r=rooms[rc];if(r&&r.viewer)send(r.viewer,msg)}
    else if(type==='answer'){const r=rooms[rc];if(r&&r.camera)send(r.camera,msg)}
    else if(type==='ice_candidate'){const r=rooms[rc];if(ws._role==='camera'&&r&&r.viewer)send(r.viewer,msg);else if(r&&r.camera)send(r.camera,msg)}
    else if(type==='motion_event'||type==='ai_event'){const r=rooms[rc];if(r&&r.viewer)send(r.viewer,msg);if(r&&r.receiver)send(r.receiver,msg)}
  });
  ws.on('close',()=>{const c=ws._room;if(!c)return;const r=rooms[c];if(!r)return;const cam=ws._role==='camera'||ws._role==='sender';const other=cam?(r.viewer||r.receiver):r.camera;if(cam)r.camera=null;else{r.viewer=null;r.receiver=null};if(other)send(other,{type:'peer_left'})});
});
server.listen(PORT,()=>console.log('SecureCam server on port '+PORT));