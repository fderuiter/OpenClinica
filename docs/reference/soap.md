# Static SOAP Service Documentation

This section provides static references to the legacy SOAP service specifications.

## beans.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema" targetNamespace="http://openclinica.org/ws/beans"
    xmlns:beans="http://openclinica.org/ws/beans" elementFormDefault="qualified">

    <complexType name="eventType">
        <sequence>
            <element name="studySubjectRef" type="beans:studySubjectRefType"/>
            <element name="studyRef" type="beans:studyRefType"/>
            <element name="eventDefinitionOID" type="beans:customStringType"/>
            <element name="location" type="beans:customStringType" minOccurs="0"/>
            <element name="startDate" type="date"/>
            <element name="startTime" type="time" minOccurs="0"/>
            <element name="endDate" type="date" minOccurs="0"/>
            <element name="endTime" type="time" minOccurs="0"/>
        </sequence>
    </complexType>
    
    <complexType name="eventsType">
        <sequence>
            <element name="event" type="beans:eventType" minOccurs="0" maxOccurs="unbounded"/>
        </sequence>
    </complexType>
    
    


    <complexType name="studySubjectType">
        <sequence>
            <element name="label" type="beans:customStringType" minOccurs="0"/>
            <element name="secondaryLabel" type="beans:customStringType" minOccurs="0"/>
            <element name="enrollmentDate" type="date"/>
            <element name="subject" type="beans:subjectType"/>
            <element name="studyRef" type="beans:studyRefType"/>
        </sequence>
    </complexType>
    
    <complexType name="studySubjectWithEventsType">
        <sequence>
            <element name="label" type="beans:customStringType" minOccurs="0"/>
            <element name="secondaryLabel" type="beans:customStringType" minOccurs="0"/>
            <element name="oid" type="beans:customStringType"/>
            <element name="enrollmentDate" type="date"/>
            <element name="subject" type="beans:subjectType"/>
            <element name="studyRef" type="beans:studyRefType"/>
            <element name="events" type="beans:eventsType" />
        </sequence>
    </complexType>
  
    <complexType name="subjectType">
        <sequence>
            <element name="uniqueIdentifier" type="beans:customStringType" minOccurs="0"/>
            <element name="gender" type="beans:genderType" minOccurs="0"/>
            <choice minOccurs="0">
                <element name="dateOfBirth" type="date"/>
                <element type="beans:customDateType" name="yearOfBirth"/>
            </choice>
        </sequence>
    </complexType>
    
    <complexType name="studiesType">
        <sequence>
            <element name="study" type="beans:studyType" minOccurs="0" maxOccurs="unbounded"/>
        </sequence>
    </complexType>
    
    
    <complexType name="studyType">
        <sequence>
            <element name="identifier" type="beans:customStringType"/>
            <element name="oid" type="beans:customStringType"/>
            <element name="name" type="beans:customStringType"/>
            <element name="sites" type="beans:sitesType"/>
        </sequence>
    </complexType>
    
    
    <complexType name="sitesType">
        <sequence>
            <element name="site" type="beans:siteType" minOccurs="0" maxOccurs="unbounded"/>
        </sequence>
    </complexType>
    
    <complexType name="siteType">
        <sequence>
            <element name="identifier" type="beans:customStringType"/>
            <element name="oid" type="beans:customStringType"/>
            <element name="name" type="beans:customStringType"/>
        </sequence>
    </complexType>
    
    
    <complexType name="studySubjectsType">
        <sequence>
            <element name="studySubject" type="beans:studySubjectWithEventsType" minOccurs="0" maxOccurs="unbounded"/>
        </sequence>
    </complexType>
    
    
    <complexType name="studyEventDefinitionsType">
        <sequence>
            <element name="studyEventDefinition" type="beans:studyEventDefinitionType" minOccurs="0" maxOccurs="unbounded"/>
        </sequence>
    </complexType>
    
    
    <complexType name="studyEventDefinitionType">
        <sequence>
            <element name="oid" type="beans:customStringType"/>
            <element name="name" type="beans:customStringType"/>
            <element name="eventDefinitionCrfs" type="beans:eventDefinitionCrfsType"/>
        </sequence>
    </complexType>
    
    <complexType name="eventDefinitionCrfsType">
        <sequence>
            <element name="eventDefinitionCrf" type="beans:eventDefinitionCrfType" minOccurs="0" maxOccurs="unbounded"/>
        </sequence>
    </complexType>
    
    <complexType name="eventDefinitionCrfType">
        <sequence>
            <element name="required" type="boolean"/>
            <element name="doubleDataEntry" type="boolean"/>
            <element name="passwordRequired" type="boolean"/>
            <element name="hideCrf" type="boolean"/>
            <element name="participantForm" type="boolean"/>
            <element name="allowAnonymousSubmission" type="boolean"/>
            <element name="submissionUrl" type="beans:customStringType"/>
            <element name="offline" type="boolean"/>
            <element name="sourceDataVerification" type="beans:customStringType"/>
            <element name="crf" type="beans:crfObjType" minOccurs="1"/>
            <element name="defaultCrfVersion" type="beans:crfVersionType" minOccurs="1"/>
        </sequence>
    </complexType>
    
    <complexType name="crfsType">
        <sequence>
            <element name="crf" type="beans:crfObjType" minOccurs="0" maxOccurs="1"/>
        </sequence>
    </complexType>
    
    <complexType name="crfObjType">
        <sequence>
            <element name="oid" type="beans:customStringType"/>
            <element name="name" type="beans:customStringType"/>
        </sequence>
    </complexType>
    
    <complexType name="crfVersionType">
        <sequence>
            <element name="oid" type="beans:customStringType"/>
            <element name="name" type="beans:customStringType"/>
        </sequence>
    </complexType>
    
    <complexType name="listStudySubjectsInStudyType">
        <sequence>
            <element name="studyRef" type="beans:studyRefType"/>
        </sequence>
    </complexType>
    
    <complexType name="studyEventDefinitionListAllType">
        <sequence>
            <element name="studyRef" type="beans:studyRefType"/>
        </sequence>
    </complexType>
    
    <complexType name="studyMetadataType">
        <sequence>
            <element name="studyRef" type="beans:studyRefType"/>
        </sequence>
    </complexType>

    <complexType name="studyRefType">
        <sequence>
            <element name="identifier" type="beans:customStringType"/>
            <element name="siteRef" type="beans:siteRefType" minOccurs="0"/>
        </sequence>
    </complexType>

    <complexType name="siteRefType">
        <sequence>
            <element name="identifier" type="beans:customStringType"/>
        </sequence>
    </complexType>

    <complexType name="studySubjectRefType">
        <sequence>
            <element name="label" type="beans:customStringType"/>
        </sequence>
    </complexType>

    <simpleType name="genderType">
        <restriction base="string">
            <enumeration value="m"/>
            <enumeration value="f"/>
        </restriction>
    </simpleType>

    <simpleType name="customStringType">
        <restriction base="normalizedString">
            <minLength value="1"/>
            <whiteSpace value="collapse"/>
        </restriction>
    </simpleType>

    <simpleType name="customDateType">
        <restriction base="integer">
            <pattern value="[1-2][0-9][0-9][0-9]"/>
            <whiteSpace value="collapse"/>
        </restriction>
    </simpleType>

    <simpleType name="customTimeType">
        <restriction base="integer">
            <pattern value="[0-2][0-9]:[0-9][0-9]"/>
            <whiteSpace value="collapse"/>
        </restriction>
    </simpleType>

</schema>

```

## crf.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema" 
        targetNamespace="http://openclinica.org/ws/crf/v1"
        xmlns:crf="http://openclinica.org/ws/crf/v1" 
        xmlns:xmime="http://www.w3.org/2005/05/xmlmime" elementFormDefault="qualified">


    <element name="createCrfRequest" type="crf:CrfType"/>
    
    <element name="createCrfResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="key" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
            </sequence>
        </complexType>
    </element>
    
    <complexType name="CrfType">
        <sequence>
            <element name="fileName" type="string"/>
            <element name="file" type="base64Binary" xmime:expectedContentTypes="application/xls"/>
        </sequence>
    </complexType>
    
</schema>
```

## data.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema"
    targetNamespace="http://openclinica.org/ws/data/v1"
    xmlns:data="http://openclinica.org/ws/data/v1" 
    xmlns:beans="http://openclinica.org/ws/beans"
    elementFormDefault="qualified">
    
    <import namespace="http://openclinica.org/ws/beans" schemaLocation="beans.xsd"></import>

    <element name="importRequest"/>

    <element name="importResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="auditMessages" type="data:auditMessagesType" minOccurs="0"/>
            </sequence>
                
        </complexType>
    </element>
    
    <complexType name="auditMessagesType">
        <sequence>
            <element name="auditMessage" type="string" />
        </sequence>
    </complexType>
    

</schema>

```

## event.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema"
    targetNamespace="http://openclinica.org/ws/event/v1"
    xmlns:events="http://openclinica.org/ws/event/v1" 
    xmlns:beans="http://openclinica.org/ws/beans"
    elementFormDefault="qualified">
    
    <import namespace="http://openclinica.org/ws/beans" schemaLocation="beans.xsd"></import>
    
    <element name="scheduleRequest">
        <complexType>
            <sequence>
                <element name="event" type="beans:eventType" maxOccurs="unbounded"/>
            </sequence>
        </complexType>
    </element>
    
    <element name="scheduleResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="eventDefinitionOID" type="string"/>
                <element name="studySubjectOID" type="string"/>
                <element name="studyEventOrdinal" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
            </sequence>
        </complexType>
    </element>
    
</schema>

```

## study.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema"
    targetNamespace="http://openclinica.org/ws/study/v1"
    xmlns:studySubjects="http://openclinica.org/ws/study/v1" 
    xmlns:beans="http://openclinica.org/ws/beans"
    elementFormDefault="qualified">
    
    <import namespace="http://openclinica.org/ws/beans" schemaLocation="beans.xsd"></import>

    <element name="listAllRequest"/>

    <element name="listAllResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="studies" type="beans:studiesType" />
            </sequence>
                
        </complexType>
    </element>
    
    <element name="getMetadataRequest">
        <complexType>
            <sequence>
               <!--   <element name="studyMetadata" type="beans:studyMetadataType"/> -->
                <element name="studyMetadata" type="beans:siteRefType"/>
            </sequence>
        </complexType>
    </element>
    
    <element name="getMetadataResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
            </sequence>
        </complexType>
    </element>

</schema>

```

## studyEventDefinition.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema"
    targetNamespace="http://openclinica.org/ws/studyEventDefinition/v1"
    xmlns:subjects="http://openclinica.org/ws/studyEventDefinition/v1" 
    xmlns:beans="http://openclinica.org/ws/beans"
    elementFormDefault="qualified">
    
    <import namespace="http://openclinica.org/ws/beans" schemaLocation="beans.xsd"></import>
    
    <element name="listAllRequest">
        <complexType>
            <sequence>
                <element name="studyEventDefinitionListAll" type="beans:studyEventDefinitionListAllType"/>
            </sequence>
        </complexType>
    </element>
    
    <element name="listAllResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="studyEventDefinitions" type="beans:studyEventDefinitionsType" />
            </sequence>
        </complexType>
    </element>
</schema>

```

## studySubject.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema"
    targetNamespace="http://openclinica.org/ws/studySubject/v1"
    xmlns:studySubjects="http://openclinica.org/ws/studySubject/v1" 
    xmlns:beans="http://openclinica.org/ws/beans"
    elementFormDefault="qualified">
    
    <import namespace="http://openclinica.org/ws/beans" schemaLocation="beans.xsd"></import>

    <element name="createRequest">
        <complexType>
            <sequence>
                <element name="studySubject" type="beans:studySubjectType"   minOccurs="1" maxOccurs="1"/>
            </sequence>
        </complexType>
    </element>

    <element name="createResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="label" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
            </sequence>
        </complexType>
    </element>
    
       
	 <element name="isStudySubjectRequest">
        <complexType>
            <sequence>
                <element name="studySubject" type="beans:studySubjectType"  minOccurs="1" maxOccurs="1"/>
           </sequence>
        </complexType>
    </element>
    
    <element name="isStudySubjectResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="studySubjectOID" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
            </sequence>
        </complexType>
    </element>
    
    <element name="listAllByStudyRequest" type="beans:listStudySubjectsInStudyType"/>
    
    <element name="listAllByStudyResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
                <element name="warning" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="error" type="string" minOccurs="0" maxOccurs="unbounded"/>
                <element name="studySubjects" type="beans:studySubjectsType" />
            </sequence>
        </complexType>
    </element>

</schema>

```

## subject.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema"
    targetNamespace="http://openclinica.org/ws/subject/v1"
    xmlns:subjects="http://openclinica.org/ws/subject/v1" 
    xmlns:beans="http://openclinica.org/ws/beans"
    elementFormDefault="qualified">
    
    <import namespace="http://openclinica.org/ws/beans" schemaLocation="beans.xsd"></import>
    
    <element name="creatRequest">
        <complexType>
            <sequence>
                <element name="subject" type="beans:subjectType"/>
                <element name="study" type="beans:studyRefType"/>
            </sequence>
        </complexType>
    </element>
    
    <element name="creatResponse">
        <complexType>
            <sequence>
                <element name="result" type="string"/>
            </sequence>
        </complexType>
    </element>
   <!--
    <complexType name="SubjectType">
        <sequence>
            <element name="personId" type="subjects:customStringType" minOccurs="0"/>
            <element name="studySubjectId" type="subjects:customStringType"/>
            <element name="secondaryId" type="subjects:customStringType" minOccurs="0"/>
            <element name="enrollmentDate" type="date"/>
            <element name="sex" type="subjects:genderType" minOccurs="0" />
            <choice minOccurs="0">
                <element name="dateOfBirth" type="date"/>
                <element type="subjects:customDateType" name="yearOfBirth"  />
            </choice>
        </sequence>
    </complexType>

    <complexType name="StudyType">
        <attribute name="uniqueIdentifier" type="subjects:customStringType"/>
    </complexType>
    
    <simpleType name="genderType">
        <restriction base="string">
            <enumeration value="m"/>
            <enumeration value="f"/>
        </restriction>
    </simpleType>
    
    <simpleType name="customStringType">
        <restriction base="normalizedString">
            <minLength value="1"/>
            <whiteSpace value="collapse"/> 
        </restriction>
    </simpleType>
    
    <simpleType name="customDateType">
        <restriction base="integer">
            <pattern value="[1-2][0-9][0-9][0-9]"></pattern>
            <whiteSpace value="collapse"/> 
        </restriction>
    </simpleType>
    -->
</schema>

```

