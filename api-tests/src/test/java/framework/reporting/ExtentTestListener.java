package framework.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;

/**
 * TestNG listener for ExtentReports integration.
 */
public class ExtentTestListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        // Initialize report on suite start
        ExtentReportManager.getInstance();
    }

    @Override
    public void onFinish(ITestContext context) {
        // Flush report on suite finish
        ExtentReportManager.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        String[] groups = result.getMethod().getGroups();

        ExtentTest test = ExtentReportManager.createTest(className + "." + testName);

        if (groups.length > 0) {
            test.assignCategory(groups);
        }

        test.info("Test started: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, "Test passed: " + result.getMethod().getMethodName());
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                test.log(Status.FAIL, "Test failed: " + throwable.getMessage());
                test.log(Status.FAIL, Arrays.toString(throwable.getStackTrace()));
            } else {
                test.log(Status.FAIL, "Test failed: " + result.getMethod().getMethodName());
            }
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, "Test skipped: " + result.getMethod().getMethodName());
            if (result.getThrowable() != null) {
                test.log(Status.SKIP, "Reason: " + result.getThrowable().getMessage());
            }
        }
        ExtentReportManager.removeTest();
    }
}
