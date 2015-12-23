package com.bsu.servlet;

import com.bsu.business.ResponseAddressData;
import com.bsu.system.tool.U;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by fengchong on 2015/12/23.
 */
@WebServlet(name = "plc_statequery")
public class PLC_StateQuery extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //访问状态数据:http://192.168.199.202:8080/pgc2/plc_state_query?point=dumpIsReady
        String point = U.getRS(request,"point");                         //查看鼓的状态
        String retval = null;
        retval=(ResponseAddressData.allState.get(point)==null)?"null":ResponseAddressData.allState.get(point).toString();
        U.p(response, retval);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}
