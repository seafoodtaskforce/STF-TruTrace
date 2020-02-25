package com.wwf.shrimp.application.client.android.models.dto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An individual document page with associated data.
 * 
 * Data could be an image for the page as well as any 
 * additional data needed to be captured with the page.
 * 
 * @author AleaActaEst
 *
 */
public class DocumentPage extends IdentifiableEntity {

	/**
	 * This will specify if the page is synced with the backend.
	 * We assume that any loaded page is NOT synced.
	 */
	private boolean pageSynced = false;
	/**
	 * The page number of this page within the document.
	 * Must be >=0
	 */
	private int pageNumber=0;
	
	/**
	 * The actual page data. Could be for example an image.
	 * Must be non-null
	 */
	private Object pageData;
	
	/**
	 * This should be an ordered list of data items.
	 * Each item has a name (i.e. key) and value
	 * we also have an optional data type.
	 * This is basically any extra data that should be added to the page.
	 */
	private List<DataEntity> data = new ArrayList<DataEntity>();
	
	/**
	 * The actual page data but as an external URI.
	 * Could be for example an image.
	 * Can be null (if the page data is not null)
	 */
	private String pageDataURL;
	
	/**
	 * The actual page data's data type.
	 * Could be used in the future as an equivalent to meta data type. 
	 * For example could specify "File:" or "http:" etc...
	 */
	private String pageDataType;

	/**
	 * Holds the image data as a string
	 */
	private String base64ImageData;

	/**
	 * Sync ID for this page
	 */
	private String syncID;

	/**
	 * Holds fhe image as a file URL
	 */
	private File imagePage;
	

	/**
	 * @return the pageNumber
	 */
	public int getPageNumber() {
		return pageNumber;
	}
	/**
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	/**
	 * @return the pageData
	 */
	public Object getPageData() {
		return pageData;
	}
	/**
	 * @param pageData the pageData to set
	 */
	public void setPageData(Object pageData) {
		this.pageData = pageData;
	}
	/**
	 * @return the data
	 */
	public List<DataEntity> getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(List<DataEntity> data) {
		this.data = data;
	}
	/**
	 * @return the pageDataURL
	 */
	public String getPageDataURL() {
		return pageDataURL;
	}
	/**
	 * @param pageDataURL the pageDataURL to set
	 */
	public void setPageDataURL(String pageDataURL) {
		this.pageDataURL = pageDataURL;
	}
	/**
	 * @return the pageDataType
	 */
	public String getPageDataType() {
		return pageDataType;
	}
	/**
	 * @param pageDataType the pageDataType to set
	 */
	public void setPageDataType(String pageDataType) {
		this.pageDataType = pageDataType;
	}

	public String getBase64ImageData() {
		return base64ImageData;
	}

	public void setBase64ImageData(String base64ImageData) {
		this.base64ImageData = base64ImageData;
	}

	public String getSyncID() {
		return syncID;
	}

	public void setSyncID(String syncID) {
		this.syncID = syncID;
	}

	public File getImagePage() {
		return imagePage;
	}

	public void setImagePage(File imagePage) {
		this.imagePage = imagePage;
	}

	@Override
	public String toString() {
		return "DocumentPage{" +
				"pageNumber=" + pageNumber +
				", pageData=" + pageData +
				", data=" + data +
                ", imagePage=" + ((imagePage == null) ? "":imagePage.getName()) +
				", pageDataURL='" + pageDataURL + '\'' +
				", pageDataType='" + pageDataType + '\'' +
				", base64ImageData='" + ((base64ImageData == null) ? "":base64ImageData.substring(0,10)) + '\'' +
				", syncID='" + syncID + '\'' +
				'}';
	}

	public boolean isPageSynced() {
		return pageSynced;
	}

	public void setPageSynced(boolean pageSynced) {
		this.pageSynced = pageSynced;
	}
}
