//
//  ViewRouter.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-23.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import Foundation
import Combine
import SwiftUI

class ViewRouter: ObservableObject {
    
    static var LOGIN_PAGE : String = "login"
    static var HOME_PAGE : String = "home"
    static var NEW_PASSTHROUGH_DOC_PAGE : String = "new passthrough doc"
    static var NEW_PROFILE_DOC_PAGE : String = "new profile doc"
    static var MY_DOCUMENTS_TAB_VIEW : String = "my docs tab view"
    static var MY_FEED_TAB_VIEW : String = "my feed tab view"
    
    @Published var currentPage: String = ViewRouter.LOGIN_PAGE
    @Published var currentTabView: Int = 1

}
