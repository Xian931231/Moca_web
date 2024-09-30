package com.mocafelab.web.enums;

/**
 * 과금 타입
 * @author tmddus2123
 * CPM
 * CPP
 */

public enum PayType {
	CPM(350000),
	CPP(100000);
	
	int price;
	
	PayType(int price) {
		this.price = price;
	}
	
	public int getPrice() {
		return price;
	}
	
	public static PayType getPrice(int price) {
		for(PayType pt : PayType.values()) {
			if(pt.getPrice() == price) {
				return pt;
			}
		}
		
		throw new RuntimeException();
	}
}
