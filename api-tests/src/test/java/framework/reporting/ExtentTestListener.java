package framework.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;

public class ExtentTestListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentReportManager.getInstance();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String[] groups = result.getMethod().getGroups();
        String description = groups.length > 0
                ? "Groups: " + Arrays.toString(groups)
                : null;
        ExtentReportManager.createTest(testName, description);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, "Test passed");
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, "Test failed: " + result.getThrowable().getMessage());
            test.fail(result.getThrowable());
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, "Test skipped");
            if (result.getThrowable() != null) {
                test.skip(result.getThrowable());
            }
        }
        ExtentReportManager.removeTest();
    }
}
