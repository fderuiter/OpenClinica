package org.akaza.openclinica.bean.extract.odm;

import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import java.io.OutputStream;

public interface ClinicalDataStreamWriter {
    void writeStartDocument(String studyOID, String metaDataVersionOID, String metadataXml) throws Exception;
    void writeSubjectData(ExportSubjectDataBean sub) throws Exception;
    void writeEndDocument() throws Exception;
    void close() throws Exception;
}
