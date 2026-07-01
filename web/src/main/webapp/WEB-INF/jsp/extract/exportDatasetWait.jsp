<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="respage"/>

<jsp:include page="../include/extract-header.jsp"/>

<!-- ============================================================== -->
<jsp:include page="../include/sidebar.jsp"/>
<jsp:include page="../include/extractDataSideInfo.jsp"/>

<h1><span class="title_manage">
<fmt:message key="export_dataset" bundle="${resword}"/> - <fmt:message key="processing" bundle="${resword}"/>
</span></h1>

<div style="text-align:center; padding: 50px;">
    <h2 id="statusMessage">Export is processing... Please wait.</h2>
    <div id="spinner" style="margin: 20px;">
        <img src="<c:url value='/images/spinner.gif'/>" alt="Loading..." />
    </div>
    <p>You can leave this page or continue waiting. The file will download automatically when ready.</p>
</div>

<script type="text/javascript">
    var taskId = '<c:out value="${taskId}"/>';
    var pollInterval = 2000; // poll every 2 seconds

    function checkStatus() {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', '<c:url value="/ExportStatus?taskId="/>' + taskId, true);
        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4 && xhr.status == 200) {
                var response = JSON.parse(xhr.responseText);
                if (response.status === 'Completed') {
                    document.getElementById('statusMessage').innerHTML = 'Export Completed! Your download should begin shortly.';
                    document.getElementById('spinner').style.display = 'none';
                    // The backend returns a path or identifier to download
                    // If it is an id or a generate link:
                    if (response.downloadUrl) {
                        window.location.href = '<c:url value="/AccessFile?fileId="/>' + response.downloadUrl;
                    } else {
                        // Fallback
                        window.location.href = '<c:url value="/ExportDataset?datasetId=${datasetId}"/>';
                    }
                } else if (response.status === 'Failed') {
                    document.getElementById('statusMessage').innerHTML = 'Export Failed: ' + (response.error || 'Unknown error');
                    document.getElementById('spinner').style.display = 'none';
                } else {
                    // Still processing
                    setTimeout(checkStatus, pollInterval);
                }
            } else if (xhr.readyState == 4) {
                // error fetching status
                document.getElementById('statusMessage').innerHTML = 'Error checking status. Please check the dataset page later.';
                document.getElementById('spinner').style.display = 'none';
            }
        };
        xhr.send();
    }

    // Start polling
    setTimeout(checkStatus, pollInterval);
</script>

<jsp:include page="../include/footer.jsp"/>
