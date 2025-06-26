package org.kosa.shoppingmaillmanager.host.broadcast;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BroadCastService {

	private final BroadCastDAO broadCastDAO;
	
	@Transactional
	public BroadCast register(BroadCast broadCast) {
		 String stream_key = UUID.randomUUID().toString();
	     broadCast.setStream_key(stream_key); // 방송 키 추가

	     // DB 저장 (insert 쿼리 or JPA save)
	     broadCastDAO.insert(broadCast);
	     
	     if(broadCast.getProductList() != null) {
	    	 for(BroadCastProduct product : broadCast.getProductList()) {
		    	 product.setBroadcast_id(broadCast.getBroadcast_id());
		    	 broadCastDAO.insertProduct(product);
		     }
	     }

	     return broadCast;
	}

}
