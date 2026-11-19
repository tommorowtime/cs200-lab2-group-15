public class ManagerTest {
    public static void main(String[] args) {
        System.out.println(">>> RUNNING MANAGER TESTS <<<");

        // 1. Test Summary Report
        String summary = Manager.generateSummaryReport();
        assert summary != null && summary.contains("Summary Report");
        assert summary.contains("Total Providers to be Paid");
        System.out.println("[PASS] generateSummaryReport()");

        // 2. Test Member Report
        String memberReport = Manager.generateMemberReport("123456789");
        assert memberReport != null && memberReport.contains("Alice Smith");
        assert memberReport.contains("Dietitian Session");
        System.out.println("[PASS] generateMemberReport()");

        // 3. Test Provider Report
        String provReport = Manager.generateProviderReport("111222333");
        assert provReport != null && provReport.contains("Dr. Sweet Tooth");
        assert provReport.contains("Dietitian Session");
        System.out.println("[PASS] generateProviderReport()");

        // 4. Test Search Providers Requiring Payment (by ID)
        String searchById = Manager.searchProvidersToPay("111222333");
        assert searchById.contains("Dr. Sweet Tooth") && searchById.contains("Dietitian Session");
        System.out.println("[PASS] searchProvidersToPay(by ID)");

        // 5. Test Search Providers Requiring Payment (by Name)
        String searchByName = Manager.searchProvidersToPay("Choc Care");
        assert searchByName.contains("444555666");
        System.out.println("[PASS] searchProvidersToPay(by Name)");

        // 6. Test Error Handling for Invalid ID
        String badMem = Manager.generateMemberReport("000000000");
        assert badMem.contains("Member not found with ID: 000000000");
        String badProv = Manager.generateProviderReport("000000000");
        assert badProv.contains("Provider not found with ID: 000000000");
        String badSearch = Manager.searchProvidersToPay("NonExistent");
        assert badSearch.contains("No provider found matching");
        System.out.println("[PASS] Error handling for missing IDs and searches");

        // 7. Test Add Service Record
        ServiceRecord newRecord = new ServiceRecord(
                "11-18-2026", "111222333", "555666777", "883921",
                "Exercise Therapy", 120.00, "New therapy session");
        Manager.addServiceRecord(newRecord);
        String updatedProvReport = Manager.generateProviderReport("111222333");
        assert updatedProvReport.contains("Exercise Therapy");
        System.out.println("[PASS] addServiceRecord() and verification");

        System.out.println("\n>>> ALL TESTS PASSED SUCCESSFULLY! <<<");
    }
}
