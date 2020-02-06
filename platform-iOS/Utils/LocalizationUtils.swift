//
//  LocalizationUtils.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-30.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import Foundation

class LocalizationUtils {

    static func localizeString(text: String) -> String {
        let formatString = NSLocalizedString(text, comment: "N/A")
        return formatString
    }
    
    /*
     Localize the string dynmically from the resource dictionary
     */
    static func localizeDynamicString(text: String, session : SessionData) -> String {
        let formatString = NSLocalizedString(text, comment: "N/A")
        return formatString
    }
    
    /*
     Localize the specoific STATUS string for the docuemnt
     */
    static func localizeDocumentStatus(text: String) -> String {
        var formatString: String = "N/A"
        if(text == DocumentDTO.DOC_STATUS_DRAFT){
            formatString = NSLocalizedString("ios_general_document_status_DRAFT" , comment: "N/A")
        }
        if(text == DocumentDTO.DOC_STATUS_SUBMITTED){
            formatString = NSLocalizedString("ios_general_document_status_SUBMITTED" , comment: "N/A")
        }
        if(text == DocumentDTO.DOC_STATUS_PENDING){
            formatString = NSLocalizedString("ios_general_document_status_PENDING" , comment: "N/A")
        }
        if(text == DocumentDTO.DOC_STATUS_ACCEPTED){
            formatString = NSLocalizedString("ios_general_document_status_ACCEPTED" , comment: "N/A")
        }
        if(text == DocumentDTO.DOC_STATUS_REJECTED){
            formatString = NSLocalizedString("ios_general_document_status_REJECTED" , comment: "N/A")
        }
        if(text == DocumentDTO.DOC_STATUS_RESUBMITTED){
            formatString = NSLocalizedString("ios_general_document_status_RESUBMITTED" , comment: "N/A")
        }

        return formatString
    }

}
