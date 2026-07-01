/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.view.Table;

/**
 * @author thickerson
 *
 *
 */
public class DatasetTable extends Table {
    public DatasetTable() {
        columns.add("Dataset Name");
        columns.add("Description");
        columns.add("Owner");
        columns.add("Creation Date");
        columns.add("Status");
        columns.add("Actions");
    }


}
