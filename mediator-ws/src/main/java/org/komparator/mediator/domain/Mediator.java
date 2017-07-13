package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Domain Root. */
public class Mediator {

	// Members ---------------------------------------------------------------

	/**
	 * Map of existing products. Uses concurrent hash table implementation
	 * supporting full concurrency of retrievals and high expected concurrency
	 * for updates.
	 */
	//private Map<String, Product> products = new ConcurrentHashMap<>();

	/**
	 * Global purchase identifier counter. Uses lock-free thread-safe single
	 * variable.
	 */
	private AtomicInteger purchaseIdCounter = new AtomicInteger(0);

	/** Map of purchases. Also uses concurrent hash table implementation. */
	private Map<String, Cart> carts = new ConcurrentHashMap<>();
	
	private Map<String, Purchase> purchases = new ConcurrentHashMap<>();

	private Mediator() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final Mediator INSTANCE = new Mediator();
	}

	public static synchronized Mediator getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void reset() {
		carts.clear();
// 		cartIdCounter.set(0);
	}

	public Boolean cartExists(String cid) {
		return carts.containsKey(cid);
	}

	public Set<String> getCartsIDs() {
		return carts.keySet();
	}

	public Cart getCart(String cid) {
		return carts.get(cid);
	}

	public void addToCart(String cid, String productId, String supplierId, String description, int quantity, int price){
		if(cartExists(cid)){
			getCart(cid).addItem(productId, supplierId, description, quantity, price);
		}
		else{
			Cart c = new Cart(cid);
			c.addItem(productId, supplierId, description, quantity, price);
			carts.put(cid, c);
		}
	}

	public Boolean purchaseExists(String cid) {
		return purchases.containsKey(cid);
	}

	public Set<String> getPurchasesIDs() {
		return purchases.keySet();
	}

	public Purchase getPurchase(String cid) {
		return purchases.get(cid);
	}

	public Purchase addPurchase(String cartId, List<Item> bought, List<Item> notBought){
		String pid = generatePurchaseId();
		Purchase p = new Purchase(pid, cartId, bought, notBought);
		purchases.put(pid, p);
		return p;
	}

	//Mediator method to add a non existing purchase in backup server
	public void updatePurchase(String purchaseId, List<Item> bought, List<Item> notBought){
		Purchase p = new Purchase(purchaseId, "0", bought, notBought);
		purchases.put(purchaseId, p);
	}

	private String generatePurchaseId() {
		// relying on AtomicInteger to make sure assigned number is unique
		int purchaseId = purchaseIdCounter.incrementAndGet();
		return Integer.toString(purchaseId);
	}

}
