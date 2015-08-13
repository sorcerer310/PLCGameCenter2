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
    <button id="bt1">测试按钮</button>
      <button id="bt_plctest">星盘灯亮</button>
      <button id="bt_plctest2">星盘灯灭</button>
  </div>

  <script type="text/javascript">
    $(document).ready(function(){
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

      $("#bt1").click(function(){
        $.post("/pgc2/helloservlet",
            {
              name:"Donald Duck",
              city:"Duckburg"
            },
            function(data,status){
              alert(data);
            });
      });



    });
  </script>

</body>
</html>
