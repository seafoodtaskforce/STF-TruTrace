//
//  MotherView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-23.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct MotherView: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    
   var body: some View {
        VStack {
            
            //
            //
            if viewRouter.currentPage == ViewRouter.HOME_PAGE {
                HomeView().environmentObject(self.viewRouter)
                .environmentObject(self.sessionData)
            }
            
            //
            //
            if viewRouter.currentPage == ViewRouter.LOGIN_PAGE {
                KeyboardHost {
                    UserLogin().environmentObject(self.viewRouter)
                    .environmentObject(self.sessionData)
                }
            }
            //
            //
            if viewRouter.currentPage == ViewRouter.MY_DOCUMENTS_TAB_VIEW {
                HomeView().environmentObject(self.viewRouter)
                    .environmentObject(self.sessionData)
            }
            
            //
            //
            if viewRouter.currentPage == ViewRouter.NEW_PASSTHROUGH_DOC_PAGE{
                NewDocumentDetails(documentCard: sessionData.newWorkDocument).environmentObject(self.viewRouter)
                .environmentObject(self.sessionData)
            }
            
            //
            //
            if viewRouter.currentPage == ViewRouter.NEW_PROFILE_DOC_PAGE{
                NewProfileDocumentDetails(documentCard: sessionData.newWorkDocument).environmentObject(self.viewRouter)
                .environmentObject(self.sessionData)
            }
        }
    }
}

struct MotherView_Previews: PreviewProvider {
    static var previews: some View {
        MotherView()
            .environmentObject(ViewRouter())
            .environmentObject(SessionData())
    }
}
