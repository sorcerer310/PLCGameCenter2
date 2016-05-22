package com.bsu.servlet;

import com.bsu.business.data.PLCRealTimeMonitorData;
import com.bsu.system.tool.U;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by fengchong on 16/5/22.
 */
@WebServlet(name = "plc_leftlife")
public class PLC_LeftLifeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //控制7个星灯的点
        String[] point = new String[]{"O101.0","O101.1","O101.2","O101.3","O101.4","O101.5","O101.6"};

        int starSum = 0;
        for(int i=0;i<point.length;i++){
            String v = (PLCRealTimeMonitorData.getInstance().getVal(point[i])==null)?"null": PLCRealTimeMonitorData.getInstance().getVal(point[i]).toString();
            if(v.equals("true"))
                starSum++;
        }
        U.p(response,String.valueOf(starSum));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}
