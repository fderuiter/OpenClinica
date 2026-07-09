package org.akaza.openclinica.view.tags;

import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class SubjectDataForEachTag extends SimpleTagSupport {

    private Iterable<SubjectDataBean> items;
    private String var;

    public void setItems(Iterable<SubjectDataBean> items) {
        this.items = items;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public void doTag() throws JspException, IOException {
        if (items != null) {
            items.forEach(item -> {
                getJspContext().setAttribute(var, item);
                try {
                    getJspBody().invoke(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
