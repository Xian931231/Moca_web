package com.mocafelab.web.enums;

/**
 * schedule_slot의 sort_info json 중 sort_type 값 
 * @author master
 *
 */
public enum SlotSortType {
	// area: 지역 광고
	// time: 시간 광고
	// public: 공익광고
	// default: 디폴트 광고
	// cpm: CPM 광고
	AREA("A"),
	TIME("T"),
	PUBLIC("C"),
	DEFAULT("D"),
	CPM("M"),
	CPP("P")
	;
	
	private String sgKind;
	
	SlotSortType(String sgKind){ 
		this.sgKind = sgKind;
	}
	
	public String getSgKind() {
		return sgKind;
	}
	
	public static SlotSortType getName(String sortType) {
		for(SlotSortType value : SlotSortType.values()) {
			if(value.name().equalsIgnoreCase(sortType)) {
				return value;
			}
		}
		return null;
	};
}
