package org.komparator.mediator.domain;

/**
 * Item entity. 
 */
public class Item {
	/** Item identifier. */	
	private String supplierId;
	/** Item identifier. */
	private String productId;
	/** Item description. */
	private String description;
	/** Available quantity of Item. */
	private int quantity;
	/** Price of Item */
	private int price;

	/** Create a new Item */
	public Item(String productId, String supplierId, String description, int quantity, int price) {
		this.productId = productId;
		this.supplierId = supplierId;
		this.description = description;
		this.quantity = quantity;
		this.price = price;
	}

	public String getId() {
		return productId + supplierId;
	}
	
	public String getProductId() {
		return productId;
	}
	
	public String getSupplierId() {
		return supplierId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String d) {
		this.description = description;
	}

	public int getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setPrice(int price) {
		this.price = price;
	}
	
	public void addQuantity(int i){
		this.quantity += i;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Item [productId=").append(productId);
		builder.append(", supplierId=").append(supplierId);
		builder.append(", description=").append(description);
		builder.append(", quantity=").append(quantity);
		builder.append(", price=").append(price);
		builder.append("]");
		return builder.toString();
	}

}
