//
//  DocumentCard.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-11-25.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct DocumentCardCell: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    
    var documentCard: DocumentDTO

    var body: some View {
        NavigationLink(destination: self.getNavigatioViewDestination()){
            HStack {
                Rectangle()
                    .fill(Color(hex: self.getDocument().TypeHEXColor))
                    .frame(width: 10, height: 80)
                
                
                VStack (alignment: .leading) {

                    // 1st line
                    ZStack (alignment: .top){
                        HStack {
                            Text(self.getDocument().documentType).bold().offset(y: 10)
                            if(!self.getDocument().currentUserRead){
                                Spacer()
                                Circle()
                                    .fill(Color.orange)
                                .frame(width: 15, height: 15).offset(y: 10).padding(.trailing, 10)
                            }
                            
                        }
                    }
                    // 1st line
                    ZStack (alignment: .top){
                        HStack {
                            Text(self.getDocument().groupName).font(.caption).offset(y: -2)
                            Spacer()
                            Image(systemName: "checkmark.circle").frame(width: 15, height: 15).offset(y: 0).padding(.trailing, 10)
                        }
                    }

                    // 3rd line
                    HStack {
                        Text(self.getDocument().owner).font(.subheadline).offset(y: -10)
                        if(self.getDocument().tags.count > 0){
                            Spacer();
                            Text(self.getDocument().tags[0].text).bold().font(.caption).padding(.trailing, 10)
                        }
                        
                    }
                
                    // 4th line
                    //HStack {
                    //    Spacer()
                    //    Text(documentCard.owner).font(.subheadline).padding(.trailing, 10)
                    //}
                    
                    HStack {
                        //Text("\(documentCard.creationDate, formatter: dateFormatterPrinter)").font(.caption)
                        Text(self.formatDateToString(date: self.getDocument().creationTimestamp)).font(.caption)
                        Spacer()
                        if(self.getDocument().tags.count > 0){
                            Image(systemName: "tag.circle.fill")
                        }else{
                            Image(systemName: "tag.circle.fill").opacity(0.3)
                        }
                        if(self.getDocument().linkedDocuments.count > 0){
                            Image(systemName: "link.circle.fill")
                        }else{
                            Image(systemName: "link.circle.fill").opacity(0.3)
                        }
                        if(self.getDocument().attachedDocuments.count > 0){
                            Image(systemName: "paperclip.circle.fill")
                        }else{
                            Image(systemName: "paperclip.circle.fill").opacity(0.3)
                        }
                        if(self.getDocument().toRecipients.count > 0){
                            Image(systemName: "person.crop.circle.fill.badge.plus")
                        }else{
                            Image(systemName: "person.crop.circle.fill.badge.plus").opacity(0.3)
                        }
                        Spacer()
                        Text(LocalizationUtils.localizeDocumentStatus(text: self.getDocument().status)).font(.caption).padding(.trailing, 10)
                    }.offset(y: -10)
                }
            }//.navigationBarTitle(Text("Details"), displayMode: .inline)
        }
    }
    
    func formatDateToString(date: String) -> String {
        return String(date.prefix(16))
    }
    
    func getNavigatioViewDestination() -> AnyView {
        //return AnyView(DocumentDetails(documentCard: documentCard).environmentObject(viewRouter)
        //.environmentObject(sessionData))
        
        if (self.getDocument().owner != self.sessionData.userCredentials.username) {
            return AnyView(FeedDocumentDetails(documentCard: self.getDocument()).environmentObject(viewRouter)
            .environmentObject(sessionData))
        }else{
            if(self.getDocument().type.documentDesignation == "Profile"){
                return AnyView(ProfileDocumentDetail(documentCard: self.getDocument()).environmentObject(viewRouter)
                .environmentObject(sessionData))
            }else {
                //sessionData.inActiveUpdateMode = true
                //sessionData.updateWorkDocumentFlag = true
                return AnyView(DocumentDetails(documentCard: self.getDocument()).environmentObject(viewRouter)
                .environmentObject(sessionData))
            }
        }
    }
    
    //
    //
    // Functions
    
    func getDocument() -> DocumentDTO {
        if let index = sessionData.feedDocumentList.firstIndex(of: documentCard) {
            return sessionData.feedDocumentList[index]
        }
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            return sessionData.profileDocumentList[index]
        }
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            return sessionData.myDocumentList[index]
        }
        return DocumentDTO()
    }
    
    
}

struct DocumentCardCell_Previews: PreviewProvider {
    static var previews: some View {
        DocumentCardCell(documentCard: DocumentDTO())
        .environmentObject(ViewRouter())
        .environmentObject(SessionData())
    }
}
