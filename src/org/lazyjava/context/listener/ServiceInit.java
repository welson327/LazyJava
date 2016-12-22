package org.lazyjava.context.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.lazyjava.jobs.CountJob;

public class ServiceInit implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("########################################");
		
		CountJob.destroy();

		System.out.println("contextDestroyed");
		System.out.println("########################################");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("########################################");
		
		
		System.out.println("contextInitialized");
		System.out.println("########################################");
	}
}
