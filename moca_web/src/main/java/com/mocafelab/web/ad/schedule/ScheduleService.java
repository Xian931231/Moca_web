package com.mocafelab.web.ad.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.gson.Gson;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;

@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleService {
	
	@Autowired
	private ScheduleMapper scheduleMapper;
	
	@Autowired
	private BeanFactory beanFactory;

	/**
	 * 편성표 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 편성표 리스트 
		List<Map<String, Object>> scheduleList = scheduleMapper.getList(param);
		
		// 편성표 리스트 총 개수
		int scheduleListCnt = scheduleMapper.getListCnt(param);
		
		// 편성표에 등록된 상품 맵핑
		scheduleList.forEach(schedule -> {
			schedule.put("product_list", scheduleMapper.getProductList(schedule));
		});
		
		respMap.setBody("list", scheduleList);
		respMap.setBody("tot_cnt", scheduleListCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 편성표 상세
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 상품 아이디로 편성표 아이디 조회
		if(!CommonUtil.checkIsNull(param, "product_id")) {
			Map<String,Object> productSchedule = scheduleMapper.getProductSchedule(param);
			
			if (CommonUtil.checkIsNull(productSchedule)) {
				return respMap.getResponse(Code.NOT_EXIST_DATA);
			}
			param.put("schedule_id", productSchedule.get("schedule_id"));
		}
		
		// 편성표 정보
		Map<String, Object> scheduleDetail = scheduleMapper.getDetail(param);
		
		if (CommonUtil.checkIsNull(scheduleDetail)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		// 편성표에 등록된 슬롯 리스트
		List<Map<String, Object>> slotList = scheduleMapper.getSlotList(param);
		
		// 편성표에 등록된 상품 리스트
		List<Map<String, Object>> proudctList = scheduleMapper.getProductList(param);
		
		scheduleDetail.put("slot_list", slotList);
		scheduleDetail.put("product_list", proudctList);
		
		respMap.setBody("data", scheduleDetail);
		
		return respMap.getResponse();
	}
	
	/**
	 * 편성표 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> addSchedule(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		// 편성표 분리 시 상품 아이디 값
		int separatedProductId = 0;
		
		// 새로 추가되는 분리된 상품의 키
		long newScheduleProductId = 0;
		
		// 분리된 상품과 슬롯이 맵핑되는 키
		long newScheduleProductSlotId = 0;
		
		// 슬롯 정보 리스트
		List<Map<String, Object>> slotList = (List<Map<String, Object>>) param.get("slot_list");
		
		// 상품 ID 리스트
		List<Integer> productIdList = (List<Integer>) param.get("product_id_list");
		
		// 등록되는 편성표 상품 ID 저장 리스트
		List<Long> scheduleProductIdList = new ArrayList<>();
		
		// 슬롯 유효성 체크
		if(!validateSlot(slotList)) {
			throw new RuntimeException();
		}
		
		// 편성표 분리가 있는경우
		if(!CommonUtil.checkIsNull(param, "separated_product_id")) {
			separatedProductId = (int)param.get("separated_product_id");
			
			// 기존 데이터 삭제
			initScheduleSeparation(separatedProductId, slotList);
		}
		Map<String, Object> productValidParam = new HashMap<>();
		
		List<String> registeredProductList = new ArrayList<>();
		List<String> inValidProductList = new ArrayList<>();
		
		// 이미 다른 편성표에 등록된 상품인지, 종료된 상품인지 체크
		Code code = Code.OK;
		for(int productId : productIdList) {
			productValidParam.put("product_id", productId);
		
			Map<String, Object> productValid = scheduleMapper.hasValidProduct(productValidParam);
			String Valid = (String)productValid.get("is_valid");
			String productName = (String)productValid.get("product_name");
			
			if(Valid.equals("R")) { // 이미 등록된 상품
				code = Code.ADMIN_SCHEDULE_REGISTERED_PRODUCT;
				registeredProductList.add(productName);
			} else if(Valid.equals("N")) { // 유효하지 않은 상품
				code = Code.ADMIN_SCHEDULE_INVALID_PRODUCT;
				inValidProductList.add(productName);
			}
		};
		
		if(code != Code.OK) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			
			if(code == Code.ADMIN_SCHEDULE_REGISTERED_PRODUCT) {
				respMap.setBody("registered_product_list", registeredProductList);
			} else {
				respMap.setBody("invalid_product_list", inValidProductList);
			}
			return respMap.getResponse(code);
		}
		
		// CPP, CPM 슬롯 개수 설정
		setCppCpmCnt(slotList, param);

		// 편성표 등록
		if(scheduleMapper.addSchedule(param) <= 0) {
			throw new RuntimeException();
		}
		
		// 상품 등록
		if(productIdList.size() > 0) {
			for(int productId : productIdList) {
				param.put("product_id", productId);
				
				if(scheduleMapper.addScheduleProduct(param) <= 0) {
					throw new RuntimeException();
				}
				long scheduleProductId = (long) param.get("schedule_product_id");
				scheduleProductIdList.add(scheduleProductId);
				
				// 분리된 상품이 있을 경우
				if(separatedProductId != 0 && separatedProductId == productId) {
					newScheduleProductId = scheduleProductId;
				}
			}
		}
		
		// 슬롯 등록
		for(Map<String, Object> slot : slotList) {
			slot.put("schedule_id", param.get("schedule_id"));
			
			String slotType = (String)slot.get("slot_type");
		
			if(slotType.equals("B")) {
				slot.put("sort_info", new Gson().toJson(slot.get("sort_info")));
			}
			
			if(scheduleMapper.addScheduleSlot(slot) <= 0) {
				throw new RuntimeException();
			}
			
			// CPP 슬롯이면 등록되는 상품당 슬롯을 맵핑
			if(slotType.equals("C")) {
				for(long id : scheduleProductIdList) {
					slot.put("schedule_product_id", id);
					
					if(scheduleMapper.addScheduleProductSlot(slot) <= 0) {
						throw new RuntimeException();
					}
					
					// 분리된 상품이 있을 경우 새롭게 생성되는 키를 저장
					if(separatedProductId != 0 && id == newScheduleProductId) {
						newScheduleProductSlotId = (long) slot.get("schedule_product_slot_id");
					}
				}
			}
			
			// 분리된 편성표가 있을 경우 CPP 광고 지정 정보 등록
			if(separatedProductId != 0 && !CommonUtil.checkIsNull(slot, "matching_sg_id")) {
				slot.put("schedule_product_slot_id", newScheduleProductSlotId);
				slot.put("sg_id", slot.get("matching_sg_id"));

				// 새로운 CPP 광고 지정 정보 등록
				if(scheduleMapper.addScheduleProductSlotSg(slot) <= 0) {
					throw new RuntimeException();	
				}
			}
		}

		return respMap.getResponse();
	}
	
	/**
	 * 편성표 분리 초기화
	 * 
	 * 기존에 등록된 정보 삭제   
	 * @param separatedProductId
	 * @param slotList
	 * @throws Exception
	 */
	private void initScheduleSeparation(int separatedProductId, List<Map<String, Object>> slotList) throws Exception {
		Map<String, Object> param = new HashMap<>();
		param.put("product_id", separatedProductId);

		Map<String, Object> scheduleProduct = scheduleMapper.getScheduleProduct(param);
		
		List<Map<String, Object>> scheduleProductSlotList = scheduleMapper.getScheduleProductSlot(scheduleProduct);
		
		// 분리되는 상품에 대해 CPP슬롯에 지정된 광고가 있으면 삭제
		for(Map<String, Object> scheduleProductSlot : scheduleProductSlotList) {
			if(scheduleMapper.getScheduleProductSlotSgCnt(scheduleProductSlot) > 0) {
				if(scheduleMapper.removeScheduleProductSlotSg(scheduleProductSlot) <= 0) {
					throw new RuntimeException();
				}
			}
		}
		
		// 기존에 상품과 맵핑된 CPP슬롯 삭제
		if(scheduleMapper.removeScheduleProductSlot(scheduleProduct) <= 0) {
			throw new RuntimeException();
		}

		// 분리된 상품의 기존 정보 삭제
		if(scheduleMapper.removeScheduleProduct(param) <= 0) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * 편성표 수정
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifySchedule(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 슬롯 정보 리스트
		List<Map<String, Object>> slotList = (List<Map<String, Object>>) param.get("slot_list");
		
		// 삭제되는 슬롯 ID 리스트
		List<Integer> removeSlotIdList = (List<Integer>) param.get("remove_slot_id_list");
		
		// 추가되는 상품 ID 리스트 (상품은 업데이트 항목이 없음)
		List<Integer> productIdList = (List<Integer>) param.get("product_id_list");
				
		// 삭제되는 편성표 상품 ID 리스트
		List<Integer> removeProductIdList = (List<Integer>) param.get("remove_schdule_product_id_list");
		
		// 등록된 편성표에 속한 상품 리스트
		List<Map<String, Object>> scheduleProductList = new ArrayList<>();
		
		// 슬롯 유효성 체크
		if(!validateSlot(slotList)) {
			throw new RuntimeException();
		}
		
		Map<String, Object> productValidParam = new HashMap<>();
		productValidParam.put("schedule_id", param.get("schedule_id"));

		List<String> registeredProductList = new ArrayList<>();
		List<String> inValidProductList = new ArrayList<>();
		
		// 이미 다른 편성표에 등록된 상품인지, 종료된 상품인지 체크
		Code code = Code.OK;
		for(int productId : productIdList) {
			productValidParam.put("product_id", productId);
		
			Map<String, Object> productValid = scheduleMapper.hasValidProduct(productValidParam);
			String Valid = (String)productValid.get("is_valid");
			String productName = (String)productValid.get("product_name");
			
			if(Valid.equals("R")) { // 이미 등록된 상품
				code = Code.ADMIN_SCHEDULE_REGISTERED_PRODUCT;
				registeredProductList.add(productName);
			} else if(Valid.equals("N")) { // 유효하지 않은 상품
				code = Code.ADMIN_SCHEDULE_INVALID_PRODUCT;
				inValidProductList.add(productName);
			}
		};
		
		if(code != Code.OK) {
			if(code == Code.ADMIN_SCHEDULE_REGISTERED_PRODUCT) {
				respMap.setBody("registered_product_list", registeredProductList);
			} else {
				respMap.setBody("invalid_product_list", inValidProductList);
			}
			return respMap.getResponse(code);
		}
		
		// CPP, CPM 슬롯 개수 설정
		setCppCpmCnt(slotList, param);
		
		// 편성표 수정
		if(scheduleMapper.modifySchedule(param) <= 0) {
			throw new RuntimeException();
		}
		
		// 상품 등록
		if(productIdList.size() > 0) {
			Map<String, Object> scheduleProductParam = new HashMap<>();
			scheduleProductParam.put("schedule_id", param.get("schedule_id"));
			param.put("slotType", "C");
			
			for(int productId : productIdList) {
				scheduleProductParam.put("product_id", productId);
				
				if(scheduleMapper.addScheduleProduct(scheduleProductParam) <= 0) {
					throw new RuntimeException();
				}

				// 새로운 상품이 추가가 되면 기존에 등록된 CPP 슬롯의 개수만큼 맵핑정보 등록
				for(Map<String, Object> slot : slotList) {
					String slotType = (String)slot.get("slot_type");
					
					if(!CommonUtil.checkIsNull(slot, "schedule_slot_id") && slotType.equals("C")) {
						scheduleProductParam.put("slot_id", slot.get("schedule_slot_id"));
						
						if(scheduleMapper.addScheduleProductSlot(scheduleProductParam) <= 0) {
							throw new RuntimeException();
						}
					}
				}
			}
		}
		scheduleProductList = scheduleMapper.getProductList(param);
		
		// 슬롯 등록 OR 수정
		for(Map<String, Object> slot : slotList) {
			slot.put("schedule_id", param.get("schedule_id"));
			
			String slotType = (String)slot.get("slot_type");
			
			// 블록 슬롯일 때 sort_info > Json 변환
			if(slotType.equals("B")) {
				slot.put("sort_info", new Gson().toJson(slot.get("sort_info")));
			}
			
			if(CommonUtil.checkIsNull(slot, "schedule_slot_id")) {
				// insert
				if(scheduleMapper.addScheduleSlot(slot) <= 0) {
					throw new RuntimeException();
				}

				// 새로 추가되는 CPP 슬롯은 편성표에 등록된 상품마다 맵핑정보를 등록
				if(slotType.equals("C")) {
					for(Map<String, Object> product : scheduleProductList) {
						slot.put("schedule_product_id", product.get("schedule_product_id"));
						
						if(scheduleMapper.addScheduleProductSlot(slot) <= 0) {
							throw new RuntimeException();
						}
					}
				}
			} else {
				// update
				if(scheduleMapper.modifyScheduleSlot(slot) <= 0) {
					throw new RuntimeException();
				}
			}
		};
		Map<String, Object> removeProductParam = new HashMap<>();
		
		// 상품 삭제
		removeProductIdList.forEach(productId -> {
			removeProductParam.put("schedule_product_id", productId);
			
			// 광고와 매칭된 상품이면 삭제 불가
			if(scheduleMapper.hasMatchingSg(removeProductParam) > 0) {
				throw new RuntimeException();
			}
			
			List<Map<String, Object>> scheduleProductSlotList = scheduleMapper.getScheduleProductSlot(removeProductParam);
			
			// 상품과 맵핑된 슬롯에 매칭된 광고 정보 삭제 ( 날짜 지난 광고정보 )
			for(Map<String, Object> scheduleProductSlot : scheduleProductSlotList) {
				if(scheduleMapper.getScheduleProductSlotSgCnt(scheduleProductSlot) > 0) {
					if(scheduleMapper.removeScheduleProductSlotSg(scheduleProductSlot) <= 0) {
						throw new RuntimeException();
					}
				}
			}
			
			// 전부 CPM 슬롯으로 생성하면 상품과 연결된 CPP 슬롯이 없으므로 상품과 연결된 CPP 개수 체크 후 삭제
			if(scheduleProductSlotList.size() > 0) {
				// 상품과 매칭된 슬롯 삭제
				if(scheduleMapper.removeScheduleProductSlot(removeProductParam) <= 0) {
					throw new RuntimeException();
				}
			}
			
			// 편성표 상품 삭제
			if(scheduleMapper.removeScheduleProduct(removeProductParam) <= 0) {
				throw new RuntimeException();
			}
		});
		
		// 슬롯 삭제
		removeSlotIdList.forEach(slotId -> {
			param.put("slot_id", slotId);
			
			// 진행중인 광고와 매칭된 슬롯이면 삭제 불가
			if(scheduleMapper.hasMatchingSg(param) > 0) {
				throw new RuntimeException();
			}

			List<Map<String, Object>> scheduleProductSlotList = scheduleMapper.getScheduleProductSlot(param);
			
			// 슬롯과 매칭된 광고 삭제 ( 위에 hasMatchingSg에 걸리지 않으면 날짜 지난 광고 정보여서 삭제 가능 )
			for(Map<String, Object> scheduleProductSlot : scheduleProductSlotList) {
				if(scheduleMapper.getScheduleProductSlotSgCnt(scheduleProductSlot) > 0) {
					if(scheduleMapper.removeScheduleProductSlotSg(scheduleProductSlot) <= 0) {
						throw new RuntimeException();
					}
				}
			}

			if(scheduleProductSlotList.size() > 0) {
				// 상품과 매칭된 슬롯 정보 삭제
				if(scheduleMapper.removeScheduleProductSlot(param) <= 0) {
					throw new RuntimeException();
				}
			}
			
			// 편성표 슬롯 삭제
			if(scheduleMapper.removeScheduleSlot(param) <= 0) {
				throw new RuntimeException();
			}
		});
		
		return respMap.getResponse();
	}
	
	/**
	 * 편성표 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeSchedule(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Integer> scheduleIdList = (List<Integer>) param.get("schedule_id_list");
		
		for(int scheduleId : scheduleIdList) {
			// 진행중인 CPP 광고 체크
			if(scheduleMapper.hasInProgressCpp(scheduleId) > 0) {
				return respMap.getResponse(Code.ADMIN_SCHEDULE_REMOVE_FAIL_PROGRESS_CPP);
			}
		}

		for(int scheduleId : scheduleIdList) {
			Map<String, Object> productParam = new HashMap<>();
			productParam.put("schedule_id", scheduleId);
			
			List<Map<String, Object>> productList = scheduleMapper.getProductList(productParam);

			// 편성표에 속한 상품이 있는 경우 삭제 진행
			if(productList.size() > 0) {
				// CPP 슬롯이 있으면 상품과 맵핑된 슬롯 정보 삭제
				for(Map<String, Object> product : productList) {
					if((int)product.get("cpp_slot_count") > 0) {
						List<Map<String, Object>> scheduleProductSlotList = scheduleMapper.getScheduleProductSlot(product);

						// 슬롯과 매칭된 광고 삭제 ( 위에 hasMatchingSg에 걸리지 않으면 날짜 지난 광고 정보여서 삭제 가능 )
						for(Map<String, Object> scheduleProductSlot : scheduleProductSlotList) {
							if(scheduleMapper.getScheduleProductSlotSgCnt(scheduleProductSlot) > 0) {
								if(scheduleMapper.removeScheduleProductSlotSg(scheduleProductSlot) <= 0) {
									throw new RuntimeException();
								}
							}
						}
						if(scheduleMapper.removeScheduleProductSlot(product) <= 0) {
							throw new RuntimeException();
						}
					}
				}
				// 편성표에 속한 상품 삭제 
				if(scheduleMapper.removeScheduleProduct(productParam) <= 0) {
					throw new RuntimeException();
				}
			}
		}
		
		// 편성표 슬롯 삭제
		if(scheduleMapper.removeScheduleSlot(param) <= 0) {
			throw new RuntimeException();
		}
		
		// 편성표 삭제 
		if(scheduleMapper.removeSchedule(param) <= 0) {
			throw new RuntimeException();
		}
		return respMap.getResponse();
	}

	/**
	 * 상품 CPP광고 순서 변경 페이지 상세
	 * @param param
	 * @return
	 */
	public Map<String, Object> getModifyCppOrderDetail(Map<String, Object> param) {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 상품이 등록된 스케쥴 조회 
		Map<String,Object> getProductSchedule = scheduleMapper.getProductSchedule(param);
		if (CommonUtil.checkIsNull(getProductSchedule)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		// 스케쥴 아이디 
		param.put("schedule_id", getProductSchedule.get("schedule_id"));
		
		// 슬롯에 매칭된 특정 상품과 광고정보를 알아내기 위한 파라미터
		param.put("separated_product_id", param.get("product_id"));
		
		// 상품의 정보: 매체 > 분류 > 상품
		Map<String, Object> getProduct = scheduleMapper.getProductDetail(param);
		
		// 편성표에 등록된 슬롯 리스트
		List<Map<String, Object>> slotList = scheduleMapper.getSlotList(param);
		
		getProduct.put("slot_list", slotList);
		
		respMap.setBody("data", getProduct);
		
		return respMap.getResponse();
	}
	
	/**
	 * 같은 스케쥴의 광고 중 재생시간이 같은 광고
	 * @param param
	 * @return
	 */
	public Map<String, Object> getSameScheduleSgList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// param : slot_id, schedule_id, play_time
		
		List<Map<String,Object>> sameScheduleSgList = scheduleMapper.getSameScheduleSgList(param);
		
		responseMap.setBody("list", sameScheduleSgList);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 상품 CPP 광고 지정 순서 변경
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyCppOrder(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		List<Map<String,Object>> slotList = (List<Map<String, Object>>) param.get("slot_list");
		
		Map<String, Object> addSlotSgParam = new HashMap<>();
		
		if (slotList != null && !slotList.isEmpty()) {
			for (Map<String, Object> map : slotList) {
				addSlotSgParam.put("schedule_product_slot_id", map.get("schedule_product_slot_id"));
				addSlotSgParam.put("sg_id", map.get("sg_id"));
				param.put("sg_id", map.get("sg_id"));
			
				// CPP 광고 지정 정보 제거
				if (scheduleMapper.removeScheduleProductSlotSg(param) < 1) {
					throw new RuntimeException();
				}

				// CPP 광고 지정 정보 등록
				if (scheduleMapper.addScheduleProductSlotSg(addSlotSgParam) < 1) {
					throw new RuntimeException();
				}
			}	
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * 스케쥴의 전날 다음날 광고들 설정
	 * @param param
	 * @return
	 */
	public Map<String, Object> setSchedule(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 상품이 등록된 스케쥴 조회 
		Map<String,Object> getProductSchedule = scheduleMapper.getProductSchedule(param);
		if (CommonUtil.checkIsNull(getProductSchedule)) {
			throw new RuntimeException();
		}
		
		Map<String, Object> makeParam = new HashMap<>();
		
		// 스케쥴 아이디 
		makeParam.put("schedule_product_id", getProductSchedule.get("schedule_product_id"));
		
		List<Map<String,Object>> slotList = (List<Map<String, Object>>) param.get("slot_list");
		
		if (slotList != null && !slotList.isEmpty()) {
			// CPP 광고 지정 정보 제거
//			if (scheduleMapper.removeScheduleProductSg(makeParam) < 1) {
//				throw new RuntimeException();
//			}
			
			for (Map<String, Object> map : slotList) {
				makeParam.put("slot_id", map.get("slot_id"));
				makeParam.put("sg_id", map.get("sg_id"));
				
				// CPP 광고 지정 정보 등록
//				if (scheduleMapper.addScheduleProductSg(makeParam) < 1) {
//					throw new RuntimeException();
//				}
			}	
		}
		
		return responseMap.getResponse();
	}
	
	
	/**
	 * 슬롯 유효성 검사
	 * 
	 * CPP 일경우 광고시간 체크
	 * 최소 하나의 디폴트광고가 있는지 체크
	 * @param scheduleList
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private boolean validateSlot(List<Map<String, Object>> slotList) throws Exception {
		// 최소 하나의 디폴트 광고 유무
		boolean hasDefault = false;
		
		// Block 슬롯에 최소 하나의 옵션 유무 
		boolean hasOption = false;
		
		for(Map<String, Object> slot : slotList) {
			String type = (String)slot.get("slot_type");
			
			if(type.equals("C")) {
				if(CommonUtil.checkIsNull(slot, "play_time")) {
					return false;
				}
			} else {
				// 최소 하나의 디폴트 광고가 있는지 체크
				if(CommonUtil.checkIsNull(slot, "sort_info")) {
					return false;
				} else {
					hasOption = false;
					
					List<Map<String, Object>> sortInfo = (List<Map<String, Object>>) slot.get("sort_info");
							
					for(Map<String, Object> info : sortInfo) {
						String sortType = (String)info.get("sort_type");
						String useYn = (String)info.get("use_yn");
						
						if(sortType.equals("default") && useYn.equals("Y")) {
							hasDefault = true;
						}
						
						if(useYn.equals("Y")) {
							hasOption = true;
						}
					}
				}
				
				if(!hasOption) {
					break;
				}
			}
		}
		
		return hasDefault && hasOption ? true : false;
	}
	
	/**
	 * CPP, CPM 개수 Count
	 * @param slotList
	 * @param param
	 * @throws Exception
	 */
	private void setCppCpmCnt(List<Map<String, Object>> slotList, Map<String, Object> param) throws Exception {
		int cppCnt = 0;
		int cpmCnt = 0;
		
		for(Map<String, Object> slot : slotList) {
			String type = (String)slot.get("slot_type");
			if(type.equals("C")) {
				cppCnt++;
			} else {
				cpmCnt++;
			}
		}
		param.put("cpp_slot_count", cppCnt);
		param.put("cpm_slot_count", cpmCnt);
	}
	
	/**
	 * 편성표에 등록 가능한 남은 상품 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getRemainProductList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 매체 목록
		List<Map<String, Object>> supplyList = scheduleMapper.getSupplyList(param);
		
		// 분류 맵핑
		supplyList.forEach(supply -> {
			param.put("member_id", supply.get("member_id"));
			List<Map<String, Object>> categoryList = scheduleMapper.getCategoryList(param);
			
			// 상품 맵핑
			categoryList.forEach(category -> {
				param.put("category_id", category.get("category_id"));
				List<Map<String, Object>> productList = scheduleMapper.getRemainProductList(param);
				
				category.put("product_list", productList);
			});
			
			supply.put("category_list", categoryList);
		});
		
		respMap.setBody("list", supplyList);

		return respMap.getResponse();
	}
	
	/**
	 * 편성표 상품에 속한 광고 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getScheduleProductSgList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 편성표 상품에 속한 광고 목록
		List<Map<String, Object>> productList = scheduleMapper.getScheduleProductSgList(param);
		
		respMap.setBody("list", productList);

		return respMap.getResponse();
	}	
}
