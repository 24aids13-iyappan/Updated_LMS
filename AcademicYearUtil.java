import java.time.LocalDate;

/*
 * AcademicYearUtil - Student's admission_year (eg. 2023) vachikittu,
 * "current year of study" ah automatic ah calculate pannum.
 *
 * Academic year roll-over June/July la nadakum (India college pattern).
 * So Jan-June varaikkum previous academic year continue aagum,
 * July mudhal pudhu academic year start aagum.
 *
 * Example:
 *   Admission Year = 2023
 *   Today = July 2026  → Academic Year = 2026 → Year of Study = 2026-2023+1 = 4th Year
 *   Today = March 2026 → Academic Year = 2025 → Year of Study = 2025-2023+1 = 3rd Year
 */
public class AcademicYearUtil {

    // Change this if your college's semester/year roll-over month is different
    private static final int ROLLOVER_MONTH = 7; // July

    public static int getCurrentAcademicYear() {
        LocalDate today = LocalDate.now();
        int calendarYear = today.getYear();
        int month = today.getMonthValue();

        if (month >= ROLLOVER_MONTH) {
            return calendarYear;
        } else {
            return calendarYear - 1;
        }
    }

    /**
     * Returns a display string like "1st Year", "2nd Year", "3rd Year",
     * "4th Year", or "Graduated" if more than 4 years have passed.
     */
    public static String calculateYearOfStudy(int admissionYear) {

        int currentAcademicYear = getCurrentAcademicYear();
        int yearNumber = (currentAcademicYear - admissionYear) + 1;

        if (yearNumber <= 0) {
            return "Not Started";
        } else if (yearNumber == 1) {
            return "1st Year";
        } else if (yearNumber == 2) {
            return "2nd Year";
        } else if (yearNumber == 3) {
            return "3rd Year";
        } else if (yearNumber == 4) {
            return "4th Year";
        } else {
            return "Graduated";
        }
    }
}
