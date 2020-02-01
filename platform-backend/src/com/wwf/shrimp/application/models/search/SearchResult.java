package com.wwf.shrimp.application.models.search;

import java.util.List;

/**
 * The Search result for any given class with results of type <T>
 * 
 * @author AleaActaEst
 *
 * @param <T> - the resulting search entity type.
 */
public class SearchResult<T> {

	/**
	 * Total number of records found
	 */
	private int totalRecords=0;
	
	/**
	 * Total number of pages found.
	 * 0 if none. -1 if paging was not requested.
	 */
	
	private int totalPages = BaseSearchCriteria.NO_PAGING;
	/**
	 * The items found of the type <T>
	 * Empty of not found.
	 */
	private List<T> list;
	
	public int getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
		if(list != null){
			this.totalRecords = list.size();
		}else{
			this.totalRecords = 0;
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SearchResult [totalRecords=" + totalRecords + ", totalPages=" + totalPages + ", list=" + list + "]";
	}
}
