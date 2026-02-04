package framework.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import framework.utils.JsonUtils;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Utility class for detailed logging in ExtentReports.
 * Provides pretty-printed JSON/XML output for better readability.
 */
public final class ReportLogger {

    private ReportLogger() {
        // Utility class
    }

    public static void logRequest(String method, String baseUrl, String path, Map<String, String> headers, Object body) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background-color:#e3f2fd;padding:10px;border-radius:5px;margin:5px 0;'>");
        sb.append("<b style='color:#1565c0;font-size:14px;'>🔵 REQUEST</b><br/><br/>");
        sb.append("<table style='width:100%;border-collapse:collapse;'>");
        sb.append("<tr><td style='width:100px;font-weight:bold;'>Method:</td><td><code style='background:#fff;padding:2px 6px;border-radius:3px;'>").append(method).append("</code></td></tr>");
        sb.append("<tr><td style='font-weight:bold;'>URL:</td><td><code style='background:#fff;padding:2px 6px;border-radius:3px;'>").append(baseUrl).append(path).append("</code></td></tr>");
        sb.append("</table>");

        if (headers != null && !headers.isEmpty()) {
            sb.append("<br/><b>Headers:</b><br/>");
            sb.append("<table style='width:100%;border-collapse:collapse;background:#fff;margin-top:5px;'>");
            headers.forEach((k, v) -> {
                sb.append("<tr style='border-bottom:1px solid #eee;'>");
                sb.append("<td style='padding:4px 8px;font-family:monospace;color:#666;'>").append(k).append("</td>");
                sb.append("<td style='padding:4px 8px;font-family:monospace;'>").append(v).append("</td>");
                sb.append("</tr>");
            });
            sb.append("</table>");
        }

        sb.append("</div>");
        test.info(sb.toString());

        if (body != null) {
            String formattedBody = formatBodyPretty(body);
            test.info(MarkupHelper.createCodeBlock(formattedBody, CodeLanguage.JSON));
        }
    }

    public static void logResponse(Response response) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null || response == null) {
            return;
        }

        int statusCode = response.getStatusCode();
        String statusColor = statusCode >= 200 && statusCode < 300 ? "#4caf50" : 
                            statusCode >= 400 ? "#f44336" : "#ff9800";

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background-color:#e8f5e9;padding:10px;border-radius:5px;margin:5px 0;'>");
        sb.append("<b style='color:#2e7d32;font-size:14px;'>🟢 RESPONSE</b><br/><br/>");
        sb.append("<table style='width:100%;border-collapse:collapse;'>");
        sb.append("<tr><td style='width:120px;font-weight:bold;'>Status Code:</td><td><span style='background:").append(statusColor).append(";color:#fff;padding:2px 8px;border-radius:3px;font-weight:bold;'>").append(statusCode).append("</span></td></tr>");
        sb.append("<tr><td style='font-weight:bold;'>Status Line:</td><td>").append(response.getStatusLine()).append("</td></tr>");
        sb.append("<tr><td style='font-weight:bold;'>Response Time:</td><td><code>").append(response.getTime()).append("ms</code></td></tr>");
        sb.append("</table>");
        sb.append("</div>");
        test.info(sb.toString());

        String body = response.getBody().asString();
        if (body != null && !body.isBlank()) {
            String contentType = response.getContentType();
            CodeLanguage language = determineCodeLanguage(contentType, body);
            String formattedBody = formatResponseBody(body, contentType);
            
            if (formattedBody.length() > 5000) {
                formattedBody = formattedBody.substring(0, 5000) + "\n\n... [truncated - response too large]";
            }
            
            test.info(MarkupHelper.createCodeBlock(formattedBody, language));
        }
    }

    public static void logAssertion(String description, Object expected, Object actual, boolean passed) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) {
            return;
        }

        String bgColor = passed ? "#e8f5e9" : "#ffebee";
        String borderColor = passed ? "#4caf50" : "#f44336";
        String icon = passed ? "✅" : "❌";
        String result = passed ? "PASS" : "FAIL";

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background:").append(bgColor).append(";border-left:4px solid ").append(borderColor).append(";padding:10px;margin:5px 0;border-radius:0 5px 5px 0;'>");
        sb.append("<table style='width:100%;border-collapse:collapse;'>");
        sb.append("<tr><td style='width:100px;font-weight:bold;'>Assertion:</td><td>").append(description).append("</td></tr>");
        sb.append("<tr><td style='font-weight:bold;'>Expected:</td><td><code style='background:#fff;padding:2px 6px;border-radius:3px;'>").append(expected).append("</code></td></tr>");
        sb.append("<tr><td style='font-weight:bold;'>Actual:</td><td><code style='background:#fff;padding:2px 6px;border-radius:3px;'>").append(actual).append("</code></td></tr>");
        sb.append("<tr><td style='font-weight:bold;'>Result:</td><td><b>").append(icon).append(" ").append(result).append("</b></td></tr>");
        sb.append("</table>");
        sb.append("</div>");

        if (passed) {
            test.pass(sb.toString());
        } else {
            test.fail(sb.toString());
        }
    }

    public static void logStep(String stepDescription) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.info("<div style='background:#fff3e0;padding:8px 12px;border-radius:5px;border-left:4px solid #ff9800;margin:5px 0;'>" +
                    "<b style='color:#e65100;'>📋 STEP:</b> " + stepDescription + "</div>");
        }
    }

    public static void info(String message) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.info("<span style='color:#666;'>ℹ️ " + message + "</span>");
        }
    }

    public static void pass(String message) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, "<span style='color:#4caf50;font-weight:bold;'>" + message + "</span>");
        }
    }

    public static void fail(String message) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, "<span style='color:#f44336;font-weight:bold;'>" + message + "</span>");
        }
    }

    /**
     * Formats request body with pretty printing for JSON.
     */
    private static String formatBodyPretty(Object body) {
        if (body == null) {
            return "";
        }
        
        if (body instanceof String) {
            return JsonUtils.prettyPrint((String) body);
        }
        
        if (body instanceof Map) {
            return JsonUtils.toPrettyJson(body);
        }
        
        // Try to convert to pretty JSON
        try {
            return JsonUtils.toPrettyJson(body);
        } catch (Exception e) {
            return body.toString();
        }
    }

    /**
     * Formats response body based on content type.
     */
    private static String formatResponseBody(String body, String contentType) {
        if (body == null || body.isBlank()) {
            return "";
        }

        // JSON content
        if (contentType != null && (contentType.contains("json") || body.trim().startsWith("{"))) {
            return JsonUtils.prettyPrint(body);
        }

        // XML content - basic indentation
        if (contentType != null && (contentType.contains("xml") || body.trim().startsWith("<"))) {
            return formatXml(body);
        }

        return body;
    }

    /**
     * Basic XML formatting with indentation.
     */
    private static String formatXml(String xml) {
        if (xml == null || xml.isBlank()) {
            return xml;
        }
        
        try {
            // Simple XML formatting - add newlines after closing tags
            return xml
                    .replaceAll(">\\s*<", ">\n<")
                    .replaceAll("(<[^/][^>]*>)", "\n$1")
                    .replaceAll("(</[^>]+>)", "$1\n")
                    .replaceAll("\n+", "\n")
                    .trim();
        } catch (Exception e) {
            return xml;
        }
    }

    /**
     * Determines the code language for syntax highlighting.
     */
    private static CodeLanguage determineCodeLanguage(String contentType, String body) {
        if (contentType != null) {
            if (contentType.contains("json")) {
                return CodeLanguage.JSON;
            }
            if (contentType.contains("xml")) {
                return CodeLanguage.XML;
            }
        }
        
        // Fallback: detect from content
        if (body != null) {
            String trimmed = body.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return CodeLanguage.JSON;
            }
            if (trimmed.startsWith("<")) {
                return CodeLanguage.XML;
            }
        }
        
        return CodeLanguage.JSON; // Default
    }
}
