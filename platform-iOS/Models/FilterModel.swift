//
//  FilterModel.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-22.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import Foundation
import SwiftUI

struct DocumentFilter: Codable {
    
    var documentType : DocType?
    var submitter : Recipient?
    var recipient : Recipient?
    var tag : DocumentTag?
    var dateFrom : Date
    var dateTo : Date
    var docStatus : String?
    //
    // Indeces
    
    var docTypeIndex = 0
    var userIndex = 0
    var recipientIndex = 0
    var tagIndex = 0
    var docStatusIndex = 0
    
    
    init(){
        documentType = DocType.getNullValue()
        submitter = Recipient.getNullValue()
        recipient = Recipient.getNullValue()
        tag = DocumentTag.getNullValue()
        dateFrom = Date()
        dateTo = Date()
        docStatus = nil
        //
        // indeces
        docTypeIndex = 0
        userIndex = 0
        recipientIndex = 0
        tagIndex = 0
        docStatusIndex = 0
    }

}
