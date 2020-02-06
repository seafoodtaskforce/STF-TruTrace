//
//  BackingDocsChooser.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-11.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct BackingDocsChooser: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    var documentCard : DocumentDTO
    
    @State private var selectedRows = Set<Int>()
    
    var body: some View {
        
        NavigationView {
            VStack {
                List(self.sessionData.allAttachable, selection: $selectedRows) { doc in
                    
                    BackingDocPreviewDetail(documentCard:doc)
                }
                .navigationBarItems(trailing : EditButton())
                .navigationBarTitle(Text("Backing Docs (\(self.listAll()))"))

            }
            
        }.onAppear(perform: loadBackingDocData)
        .onDisappear(perform: setChosenBackingDocs)

    }
    
    func listAll() -> String{
        for item in selectedRows {
            print(item)
            
        }
        //self.setChosenRecipients()
        return String(selectedRows.count)
    }
    

    
    func getBackingDocForId(id: Int) -> DocumentDTO {
        for item in self.sessionData.allAttachable {
            if(item.id == id){
                return item
            }
        }
        return DocumentDTO()
    }
    
    //
    // Load all docs that can be attached
    //
    func loadBackingDocData() {
        guard let url = URL(string:
            (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_GET_BACKING_DOCS_FOR_USER_ALL))
        
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
                    self.sessionData.allAttachable = decodedResponse
                    print("Number of Backing Docs : \(self.sessionData.allAttachable.count)")
                    //
                    // Set chosen Recipients
                    for item in self.getDocument().attachedDocuments{
                        self.selectedRows.insert(item.id)
                    }
                    print("Number of Selected Docs <card> : \(self.getDocument().attachedDocuments.count)")
                    print("Number of Selected Docs : \(self.selectedRows.count)")

                }
        }.resume()
    }
    
    func getDocument() -> DocumentDTO{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            return sessionData.myDocumentList[index]
        }
        if sessionData.newWorkDocumentFlag == true { return sessionData.newWorkDocument}
        return DocumentDTO()
    }
    
    func setChosenBackingDocs(){
        var myDocs = [DocumentDTO]()
        for item in selectedRows {
            print(item)
            myDocs.append(self.getBackingDocForId(id: item))
        }
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            sessionData.myDocumentList[index].attachedDocuments = myDocs
        }
         if sessionData.newWorkDocumentFlag == true { sessionData.newWorkDocument.attachedDocuments =  myDocs}
    }
}

struct BackingDocsChooser_Previews: PreviewProvider {
    static var previews: some View {
        BackingDocsChooser(documentCard : DocumentDTO())
        .environmentObject(ViewRouter())
        .environmentObject(SessionData())
    }
}

struct BackingDocPreviewDetail: View {
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
