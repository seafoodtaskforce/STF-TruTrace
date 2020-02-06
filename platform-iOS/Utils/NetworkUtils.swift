//
//  NetworkUtils.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-02.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import Foundation

class RESTServer {
    
    static var REMOTE_SERVER_URL : String = "http://204.236.203.207:8080/WWFShrimpProject_v2/api_v2"
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
    static func fetchDocMarkAsReadURL(username : String, sessionId : String) -> String {
        let imageFetchURL = (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_USER_POST_DOCUMENT_MARK_READ)
        var imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{username}", with: username)
        imageFetchURLReplaced = imageFetchURLReplaced.replacingOccurrences(of: "{session_id}", with: sessionId)
        return imageFetchURLReplaced
    }
    
    
    //
    //
    //
    static func fetchSetDocStatusURL(username : String, sessionId : String, status: String) -> String {
        let imageFetchURL = (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_POST_SET_DOCUMENT_STATUS)
        var imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{username}", with: username)
        imageFetchURLReplaced = imageFetchURLReplaced.replacingOccurrences(of: "{session_id}", with: sessionId)
        imageFetchURLReplaced = imageFetchURLReplaced.replacingOccurrences(of: "{status}", with: status)
        return imageFetchURLReplaced
    }
    
    
    //
    //
    //
    static func fetchDocPagesDeleteURL(username : String, sessionId : String) -> String {
        let imageFetchURL = (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_DELETE_DOCUMENT_PAGE)
        let imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{session_id}", with: sessionId)
        return imageFetchURLReplaced
    }
    
    //
    //
    //
    static func fetchDocDeleteURL(username : String, sessionId : String) -> String {
        let imageFetchURL = (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_USER_DEL_DOCUMENT)
        let imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{session_id}", with: sessionId)
        return imageFetchURLReplaced
    }
}
