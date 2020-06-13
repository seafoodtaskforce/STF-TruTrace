package com.wwf.shrimp.application.client.android.models.dto.search;

/**
 * The base class for paged search parameters.
 * If page number is <=0 then the search is NOT paged.
 * If page size is <=0 then the search is not paged.
 *
 * @author AleaActaEst
 *
 */
public class BaseSearchCriteria {

    public static int NO_PAGING = -1;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "BaseSearchCriteria [pageSize=" + pageSize + ", pageNumber=" + pageNumber + ", sortBy=" + sortBy
                + ", sortType=" + sortType + "]";
    }
    private int pageSize=0;
    private int pageNumber=0;
    private String sortBy=null;
    private SortType sortType=null;
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public int getPageNumber() {
        return pageNumber;
    }
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    public String getSortBy() {
        return sortBy;
    }
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    public SortType getSortType() {
        return sortType;
    }
    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

}
