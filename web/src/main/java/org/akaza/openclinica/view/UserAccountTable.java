/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

import org.akaza.openclinica.bean.core.EntityAction;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.admin.DeleteStudyUserRoleServlet;
import org.akaza.openclinica.control.admin.DeleteUserServlet;
import org.akaza.openclinica.control.admin.EditStudyUserRoleServlet;
import org.akaza.openclinica.control.admin.EditUserAccountServlet;
import org.akaza.openclinica.control.admin.ViewUserAccountServlet;

import java.util.ArrayList;

public class UserAccountTable extends Table {
    public UserAccountTable() {
        columns.add("Username");
        columns.add("First Name");
        columns.add("Last Name");
        columns.add("Status");
        columns.add("Actions");
    }

}