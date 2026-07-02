package org.akaza.openclinica.logic.importdata;

import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;

public abstract class SubjectDataProcessor<R> {
    private R result;
    private boolean stop = false;

    public abstract void process(SubjectDataBean bean);

    protected void stop(R result) {
        this.result = result;
        this.stop = true;
    }

    public boolean isStop() {
        return stop;
    }

    public R getResult() {
        return result;
    }
    
    public void setResult(R result) {
        this.result = result;
    }

    public static <R> R process(java.util.List<SubjectDataBean> list, SubjectDataProcessor<R> processor) {
        if (list instanceof StreamingSubjectDataList) {
            return ((StreamingSubjectDataList) list).process(processor);
        } else {
            for (SubjectDataBean bean : list) {
                processor.process(bean);
                if (processor.isStop()) {
                    return processor.getResult();
                }
            }
            return processor.getResult();
        }
    }
}
