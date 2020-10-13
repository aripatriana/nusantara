package com.nusantara.automate.constants;

import com.nusantara.automate.Menu;

public enum SimpleMenu implements Menu{

	// menu order
	ORDER_CRE("Seller Management","Order Repo","REPO::SELL_MGMT::ORDER_REPO","Create","order-repo.create"),
	ORDER_CREDC("Seller Management","Order Repo","REPO::SELL_MGMT::ORDER_REPO","Create Direct Check","order-repo.create-direct-check"),
	ORDER_CREDA("Seller Management","Order Repo","REPO::SELL_MGMT::ORDER_REPO","Create Direct Approve","order-repo.create-direct-approve"),
	ORDER_INQ("Seller Management","Order Repo","REPO::SELL_MGMT::ORDER_REPO","Inquiry","order-repo.inquiry"),
	ORDER_CHK("Seller Management","Order Repo","REPO::SELL_MGMT::ORDER_REPO","Check","order-repo.check"),
	ORDER_APP("Seller Management","Order Repo","REPO::SELL_MGMT::ORDER_REPO","Approve","order-repo.approve"),
	
	// menu order confirmation
	ORDER_CONF_CRE("Buyer Management","Order Confirmation","REPO::BUY_MGMT::CONFIRMATION_ORDER","Create","order-confirmation.create"),
	ORDER_CONF_CREDC("Buyer Management","Order Confirmation","REPO::BUY_MGMT::CONFIRMATION_ORDER","Create-Direct Check","order-confirmation.create-direct-check"),
	ORDER_CONF_CREDA("Buyer Management","Order Confirmation","REPO::BUY_MGMT::CONFIRMATION_ORDER","Create-Direct Approve","order-confirmation.create-direct-approve"),
	ORDER_CONF_INQ("Buyer Management","Order Confirmation","REPO::BUY_MGMT::CONFIRMATION_ORDER","Inquiry","order-confirmation.inquiry"),
	ORDER_CONF_CHK("Buyer Management","Order Confirmation","REPO::BUY_MGMT::CONFIRMATION_ORDER","Check","order-confirmation.check"),
	ORDER_CONF_APP("Buyer Management","Order Confirmation","REPO::BUY_MGMT::CONFIRMATION_ORDER","Approve","order-confirmation.approve");
	
	private String menu;
	private String subMenu;
	private String menuId;
	private String form;
	private String id;
	
	private SimpleMenu(String menu, String subMenu, String menuId, String form, String id) {
		this.menu = menu;
		this.subMenu = subMenu;
		this.menuId = menuId;
		this.form = form;
		this.id = id;
	}
	
	public String getMenu() {
		return menu;
	}
	
	public String getMenuId() {
		return menuId;
	}
	
	public String getForm() {
		return form;
	}
	
	public String getSubMenu() {
		return subMenu;
	}
	
	@Override
	public String getId() {
		return id;
	}
}
