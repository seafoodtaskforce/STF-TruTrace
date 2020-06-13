//
//  SessionData.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-31.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import Foundation
import Combine
import SwiftUI

class SessionData: ObservableObject {

    // global data used througout
    @Published var appResources: [ResourceItem] = [ResourceItem]()
    @Published var userCredentials: AuthCredentials = AuthCredentials()
    @Published var contactInfo : UserContactInfo = UserContactInfo()
    @Published var userGroups : [UserGroup] = [UserGroup]()
    @Published var documentList : [DocumentDTO] = [DocumentDTO]()
    @Published var userProfileImage : UIImage = UIImage()
    @Published var dynamicFieldDefinitions: [DynamicFieldDefinition] = [DynamicFieldDefinition]()
    
    // doc breakdown
    @Published var profileDocumentList : [DocumentDTO] = [DocumentDTO]()
    @Published var myDocumentList : [DocumentDTO] = [DocumentDTO]()
    @Published var feedDocumentList : [DocumentDTO] = [DocumentDTO]()
    @Published var profileDocTypes : [DocType] = [DocType]()
    @Published var passthroughDocTypes : [DocType] = [DocType]()
    // Current Data
    @Published var allRecipients : [Recipient] = [Recipient]()
    @Published var allOrganizationUsers : [SimpleUser] = [SimpleUser]()
    @Published var allTags : [DocumentTag] = [DocumentTag]()
    @Published var allLinkable : [DocumentDTO] = [DocumentDTO]()
    @Published var allAttachable : [DocumentDTO] = [DocumentDTO]()
    // Work Data
    @Published var newWorkDocument : DocumentDTO = DocumentDTO()
    @Published var newWorkDocumentFlag : Bool = false;
    @Published var updateWorkDocumentFlag : Bool = false;
    @Published var inActiveUpdateMode : Bool = false;
    // User Id
    @Published var userId : Int = 0
    // Filters
    @Published var myDocsFilter : DocumentFilter = DocumentFilter()
    @Published var feedDocsFilter : DocumentFilter = DocumentFilter()
    @Published var profileDocsFilter : DocumentFilter = DocumentFilter()
    // Offfline
    @Published var isOnlineFlag : Bool = true
    // server data
    @Published var serverURL : String = ""
    
    
}
