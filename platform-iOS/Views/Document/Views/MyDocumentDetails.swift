//
//  DocumentDetails.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-11-25.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct DocumentDetails: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    @Environment(\.presentationMode) var presentationMode
    
    
    @State private var inputImage = UIImage()
    @State private var showingImagePicker = false
    @State private var isShowingScannerSheet = false
    @State private var showingRecipientsList = false
    @State private var showingLinkedDocsList = false
    @State private var showingBackingDocsList = false
    @State private var showingTagsList = false
    @State private var docTypeIndex = -1
    @State private var showSaveActionSheet = false

    
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
                            //Text(documentCard.documentType).font(.headline)
                            Picker(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_doc_label_type"), selection: $docTypeIndex){
                                ForEach(0 ..< getDocTypeCount()){
                                    Text(self.sessionData.passthroughDocTypes[$0].value)
                                }
                            }
                            Text(LocalizationUtils.localizeDocumentStatus(text: documentCard.status)).font(.caption)
                            if(documentCard.status == "REJECTED"
                                && self.isThereNote(doc: documentCard)){
                                    Divider()
                                    Toggle(isOn: $showNotes) {
                                        Text("Show Notes").font(.subheadline).foregroundColor(Color.gray)
                                    }
                            }
                        }
                        .onAppear{
                            if(self.docTypeIndex == -1){
                                self.getDocTypeIndex(docType : self.getDocument().type)
                            }
                            
                        }
                        .onDisappear{
                            self.setCurrentDocType()
                        }
                        if(showNotes){
                            VStack{
                                HStack {
                                    Text("Header")
                                    Spacer()
                                    Text(documentCard.notes[0].getHeader())
                                }
                                HStack {
                                    Text("Body")
                                    Spacer()
                                    Text(documentCard.notes[0].getBody())
                                }
                            }
                        }
                    }
                }
                
                
                //
                // Gallery sections
                Section (header :
                    HStack{
                        Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_page_gallery"))
                        Text("\(getDocumentPages().count)")
                            .font(.caption)
                            .fontWeight(.black)
                            .padding(5)
                            .background(Self.pillColor)
                            .clipShape(Circle())
                            .foregroundColor(.white)
                        Spacer()
                        HStack(spacing: 20) {
                            //EditButton()
                            Button(action: openCamera) {
                                Image(systemName: "camera").resizable()
                                .frame(width:20.0, height: 20.0)
                            }
                            .disabled(!isEditable())
                            .sheet(isPresented: self.$isShowingScannerSheet) { self.makeScannerView().environmentObject(self.sessionData) }
                            Button(action: {
                                // add another image to the mix
                                self.inputImage = UIImage()
                                self.showingImagePicker = true
                            }) {
                                Image(systemName: "photo.on.rectangle").resizable()
                                .frame(width:20.0, height: 20.0)
                            }
                            .disabled(!isEditable())
                            .sheet(isPresented: $showingImagePicker, onDismiss: loadImage) {
                                ImagePicker(image: self.$inputImage)
                            }
                            
                            //.sheet(isPresented: $isShowingImagePicker, content: {
                            //    ImagePickerView(isPresented : self.$isShowingImagePicker,
                            //                    documentCard : self.documentCard).environmentObject(self.sessionData)
                            //})
                        }
                    }){
                    
                    ForEach(getDocumentPages(), id: \.pageNumber) { page in
                        DocumentPageCell(page : page, pageIndex: self.getDocumentPageIndex(docPage: page))
                    }.onDelete(perform: deletePageItems)
                }
                
                //
                // Recipients section
                Section (header :
                    HStack{
                        Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_recipients"))
                        Text("\(self.getDocumentRecipients().count)")
                        .font(.caption)
                        .fontWeight(.black)
                        .padding(5)
                        .background(Self.pillColor)
                        .clipShape(Circle())
                        .foregroundColor(.white)
                        Spacer()
                        Button(action: {self.showingRecipientsList.toggle()}) {
                            Image(systemName: "person.badge.plus").resizable()
                            .frame(width:20.0, height: 20.0)
                        }
                        .disabled(!isEditable())
                        .sheet(isPresented: self.$showingRecipientsList) {
                            RecipientChooser(documentCard : self.documentCard)
                                .environmentObject(self.viewRouter)
                                .environmentObject(self.sessionData) }
                    }){
                    ForEach(getDocumentRecipients(), id: \.id){ recipient in
                        UserCell(recipient : recipient)
                    }.onDelete(perform: deleteRecipientItems)
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
                    
                    Spacer()
                    Button(action: {self.showingTagsList.toggle()}) {
                        Image(systemName: "tag").resizable()
                        .frame(width:20.0, height: 20.0)
                    }
                    .disabled(!isEditable())
                    .sheet(isPresented: self.$showingTagsList) {
                        TagsChooser(documentCard : self.documentCard)
                            .environmentObject(self.viewRouter)
                            .environmentObject(self.sessionData) }
                    
                }){
                    ForEach(getDocumentTags(), id: \.id) { tag in
                        Text(tag.text)
                    }.onDelete(perform: deleteTagItems)
                }
                
                //
                // Linked Docs Section
                Section (header : HStack{
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_linked_docs"))
                    Text("\(getDocumentLinked().count)")
                        .font(.caption)
                        .fontWeight(.black)
                        .padding(5)
                        .background(Self.pillColor)
                        .clipShape(Circle())
                        .foregroundColor(.white)
                    Spacer()
                    Button(action: {self.showingLinkedDocsList.toggle()}) {
                        Image(systemName: "link.circle").resizable()
                        .frame(width:20.0, height: 20.0)
                    }
                    .disabled(!isEditable())
                    .sheet(isPresented: self.$showingLinkedDocsList) {
                        LinkedDocChoser(documentCard : self.documentCard)
                            .environmentObject(self.viewRouter)
                            .environmentObject(self.sessionData) }
                }){
                    ForEach(getDocumentLinkedDocs()){ linkedDoc in
                        TraceDocCell(docData : linkedDoc)
                    }.onDelete(perform: deleteLinkedDocItems)
                }
                //
                // Backing Docs
                Section (header : HStack{
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_backup_docs"))
                    Text("\(documentCard.attachedDocuments.count)")
                        .font(.caption)
                        .fontWeight(.black)
                        .padding(5)
                        .background(Self.pillColor)
                        .clipShape(Circle())
                        .foregroundColor(.white)
                    Spacer()
                    Button(action: {self.showingBackingDocsList.toggle()}) {
                        Image(systemName: "paperclip.circle").resizable()
                        .frame(width:20.0, height: 20.0)
                    }
                    .disabled(!isEditable())
                    .sheet(isPresented: self.$showingBackingDocsList) {
                        BackingDocsChooser(documentCard : self.documentCard)
                            .environmentObject(self.viewRouter)
                            .environmentObject(self.sessionData) }
                }){
                    ForEach(getDocumentBackingDocs()) { backingDoc in
                        TraceDocCell(docData : backingDoc)
                    }.onDelete(perform: deleteBackingDocItems)
                }
                
                
                
            }
            .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_navigation_header")), displayMode: .inline)
            .navigationBarItems(
            trailing:
            HStack {
                Button(action: {
                    self.rollbackAnyChanges()
                    self.presentationMode.wrappedValue.dismiss()
                }){
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_navigation_button_cancel"))
                }
                .disabled(!isEditable())

                Button(action: {
                    self.showSaveActionSheet.toggle()
                }){
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_navigation_button_save"))
                }
                .disabled(!isEditable())
                .actionSheet(isPresented: $showSaveActionSheet) {
                    SaveDocumentActionSheet
                }
            })
            //.listStyle(GroupedListStyle())
        
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
    
    func deletePageItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            for pageIndex in offsets {
                sessionData.myDocumentList[index].pages[pageIndex].deleted = true
            }
            //sessionData.myDocumentList[index].pages.remove(atOffsets: offsets)
        }
    }
    
    //
    // Delete
    func deleteRecipientItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            for recipientIndex in offsets {
                sessionData.myDocumentList[index].toRecipients[recipientIndex].deleted = true
            }
            // sessionData.myDocumentList[index].toRecipients.remove(atOffsets: offsets)
        }
    }
    
    //
    // Delete
    func deleteLinkedDocItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            for linkedIndex in offsets {
                sessionData.myDocumentList[index].linkedDocuments[linkedIndex].deleted = true
            }
            // sessionData.myDocumentList[index].linkedDocuments.remove(atOffsets: offsets)
        }
    }
    
    //
    // Delete
    func deleteBackingDocItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            for attachedIndex in offsets {
                sessionData.myDocumentList[index].attachedDocuments[attachedIndex].deleted = true
            }
            //sessionData.myDocumentList[index].attachedDocuments.remove(atOffsets: offsets)
        }
    }
    
    //
    // Delete
    func deleteTagItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
             for tagIndex in offsets {
                sessionData.myDocumentList[index].tags[tagIndex].deleted = true
            }
            // sessionData.myDocumentList[index].tags.remove(atOffsets: offsets)
        }
    }
    
    //
    // Get the doc pages
    func getDocumentPages() -> [DocumentPage]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            let activePages = sessionData.myDocumentList[index].pages.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activePages
            // return sessionData.myDocumentList[index].pages
        }
        return [DocumentPage]()
    }
    
    func getDocumentTags() -> [DocumentTag]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            let activeTags = sessionData.myDocumentList[index].tags.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeTags
            //return sessionData.myDocumentList[index].tags
        }
        return [DocumentTag]()
    }
    
    func getDocumentBackingDocs() -> [DocumentDTO]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            let activeAttachedDocs = sessionData.myDocumentList[index].attachedDocuments.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeAttachedDocs
            //return sessionData.myDocumentList[index].attachedDocuments
        }
        return [DocumentDTO]()
    }
    
    func getDocumentLinkedDocs() -> [DocumentDTO]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            let activeLinkedDocs = sessionData.myDocumentList[index].linkedDocuments.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeLinkedDocs
            //return sessionData.myDocumentList[index].linkedDocuments
        }
        return [DocumentDTO]()
    }
    
    func getDocumentRecipients() -> [Recipient]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            let activeRecipients = sessionData.myDocumentList[index].toRecipients.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeRecipients
            //return sessionData.myDocumentList[index].toRecipients
        }
        return [Recipient]()
    }
    
    func getDocumentLinked() -> [DocumentDTO]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            let activeLinkedDocs = sessionData.myDocumentList[index].linkedDocuments.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeLinkedDocs
            //return sessionData.myDocumentList[index].linkedDocuments
        }
        return [DocumentDTO]()
    }
    
    func getDocumentsAttached() -> [DocumentDTO]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            let activeAttachedDocs = sessionData.myDocumentList[index].attachedDocuments.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeAttachedDocs
            //return sessionData.myDocumentList[index].attachedDocuments
        }
        return [DocumentDTO]()
    }
    
    func getDocument() -> DocumentDTO{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            return sessionData.myDocumentList[index]
        }
        return DocumentDTO()
    }
    
    private func getDocTypeIndex(docType : DocType){
        if let index = sessionData.passthroughDocTypes.firstIndex(of: docType) {
            self.docTypeIndex = index
            print("Doc Type Index <get>: \(index)")
            print("Doc Type Index <get> <data>: \(docType)")
        }
    }
    
    /*
     Get the current index of this page
     */
    func getDocumentPageIndex(docPage : DocumentPage) -> Int {
        var pageIndex = 0
        
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {

            for page in sessionData.myDocumentList[index].pages {
                if page.tempId == docPage.tempId { return pageIndex }
                if !page.deleted {pageIndex += 1 }
            }
        }
        
        return pageIndex
    }
    
    private func setCurrentDocType() {
        print("Doc Type State Index <set>: \($docTypeIndex)")
        print("Doc Type Index <START>")
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            if(index != nil && docTypeIndex >= 0){
                print("Doc Type Index <Process>")
                sessionData.myDocumentList[index].type = sessionData.passthroughDocTypes[docTypeIndex]
                print("Doc Type Index <set>: \(index)")
                print("Doc Type Index <set> <data>: \(sessionData.passthroughDocTypes[docTypeIndex])")
            }else{
                print("no values for doctype")
                docTypeIndex = 0
            }
        }
        print("Doc Type Index <END>")
    }
    
    func loadImage() {
        if inputImage.size.height > 0 {
            addNewPage(image: inputImage) }
    }
    
    func addNewPage(image : UIImage){
        print("Adding a new page for doc id: .\(documentCard.id)")
        print("Adding a new page for doc id <list>: .\(sessionData.myDocumentList.count)")
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            print("Adding a new page: .\(index)")
            print("Adding a new page <before>: .\(sessionData.myDocumentList[index].pages.count)")
            sessionData.myDocumentList[index].pages.append(DocumentPage(localImage: image, pageNumber: sessionData.myDocumentList[index].pages.count))
            print("Adding a new page <after>: .\(sessionData.myDocumentList[index].pages.count)")
        }
    }
    
    private func openCamera() {
        isShowingScannerSheet = true
    }
     
    private func makeScannerView() -> ScannerView {
        ScannerView(completion: { textPerPage in
            if let text = textPerPage?.joined(separator: "\n").trimmingCharacters(in: .whitespacesAndNewlines) {
                //self.text = text
            }
            self.isShowingScannerSheet = false
        }, document: documentCard)
    }
    
    private func getDocTypeCount() -> Int {
        return sessionData.passthroughDocTypes.count
    }
    
    private func isEditable() -> Bool {
        if ( self.documentCard.status == DocumentDTO.DOC_STATUS_ACCEPTED
            || self.documentCard.status == DocumentDTO.DOC_STATUS_SUBMITTED
            || self.documentCard.status == DocumentDTO.DOC_STATUS_PENDING){
            return false
        } else {
            return true
        }
    }
    
    //
    // Save New Document in Backend
    func saveDocument(status: String) {
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard){

           // add the document to the session
           print("Updating Existing Record in <saveDocument>: \(self.sessionData.myDocumentList.count)")
           print("Updating Existing Record at index <saveDocument>: \(index)")
           
           //
           //self.sessionData.myDocumentList.insert(self.sessionData.newWorkDocument, at: 0)
           //
           // Add instance data
           
           // timestamp
           let today = Date()
           let timestampFormatter = DateFormatter()
           timestampFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
           self.sessionData.myDocumentList[index].creationTimestamp = timestampFormatter.string(from: today)
           print("New Document Date <saveDocument>: \(sessionData.myDocumentList[index].creationTimestamp )")
           
           // was read
           self.sessionData.myDocumentList[index].currentUserRead = true
           
           // Status
           self.sessionData.myDocumentList[index].status = status
            
            //
            // remove the pages
            let pages = self.sessionData.myDocumentList[index].pages
            self.sessionData.myDocumentList[index].pages = [DocumentPage]()
           
           //
           // Upload
           
            self.uploadUpdatedDocument(index: index, pages : pages)

           //self.viewRouter.currentPage = ViewRouter.MY_DOCUMENTS_TAB_VIEW
        }

    } // saveDocument
    
    //
    // Upload the new docum,snrt
    func uploadUpdatedDocument(index : Int, pages : [DocumentPage])  {
        var jsonBody : Data?
        guard let url = URL(string:
            (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_POST_UPDATE_EXISTING_DOCUMENT))
        
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
        if let encoded = try? jsonEncoder.encode(sessionData.myDocumentList[index]) {
            jsonBody = encoded
        }

        
        //let finalBody = try! JSONSerialization.data(withJSONObject: authData)
        if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
            print("New Document Creation JSON: \(json.count)")
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
                //print("Response from Server: \(dataString)")
            }
            //let decodedResponse = try! JSONDecoder().decode(AuthResponse.self, from: data)
                
            DispatchQueue.main.async {
                
                    //
                    // Put the pages back
                    self.sessionData.myDocumentList[index].pages = pages
                    
                    //
                    // send each page separately to the server
                    for docPage in pages {
                        if(docPage.id <= 0 && docPage.deleted == false){
                            print("Pages <doc> <create> \(docPage)")
                            self.loadPageImage(page: docPage, sessionId : self.sessionData.myDocumentList[index].syncID)
                        }
                        if(docPage.deleted == true && docPage.id > 0){
                            print("Pages <doc> <delete> \(docPage)")
                            self.deletePageImage(page: docPage, sessionId : self.sessionData.myDocumentList[index].syncID)
                        }
                    }
            }
        }.resume()
    }
    
    //
    //
    // Action Sheet for saving a new doc
    var SaveDocumentActionSheet: ActionSheet {
        ActionSheet(title: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_title")), message: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_message")), buttons: [
                .default(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_as_draft")), action: {
                    print("Save As DRAFT")
                    self.saveDocument(status: DocumentDTO.DOC_STATUS_DRAFT)
                    self.presentationMode.wrappedValue.dismiss()
                }),
                .default(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_as_submit")), action: {
                    print("Save and SUBMIT")
                    self.saveDocument(status: DocumentDTO.DOC_STATUS_SUBMITTED)
                    self.presentationMode.wrappedValue.dismiss()
                }),
                .destructive(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_cancel")))
                ])
    }
    
    func getSaveAlert()  -> UIAlertController {
        let saveAlert = UIAlertController(title: "Change your profile image", message: nil, preferredStyle: .actionSheet)

        saveAlert.addAction(UIAlertAction(title: "Photo Library", style: .default, handler: nil))
        saveAlert.addAction(UIAlertAction(title: "Online Stock Library", style: .default, handler: nil))
        let cancel = UIAlertAction(title: "Cancel", style: .destructive, handler: nil)

        saveAlert.addAction(cancel)
        saveAlert.view.addSubview(UIView()) // I can't explain it, but it works!
        // view.present(saveAlert, animated:false)
        return saveAlert
    }
    
    /*
     Roll back the changes that were done to the docuemnt and revert back to the previous state
     */
    func rollbackAnyChanges(){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            
            //
            // revert pages that were deleted
            print("Rolling Back Changes: <my documents>")
            for i in 0 ..< sessionData.myDocumentList[index].pages.count {
                sessionData.myDocumentList[index].pages[i].deleted = false
            }
            // revert new pages that were created
            let newPages = sessionData.myDocumentList[index].pages.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <pages> \(newPages)")
            sessionData.myDocumentList[index].pages = newPages
            
            //
            // Revert Recipients
            for i in 0 ..< sessionData.myDocumentList[index].toRecipients.count {
                sessionData.myDocumentList[index].toRecipients[i].deleted = false
            }
            // revert new pages that were created
            let newRecipients = sessionData.myDocumentList[index].toRecipients.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <recipients> \(newRecipients)")
            sessionData.myDocumentList[index].toRecipients = newRecipients
            
            //
            // Revert Tags
            for i in 0 ..< sessionData.myDocumentList[index].tags.count {
                sessionData.myDocumentList[index].tags[i].deleted = false
            }
            // revert new pages that were created
            let newTags = sessionData.myDocumentList[index].tags.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <tags> \(newTags)")
            sessionData.myDocumentList[index].tags = newTags
            
            //
            // Revert Linked Docs
            for i in 0 ..< sessionData.myDocumentList[index].linkedDocuments.count {
                sessionData.myDocumentList[index].linkedDocuments[i].deleted = false
            }
            // revert new pages that were created
            let newLinkedDocs = sessionData.myDocumentList[index].linkedDocuments.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <linked> \(newLinkedDocs)")
            sessionData.myDocumentList[index].linkedDocuments = newLinkedDocs
            
            //
            // Revert Backing Docs
            for i in 0 ..< sessionData.myDocumentList[index].attachedDocuments.count {
                sessionData.myDocumentList[index].attachedDocuments[i].deleted = false
            }
            // revert new pages that were created
            let newAttachedDocs = sessionData.myDocumentList[index].attachedDocuments.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <attached> \(newAttachedDocs)")
            sessionData.myDocumentList[index].attachedDocuments = newAttachedDocs
        }
    }
    
    //
    // Load the page to teh server
    func loadPageImage(page: DocumentPage, sessionId : String) {
        self.saveImagePageToServer(page: page, sessionId : sessionId)
    }
    
    //
    // Load the page to teh server
    func deletePageImage(page: DocumentPage, sessionId : String) {
        self.deleteImagePageFromServer(page: page, sessionId : sessionId)
    }
    
    
    //
    // Save the image to the server
    func saveImagePageToServer(page: DocumentPage, sessionId : String){
        
        let boundary = UUID().uuidString
        let filename = "page.jpg"
        let urlString = RESTServer.REMOTE_SERVER_URL + RESTServer.REST_POST_SAVE_DOCUMENT_PAGE
        print("[saveImagePageToServer] \(urlString)")

        guard let url = URL(string: urlString)
        
        else {
            print("invalid URL")
            return
        }
        let config = URLSessionConfiguration.default
        let session = URLSession(configuration: config)
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        var data = Data()
        
        //
        // Add user name
        let fieldNameUser = "userName"
        let fieldValueUser = sessionData.userCredentials.username
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"\(fieldNameUser)\"\r\n\r\n".data(using: .utf8)!)
        data.append("\(fieldValueUser)".data(using: .utf8)!)
        
        //
        // Add Session ID
        let fieldNameSessionId = "sessionId"
        let fieldValueSessionId = sessionId
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"\(fieldNameSessionId)\"\r\n\r\n".data(using: .utf8)!)
        data.append("\(fieldValueSessionId)".data(using: .utf8)!)
        
        //
        // Add page number
        let fieldNamePageNumber = "pageNumber"
        let fieldValuePageNumber  = page.pageNumber
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"\(fieldNamePageNumber)\"\r\n\r\n".data(using: .utf8)!)
        data.append("\(fieldValuePageNumber)".data(using: .utf8)!)
        
        //
        //
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        data.append("Content-Type: image/jpg\r\n\r\n".data(using: .utf8)!)
        data.append(page.getFormEncodedPageImageData())

        // End the raw http request data, note that there is 2 extra dash ("-") at the end, this is to indicate the end of the data
        // According to the HTTP 1.1 specification https://tools.ietf.org/html/rfc7230
        data.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        
        session.uploadTask(with: request, from: data, completionHandler: {
            responseData, response, error in
            
            if(error != nil){
                print("\(error!.localizedDescription)")
            }
            
            guard let responseData = responseData else {
                print("no response data")
                return
            }
            
            if let responseString = String(data: responseData, encoding: .utf8) {
                print("uploaded to: \(responseString)")
            }


            let decodedPage = try! JSONDecoder().decode(DocumentPage.self, from: responseData)
            print("Page Response <doc> <create> \(decodedPage)")
            
            DispatchQueue.main.async {
                //
                // reinsert the id of the page into the main document
                if let documentIndex = self.sessionData.myDocumentList.firstIndex(where: {$0.syncID == sessionId}) {
                    // Find the specfcific page
                    if let pageIndex = self.sessionData.myDocumentList[documentIndex].pages.firstIndex(where: {$0.pageNumber == page.pageNumber}) {
                        self.sessionData.myDocumentList[documentIndex].pages[pageIndex].id = decodedPage.id
                    } else {
                        // item could not be found
                    }
                } else {
                    // item could not be found
                }
            }

        }).resume()
    }
    
     /*
     Delete the page image data from the server
     */
     func deleteImagePageFromServer(page: DocumentPage, sessionId : String){
         var jsonBody : Data?
         let urlString = RESTServer.fetchDocPagesDeleteURL(username: sessionData.userCredentials.username, sessionId: sessionId)
         print("[deleteImagePageFromServer] \(urlString)")

         guard let url = URL(string: urlString)
         
         else {
             print("invalid URL")
             return
         }
         
         var request = URLRequest(url : url)
         request.httpMethod = "DELETE"
         request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
         request.setValue("application/json", forHTTPHeaderField: "Content-Type")
         
         let jsonEncoder = JSONEncoder()
         jsonEncoder.outputFormatting = .withoutEscapingSlashes
         var pageIdData = [Int]()
         pageIdData.append(page.id)
         if let encoded = try? jsonEncoder.encode(pageIdData) {
             jsonBody = encoded
         }
         
         request.httpBody = jsonBody
         print("deleteImagePageFromServer \(jsonBody)")
        
        if let json = String(data: (jsonBody ?? nil)!, encoding: .utf8) {
            print("[deleteImagePageFromServer]: \(json)")
        }
         
         URLSession.shared.dataTask(with: request) {
         (data, response, error) in
         //print(data)
         //print("New Document Creation response: \(response.)")
         //print("New Document Creation response: \(error)")
         guard let data = data else { return }
             
             DispatchQueue.main.async {

             }

         }.resume()
     }
    
    // self.present(SaveDocumentActionSheet, animated:false)

}

struct DocumentDetails_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            DocumentDetails(documentCard : DocumentDTO())
            .environmentObject(ViewRouter())
            .environmentObject(SessionData())
        }
    }
}


