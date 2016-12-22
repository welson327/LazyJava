package org.lazyjava.utility;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//=========================================================================
// Purpose:		Provide easy way to create queue-thread-handling system
// Parameters:
// Return:
// Remark:		http://docs.oracle.com/javase/tutorial/java/generics/why.html
// Author:		welson
// Sample code:	
/*
				## Method 1: new this class and override
					QueueService<File> qs = new QueueService<File>() {
						protected void popCallback(Integer job) {
							// do something
						}
					};
					qs.start();
					
					
				## Method 2: Define sub-class and override
					public class FileQueueService extends QueueService<File> {
						protected void popCallback(File job) {
							// do something
						}
					}
					QueueService<File> qs = new FileQueueService();
					qs.start();
					
					
				## Safe destroy queue-service:
				 	s.interrupt(); // this will call interruptedCallback() if defined.
				 	s.destroy();					
*/
//=========================================================================
public abstract class QueueService<T> {
	private BlockingQueue<T> jobqueue = null;
	private ThreadGroup threadGroup = null;
	private Thread[] consumers = null;
	
	private String queueName = "QueueService";
	private boolean isThreadRun = true;
	private String threadGroupId = null;
	
	private boolean canPop = true;
	private boolean isIntererupt = false;
	private boolean isIntererupttedDone = false;
	
	public QueueService() {
		this(1);
	}
	
	public QueueService(int threadNumbers) {
		threadGroupId = UUID.randomUUID().toString();
		jobqueue = new LinkedBlockingQueue<T>();
		
		threadGroup = new ThreadGroup(threadGroupId);
		consumers = new Thread[threadNumbers];
		for(int i=0; i<threadNumbers; ++i) {
			consumers[i] = new Thread(threadGroup, mTask);
			//taskThread.start();
		}
	}
	
	public boolean push(T job) {
		boolean isSuccess = false;
		if(jobqueue != null) {
			canPop = false;
			isSuccess = jobqueue.add(job);
			if(isSuccess) {
				pushCallback(job);
				canPop = true;
			}
		}
		return isSuccess;
	}
	
	public T pop() throws InterruptedException {
		T task = null;
		if(jobqueue != null) {
			while(!canPop) {
				sleep(100);
			}
			task = jobqueue.take();
		}
		return task;
	}
	
	public void clear() {
		if(jobqueue != null) {
			jobqueue.clear();
		}
	}
	
	public int length() {
		return jobqueue.size();
	}
	
	public void start() {
		isIntererupt = false;
		isIntererupttedDone = false;

		for(int i=0; i<consumers.length; ++i) {
			if(consumers[i] != null) {
				consumers[i].start();
			}
		}
	}
	
	public void interrupt() {
		isIntererupt = true;
		isIntererupttedDone = false;
	}
	
	// You can use this function to see if interruptCallback() run done.
	public boolean isInterruptEnd() {
		return isIntererupttedDone;
	}
	
	public void destroy() {
    	if(threadGroup != null) {
    		DBG(String.format("threadGroup[%s] safe stop ...", threadGroupId));
	    	threadGroup.interrupt();
	    	
			while(threadGroup.activeCount() > 0) {
				try { sleep(100); } catch (Exception e) {}
			}
			
			consumers = null;
			threadGroup = null;
			DBG(String.format("threadGroup[%s] stopped ok!", threadGroupId));
    	}
	}
	
	public String getName() {
		return queueName;
	}
	public void setName(String name) {
		queueName = name;
	}
	
	protected void pushCallback(T job) {}
	protected abstract void popCallback(T job);
	protected void interruptedCallback(BlockingQueue<T> jobqueue) {
		System.out.println("super interruptedCallback");
	}
	
	//-----------------------------------------------------------------------//
	private Runnable mTask = new Runnable() {
    	public void run() {
    		T job = null;
    		isThreadRun = true;
            
    		String threadId = UUID.randomUUID().toString();
        	DBG(String.format("QueueService started! ID=%s\n", threadId));
        	
    		while (isThreadRun) {
    			try {
    				// avoid BlockingQueue.take() waiting
    				while(jobqueue.isEmpty()) {
    					sleep(300);
    					throwExceptionIfInterrupt(threadId);
    				}
    				throwExceptionIfInterrupt(threadId);
    				
    				
 				    job = jobqueue.take();
 				    popCallback(job);
    			} catch  (InterruptedException e) {
    				isThreadRun = false;
    				interruptedCallback(jobqueue);
    				isIntererupttedDone = true;
    			} catch (Exception e) {
    				e.printStackTrace();
    			} finally {
    				// add back to job and do again
    			}
    		}
    		DBG(String.format("QueueService stopped! ID=%s\n", threadId));
    	}
    };
    
    private void throwExceptionIfInterrupt(String threadId) throws InterruptedException {
    	if(isIntererupt) {
	    	DBG(String.format("QueueService is interrupted manually! ID=%s\n", threadId));
	    	throw new InterruptedException();
	    }
    }
    
    protected void sleep(long ms) {
    	try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
    }
    
    protected void DBG(String msg) {
		System.out.printf("[%s] %s\n", queueName, msg);
    }
}
