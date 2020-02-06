//
//  OrganizationInformationEditView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-22.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct OrganizationInformationEditView: View {
    @EnvironmentObject var sessionData: SessionData
    @Environment(\.presentationMode) var presentationMode
    
    @State var lineId: String = ""
    @State var emailAddress: String = ""
    
    var body: some View {
            Form{
                Section(header: Text(LocalizationUtils.localizeString(text: "ios_user_organization_data_section_label_organization_information"))) {
                    // line id
                    // - mutable
                    HStack{
                        Image(systemName: "bubble.left.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 8))
                        Text(LocalizationUtils.localizeString(text: "ios_user_organization_data_label_linerid")).bold()
                        Spacer()
                        Spacer()
                        //Text(self.sessionData.contactInfo.firstName)
                        // User name
                        TextField("required", text: $lineId)
                            //.textFieldStyle(RoundedBorderTextFieldStyle())
                            //.background(
                            //   Color(UIColor.systemGray).opacity(0.95))
                            //        .padding()
                            .autocapitalization(.none)
                    }
                    // email
                    // - mutable
                    HStack{
                        Image(systemName: "envelope.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 8))
                        Text(LocalizationUtils.localizeString(text: "ios_user_organizationdata_label_email")).bold()
                        Spacer()
                        Spacer()
                        TextField("required", text: $emailAddress)
                            .textContentType(.emailAddress)
                            .keyboardType(.emailAddress)
                            //.textFieldStyle(RoundedBorderTextFieldStyle())
                            //.background(
                            //    Color(UIColor.systemGray).opacity(0.95))
                            //        .padding()
                            .autocapitalization(.none)
                        //Text(self.sessionData.contactInfo.lastName)
                    }
                }
                .onAppear{
                    self.lineId = self.sessionData.contactInfo.lineId
                    self.emailAddress = self.sessionData.contactInfo.emailAddress
                }// Section
            } // List
            .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_user_organization_data_navigation_header")), displayMode: .inline)
            .navigationBarItems(
                trailing:
                Button(LocalizationUtils.localizeString(text: "ios_user_organization_data_navigation_button_save")) {
                    print("Saving User Personal Data")
                    self.saveOrganizationData()
                    self.presentationMode.wrappedValue.dismiss()
                }.disabled(!canBeSaved())
            )
    }
    
    //
    // Check if the form can be saved
    func canBeSaved() -> Bool {
        if( self.emailAddress.isEmpty ) { return false }
        
        if( (self.emailAddress != self.sessionData.contactInfo.emailAddress)
            || (self.lineId != self.sessionData.contactInfo.lineId)) { return true }
        
        return false
    }
    
    //
    // Save Personal Data
    func saveOrganizationData(){
        var jsonBody : Data?
        // prepare the user data
        sessionData.contactInfo.lineId = self.lineId
        sessionData.contactInfo.emailAddress = self.emailAddress
        
        let userRequestData: UserRequestProfileData = UserRequestProfileData (
            id: sessionData.userId,
            contactInfo : sessionData.contactInfo
        )
        
        guard let url = URL(string:
            (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_POST_UPDATE_USER_INFO))
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let jsonEncoder = JSONEncoder()
        jsonEncoder.outputFormatting = .withoutEscapingSlashes
        if let encoded = try? jsonEncoder.encode(userRequestData) {
            jsonBody = encoded
        }
        
        request.httpBody = jsonBody
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            guard let data = data else { return }

                DispatchQueue.main.async {
                  print("Personal User Information Updated")
                }
        }.resume()

    }
}

struct OrganizationInformationEditView_Previews: PreviewProvider {
    static var previews: some View {
        OrganizationInformationEditView().environmentObject(SessionData())
    }
}
