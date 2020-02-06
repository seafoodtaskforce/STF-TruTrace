//
//  DocumentModel.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-03.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import Foundation
import SwiftUI

//
//  Document Page
//  With JSON initialization
//
struct DocumentPage: Codable, Identifiable {
    var id : Int = -1
    var pageNumber : Int = 0
    var deleted : Bool = false
    var localImage : UIImage?
    var base64ImageData : String?
    var sessionID : String
    var isNew : Bool = true
    var tempId : UUID = UUID()
    
    private enum CodingKeys: String, CodingKey{
        case id
        case pageNumber
        case deleted
        case base64ImageData
        case sessionID
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            pageNumber = (try container.decodeIfPresent(Int.self, forKey: .pageNumber)) ?? -1
            base64ImageData = (try container.decodeIfPresent(String.self, forKey: .base64ImageData)) ?? nil
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
            sessionID = (try container.decodeIfPresent(String.self, forKey: .sessionID)) ?? ""
        
            //
            tempId = UUID()
    }
    
    init(id : Int, pageNumber : Int ,deleted : Bool){
        self.id = id
        self.pageNumber = pageNumber
        self.deleted = deleted
        self.base64ImageData = nil
        self.localImage = nil
        self.sessionID = ""
        self.isNew = true
        self.tempId = UUID()
    }
    
    init(localImage : UIImage, pageNumber : Int){
        self.id = -1
        self.pageNumber = pageNumber
        self.deleted = false
        self.localImage = localImage
        self.base64ImageData = nil
        self.sessionID = ""
        self.isNew = true
        self.tempId = UUID()
    }
    
    func getPageNumber() -> String {
        let index = self.pageNumber + 1
        return String(index)
    }
    
    func getFormEncodedPageImageData() -> Data {
        let resizedImage = self.localImage!.resized(toWidth: 460.0)
        return (resizedImage?.jpegData(compressionQuality: 0.5))!
    }
    
    func getBase64EncodedPage() -> DocumentPage {
        var page : DocumentPage = DocumentPage(id: self.id, pageNumber : self.pageNumber, deleted : false)
        // resize
        let resizedImage = self.localImage!.resized(toWidth: 460.0)
        //page.base64ImageData = resizedImage?.jpegData(compressionQuality: 0.5)?.base64EncodedString(options: .lineLength64Characters) ?? ""
        page.base64ImageData = resizedImage?.jpegData(compressionQuality: 0.5)?.base64EncodedString() ?? ""
        
        page.base64ImageData = page.base64ImageData!.replacingOccurrences(of: "\\/", with: "/")
        page.localImage = nil
        print("BASE 64 Encoding image size: \(page.base64ImageData?.count ?? -1)")
        return page
    }
    
    
}

//
//  Document Tag
//  With JSON initialization
//
struct DocumentTag: Codable,Identifiable, Equatable {
    static var CUSTOM_TAG_PREFIX: String = "CUSTOM:"
    static var NULL_VALUE: String = "(none)"
    
    var id : Int = 0
    var text : String = ""
    var organizationId : Int = 0
    var custom : Bool = false
    var customPrefix : String = ""
    var deleted : Bool = false
    var isNew :Bool = true
    
    private enum CodingKeys: String, CodingKey{
        case id
        case text
        case organizationId
        case custom
        case customPrefix
        case deleted
        case isNew
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            text = (try container.decodeIfPresent(String.self, forKey: .text)) ?? ""
            organizationId = (try container.decodeIfPresent(Int.self, forKey: .organizationId)) ?? 0
            custom = (try container.decodeIfPresent(Bool.self, forKey: .custom)) ?? false
            customPrefix = (try container.decodeIfPresent(String.self, forKey: .customPrefix)) ?? ""
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
            isNew = (try container.decodeIfPresent(Bool.self, forKey: .isNew)) ?? false
    }
    
    init(){
         id = 0
         text = ""
         organizationId = 0
         custom = false
         customPrefix = ""
         deleted = false
         isNew = true
    }
    
    init(text:String, organizationId:Int){
         self.id = 0
         self.text = text
         self.organizationId = organizationId
         self.custom = false
         self.customPrefix = ""
         self.deleted = false
         self.isNew = true
    }
    
    static func ==(lhs: DocumentTag, rhs: DocumentTag) -> Bool {
        return (lhs.id == rhs.id || lhs.text ==  rhs.text)
    }
    
    static func getNullValue() -> DocumentTag {
        var nilValue = DocumentTag()
        nilValue.text = DocumentTag.NULL_VALUE
        return nilValue
    }
}

//
//  Document Type
//  With JSON initialization
//
struct DocType: Codable, Identifiable, Equatable{
    
    static var DOC_TYPE_PASSTHROUGH : String = "Passthrough"
    static var DOC_TYPE_PROFILE: String = "Profile"
    static var NULL_VALUE: String = "(none)"
    
    var id : Int = 0
    var hexColorCode : String = "#ff0040ff"
    var documentDesignation : String = ""
    var name : String = ""
    var value : String = ""
    var resource : Bool = false
    var deleted : Bool = false
    
    private enum CodingKeys: String, CodingKey{
        case id
        case hexColorCode
        case documentDesignation
        case name
        case value
        case resource
        case deleted
    }
    
    init(){
        id = 0
        hexColorCode = "#ff0040ff"
        documentDesignation = DocType.DOC_TYPE_PASSTHROUGH
        name = ""
        value = ""
        resource = false
        deleted = false
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            hexColorCode = (try container.decodeIfPresent(String.self, forKey: .hexColorCode)) ?? ""
            documentDesignation = (try container.decodeIfPresent(String.self, forKey: .documentDesignation)) ?? ""
            name = (try container.decodeIfPresent(String.self, forKey: .name)) ?? ""
            value = (try container.decodeIfPresent(String.self, forKey: .value)) ?? ""
            resource = (try container.decodeIfPresent(Bool.self, forKey: .resource)) ?? false
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }
    
    static func ==(lhs: DocType, rhs: DocType) -> Bool {
        return lhs.id == rhs.id
    }
    
    static func getNullValue() -> DocType {
        var nilValue = DocType()
        nilValue.name = DocType.NULL_VALUE
        return nilValue
    }
}

//
//  Document Note
//  With JSON initialization
//
struct DocumentNote: Codable, Identifiable {
    var id : Int = 0
    var note : String = ""
    var owner : String = ""
    var deleted : Bool = false
    
    private enum CodingKeys: String, CodingKey{
        case id
        case note
        case owner
        case deleted
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            note = (try container.decodeIfPresent(String.self, forKey: .note)) ?? ""
            owner = (try container.decodeIfPresent(String.self, forKey: .owner)) ?? ""
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }
    
    init() {
        id = 0
        note = ""
        owner = ""
        deleted = false
    }
    
    init(id : Int, note: String, owner : String, deleted: Bool) {
        self.id = id
        self.note = note
        self.owner = owner
        self.deleted = deleted
    }
    
    func getHeader() -> String {
        if(!self.note.isEmpty) {
        let array = self.note.components(separatedBy: ";;;;")
            return array[0]
        }
        return ""
    }
    
    func getBody() -> String {
        if(!self.note.isEmpty) {
            let array = self.note.components(separatedBy: ";;;;")
            return array[1]
        }
        return ""
    }
    
    func getSample() -> DocumentNote {
        return DocumentNote(id : 0, note: "Hello There", owner : "owner", deleted: false)
    }
}

//
//   Document Model
//
//
struct DocumentDTO: Codable, Identifiable, Equatable {
    static var DOC_STATUS_DRAFT: String = "DRAFT"
    static var DOC_STATUS_SUBMITTED : String = "SUBMITTED"
    static var DOC_STATUS_PENDING : String = "PENDING"
    static var DOC_STATUS_REJECTED : String = "REJECTED"
    static var DOC_STATUS_ACCEPTED : String = "ACCEPTED"
    static var DOC_STATUS_RESUBMITTED : String = "RESUBMITTED"
    
    var id : Int = 0
    var pages: [DocumentPage] = [DocumentPage]()
    var tags : [DocumentTag] = [DocumentTag]()
    var type : DocType = DocType()
    var documentType : String = ""
    var owner : String = ""
    var creationTimestamp : String  = ""
    var creationTimestampDate : Date  = Date()
    var TypeHEXColor : String = "#ff0040ff"
    var syncID : String = ""
    var currentUserRead : Bool = false
    var linkedDocuments : [DocumentDTO] = [DocumentDTO]()
    var attachedDocuments : [DocumentDTO] = [DocumentDTO]()
    var groupId : Int = 0
    var organizationId : Int = 0
    var groupName : String = ""
    var toRecipients : [Recipient] = [Recipient]()
    var notes : [DocumentNote] = [DocumentNote]()
    var status : String = ""
    var deleted : Bool = false
    var isNew : Bool = true
    
    private enum CodingKeys: String, CodingKey{
        case id
        case documentType
        case owner
        case creationTimestamp
        case TypeHEXColor
        case syncID
        case currentUserRead
        case groupId
        case organizationId
        case groupName
        case status

        case pages
        case tags
        case type
        case linkedDocuments
        case attachedDocuments
        case toRecipients
        case notes
        case deleted
        case isNew
    }
    
    init(){
        id = 0
        documentType = ""
        owner = ""
        creationTimestamp = ""
        creationTimestampDate = Date()
        TypeHEXColor = "#ff0040ff"
        syncID = UUID().uuidString
        currentUserRead = false
        groupId = 0
        organizationId = 0
        groupName = ""
        status = "DRAFT"
        
        pages = [DocumentPage]()
        tags = [DocumentTag]()
        type = DocType()
        linkedDocuments = [DocumentDTO]()
        attachedDocuments = [DocumentDTO]()
        toRecipients = [Recipient]()
        notes = [DocumentNote]()
        deleted = false
        isNew = true
    }
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            documentType = (try container.decodeIfPresent(String.self, forKey: .documentType)) ?? ""
            owner = (try container.decodeIfPresent(String.self, forKey: .owner)) ?? ""
            creationTimestamp = (try container.decodeIfPresent(String.self, forKey: .creationTimestamp)) ?? ""
            TypeHEXColor = (try container.decodeIfPresent(String.self, forKey: .TypeHEXColor)) ?? ""
            syncID = (try container.decodeIfPresent(String.self, forKey: .syncID)) ?? ""
            currentUserRead = (try container.decodeIfPresent(Bool.self, forKey: .currentUserRead)) ?? false
            groupId = (try container.decodeIfPresent(Int.self, forKey: .groupId)) ?? 0
            organizationId = (try container.decodeIfPresent(Int.self, forKey: .organizationId)) ?? 0
            groupName = (try container.decodeIfPresent(String.self, forKey: .groupName)) ?? ""
            status = (try container.decodeIfPresent(String.self, forKey: .status)) ?? ""
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
            isNew = (try container.decodeIfPresent(Bool.self, forKey: .isNew)) ?? false
        
            pages = (try container.decodeIfPresent([DocumentPage].self, forKey: .pages)) ?? [DocumentPage]()
            tags = (try container.decodeIfPresent([DocumentTag].self, forKey: .tags)) ?? [DocumentTag]()
            toRecipients = (try container.decodeIfPresent([Recipient].self, forKey: .toRecipients)) ?? [Recipient]()
            type = (try container.decodeIfPresent(DocType.self, forKey: .type)) ?? DocType()
            notes = (try container.decodeIfPresent([DocumentNote].self, forKey: .notes)) ?? [DocumentNote]()
        
            linkedDocuments = (try container.decodeIfPresent([DocumentDTO].self, forKey: .linkedDocuments)) ?? [DocumentDTO]()
            attachedDocuments = (try container.decodeIfPresent([DocumentDTO].self, forKey: .attachedDocuments)) ?? [DocumentDTO]()
        //
        // Other iitialization
        creationTimestampDate = DateUtils.formatStringToDate(date: creationTimestamp)
        
    }
    
    //
    //
    // Encode
    func encodeToSend() -> DocumentDTO {
        var encodedDoc: DocumentDTO = self
        var pages : [DocumentPage] = [DocumentPage]()
        
        // encode the pages
        for item in self.pages {
            if(item.id <= 0){
                pages.append(item.getBase64EncodedPage())
                print("BASE 64 Encoded: ")
                print(item.getBase64EncodedPage())
            }
        }
        
        // add the pages
        encodedDoc.pages = pages
        
        return encodedDoc
    }
    
    static func ==(lhs: DocumentDTO, rhs: DocumentDTO) -> Bool {
        return (lhs.syncID == rhs.syncID || lhs.id == rhs.id)
    }
}


//
// Notification Data
//
//
struct NotificationDTO: Codable, Identifiable, Equatable {
    var id : Int = 0
    var creationTimestamp : String
    var auditData : AuditDTO
    var deleted : Bool = false
    
    private enum CodingKeys: String, CodingKey{
        case id
        case creationTimestamp
        case auditData
        case deleted
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            creationTimestamp = (try container.decodeIfPresent(String.self, forKey: .creationTimestamp)) ?? ""
            auditData = (try container.decodeIfPresent(AuditDTO.self, forKey: .auditData)) ?? AuditDTO()
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }
    
}

//
// Aidot Data with additional information for Notifications
//
//
struct AuditDTO: Codable, Identifiable, Equatable {
    var id : Int = 0
    var actor : SimpleUser
    var itemId: String
    var action: String
    var deleted : Bool = false
    
    private enum CodingKeys: String, CodingKey{
        case id
        case actor
        case itemId
        case action
        case deleted
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            actor = (try container.decodeIfPresent(SimpleUser.self, forKey: .actor)) ?? SimpleUser()
            itemId = (try container.decodeIfPresent(String.self, forKey: .itemId)) ?? ""
            action = (try container.decodeIfPresent(String.self, forKey: .action)) ?? ""
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }

    init(){
        id = 0
        actor = SimpleUser()
        itemId = ""
        action = ""
        deleted = false
    }
}





