//
//  TagsChooser.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-11.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct TagsChooser: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    var documentCard : DocumentDTO
    
    @State private var selectedRows = Set<Int>()
    @State private var newTagIsOn = false
    @State private var newTag = ""
    
    var body: some View {
        
        NavigationView {
            VStack {
                    if(self.newTagIsOn){
                        Section {
                            VStack{
                                Divider()
                                TextField("Enter New Tag", text: $newTag)
                            }
                        }
                        Divider()
                        Section {
                            HStack {
                                Button(action: {}) {
                                    Text("CREATE").onTapGesture {
                                        // ad the new tag to the list
                                        let documentTag = DocumentTag(text: self.newTag, organizationId:  self.sessionData.userGroups[0].organizationId)
                                        
                                        self.sessionData.allTags.append(documentTag)
                                        
                                        self.createNewTag(documentTag : documentTag)
                                        self.newTag = ""
                                        self.newTagIsOn.toggle()
                                        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to:nil, from:nil, for:nil)
                                    }
                                }
                                Spacer()
                                Button(action: {}) {
                                    Text("CANCEL").onTapGesture {
                                        self.newTagIsOn.toggle()
                                        self.newTag = ""
                                        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to:nil, from:nil, for:nil)
                                    }
                                }
                            }
                    }
                }
                
                List(self.sessionData.allTags, selection: $selectedRows) { tag in
                    
                    HStack {
                        VStack {
                            Text(tag.text)
                                .font(.title)
                                .fontWeight(.bold)
                        }
                    }
                }
                .navigationBarItems(
                    leading:
                    HStack {
                        Button(action: {
                            self.newTagIsOn.toggle()

                        }){
                            Text("New Tag")
                        }
                    },
                    trailing : EditButton())
                .navigationBarTitle(Text("Tags (\(self.listAll()))"))

            }
            
        }
        .onAppear(perform: loadAllTags)
        .onDisappear(perform: setChosenTags)

    }
    
    func listAll() -> String{
        for item in selectedRows {
            print(item)
        }
        //self.setChosenRecipients()
        return String(selectedRows.count)
    }
    

    
    func getTagForId(id: Int) -> DocumentTag {
        for item in self.sessionData.allTags {
            if(item.id == id){
                return item
            }
        }
        return DocumentTag()
    }
    
    //
    // Load all docs that can be attached
    //
    func loadAllTags() {
        guard let url = URL(string:
            (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_GET_TAGS_FOR_USER_ALL))
        
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
                    print("Number of Backing Docs : \(self.sessionData.allAttachable.count)")
                    //
                    // Set chosen Recipients
                    for item in self.getDocument().tags{
                        self.selectedRows.insert(item.id)
                    }
                    print("Number of Selected Docs <card> : \(self.getDocument().tags.count)")
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
    
    func setChosenTags(){
        var myTags = [DocumentTag]()
        for item in selectedRows {
            print(item)
            myTags.append(self.getTagForId(id: item))
        }
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            sessionData.myDocumentList[index].tags = myTags
        }
        if sessionData.newWorkDocumentFlag == true { sessionData.newWorkDocument.tags  = myTags}
    }
    
    //
    // Create New Tags
    func createNewTag(documentTag : DocumentTag) {
        var jsonBody : Data?
        guard let url = URL(string:
            (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_POST_CREATE_NEW_TAG))
        
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
        if let encoded = try? jsonEncoder.encode(documentTag) {
            jsonBody = encoded
        }

        
        //let finalBody = try! JSONSerialization.data(withJSONObject: authData)
        if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
            print("New Tag Creation JSON: \(json)")
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
                // get the data for the logged in user
            if let dataString = String(data: data, encoding: .utf8){
                print("Response from Server <tag creation> : \(dataString)")
            }
            let decodedResponse = try! JSONDecoder().decode(DocumentTag.self, from: data)

            DispatchQueue.main.async {
                                //
                    // Find the tag in session
                if let index = self.sessionData.allTags.firstIndex(where: {$0.text == documentTag.text}){
                        self.sessionData.allTags[index].id  = decodedResponse.id
                        self.selectedRows.insert(decodedResponse.id)
                    }
            }
        }.resume()
    }
}

struct TagsChooser_Previews: PreviewProvider {
    static var previews: some View {
        TagsChooser(documentCard : DocumentDTO())
        .environmentObject(ViewRouter())
        .environmentObject(SessionData())
    }
}
