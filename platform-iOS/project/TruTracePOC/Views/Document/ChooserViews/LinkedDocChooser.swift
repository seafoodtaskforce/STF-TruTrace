//
//  LinkedDocChooser.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-11.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct LinkedDocChoser: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    var documentCard : DocumentDTO
    
    @State private var selectedRows = Set<Int>()
    
    var body: some View {
        
        NavigationView {
            VStack {
                List(self.sessionData.allLinkable, selection: $selectedRows) { doc in
                    
                    LinkedDocPreviewDetail(documentCard : doc)
                }
                .navigationBarItems(trailing : EditButton())
                .navigationBarTitle(Text("Linked Docs (\(self.listAll()))"), displayMode: .inline)

            }
            
        }.onAppear(perform: loadLinkedDocData)
        .onDisappear(perform: setChosenLinkedDocs)

    }
    
    func listAll() -> String{
        //self.setChosenRecipients()
        return String(selectedRows.count)
    }
    

    
    func getLinkedDocForId(id: Int) -> DocumentDTO {
        for item in self.sessionData.allLinkable {
            if(item.id == id){
                return item
            }
        }
        return DocumentDTO()
    }
    
    //
    // Load all recipients
    //
    func loadLinkedDocData() {
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_GET_LINKABLE_DOCS_FRO_USER_ALL))
        
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
            let decodedResponse = try! JSONDecoder().decode([DocumentDTO].self, from: data)
                
                DispatchQueue.main.async {
                        print("Document Data Response --> \(decodedResponse)")
                    self.sessionData.allLinkable = decodedResponse
                    print("Number of Recipients : \(self.sessionData.allLinkable.count)")
                    //
                    // Set chosen Recipients
                    for item in self.getDocument().linkedDocuments{
                        self.selectedRows.insert(item.id)
                    }
                    print("Number of Selected Docs <card> : \(self.getDocument().linkedDocuments.count)")
                    print("Number of Selected Docs : \(self.selectedRows.count)")

                }
        }.resume()
    }
    
    func getDocument() -> DocumentDTO{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            return sessionData.myDocumentList[index]
        }
        if sessionData.newWorkDocumentFlag == true { return sessionData.newWorkDocument }
        return DocumentDTO()
    }
    
    func setChosenLinkedDocs(){
        var myDocs = [DocumentDTO]()
        for item in selectedRows {
            print(item)
            myDocs.append(self.getLinkedDocForId(id: item))
        }
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            sessionData.myDocumentList[index].linkedDocuments = myDocs
        }
        if sessionData.newWorkDocumentFlag == true { sessionData.newWorkDocument.linkedDocuments =  myDocs}
    }
}

struct LinkedDocChoser_Previews: PreviewProvider {
    static var previews: some View {
        LinkedDocChoser(documentCard : DocumentDTO())
        .environmentObject(ViewRouter())
        .environmentObject(SessionData())
    }
}

struct LinkedDocPreviewDetail: View {
    var documentCard : DocumentDTO
    
    var body: some View {
        NavigationLink(destination: FeedDocumentDetails(documentCard: self.documentCard)){
            HStack {
                VStack {
                    Text(documentCard.owner)
                        .font(.title)
                        .fontWeight(.bold)
                    Text(documentCard.documentType)
                        .font(.footnote)
                        .fontWeight(.regular)
                }
            }
        }
    }
}
