//
//  DateUtils.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-24.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import Foundation

class DateUtils {

    static func formatStringToDate(date: String) -> Date {
        let formatter = DateFormatter()
        if(date.contains("-")){
            formatter.dateFormat = "yyyy-mm-dd HH:mm:ss"
        }
        if(date.contains("/")){
            formatter.dateFormat = "mm/dd/yy HH:mm"
        }
        let myDate = formatter.date(from: date)!
        return myDate
    }

}
