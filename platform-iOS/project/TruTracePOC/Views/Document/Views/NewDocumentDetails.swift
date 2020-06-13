//
//  NewDocumentDetails.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-13.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct NewDocumentDetails: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    
    @State private var inputImage: UIImage = UIImage()
    @State private var showingImagePicker = false
    @State private var isShowingScannerSheet = false
    @State private var showingRecipientsList = false
    @State private var showingLinkedDocsList = false
    @State private var showingBackingDocsList = false
    @State private var showingTagsList = false
    @State private var docTypeIndex = -1
    @State private var showSaveActionSheet = false
    @State private var showNotes = false
    
    // temp document
    var documentCard : DocumentDTO
    
    static let pillColor: Color =  Color.blue

    var body: some View {
        
        NavigationView {
            Form {
                //
                // Summary section
                Section (header : Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_summary"))){

                    VStack {
                        
                        /* *******************************
                         Document Type and Status Section
                         */
                        VStack(alignment: .leading) {
                            //
                            // Pick the Document Type
                            Picker(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_doc_label_type"), selection: $docTypeIndex){
                                ForEach(0 ..< getDocTypeCount()){
                                  Text(self.sessionData.passthroughDocTypes[$0].value)
                              }
                            }
                            // Document Status
                            Text(LocalizationUtils.localizeDocumentStatus(text: documentCard.status)).font(.caption)
                            if(self.getDocument().status == "REJECTED"
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
                            //self.setCurrentDocType()
                        }
                        //
                        // SHow notes if available
                        if(showNotes){
                            VStack{
                                HStack {
                                    Text("Header")
                                    Spacer()
                                    Text(self.getDocument().notes[0].getHeader())
                                }
                                HStack {
                                    Text("Body")
                                    Spacer()
                                    Text(self.getDocument().notes[0].getBody())
                                }
                            }
                        } // <Show Notes>
                    }
                }
                // <Summary section>
                
                
                //
                // Gallery sections
                Section (header :
                    HStack{
                        Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_page_gallery"))
                        Text("\(self.getDocumentPages(useDeletedFlag : false).count)")
                            .font(.caption)
                            .fontWeight(.black)
                            .padding(5)
                            .background(Self.pillColor)
                            .clipShape(Circle())
                            .foregroundColor(.white)
                        Spacer()
                        HStack(spacing: 20) {
                            //
                            // Scanner Options
                            Button(action: openCamera) {
                                Image(systemName: "camera").resizable()
                                .frame(width:20.0, height: 20.0)
                            }.sheet(isPresented: self.$isShowingScannerSheet) { self.makeScannerView().environmentObject(self.sessionData)
                            }
                            //
                            // Gallery Image Options
                            Button(action: {
                                // add another image to the mix
                                self.inputImage = UIImage()
                                self.showingImagePicker = true
                            }) {
                                Image(systemName: "photo.on.rectangle").resizable()
                                .frame(width:20.0, height: 20.0)
                            }.sheet(isPresented: $showingImagePicker, onDismiss: loadImage) {
                                ImagePicker(image: self.$inputImage)
                            }
                        }
                    }){
                    ForEach(getDocumentPages(useDeletedFlag : true), id: \.pageNumber) { page in
                        DocumentPageCell(page : page, pageIndex : self.getDocumentPageIndex(docPage: page))
                    }.onDelete(perform: deletePageItems)
                }
                // <Gallery sections>
                
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
                        }.sheet(isPresented: self.$showingRecipientsList) {
                            RecipientChooser(documentCard : self.documentCard)
                                .environmentObject(self.viewRouter)
                                .environmentObject(self.sessionData) }
                    }){
                    ForEach(getDocumentRecipients(), id: \.id) { recipient in
                        UserCell(recipient : recipient).environmentObject(self.sessionData)
                    }.onDelete(perform: deleteRecipientItems)
                }
                // Recipients Section
                
                //
                // Doc Info section
                Section (header :
                    HStack{
                        Text(LocalizationUtils.localizeString(text: "ios_my_docs_page_dynamic_doc_info"))
                        Text("\(self.getDocumentDocInfoDefinitions().count)")
                        .font(.caption)
                        .fontWeight(.black)
                        .padding(5)
                        .background(Self.pillColor)
                        .clipShape(Circle())
                        .foregroundColor(.white)
                        Spacer()
                    }){
                        List(self.getDocumentDocInfoDefinitions()) { fieldDefinition in
                            NavigationLink(
                            destination:
                                NewDocInfoItemView(docInfoDefinition: DocumentDataUtils.getDynamicFieldDefinition(definitionId : fieldDefinition.id, sessionData: self.sessionData)
                            ).environmentObject(self.sessionData)) {
                                HStack {
                                    Text(fieldDefinition.displayName + ": ")
                                        .font(.headline)
                                    Text(DocumentDataUtils.getDynamicFieldValue(document : self.getDocument(), dynamicFieldDefinitionId : fieldDefinition.id))
                                        .font(.footnote)
                                        .fontWeight(.regular)
                                }
                            }
                    }
                }
                
                //
                // Tags section
                Section (header : HStack{
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_tags"))
                    Text("\(self.getDocument().tags.count)")
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
                    }.sheet(isPresented: self.$showingTagsList) {
                        TagsChooser(documentCard : self.getDocument())
                            .environmentObject(self.viewRouter)
                            .environmentObject(self.sessionData) }
                    
                }){
                    ForEach(getDocumentTags(), id: \.id) { tag in
                        Text(tag.text)
                    }.onDelete(perform: deleteTagItems)
                }
                // Tag Sections
                
                
                
                
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
                    }.sheet(isPresented: self.$showingLinkedDocsList) {
                        LinkedDocChoser(documentCard : self.getDocument())
                            .environmentObject(self.viewRouter)
                            .environmentObject(self.sessionData) }
                }){
                    ForEach(getDocumentLinkedDocs()){ linkedDoc in
                        TraceDocCell(docData : linkedDoc)
                    }.onDelete(perform: deleteLinkedDocItems)
                }
                // Linked Docs
                
                
                //
                // Backing Docs
                Section (header : HStack{
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_backup_docs"))
                    Text("\(sessionData.newWorkDocument.attachedDocuments.count)")
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
                    }.sheet(isPresented: self.$showingBackingDocsList) {
                        BackingDocsChooser(documentCard : self.getDocument())
                            .environmentObject(self.viewRouter)
                            .environmentObject(self.sessionData) }
                }){
                    ForEach(getDocumentBackingDocs()) { backingDoc in
                        TraceDocCell(docData : backingDoc)
                    }.onDelete(perform: deleteBackingDocItems)
                }
                // Backing Docks
  
            }
            // Form
            .listStyle(GroupedListStyle())
            .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_new_profile_docs_detail_page_navigation_header")), displayMode: .inline)
            .navigationBarItems(
                leading:
                HStack {
                    Button(action: {
                        // add another image to the mix
                        self.viewRouter.currentTabView = 3
                        self.viewRouter.currentPage = ViewRouter.MY_DOCUMENTS_TAB_VIEW
                    }) {
                        Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_navigation_button_cancel"))
                    }
                }
                ,
                trailing:
                HStack {
                    Button(action: {
                        self.showSaveActionSheet.toggle()
                    }){
                        Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_navigation_button_save"))
                    }
                    .actionSheet(isPresented: $showSaveActionSheet) {
                        SaveDocumentActionSheet
                    }
                })
        //
        }
    } // View
    
    
    /*
     Sort the visible pages
     */
    func getSortedPages() -> [DocumentPage] {
        // Sort by page number
        let sortedPages = self.getDocument().pages.sorted{
            $0.pageNumber < $1.pageNumber
        }
        return sortedPages
    }

    /*
     Check if there is a note for this docuemnt
     */
    func isThereNote(doc : DocumentDTO) -> Bool {
        if doc.notes.count == 0 { return false } else {
            return true
        }
    }
    
    /*
     Delete the chosen page from List
     */
    func deletePageItems(at offsets: IndexSet){
        //sessionData.newWorkDocument.pages.remove(atOffsets: offsets)
        
        for pageIndex in offsets {
            print("Removing <Adding Pages> Pages: Offsets <index> \(pageIndex)")
            // Start Index
            var realIndex = 0
            var skipCount = 0
            // Skip over the deleted pages
            for page in self.getDocumentPages(useDeletedFlag : false) {
                if page.deleted == true {
                    skipCount += 1
                }else{
                    if (realIndex - skipCount) == pageIndex {
                            sessionData.newWorkDocument.pages[realIndex].deleted = true
                            print("Removing <Adding Pages> Pages: Delete \(realIndex)")
                            print("Removing <Adding Pages> Pages <id>: Delete \(sessionData.newWorkDocument.pages[realIndex].id)")
                            print("Removing <Adding Pages> Pages <all>: Delete \(sessionData.newWorkDocument.pages)")
                    }
                }
                realIndex += 1
            }
        }
    }
    
    /*
     Delete the recipient from List
     */
    func deleteRecipientItems(at offsets: IndexSet){
        for recipientIndex in offsets {
            // Start Index
            var realIndex = 0
            var skipCount = 0
            // Skip over the deleted pages
            for recipient in sessionData.newWorkDocument.toRecipients {
                if recipient.deleted == true {
                    skipCount += 1
                }else{
                    if (realIndex - skipCount) == recipientIndex {
                        sessionData.newWorkDocument.toRecipients[realIndex].deleted = true
                    }
                }
                realIndex += 1
            }
        }
    }
    
    /*
     Delete Linked Doc from List
     */
    func deleteLinkedDocItems(at offsets: IndexSet){
        for linkedIndex in offsets {
            // Start Index
            var realIndex = 0
            var skipCount = 0
            // Skip over the deleted pages
            for linkedDoc in sessionData.newWorkDocument.linkedDocuments {
                if linkedDoc.deleted == true {
                    skipCount += 1
                }else{
                    if (realIndex - skipCount) == linkedIndex {
                        sessionData.newWorkDocument.linkedDocuments[realIndex].deleted = true
                    }
                }
                realIndex += 1
            }
        }
    }
    
    /*
     Delete backing docks from List
     */
    func deleteBackingDocItems(at offsets: IndexSet){
        for attachedIndex in offsets {
            // Start Index
            var realIndex = 0
            var skipCount = 0
            // Skip over the deleted pages
            for backingDoc in sessionData.newWorkDocument.attachedDocuments {
                if backingDoc.deleted == true {
                    skipCount += 1
                }else{
                    if (realIndex - skipCount) == attachedIndex {
                        sessionData.newWorkDocument.attachedDocuments[realIndex].deleted = true
                    }
                }
                realIndex += 1
            }
        }
    }
    
    /*
     Delete Tag Item from List
     */
    func deleteTagItems(at offsets: IndexSet){
        for tagIndex in offsets {
            // Start Index
            var realIndex = 0
            var skipCount = 0
            // Skip over the deleted pages
            for tag in sessionData.newWorkDocument.tags {
                if tag.deleted == true {
                    skipCount += 1
                }else{
                    if (realIndex - skipCount) == tagIndex {
                        sessionData.newWorkDocument.tags[realIndex].deleted = true
                    }
                }
                realIndex += 1
            }
        }
    }
    
    /*
     Get the document pages for this doc
     */
    func getDocumentPages(useDeletedFlag : Bool) -> [DocumentPage]{
        var activePages : [DocumentPage] = [DocumentPage]()
        
        if(useDeletedFlag){
            activePages = sessionData.newWorkDocument.pages.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
        }else{
            activePages = sessionData.newWorkDocument.pages
        }
        return activePages
    }
    
    /*
     Get the current index of this page
     */
    func getDocumentPageIndex(docPage : DocumentPage) -> Int {
        var index = 0
        
        for page in sessionData.newWorkDocument.pages {
            if page.tempId == docPage.tempId { return index }
            if !page.deleted {index += 1 }
        }
        
        return index
    }
    
    func getDocumentTags() -> [DocumentTag]{
        let activeTags = sessionData.newWorkDocument.tags.filter {
            switch $0.deleted {
                case true: return false
                default: return true
            }
        }
        return activeTags
    }
    
    func getDocumentBackingDocs() -> [DocumentDTO]{
        let activeAttachedDocs = sessionData.newWorkDocument.attachedDocuments.filter {
            switch $0.deleted {
                case true: return false
                default: return true
            }
        }
        return activeAttachedDocs
    }
    
    func getDocumentLinkedDocs() -> [DocumentDTO]{
        let activeLinkedDocs = sessionData.newWorkDocument.linkedDocuments.filter {
            switch $0.deleted {
                case true: return false
                default: return true
            }
        }
        return activeLinkedDocs
    }
    
    func getDocumentRecipients() -> [Recipient]{
        let activeRecipients = sessionData.newWorkDocument.toRecipients.filter {
            switch $0.deleted {
                case true: return false
                default: return true
            }
        }
        return activeRecipients
    }
    
    func getDocumentLinked() -> [DocumentDTO]{
        let activeLinkedDocs = sessionData.newWorkDocument.linkedDocuments.filter {
            switch $0.deleted {
                case true: return false
                default: return true
            }
        }
        return activeLinkedDocs
    }
    
    func getDocumentsAttached() -> [DocumentDTO]{
        let activeAttachedDocs = sessionData.newWorkDocument.attachedDocuments.filter {
            switch $0.deleted {
                case true: return false
                default: return true
            }
        }
        return activeAttachedDocs
    }
    
    func getDocument() -> DocumentDTO{
        return sessionData.newWorkDocument
    }
    
    private func getDocTypeIndex(docType : DocType){
        if let index = sessionData.passthroughDocTypes.firstIndex(of: docType) {
            self.docTypeIndex = index
            print("Doc Type Index <get>: \(index)")
            print("Doc Type Index <get> <data>: \(docType)")
        }
    }
    
    private func setCurrentDocType() {
        print("Doc Type State Index <set>: \($docTypeIndex)")
        print("Doc Type Index <START>")
        if(docTypeIndex >= 0){
                print("Doc Type Index <Process>")
                sessionData.newWorkDocument.type = sessionData.passthroughDocTypes[docTypeIndex]
                sessionData.newWorkDocument.documentType = sessionData.newWorkDocument.type.value
                print("Doc Type Index <set> <data>: \(sessionData.passthroughDocTypes[docTypeIndex])")
        }else{
                print("no values for doctype")
                docTypeIndex = 0
        }
        print("Doc Type Index <END>")
    }
    
    func loadImage() {
        if inputImage.size.height > 0 {
            addNewPage(image: inputImage) }
    }
    
    func addNewPage(image : UIImage){
        print("Adding a new page for doc id: .\(sessionData.newWorkDocument.id)")
        print("Adding a new page for doc id <list>: .\(sessionData.myDocumentList.count)")
        print("Adding a new page <before>: .\(sessionData.newWorkDocument.pages.count)")
        sessionData.newWorkDocument.pages.append(DocumentPage(localImage: image, pageNumber: sessionData.newWorkDocument.pages.count))
            print("Adding a new page <after>: .\(sessionData.newWorkDocument.pages.count)")
    }
    
    private func openCamera() {
        isShowingScannerSheet = true
    }
     
    private func makeScannerView() -> ScannerView {
        ScannerView(completion: { textPerPage in
            self.isShowingScannerSheet = false
        }, document: documentCard)
    }
    
    private func getDocTypeCount() -> Int {
        return sessionData.passthroughDocTypes.count
    }

    //
    // Save New Document in Backend
    func saveDocument(status: String) {
        // add the document to the session
        print("Inserting New Record into: \(self.sessionData.myDocumentList.count)")
        
        self.sessionData.newWorkDocumentFlag = false
        
        
        //
        //self.sessionData.myDocumentList.insert(self.sessionData.newWorkDocument, at: 0)
        //
        // Add instance data
        
        // timestamp
        let today = Date()
        let timestampFormatter = DateFormatter()
        timestampFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        self.sessionData.newWorkDocument.creationTimestamp = timestampFormatter.string(from: today)
        print("New Document Date: \(self.sessionData.newWorkDocument.creationTimestamp )")
        
        // was read
        self.sessionData.newWorkDocument.currentUserRead = true
        
        // Status
        self.sessionData.newWorkDocument.status = status
        
        // owner
        self.sessionData.newWorkDocument.owner = self.sessionData.userCredentials.username

        // Group
        self.sessionData.newWorkDocument.groupName = self.sessionData.userGroups[0].name
        self.sessionData.newWorkDocument.organizationId = self.sessionData.userGroups[0].organizationId
        self.sessionData.newWorkDocument.groupId = self.sessionData.userGroups[0].id
        
        // Document Type
        self.setCurrentDocType()
        
        //
        // remove the pages
        let pages = self.sessionData.newWorkDocument.pages
        self.sessionData.newWorkDocument.pages = [DocumentPage]()
        
        //
        // upload the doc if we are online
        if(self.sessionData.isOnlineFlag) {
            //
            // Upload the file to the server
            //self.uploadNewDocument(document : self.sessionData.newWorkDocument.encodeToSend(), pages : pages)
            RESTServer.uploadNewDocument(document : self.sessionData.newWorkDocument, pages : pages, sessionData: self.sessionData, docUITabDesignation : DocumentUITypeDesignation.my)
            
        }else{
            //
            // Save the serialized file to the phone
            self.serializeLocallyNewDocument(document : self.sessionData.newWorkDocument, pages : pages)
        }
        

        // Re-route
        self.viewRouter.currentTabView = 3
        self.viewRouter.currentPage = ViewRouter.MY_DOCUMENTS_TAB_VIEW

    } // saveDocument
    
    //
    //
    // Serialize the new docuemnt to the phone media
    func serializeLocallyNewDocument(document : DocumentDTO, pages : [DocumentPage]) {
        //
        // Put the pages back
        self.sessionData.newWorkDocument.pages = pages
        
        //
        // There is no id to put in
        self.sessionData.newWorkDocument.id = 0
        
        //
        // get the new document id and add it to the session docuemnt
        self.sessionData.myDocumentList.insert(self.sessionData.newWorkDocument, at: 0)
        
        //
        // Serialize the document as JSON
        let encoder = JSONEncoder()
        if let encoded = try? encoder.encode(self.sessionData.newWorkDocument) {
            let defaults = UserDefaults.standard
            defaults.set(encoded,
                         forKey: (DocumentDTO.DOC_SERIALIZATION_PREFIX +  self.sessionData.newWorkDocument.syncID))
        }
        
        //
        // send each page separately to the server
        for docPage in pages {
            print("Pages <doc> <create> \(docPage)")
            self.serializeImagePageLocally(page: docPage, sessionId : self.sessionData.newWorkDocument.syncID)
        }
    }
    
    
    
    //
    //
    // Action Sheet for saving a new doc
    var SaveDocumentActionSheet: ActionSheet {
        ActionSheet(title: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_title")), message: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_message")), buttons: [
                .default(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_as_draft")), action: {
                    print("Save As DRAFT")
                    self.saveDocument(status: DocumentDTO.DOC_STATUS_DRAFT)
                    
                }),
                .default(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_as_submit")), action: {
                    print("Save and SUBMIT")
                    self.saveDocument(status: DocumentDTO.DOC_STATUS_SUBMITTED)
                    
                }),
                .destructive(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_action_save_cancel")))
                ])
    }
    
    
    //
    // Load the page to teh server
    func loadPageImage(page: DocumentPage, sessionId : String) {
        RESTServer.saveImagePageToServer(page: page, sessionId : sessionId, sessionData: sessionData, docUITabDesignation: DocumentUITypeDesignation.my)
    }
    
    //
    // Serialize the page locally to the phone
    func serializeImagePageLocally(page: DocumentPage, sessionId : String) {
        //
        //
        let pageName = DocumentDTO.DOC_SERIALIZATION_PREFIX
            + sessionId + "." + "page."
            + String(page.pageNumber)
        
        let data = page.getFormEncodedPageImageData()
        let filename = ImageUtils.getDocumentsDirectory().appendingPathComponent("\(pageName).png")
        print("<offline storage> page name: \(filename)")

        //
        // Save the page to the phone
        try? data.write(to: filename)
    }
    
    func getDocumentDocInfoDefinitions() -> [DynamicFieldDefinition]{
            
        let activeFieldDefinitions = DocumentDataUtils.getDynamicFieldDefinitions(document : sessionData.newWorkDocument, sessionData : sessionData).filter {
            switch $0.deleted {
                case true: return false
                default: return true
            }
        }
        return activeFieldDefinitions

    }

} // Struct

struct NewDocumentDetails_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            DocumentDetails(documentCard : DocumentDTO())
            .environmentObject(ViewRouter())
            .environmentObject(SessionData())
        }
    }
}


