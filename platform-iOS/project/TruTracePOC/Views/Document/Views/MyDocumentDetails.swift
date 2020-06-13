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
                        Text("\(self.getDocumentPages(useDeletedFlag : true).count)")
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
                    if(isEditable()){
                        ForEach(getDocumentPages(useDeletedFlag : true), id: \.pageNumber) { page in
                            DocumentPageCell(page : page, pageIndex: self.getDocumentPageIndex(docPage: page))
                        }.onDelete(perform: deletePageItems)
                    }else{
                        ForEach(getDocumentPages(useDeletedFlag : true), id: \.pageNumber) { page in
                            DocumentPageCell(page : page, pageIndex: self.getDocumentPageIndex(docPage: page))
                        }
                    }
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
                    if(isEditable()){
                        ForEach(self.getDocumentRecipients(), id: \.id){ recipient in
                            UserCell(recipient : recipient).environmentObject(self.sessionData)
                        }.onDelete(perform: deleteRecipientItems)
                    }else{
                        ForEach(self.getDocumentRecipients(), id: \.id){ recipient in
                            UserCell(recipient : recipient).environmentObject(self.sessionData)
                        }
                    }
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
                                DocInfoItemView(documentCard : self.documentCard
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
                    Text("\(getDocumentTags().count)")
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
                    if(isEditable()){
                        ForEach(getDocumentTags(), id: \.id) { tag in
                            Text(tag.text)
                        }.onDelete(perform: deleteTagItems)
                    }else{
                        ForEach(getDocumentTags(), id: \.id) { tag in
                            Text(tag.text)
                        }
                    }
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
                    if(isEditable()){
                        ForEach(getDocumentLinkedDocs()){ linkedDoc in
                            TraceDocCell(docData : linkedDoc)
                        }.onDelete(perform: deleteLinkedDocItems)
                    }else{
                        ForEach(getDocumentLinkedDocs()){ linkedDoc in
                            TraceDocCell(docData : linkedDoc)
                        }
                    }
                }
                
                //
                // Backing Docs
                Section (header : HStack{
                    Text(LocalizationUtils.localizeString(text: "ios_profile_docs_detail_page_section_label_backup_docs"))
                    Text("\(getDocumentBackingDocs().count)")
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
                    if(isEditable()){
                        ForEach(getDocumentBackingDocs()) { backingDoc in
                            TraceDocCell(docData : backingDoc)
                        }.onDelete(perform: deleteBackingDocItems)
                    }else{
                        ForEach(getDocumentBackingDocs()) { backingDoc in
                            TraceDocCell(docData : backingDoc)
                        }
                    }
                    
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
    
    func deletePageItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            print("Removing <Adding Pages> Pages: Offsets \(offsets)")

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
                            if(isEditable()){
                                sessionData.myDocumentList[index].pages[realIndex].deleted = true
                                print("Removing <Adding Pages> Pages: Delete \(realIndex)")
                                print("Removing <Adding Pages> Pages <id>: Delete \(sessionData.myDocumentList[index].pages[realIndex].id)")
                                print("Removing <Adding Pages> Pages <all>: Delete \(sessionData.myDocumentList[index].pages)")
                                
                                
                            }
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Delete
    func deleteRecipientItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            for recipientIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for recipient in sessionData.myDocumentList[index].toRecipients {
                    if recipient.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == recipientIndex {
                            sessionData.myDocumentList[index].toRecipients[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Delete
    func deleteLinkedDocItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            for linkedIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for linkedDoc in sessionData.myDocumentList[index].linkedDocuments {
                    if linkedDoc.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == linkedIndex {
                            sessionData.myDocumentList[index].linkedDocuments[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Delete
    func deleteBackingDocItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            for attachedIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for backingDoc in sessionData.myDocumentList[index].attachedDocuments {
                    if backingDoc.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == attachedIndex {
                            sessionData.myDocumentList[index].attachedDocuments[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Delete
    func deleteTagItems(at offsets: IndexSet){
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
             for tagIndex in offsets {
                // Start Index
                var realIndex = 0
                var skipCount = 0
                // Skip over the deleted pages
                for tag in sessionData.myDocumentList[index].tags {
                    if tag.deleted == true {
                        skipCount += 1
                    }else{
                        if (realIndex - skipCount) == tagIndex {
                            sessionData.myDocumentList[index].tags[realIndex].deleted = true
                        }
                    }
                    realIndex += 1
                }
            }
        }
    }
    
    //
    // Get the doc pages
    func getDocumentPages(useDeletedFlag : Bool) -> [DocumentPage]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            var activePages : [DocumentPage] = [DocumentPage]()
            if(useDeletedFlag){
                activePages = sessionData.myDocumentList[index].pages.filter {
                    switch $0.deleted {
                        case true: return false
                        default: return true
                    }
                }
            }else{
                activePages = sessionData.myDocumentList[index].pages
            }
            // sort the data
            activePages.sort {
                $0.pageNumber < $1.pageNumber
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
    
    func getDocumentDocInfoDefinitions() -> [DynamicFieldDefinition]{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            
            let activeFieldDefinitions = DocumentDataUtils.getDynamicFieldDefinitions(document : sessionData.myDocumentList[index], sessionData : sessionData).filter {
                switch $0.deleted {
                    case true: return false
                    default: return true
                }
            }
            return activeFieldDefinitions
        }
        return [DynamicFieldDefinition]()
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

        for page in self.getDocumentPages(useDeletedFlag : true) {
            if page.tempId == docPage.tempId { return pageIndex }
            if !page.deleted {pageIndex += 1 }
        }
        
        return pageIndex
    }
    
    
    //
    //
    // Set the current type of the document
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
            sessionData.myDocumentList[index].pages.append(DocumentPage(localImage: image, pageNumber: DocumentDataUtils.getNextPageIndex(document : sessionData.myDocumentList[index])))
            print("Adding a new page <after>: .\(sessionData.myDocumentList[index].pages.count)")
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
    // Save Existing Document in Backend
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
            print("New Document Pages <saveDocument>: \(sessionData.myDocumentList[index].pages)")
            
            //
            // Remove the deleted elements
            
            // remove recipients
            self.sessionData.myDocumentList[index].toRecipients = self.getDocumentRecipients()
            // remove tags
            self.sessionData.myDocumentList[index].tags = self.getDocumentTags()
            // remove linked
            self.sessionData.myDocumentList[index].linkedDocuments = self.getDocumentLinked()
            // remove backup
            self.sessionData.myDocumentList[index].attachedDocuments = self.getDocumentBackingDocs()
            
            //
            // Upload
            RESTServer.uploadUpdatedDocument(index: index, pages : pages, sessionData : sessionData, docUITabDesignation : DocumentUITypeDesignation.my)

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
            print("Rolling Back Changes: <my documents - pages>")
            for i in 0 ..< sessionData.myDocumentList[index].pages.count {
                sessionData.myDocumentList[index].pages[i].deleted = false
            }
            print("Rolling Back Changes: <my documents - pages> <before> \(sessionData.myDocumentList[index].pages)")
            // revert new pages that were created
            let newPages = sessionData.myDocumentList[index].pages.filter {
                switch $0.id {
                    case 0: return false
                    case -1: return false
                    default: return true
                }
            }
            
            sessionData.myDocumentList[index].pages = newPages
            print("Rolling Back Changes: <my documents - pages> <after> \(sessionData.myDocumentList[index].pages)")
            
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
    // Load the page to the server
    func loadPageImage(page: DocumentPage, sessionId : String) {
        RESTServer.saveImagePageToServer(page: page, sessionId : sessionId, sessionData: sessionData, docUITabDesignation: DocumentUITypeDesignation.my)
    }
    
    //
    // Delete the page from the server
    func deletePageImage(page: DocumentPage, sessionId : String) {
        RESTServer.deleteImagePageFromServer(page: page, sessionId : sessionId, sessionData: self.sessionData)
    }
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


