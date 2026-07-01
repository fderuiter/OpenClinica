/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.FilterBean;
import org.akaza.openclinica.control.extract.ApplyFilterServlet;
import org.akaza.openclinica.control.extract.EditFilterServlet;
import org.akaza.openclinica.control.extract.RemoveFilterServlet;

/**
 * The extension of Shai Sachs' Table class, Essentially builds the rows for
 * creating a filter table.
 *
 * @author thickerson
 *
 */
public class FilterTable extends Table {
    public FilterTable() {
        columns.add("Filter Name");
        columns.add("Description");
        columns.add("Owner");
        columns.add("Creation Date");
        columns.add("Status");
        columns.add("Actions");
    }



}
