//
//  DocumentDataUtils.swift
//  TruTracePOC
//
//  Created by Piotr Paweska on 2020-02-12.
//  Copyright © 2020 Piotr Paweska. All rights reserved.
//

import SwiftUI

enum DocumentUITypeDesignation {
    case profile
    case feed
    case my
}

class DocumentDataUtils {
    
    static var CUSTOM_TAG_PREFIX: String = "CUSTOM:"
    static var NULL_VALUE: String = "(none)"
    
    //
    //
    // Check if the document is a feed doc
    static func isFeedDoc(document : DocumentDTO, sessionData : SessionData) -> Bool {
        if document.owner != sessionData.userCredentials.username { return true }
        return false
    }
    
    //
    //
    // Check if the document is a my doc
    static func isMyDoc(document : DocumentDTO, sessionData : SessionData) -> Bool {
        if (document.owner == sessionData.userCredentials.username
            && document.type.documentDesignation == DocType.DOC_TYPE_PASSTHROUGH ) { return true }
        return false
    }
    
    //
    //
    // Check if the document is a profile doc
    static func isMyProfileDoc(document : DocumentDTO, sessionData : SessionData) -> Bool {
        if (document.owner == sessionData.userCredentials.username
            && document.type.documentDesignation == DocType.DOC_TYPE_PROFILE ) { return true }
        return false
    }
    
    //
    //
    // Get the field definitions for this doc
    static func getDynamicFieldDefinitions(document : DocumentDTO, sessionData : SessionData) -> [DynamicFieldDefinition]  {
        var definitions : [DynamicFieldDefinition] = [DynamicFieldDefinition] ()
        
        for i in 0 ..< sessionData.dynamicFieldDefinitions.count {
            if document.type.id == sessionData.dynamicFieldDefinitions[i].docTypeId {
                definitions.append(sessionData.dynamicFieldDefinitions[i])
            }
        }

        return definitions
    }
    
    
    //
    //
    // Get the ordinal of the field definition for this doc
    static func getDynamicFieldDefinitionOrdinal(definitionId : Int, document : DocumentDTO, sessionData : SessionData) -> Int {
        var ordinal: Int = 0
        var definitions : [DynamicFieldDefinition] = getDynamicFieldDefinitions(document : document, sessionData : sessionData)
        
        for i in 0 ..< definitions.count {
            if definitionId == definitions[i].id {
                ordinal = i
                return ordinal
            }
        }

        return ordinal
    }
    //
    //
    // Get the field definitions for this doc
    static func getDynamicFieldDefinition(definitionId : Int, sessionData : SessionData) -> DynamicFieldDefinition  {
        var definition : DynamicFieldDefinition = DynamicFieldDefinition ()
        
        for i in 0 ..< sessionData.dynamicFieldDefinitions.count {
            if definitionId == sessionData.dynamicFieldDefinitions[i].id {
                definition = sessionData.dynamicFieldDefinitions[i]
                return definition
            }
        }

        return definition
    }
    
    //
    //
    // Get the specific data value for the doc
    static func getDynamicFieldValue(document : DocumentDTO, dynamicFieldDefinitionId : Int) -> String  {
        let result : String = ""
        
        for i in 0 ..< document.dynamicFieldData.count {
            if dynamicFieldDefinitionId == document.dynamicFieldData[i].dynamicFieldDefinitionId {
                return document.dynamicFieldData[i].data
            }
        }
        return result
    }
    
    //
    //
    // Get the dummy data for the data field
    static func getDynamicFieldDataDummyValueByOrdinal(ordinal : Int, document : DocumentDTO, sessionData : SessionData) -> DynamicFieldData  {
        var result : DynamicFieldData = DynamicFieldData()
        let definitions: [DynamicFieldDefinition]
            = DocumentDataUtils.getDynamicFieldDefinitions(document : document, sessionData : sessionData)
        
        let definition:DynamicFieldDefinition = definitions[ordinal]
        // get the data
        result.dynamicFieldDefinitionId = definition.id
        result.fieldDisplayNameValue = definition.displayName
        result.parentResourceId = document.id
        
        return result
    }
    
    //
    //
    //
    static func getNextPageIndex(document : DocumentDTO) -> Int {
        var pageIndex : Int = 0
        
        for i in 0 ..< document.pages.count {
            if(document.pages[i].pageNumber > pageIndex){
                pageIndex = document.pages[i].pageNumber
            }
        }
        return pageIndex+1
    }
    
}
