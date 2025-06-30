package org.kosa.shoppingmaillmanager.host.broadcast;

import java.util.List;
import java.util.UUID;

import org.kosa.shoppingmaillmanager.security.AESUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BroadCastService {

	private final BroadCastDAO broadCastDAO;
	
	@Transactional
	public BroadCastRegisterResult register(BroadCast broadCast) {
		if (broadCast.getStream_key() == null || broadCast.getStream_key().isBlank()) {
	        throw new IllegalArgumentException("방송 키 누락");
	    }
		
		String raw_key = broadCast.getStream_key();
 		String encrypted_key;
		 try {
	        	encrypted_key = AESUtil.encrypt(broadCast.getStream_key());
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	throw new RuntimeException("스트림 키 암호화 실패", e);
	        }
		 // 암호화된 stream_key 저장
		 broadCast.setStream_key(encrypted_key);

	     // DB 저장 (insert 쿼리 or JPA save)
	     broadCastDAO.insert(broadCast);
	     
	     if(broadCast.getProductList() != null) {
	    	 for(BroadCastProduct product : broadCast.getProductList()) {
		    	 product.setBroadcast_id(broadCast.getBroadcast_id());
		    	 broadCastDAO.insertProduct(product);
		     }
	     }

	     return new BroadCastRegisterResult(broadCast, raw_key);
	}

	public List<BroadCastProduct> findByKeyword(String keyword) {
		return broadCastDAO.findByKeyword(keyword); // LIKE 검색용으로 감싸기
	}

	public BroadCast findById(int broadcast_id) {
		return broadCastDAO.findById(broadcast_id);
	}
	
	public BroadCast getBroadcastDetails(int broadcast_id) {
        BroadCast broadcast = broadCastDAO.findBroadcastById(broadcast_id);
        if (broadcast == null) {
            throw new IllegalArgumentException("존재하지 않는 방송입니다.");
        }

        List<BroadCastProduct> products = broadCastDAO.findProductsByBroadcastId(broadcast_id);
        List<BroadCastViewer> viewers = broadCastDAO.findViewersByBroadcastId(broadcast_id);

        broadcast.setProductList(products);
        broadcast.setViewerList(viewers);

        return broadcast;
    }

	public void updateStatus(BroadCast broadCast) {
		broadCastDAO.updateStatus(broadCast);
	}
}
