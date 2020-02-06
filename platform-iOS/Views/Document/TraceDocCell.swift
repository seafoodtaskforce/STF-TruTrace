//
//  TraceDocCell.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-04.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct TraceDocCell: View {
    var docData: DocumentDTO
    
    var dateFormatterParser : DateFormatter {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "dd/MM/dd HH:mm"
        return dateFormatter
    }
    var dateFormatterPrinter : DateFormatter {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy/MM/dd HH:mm"
        return dateFormatter
    }
    
    
    var body: some View {
        VStack (alignment: .leading){
            HStack {
                Text(docData.documentType).font(.headline)
            }
            HStack {
                Text(docData.owner).font(.footnote)
                Spacer()
                //Text("\(docData.creationDate, formatter: dateFormatterPrinter)").font(.caption)
                Text(docData.creationTimestamp).font(.caption)
            }
            
        }
    }
}

struct TraceDocCell_Previews: PreviewProvider {
    static var previews: some View {
        TraceDocCell(docData : DocumentDTO())
    }
}
