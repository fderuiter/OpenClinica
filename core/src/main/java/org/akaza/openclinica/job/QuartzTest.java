package org.akaza.openclinica.job;
import org.quartz.JobDetail;
public class QuartzTest {
    public void test(JobDetail jd) {
        jd.getKey();
    }
}
