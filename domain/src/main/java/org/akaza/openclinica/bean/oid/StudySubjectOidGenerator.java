package org.akaza.openclinica.bean.oid;

public class StudySubjectOidGenerator extends OidGenerator {

    @Override
    protected int getArgumentLength() {
        return 1;
    }

    @Override
    String createOid(String... keys) {
        String oid = "SS_";
        String studySubjectID = keys[0];
        studySubjectID = truncateTo8Chars(capitalize(stripNonAlphaNumeric(studySubjectID)));
        return oid + studySubjectID;
    }
}
