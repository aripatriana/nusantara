package com.nusantara.automate.action.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.Callback;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.WebCallback;
import com.nusantara.automate.WebElementWrapper;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.util.Sleep;


/**
 * The action for handling the modal page response 
 * 
 * @author ari.patriana
 *
 */
public class ModalSuccessAction extends WebElementWrapper implements Actionable {
	Logger log = LoggerFactory.getLogger(ModalSuccessAction.class);
	
	private Callback callback;
	private String successId;
	private String failedId;
	
	public ModalSuccessAction(String successId, String failedId, WebCallback callback) {
		callback.setSuccessId(successId);
		callback.setFailedId(failedId);
		
		this.callback = callback;
		this.successId = successId;
		this.failedId = failedId;
		
		ContextLoader.setObject(callback);
	}
	
	public ModalSuccessAction(WebCallback callback) {
		this("saveSuccess", "txFailed", callback);
	}
	
	public ModalSuccessAction(String successId, WebCallback callback) {
		this(successId, "txFailed", callback);
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		log.info("Waiting modal success opened");
		
		try {
			ExecutorService executor = Executors.newFixedThreadPool(2);
			
			CountDownLatch latch = new CountDownLatch(2);
			AtomicBoolean modalSuccess = new AtomicBoolean(Boolean.FALSE);
			
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						WebDriverWait wait = new WebDriverWait(getDriver(),180);
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(successId)));
						modalSuccess.set(Boolean.TRUE);
						latch.countDown();
						log.info("Modal success opened");
					} catch (TimeoutException e) {
						// do nothing
					}
					
				}
			});
			
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						WebDriverWait wait = new WebDriverWait(getDriver(),180);
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(failedId)));
						modalSuccess.set(Boolean.FALSE);
						latch.countDown();
						log.info("Modal failed opened");
					} catch (TimeoutException e) {
						// do nothing
					}
				}
			});
			
			for (;;) {
				long countdown = latch.getCount();
				if (countdown == 1) {
					// shutdown thread
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							executor.shutdown();
							// Wait until all threads are finish
							// safe mode 
							try {
								executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
							} catch (InterruptedException e1) {
								log.error("Failed to wait termination while all thread not yet finished");
							}
							
						}
					}).start();
					
					break;
				}
			}
			
			Sleep.wait(1000);
			
			if (modalSuccess.get()) {
				callback.callback(findElementById(successId), webExchange);
			} else {
				callback.callback(findElementById(failedId), webExchange);				
			}
				
		} catch (FailedTransactionException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch latch = new CountDownLatch(2);
		
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("COUNT DOWN 1");
				latch.countDown();
				
			}
		});
		
		
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("COUNT DOWN 2");
				latch.countDown();
				
			}
		});
		
		System.out.println(Thread.currentThread());
//		
//		System.out.println("FOR LOOP");
//		for (;;) {
//			long countdown = latch.getCount();
//			if (countdown == 1) {
//
//				System.out.println("LATCH 1");
//				// shutdown thread
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//
//						System.out.println("SHUTDOWN");
//						executor.shutdown();
//						// Wait until all threads are finish
//						// safe mode 
//						try {
//							executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
//						} catch (InterruptedException e1) {
//							e1.printStackTrace();
//						}
//
//						System.out.println("SHUTDOWN DONE");
//						
//					}
//				}).start();
//				
//				break;
//			}
//		}
		

		System.out.println("DONEE");
	}

}
