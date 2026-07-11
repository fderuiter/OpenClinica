package org.akaza.openclinica.domain.rule.action;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.ApplicationContextProvider;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.service.managestudy.DiscrepancyNoteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

public class DiscrepancyNoteActionProcessorTest {

    @Mock
    private RuleActionRunLogDao ruleActionRunLogDao;

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private PlatformTransactionManager mockTransactionManager;

    @Mock
    private TransactionStatus mockTransactionStatus;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ApplicationContextProvider provider = new ApplicationContextProvider();
        provider.setApplicationContext(mockApplicationContext);

        when(mockApplicationContext.getBean("transactionManager")).thenReturn(mockTransactionManager);
        when(mockTransactionManager.getTransaction(any())).thenReturn(mockTransactionStatus);
    }

    @Test
    public void testTransactionRollbackOnException() {
        // Setup
        RuleSetRuleBean ruleSetRule = new RuleSetRuleBean();
        RuleBean ruleBean = new RuleBean();
        ruleBean.setOid("R1");
        ruleSetRule.setRuleBean(ruleBean);

        DiscrepancyNoteActionProcessor processor = new DiscrepancyNoteActionProcessor(null, ruleActionRunLogDao, ruleSetRule);

        // Inject mock service
        DiscrepancyNoteService mockService = mock(DiscrepancyNoteService.class);
        processor.discrepancyNoteService = mockService;

        RuleActionBean ruleAction = new DiscrepancyNoteActionBean();
        ruleAction.setCuratedMessage("Test message");

        ItemDataBean itemDataBean = new ItemDataBean();
        itemDataBean.setId(1);
        itemDataBean.setValue("Value");

        StudyBean currentStudy = new StudyBean();
        UserAccountBean ub = new UserAccountBean();

        // Simulate an exception during save
        doThrow(new RuntimeException("Database error")).when(mockService)
                .saveFieldNotes("Test message", 1, "ItemData", currentStudy, ub);

        try {
            processor.execute(RuleRunnerMode.DATA_ENTRY, ExecutionMode.SAVE, ruleAction, itemDataBean, "ItemData", currentStudy, ub);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // Expected exception
            assert e.getMessage().equals("Database error");
        }

        // Verify that transaction was rolled back
        verify(mockTransactionManager).rollback(mockTransactionStatus);
    }
}
