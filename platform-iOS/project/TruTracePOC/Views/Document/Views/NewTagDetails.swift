//
//  NewTagDetails.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-15.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct NewTagDetails: View {
    @State private var newTag = ""

    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Enter New Tag", text: $newTag)
                }
            }
            .navigationBarTitle(Text("New Tag"), displayMode: .inline)
        }
    }
}

struct NewTagDetails_Previews: PreviewProvider {
    static var previews: some View {
        NewTagDetails()
    }
}
