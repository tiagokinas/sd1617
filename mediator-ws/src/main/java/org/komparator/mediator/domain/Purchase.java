package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Purchase {

	private String purchaseId;
	private String cartId;
	private int totalPrice;

	private List<Item> itemsBought = new ArrayList<Item>();
	private List<Item> itemsNotBought = new ArrayList<Item>();

	public Purchase(String purchaseId, String cartId, List<Item> itemsBought, List<Item> itemsNotBought) {
		this.purchaseId = purchaseId;
		this.cartId = cartId;
		this.itemsBought = itemsBought;
		this.itemsNotBought = itemsNotBought;

		totalPrice = 0;
		for(Item i : itemsBought)
			totalPrice += i.getPrice()*i.getQuantity();
	}

	public String getCartId(){
		return cartId;
	}
	
	public String getPurchaseId(){
		return purchaseId;
	}
	
	public List<Item> getItemsBought(){
		return itemsBought;
	}
	
	public List<Item> getItemsNotBought(){
		return itemsNotBought;
	}

	public void updatePurchase(List<Item> bought, List<Item> notBought){
		this.itemsBought = bought;
		this.itemsNotBought = notBought;

		totalPrice = 0;
		for(Item i : itemsBought)
			totalPrice += i.getPrice()*i.getQuantity();
	}
	
	public String state(){
		if(itemsNotBought.size() == 0)
			return "COMPLETA";
		else if(itemsBought.size() == 0)
			return "VAZIA";
		else
			return "PARCIAL";
	}

	public int getTotalPrice(){
		return totalPrice;
	}
}
