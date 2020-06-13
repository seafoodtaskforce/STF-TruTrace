//
//  ProfileDocumentDetail.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-09.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct ProfileDocumentDetail: View {
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
                    VStack {
                        VStack(alignment: .leading) {
                            Picker(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_doc_label_type"), selection: $docTypeIndex){
                                ForEach(0 ..< getDocTypeCount()){
                                    Text(self.sessionData.profileDocTypes[$0].value)
                                }
                            }
                            Text(LocalizationUtils.localizeDocumentStatus(text: documentCard.status)).font(.caption)
                            if(documentCard.status == DocumentDTO.DOC_STATUS_REJECTED
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
                        UserCell(recipient : recipient).environmentObject(self.sessionData)
                    }.onDelete(perform: deleteRecipientItems)
                }
                
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
                                ProfileDocInfoItemView(documentCard : self.documentCard
                                , docInfoDefinition: DocumentDataUtils.getDynamicFieldDefinition(definitionId : fieldDefinition.id, sessionData: self.sessionData)
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
    
    //
    // Delete page items (soft delete)
    func deletePageItems(at offsets: IndexSet){
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {

            for pageIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for page in sessionData.profileDocumentList[index].pages {
                    if page.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == pageIndex {
                            sessionData.profileDocumentList[index].pages[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Delete recipient items (soft delete)
    func deleteRecipientItems(at offsets: IndexSet){
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            for recipientIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for recipient in sessionData.profileDocumentList[index].toRecipients {
                    if recipient.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == recipientIndex {
                            sessionData.profileDocumentList[index].toRecipients[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Delete linked doc items (soft delete)
    func deleteLinkedDocItems(at offsets: IndexSet){
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            for linkedIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for linkedDoc in sessionData.profileDocumentList[index].linkedDocuments {
                    if linkedDoc.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == linkedIndex {
                            sessionData.profileDocumentList[index].linkedDocuments[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Delete backup doc items (soft delete)
    func deleteBackingDocItems(at offsets: IndexSet){
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            for attachedIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for backingDoc in sessionData.profileDocumentList[index].attachedDocuments {
                    if backingDoc.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == attachedIndex {
                            sessionData.profileDocumentList[index].attachedDocuments[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Delete tag items (soft delete)
    func deleteTagItems(at offsets: IndexSet){
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
             for tagIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for tag in sessionData.profileDocumentList[index].tags {
                    if tag.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == tagIndex {
                            sessionData.profileDocumentList[index].tags[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Get active doc pages
    func getDocumentPages() -> [DocumentPage]{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            var activePages = sessionData.profileDocumentList[index].pages.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            activePages.sort {
                $0.pageNumber < $1.pageNumber
            }
            return activePages
        }
        return [DocumentPage]()
    }
    
    //
    // Get active doc tags
    func getDocumentTags() -> [DocumentTag]{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            let activeTags = sessionData.profileDocumentList[index].tags.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeTags
        }
        return [DocumentTag]()
    }
    
    //
    // Get active backup docs
    func getDocumentBackingDocs() -> [DocumentDTO]{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            let activeAttachedDocs = sessionData.profileDocumentList[index].attachedDocuments.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeAttachedDocs
        }
        return [DocumentDTO]()
    }
    
    //
    // Get active linked docs
    func getDocumentLinkedDocs() -> [DocumentDTO]{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            let activeLinkedDocs = sessionData.profileDocumentList[index].linkedDocuments.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeLinkedDocs
        }
        return [DocumentDTO]()
    }
    
    //
    // Get active doc recipients
    func getDocumentRecipients() -> [Recipient]{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            let activeRecipients = sessionData.profileDocumentList[index].toRecipients.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeRecipients
        }
        return [Recipient]()
    }
    
    //
    // Get active linked docs
    func getDocumentLinked() -> [DocumentDTO]{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            let activeLinkedDocs = sessionData.profileDocumentList[index].linkedDocuments.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeLinkedDocs
        }
        return [DocumentDTO]()
    }
    
    //
    // Get active doc pages
    func getDocumentsAttached() -> [DocumentDTO]{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            let activeAttachedDocs = sessionData.profileDocumentList[index].attachedDocuments.filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeAttachedDocs
        }
        return [DocumentDTO]()
    }
    
    func getDocument() -> DocumentDTO{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            return sessionData.profileDocumentList[index]
        }
        return DocumentDTO()
    }
    
    private func getDocTypeIndex(docType : DocType){
        if let index = sessionData.profileDocTypes.firstIndex(of: docType) {
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
        
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {

            for page in sessionData.profileDocumentList[index].pages {
                if page.tempId == docPage.tempId { return pageIndex }
                if !page.deleted {pageIndex += 1 }
            }
        }
        
        return pageIndex
    }
    
    private func setCurrentDocType() {
        print("Doc Type State Index <set>: \($docTypeIndex)")
        print("Doc Type Index <START>")
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            if(index != nil && docTypeIndex >= 0){
                print("Doc Type Index <Process>")
                sessionData.profileDocumentList[index].type = sessionData.profileDocTypes[docTypeIndex]
                print("Doc Type Index <set>: \(index)")
                print("Doc Type Index <set> <data>: \(sessionData.profileDocTypes[docTypeIndex])")
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
        print("Adding a new page for doc id <list>: .\(sessionData.profileDocumentList.count)")
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            print("Adding a new page: .\(index)")
            print("Adding a new page <before>: .\(sessionData.profileDocumentList[index].pages.count)")
            sessionData.profileDocumentList[index].pages.append(DocumentPage(localImage: image, pageNumber: sessionData.profileDocumentList[index].pages.count))
            print("Adding a new page <after>: .\(sessionData.profileDocumentList[index].pages.count)")
        }
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
        return sessionData.profileDocTypes.count
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
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard){

           // add the document to the session
           print("Updating Existing Record in <saveDocument>: \(self.sessionData.profileDocumentList.count)")
           print("Updating Existing Record at index <saveDocument>: \(index)")
           
           //
           //self.sessionData.profileDocumentList.insert(self.sessionData.newWorkDocument, at: 0)
           //
           // Add instance data
           
           // timestamp
           let today = Date()
           let timestampFormatter = DateFormatter()
           timestampFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
           self.sessionData.profileDocumentList[index].creationTimestamp = timestampFormatter.string(from: today)
           print("New Document Date <saveDocument>: \(sessionData.profileDocumentList[index].creationTimestamp )")
           
           // was read
           self.sessionData.profileDocumentList[index].currentUserRead = true
           
           // Status
           self.sessionData.profileDocumentList[index].status = status
            
            //
            // remove the pages
            let pages = self.sessionData.profileDocumentList[index].pages
            self.sessionData.profileDocumentList[index].pages = [DocumentPage]()
           
           //
           // Upload
            RESTServer.uploadUpdatedDocument(index: index, pages : pages, sessionData : sessionData, docUITabDesignation : DocumentUITypeDesignation.profile)

           //self.viewRouter.currentPage = ViewRouter.MY_DOCUMENTS_TAB_VIEW
        }

    } // saveDocument
    
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
    
    /*
     Roll back the changes that were done to the docuemnt and revert back to the previous state
     */
    func rollbackAnyChanges(){
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            
            //
            // revert pages that were deleted
            print("Rolling Back Changes: <my documents>")
            for i in 0 ..< sessionData.profileDocumentList[index].pages.count {
                sessionData.profileDocumentList[index].pages[i].deleted = false
            }
            // revert new pages that were created
            let newPages = sessionData.profileDocumentList[index].pages.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <pages> \(newPages)")
            sessionData.profileDocumentList[index].pages = newPages
            
            //
            // Revert Recipients
            for i in 0 ..< sessionData.profileDocumentList[index].toRecipients.count {
                sessionData.profileDocumentList[index].toRecipients[i].deleted = false
            }
            // revert new pages that were created
            let newRecipients = sessionData.profileDocumentList[index].toRecipients.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <recipients> \(newRecipients)")
            sessionData.profileDocumentList[index].toRecipients = newRecipients
            
            //
            // Revert Tags
            for i in 0 ..< sessionData.profileDocumentList[index].tags.count {
                sessionData.profileDocumentList[index].tags[i].deleted = false
            }
            // revert new pages that were created
            let newTags = sessionData.profileDocumentList[index].tags.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <tags> \(newTags)")
            sessionData.profileDocumentList[index].tags = newTags
            
            //
            // Revert Linked Docs
            for i in 0 ..< sessionData.profileDocumentList[index].linkedDocuments.count {
                sessionData.profileDocumentList[index].linkedDocuments[i].deleted = false
            }
            // revert new pages that were created
            let newLinkedDocs = sessionData.profileDocumentList[index].linkedDocuments.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <linked> \(newLinkedDocs)")
            sessionData.profileDocumentList[index].linkedDocuments = newLinkedDocs
            
            //
            // Revert Backing Docs
            for i in 0 ..< sessionData.profileDocumentList[index].attachedDocuments.count {
                sessionData.profileDocumentList[index].attachedDocuments[i].deleted = false
            }
            // revert new pages that were created
            let newAttachedDocs = sessionData.profileDocumentList[index].attachedDocuments.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            print("Rolling Back Changes: <my documents> <attached> \(newAttachedDocs)")
            sessionData.profileDocumentList[index].attachedDocuments = newAttachedDocs
        }
    }
    
    //
    // Load the page to teh server
    func loadPageImage(page: DocumentPage, sessionId : String) {
        RESTServer.saveImagePageToServer(page: page, sessionId : sessionId, sessionData: sessionData, docUITabDesignation: DocumentUITypeDesignation.profile)
    }
    
    //
    // Load the page to teh server
    func deletePageImage(page: DocumentPage, sessionId : String) {
        RESTServer.deleteImagePageFromServer(page: page, sessionId : sessionId, sessionData: self.sessionData)
    }
    
    func getDocumentDocInfoDefinitions() -> [DynamicFieldDefinition]{
        if let index = sessionData.profileDocumentList.firstIndex(of: documentCard) {
            
            let activeFieldDefinitions = DocumentDataUtils.getDynamicFieldDefinitions(document : sessionData.profileDocumentList[index], sessionData : sessionData).filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeFieldDefinitions
        }
        return [DynamicFieldDefinition]()
    }

}

struct ProfileDocumentDetail_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ProfileDocumentDetail(documentCard : DocumentDTO())
            .environmentObject(ViewRouter())
            .environmentObject(SessionData())
        }
    }
}
