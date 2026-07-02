sed -i 's/import org.akaza.openclinica.domain.datamap.CrfBean;//g' core/src/main/java/org/akaza/openclinica/bean/core/Utils.java
sed -i 's/import org.akaza.openclinica.domain.datamap.CrfVersion;//g' core/src/main/java/org/akaza/openclinica/bean/core/Utils.java
sed -i 's/public static String getCrfMediaFilePath(CrfBean crf, CrfVersion version)/public static String getCrfMediaFilePath(String crfOid, String versionOid)/g' core/src/main/java/org/akaza/openclinica/bean/core/Utils.java
sed -i 's/crf.getOcOid()/crfOid/g' core/src/main/java/org/akaza/openclinica/bean/core/Utils.java
sed -i 's/version.getOcOid()/versionOid/g' core/src/main/java/org/akaza/openclinica/bean/core/Utils.java
sed -i 's/Utils.getCrfMediaFilePath(crf, version)/Utils.getCrfMediaFilePath(crf.getOcOid(), version.getOcOid())/g' core/src/main/java/org/akaza/openclinica/service/crfdata/XformMetaDataService.java
