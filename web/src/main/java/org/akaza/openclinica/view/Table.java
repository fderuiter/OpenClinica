/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.control.form.FormProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class Table {
    public static final int NUM_ROWS_PER_PAGE = 10;

    protected ArrayList rows; // an array of Entities
    protected ArrayList columns; // an array of Strings which are column
    // headings, setup during initialization
    protected int numColumns; // provided for convenience in showTable class;
    // always equals columns.size()

    protected int currPageNumber; // which page are we viewing now?
    protected int totalPageNumbers; // how many page numbers are there total?
    // always equal to ceil(rows.size() /
    // NUM_ROWS_PER_PAGE)
    protected int sortingColumnInd; // index into arrColumns of the Column were
    // current sorting by
    protected boolean ascendingSort; // true for ascending sort, false
    // otherwise
    protected boolean filtered; // true if we use the keyword filter, false
    // otherwise
    protected String keywordFilter; // String the user wants to search for among
    // the rows

    protected String postAction;
    protected HashMap postArgs;
    protected String baseGetQuery;

    protected String noRowsMessage;
    protected String noColsMessage;

    public Table() {
        rows = new ArrayList();
        columns = new ArrayList();
        numColumns = 0;

        currPageNumber = 0;
        totalPageNumbers = 0;
        sortingColumnInd = 0;
        ascendingSort = true;
        filtered = false;
        keywordFilter = "";

        postAction = "";
        postArgs = new HashMap();
        baseGetQuery = "";

        noRowsMessage = "";
        noColsMessage = "";
    }

    public int getTotalPageNumbers() {
        return totalPageNumbers;
    }

    public ArrayList getColumns() {
        return columns;
    }

    public void setColumns(ArrayList columns) {
        this.columns = columns;
        numColumns = columns.size();
    }

    public boolean isAscendingSort() {
        return ascendingSort;
    }

    public int getCurrPageNumber() {
        return currPageNumber;
    }

    public String getKeywordFilter() {
        return keywordFilter;
    }

    public ArrayList getRows() {
        return rows;
    }

    private void updateTotalPageNumbers() {
        totalPageNumbers = rows.size() / NUM_ROWS_PER_PAGE;
    }

    public void setRows(ArrayList rows) {
        this.rows = rows;
        updateTotalPageNumbers();
    }

    public void addRow(EntityBean e) {
        rows.add(e);
        updateTotalPageNumbers();
    }

    public void processGetQuery(FormProcessor fp) {

    }

    public boolean isFiltered() {
        return filtered;
    }

    public int getSortingColumnInd() {
        return sortingColumnInd;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setQuery(String baseURL, HashMap args) {
        postAction = baseURL;
        postArgs = args;

        baseGetQuery = baseURL + "?";
        baseGetQuery += FormProcessor.FIELD_SUBMITTED + "=" + 1;

        Iterator it = args.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String value = (String) args.get(key);
            baseGetQuery += "&" + key + "=" + value;
        }

    }

    public String getBaseGetQuery() {
        return baseGetQuery;
    }

    public String getPostAction() {
        return postAction;
    }

    public HashMap getPostArgs() {
        return postArgs;
    }
}
