package com.nusantara.automate.action.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.Callback;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.WebCallback;
import com.nusantara.automate.WebElementWrapper;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.exception.ModalFailedException;
import com.nusantara.automate.util.Sleep;


/**
 * The action for handling the modal page response 
 * 
 * @author ari.patriana
 *
 */
public class ModalSuccessAction extends WebElementWrapper implements Actionable {
	Logger log = LoggerFactory.getLogger(ModalSuccessAction.class);
	
	@Value(value = "timeout.modal.callback")
	private String timeoutModalCallback;
	
	private Callback callback;
	private String successId;
	private String[] failedId;
	
	public ModalSuccessAction(String successId, String failedId, WebCallback callback) {
		callback.setSuccessId(successId);
		callback.setFailedId(new String[] {failedId});
		
		this.callback = callback;
		this.successId = successId;
		this.failedId = new String[] {failedId};
		
		ContextLoader.setObject(callback);
	}
	
	public ModalSuccessAction(String successId, String[] failedId, WebCallback callback) {
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
	public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		log.info("Waiting modal success open");
		int totalThread = failedId.length+2; 
		try {
			ExecutorService executor = Executors.newFixedThreadPool(totalThread);
			CountDownLatch countDownOk = new CountDownLatch(totalThread);
			CountDownLatch countDownLatch = new CountDownLatch(totalThread);
			ConcurrentHashMap<Boolean, String> modalSuccess = new ConcurrentHashMap<Boolean, String>();
			
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						WebDriverWait wait = new WebDriverWait(getDriver(),Integer.valueOf(timeoutModalCallback));
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(successId)));
						modalSuccess.put(Boolean.TRUE, successId);
						countDownOk.countDown();	
						log.info("Modal success open");
					} catch (TimeoutException e) {
						// do nothing
					} finally {
						countDownLatch.countDown();			
					}
				}
			});
			
			for (String failedModalId : failedId) {
				executor.execute(new Runnable() {
					
					@Override
					public void run() {
						try {
							WebDriverWait wait = new WebDriverWait(getDriver(),Integer.valueOf(timeoutModalCallback));
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(failedModalId)));
							modalSuccess.put(Boolean.FALSE, failedModalId);
							countDownOk.countDown();	
							log.info("Modal failed open");
						} catch (TimeoutException e) {
							// do nothing
						} finally {
							countDownLatch.countDown();					
						}
					}
				});
			}
			
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						WebDriverWait wait = new WebDriverWait(getDriver(),Integer.valueOf(timeoutModalCallback));
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div/div[contains(@id,'tooltip') and contains(@class,'tooltip')]")));
						
						// sleep 3 seconds, to make sure for modal failed to open
						Sleep.wait(3000);
						if (modalSuccess.size() ==0 ) {
							try {
								modalSuccess.put(Boolean.FALSE, findElementByXpath(WebElementWrapper.DEFAULT_MODAL, 1).getAttribute("id"));
							} catch (Exception e) {
								modalSuccess.put(Boolean.FALSE, WebElementWrapper.DEFAULT_MAIN);
							}

							countDownOk.countDown();	
							log.info("Tooltip error open");
						}
						
						
					} catch (TimeoutException e) {
						// do nothing
					} finally {
						countDownLatch.countDown();			
					}
					
				}
			});
			
			for (;;) {
				if (countDownOk.getCount() == (totalThread-1) || countDownLatch.getCount() == 0) {
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
					
					if (countDownLatch.getCount() == 0)
						throw new FailedTransactionException("All window modal not open");
					break;
				}

				Sleep.wait(100);
			}
			
			// wait until modal fully open
			Sleep.wait(1000);
			
			if (modalSuccess.containsKey(Boolean.TRUE)) {
				callback.callback(findElementById(modalSuccess.get(Boolean.TRUE)), webExchange);
			} else {
				callback.callback(findElementById(modalSuccess.get(Boolean.FALSE)), webExchange);				
			}
	
		} catch (FailedTransactionException e) {
			throw e;
		} catch (ModalFailedException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
		
	}

}
