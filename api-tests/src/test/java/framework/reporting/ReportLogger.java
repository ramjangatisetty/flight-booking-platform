package framework.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import framework.utils.JsonUtils;
import io.restassured.response.Response;

import java.util.Map;

public final class ReportLogger {
    private ReportLogger() {}

    public static void logRequest(String method, String baseUrl, String path,
                                   Map<String, String> headers, Object body) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background:#e3f2fd;padding:10px;border-radius:5px;margin:5px 0;'>");
        sb.append("<b>🔵 REQUEST</b><br/>");
        sb.append("<b>Method:</b> ").append(method).append("<br/>");
        sb.append("<b>URL:</b> ").append(baseUrl).append(path).append("<br/>");

        if (headers != null && !headers.isEmpty()) {
            sb.append("<b>Headers:</b><br/>");
            sb.append("<table style='margin-left:20px;'>");
            headers.forEach((k, v) -> sb.append("<tr><td>").append(k).append("</td><td>").append(v).append("</td></tr>"));
            sb.append("</table>");
        }
        sb.append("</div>");
        test.info(sb.toString());

        if (body != null) {
            String bodyStr = body instanceof String ? (String) body : JsonUtils.toPrettyJson(body);
            test.info(MarkupHelper.createCodeBlock(bodyStr, CodeLanguage.JSON));
        }
    }

    public static void logResponse(Response response) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) return;

        int status = response.getStatusCode();
        String statusColor = status >= 200 && status < 300 ? "#c8e6c9"
                : status >= 400 ? "#ffcdd2" : "#fff9c4";

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background:").append(statusColor).append(";padding:10px;border-radius:5px;margin:5px 0;'>");
        sb.append("<b>🟢 RESPONSE</b><br/>");
        sb.append("<b>Status:</b> ").append(status).append(" ").append(response.getStatusLine()).append("<br/>");
        sb.append("<b>Time:</b> ").append(response.getTime()).append("ms<br/>");
        sb.append("</div>");
        test.info(sb.toString());

        String body = response.getBody().asString();
        if (body != null && !body.isBlank()) {
            String truncated = body.length() > 5000 ? body.substring(0, 5000) + "\n... [truncated]" : body;
            CodeLanguage lang = body.trim().startsWith("<") ? CodeLanguage.XML : CodeLanguage.JSON;
            if (lang == CodeLanguage.JSON) {
                truncated = JsonUtils.prettyPrint(truncated);
            }
            test.info(MarkupHelper.createCodeBlock(truncated, lang));
        }
    }

    public static void logAssertion(String description, Object expected, Object actual, boolean passed) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) return;

        String icon = passed ? "✅" : "❌";
        String color = passed ? "#c8e6c9" : "#ffcdd2";

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background:").append(color).append(";padding:10px;border-radius:5px;margin:5px 0;border-left:4px solid ")
                .append(passed ? "#4caf50" : "#f44336").append(";'>");
        sb.append("<b>").append(icon).append(" ASSERTION:</b> ").append(description).append("<br/>");
        sb.append("<b>Expected:</b> <code>").append(expected).append("</code><br/>");
        sb.append("<b>Actual:</b> <code>").append(actual).append("</code>");
        sb.append("</div>");

        if (passed) {
            test.pass(sb.toString());
        } else {
            test.fail(sb.toString());
        }
    }

    public static void logStep(String stepDescription) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) return;

        test.info("<div style='background:#fff3e0;padding:8px;border-radius:5px;border-left:4px solid #ff9800;'>"
                + "<b>📋 STEP:</b> " + stepDescription + "</div>");
    }

    public static void info(String message) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.info("ℹ️ " + message);
        }
    }
}
