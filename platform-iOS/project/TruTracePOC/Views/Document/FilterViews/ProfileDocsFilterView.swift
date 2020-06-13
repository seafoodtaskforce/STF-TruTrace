//
//  ProfileDocsFilterView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-29.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct ProfileDocsFilterView: View {
    @EnvironmentObject var sessionData: SessionData
    @Environment(\.presentationMode) var presentationMode
    
    @State private var docTypeIndex = 0
    @State private var userIndex = 0
    @State private var recipientIndex = 0
    @State private var tagIndex = 0
    @State private var docStatusIndex = 0
    
    var dateFormatter : DateFormatter {
        let formatter = DateFormatter()
        formatter.dateStyle = .long
        return formatter
    }
    
    // Filter State Data
    @State private var dateFrom = Date()
    @State private var dateTo = Date()
    
    @State private var dateRangeFrom: Date = Date()
    
    var body: some View {
        NavigationView {
            Form {
                //
                // Dates Section
                Section(
                header: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_section_label_date_range")),
                footer: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_section_label_date_range_footer")).font(.footnote))  {
                    DatePicker(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_label_date_range_from"), selection : $dateFrom, in: dateRangeFrom..., displayedComponents: .date)
                    DatePicker(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_label_date_range_to"), selection : $dateTo, in: ...Date(), displayedComponents: .date)
                } // End Dates Section
                 
                
                // Doc Type
                Section (
                header: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_section_label_doc_type")),
                footer: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_section_label_doc_type_footer")).font(.footnote)) {
                    Picker(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_label_doc_type"), selection: $docTypeIndex){
                        ForEach(0 ..< self.getDocTypeCount(), id: \.self){
                            Text(self.getDocTypeString(index: $0))
                        }
                    }
                } // End Doc Type Section
                
                // Users (Submitters)
                Section(
                header: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_section_label_submitter_recipient")),
                footer: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_section_label_submitter_recipient_footer")).font(.footnote))  {
                    Picker(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_label_submitter"), selection: $userIndex){
                        ForEach(0 ..< self.getUserCount(), id: \.self){
                            Text(self.getUserString(index: $0))
                        }
                    }
                    Picker(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_label_recipient"), selection: $recipientIndex){
                        ForEach(0 ..< self.getRecipientCount(), id: \.self){
                            Text(self.getRecipientString(index: $0))
                        }
                    }
                } // End USer and Recipient Section
                
                // Tag
                Section(
                header: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_section_label_tag")),
                footer: Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_section_label_tag_footer")).font(.footnote))  {
                    Picker(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_label_tag"), selection: $tagIndex){
                        ForEach(0 ..< self.getTagCount(), id: \.self){
                            Text(self.getTagString(index: $0))
                        }
                    }
                } // End Doc Type Section
                
            } // End Form
            .navigationBarItems(
                trailing :
                Button(action: {
                    self.presentationMode.wrappedValue.dismiss()
                }) {
                    HStack{
                       Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_navigation_button_done"))
                    }
                    
                })
            .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_profile_docs_filter_page_navigation_header")), displayMode: .inline)
        } // End Navigation
            .onAppear(){
                self.initDataFromFilter()
        }
            .onDisappear(){
                print("<ProfileDocsFilterView> is dismissed")
                self.setFilter()
        }
        
        
    } // End View Body
    
    private func getDocTypeCount() -> Int {
        print("<ProfileDocsFilterView> Showing Filter with Doc Types#  \(sessionData.profileDocTypes.count)")
        return sessionData.profileDocTypes.count + 1
    }
    
    private func getUserCount() -> Int {
        print("<ProfileDocsFilterView> Showing Filter with users# \(sessionData.allRecipients.count)")
        return sessionData.allRecipients.count + 1
    }
    
    private func getRecipientCount() -> Int {
        print("<ProfileDocsFilterView> Showing Filter with recipients# \(sessionData.allRecipients.count)")
        return sessionData.allRecipients.count + 1
    }
    
    private func getTagCount() -> Int {
        print("<ProfileDocsFilterView> Showing Filter with tags# \(sessionData.allTags.count)")
        return sessionData.allTags.count + 1
    }
    
    private func getDocTypeString(index : Int) -> String {
        if(index == 0){
            return DocType.NULL_VALUE
        }
        return self.sessionData.profileDocTypes[index-1].value
    }
    
    private func getUserString(index : Int) -> String {
        if(index == 0){
            return Recipient.NULL_VALUE
        }
        return self.sessionData.allRecipients[index-1].name
    }
    
    private func getRecipientString(index : Int) -> String {
        if(index == 0){
            return Recipient.NULL_VALUE
        }
        return self.sessionData.allRecipients[index-1].name
    }
    
    private func getTagString(index : Int) -> String {
        if(index == 0){
            return DocumentTag.NULL_VALUE
        }
        return self.sessionData.allTags[index-1].text
    }
    
    
    private func getDocType(index : Int) -> DocType {
        if(index == 0){
            return DocType.getNullValue()
        }
        return self.sessionData.profileDocTypes[index-1]
    }
    
    private func getUser(index : Int) -> Recipient {
        if(index == 0){
            return Recipient.getNullValue()
        }
        return self.sessionData.allRecipients[index-1]
    }
    
    private func getRecipient(index : Int) -> Recipient {
        if(index == 0){
            return Recipient.getNullValue()
        }
        return self.sessionData.allRecipients[index-1]
    }
    
    private func getTag(index : Int) -> DocumentTag {
        if(index == 0){
            return DocumentTag.getNullValue()
        }
        return self.sessionData.allTags[index-1]
    }
    
    private func initDataFromFilter(){
        if(self.sessionData.profileDocumentList.count > 0) {
            dateRangeFrom = self.sessionData.profileDocumentList[self.sessionData.profileDocumentList.count - 1].creationTimestampDate
        }
        // Rest of data
        self.dateFrom = sessionData.profileDocsFilter.dateFrom
        self.dateTo = sessionData.profileDocsFilter.dateTo
        //
        // indeces
        self.docTypeIndex = sessionData.profileDocsFilter.docTypeIndex
        self.recipientIndex = sessionData.profileDocsFilter.recipientIndex
        self.userIndex = sessionData.profileDocsFilter.userIndex
        self.tagIndex = sessionData.profileDocsFilter.tagIndex
    }
    
    private func setFilter(){
        sessionData.profileDocsFilter.dateFrom = self.dateFrom
        sessionData.profileDocsFilter.dateTo = self.dateTo
        sessionData.profileDocsFilter.documentType = self.getDocType(index : docTypeIndex)
        sessionData.profileDocsFilter.recipient = self.getRecipient(index : recipientIndex)
        sessionData.profileDocsFilter.submitter = self.getUser(index : userIndex)
        sessionData.profileDocsFilter.tag = self.getTag(index : tagIndex)
        //
        // indeces
        sessionData.profileDocsFilter.docTypeIndex = docTypeIndex
        sessionData.profileDocsFilter.recipientIndex = recipientIndex
        sessionData.profileDocsFilter.userIndex = userIndex
        sessionData.profileDocsFilter.tagIndex = tagIndex
    }
}

struct ProfileDocsFilterView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileDocsFilterView().environmentObject(SessionData())
    }
}

