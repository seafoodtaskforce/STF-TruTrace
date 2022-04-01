export class DateUtils {

    /**
     * Simple resource fetching method which gets the current tdate/time in the 
     * YYYY-MM-DD HH:mm:ss" format
     */
    static getCurrentDateTime() {

        var currDate: Date = new Date();
        var dateAsString = "";

        dateAsString = "" 
                + currDate.getFullYear() + "-" 
                + (currDate.getMonth() + 1) + "-" 
                + currDate.getDate() + " " 
                + currDate.getHours() + ":" 
                + currDate.getMinutes() + ":" 
                + currDate.getSeconds();

        return dateAsString;
    }
    static getDateAsString(dateIn : Date) {
        var dateAsString = "";

        if(dateIn == null) {
            dateIn = new Date();
        }

        dateAsString = "" 
            + dateIn.getFullYear() + "-" 
            + (dateIn.getMonth() + 1) + "-" 
            + dateIn.getDate() + " " 
        return dateAsString;
    }

    static getDateFromString(date: string){
        var convDate: Date;
        let dateMs = Date.parse(date);
        convDate = new Date(dateMs);

        return convDate;
    }

    // Returns an array of dates between the two dates
    static getDatesBetween = (startDate, endDate) => {
        const dates = [];

        // Strip hours minutes seconds etc.
        let currentDate = new Date(
            startDate.getFullYear(),
            startDate.getMonth(),
            startDate.getDate()
        );

        while (currentDate <= endDate) {
            let dateString = DateUtils.getDateAsString(currentDate)
            dates.push(dateString);

            currentDate = new Date(
                currentDate.getFullYear(),
                currentDate.getMonth(),
                currentDate.getDate() + 1, // Will increase month if over range
            );
        }

        return dates;
    }
}