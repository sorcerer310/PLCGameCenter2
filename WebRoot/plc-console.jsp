<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2015/8/11
  Time: 15:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>PLC简易控制台页面</title>
  <%--<script type="text/javascript" src="<c:url value='/scripts/jquery.tablesorter.js'/>"></script>--%>
    <script type="text/javascript">
        var sendpath = "/pgc2/plc_send_serial";
    </script>
</head>
<body>
  <div>
    <p>直接发送plc指令</p>
    <%--<button id="bt1">测试按钮</button>--%>
      <%--<button id="bt_plctest">星盘灯亮</button>--%>
      <%--<button id="bt_plctest2">星盘灯灭</button>--%>
      <button id="bt_reset">复位</button>
      <button id="bt_lockPassageway1">通道锁1</button>
      <button id="bt_lockPassageway2">通道锁2</button><br/>
      <button id="bt_lockShip">船舱锁</button>
      <button id="bt_lockAltar">祭坛锁</button>
      <button id="bt_lockRoad">大道锁</button><br/>
      <button id="bt_lockHuarong">华容道锁</button>
      <button id="bt_lockSuccess">通关锁</button>
      <button id="bt_doorStarOpen">星阵门开</button><br/>
      <button id="bt_doorStarClose">星阵门关</button>
      <button id="bt_doorMapOpen">地图门开</button>
      <button id="bt_doorMapClose">地图门关</button><br/>
      <button id="bt_doorAltarOpen">祭坛门开</button>
      <button id="bt_doorAltarClose">祭坛门关</button>
  </div>

  <script type="text/javascript">
    $(document).ready(function(){

        $("#bt_test").mousedown(function(){
            $.post(sendpath, {type:"click",area:"w",address1:"000500",val1:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_lockPassageway1").mousedown(function(){
            $.post(sendpath, {type:"click",area:"w",address1:"000501",val1:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_lockPassageway2").mousedown(function(){
            $.post(sendpath, {type:"click",area:"w",address1:"000502",val1:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_lockShip").mousedown(function(){
            $.post(sendpath, {type:"click",area:"w",address1:"000503",val1:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_lockAltar").mousedown(function(){
            $.post(sendpath, {type:"click",area:"w",address1:"000504",val1:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_lockRoad").mousedown(function(){
            $.post(sendpath, {type:"click",area:"w",address1:"000505",val1:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_lockHuarong").mousedown(function(){
            $.post(sendpath, {type:"click",area:"w",address1:"000506",val1:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_lockSuccess").mousedown(function(){
            $.post(sendpath, {type:"click",area:"w",address1:"000507",val1:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_doorStarOpen").mousedown(function(){
            $.post(sendpath, {type:"h-bridge",area:"w",address1:"000600",address2:"000700",val1:"00",val2:"01",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_doorStarOpen").mouseup(function(){
            $.post(sendpath, {type:"nomal",area:"w",address1:"000700",val1:"00",readOrWrite:"write"}, function(data,status){});
        });
        $("#bt_doorStarClose").mousedown(function(){$.post(sendpath, {type:"h-bridge",area:"w",address1:"000600",address2:"000700",val1:"01",val2:"00",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorStarClose").mouseup(function(){$.post(sendpath, {type:"nomal",area:"w",address1:"000600",val1:"00",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorMapOpen").mousedown(function(){$.post(sendpath, {type:"h-bridge",area:"w",address1:"000601",address2:"000701",val1:"00",val2:"01",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorMapOpen").mouseup(function(){$.post(sendpath, {type:"nomal",area:"w",address1:"000701",val1:"00",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorMapClose").mousedown(function(){$.post(sendpath, {type:"h-bridge",area:"w",address1:"000601",address2:"000701",val1:"01",val2:"00",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorMapClose").mouseup(function(){$.post(sendpath, {type:"nomal",area:"w",address1:"000601",val1:"00",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorAltarOpen").mousedown(function(){$.post(sendpath, {type:"h-bridge",area:"w",address1:"000602",address2:"000702",val1:"00",val2:"01",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorAltarOpen").mouseup(function(){$.post(sendpath, {type:"nomal",area:"w",address1:"000702",val1:"00",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorAltarClose").mousedown(function(){$.post(sendpath, {type:"h-bridge",area:"w",address1:"000602",address2:"000702",val1:"01",val2:"00",readOrWrite:"write"}, function(data,status){});});
        $("#bt_doorAltarClose").mouseup(function(){$.post(sendpath, {type:"nomal",area:"w",address1:"000602",val1:"00",readOrWrite:"write"}, function(data,status){});});


        $("#bt_plctest").click(function(){
            $.post(sendpath,
                    {
                        plccmd:"PLCTEST"
                    },
                    function(data,status){
                        //alert(data);
                    });
        });

      $("#bt_plctest2").click(function(){
        $.post(sendpath,
                {
                  plccmd:"PLCTEST2"
                },
                function(data,status){
                  //alert(data);
                });
      });

//      $("#bt1").click(function(){
//        $.post("/pgc2/helloservlet",
//            {
//              name:"Donald Duck",
//              city:"Duckburg"
//            },
//            function(data,status){
//              alert(data);
//            });
//      });

      $("#bt1").mousedown(function(){
          alert("mousedown");
      });

      $("#bt1").mouseup(function(){
          alert("mouseup");
      });


    });
  </script>

</body>
</html>
