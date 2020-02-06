//
//  UserCell.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-04.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct UserCell: View {
    let recipient: Recipient
    
    var body: some View {
        HStack {
            ImageViewRecipientProfileWidget(imageUrl: ImageUtils.fetchProfileURL(username : self.recipient.name))
                
            VStack (alignment: .leading){
                Text(recipient.name).font(.headline)
                Text(recipient.userGroups[0].name)
            }
            Spacer()
        }
        
    }
}

struct UserCell_Previews: PreviewProvider {
    static var previews: some View {
        UserCell(recipient : Recipient())
    }
}
