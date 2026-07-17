package org.akaza.openclinica.controller.migration;

import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.view.StudyInfoPanelLine;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;

@Controller
public class EnterpriseController {

    @RequestMapping(value = "/Enterprise", method = {RequestMethod.GET, RequestMethod.POST})
    public String enterprise(HttpServletRequest request, HttpSession session) {
        StudyInfoPanel panel = new StudyInfoPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        
        ArrayList<StudyInfoPanelLine> data = panel.getUserOrderedData();
        data.add(new StudyInfoPanelLine("", ""));
        panel.setUserOrderedData(data);
        
        session.setAttribute("panel", panel);
        
        return "login/enterprise";
    }
}
