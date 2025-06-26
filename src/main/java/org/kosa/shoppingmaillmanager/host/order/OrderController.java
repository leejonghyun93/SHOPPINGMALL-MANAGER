package org.kosa.shoppingmaillmanager.host.order;

import org.kosa.shoppingmaillmanager.page.PageResponseVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	
	@GetMapping("/")
	public ResponseEntity<PageResponseVO<OrderListDTO>> orderList(
			@RequestParam(defaultValue = "1") int pageNo, 
    		@RequestParam(defaultValue = "10") int size, 
    		@RequestParam(required = false) String searchValue){
		
		PageResponseVO<OrderListDTO> pageResponse = orderService.list(searchValue, pageNo, size);
        return ResponseEntity.ok(pageResponse);
	}
	
	@GetMapping("/detail")
	public ResponseEntity<OrderDetailDTO> orderDetail(@RequestParam String order_id){
		OrderDetailDTO order = orderService.getOrder(order_id);
		
		if (order == null) {
	        return ResponseEntity.notFound().build(); // 404
	    }

	    return ResponseEntity.ok(order); // 200 + JSON 바디
	}
	
}
