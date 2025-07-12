package org.kosa.shoppingmaillmanager.host.order;

import java.util.List;

import org.kosa.shoppingmaillmanager.page.PageResponseVO;
import org.kosa.shoppingmaillmanager.user.User;
import org.kosa.shoppingmaillmanager.user.UserDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
	private final OrderService orderService;
	private final UserDAO userDAO;
	
	@GetMapping("/")
	public ResponseEntity<PageResponseVO<OrderListDTO>> orderList(
			@RequestParam(defaultValue = "1") int pageNo, 
    		@RequestParam(defaultValue = "10") int size, 
    		@RequestParam(required = false) String searchColumn, 
    		@RequestParam(required = false) String searchValue, 
    		@RequestParam(required = false) String startDate, 
    		@RequestParam(required = false) String endDate, 
    		@RequestParam(required = false) List<String> order_status, 
    		@RequestParam(required = false) List<String> payment_method, 
    		@RequestParam(required = false) String recipient_name, 
    		@RequestParam(required = false) String recipient_phone, 
    		@RequestParam(required = false) String order_address_detail, 
    		@RequestParam(required = false) String user_name,
    		@RequestParam(required = false) String user_phone,
    		@RequestParam(required = false) String user_email,
    		@RequestParam(required = false) String sortOption){
		
		String loginUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Integer host_id = userDAO.findByHostId(loginUserId);
		
		PageResponseVO<OrderListDTO> pageResponse = orderService.list(
				host_id, searchColumn, searchValue, pageNo, size, startDate, endDate, order_status,
				payment_method, recipient_name, recipient_phone, order_address_detail, 
				user_name, user_phone, user_email, sortOption);
        return ResponseEntity.ok(pageResponse);
	}
	
	@GetMapping("/detail")
	public ResponseEntity<OrderDetailDTO> orderDetail(@RequestParam String order_id){
		OrderDetailDTO order = orderService.getOrder(order_id);
		
		if (order == null) {
	        return ResponseEntity.notFound().build(); // 404Add commentMore actions
	    }

	    return ResponseEntity.ok(order); // 200 + JSON 바디
	}
	
	@GetMapping("/user-detail")
	public ResponseEntity<List<OrderByUserDTO>> userDetail(@RequestParam String user_id){
		List<OrderByUserDTO> order = orderService.getOrderByUser(user_id);
		
//		if (order == null) {
//			return ResponseEntity.ok(order); // 빈 객체 반환
//	    }

	    return ResponseEntity.ok(order); // 200 + JSON 바디
	}
	
	@PutMapping("/detail")
	public ResponseEntity<String> updateRecipientInfo(@RequestBody OrderDetailDTO orderDetailDTO) {
	    boolean success = orderService.updateRecipient(orderDetailDTO);

	    if (success) {
	        return ResponseEntity.ok("수정 완료"); // 200 OK
	    } else {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 실패"); // 500
	    }
	}
	
	@DeleteMapping("/{order_id}")
	public ResponseEntity<String> cancelOrder(@PathVariable("order_id") String order_id) {
	    boolean isCancelled = orderService.cancelOrder(order_id);

	    if (isCancelled) {
	        return ResponseEntity.ok("주문이 취소되었습니다."); // 200 OK
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("주문 취소 실패"); // 500
	    }
	}
	
	@DeleteMapping
	public ResponseEntity<?> cancelMultipleOrders(@RequestBody List<String> order_ids) {
	    orderService.cancelOrders(order_ids);
	    return ResponseEntity.ok("선택한 주문이 취소되었습니다.");
	}
}
