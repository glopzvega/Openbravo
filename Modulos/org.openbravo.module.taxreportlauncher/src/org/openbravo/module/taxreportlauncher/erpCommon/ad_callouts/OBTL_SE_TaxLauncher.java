/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.module.taxreportlauncher.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationAcctSchema;
import org.openbravo.model.financialmgmt.calendar.Calendar;
import org.openbravo.model.financialmgmt.calendar.Year;
import org.openbravo.module.taxreportlauncher.TaxReport;
import org.openbravo.module.taxreportlauncher.Dao.TaxReportLauncherDao;
import org.openbravo.xmlEngine.XmlDocument;

public class OBTL_SE_TaxLauncher extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  private TaxReportLauncherDao dao;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getStringParameter("inpOrganization");
      String strReportId = vars.getStringParameter("inpReport");
      String strAccSchemaId = vars.getStringParameter("inpcAcctSchema");
      // String strCalendarId = vars.getStringParameter("inpcCalendar");
      String strYearId = vars.getStringParameter("inpYear");
      String strPeriod = vars.getStringParameter("inpPeriod");
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      try {
        printPage(response, vars, strOrgId, strReportId, strAccSchemaId, strYearId, strPeriod,
            strChanged);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strReportId, String strAccSchemaId, String strYearId, String strPeriod,
      String strChanged) throws IOException, ServletException {

    dao = new TaxReportLauncherDao();

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='OBTL_SE_TaxLauncher';\n\n");
    resultado.append("var respuesta = new Array( \n");

    if (strChanged.equals("inpOrganization")) {
      // Update Reports, Acct Schemas, Calendar, Year and Periods

      if (strOrgId.equals("")) {
        // Clean all the fields
        resultado.append("new Array(\"inpcCalendar\", null), \n");
        resultado.append("\t new Array(\"inpReport\", null), \n");
        resultado.append("\t new Array(\"inpcAcctSchema\", null), \n");
        resultado.append("\t new Array(\"inpYear\", null), \n");
        resultado.append("\t new Array(\"inpPeriod\", null)");
      } else {
        // Update Calendar
        final Organization org = dao.getOrg(strOrgId);
        final Calendar cal = dao.getOrgCalendar(org);
        resultado.append("new Array(\"inpcCalendar\", ");
        if (cal != null) {
          resultado.append("new Array(");
          resultado.append("new Array(\"" + cal.getId() + "\", \"" + cal.getName() + "\")");
          resultado.append(", \"true\" )");
        } else {
          resultado.append("null");
        }
        resultado.append("), \n");

        // Update Reports
        final List<TaxReport> taxReportList = dao.getOrgTaxReportsOrderByName(org);
        resultado.append("new Array(\"inpReport\", ");
        if (taxReportList == null || taxReportList.isEmpty()) {
          resultado.append("null");
          strReportId = "";
        } else {
          resultado.append("new Array(");
          int i = 0;
          for (Iterator<TaxReport> iterator = taxReportList.iterator(); iterator.hasNext();) {
            TaxReport report = iterator.next();
            if (i++ == 0)
              strReportId = report.getId();
            resultado.append("new Array(\"" + report.getId() + "\", \"" + report.getName() + "\" ");
            if (i == 1) {
              resultado.append(", \"true\" ");
            }
            resultado.append(")");

            if (iterator.hasNext())
              resultado.append(",\n \t");
          }
          resultado.append(")");
        }
        resultado.append("), \n");

        // Update Accounting Schemas
        final List<OrganizationAcctSchema> orgSchemaList = dao.getOrgAcctSchemas(org);
        resultado.append("new Array(\"inpcAcctSchema\", ");
        if (orgSchemaList == null || orgSchemaList.isEmpty()) {
          resultado.append("null");
        } else {
          int i = 0;
          resultado.append("new Array(");
          for (Iterator<OrganizationAcctSchema> iterator = orgSchemaList.iterator(); iterator
              .hasNext();) {
            OrganizationAcctSchema orgSchema = iterator.next();
            resultado.append("new Array(\"" + orgSchema.getAccountingSchema().getId() + "\", \""
                + orgSchema.getAccountingSchema().getName() + "\"");
            if (i++ == 0) {
              resultado.append(", \"true\" ");
            }
            resultado.append(")");
            if (iterator.hasNext())
              resultado.append(",\n \t");
          }
          resultado.append(")");
        }
        resultado.append("), \n");

        // Update Years
        final List<Year> yearList = dao.getYearsOrderByDateDesc(cal);
        resultado.append("new Array(\"inpYear\", ");
        if (yearList == null || yearList.isEmpty()) {
          resultado.append("null");
        } else {
          resultado.append("new Array(");
          int i = 0;
          for (Iterator<Year> iterator = yearList.iterator(); iterator.hasNext();) {
            Year year = iterator.next();
            if (i++ == 0)
              strYearId = year.getId();
            resultado.append("new Array(\"" + year.getId() + "\", \"" + year.getFiscalYear()
                + "\" ");
            if (i == 1) {
              resultado.append(", \"true\" ");
            }
            resultado.append(")");
            if (iterator.hasNext())
              resultado.append(",\n \t");
          }
          resultado.append(")");
        }
        resultado.append("), \n");

        // Update Report's Periods
        final List<String[]> periodList = dao.getReportPeriods(strReportId, strYearId, " - ", vars
            .getLanguage());
        resultado.append("new Array(\"inpPeriod\", ");
        if (periodList == null || periodList.isEmpty()) {
          resultado.append("null");
        } else {
          resultado.append("new Array(");
          int i = 0;
          for (Iterator<String[]> iterator = periodList.iterator(); iterator.hasNext();) {
            String[] data = iterator.next();
            resultado.append("new Array(\"" + data[0] + "\", \"" + data[1] + "\" ");
            if (i++ == 0)
              resultado.append(", \"true\" ");
            resultado.append(")");
            if (iterator.hasNext())
              resultado.append(",\n \t");
          }
          resultado.append(")");
        }
        resultado.append(")");
      }

    } else if (strChanged.equals("inpReport") || strChanged.equals("inpYear")) {
      if (strYearId == null || strYearId.equals("")) {
        resultado.append("\t new Array(\"inpPeriod\", null)");
      } else {
        // Update Report's Periods
        final List<String[]> periodList = dao.getReportPeriods(strReportId, strYearId, " - ", vars
            .getLanguage());
        resultado.append("new Array(\"inpPeriod\", ");
        if (periodList == null || periodList.isEmpty()) {
          resultado.append("null");
        } else {
          resultado.append("new Array(");
          int i = 0;
          for (Iterator<String[]> iterator = periodList.iterator(); iterator.hasNext();) {
            String[] data = iterator.next();
            resultado.append("new Array(\"" + data[0] + "\", \"" + data[1] + "\" ");
            if (i++ == 0)
              resultado.append(", \"true\" ");
            resultado.append(")");

            if (iterator.hasNext())
              resultado.append(",\n \t");
          }
          resultado.append(")");
        }
        resultado.append(")");

      }
    }

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    xmlDocument.setParameter("formName", "frmMain");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
