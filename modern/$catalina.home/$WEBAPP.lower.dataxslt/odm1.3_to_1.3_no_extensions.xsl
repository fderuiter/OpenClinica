<xsl:stylesheet version="2.0"
	xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance" xmlns:def="http://www.cdisc.org/ns/def/v1.0"
	xmlns:xlink="http://www.w3c.org/1999/xlink" xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
	exclude-result-prefixes="xlink" xmlns:exsl="http://exslt.org/common">


	<!-- ****************************************************************************************************** -->
	<!-- File: odm1.3_to_1.2_extensions.xsl -->
	<!-- Date: 2011-04-15 -->
	<!-- Version: 1.0.0 -->
	<!-- Author: Pradnya Gawade(Akaza) -->
	<!-- Organization: Akaza Research -->
	<!-- Description: XSL sheetsheet to convert ODM 1.3 to ODM 1.3 without extensions. -->
	<!-- Notes: none yet -->
	<!-- Source Location: SVN repository -->
	<!-- Release Notes for version 1.0.0: -->
	<!-- 1. TBD -->
	<!-- ****************************************************************************************************** -->


	<!-- standard copy template -->
	<xsl:strip-space elements="*" />

	<xsl:template name="copyTemplate" match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:copy>
	</xsl:template>

    <!-- Handle ordering of elements for ODM standard schema compliance -->
    <xsl:template match="odm:SubjectData | odm:StudyEventData | odm:FormData | odm:ItemGroupData | odm:ItemData">
        <xsl:copy>
            <!-- 1. Copy standard attributes -->
            <xsl:apply-templates select="@*[not(namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1') and not(namespace-uri()='http://www.openclinica.org/ns/rules/v3.1')]" />
            
            <!-- 2. Generate AuditRecords from OpenClinica AuditLogs -->
            <xsl:apply-templates select="OpenClinica:AuditLogs/OpenClinica:AuditLog" mode="auditRecord" />
            
            <!-- 3. Generate Signature if element is signed -->
            <xsl:if test="@OpenClinica:Signed='Yes'">
                <xsl:variable name="sigAudit" select="OpenClinica:AuditLogs/OpenClinica:AuditLog[@AuditType='Event Signed' or @AuditType='Subject/Event Signed' or contains(@AuditType, 'Signed')]"/>
                <xsl:if test="$sigAudit">
                    <odm:Signature>
                        <odm:UserRef UserOID="{$sigAudit[last()]/@UserID}"/>
                        <odm:SignatureRef SignatureOID="SIG.{$sigAudit[last()]/@UserID}"/>
                        <odm:DateTimeStamp>
                            <xsl:value-of select="$sigAudit[last()]/@DateTimeStamp"/>
                        </odm:DateTimeStamp>
                    </odm:Signature>
                </xsl:if>
            </xsl:if>
            
            <!-- 4. Apply templates to all other child nodes except OpenClinica extensions -->
            <xsl:apply-templates select="node()[not(self::OpenClinica:*) and not(namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1') and not(namespace-uri()='http://www.openclinica.org/ns/rules/v3.1')]" />
        </xsl:copy>
    </xsl:template>

    <!-- Mode template to correctly format AuditRecords -->
    <xsl:template match="OpenClinica:AuditLog" mode="auditRecord">
        <odm:AuditRecord>
            <odm:UserRef UserOID="{@UserID}"/>
            <xsl:if test="ancestor::odm:SubjectData/odm:SiteRef/@LocationOID != ''">
                <odm:LocationRef LocationOID="{ancestor::odm:SubjectData/odm:SiteRef/@LocationOID}"/>
            </xsl:if>
            <odm:DateTimeStamp><xsl:value-of select="@DateTimeStamp"/></odm:DateTimeStamp>
            <xsl:if test="@ReasonForChange != ''">
                <odm:ReasonForChange><xsl:value-of select="@ReasonForChange"/></odm:ReasonForChange>
            </xsl:if>
            <xsl:if test="@ID != ''">
                <odm:SourceID><xsl:value-of select="@ID"/></odm:SourceID>
            </xsl:if>
        </odm:AuditRecord>
    </xsl:template>

	
<xsl:template name="removeOCExtnElmnt" priority="2" match="//*[namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' or namespace-uri()='http://www.openclinica.org/ns/rules/v3.1']" ></xsl:template>

<xsl:template name="removeOCExtnAttrib" priority="1" match="//@*[namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' or namespace-uri()='http://www.openclinica.org/ns/rules/v3.1']" ></xsl:template>
<!--
	<xsl:template name="namespaceTo1.2_no" priority="1"
		match="//@*[namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' ] ">
		<xsl:element name="{local-name()}" namespace="''">
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:element>
	</xsl:template>

	<xsl:template name="namespaceTo1.2_rules" priority="2"
		match="//@*[namespace-uri()='http://www.openclinica.org/ns/rules/v3.1' ] ">
		<xsl:element name="{local-name()}" namespace="''">
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:element>
	</xsl:template>
-->
<!--

	<xsl:template priority="4" match="@ODMVersion">
		<xsl:attribute name="ODMVersion">1.2</xsl:attribute>
	</xsl:template>
	-->
<xsl:template match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:RuleImport" priority="3"></xsl:template>


	
	<!--<xsl:template priority="4" match="ODM[@xmlns:OpenClinica='http://www.openclinica.org/ns/odm_ext_v130/v3.1']"/>-->

</xsl:stylesheet>