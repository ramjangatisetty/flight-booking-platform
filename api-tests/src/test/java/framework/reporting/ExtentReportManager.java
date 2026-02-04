package framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import framework.config.TestConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentReportManager {
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    private ExtentReportManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String reportPath = "build/reports/extent/extent-report-" + timestamp + ".html";

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("API Test Automation Report");
            spark.config().setReportName("Flight Booking Platform - API Tests");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Environment", TestConfig.getInstance().getEnv());
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }

    public static ExtentTest createTest(String testName) {
        return createTest(testName, null);
    }

    public static ExtentTest createTest(String testName, String description) {
        ExtentTest test = description != null
                ? getInstance().createTest(testName, description)
                : getInstance().createTest(testName);
        testThread.set(test);
        return test;
    }

    public static ExtentTest getTest() {
        return testThread.get();
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
        }
    }

    public static void removeTest() {
        testThread.remove();
    }
}
