package com.nusantara.automate.workflow;


import java.util.LinkedList;
import java.util.Map;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.AbstractBaseDriver;
import com.nusantara.automate.Actionable;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.DBConnection;
import com.nusantara.automate.DriverManager;
import com.nusantara.automate.FormActionable;
import com.nusantara.automate.Menu;
import com.nusantara.automate.MenuAwareness;
import com.nusantara.automate.MultipleFormActionable;
import com.nusantara.automate.Retention;
import com.nusantara.automate.WebElementWrapper;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.action.ManagedFormAction;
import com.nusantara.automate.action.ManagedMultipleFormAction;
import com.nusantara.automate.action.common.ModalSuccessAction;
import com.nusantara.automate.action.common.OpenFormAction;
import com.nusantara.automate.action.common.OpenMenuAction;
import com.nusantara.automate.action.common.OpenSubMenuAction;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.report.ReportManager;
import com.nusantara.automate.report.ReportMonitor;


/**
 * Main workflow
 * 
 * @author ari.patriana
 *
 */
public abstract class Workflow {

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
	
	public Workflow openPage(String url) {
		log.info("Open Page " + url);
		
		DriverManager.getDefaultDriver().get(url);
		return this;
	}
	
	public void setActiveMenu(Menu activeMenu) {
		this.activeMenu = activeMenu;
		webExchange.put("active_module_id", activeMenu.getModuleId());
		webExchange.put("active_menu_id", activeMenu.getId());
	}

	public Workflow openMenu(Menu menu) {
		OpenMenuAction menuAction = new OpenMenuAction(null, menu.getMenu());
		OpenSubMenuAction subMenuAction = new OpenSubMenuAction(menuAction, menu.getSubMenu(), menu.getMenuId());
		OpenFormAction formAction = new OpenFormAction((menu.getSubMenu() != null ? subMenuAction : menuAction), menu.getMenuId(), menu.getForm());
		((MenuAwareness) formAction).setMenu(menu);
		
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
	
	public Workflow actionMajor(Actionable actionable) {
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
	
	public abstract Workflow endLoop() throws Exception;
	
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
	
	@Deprecated
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
						
						ReportMonitor.logDataEntry(getWebExchange().getSessionList(),getWebExchange().get("active_scen").toString(),
								getWebExchange().get("active_workflow").toString(), null, null);
					} catch (FailedTransactionException e) {
						webExchange.addListFailedSession(webExchange.getSessionList());
						log.info("Transaction is not completed, skipped for further processes");
						log.error("ERROR ", e);
						
						ReportMonitor.logDataEntry(getWebExchange().getSessionList(),getWebExchange().get("active_scen").toString(),
								getWebExchange().get("active_workflow").toString(), null, null, 
								e.getMessage(), ReportManager.FAILED);
					}
				} else {
					int i = 0;
					while(true) {
						String sessionId = webExchange.createSession(i);
						if (!webExchange.isSessionFailed(sessionId)) {
							log.info("Execute data-row index " + i + " with session " + sessionId);
							webExchange.setCurrentSession(sessionId);
							
							Map<String, Object> metadata = webExchange.getMetaData(getActiveMenu().getModuleId(), i);
							
							try {	
								if (isCompositeVariable(actionable)) {
									if (actionable instanceof ManagedFormAction) {
										((ManagedFormAction) actionable).setMetadata(metadata);
									} else {
										ContextLoader.setObjectLocalWithCustom(actionable, metadata);	
									}
								} else {
									ContextLoader.setObjectLocal(actionable);	
								}
							
								executeSafeActionable(actionable);
								((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
								
								ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get("active_scen").toString(),
										getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalMap(), metadata);
							} catch (FailedTransactionException e) {
								log.info("Transaction is not completed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
								log.error("ERROR ", e);

								webExchange.addFailedSession(sessionId);
								((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
								
								ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get("active_scen").toString(),
										getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalMap(),
										metadata, e.getMessage(), ReportManager.FAILED);
							}
						}
						i++;
						
						if (webExchange.getSessionList().size() <= i) {
							webExchange.clearCachedSession();
							break;
						}
					}
				}

			} else {
				int i = 0;
				while(true) {
					String sessionId = webExchange.createSession(i);
					if (!webExchange.isSessionFailed(sessionId)) {
						Map<String, Object> metadata = webExchange.getMetaData(getActiveMenu().getModuleId(),i, true);
						
						log.info("Execute data-row index " + i + " with session " + sessionId);
					
						try {
							if (actionable instanceof ManagedFormAction) {
								((ManagedFormAction) actionable).setMetadata(metadata);
							} else {
								ContextLoader.setObjectWithCustom(actionable, metadata);	
							}
							
							executeSafeActionable(actionable);
							((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
							
							ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get("active_scen").toString(),
									getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalMap(), metadata);
						} catch (FailedTransactionException e) {
							log.info("Transaction is not completed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
							log.error("ERROR ", e);
							
							webExchange.addFailedSession(sessionId);
							((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
							
							ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(), getWebExchange().get("active_scen").toString(),
									getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalMap(),
									metadata, e.getMessage(), ReportManager.FAILED);
						}
					}
					i++;
					
					if (webExchange.getListMetaData(getActiveMenu().getModuleId()).size() <= i) {
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
		} catch (StaleElementReferenceException | ElementNotInteractableException | TimeoutException  | NoSuchElementException | IllegalArgumentException e) {
			retryWhenException(actionable, ++retry);
		}
	}
	
	private void retryWhenException(Actionable actionable, int retry) throws FailedTransactionException {
		try {
			log.info("Something happened, be calm! we still loving you!");

			((AbstractBaseDriver) actionable).getDriver().navigate().refresh();
			actionable.submit(webExchange);
		} catch (StaleElementReferenceException | ElementNotInteractableException | TimeoutException  | NoSuchElementException | IllegalArgumentException e) {			
			if (retry < MAX_RETRY_LOAD_PAGE) {
				retryWhenException(actionable, ++retry);
			} else {
				try {
					((AbstractBaseDriver)actionable).captureFailedFullModal(((WebElementWrapper)actionable).getModalId());
				} catch (Exception e1) {
					((AbstractBaseDriver)actionable).captureFailedFullWindow();
				}
				
				log.error("ERROR ", e);
				throw new FailedTransactionException("Failed for transaction, " + e.getMessage());
			}	
		}
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
//		Thread.currentThread().interrupt();
		return this;
	}
	

}
