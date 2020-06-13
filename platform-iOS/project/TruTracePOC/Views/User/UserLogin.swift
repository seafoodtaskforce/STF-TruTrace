//
//  UserLogin.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-11-25.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct UserLogin: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    @State var result = AuthResponse()
    @State private var isNotAuthenticated : Bool = false
    
    @State var username: String = ""
    @State var password: String = ""
    
    init(){
        if self.result.credentials.token != nil { self.isNotAuthenticated = false}
        else { self.isNotAuthenticated = true}
    }
    
    var myProfileDocsFilter : [DocumentDTO] {
        return sessionData.documentList.filter { $0.type.documentDesignation  == "Profile"
            && $0.owner == sessionData.userCredentials.username
        }
    }
    
    var myPassthroughDocsFilter : [DocumentDTO] {
        return sessionData.documentList.filter { $0.type.documentDesignation  == "Passthrough"
            && $0.owner == sessionData.userCredentials.username
        }
    }
    
    var feedDocsFilter : [DocumentDTO] {
        return sessionData.documentList.filter { $0.owner != sessionData.userCredentials.username}
    }
    
    var body: some View {
        VStack {
            // Company Logo
            CompanyLogoView()
            
            // User name
            TextField(LocalizationUtils.localizeString(text: "ios_login_page_hint_text_username")
                    , text: $username)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .background(
                    Color(UIColor.systemGray).opacity(0.95))
                        .padding()
                .autocapitalization(.none)

            // Password
            SecureField(LocalizationUtils.localizeString(text: "ios_login_page_hint_text_password"), text: $password)
            .textFieldStyle(RoundedBorderTextFieldStyle())
            .background(
                Color(UIColor.systemBackground).opacity(0.95))
                .padding()
            
            
            // Login button
            Button(action: {
                // authentiate the user in the backend
                self.loadAuthData()
                
            }) {
                Text(LocalizationUtils.localizeString(text: "ios_login_page_authentication_button_LOGIN"))
                .font(.headline)
                .foregroundColor(.white)
                .padding()
                .frame(width: 220, height: 60)
                .background(Color(UIColor.systemGray))
                .cornerRadius(15.0)
            }
            .onAppear(){
                let server : RESTServer = Bundle.main.decode(RESTServer.self, from: "server.json")
                self.sessionData.serverURL = server.accessUrl
                print("Loading Server URL <server> URL: \(self.sessionData.serverURL)")
            }
            .disabled(!canLogin())
            .alert(isPresented: $isNotAuthenticated) {
                Alert(
                    title: Text(LocalizationUtils.localizeString(text: "ios_login_page_error_authentication_header")),
                    message: Text(LocalizationUtils.localizeString(text: "ios_login_page_error_authentication_message")),
                    dismissButton: .default(Text(LocalizationUtils.localizeString(text: "ios_login_page_error_authentication_button_OK"))))
            }
        }
        .padding()
    }
    
    func canLogin() -> Bool{
        if(!self.username.isEmpty && !self.password.isEmpty) { return true }
        else { return false }
    }
    
    func loadAuthData() {
        var jsonBody : Data?
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_USER_POST_AUTHENTICATE))
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let authData = AuthRequestData(username: self.username, password: self.password, requestOrigin: "iOS Device")
        let jsonEncoder = JSONEncoder()
        if let encoded = try? jsonEncoder.encode(authData) {
            jsonBody = encoded
        }

        
        //let finalBody = try! JSONSerialization.data(withJSONObject: authData)
        if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
            print(json)
        }
        
        request.httpBody = jsonBody
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            //print(data)
            //print(response)
            //print(error)
            guard let data = data else { return }
                //
                // get the data for the logged in user
            if let dataString = String(data: data, encoding: .utf8){
                print(dataString)
            }
            let decodedResponse = try! JSONDecoder().decode(AuthResponse.self, from: data)
            //
            // Check the login
            self.result = decodedResponse
                
                DispatchQueue.main.async {
                        print("decoded response \(decodedResponse)")
                        
                        if (self.result.credentials.token != nil) {
                            self.viewRouter.currentPage = ViewRouter.HOME_PAGE
                            self.isNotAuthenticated = false
                            self.sessionData.appResources = self.result.appResources
                            self.sessionData.contactInfo = self.result.contactInfo
                            self.sessionData.userCredentials = self.result.credentials
                            self.sessionData.userGroups = self.result.userGroups
                            self.sessionData.userId = self.result.id
                            self.sessionData.dynamicFieldDefinitions = self.result.dynamicFieldDefinitions
                            RESTServer.loadDocumentData(sessionData: self.sessionData) { result in
                            switch result {
                                case .success(let str):
                                    print(str)
                                case .failure(let error):
                                    switch error {
                                    case .badURL:
                                        print("Bad URL")
                                    case .requestFailed:
                                        print("Network problems")
                                    case .unknown:
                                        print("Unknown error")
                                    }
                                }
                            }
                            RESTServer.loadAllTags(sessionData: self.sessionData)
                            RESTServer.loadRecipientData(sessionData: self.sessionData)
                            
                        }else{
                            self.isNotAuthenticated = true
                        }
                }
        }.resume()
    }
    
    /*
     Initalize Docuemnt Filters
     */
    func initFilters(){
        //
        // My Docs Filter
        self.sessionData.myDocsFilter = DocumentFilter()
        if(self.sessionData.myDocumentList.count > 0){
            self.sessionData.myDocsFilter.dateFrom = self.sessionData.myDocumentList[self.sessionData.myDocumentList.count-1].creationTimestampDate
        }
        
        //
        // My Profile Filter
        self.sessionData.profileDocsFilter = DocumentFilter()
        if(self.sessionData.profileDocumentList.count > 0){
            self.sessionData.profileDocsFilter.dateFrom = self.sessionData.profileDocumentList[self.sessionData.profileDocumentList.count-1].creationTimestampDate
        }
        
        //
        // Feed Doc Filter
        self.sessionData.feedDocsFilter = DocumentFilter()
        if(self.sessionData.feedDocumentList.count > 0){
            self.sessionData.feedDocsFilter.dateFrom = self.sessionData.feedDocumentList[self.sessionData.feedDocumentList.count-1].creationTimestampDate
        }
        
    }
    
    func initDocTypes(){
        //
        // Profile Doc Types
        self.sessionData.profileDocTypes = self.sessionData.userGroups[0].allowedDocTypes.filter { $0.documentDesignation == DocType.DOC_TYPE_PROFILE
        }
        
        //
        // Passthrough Doc Types
        self.sessionData.passthroughDocTypes = self.sessionData.userGroups[0].allowedDocTypes.filter { $0.documentDesignation == DocType.DOC_TYPE_PASSTHROUGH
        }
    }
    
    /*
     Load All documents from the backend
     */
    func loadDocumentData() {
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_USER_GET_DOCUMENTS_ALL))
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "GET"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            //print(data)
            //print(response)
            //print(error)
            guard let data = data else { return }
            let decodedResponse = try! JSONDecoder().decode([DocumentDTO].self, from: data)
                
                DispatchQueue.main.async {
                        print("Document Data Response --> \(decodedResponse)")
                    self.sessionData.documentList = decodedResponse
                    print("Number of Documents : \(self.sessionData.documentList.count)")
                        //if (self.result.credentials.token != nil) {
                        //    self.isAuthorized = true
                        //}
                    
                    //
                    // set the profile docs
                    self.sessionData.profileDocumentList = self.myProfileDocsFilter.reversed()
                    
                    //
                    // set my docs
                    self.sessionData.myDocumentList = self.myPassthroughDocsFilter.reversed()

                    
                    //
                    // set feed docs
                    self.sessionData.feedDocumentList = self.feedDocsFilter.reversed()
                    
                    //
                    // Init filters
                    self.initFilters()
                    
                    //
                    // Load doc types
                    self.initDocTypes()
                }
        }.resume()
    } // Load document data
    
    //
    // Load all recipients
    //
    func loadRecipientData() {
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_GET_USER_ALL))
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "GET"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            guard let data = data else { return }
            let decodedResponse = try! JSONDecoder().decode([Recipient].self, from: data)
                
                DispatchQueue.main.async {
                        print("Document Data Response --> \(decodedResponse)")
                    self.sessionData.allRecipients = decodedResponse
                }
        }.resume()
    } // load recipient data
    
    //
    // Load all docs that can be attached
    //
    func loadAllTags() {
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_GET_TAGS_FOR_USER_ALL))
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "GET"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            guard let data = data else { return }
            let decodedResponse = try! JSONDecoder().decode([DocumentTag].self, from: data)
                
                DispatchQueue.main.async {
                        print("Document Data Response --> \(decodedResponse)")
                    self.sessionData.allTags = decodedResponse
                }
        }.resume()
    } // load all tags
    
}

struct UserLogin_Previews: PreviewProvider {
    static var previews: some View {
        UserLogin()
        .environmentObject(ViewRouter())
        .environmentObject(SessionData())
    }
}


struct CompanyLogoView: View {
    var body: some View {
        Image("ic_truetrace_logo").resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: 200.0, height: 200.0)
            .padding(.bottom, 75)
    }
}
