//
//  RejectionNoteSheetView.swift
//  TruTracePOC
//
//  Created by Piotr Paweska on 2020-02-25.
//  Copyright © 2020 Piotr Paweska. All rights reserved.
//

import SwiftUI

struct RejectionNoteSheetView: View {
    @EnvironmentObject var sessionData: SessionData
    @EnvironmentObject var viewRouter: ViewRouter
    @Environment(\.presentationMode) var presentationMode
    
    var documentCard : DocumentDTO
    
    @State private var noteHeader : String = ""
    @State private var noteBody : String = ""
    
    var body: some View {
        NavigationView {
            Form {
                    Section(
                    header: Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_header"))) {
                        TextField(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_header"), text: $noteHeader)
                    } // End Dates Section
                    
                    Section(
                    header: Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_body")),
                    footer : Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_footer"))) {
                        TextField(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_body"), text: $noteBody)
                        .lineLimit(4)
                    } // End Dates Section

            } // Form
            .navigationBarItems(
                leading:
                Button(action: {
                    self.presentationMode.wrappedValue.dismiss()
                }) {
                    HStack{
                       Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_button_cancel"))
                    }
                },
                trailing:
                Button(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_button_confirm")) {
                    self.setStatus(status: DocumentDTO.DOC_STATUS_REJECTED)
                    RESTServer.setDocStatus(status : DocumentDTO.DOC_STATUS_REJECTED, documentCard : self.documentCard, sessionData : self.sessionData)
                    RESTServer.addRejectionNote(document : self.setRejectionNote(), sessionData : self.sessionData)
                    self.presentationMode.wrappedValue.dismiss()
                    
                }.disabled(!canBeSaved()))
            .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_page_detail_document_reject_sheet_title")), displayMode: .inline)
        } // Navigation
    }
    
    func setStatus(status : String){
        if let index = sessionData.feedDocumentList.firstIndex(of: documentCard){
            print("<RejectionNoteSheetView> <set status> - local status")
            sessionData.feedDocumentList[index].status = status
        }
    }
    
    //
    // Check if the form can be saved
    func canBeSaved() -> Bool {

        if( self.noteHeader.isEmpty || self.noteHeader.count < 1
            || self.noteBody.isEmpty || self.noteBody.count < 1) { return false }
        
        return true
    }
    
    func setRejectionNote() -> DocumentDTO {
        var result : DocumentDTO = DocumentDTO()
        var rejectionNote : DocumentNote = DocumentNote()
        var rejectionNotes : [DocumentNote] = [DocumentNote]()
        
        rejectionNote.note = self.noteHeader + DocumentNote.NOTE_HEADER_SEPARATOR_TOKEN + self.noteBody
        if let index = sessionData.feedDocumentList.firstIndex(of: documentCard){
            print("<RejectionNoteSheetView> <set status> - local status")
            rejectionNotes.append(rejectionNote)
            sessionData.feedDocumentList[index].notes = rejectionNotes
            result = sessionData.feedDocumentList[index]
        }
        return result
    }
}

