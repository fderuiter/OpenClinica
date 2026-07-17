package org.akaza.openclinica.controller.migration;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.TermType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.web.SQLInitServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.akaza.openclinica.control.SpringServletAccess;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Properties;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.i18n.core.LocaleResolver;

@Controller
public class RequestAccountController {
    private StudyDAO _studyDAO;
    private UserAccountDAO _userAccountDAO;

    @Autowired
    public RequestAccountController(StudyDAO _studyDAO, UserAccountDAO _userAccountDAO) {
        this._studyDAO = _studyDAO;
        this._userAccountDAO = _userAccountDAO;
    }


    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @RequestMapping(value = "/RequestAccount", method = {RequestMethod.GET, RequestMethod.POST})
    public String requestAccount(HttpServletRequest request, HttpSession session) throws Exception {
        String action = request.getParameter("action");

        StudyDAO sdao = this._studyDAO;
        ArrayList studies = (ArrayList) sdao.findAll();
        ArrayList roles = Role.toArrayList();
        roles.remove(Role.ADMIN);

        request.setAttribute("roles", roles);
        request.setAttribute("studies", studies);

        if (StringUtil.isBlank(action)) {
            session.setAttribute("newUserBean", new UserAccountBean());
            return "login/requestAccount";
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                return confirmAccount(request, session);
            } else if ("submit".equalsIgnoreCase(action)) {
                return submitAccount(request, session);
            } else {
                return "login/requestAccount";
            }
        }
    }

    private String confirmAccount(HttpServletRequest request, HttpSession session) throws Exception {
        Validator v = new Validator(request);
        v.addValidation("name", Validator.NO_BLANKS);
        v.addValidation("firstName", Validator.NO_BLANKS);
        v.addValidation("lastName", Validator.NO_BLANKS);
        v.addValidation("email", Validator.IS_A_EMAIL);
        v.addValidation("email2", Validator.CHECK_SAME, "email");
        v.addValidation("institutionalAffiliation", Validator.NO_BLANKS);
        v.addValidation("activeStudyId", Validator.IS_AN_INTEGER);
        v.addValidation("activeStudyRole", Validator.IS_VALID_TERM, TermType.ROLE);

        HashMap errors = v.validate();
        FormProcessor fp = new FormProcessor(request);
        UserAccountBean ubForm = getUserBean(fp);
        request.setAttribute("otherStudy", fp.getString("otherStudy"));
        session.setAttribute("newUserBean", ubForm);

        if (!errors.isEmpty()) {
            request.setAttribute("formMessages", errors);
            return "login/requestAccount";
        } else {
            UserAccountDAO udao = this._userAccountDAO;
            UserAccountBean ubDB = (UserAccountBean) udao.findByUserName(ubForm.getName());

            if (ubDB == null || StringUtil.isBlank(ubDB.getName())) {
                StudyDAO sdao = this._studyDAO;
                StudyBean study = (StudyBean) sdao.findByPK(ubForm.getActiveStudyId());
                String studyName = study.getName();
                request.setAttribute("studyName", studyName);
                return "login/requestAccountConfirm";
            } else {
                ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(LocaleResolver.getLocale(request));
                request.setAttribute("formMessages", new HashMap<String, String>() {{ put("error", respage.getString("your_user_name_used_by_other_try_another")); }});
                return "login/requestAccount";
            }
        }
    }

    private String submitAccount(HttpServletRequest request, HttpSession session) throws Exception {
        String otherStudy = request.getParameter("otherStudy");
        String studyName = request.getParameter("studyName");
        UserAccountBean ubForm = (UserAccountBean) session.getAttribute("newUserBean");
        
        ResourceBundle resword = ResourceBundleProvider.getWordsBundle(LocaleResolver.getLocale(request));

        StringBuffer email = new StringBuffer("From: " + ubForm.getEmail() + "\n");
        email.append("Sent: " + new Date() + "\n");
        email.append("To: " + SQLInitServlet.getField("adminEmail") + "\n");
        email.append("Subject: Request Account\n\n\n");
        email.append("Dear Admin, \n\n");
        email.append(ubForm.getFirstName() + " is requesting for an account on the OpenClinica system running at " + SQLInitServlet.getField("sysURL")
            + ". \n\n");
        email.append("His/her information is shown as follows: \n\n");
        email.append(resword.getString("name") + ": " + ubForm.getFirstName() + " " + ubForm.getLastName());
        email.append("\n" + resword.getString("user_name") + ": " + ubForm.getName());
        email.append("\n" + resword.getString("email") + ": " + ubForm.getEmail());
        email.append("\n" + resword.getString("institutional_affiliation") + ": " + ubForm.getInstitutionalAffiliation());
        email.append("\n" + resword.getString("default_active_study") + ":" + studyName + ", id:" + ubForm.getActiveStudyId());
        email.append("\n" + resword.getString("other_study") + otherStudy);
        email.append("\n" + resword.getString("user_role_requested") + ubForm.getActiveStudyRoleName());
        String emailBody = email.toString();
        
        try {
            JavaMailSenderImpl mailSender = (JavaMailSenderImpl) SpringServletAccess.getApplicationContext(request.getSession().getServletContext()).getBean("mailSender");
            Properties javaMailProperties = mailSender.getJavaMailProperties();
            if(null != javaMailProperties){
            	if (javaMailProperties.get("mail.smtp.localhost") == null || ((String)javaMailProperties.get("mail.smtp.localhost")).equalsIgnoreCase("") ){
            		javaMailProperties.put("mail.smtp.localhost", "localhost");
            	}
            }
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setFrom(ubForm.getEmail().trim());
            
            String to = EmailEngine.getAdminEmail();
            String[] toArray = to.split(",");
            for(int i=0; i<toArray.length; i++) {
                toArray[i] = toArray[i].trim();
            }
            helper.setTo(toArray);
            
            helper.setSubject("request account");
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        session.removeAttribute("newUserBean");
        return "login/login";
    }

    private UserAccountBean getUserBean(FormProcessor fp) {
        UserAccountBean ubForm = new UserAccountBean();
        ubForm.setName(fp.getString("name"));
        ubForm.setFirstName(fp.getString("firstName"));
        ubForm.setLastName(fp.getString("lastName"));
        ubForm.setEmail(fp.getString("email"));
        ubForm.setInstitutionalAffiliation(fp.getString("institutionalAffiliation"));
        ubForm.setActiveStudyId(fp.getInt("activeStudyId"));
        StudyUserRoleBean uRole = new StudyUserRoleBean();
        uRole.setStudyId(fp.getInt("activeStudyId"));
        uRole.setRole(Role.get(fp.getInt("activeStudyRole")));
        ubForm.addRole(uRole);
        return ubForm;
    }
}
