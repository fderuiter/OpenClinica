sed -i 's/import org.akaza.openclinica.service.crfdata.SCDData;//g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/import org.akaza.openclinica.service.crfdata.front.InstantOnChangeFrontStrGroup;//g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/private SCDData scdData;/private Object scdData;/g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/scdData = new SCDData();//g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/private InstantOnChangeFrontStrGroup instantFrontStrGroup;/private Object instantFrontStrGroup;/g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/instantFrontStrGroup = new InstantOnChangeFrontStrGroup();//g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/public SCDData getScdData/public Object getScdData/g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/public void setScdData(SCDData/public void setScdData(Object/g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/public InstantOnChangeFrontStrGroup getInstantFrontStrGroup/public Object getInstantFrontStrGroup/g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
sed -i 's/public void setInstantFrontStrGroup(InstantOnChangeFrontStrGroup/public void setInstantFrontStrGroup(Object/g' domain/src/main/java/org/akaza/openclinica/bean/submit/DisplayItemBean.java
rm -rf domain/src/main/java/org/akaza/openclinica/service/
