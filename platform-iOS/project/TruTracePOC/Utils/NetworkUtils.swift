//
//  NetworkUtils.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-02.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import Foundation

enum NetworkError: Error {
    case badURL, requestFailed, unknown
}

class RESTServer : Codable {
    
    var accessUrl : String
    
    //static var REMOTE_SERVER_URL : String = "http://204.236.203.207:8080/WWFShrimpProject_v2/api_v2"
    //static var REMOTE_SERVER_URL : String = "http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2"
    
    
    //
    // User Data
    static var REST_USER_POST_AUTHENTICATE : String = "/security/authenticate"
    static var REST_USER_GET_PROFILE_IMAGE : String = "/user/profileimage?user_name={username}"
    static var REST_USER_POST_PROFILE_IMAGE : String = "/user/profileimage"


    static var REST_USER_POST_DOCUMENT_MARK_READ : String = "/document/markread?doc_id={session_id}&user_name={username}"
    static var REST_USER_DEL_DOCUMENT : String = "/document/delete/{session_id}"
    
    //
    // Users
    static var REST_GET_USER_ALL : String = "/user/fetchall"
    static var REST_POST_UPDATE_USER_INFO : String = "/user/update"
    static var REST_POST_UPDATE_USER_CREDENTIALS : String = "/user/updatecredentials"
    
    //
    // Docs
    static var REST_GET_LINKABLE_DOCS_FRO_USER_ALL : String = "/document/fetchalldocstolink"
    static var REST_GET_BACKING_DOCS_FOR_USER_ALL : String = "/document/fetchalldocstoattach"
    static var REST_POST_CREATE_NEW_DOCUMENT : String = "/document/create"
    static var REST_POST_UPDATE_EXISTING_DOCUMENT : String = "/document/update"
    static var REST_POST_SET_DOCUMENT_STATUS : String = "/document/status?doc_id={session_id}&user_name={username}&doc_status={status}"
    static var REST_USER_GET_DOCUMENTS_ALL : String = "/document/fetchall"
    static var REST_USER_GET_DOCUMENT_PAGE : String = "/document/page?doc_id={doc_id}"
    static var REST_USER_GET_DOCUMENT_PAGE_THUMBNAIL : String = "/document/pagethumbnail?doc_id={doc_id}"
    static var REST_POST_CREATE_NEW_DOCUMENT_NOTES : String = "/document/addnotes"
    
    
    
    //
    // Pages
    static var REST_POST_SAVE_DOCUMENT_PAGE : String = "/document/pageimage"
    static var REST_DELETE_DOCUMENT_PAGE : String = "/document/delete/pages/{session_id}"
    
    
    //
    // Tags
    static var REST_GET_TAGS_FOR_USER_ALL : String = "/tag/fetchall"
    static var REST_POST_CREATE_NEW_TAG : String = "/tag/create"
    
    //
    // Notifications
    static var REST_GET_NOTIFICATIONS_FOR_USER_ALL : String = "/notification/fetchall"
    
 
    //
    //
    //
    static func fetchDocMarkAsReadURL(username : String, sessionId : String, sessionData : SessionData) -> String {
        let imageFetchURL = (sessionData.serverURL + RESTServer.REST_USER_POST_DOCUMENT_MARK_READ)
        var imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{username}", with: username)
        imageFetchURLReplaced = imageFetchURLReplaced.replacingOccurrences(of: "{session_id}", with: sessionId)
        return imageFetchURLReplaced
    }
    
    
    //
    //
    //
    static func fetchSetDocStatusURL(username : String, sessionId : String, status: String, sessionData : SessionData) -> String {
        let imageFetchURL = (sessionData.serverURL + RESTServer.REST_POST_SET_DOCUMENT_STATUS)
        var imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{username}", with: username)
        imageFetchURLReplaced = imageFetchURLReplaced.replacingOccurrences(of: "{session_id}", with: sessionId)
        imageFetchURLReplaced = imageFetchURLReplaced.replacingOccurrences(of: "{status}", with: status)
        return imageFetchURLReplaced
    }
    
    
    //
    //
    //
    static func fetchDocPagesDeleteURL(username : String, sessionId : String, sessionData : SessionData) -> String {
        let imageFetchURL = (sessionData.serverURL + RESTServer.REST_DELETE_DOCUMENT_PAGE)
        let imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{session_id}", with: sessionId)
        return imageFetchURLReplaced
    }
    
    //
    //
    //
    static func fetchDocDeleteURL(username : String, sessionId : String, sessionData : SessionData) -> String {
        let imageFetchURL = (sessionData.serverURL + RESTServer.REST_USER_DEL_DOCUMENT)
        let imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{session_id}", with: sessionId)
        return imageFetchURLReplaced
    }
    
    
    //
    //
    // Upload the new document
    static func uploadNewDocument(document : DocumentDTO, pages : [DocumentPage], sessionData : SessionData, docUITabDesignation : DocumentUITypeDesignation) {
         var jsonBody : Data?
         guard let url = URL(string:
             (sessionData.serverURL + RESTServer.REST_POST_CREATE_NEW_DOCUMENT))
         
         else {
             print("invalid URL")
             return
         }
         
         var request = URLRequest(url : url)
         request.httpMethod = "POST"
         request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
         request.setValue("true", forHTTPHeaderField: "sparse")
         request.setValue("application/json", forHTTPHeaderField: "Accept")
         request.setValue("application/json", forHTTPHeaderField: "Content-Type")
     
         let jsonEncoder = JSONEncoder()
         jsonEncoder.outputFormatting = .withoutEscapingSlashes
         if let encoded = try? jsonEncoder.encode(document) {
             jsonBody = encoded
         }

         
         //let finalBody = try! JSONSerialization.data(withJSONObject: authData)
         if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
             print("New Document Creation JSON: \(json.count)")
             print("New Document Creation JSON: \(json)")
         }
         
         request.httpBody = jsonBody
         
         //
         // Async Call to create
         URLSession.shared.dataTask(with: request) {
             (data, response, error) in
             guard let data = data else { return }
             
             //
             // get the response data from the server
             let decodedResponse = try! JSONDecoder().decode(DocumentDTO.self, from: data)
                 
             DispatchQueue.main.async {
                
                print("decoded response <doc> <create>  \(decodedResponse)")
                // Save the document in session
                //
                 
                //
                // Put the pages back for my documents
                if docUITabDesignation  != DocumentUITypeDesignation.feed {
                    sessionData.newWorkDocument.pages = pages.filter{ $0.deleted == false }
                }
                 
                //
                // put the id into the doc
                if docUITabDesignation  != DocumentUITypeDesignation.feed {
                    sessionData.newWorkDocument.id = decodedResponse.id
                }
                 
                //
                // get the new document id and add it to the session docuemnt
                if docUITabDesignation  == DocumentUITypeDesignation.my {
                    sessionData.myDocumentList.insert(sessionData.newWorkDocument, at: 0)
                }
                 
                 //
                 // send each page separately to the server
                 for docPage in pages {
                     print("Pages <doc> <create> \(docPage)")
                    RESTServer.saveImagePageToServer(page: docPage, sessionId : sessionData.newWorkDocument.syncID, sessionData : sessionData, docUITabDesignation : docUITabDesignation)
                 }
             }
         }.resume()
     } // Upload New Document
    
    //
    // Upload the updated document
    static func uploadUpdatedDocument(index : Int, pages : [DocumentPage], sessionData : SessionData, docUITabDesignation : DocumentUITypeDesignation)  {
        var jsonBody : Data?
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_POST_UPDATE_EXISTING_DOCUMENT))
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    
        let jsonEncoder = JSONEncoder()
        if let encoded = try? jsonEncoder.encode(sessionData.myDocumentList[index]) {
            jsonBody = encoded
        }

        
        //let finalBody = try! JSONSerialization.data(withJSONObject: authData)
        if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
            print("New Document Creation JSON: \(json.count)")
        }
        
        request.httpBody = jsonBody
        
        //
        // Async Call to create
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            //print(data)
            //print("New Document Creation response: \(response.)")
            //print("New Document Creation response: \(error)")
            DispatchQueue.main.async {
                
                    //
                    // Put the pages back but make sure to remove the deleted pages
                sessionData.myDocumentList[index].pages = pages.filter{ $0.deleted == false}
                    
                    //
                    // send each page separately to the server
                    for docPage in pages {
                        if(docPage.id <= 0 && docPage.deleted == false){
                            print("Pages <doc> <create> \(docPage)")
                            RESTServer.saveImagePageToServer(page: docPage, sessionId : sessionData.myDocumentList[index].syncID, sessionData : sessionData, docUITabDesignation : docUITabDesignation)
                        }
                        if(docPage.deleted == true && docPage.id > 0){
                            print("Pages <doc> <delete> \(docPage)")
                            RESTServer.deleteImagePageFromServer(page: docPage, sessionId : sessionData.myDocumentList[index].syncID, sessionData : sessionData)
                        }
                    }
            }
        }.resume()
    } // Upload the updated document
    
    
    //
    // Upload the new serialized document
    static func uploadNewSerializedDocument(document : DocumentDTO, pages : [DocumentPage], sessionData : SessionData) {
         var jsonBody : Data?
         guard let url = URL(string:
             (sessionData.serverURL + RESTServer.REST_POST_CREATE_NEW_DOCUMENT))
         
         else {
             print("invalid URL")
             return
         }
         
         var request = URLRequest(url : url)
         request.httpMethod = "POST"
         request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
         request.setValue("true", forHTTPHeaderField: "sparse")
         request.setValue("application/json", forHTTPHeaderField: "Accept")
         request.setValue("application/json", forHTTPHeaderField: "Content-Type")
     
         let jsonEncoder = JSONEncoder()
         jsonEncoder.outputFormatting = .withoutEscapingSlashes
         if let encoded = try? jsonEncoder.encode(document) {
             jsonBody = encoded
         }

         
         //let finalBody = try! JSONSerialization.data(withJSONObject: authData)
         if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
             print("New Document Creation JSON: \(json.count)")
             print("New Document Creation JSON: \(json)")
         }
         
         request.httpBody = jsonBody
         
         //
         // Async Call to create
         URLSession.shared.dataTask(with: request) {
             (data, response, error) in
             //print(data)
             //print("New Document Creation response: \(response.)")
             //print("New Document Creation response: \(error)")
             guard let data = data else { return }
             
             //
             // get the response data from the server
             let decodedResponse = try! JSONDecoder().decode(DocumentDTO.self, from: data)
                 
             DispatchQueue.main.async {
                print("decoded response <doc> <create>  \(decodedResponse)")
                var foundDoc: Bool = false
                // Save the document in session
                //
            
                // find the document in the session
                //
                if let index = sessionData.myDocumentList.firstIndex(where: { $0.syncID == decodedResponse.syncID }) {
                    sessionData.myDocumentList[index].id = decodedResponse.id
                }
                if let index = sessionData.profileDocumentList.firstIndex(where: { $0.syncID == decodedResponse.syncID }) {
                    sessionData.profileDocumentList[index].id = decodedResponse.id
                }

                 
                 //
                 // send each page separately to the server
                 //for docPage in pages {
                 //    print("Pages <doc> <create> \(docPage)")
                 //   RESTServer.saveImagePageToServer(page: docPage, sessionId : sessionData.newWorkDocument.syncID, sessionData : sessionData)
                 //}
             }
         }.resume()
     } // Upload New Serialized Document
    
    //
    //
    // Save the image to the server
    static func saveImagePageToServer(page: DocumentPage, sessionId : String, sessionData : SessionData, docUITabDesignation : DocumentUITypeDesignation){
        
        let boundary = UUID().uuidString
        let filename = "page.jpg"
        let urlString = sessionData.serverURL + RESTServer.REST_POST_SAVE_DOCUMENT_PAGE
        print("[saveImagePageToServer] \(urlString)")

        guard let url = URL(string: urlString)
        
        else {
            print("invalid URL")
            return
        }
        let config = URLSessionConfiguration.default
        let session = URLSession(configuration: config)
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        var data = Data()
        
        //
        // Add user name
        let fieldNameUser = "userName"
        let fieldValueUser = sessionData.userCredentials.username
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"\(fieldNameUser)\"\r\n\r\n".data(using: .utf8)!)
        data.append("\(fieldValueUser)".data(using: .utf8)!)
        
        //
        // Add Session ID
        let fieldNameSessionId = "sessionId"
        let fieldValueSessionId = sessionId
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"\(fieldNameSessionId)\"\r\n\r\n".data(using: .utf8)!)
        data.append("\(fieldValueSessionId)".data(using: .utf8)!)
        
        //
        // Add page number
        let fieldNamePageNumber = "pageNumber"
        let fieldValuePageNumber  = page.pageNumber
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"\(fieldNamePageNumber)\"\r\n\r\n".data(using: .utf8)!)
        data.append("\(fieldValuePageNumber)".data(using: .utf8)!)
        
        //
        //
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        data.append("Content-Type: image/jpg\r\n\r\n".data(using: .utf8)!)
        data.append(page.getFormEncodedPageImageData())

        // End the raw http request data, note that there is 2 extra dash ("-") at the end, this is to indicate the end of the data
        // According to the HTTP 1.1 specification https://tools.ietf.org/html/rfc7230
        data.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        
        session.uploadTask(with: request, from: data, completionHandler: {
            responseData, response, error in
            
            if(error != nil){
                print("\(error!.localizedDescription)")
            }
            
            guard let responseData = responseData else {
                print("no response data")
                return
            }
            
            if let responseString = String(data: responseData, encoding: .utf8) {
                print("uploaded to: \(responseString)")
            }


            let decodedPage = try! JSONDecoder().decode(DocumentPage.self, from: responseData)
            print("Page Response <doc> <create> \(decodedPage)")
            
            DispatchQueue.main.async {
                //
                // reinsert the id of the page into the main document in the correct feed
                if(docUITabDesignation == DocumentUITypeDesignation.my) {
                    if let documentIndex = sessionData.myDocumentList.firstIndex(where: {$0.syncID == sessionId}) {
                        // Find the specfcific page
                        if let pageIndex = sessionData.myDocumentList[documentIndex].pages.firstIndex(where: {$0.pageNumber == page.pageNumber}) {
                            sessionData.myDocumentList[documentIndex].pages[pageIndex].id = decodedPage.id
                        } else {
                            // item could not be found
                        }
                    } else {
                        // item could not be found
                    }
                }
                
                if(docUITabDesignation == DocumentUITypeDesignation.profile) {
                    if let documentIndex = sessionData.profileDocumentList.firstIndex(where: {$0.syncID == sessionId}) {
                        // Find the specfcific page
                        if let pageIndex = sessionData.profileDocumentList[documentIndex].pages.firstIndex(where: {$0.pageNumber == page.pageNumber}) {
                            sessionData.profileDocumentList[documentIndex].pages[pageIndex].id = decodedPage.id
                        } else {
                            // item could not be found
                        }
                    } else {
                        // item could not be found
                    }
                }
            }
        }).resume()
    } // Save Image to the Server
    

    //
    // Delete the page image data from the server
    static func deleteImagePageFromServer(page: DocumentPage, sessionId : String, sessionData : SessionData){
        var jsonBody : Data?
        let urlString = RESTServer.fetchDocPagesDeleteURL(username: sessionData.userCredentials.username, sessionId: sessionId, sessionData: sessionData)
        print("[deleteImagePageFromServer] \(urlString)")

        guard let url = URL(string: urlString)
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "DELETE"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let jsonEncoder = JSONEncoder()
        jsonEncoder.outputFormatting = .withoutEscapingSlashes
        var pageIdData = [Int]()
        pageIdData.append(page.id)
        if let encoded = try? jsonEncoder.encode(pageIdData) {
            jsonBody = encoded
        }
        
        request.httpBody = jsonBody
        print("deleteImagePageFromServer \(jsonBody!)")
       
       if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
           print("[deleteImagePageFromServer]: \(json)")
       }
        
        URLSession.shared.dataTask(with: request) {
        (data, response, error) in
            
            // Get back to the main thread
            DispatchQueue.main.async {
                // nothing to do here
            }

        }.resume()
    } // Delete Page Image from the server
    
    /*
     Load All documents from the backend
     */
    static func loadDocumentData(sessionData : SessionData, completion: @escaping (Result<String, NetworkError>) -> Void) {
        //
        // create the URL
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_USER_GET_DOCUMENTS_ALL))
        
        else {
            print("invalid URL")
            completion(.failure(.badURL))
            return
        }
        
        //
        // create the full request
        var request = URLRequest(url : url)
        
        request.httpMethod = "GET"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in

            //
            // get the data into the main thread
            DispatchQueue.main.async {
                
                if let data = data {
                    let decodedResponse = try! JSONDecoder().decode([DocumentDTO].self, from: data)
                    
                    print("<REST> <loadDocumentData> Document Data Response --> \(decodedResponse)")
                    sessionData.documentList = decodedResponse
                    print("<REST> <loadDocumentData> Number of Documents : \(sessionData.documentList.count)")
                    
                    //
                    // set the profile docs
                    sessionData.profileDocumentList = RESTServer.myProfileDocsFilter(sessionData: sessionData).reversed()
                    sessionData.profileDocumentList.sort {
                        $0.creationTimestamp > $1.creationTimestamp
                    }
                    
                    //
                    // set my docs
                    sessionData.myDocumentList = RESTServer.myPassthroughDocsFilter(sessionData: sessionData).reversed()
                    sessionData.myDocumentList.sort {
                        $0.creationTimestamp > $1.creationTimestamp
                    }
                    
                    //
                    // set feed docs
                    sessionData.feedDocumentList = RESTServer.feedDocsFilter(sessionData: sessionData).reversed()
                    sessionData.feedDocumentList.sort {
                        $0.creationTimestamp > $1.creationTimestamp
                    }
                    
                    //
                    // Init filters
                    RESTServer.initFilters(sessionData: sessionData)
                    
                    //
                    // Load doc types
                    RESTServer.initDocTypes(sessionData: sessionData)
                } else if error != nil {
                    // any sort of network failure
                    completion(.failure(.requestFailed))
                } else {
                    // this ought not to be possible, yet here we are
                    completion(.failure(.unknown))
                }
            }
            
        }.resume()
    } // Load document data
    
    static func myProfileDocsFilter(sessionData : SessionData) -> [DocumentDTO] {
        return sessionData.documentList.filter { $0.type.documentDesignation  == "Profile"
            && $0.owner == sessionData.userCredentials.username
        }
    }
    
    static func myPassthroughDocsFilter(sessionData : SessionData) -> [DocumentDTO] {
        return sessionData.documentList.filter { $0.type.documentDesignation  == "Passthrough"
            && $0.owner == sessionData.userCredentials.username
        }
    }
    
    static func feedDocsFilter(sessionData : SessionData) -> [DocumentDTO] {
        return sessionData.documentList.filter { $0.owner != sessionData.userCredentials.username}
    }
    
    /*
     Initalize Docuemnt Filters
     */
    static func initFilters(sessionData : SessionData){
        //
        // My Docs Filter
        sessionData.myDocsFilter = DocumentFilter()
        if(sessionData.myDocumentList.count > 0){
            sessionData.myDocsFilter.dateFrom = sessionData.myDocumentList[sessionData.myDocumentList.count-1].creationTimestampDate
        }
        
        //
        // My Profile Filter
        sessionData.profileDocsFilter = DocumentFilter()
        if(sessionData.profileDocumentList.count > 0){
            sessionData.profileDocsFilter.dateFrom = sessionData.profileDocumentList[sessionData.profileDocumentList.count-1].creationTimestampDate
        }
        
        //
        // Feed Doc Filter
        sessionData.feedDocsFilter = DocumentFilter()
        if(sessionData.feedDocumentList.count > 0){
            sessionData.feedDocsFilter.dateFrom = sessionData.feedDocumentList[sessionData.feedDocumentList.count-1].creationTimestampDate
        }
    }
    
    static func initDocTypes(sessionData : SessionData){
        //
        // Profile Doc Types
        sessionData.profileDocTypes = sessionData.userGroups[0].allowedDocTypes.filter { $0.documentDesignation == DocType.DOC_TYPE_PROFILE
        }
        
        //
        // Passthrough Doc Types
        sessionData.passthroughDocTypes = sessionData.userGroups[0].allowedDocTypes.filter { $0.documentDesignation == DocType.DOC_TYPE_PASSTHROUGH
        }
    }
    
    //
    // Load all tags
    //
    static func loadAllTags(sessionData : SessionData) {
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
                    sessionData.allTags = decodedResponse
                }
        }.resume()
    } // load all tags
    
    //
    // Load all recipients
    //
    static func loadRecipientData(sessionData : SessionData) {
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
                    sessionData.allRecipients = decodedResponse
                }
        }.resume()
    } // load recipient data
    
    /*
     Mark the current document as read.
     */
    static func markDocAsRead(document : DocumentDTO, sessionData : SessionData) {
        let urlString = RESTServer.fetchDocMarkAsReadURL(
        username : sessionData.userCredentials.username,
        sessionId: document.syncID,
        sessionData: sessionData
        )
        
        if document.currentUserRead { return }
        print("Mark Read - URL .\(urlString)")
        guard let url = URL(string: urlString)
        
        else {
            print("invalid URL")
            return
        }
        
        print("Marking as READ .\(document.syncID)")
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        
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
        }.resume()
    }
    
    /*
     Set the status of the current document
     */
    static func setDocStatus(status : String, documentCard : DocumentDTO, sessionData : SessionData){
        let urlString = RESTServer.fetchSetDocStatusURL(
        username : sessionData.userCredentials.username,
        sessionId: documentCard.syncID,
        status:status,
        sessionData : sessionData
        )
        
        //if documentCard.currentUserRead { return }
        print("<FeedDocumentDetails> <set status> - URL .\(urlString)")
        guard let url = URL(string: urlString)
        
        else {
            print("invalid URL")
            return
        }
        
        print("<FeedDocumentDetails> <set status> session id .\(documentCard.syncID)")
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            guard let data = data else { return }
            if let dataString = String(data: data, encoding: .utf8){
                print(dataString)
            }
        }.resume()
    } // End Set Doc Status
    
    /*
     Add a Rejection Note to the document
     */
    static func addRejectionNote(document : DocumentDTO, sessionData : SessionData) {
        var jsonBody : Data?
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_POST_CREATE_NEW_DOCUMENT_NOTES))
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(String(document.id), forHTTPHeaderField: "doc-id")
        
        let jsonEncoder = JSONEncoder()
        jsonEncoder.outputFormatting = .withoutEscapingSlashes
        if let encoded = try? jsonEncoder.encode(document.notes[0]) {
            jsonBody = encoded
        }
        if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
            print("<addRejectionNote> Adding Doc Notes JSON: \(json)")
        }
        
        request.httpBody = jsonBody
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            guard let data = data else { return }
                //
                // get the data for the logged in user
            if let dataString = String(data: data, encoding: .utf8){
                print(dataString)
            }
        }.resume()
    }
}

//
//
// Extension of Bundle for loading json files
//
extension Bundle {
    func decode<T: Decodable>(_ type: T.Type, from file: String, dateDecodingStrategy: JSONDecoder.DateDecodingStrategy = .deferredToDate, keyDecodingStrategy: JSONDecoder.KeyDecodingStrategy = .useDefaultKeys) -> T {
        guard let url = self.url(forResource: file, withExtension: nil) else {
            fatalError("Failed to locate \(file) in bundle.")
        }

        guard let data = try? Data(contentsOf: url) else {
            fatalError("Failed to load \(file) from bundle.")
        }

        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = dateDecodingStrategy
        decoder.keyDecodingStrategy = keyDecodingStrategy

        do {
            return try decoder.decode(T.self, from: data)
        } catch DecodingError.keyNotFound(let key, let context) {
            fatalError("Failed to decode \(file) from bundle due to missing key '\(key.stringValue)' not found – \(context.debugDescription)")
        } catch DecodingError.typeMismatch(_, let context) {
            fatalError("Failed to decode \(file) from bundle due to type mismatch – \(context.debugDescription)")
        } catch DecodingError.valueNotFound(let type, let context) {
            fatalError("Failed to decode \(file) from bundle due to missing \(type) value – \(context.debugDescription)")
        } catch DecodingError.dataCorrupted(_) {
            fatalError("Failed to decode \(file) from bundle because it appears to be invalid JSON")
        } catch {
            fatalError("Failed to decode \(file) from bundle: \(error.localizedDescription)")
        }
    }
}
