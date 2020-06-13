//
//  SecurityInformationEditView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-22.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

struct SecurityInformationEditView: View {
    @EnvironmentObject var sessionData: SessionData
    @Environment(\.presentationMode) var presentationMode
    
    @State var password1: String = ""
    @State var password2: String = ""
    
    var body: some View {
            Form{
                Section(
                    header: Text(LocalizationUtils.localizeString(text: "ios_user_security__data_section_label_security_information")),
                    footer: Text(LocalizationUtils.localizeString(text: "ios_user_security__data_section_label_security_information_footer")).font(.footnote)) {
                    // line id
                    // - mutable
                    HStack{
                        Image(systemName: "keyboard").padding(EdgeInsets(top: 0, leading: 6, bottom: 0, trailing: 6))
                        Text(LocalizationUtils.localizeString(text: "ios_user_security_data_label_new")).bold()
                        Spacer()
                        Spacer()
                        Spacer()
                        SecureField(LocalizationUtils.localizeString(text: "ios_user_security_data_label_hint_new"), text: $password1)
                            .autocapitalization(.none)
                    }
                    // email
                    // - mutable
                    HStack{
                        Image(systemName: "keyboard").padding(EdgeInsets(top: 0, leading: 6, bottom: 0, trailing: 6))
                        Text(LocalizationUtils.localizeString(text: "ios_user_security_data_label_verify")).bold()
                        Spacer()
                        Spacer()
                        SecureField(LocalizationUtils.localizeString(text: "ios_user_security_data_label_hint_verify"), text: $password2)
                            .autocapitalization(.none)
                    }
                } // Section
                
            } // Form
            .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_user_security_data_navigation_header")), displayMode: .inline)
            .navigationBarItems(
                trailing:
                Button(LocalizationUtils.localizeString(text: "ios_user_security_data_navigation_button_save")) {
                    print("Saving User Personal Data")
                    self.saveSecurityData()
                    self.presentationMode.wrappedValue.dismiss()
                }.disabled(!canBeSaved())
            )
    }
    
    //
    // Check if the form can be saved
    func canBeSaved() -> Bool {
        
        
        if( self.password1.isEmpty || self.password1.count < 8
            || self.password2.isEmpty || self.password2.count < 8) { return false }
        
        if( (self.password1 == self.password2)) { return true }
        
        return false
    }
    
    //
    // Save Personal Data
    func saveSecurityData(){
        var jsonBody : Data?
        // prepare the user data
        
        let userRequestCredentialsData: UserRequestCredentialsData = UserRequestCredentialsData (
            username : sessionData.userCredentials.username, password: self.password1
        )
        
        guard let url = URL(string:
            (sessionData.serverURL + RESTServer.REST_POST_UPDATE_USER_CREDENTIALS))
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let jsonEncoder = JSONEncoder()
        jsonEncoder.outputFormatting = .withoutEscapingSlashes
        if let encoded = try? jsonEncoder.encode(userRequestCredentialsData) {
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

struct SecurityInformationEditView_Previews: PreviewProvider {
    static var previews: some View {
        SecurityInformationEditView().environmentObject(SessionData())
    }
}
