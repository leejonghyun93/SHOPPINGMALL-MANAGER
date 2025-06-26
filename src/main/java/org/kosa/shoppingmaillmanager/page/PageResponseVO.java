package org.kosa.shoppingmaillmanager.page;

import java.util.List;

import lombok.Data;

@Data
public class PageResponseVO<T>{
	private List<T> list;
	private int totalCount = 0;
	private int totalPage = 0;
	
	private int startPage = 0;
	private int endPage = 0;
	
	private int pageNo = 0;
	private int size = 10;
	
	public PageResponseVO(int pageNo, List<T> list, int totalCount, int size) {
		this.totalCount = totalCount;
		this.pageNo = pageNo;
		this.list = list;
		this.size = size;
		
		totalPage = (int)Math.ceil((double)totalCount / size);
		
		startPage = ((pageNo - 1)/10) * 10 + 1;
		
		endPage = ((pageNo - 1)/10) * 10 + 10;
		
		if(endPage > totalPage) endPage = totalPage;
	}
	
	public boolean isPrev() {
		return startPage != 1;
	}
	
	public boolean isNext() {
		return totalPage != endPage;
	}
	public int getTotalPages() {
        return (int) Math.ceil((double) totalCount / size);
    }
}
