package com.github.switcherapi.fixture;

/**
 * Blueprint class to generate JSON to validate Payload algorithms
 * 
 * @author Roger Floriano (petruki)
 * @since 2022-06-13
 */
public class Product {
	
	public String name;
	public String[] categories;
	public Order order;
	
	public static Product getFixture() {
		Product product = new Product();
		product.name = "product-1";
		product.categories = new String[] {"A", "B"};
		product.order = Order.getOrder();
		return product;
	}

	static class Order {
		public int qty;
		public Tracking[] tracking;
		
		public static Order getOrder() {
			Order order = new Order();
			order.qty = 1;
			order.tracking = new Tracking[] { 
					Tracking.getTracking("delivered", null),
					Tracking.getTracking("delivered", "comments")};
			return order;
		}
	}
	
	static class Tracking {
		public String status;
		public String comments;
		
		public static Tracking getTracking(String status, String comments) {
			Tracking tracking = new Tracking();
			tracking.status = status;
			tracking.comments = comments;
			return tracking;
		}
	}

}
