package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.io.PrintWriter;

public class ExportStatusServlet extends SecureController {

    @Override
    public void processRequest() throws Exception {
        String taskId = request.getParameter("taskId");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        ExportTask task = ExportDatasetServlet.getExportTask(taskId);
        if (task == null) {
            out.print("{\"status\":\"Failed\", \"error\":\"Task not found\"}");
        } else {
            out.print("{");
            out.print("\"status\":\"" + task.getStatus() + "\"");
            if (task.getDownloadUrl() != null) {
                out.print(", \"downloadUrl\":\"" + task.getDownloadUrl() + "\"");
            }
            if (task.getErrorMessage() != null) {
                // simple escape for JSON
                out.print(", \"error\":\"" + task.getErrorMessage().replace("\"", "\\\"") + "\"");
            }
            out.print("}");
        }
        out.flush();
        out.close();
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        // Assume users checking status are allowed if they have basic access
        if (ub == null) {
            throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");
        }
    }
}
