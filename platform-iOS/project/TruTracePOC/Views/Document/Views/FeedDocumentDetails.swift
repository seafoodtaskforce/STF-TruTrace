//
//  FeedDocumetDetails.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-08.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct FeedDocumentDetails: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    @Environment(\.presentationMode) var presentationMode
    
    @State var noteHeader: String = ""
    @State var noteContent: String = ""
    @State private var showRejectionNotesSheet = false
    
    var documentCard : DocumentDTO
    
    static let pillColor: Color =  Color.blue
    @State private var showNotes = false

    var body: some View {

            Form {
                //
                // Summary section
                Section (header : Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_summary"))){
                    //Picker("Doc Type", selection: $docTypeIndex){
                    //    ForEach(0 ..< self.docTypes.count){
                    //        Text(self.docTypes[$0].value)
                    //    }
                    //}
                    VStack {
                        VStack(alignment: .leading) {
                            
                            Text(getDocument().documentType).font(.headline)
                            Text(LocalizationUtils.localizeDocumentStatus(text: getDocument().status)).font(.caption)
                            if(getDocument().status == DocumentDTO.DOC_STATUS_REJECTED && self.isThereNote(doc: getDocument())){
                                Divider()
                                Toggle(isOn: $showNotes) {
                                    Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_show_notes_label"))
                                        .font(.subheadline)
                                        .foregroundColor(Color.gray)
                                }
                            }
                            if(getDocument().status == "SUBMITTED"){
                                Divider()
                                VStack{
                                    // buttons
                                    //
                                    HStack {
                                        Button(action: {}) {
                                            Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_button_accept")).onTapGesture {
                                                print("<FeedDocumentDetails> <set status> - ACCEPT")
                                                self.setStatus(status: DocumentDTO.DOC_STATUS_ACCEPTED)
                                                RESTServer.setDocStatus(status : DocumentDTO.DOC_STATUS_ACCEPTED, documentCard : self.documentCard, sessionData : self.sessionData)
                                                //self.setDocStatus(status : DocumentDTO.DOC_STATUS_ACCEPTED)
                                                self.presentationMode.wrappedValue.dismiss()
                                            }
                                        }
                                        Spacer()
                                        Button(action: {}) {
                                            Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_button_reject")).onTapGesture {
                                                self.showRejectionNotesSheet.toggle()
                                                //self.setStatus(status: DocumentDTO.DOC_STATUS_REJECTED)
                                                //self.setDocStatus(status : DocumentDTO.DOC_STATUS_REJECTED)
                                                //self.presentationMode.wrappedValue.dismiss()
                                                
                                            }
                                        }.sheet(isPresented: self.$showRejectionNotesSheet) {
                                            self.getRejectionNoteSheetView()
                                        }
                                    }

                                }
                            }
                        }
 
                        
                        if(showNotes){
                            VStack{
                                HStack {
                                    Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_header"))
                                    Spacer()
                                    Text(getDocument().notes[0].getHeader())
                                }
                                HStack {
                                    Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_body"))
                                    Spacer()
                                    Text(getDocument().notes[0].getBody())
                                }
                            }
                        }
                    }
                }.onAppear { self.markDocAsRead() }
                
                //
                // Gallery sections
                Section (header :
                    HStack{
                        Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_page_gallery"))
                        Text("\(documentCard.pages.count)")
                            .font(.caption)
                            .fontWeight(.black)
                            .padding(5)
                            .background(Self.pillColor)
                            .clipShape(Circle())
                            .foregroundColor(.white)
                    }){
                    
                    ForEach(getSortedPages(), id: \.id) { page in
                        DocumentPageCell(page : page, pageIndex: self.getDocumentPageIndex(docPage : page) )
                    }
                }
                
                //
                // Recipients section
                Section (header :
                    HStack{
                        Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_recipients"))
                        Text("\(documentCard.toRecipients.count)")
                        .font(.caption)
                        .fontWeight(.black)
                        .padding(5)
                        .background(Self.pillColor)
                        .clipShape(Circle())
                        .foregroundColor(.white)
                    }){
                    ForEach(documentCard.toRecipients, id: \.id){ recipient in
                        UserCell(recipient : recipient).environmentObject(self.sessionData)
                    }
                }
                
                //
                // Tags section
                Section (header : HStack{
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_tags"))
                    Text("\(documentCard.tags.count)")
                    .font(.caption)
                    .fontWeight(.black)
                    .padding(5)
                    .background(Self.pillColor)
                    .clipShape(Circle())
                    .foregroundColor(.white)
                }){
                    ForEach(documentCard.tags, id: \.id) { tag in
                        Text(tag.text)
                    }
                }
                
                //
                // Linked Docs Section
                Section (header : HStack{
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_linked_docs"))
                    Text("\(documentCard.linkedDocuments.count)")
                        .font(.caption)
                        .fontWeight(.black)
                        .padding(5)
                        .background(Self.pillColor)
                        .clipShape(Circle())
                        .foregroundColor(.white)
                }){
                    ForEach(documentCard.linkedDocuments){ linkedDoc in
                        TraceDocCell(docData : linkedDoc)
                        
                    }
                }
                
                Section (header : HStack{
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_backup_docs"))
                    Text("\(documentCard.attachedDocuments.count)")
                        .font(.caption)
                        .fontWeight(.black)
                        .padding(5)
                        .background(Self.pillColor)
                        .clipShape(Circle())
                        .foregroundColor(.white)
                }){
                    ForEach(documentCard.attachedDocuments) { backingDoc in
                        TraceDocCell(docData : backingDoc)
                    }
                }
                
            }
            .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_navigation_header")), displayMode: .inline)
            .listStyle(GroupedListStyle())
        
        //
    }
    
    func getSortedPages() -> [DocumentPage] {
        let sortedPages = documentCard.pages.sorted{
            $0.pageNumber < $1.pageNumber
        }
        return sortedPages
    }
    
    
    func isThereNote(doc : DocumentDTO) -> Bool {
        if doc.notes.count == 0 { return false } else {
            return true
        }
    }
    /*
     Mark the current document as read.
     */
    func markDocAsRead() {
        if let index = sessionData.feedDocumentList.firstIndex(of: documentCard) {
            sessionData.feedDocumentList[index].currentUserRead = true
        }

        let urlString = RESTServer.fetchDocMarkAsReadURL(
        username : self.sessionData.userCredentials.username,
        sessionId: documentCard.syncID,
        sessionData: sessionData
        )
        guard let url = URL(string: urlString)
        
        else {
            print("invalid URL")
            return
        }
        
        print("Marking as READ .\(documentCard.syncID)")
        
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
    func setDocStatus(status : String){
        let urlString = RESTServer.fetchSetDocStatusURL(
        username : self.sessionData.userCredentials.username,
        sessionId: documentCard.syncID,
        status:status,
        sessionData: sessionData
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
    } // End Set Doc Status
    
    func getDocument() -> DocumentDTO{
        if let index = sessionData.feedDocumentList.firstIndex(of: documentCard) {
            return sessionData.feedDocumentList[index]
        }
        return DocumentDTO()
    }
    
    func setStatus(status : String){
        if let index = sessionData.feedDocumentList.firstIndex(of: documentCard){
            print("<FeedDocumentDetails> <set status> - local status")
            sessionData.feedDocumentList[index].status = status
        }
    }
    
    /*
     Get the current index of this page
     */
    func getDocumentPageIndex(docPage : DocumentPage) -> Int {
        var pageIndex = 0
        
        if let index = sessionData.feedDocumentList.firstIndex(of: documentCard) {

            for page in sessionData.feedDocumentList[index].pages {
                if page.tempId == docPage.tempId { return pageIndex }
                if !page.deleted {pageIndex += 1 }
            }
        }
        
        return pageIndex
    }
    
    //
    // get the filter view for this view
    private func getRejectionNoteSheetView() -> AnyView {
        print("<FeedDocumentDetails> Showing Note Reection Sheet\(self.sessionData.passthroughDocTypes.count)")
        return AnyView(RejectionNoteSheetView(documentCard : self.documentCard)
            .environmentObject(sessionData)
            .environmentObject(viewRouter))
    }
}

struct FeedDocumentDetails_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            
            FeedDocumentDetails(documentCard: DocumentDTO())
            .environmentObject(ViewRouter())
            .environmentObject(SessionData())
        }
    }
}
