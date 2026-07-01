package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.rule.RuleEvaluationLogBean;
import org.akaza.openclinica.dao.hibernate.RuleEvaluationLogDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RuleEvaluationLoggerService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private RuleEvaluationLogDao ruleEvaluationLogDao;

    private static ConcurrentLinkedDeque<RuleEvaluationLogBean> DEQUE = new ConcurrentLinkedDeque<RuleEvaluationLogBean>();

    public static void addLog(RuleEvaluationLogBean log) {
        DEQUE.add(log);
    }

    @Scheduled(fixedDelay = 2000)
    public void processLogs() {
        RuleEvaluationLogBean log = DEQUE.pollFirst();
        if (log == null) {
            return;
        }

        ArrayList<RuleEvaluationLogBean> batch = new ArrayList<RuleEvaluationLogBean>();
        while (log != null) {
            batch.add(log);
            if (batch.size() >= 100) {
                break;
            }
            log = DEQUE.pollFirst();
        }

        for (RuleEvaluationLogBean item : batch) {
            try {
                ruleEvaluationLogDao.saveOrUpdate(item);
            } catch (Exception e) {
                logger.error("Error saving RuleEvaluationLogBean", e);
            }
        }
    }
}
