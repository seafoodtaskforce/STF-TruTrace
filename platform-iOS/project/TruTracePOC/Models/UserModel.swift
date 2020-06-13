//
//  UserModel.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-31.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct UserRequestProfileData: Codable {
    var id: Int
    var contactInfo : UserContactInfo
    
    //
    // Init for contact data info
    init(id: Int,contactInfo : UserContactInfo){
        self.id = id
        self.contactInfo = contactInfo
    }
}

struct UserRequestCredentialsData: Codable {
    var password: String
    var username: String
    
    //
    // Init for contact data info
    init(username : String, password : String){
        self.username = username
        self.password = password
    }
}

struct ResourceItem: Codable {
    var key : String
    var locale : String
    var type : String?
    var subType : String?
    var value : String
    var id : Int = 0
    var deleted : Bool = false
    
    private enum CodingKeys: String, CodingKey{

        case key
        case locale
        case type
        case subType
        case value
        case id
        case deleted
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            key = (try container.decodeIfPresent(String.self, forKey: .key)) ?? ""
            locale = (try container.decodeIfPresent(String.self, forKey: .locale)) ?? "en"
            type = (try container.decodeIfPresent(String.self, forKey: .type)) ?? ""
            value = (try container.decodeIfPresent(String.self, forKey: .value)) ?? ""
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }

}

struct UserGroup: Codable {
    static var NULL_VALUE: String = "(none)"
    
    var parentId: Int = 0
    var organizationId : Int = 0
    var name: String = "n/a"
    var allowedDocTypes : [DocType]
    var id: Int = 0
    var deleted : Bool = false
    
    private enum CodingKeys: String, CodingKey{

        case parentId
        case organizationId
        case name
        case allowedDocTypes
        case id
        case deleted
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            parentId = (try container.decodeIfPresent(Int.self, forKey: .parentId)) ?? 0
            organizationId = (try container.decodeIfPresent(Int.self, forKey: .organizationId)) ?? 0
            name = (try container.decodeIfPresent(String.self, forKey: .name)) ?? "n/a"
            allowedDocTypes = (try container.decodeIfPresent([DocType].self, forKey: .allowedDocTypes)) ?? [DocType]()
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }
    
    init() {
        parentId = 0
        organizationId = 0
        name = "n/a"
        allowedDocTypes = [DocType]()
        id = 0
    }
}

struct SimpleUser: Codable, Identifiable, Hashable {
    
    var id : Int = 0
    var name: String = "n/a"
    var deleted : Bool = false
    
    static func == (lhs: SimpleUser, rhs: SimpleUser) -> Bool {
        return lhs.id == rhs.id && lhs.name == rhs.name
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id.hashValue)
    }
    
    private enum CodingKeys: String, CodingKey{
         case id
         case name
         case deleted
     }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            name = (try container.decodeIfPresent(String.self, forKey: .name)) ?? ""
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }
    
    init() {
        id = 0
        name = "n/a"
        deleted = false
    }
    
}

struct Recipient: Codable, Identifiable, Hashable {
    
    static var NULL_VALUE: String = "(none)"
    
    static func == (lhs: Recipient, rhs: Recipient) -> Bool {
        return lhs.id == rhs.id && lhs.name == rhs.name
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id.hashValue)
    }
    
    var id : Int = 0
    var name: String = "n/a"
    var userGroups : [UserGroup]
    var deleted : Bool = false
    var isNew : Bool = false
    
    private enum CodingKeys: String, CodingKey{
         case id
         case name
         case userGroups
         case isNew
         case deleted
     }
     
     init(from decoder: Decoder) throws {
         let container = try decoder.container(keyedBy: CodingKeys.self)
             id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
             name = (try container.decodeIfPresent(String.self, forKey: .name)) ?? ""
             userGroups = (try container.decodeIfPresent([UserGroup].self, forKey: .userGroups)) ?? [UserGroup]()
             deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
             isNew = (try container.decodeIfPresent(Bool.self, forKey: .isNew)) ?? false
     }
    
    init() {
        id = 0
        name = "n/a"
        userGroups = [UserGroup]()
        deleted = false
        self.isNew = false
    }
    
    static func getNullValue() -> Recipient {
        var nilValue = Recipient()
        nilValue.name = Recipient.NULL_VALUE
        return nilValue
    }
    
}

struct AuthRequestData: Codable {
    var username : String
    var password: String
    var requestOrigin: String
}

struct AuthToken: Codable {
    var tokenValue : String
    var expirationDate : String
    var invalidated: Bool
    
    init() {
        tokenValue = ""
        expirationDate = ""
        invalidated = true
    }
}

struct UserContactInfo: Codable {
    var emailAddress: String
    var cellNumber: String
    var firstName: String
    var lastName: String
    var id: Int
    var lineId: String
    var nickName : String
    var deleted: Bool
    
    private enum CodingKeys: String, CodingKey{
        case emailAddress
        case cellNumber
        case firstName
        case lastName
        case id
        case lineId
        case nickName
        case deleted
    }
    
    init(){
        emailAddress = "n/a"
        cellNumber = "n/a"
        firstName = "n/a"
        lastName = "n/a"
        lineId = "n/a"
        nickName = "n/a"
        id = 0
        deleted = false
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            emailAddress = (try container.decodeIfPresent(String.self, forKey: .emailAddress)) ?? "n/a"
            cellNumber = (try container.decodeIfPresent(String.self, forKey: .cellNumber)) ?? "n/a"
            firstName = (try container.decodeIfPresent(String.self, forKey: .firstName)) ?? "n/a"
            lastName = (try container.decodeIfPresent(String.self, forKey: .lastName)) ?? "n/a"
            lineId = (try container.decodeIfPresent(String.self, forKey: .lineId)) ?? "n/a"
            nickName = (try container.decodeIfPresent(String.self, forKey: .nickName)) ?? "n/a"
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }
}

struct AuthCredentials: Codable {
    var username : String
    var token : AuthToken?
    var requestOrigin: String
    
    init() {
        username = "n/a"
        token = nil
        requestOrigin = "n/a"
    }
}


//
//  Authorization Response
//
//
struct AuthResponse: Codable {
    var credentials: AuthCredentials
    var contactInfo : UserContactInfo
    var appResources: [ResourceItem]
    var dynamicFieldDefinitions: [DynamicFieldDefinition]
    var userGroups : [UserGroup]
    var name : String = ""
    var id: Int = 0
    var deleted : Bool = false
    
    private enum CodingKeys: String, CodingKey{
        case credentials
        case contactInfo
        case appResources
        case dynamicFieldDefinitions
        case userGroups
        case id
        case name
        case deleted
    }
    
    init() {
        credentials = AuthCredentials()
        contactInfo = UserContactInfo()
        appResources = [ResourceItem]()
        dynamicFieldDefinitions = [DynamicFieldDefinition]()
        userGroups = [UserGroup]()
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
            credentials = (try container.decodeIfPresent(AuthCredentials.self, forKey: .credentials)) ?? AuthCredentials()
            contactInfo = (try container.decodeIfPresent(UserContactInfo.self, forKey: .contactInfo)) ?? UserContactInfo()
            appResources = (try container.decodeIfPresent([ResourceItem].self, forKey: .appResources)) ?? [ResourceItem]()
            dynamicFieldDefinitions = (try container.decodeIfPresent([DynamicFieldDefinition].self, forKey: .dynamicFieldDefinitions)) ?? [DynamicFieldDefinition]()
            userGroups = (try container.decodeIfPresent([UserGroup].self, forKey: .userGroups)) ?? [UserGroup]()
            name = (try container.decodeIfPresent(String.self, forKey: .name)) ?? "n/a"
            id = (try container.decodeIfPresent(Int.self, forKey: .id)) ?? 0
            deleted = (try container.decodeIfPresent(Bool.self, forKey: .deleted)) ?? false
    }
}
