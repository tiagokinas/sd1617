package org.komparator.mediator.ws.cli;


import java.util.*;
import javax.xml.ws.*;
import org.komparator.mediator.ws.*;

public class FrontEnd implements MediatorPortType {
    MediatorPortType port = null;

    public FrontEnd(MediatorPortType p){
        port=p;
    }

    //has to generate ids
    //has to implement send at max 1 time
    //has to implement try-catch to certain exceptions
    //      and add uddi rebinds

    public MediatorPortType getPort(){return port;}

    @Override
    public void clear() {
        port.clear();
    }

    @Override
    public void imAlive() {
        System.err.println("FrontEnd is asking Simon...");
        port.imAlive();
    }

    @Override
    public void updateShopHistory(ShoppingResultView srv){
        port.updateShopHistory(srv);
    }

    @Override
    public void updateCart(CartView cv){
        port.updateCart(cv);
    }

    @Override
    public String ping(String arg0) {
        return port.ping(arg0);
    }

    @Override
    public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
        return port.searchItems(descText);
    }

    @Override
    public List<CartView> listCarts() {
        return port.listCarts();
    }

    @Override
    public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
        return port.getItems(productId);
    }

    @Override
    public ShoppingResultView buyCart(String cartId, String creditCardNr)
    throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        return port.buyCart(cartId, creditCardNr);
    }

    @Override
    public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
    InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        port.addToCart(cartId, itemId, itemQty);
    }

    @Override
    public List<ShoppingResultView> shopHistory() {
        return port.shopHistory();
    }
}
