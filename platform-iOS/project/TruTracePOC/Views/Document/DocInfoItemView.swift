//
//  DocInfoItemView.swift
//  TruTracePOC
//
//  Created by Piotr Paweska on 2020-06-03.
//  Copyright © 2020 Piotr Paweska. All rights reserved.
//

import SwiftUI

struct DocInfoItemView: View {
    @EnvironmentObject var sessionData: SessionData
    @Environment(\.presentationMode) var presentationMode
    
    @State var docInfoDataValue: String = ""
    
    var documentCard : DocumentDTO
    var docInfoDefinition : DynamicFieldDefinition
    
    
    var body: some View {
        Form{
            Section(
                header: Text("Please Enter Data Below"),
                footer: Text(self.docInfoDefinition.description).font(.footnote)
            ) {
                // line id
                // - mutable
                HStack{
                    Text(self.docInfoDefinition.displayName).bold()
                    Spacer()
                    Spacer()
                    Spacer()
                    TextField("required", text: $docInfoDataValue)
                        .autocapitalization(.none)
                }
            }
            .onAppear{
                self.docInfoDataValue = DocumentDataUtils.getDynamicFieldValue(document : self.getDocument(), dynamicFieldDefinitionId : self.docInfoDefinition.id)
            }// Section
            
        } // Form
        .navigationBarTitle(Text(LocalizationUtils.localizeString(text: "ios_user_security_data_navigation_header")), displayMode: .inline)
        .navigationBarItems(
            trailing:
            Button(LocalizationUtils.localizeString(text: "ios_user_security_data_navigation_button_save")) {
                print("Saving User Personal Data")
                self.saveDocInfoData()
                self.presentationMode.wrappedValue.dismiss()
            }.disabled(false)
        )
    }
    
    func getDocument() -> DocumentDTO{
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            return sessionData.myDocumentList[index]
        }
        return DocumentDTO()
    }
    
    func saveDocInfoData(){
        // save the change in docuemnt in session
        if let index = sessionData.myDocumentList.firstIndex(of: documentCard) {
            // find the field
            var ordinal = DocumentDataUtils.getDynamicFieldDefinitionOrdinal(definitionId : self.docInfoDefinition.id, document : self.documentCard, sessionData : self.sessionData)
            // ensure that the document has all the fields.
            while sessionData.myDocumentList[index].dynamicFieldData.count-1 < ordinal{
                // add the missing field
                let field: DynamicFieldData
                    = DocumentDataUtils.getDynamicFieldDataDummyValueByOrdinal(ordinal : sessionData.myDocumentList[index].dynamicFieldData.count , document : documentCard, sessionData : sessionData)
                sessionData.myDocumentList[index].dynamicFieldData.append(field)
            }
            
            // save it
            sessionData.myDocumentList[index].dynamicFieldData[ordinal].data = self.docInfoDataValue
        }
    }
}

struct DocInfoItemView_Previews: PreviewProvider {
    static var previews: some View {
        DocInfoItemView(documentCard : DocumentDTO(),
                        docInfoDefinition : DynamicFieldDefinition())
            .environmentObject(SessionData())
    }
}
