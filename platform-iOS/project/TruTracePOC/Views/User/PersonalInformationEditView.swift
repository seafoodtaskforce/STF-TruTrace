//
//  PersonalInformationEditView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-21.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct PersonalInformationEditView: View {
    @EnvironmentObject var sessionData: SessionData
    @Environment(\.presentationMode) var presentationMode
    
    @State var firstName: String = ""
    @State var lastName: String = ""
    @State var nickName: String = ""
    
    var body: some View {
            Form{
                Section(header: Text(LocalizationUtils.localizeString(text: "ios_user_personal_data_navigation_header"))) {
                    // First Name
                    // - mutable
                    HStack{
                        Image(systemName: "f.square.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 10))
                        Text(LocalizationUtils.localizeString(text: "ios_user_personal_data_label_first")).bold()
                        Spacer()
                        Spacer()
                        //Text(self.sessionData.contactInfo.firstName)
                        // User name
                        TextField("required", text: $firstName)
                            //.textFieldStyle(RoundedBorderTextFieldStyle())
                            //.background(
                            //   Color(UIColor.systemGray).opacity(0.95))
                            //        .padding()
                            .autocapitalization(.none)
                    }
                    // Last Name
                    // - mutable
                    HStack{
                        Image(systemName: "l.square.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 10))
                        Text(LocalizationUtils.localizeString(text: "ios_user_personal_data_label_last")).bold()
                        Spacer()
                        Spacer()
                        TextField("required", text: $lastName)
                            //.textFieldStyle(RoundedBorderTextFieldStyle())
                            //.background(
                            //    Color(UIColor.systemGray).opacity(0.95))
                            //        .padding()
                            .autocapitalization(.none)
                        //Text(self.sessionData.contactInfo.lastName)
                    }
                    // Nickname
                    // - mutable
                    HStack{
                        Image(systemName: "person.circle.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 10))
                        Text(LocalizationUtils.localizeString(text: "ios_user_personal_data_label_nick")).bold()
                        TextField("optional", text: $nickName)
                        .autocapitalization(.none)
                        //Text(self.sessionData.contactInfo.nickname)
                    }
                }
                .onAppear{
                    self.firstName = self.sessionData.contactInfo.firstName
                    self.lastName = self.sessionData.contactInfo.lastName
                    self.nickName = self.sessionData.contactInfo.nickName
                }// Section
            } // List
            .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_user_personal_data_navigation_header")), displayMode: .inline)
            .navigationBarItems(
                trailing:
                Button(LocalizationUtils.localizeString(text: "ios_user_personal_data_navigation_button_save")) {
                    print("Saving User Personal Data")
                    self.savePersonalData()
                    self.presentationMode.wrappedValue.dismiss()
                }.disabled(!canBeSaved())
            )
    }
    
    //
    // Check if the form can be saved
    func canBeSaved() -> Bool {
        if( self.firstName.isEmpty || self.lastName.isEmpty ) { return false }
        
        if( (self.firstName != self.sessionData.contactInfo.firstName)
            || (self.lastName != self.sessionData.contactInfo.lastName)
            || (self.nickName != self.sessionData.contactInfo.nickName) ) { return true }
        
        return false
    }
    
    //
    // Save Personal Data
    func savePersonalData(){
        var jsonBody : Data?
        // prepare the user data
        sessionData.contactInfo.firstName = self.firstName
        sessionData.contactInfo.lastName = self.lastName
        sessionData.contactInfo.nickName = self.nickName
        
        let userRequestData: UserRequestProfileData = UserRequestProfileData (
            id: sessionData.userId,
            contactInfo : sessionData.contactInfo
        )
        
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_POST_UPDATE_USER_INFO))
        
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

struct PersonalInformationEditView_Previews: PreviewProvider {
    static var previews: some View {
        PersonalInformationEditView().environmentObject(SessionData())
    }
}
