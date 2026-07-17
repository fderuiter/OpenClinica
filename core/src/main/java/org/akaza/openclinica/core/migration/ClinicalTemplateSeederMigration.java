package org.akaza.openclinica.core.migration;

import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Date;
import java.util.ResourceBundle;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.control.admin.SpreadSheetTableRepeating;
import org.akaza.openclinica.control.admin.SpreadSheetTableClassic;
import org.akaza.openclinica.bean.admin.NewCRFBean;
import org.akaza.openclinica.dao.hibernate.MeasurementUnitDao;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

@Component
public class ClinicalTemplateSeederMigration extends AbstractJavaManagedDataMigration {
    private ItemDataDAO _itemDataDAO;

    private CRFDAO _cRFDAO;
    private CRFVersionDAO _cRFVersionDAO;
    private ItemDAO _itemDAO;
    private ItemFormMetadataDAO _itemFormMetadataDAO;
    private ItemGroupDAO _itemGroupDAO;

    private UserAccountDAO _userAccountDAO;

    private StudyDAO _studyDAO;

    @Autowired
    public ClinicalTemplateSeederMigration(StudyDAO _studyDAO, UserAccountDAO _userAccountDAO, CRFDAO _cRFDAO, CRFVersionDAO _cRFVersionDAO, ItemDAO _itemDAO, ItemFormMetadataDAO _itemFormMetadataDAO, ItemGroupDAO _itemGroupDAO, ItemDataDAO _itemDataDAO) {
        super(_userAccountDAO);
        this._itemDataDAO = _itemDataDAO;

        this._cRFDAO = _cRFDAO;
        this._cRFVersionDAO = _cRFVersionDAO;
        this._itemDAO = _itemDAO;
        this._itemFormMetadataDAO = _itemFormMetadataDAO;
        this._itemGroupDAO = _itemGroupDAO;

        this._userAccountDAO = _userAccountDAO;

        this._studyDAO = _studyDAO;
    }


    @Override
    protected void doMigration() throws Exception {
        String seedEnabled = System.getenv("SEED_CLINICAL_DATA");
        String templatePath = System.getenv("CLINICAL_TEMPLATE_PATH");

        if (seedEnabled == null || (!seedEnabled.equalsIgnoreCase("y") && !seedEnabled.equalsIgnoreCase("yes") && !seedEnabled.equalsIgnoreCase("true"))) {
            System.out.println("Clinical data seeding skipped (SEED_CLINICAL_DATA is not enabled).");
            return;
        }

        if (templatePath == null || templatePath.isEmpty()) {
            System.out.println("Clinical data seeding skipped: No template path provided.");
            return;
        }
        
        System.out.println("Starting interactive clinical data seeding from template: " + templatePath);
        
        // 1. Create a Seeded Study
        StudyDAO studyDAO = this._studyDAO;
        StudyBean study = new StudyBean();
        study.setName("Seeded Study " + System.currentTimeMillis());
        study.setIdentifier("SEED-" + System.currentTimeMillis());
        study.setOid("S_" + study.getIdentifier());
        study.setSummary("Study automatically generated during seeding");
        study.setStatus(Status.AVAILABLE);
        study.setOwner(systemUser);
        study.setCreatedDate(new Date());
        
        // Required constraints step one
        study.setExpectedTotalEnrollment(100);
        study.setSponsor("Default Sponsor");
        study.setParentStudyId(0);
        study.setProtocolDateVerification(new Date());

        study = (StudyBean) studyDAO.create(study);
        
        System.out.println("Created Study: " + study.getName() + " (ID: " + study.getId() + ")");
        
        // 2. Parse Excel
        ResourceBundle resPageMsg = ResourceBundleProvider.getPageMessagesBundle(Locale.ENGLISH);
        if (resPageMsg == null) {
            resPageMsg = ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages", Locale.ENGLISH);
        }

        FileInputStream inStream = new FileInputStream(templatePath);
        SpreadSheetTableRepeating htab = new SpreadSheetTableRepeating(inStream, systemUser, "1.0", Locale.ENGLISH, study.getId(), _cRFDAO, _cRFVersionDAO, _itemDAO, _itemFormMetadataDAO, _itemGroupDAO);
        // Note: MeasurementUnitDao requires SessionFactory, but wait! We can bypass it if it's not strictly used or we can initialize it?
        // Actually spreadSheetTableRepeating only needs it if we use units. We can try bypassing or getting it from Spring.
        // Let's get it from ApplicationContextProvider!
        MeasurementUnitDao muDao = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("measurementUnitDao", MeasurementUnitDao.class);
        htab.setMeasurementUnitDao(muDao);
        
        NewCRFBean nib = null;
        if (htab.isRepeating()) {
            nib = htab.toNewCRF(dataSource, resPageMsg);
        } else {
            FileInputStream inStreamClassic = new FileInputStream(templatePath);
            SpreadSheetTableClassic sstc = new SpreadSheetTableClassic(inStreamClassic, systemUser, "1.0", Locale.ENGLISH, study.getId(), _cRFDAO, _cRFVersionDAO, _itemDAO, _itemDataDAO, _itemGroupDAO);
            sstc.setMeasurementUnitDao(muDao);
            nib = sstc.toNewCRF(dataSource, resPageMsg);
            inStreamClassic.close();
        }
        inStream.close();
        
        if (nib.getErrors() != null && !nib.getErrors().isEmpty()) {
            System.err.println("Errors found during Excel parsing:");
            for (Object err : nib.getErrors()) {
                System.err.println("- " + err);
            }
            throw new RuntimeException("Excel parsing failed.");
        }
        
        // 3. Insert to DB
        nib.insertToDB();
        System.out.println("Successfully seeded clinical data template!");
    }
}
