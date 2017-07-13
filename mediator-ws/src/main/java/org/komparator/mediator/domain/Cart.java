package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Cart {

	
	private String cartId;

	private Map<String, Item> items = new ConcurrentHashMap<>();

	public Cart(String cartId) {
		this.cartId = cartId;
	}

	public String getCartId(){
		return cartId;
	}
	
	public Boolean itemExists(String id) {
		return items.containsKey(id);
	}

	public Set<String> getItemsIDs() {
		return items.keySet();
	}

	public void updateItem(String productId, String supplierId, String description, int quantity, int price){
		if(itemExists(productId+supplierId)){
			getItem(productId+supplierId).setQuantity(quantity);
			getItem(productId+supplierId).setPrice(price);
			getItem(productId+supplierId).setDescription(description);
		}
		else{
			addItem(productId, supplierId, description, quantity, price);
		}
	}

	public Item getItem(String id) {
		return items.get(id);
	}
	
	public void addItem(String productId, String supplierId, String description, int quantity, int price){
		if(acceptItem(productId, supplierId, description, quantity, price)){
			if(itemExists(productId + supplierId)){
				getItem(productId + supplierId).addQuantity(quantity);
			}
			else{
				items.put(productId + supplierId, new Item(productId, supplierId, description, quantity, price));
			}
		}
	}
	
	private Boolean acceptItem(String productId, String supplierId, String description, int quantity, int price) {
		return productId != null && !"".equals(productId) && supplierId != null && !"".equals(supplierId) && description != null &&
		!"".equals(description) && quantity > 0 && price > 0;
	}

}
