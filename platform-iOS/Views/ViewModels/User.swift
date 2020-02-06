//
//  User.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-02.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import Foundation

class User : Identifiable, ObservableObject {

    @Published var id = 0
    @Published var name : String
    @Published var organization: String
    @Published var profilePicture: String
    @Published var emailAddress : String
    @Published var lineID : String
    @Published var nickname : String
    @Published var notifications: Bool
    @Published var lastLogin: String?=nil
    
    init(id: Int, name: String, organization: String, profilePicture: String, emailAddress: String, lineID: String, nickname: String, notifications: Bool){
        self.id = id
        self.name = name
        self.organization = organization
        self.profilePicture = profilePicture
        self.emailAddress = emailAddress
        self.lineID = lineID
        self.nickname = nickname
        self.notifications = notifications
    }
    
    init(id: Int, name: String, organization: String){
        self.id = id
        self.name = name
        self.organization = organization
        self.profilePicture = ""
        self.emailAddress = ""
        self.lineID = ""
        self.nickname = ""
        self.notifications = false
    }

}

extension User {
    
    static func all() -> [User] {
        return [
            User(id: 0, name: "ppaweska", organization: "farm-1", profilePicture: "profile1", emailAddress: "ppaweska@hotmail.com", lineID: "paw", nickname: "iceman", notifications: true),
            User(id: 1, name: "user1", organization: "farm-2", profilePicture: "profile2", emailAddress: "user1@hotmail.com", lineID: "user1", nickname: "johnny", notifications: true),
            User(id: 2, name: "user2", organization: "farm-3", profilePicture: "profile3", emailAddress: "user2@hotmail.com", lineID: "user2", nickname: "brat", notifications: false),
            User(id: 3, name: "user3", organization: "farm-4", profilePicture: "profile4", emailAddress: "user3@hotmail.com", lineID: "user3", nickname: "brad", notifications: false),
            User(id: 4, name: "user4", organization: "farm-5", profilePicture: "profile5", emailAddress: "user4@hotmail.com", lineID: "user4", nickname: "mono", notifications: false)
            
        ]
    }
    
    static var example: User = User.all()[0]
}

