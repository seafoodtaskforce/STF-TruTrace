//
//  PageDetail.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-03.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct PageDetail: View {
    @EnvironmentObject var sessionData: SessionData
    let page : DocumentPage
    let pageIndex : Int
    
    @State private var zoomed: Bool = false
    
    var body: some View {
        VStack {
            if page.localImage != nil { ImageViewPageLocalZoomableWidget(image : page.localImage!)}
            else {
                ImageViewPageZoomableWidget(imageUrl: ImageUtils.fetchDocumentPageURL(pageId : self.page.id, sessionData: sessionData))}
            Text("\(LocalizationUtils.localizeString(text: "ios_page_detail_page")) \(pageIndex + 1)")
        }.navigationBarTitle(Text("\(LocalizationUtils.localizeString(text: "ios_page_detail_navigation_header")) \(pageIndex + 1)"), displayMode: .inline)
    }
}

struct HIkeDetail_Previews: PreviewProvider {
    static var previews: some View {
        PageDetail(page : DocumentPage(id: 3544, pageNumber: 0, deleted : false
        ), pageIndex: 0).environmentObject(SessionData())
    }
}

