//
//  RecipientChooser.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-11.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct RecipientChooser: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    var documentCard : DocumentDTO
    
    @State private var selectedRows = Set<Int>()
    
    var body: some View {
        
        NavigationView {
            VStack {
                List(self.sessionData.allRecipients, selection: $selectedRows) { recipient in
                    
                    HStack {
                        VStack {
                            Text(recipient.name)
                                .font(.title)
                                .fontWeight(.bold)
                            Text(recipient.userGroups[0].name)
                                .font(.footnote)
                                .fontWeight(.regular)
                        }
                    }
                }
                .navigationBarItems(trailing : EditButton())
                .navigationBarTitle(Text("Recipients (\(self.listAll()))"))

            }
            
        }.onAppear(perform: loadRecipientData)
        .onDisappear(perform: setChosenRecipients)

    }
    
    func listAll() -> String {
        for item in selectedRows {
            print(item)
            
        }
        //self.setChosenRecipients()
        return String(selectedRows.count)
    }
    

    
    func getRecipientForId(id: Int) -> Recipient{
        for item in self.sessionData.allRecipients {
            if(item.id == id){
                return item
            }
        }
        return Recipient()
    }
    
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
                    print("Number of Recipients : \(self.sessionData.allRecipients.count)")
                    //
                    // Set chosen Recipients
                    for item in self.getDocument().toRecipients {
                        self.selectedRows.insert(item.id)
                    }
                    print("Number of Selected Recipients <card> : \(self.getDocument().toRecipients.count)")
                    print("Number of Selected Recipients : \(self.selectedRows.count)")

                }
        }.resume()
    }
    
    func getDocument() -> DocumentDTO {
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            return sessionData.myDocumentList[index]
        }
        if sessionData.newWorkDocumentFlag == true { return sessionData.newWorkDocument }
        return DocumentDTO()
    }
    
    func setChosenRecipients(){
        var myRecipients = [Recipient]()
        for item in selectedRows {
            print(item)
            myRecipients.append(self.getRecipientForId(id: item))
        }
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            sessionData.myDocumentList[index].toRecipients = myRecipients
        }
        if sessionData.newWorkDocumentFlag == true { return sessionData.newWorkDocument.toRecipients = myRecipients }
    }
}

struct RecipientChooser_Previews: PreviewProvider {
    static var previews: some View {
        RecipientChooser(documentCard : DocumentDTO())
        .environmentObject(ViewRouter())
        .environmentObject(SessionData())
    }
}
