package com.nusantara.automate.workflow;


import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.AbstractBaseDriver;
import com.nusantara.automate.Actionable;
import com.nusantara.automate.ConfigLoader;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.DBConnection;
import com.nusantara.automate.DriverManager;
import com.nusantara.automate.FormActionable;
import com.nusantara.automate.Menu;
import com.nusantara.automate.MenuAwareness;
import com.nusantara.automate.MultipleFormActionable;
import com.nusantara.automate.Retention;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.action.ManagedFormAction;
import com.nusantara.automate.action.ManagedMultipleFormAction;
import com.nusantara.automate.action.common.ModalSuccessAction;
import com.nusantara.automate.action.common.OpenFormAction;
import com.nusantara.automate.action.common.OpenMenuAction;
import com.nusantara.automate.exception.FailedTransactionException;


/**
 * Main workflow
 * 
 * @author ari.patriana
 *
 */
public class Workflow {

	Logger log = LoggerFactory.getLogger(Workflow.class);
	protected WebExchange webExchange;
	protected LinkedList<Actionable> actionableForLoop;
	protected boolean activeLoop = false;
	protected Menu activeMenu;
	protected final int MAX_RETRY_LOAD_PAGE = 3;
	protected boolean scopedAction = false;
	int scopedActionIndex = 0;
	
	public Workflow(WebExchange webExchange) {
		this.webExchange = webExchange;
	}
	
	protected WebExchange getWebExchange() {
		return webExchange;
	}
	
	public Menu getActiveMenu() {
		return activeMenu;
	}
	
	public void scopedAction() {
		this.scopedAction = true;
		this.scopedActionIndex = 0;
	}
	
	public void resetScopedAction() {
		this.scopedAction = false;
	}
	
	public static Workflow configure() {
		WebExchange webExchange = new WebExchange();
		for (Entry<String, Object> config : ConfigLoader.getConfigMap().entrySet()) {
			webExchange.put(config.getKey(), config.getValue());
		}
		ContextLoader.setWebExchange(webExchange);
		return new Workflow(webExchange);
	}
	
	public Workflow openPage(String url) {
		log.info("Open Page " + url);
		
		DriverManager.getDefaultDriver().get(url);
		return this;
	}
	
	private void setMenuAwareness(Menu menu, Actionable actionable) {
		if (actionable instanceof MenuAwareness) {
			this.activeMenu = menu;
			((MenuAwareness) actionable).setMenu(menu);
		}
	}
	
	public Workflow openMenu(Menu menu) {
		OpenMenuAction menuAction = new OpenMenuAction(null, menu.getMenu());
		setMenuAwareness(menu, menuAction);
		
		OpenMenuAction subMenuAction = new OpenMenuAction(menuAction, menu.getSubMenu());
		setMenuAwareness(menu, subMenuAction);
		
		OpenFormAction formAction = new OpenFormAction(subMenuAction, menu.getMenuId(), menu.getForm());
		setMenuAwareness(menu, formAction);
	
		if (!activeLoop) {
			menuAction.submit(webExchange);
			subMenuAction.submit(webExchange);
			formAction.submit(webExchange);
			activeMenu = menu;
		} else {
			actionableForLoop.add(menuAction);
			actionableForLoop.add(subMenuAction);
			actionableForLoop.add(formAction);			
		}

		return this;
	}
	
	public Workflow action(Actionable actionable) {
		if (!activeLoop) {
			try {
				if (ContextLoader.isPersistentSerializable(actionable)) {
					if (ContextLoader.isLocalVariable(actionable)) {
						ContextLoader.setObjectLocal(actionable);
						executeSafeActionable(actionable);
					} else {
						ContextLoader.setObject(actionable);
						executeSafeActionable(actionable);	
					}
				} else {
					ContextLoader.setObject(actionable);
					executeSafeActionable(actionable);
				}
			} catch (FailedTransactionException e) {
				log.error("ERROR ", e);
			}
		} else {
			if (scopedAction) {
				if (scopedActionIndex == 0) {
					ManagedFormAction scoped = null;
					if (actionable instanceof FormActionable) {
						scoped = new ManagedFormAction(actionable.getClass());
					} else if (actionable instanceof MultipleFormActionable) {
						scoped = new ManagedMultipleFormAction(actionable.getClass());
					}
					
					if (scoped != null) {
						scoped.addActionable(actionable);
						actionableForLoop.add(scoped);						
					} else {
						log.warn("Managed Action is missing for " + actionable);
					}
				} else {
					Actionable act = actionableForLoop.getLast();
					if (act != null)
						((ManagedFormAction) act).addActionable(actionable);
					else 
						log.warn("Managed Action is missing for " + actionable);
				}
				scopedActionIndex++;
			} else {
				actionableForLoop.add(actionable);	
			}
		}
		return this;
	}
	
	public Workflow load(Retention retention) {
		webExchange.setRetention(true);
		retention.perform(webExchange);
		return this;
	}
	
	public Workflow loop() {
		if (!webExchange.isRetention())
			throw new RuntimeException("Retention not initialized");
		actionableForLoop = new LinkedList<Actionable>();
		activeLoop = true;
		return this;
	}
	
	
	/**
	 * Proses dilakukan sequential
	 * 
	 * @return
	 */
	public Workflow endLoop() {
		if (!activeLoop) {
			throw new RuntimeException("Loop must be initialized");	
		}
		
		if (webExchange.getMetaDataSize() > 0) {
			int length = webExchange.getMetaDataSize();
			
			log.info("Total data-row " + length);
			
			int index = 1;
			for (Map<String, Object> metadata : webExchange.getListMetaData(getActiveMenu().getModuleId())) {
				log.info("Execute data-row index " + index);
				try {
					for (Actionable actionable : actionableForLoop) {
						
						if (actionable instanceof MenuAwareness) {
							activeMenu = ((MenuAwareness) actionable).getMenu();
						}
					
						executeActionableNoSession(actionable, metadata);	
					}
				} catch (Exception e) { 
					log.info("Skipping data-row index " + index);
					log.error("ERROR ", e);
				}
				
				index++;
			}
		}
		
		webExchange.setRetention(Boolean.FALSE);
		webExchange.clearMetaData();
		activeLoop = false;
		return this;
	}
	
	public void executeActionableNoSession(Actionable actionable, Map<String, Object> metadata) throws Exception {
		if (isPersistentSerializable(actionable)) {
			// execute map serializable
			if (isLocalVariable(actionable)) {
				ContextLoader.setObjectLocal(actionable);
				executeSafeActionable(actionable);
			} else {
				ContextLoader.setObjectWithCustom(actionable, metadata);
				executeSafeActionable(actionable);
			}
		} else {
			// execute common action
			ContextLoader.setObject(actionable);
			executeSafeActionable(actionable);
		}
	}
	
	private boolean isPersistentSerializable(Object object) {
		if (object instanceof ManagedFormAction) {
			return ContextLoader.isPersistentSerializable(((ManagedFormAction) object).getInheritClass());
		}
		return ContextLoader.isPersistentSerializable(object);
	}
	
	private boolean isLocalVariable(Object object) {
		if (object instanceof ManagedFormAction) {
			return ContextLoader.isLocalVariable(((ManagedFormAction) object).getInheritClass());
		}
		return ContextLoader.isLocalVariable(object);
	}
	
	private boolean isCompositeVariable(Object object) {
		if (object instanceof ManagedFormAction) {
			return ContextLoader.isCompositeVariable(((ManagedFormAction) object).getInheritClass());
		}
		return ContextLoader.isCompositeVariable(object);
	}
	
	public void executeActionableWithSession(Actionable actionable) throws Exception {
		if (isPersistentSerializable(actionable)) {
			// execute map serializable
			if (isLocalVariable(actionable) || isCompositeVariable(actionable)) {
				if (actionable instanceof ManagedMultipleFormAction) {
					try {
						ContextLoader.setObjectLocal(actionable);	
						executeSafeActionable(actionable);
						((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
					} catch (FailedTransactionException e) {
						webExchange.addListFailedSession(webExchange.getSessionList());
						log.info("Transaction is not completed, skipped for further processes");
						log.error("ERROR " + e.getMessage());
						log.error("ERROR ", e);
					}
				} else {
					int i = 0;
					while(true) {
						String sessionId = webExchange.createSession(i);
						if (!webExchange.isSessionFailed(sessionId)) {
							log.info("Execute data-row index " + i + " with session " + sessionId);
							webExchange.setCurrentSession(sessionId);
							
							try {
								if (isCompositeVariable(actionable)) {
									if (actionable instanceof ManagedFormAction) {
										((ManagedFormAction) actionable).setMetadata(webExchange.getMetaData(getActiveMenu().getModuleId(), i));
									} else {
										ContextLoader.setObjectLocalWithCustom(actionable, webExchange.getMetaData(getActiveMenu().getModuleId(), i));	
									}
								} else {
									ContextLoader.setObjectLocal(actionable);	
								}
							
								executeSafeActionable(actionable);
								((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
							} catch (FailedTransactionException e) {
								log.info("Transaction is not completed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
								log.error("ERROR " + e.getMessage());
								log.error("ERROR ", e);

								webExchange.addFailedSession(sessionId);
								((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
							}
						}
						i++;
						
						if (webExchange.getSessionList().size() == i) {
							webExchange.clearCachedSession();
							break;
						}
					}
				}

			} else {
				int i = 0;
				while(true) {
					String sessionId = webExchange.createSession(i);

					Map<String, Object> metadata = webExchange.getMetaData(getActiveMenu().getModuleId(),i);
					
					log.info("Execute data-row index " + i + " with session " + sessionId);
				
					try {
						if (actionable instanceof ManagedFormAction) {
							((ManagedFormAction) actionable).setMetadata(metadata);
						} else {
							ContextLoader.setObjectWithCustom(actionable, metadata);	
						}
						
						executeSafeActionable(actionable);
						((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
					} catch (FailedTransactionException e) {
						log.info("Transaction is not completed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
						log.error("ERROR " + e.getMessage());
						log.error("ERROR ", e);
						
						webExchange.addFailedSession(sessionId);
						((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
					}
					i++;
					
					if (webExchange.getListMetaData(getActiveMenu().getModuleId()).size() == i) {
						webExchange.clearCachedSession();
						break;
					}
				}
			}
		} else {
			ContextLoader.setObject(actionable);
			executeSafeActionable(actionable);
		}
	}
	
	public void executeSafeActionable(Actionable actionable) throws FailedTransactionException {
		int retry = 1;
		try {
			actionable.submit(webExchange);
		} catch (StaleElementReferenceException | ElementClickInterceptedException | TimeoutException  | NoSuchElementException | IllegalArgumentException e) {
			retryWhenException(actionable, ++retry);
		}
		
	}
	
	private void retryWhenException(Actionable actionable, int retry) throws FailedTransactionException {
		try {
			log.info("Something happened, be calm! we still loving you!");
			((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
			actionable.submit(webExchange);
		} catch (StaleElementReferenceException | ElementClickInterceptedException | TimeoutException  | NoSuchElementException | IllegalArgumentException e) { 
			if (retry < MAX_RETRY_LOAD_PAGE) {
				retryWhenException(actionable, ++retry);
			} else {
				log.error("ERROR " + e.getMessage());
				log.error("ERROR ", e);
				throw new FailedTransactionException("Failed for transaction");
			}	
		}
	}
	
	public static void main(String[] args) {
		int i = 0;
		System.out.println(i++);
		System.out.println(i);
	}
	
	
	public Workflow waitUntil(ModalSuccessAction actionable) {
		if (!activeLoop) {
			try {
				actionable.submit(webExchange);
			} catch (FailedTransactionException e) {
				log.error("ERROR ", e);
			}
		} else {
			if (scopedAction) {
				if (scopedActionIndex == 0) {
					ManagedFormAction scoped = null;
					if (actionable instanceof FormActionable) {
						scoped = new ManagedFormAction(actionable.getClass());
					} else if (actionable instanceof MultipleFormActionable) {
						scoped = new ManagedMultipleFormAction(actionable.getClass());
					}
					
					if (scoped != null) {
						scoped.addActionable(actionable);
						actionableForLoop.add(scoped);						
					} else {
						log.warn("Managed Action is missing for " + actionable);
					}
				} else {
					Actionable act = actionableForLoop.getLast();
					if (act != null)
						((ManagedFormAction) act).addActionable(actionable);
					else
						log.warn("Managed Action is missing for " + actionable);
				}
			} else {
				actionableForLoop.add(actionable);	
			}
			 
		}
		return this;
	}
	
	public Workflow clearSession() {
		log.info("Clear session");
		DBConnection.close();
		webExchange.clear();
		Thread.currentThread().interrupt();
		return this;
	}
	

}
