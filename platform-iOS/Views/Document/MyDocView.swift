//
//  MyDocView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-03.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct MyDocView: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    
    
    @State private var showFilterDocsSheet = false
    @State private var filterIsOn = true
    
    var body: some View {
        NavigationView {
            List{
                ForEach(sessionData.myDocumentList.filter{self.filterDocumentItem(element : $0)}){ doc in
                    DocumentCardCell(documentCard: doc)
                }.onDelete(perform: deleteItems)
            }.navigationBarTitle(LocalizationUtils.localizeString(text: "ios_my_docs_Page_navigation_header"))
            .navigationBarItems(leading:
            HStack {
                Button(action: {
                    // add another image to the mix
                    self.createNewDocument()
                    self.viewRouter.currentPage = ViewRouter.NEW_PASSTHROUGH_DOC_PAGE
                }) {
                    Text(LocalizationUtils.localizeString(text: "ios_my_docs_Page_navigation_button_new"))
                }
            }
            ,
            trailing:
            Button(action: {
                self.showFilterDocsSheet.toggle()
            }) {
                HStack{
                   Text(LocalizationUtils.localizeString(text: "ios_my_docs_Page_navigation_button_filter"))
                    Toggle(isOn : $filterIsOn){
                        Text("")
                    }
                }
                
            }.sheet(isPresented: self.$showFilterDocsSheet) {
                self.getFilterView()
            })
        }
    }
    
    func deleteItems(at offsets: IndexSet){
        for i in offsets {
            self.deleteDocument(sessionId : sessionData.myDocumentList[i].syncID)
            print(sessionData.myDocumentList[i].syncID)
        }
        sessionData.myDocumentList.remove(atOffsets: offsets)
    }
    
    func deleteDocument(sessionId : String) {
        let urlString = RESTServer.fetchDocDeleteURL(
            username : self.sessionData.userCredentials.username,
            sessionId: sessionId
        )
        
        print("Delete Document - URL .\(urlString)")
        guard let url = URL(string: urlString)
        
        else {
            print("invalid URL")
            return
        }
        
        print("Deleting Document .\(sessionId)")
        
        var request = URLRequest(url : url)
        request.httpMethod = "DELETE"
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
    
    private func createNewDocument(){
        // create a new doc
        var documentCard : DocumentDTO = DocumentDTO()
        // add other dastas
        documentCard.type = self.sessionData.passthroughDocTypes[0]
        self.sessionData.newWorkDocumentFlag = true
        
        // add it to the list of docs (append)
        self.sessionData.newWorkDocument = documentCard
    }
    
    private func getFilterView() -> AnyView {
        print("<MyDocView> Showing Filter with \(self.sessionData.passthroughDocTypes.count)")
        return AnyView(MyDocsFilterView().environmentObject(sessionData))
    }
    
    //
    // Filter the list data
    private func getFilteredDocumentList() -> [DocumentDTO]{
        return sessionData.myDocumentList.filter{self.filterDocumentItem(element : $0)}
    }
    
    private func filterDocumentItem(element : DocumentDTO) -> Bool {
        if(!filterIsOn) { return true }
        
        //
        // Dates
        let componentsFrom = Calendar.current.dateComponents([.year, .month, .day], from: sessionData.myDocsFilter.dateFrom )
        let componentsTo = Calendar.current.dateComponents([.year, .month, .day], from: sessionData.myDocsFilter.dateTo )
        let componentsCompareDate = Calendar.current.dateComponents([.year, .month, .day], from: element.creationTimestampDate  )
        
        let componentsFromDate = Calendar.current.date(from: componentsFrom)!
        let componentsToDate = Calendar.current.date(from: componentsTo)!
        let componentsCompareUserDate = Calendar.current.date(from: componentsCompareDate)!
        
        if(componentsCompareUserDate < componentsFromDate
            || componentsCompareUserDate > componentsToDate) {
            return false
        }
        //if(element.creationTimestampDate < sessionData.myDocsFilter.dateFrom
        //    || element.creationTimestampDate > sessionData.myDocsFilter.dateTo) {
        //    return false
        //}
        
        //
        // recipient
        if(sessionData.myDocsFilter.recipient!.name != Recipient.NULL_VALUE){
            // check further
            if(!element.toRecipients.contains{$0.name == sessionData.myDocsFilter.recipient!.name}) {
                return false
            }
        }
        
        //
        // Doc Data Type
        if(sessionData.myDocsFilter.documentType!.name != Recipient.NULL_VALUE){
            // check further
            if(element.type.name != sessionData.myDocsFilter.documentType!.name ) {
                return false
            }
        }
        
        //
        // tag
        if(sessionData.myDocsFilter.tag!.text != DocumentTag.NULL_VALUE){
            // check further
            if(!element.tags.contains{$0.text == sessionData.myDocsFilter.tag!.text}) {
                return false
            }
        }
        
        return true
    }
}

struct MyDocView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView{
            MyDocView()
            .environmentObject(ViewRouter())
            .environmentObject(SessionData())
        }
    }
}

