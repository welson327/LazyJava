package org.lazyjava.jobs;

import org.lazyjava.utility.QueueService;

public class CountJob {
	private static QueueService<CountJobBean> queue_service = null;
	static {
		queue_service = new CountQueueService();
		queue_service.start();
	}
	
	public static int size() {
		return queue_service.length();
	}

	public static boolean addBrowsingCountJob(String stickerId, int addValue) {
		CountJobBean b = new CountJobBean();
		b.id = stickerId;
		b.field = "browsingCnt";
		b.addValue = addValue;
		return queue_service.push(b);
	}
	
	public static void destroy() {
		try {
			if(queue_service != null) {
				System.out.println(" queue_service: .clear");
				queue_service.clear();
				
				System.out.println(" queue_service: .interrupt");
				queue_service.interrupt();
				while(!queue_service.isInterruptEnd()) {
					Thread.sleep(1000);
				}
				
				System.out.println(" queue_service: .destroy");
				queue_service.destroy();
				
				queue_service = null;
				System.out.println(" queue_service: .destroyed");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class CountJobBean {
	public String id = null;
	public String field = null;
	public int addValue = 1;
}

class CountQueueService extends QueueService<CountJobBean> {
	@Override
	protected void popCallback(CountJobBean b) {
		try {
			System.out.printf("run job for id=%s\n", b.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
