package org.akaza.openclinica.service.rule;

import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RuleSetServiceTest {

    @Test
    public void testFilterByStatusEqualsAvailable() {
        RuleSetService service = new RuleSetService();

        RuleSetBean availableSet = new RuleSetBean();
        availableSet.setStatus(Status.AVAILABLE);
        
        RuleSetRuleBean availableRule = new RuleSetRuleBean();
        availableRule.setStatus(Status.AVAILABLE);
        RuleSetRuleBean deletedRule = new RuleSetRuleBean();
        deletedRule.setStatus(Status.DELETED);
        
        availableSet.addRuleSetRule(availableRule);
        availableSet.addRuleSetRule(deletedRule);

        RuleSetBean deletedSet = new RuleSetBean();
        deletedSet.setStatus(Status.DELETED);

        List<RuleSetBean> ruleSets = new ArrayList<>();
        ruleSets.add(availableSet);
        ruleSets.add(deletedSet);

        List<RuleSetBean> result = service.filterByStatusEqualsAvailable(ruleSets);

        assertEquals("Only one RuleSet should remain", 1, result.size());
        assertEquals("Remaining RuleSet should be AVAILABLE", Status.AVAILABLE, result.get(0).getStatus());
        assertEquals("Only one RuleSetRule should remain", 1, result.get(0).getRuleSetRules().size());
        assertEquals("Remaining RuleSetRule should be AVAILABLE", Status.AVAILABLE, result.get(0).getRuleSetRules().get(0).getStatus());
    }
}
