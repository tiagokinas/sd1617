package org.komparator.mediator.ws;

import java.util.*;

import javax.jws.WebService;
import pt.ulisboa.tecnico.sdis.ws.uddi.*;
import pt.ulisboa.tecnico.sdis.ws.cli.*;

import org.komparator.mediator.ws.cli.*;
import org.komparator.supplier.ws.cli.*;

import org.komparator.supplier.ws.*;

// import org.komparator.supplier.*;
// import org.komparator.supplier.domain.Product;
import org.komparator.mediator.ws.*;
import org.komparator.mediator.domain.*;
import javax.jws.HandlerChain;

@HandlerChain(file = "/mediator-ws_handler-chain.xml")
@WebService(
	endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
	wsdlLocation = "mediator.1_0.wsdl", 
	name = "MediatorWebService", 
	portName = "MediatorPort", 
	targetNamespace = "http://ws.mediator.komparator.org/", 
	serviceName = "MediatorService"
	)
public class MediatorPortImpl implements MediatorPortType{
	
	private final String supplierName = "A53_Supplier%";
	
	// end point manager
	private MediatorEndpointManager endpointManager;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	// Main operations -------------------------------------------------------

	public synchronized  List<ItemView> getItems(String productId) throws InvalidItemId_Exception{
		if (productId == null)
			throwInvalidItemId("Product identifier cannot be null!");
		productId = productId.trim();
		if (productId.length() == 0)
			throwInvalidItemId("Product identifier cannot be empty or whitespace!");

		List<ItemView> liv = new ArrayList<ItemView>();
		ArrayList<SupplierClient> suppliers;
		ProductView pv = null;

		try{
			suppliers = getSuppliers(supplierName);

			for(SupplierClient sc : suppliers){
				pv = sc.getProduct(productId);

				if(pv !=null)
					liv.add(newItemView(pv, sc.getName()));
			}
		}catch(UDDINamingException ex){
			System.err.println("Caught exception while contacting UDDI\n" + ex.getMessage());
		}catch(SupplierClientException ex){
			System.err.println("Caught exception while creating a supplier client\n" + ex.getMessage());
		}catch(BadProductId_Exception ex){
			throwInvalidItemId(ex.getMessage());
		}
		
		Collections.sort(liv, new Comparator<ItemView>() {
			@Override
			public int compare(ItemView item1, ItemView item2)
			{ return  new Integer(item1.getPrice()).compareTo(new Integer(item2.getPrice()));}
		});
		
		return liv;
	}
	
	@Override
	public synchronized  List<ItemView> searchItems(String descText) throws InvalidText_Exception{
		if(descText == null || descText.trim().length() == 0)
			throwInvalidText("Text is invalid");

		List<ItemView> liv = new ArrayList<ItemView>();
		ArrayList<SupplierClient> suppliers;

		try{
			suppliers = getSuppliers(supplierName);

			for(SupplierClient sc : suppliers){
				
				List<ProductView> lpv = sc.searchProducts(descText);
				for(ProductView pv : lpv){
					liv.add(newItemView(pv, sc.getName()));
				}
			}
		}catch(UDDINamingException ex){
			System.err.println("Caught exception while contacting UDDI\n" + ex.getMessage());
		}catch(SupplierClientException ex){
			System.err.println("Caught exception while creating a supplier client\n" + ex.getMessage());
		}catch(BadText_Exception ex){
			throwInvalidText(ex.getMessage());
		}
		
		Collections.sort(liv, new Comparator<ItemView>() {
			@Override
			public int compare(ItemView item1, ItemView item2)
			{ 
				int c = item1.getItemId().getProductId().compareTo(item2.getItemId().getProductId());
				if(c == 0){return  new Integer(item1.getPrice()).compareTo(new Integer(item2.getPrice()));}
				else {return c;}
			}
		});
		
		return liv;
	}

	@Override
	public synchronized  void addToCart(String cartId, ItemIdView itemId, int itemQty) 
	throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception
	{
		if(cartId == null || !cartId.matches("[A-Z0-9a-z]+"))
			throwInvalidCartId("Cart identifier must be alphanumeric!");
		
		if(itemId == null || itemId.getSupplierId() == null || itemId.getProductId() == null 
			|| !(itemId.getSupplierId() + itemId.getProductId()).matches("[A-Z0-9a-z_]+"))
			throwInvalidItemId("Product identifier must be alphanumeric!");
		
		if(itemQty < 1)
			throwInvalidQuantity("Item quantity must be greater than 0!");
		
		Mediator m = Mediator.getInstance();
		SupplierClient sc = null;
		ProductView pv = null;
		
		try{
			sc = getSupplier(itemId.getSupplierId());
			pv = sc.getProduct(itemId.getProductId());
			
		}catch(UDDINamingException | SupplierClientException ex){
			throwInvalidItemId(ex.getMessage());
		}catch(BadProductId_Exception ex){
			throwInvalidItemId(ex.getMessage());
		}
		
		if(pv == null)
			throwInvalidItemId("Item does not exist!");
		else if(pv.getQuantity() < itemQty ){
			throwNotEnoughItems("Supplier does not have enough products!");
		}else{
            if(m.cartExists(cartId) && m.getCart(cartId).itemExists(pv.getId() + itemId.getSupplierId()) 
                && m.getCart(cartId).getItem(pv.getId() + itemId.getSupplierId()).getQuantity() + itemQty > pv.getQuantity())
                    throwNotEnoughItems("Supplier does not have enough products!");
            else
                m.addToCart(cartId, pv.getId(), itemId.getSupplierId(), pv.getDesc(),  itemQty, pv.getPrice());
            	updateCart(newCartView(Mediator.getInstance().getCart(cartId)));
        }


	}

	@Override
	public synchronized  ShoppingResultView buyCart(String cartId, String creditCardNr)
	throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception
	{
		if(cartId == null || !cartId.matches("[A-Z0-9a-z]+"))
			throwInvalidCartId("Cart identifier must be alphanumeric!");
        
        Mediator m = Mediator.getInstance();
		Cart cart = null;
        if (m.cartExists(cartId))
			cart = m.getCart(cartId);
		else
			throwInvalidCartId("Cart with id " + cartId + " does not exist!");
			
		if(creditCardNr == null || !creditCardNr.matches("[0-9]+"))
			throwInvalidCreditCard("Credit Card number must be numeric!");
		if(!validateNumber(creditCardNr))
			throwInvalidCreditCard("Credit Card number must be valid!");





		List<Item> itemsBought = new ArrayList<Item>();
		List<Item> itemsNotBought = new ArrayList<Item>();

		SupplierClient sc = null;
		ProductView pv = null;
		Item item = null;
		Purchase p = null;

		for(String itemid : cart.getItemsIDs()){
			item = cart.getItem(itemid);
			try{
				sc = getSupplier(item.getSupplierId());
				pv = sc.getProduct(item.getProductId());
				if(pv != null && pv.getQuantity() >= item.getQuantity()) {
					String s = sc.buyProduct(item.getProductId(), item.getQuantity());

					itemsBought.add(item);
				}
				else
					throwInvalidQuantity("");
			}catch(InvalidQuantity_Exception|BadQuantity_Exception|InsufficientQuantity_Exception ex){
				itemsNotBought.add(item);
				System.err.println("Quantity Exception " + ex.getMessage());
			}catch(UDDINamingException | SupplierClientException ex){
				itemsNotBought.add(item);
				System.err.println("Supplier Exception " + ex.getMessage());
			}catch(BadProductId_Exception ex){
				itemsNotBought.add(item);
				System.err.println("Product Exception " + ex.getMessage());
			}
		}
		ShoppingResultView srv = newShoppingResultView(m.addPurchase(cartId, itemsBought, itemsNotBought));
		updateShopHistory(srv);
		return srv;
	}


	// Auxiliary operations --------------------------------------------------	

	private ArrayList<SupplierClient> getSuppliers(String wsName) throws UDDINamingException, SupplierClientException{
		ArrayList<SupplierClient> suppliers = new ArrayList<SupplierClient>();
		UDDINaming uddi = this.endpointManager.getUddiNaming();

		Collection<UDDIRecord> records = uddi.listRecords(wsName);
		for(UDDIRecord r : records){
			suppliers.add(new SupplierClient(r.getUrl(), r.getOrgName()));
		}
		return suppliers;
	}

	private SupplierClient getSupplier(String wsName) throws UDDINamingException, SupplierClientException{
		UDDINaming uddi = this.endpointManager.getUddiNaming();

		UDDIRecord r = uddi.lookupRecord(wsName);
		if (r != null)
			return new SupplierClient(r.getUrl(), r.getOrgName());
		else
			throw new SupplierClientException("Supplier does not exist in UDDI!");
	}

	private boolean validateNumber(String cardNumber){
		boolean res = false;
		try{
			CreditCardClient cc = new  CreditCardClient(endpointManager.getCCURL());

			res = cc.validateNumber(cardNumber);
		}catch(CreditCardClientException ex){
			System.err.println("Credit card " + cardNumber + "is not valid");
		}

		return res;
	}

	@Override
	public synchronized  String ping(String name) {
		String response = "Response:\n";
		ArrayList<SupplierClient> suppliers = new ArrayList<SupplierClient>();

		try{
			suppliers = getSuppliers(supplierName);

			for(SupplierClient sc : suppliers){
				response += sc.ping("client") + "\n";
			}
		}catch(UDDINamingException ex){
			response += "Caught exception while contacting UDDI\n" + ex.getMessage();
		}catch(SupplierClientException ex){
			response = "Caught exception while creating a supplier client\n" + ex.getMessage();
		}
		response += name + "\n";
		return response; 
	}

	@Override
	public synchronized  void clear(){
		ArrayList<SupplierClient> suppliers = new ArrayList<SupplierClient>();
		try{
			suppliers = getSuppliers(supplierName);

			for(SupplierClient sc : suppliers){
				System.err.println("Clearing supplier " + sc.getName());
				sc.clear();
			}
		}catch(UDDINamingException ex){
			System.err.println("Caught exception while contacting UDDI\n" + ex.getMessage());
		}catch(SupplierClientException ex){
			System.err.println("Caught exception while creating a supplier client\n" + ex.getMessage());
		}

		Mediator.getInstance().reset();
	}

	@Override
	public synchronized  void imAlive(){
		if(!endpointManager.isPrimary()){
			endpointManager.generateNewTimestamp();
		}
		System.out.println("Simon says im Alive!");
	}

	@Override
	public synchronized  void updateShopHistory(ShoppingResultView srv){
		if(endpointManager.isPrimary()){
			try{
			MediatorClient mc = new MediatorClient(MediatorApp.wsSecondaryMediator);

			mc.updateShopHistory(srv);

			}catch(Exception ex){System.err.println(ex.getMessage());}
		}
		else{
			updatePurchaseWithView(srv);
		}
	}

	@Override
	public synchronized  void updateCart(CartView cv){
		if(endpointManager.isPrimary()){
			try{
			MediatorClient mc = new MediatorClient(MediatorApp.wsSecondaryMediator);

			mc.updateCart(cv);

			}catch(Exception ex){System.err.println(ex.getMessage());}
		}
		else{
			updateCartWithView(cv);
		}
	}

	@Override
	public synchronized  List<CartView> listCarts(){
		List<CartView> lcv = new ArrayList<CartView>();

		Mediator m = Mediator.getInstance();

		for(String cid : m.getCartsIDs()){
			CartView cv = new CartView();
			cv.setCartId(cid);
			for(String id : m.getCart(cid).getItemsIDs()){
				cv.getItems().add(newCartItemView(m.getCart(cid).getItem(id)));
			}
			lcv.add(cv);
		}
		return lcv;
	}

	@Override
	public synchronized  List<ShoppingResultView> shopHistory(){
		List<ShoppingResultView> lsrv = new ArrayList<ShoppingResultView>();

		Mediator m = Mediator.getInstance();

		for(String pid: m.getPurchasesIDs())
			lsrv.add(newShoppingResultView(m.getPurchase(pid)));
		
		return lsrv;
	}

	private void updateCartWithView(CartView cv){
		for(CartItemView civ : cv.getItems()){
			if(Mediator.getInstance().cartExists(cv.getCartId())){
				Mediator.getInstance().getCart(cv.getCartId()).updateItem(civ.getItem().getItemId().getProductId(), 
				civ.getItem().getItemId().getSupplierId(), civ.getItem().getDesc(), civ.getQuantity(), civ.getItem().getPrice());
			}
			else{
				Mediator.getInstance().addToCart(cv.getCartId(), civ.getItem().getItemId().getProductId(), 
				civ.getItem().getItemId().getSupplierId(), civ.getItem().getDesc(), civ.getQuantity(), civ.getItem().getPrice());
			}
		}
	}

	private void updatePurchaseWithView(ShoppingResultView srv){
			List<Item> bought = new ArrayList<Item>();
			List<Item> notBought = new ArrayList<Item>();

			for(CartItemView civ : srv.getPurchasedItems()){
				bought.add(new Item(civ.getItem().getItemId().getProductId(), civ.getItem().getItemId().getSupplierId(), 
									civ.getItem().getDesc(), civ.getQuantity(), civ.getItem().getPrice()));
			}

			for(CartItemView civ : srv.getDroppedItems()){
				notBought.add(new Item(civ.getItem().getItemId().getProductId(), civ.getItem().getItemId().getSupplierId(), 
									civ.getItem().getDesc(), civ.getQuantity(), civ.getItem().getPrice()));
			}

			if(Mediator.getInstance().purchaseExists(srv.getId())){
				
				Mediator.getInstance().getPurchase(srv.getId()).updatePurchase(bought, notBought);
			}
			else{
				//Mediator method to add a non existing purchase in backup server
				Mediator.getInstance().updatePurchase(srv.getId(), bought, notBought);
			}
			
	}

	// View helpers -----------------------------------------------------

	private ItemView newItemView(ProductView pv, String supplier) {
		ItemView view = new ItemView();
		ItemIdView idView = new ItemIdView();
		idView.setProductId(pv.getId());
		idView.setSupplierId(supplier);
		view.setItemId(idView);
		view.setDesc(pv.getDesc());
		view.setPrice(pv.getPrice());
		return view;
	}

	private ItemView newItemView(Item i) {
		ItemView view = new ItemView();
		ItemIdView idView = new ItemIdView();
		idView.setProductId(i.getProductId());
		idView.setSupplierId(i.getSupplierId());
		view.setItemId(idView);
		view.setDesc(i.getDescription());
		view.setPrice(i.getPrice());
		return view;
	}

	private CartView newCartView(Cart c) {
		CartView cv = new CartView();
		cv.setCartId(c.getCartId());
		return cv;
	}

	public CartItemView newCartItemView(Item i){
		CartItemView civ = new CartItemView();
		civ.setItem(newItemView(i));
		civ.setQuantity(i.getQuantity());
		return civ;
	}

	public ShoppingResultView newShoppingResultView(Purchase p){
		ShoppingResultView srv = new ShoppingResultView();
		srv.setId(p.getPurchaseId());
		for(Item i : p.getItemsBought())
			srv.getPurchasedItems().add(newCartItemView(i));

		for(Item i : p.getItemsNotBought())
			srv.getDroppedItems().add(newCartItemView(i));

		if(p.state() == "COMPLETA")
			srv.setResult(Result.COMPLETE);
		else if(p.state() == "VAZIA")
			srv.setResult(Result.EMPTY);
		else
			srv.setResult(Result.PARTIAL);
		srv.setTotalPrice(p.getTotalPrice());

		return srv;
	}

	// Exception helpers -----------------------------------------------------

	// Helper method to throw new NotEnoughItems exception 
	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.message = message;
		throw new NotEnoughItems_Exception(message, faultInfo);
	}


	//Helper method to throw new EmptyCart exception 
	private void throwEmptyCart(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.message = message;
		throw new EmptyCart_Exception(message, faultInfo);
	}


	// Helper method to throw new InvalidCartId exception 
	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.message = message;
		throw new InvalidCartId_Exception(message, faultInfo);
	}


	// Helper method to throw new InvalidCreditCard exception 
	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.message = message;
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}

	// Helper method to throw new InvalidItemId exception 
	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.message = message;
		throw new InvalidItemId_Exception(message, faultInfo);
	}

	//	Helper method to throw new InvalidQuantity exception 
	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.message = message;
		throw new InvalidQuantity_Exception(message, faultInfo);
	}

		//	Helper method to throw new InvalidText exception 
	private void throwInvalidText(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.message = message;
		throw new InvalidText_Exception(message, faultInfo);
	}


}
