package com.davehock;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SmartMeterServletContextListener implements ServletContextListener {

    private SmartMeterManager smartMeterThread = null;

    public void contextInitialized(ServletContextEvent sce) {
        if ((smartMeterThread == null) || (!smartMeterThread.isAlive())) {
           smartMeterThread = new SmartMeterManager();
           smartMeterThread.init();
           sce.getServletContext().setAttribute(SmartMeterManager.getName(), smartMeterThread);
        }
   }

    public void contextDestroyed(ServletContextEvent sce){
        try {
           smartMeterThread.close();
           smartMeterThread.interrupt();

        } catch (Exception ex) {
        }
    }
}