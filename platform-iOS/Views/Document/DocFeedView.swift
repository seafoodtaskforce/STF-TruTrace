//
//  DocFeedView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-03.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct DocFeedView: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    
    @State private var showFilterDocsSheet = false
    @State private var filterIsOn = true
    
    var body: some View {
        NavigationView {
            List{
                ForEach(sessionData.feedDocumentList.filter{self.filterDocumentItem(element : $0)}){doc in
                    DocumentCardCell(documentCard: doc)
                }
            }.navigationBarTitle(LocalizationUtils.localizeString(text: "ios_feed_docs_page_navigation_header"))
            .navigationBarItems(
            trailing:
            Button(action: {
                self.showFilterDocsSheet.toggle()
            }) {
                HStack{
                   Text(LocalizationUtils.localizeString(text: "ios_feed_docs_page_navigation_button_filter"))
                    Toggle(isOn : $filterIsOn){
                        Text("")
                    }
                }
                
            }.sheet(isPresented: self.$showFilterDocsSheet) {
                self.getFilterView()
            })
        }
    }
    
    private func getFilterView() -> AnyView {
        print("<DocFeedView> Showing Filter with \(self.sessionData.passthroughDocTypes.count)")
        return AnyView(DocFeedFilterView().environmentObject(sessionData))
    }
    
    //
    // Filter the list data
    private func getFilteredDocumentList() -> [DocumentDTO]{
        return sessionData.feedDocumentList.filter{self.filterDocumentItem(element : $0)}
    }
    
    private func filterDocumentItem(element : DocumentDTO) -> Bool {
        if(!filterIsOn) { return true }
        
        //
        // Dates
        let componentsFrom = Calendar.current.dateComponents([.year, .month, .day], from: sessionData.feedDocsFilter.dateFrom )
        let componentsTo = Calendar.current.dateComponents([.year, .month, .day], from: sessionData.feedDocsFilter.dateTo )
        let componentsCompareDate = Calendar.current.dateComponents([.year, .month, .day], from: element.creationTimestampDate  )
        
        let componentsFromDate = Calendar.current.date(from: componentsFrom)!
        let componentsToDate = Calendar.current.date(from: componentsTo)!
        let componentsCompareUserDate = Calendar.current.date(from: componentsCompareDate)!
        
        if(componentsCompareUserDate < componentsFromDate
            || componentsCompareUserDate > componentsToDate) {
            return false
        }
        //if(element.creationTimestampDate < sessionData.myDocsFilter.dateFrom
        //    || element.creationTimestampDate > sessionData.myDocsFilter.dateTo) {
        //    return false
        //}
        
        //
        // recipient
        if(sessionData.feedDocsFilter.recipient!.name != Recipient.NULL_VALUE){
            // check further
            if(!element.toRecipients.contains{$0.name == sessionData.feedDocsFilter.recipient!.name}) {
                return false
            }
        }
        
        //
        // Doc Data Type
        if(sessionData.feedDocsFilter.documentType!.name != Recipient.NULL_VALUE){
            // check further
            if(element.type.name != sessionData.feedDocsFilter.documentType!.name ) {
                return false
            }
        }
        
        //
        // tag
        if(sessionData.feedDocsFilter.tag!.text != DocumentTag.NULL_VALUE){
            // check further
            if(!element.tags.contains{$0.text == sessionData.feedDocsFilter.tag!.text}) {
                return false
            }
        }
        
        return true
    }
}

struct DocFeedView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView{
            DocFeedView()
            .environmentObject(ViewRouter())
            .environmentObject(SessionData())
        }
    }
}
