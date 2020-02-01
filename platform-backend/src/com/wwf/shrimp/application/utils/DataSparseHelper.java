package com.wwf.shrimp.application.utils;

import com.wwf.shrimp.application.models.Document;

/**
 * Utility for sparsifying data on different types of objects. 
 * This would be used to make objects more light-weight for transport
 * @author user argolite
 *
 */
public class DataSparseHelper {
	
	/**
	 * Sparsify the Document entity
	 * @param doc - the document to lighten-up.
	 * @return  - version of the objects but with less data
	 */
	public static Document sparsify(Document doc){
		
		doc.setAttachedDocuments(null);
		doc.setBase64ImageData(null);
		doc.setData(null);
		doc.setDocumentImageURI(null);
		doc.setLinkedDocuments(null);
		doc.setMetadata(null);
		doc.setPages(null);
		doc.setTags(null);
		
		return doc;
	}

}
