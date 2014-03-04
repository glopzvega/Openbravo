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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.module.taxreportlauncher.erpCommon.ad_reports;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.module.taxreportlauncher.TaxReportParameter;
import org.openbravo.module.taxreportlauncher.Dao.TaxReportLauncherDao;
import org.openbravo.module.taxreportlauncher.Exception.OBTL_Exception;
import org.openbravo.module.taxreportlauncher.Utility.OBTL_Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class OBTL_TaxReportLauncher extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private TaxReportLauncherDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      // By default, always display empty fields
      printPageDataSheet(response, vars);
    } else if (vars.commandIn("FIND")) {
      String strOrgId = vars.getRequestGlobalVariable("inpOrganization", "TaxReportLauncher|Org");
      String strReportId = vars.getRequestGlobalVariable("inpReport", "TaxReportLauncher|Report");
      String strAccSchemaId = vars.getRequestGlobalVariable("inpcAcctSchema",
          "TaxReportLauncher|AcctSchema");
      String strCalendarId = vars.getRequestGlobalVariable("inpcCalendar",
          "TaxReportLauncher|Calendar");
      String strYearId = vars.getRequestGlobalVariable("inpYear", "TaxReportLauncher|Year");
      String strPeriod = vars.getRequestGlobalVariable("inpPeriod", "TaxReportLauncher|Period");

      // Mandatory to store them as session variable
      vars.setSessionValue("TaxReportLauncher|AcctSchema", strAccSchemaId);
      vars.setSessionValue("TaxReportLauncher|Org", strOrgId);
      vars.setSessionValue("TaxReportLauncher|Report", strReportId);
      vars.setSessionValue("TaxReportLauncher|Calendar", strCalendarId);
      vars.setSessionValue("TaxReportLauncher|Year", strYearId);
      vars.setSessionValue("TaxReportLauncher|Period", strPeriod);
      printPagePopUp(response, vars);
    } else if (vars.commandIn("GENERATE")) {
      String strOrgId = vars.getSessionValue("TaxReportLauncher|Org");
      String strReportId = vars.getSessionValue("TaxReportLauncher|Report");
      String strAccSchemaId = vars.getSessionValue("TaxReportLauncher|AcctSchema");
      String strCalendarId = vars.getSessionValue("TaxReportLauncher|Calendar");
      String strYearId = vars.getSessionValue("TaxReportLauncher|Year");
      String strPeriod = vars.getSessionValue("TaxReportLauncher|Period");

      generateFile(response, vars, strOrgId, strReportId, strAccSchemaId, strCalendarId, strYearId,
          strPeriod);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    dao = new TaxReportLauncherDao();

    if (log4j.isDebugEnabled())
      log4j.debug("Output: Displaying Tax Report Launcher window");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    String[] discard = { "" };

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/module/taxreportlauncher/erpCommon/ad_reports/OBTL_TaxReportLauncher",
        discard).createXmlDocument();

    try {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "TaxReportLauncher", false, "", "",
          "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.module.taxreportlauncher.erpCommon.ad_reports.OBTL_TaxReportLauncher");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());

      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "OBTL_TaxReportLauncher.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());

      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "OBTL_TaxReportLauncher.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());

      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("TaxReportLauncher");
      vars.removeMessage("TaxReportLauncher");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    // Get Organizations (only Legal Entities with accounting)
    xmlDocument.setData("reportAD_Org_ID", "liststructure",
        FieldProviderFactory.getFieldProviderArray(dao.getLegalEntityOrgs(true).toArray()));

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePopUp(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    // TODO check association of tax report parameters with tax rates. Show warning in case of no
    // association

    dao = new TaxReportLauncherDao();
    boolean bolErr = false;

    if (log4j.isDebugEnabled())
      log4j.debug("Output: Displaying Tax Report Launcher window");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    String[] discard = { "" };

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/module/taxreportlauncher/erpCommon/ad_reports/OBTL_TaxReportLauncher_PopUp",
        discard).createXmlDocument();

    try {
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("TaxReportLauncher");
      vars.removeMessage("TaxReportLauncher");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        bolErr = true;
      }
    }

    // Print input parameters of the report
    try {
      String strReport = vars.getSessionValue("TaxReportLauncher|Report");
      OBTLTaxReportLauncherData[] data = OBTLTaxReportLauncherData.select(this, strReport);
      xmlDocument.setData("structure", data);
      // If the window is being repainted due to an exception, then the values user entered are
      // repainted.
      if (bolErr) {
        final List<TaxReportParameter> reportParameters = dao.getTaxReportParameters(strReport,
            "INPUT");
        for (int i = 0; i < reportParameters.size(); i++) {
          final TaxReportParameter trp = reportParameters.get(i);
          if (trp.getInputtype().equals("T"))
            data[i].defaulttext = vars.getStringParameter("text" + trp.getId(), "");
          else if (trp.getInputtype().equals("C"))
            data[i].defaultcheck = vars.getStringParameter("check" + trp.getId(), "");
        }
      }
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  @SuppressWarnings("unchecked")
  private void generateFile(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strReportId, String strAccSchemaId, String strCalendarId, String strYearId,
      String strPeriod) throws IOException, ServletException {

    dao = new TaxReportLauncherDao();

    /* Reading input parameters */
    String strErr = "";
    Hashtable<String, String> inputParams = new Hashtable<String, String>();
    final List<TaxReportParameter> reportParameters = dao.getTaxReportParameters(strReportId,
        "INPUT");
    for (int i = 0; i < reportParameters.size(); i++) {
      final TaxReportParameter trp = reportParameters.get(i);
      String paramName = "";
      if (trp.getInputtype().equals("T"))
        paramName = "text";
      else if (trp.getInputtype().equals("C"))
        paramName = "check";
      else {
        strErr = trp.getName();
        break;
      }
      if (!(vars.getStringParameter(paramName + trp.getId()).equals("") && trp.getInputtype()
          .equals("T")))
        inputParams.put(trp.getSearchKey(), vars.getStringParameter(paramName + trp.getId()));
    }

    final String strClassName = dao.getTaxReportClass(strReportId);
    OBTL_TaxReport_I classReport;
    try {
      classReport = (OBTL_TaxReport_I) Class.forName(strClassName).newInstance();
      HashMap<String, Object> reportMap = classReport.generateElectronicFile(strOrgId, strReportId,
          strAccSchemaId, strYearId, strPeriod, inputParams);

      String fileName = OBTL_Utility.getReportValidFileName((String) reportMap.get("fileName"),
          strReportId);

      String extension;
      try {
        extension = fileName.substring(fileName.lastIndexOf("."));
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
      } catch (final IndexOutOfBoundsException iobe) {
        extension = null;
      }

      final UUID reportId = UUID.randomUUID();
      if (reportMap.get("file") != null) {
        // Generate txt file
        extension = (extension == null || "".equals(extension)) ? ".txt" : extension;
        Utility.generateFile(globalParameters.strFTPDirectory, fileName + "-" + reportId
            + extension, reportMap.get("file").toString());
      } else if (reportMap.get("files") != null) {
        // Generate zip file
        extension = ".zip";

        HashSet<File> files = new HashSet<File>();
        for (final File file : (Collection<File>) reportMap.get("files")) {
          files.add(file);
        }
        OBTL_Utility.createZIPfile(globalParameters.strFTPDirectory, fileName + "-" + reportId
            + extension, files);
      } else {
        throw new OBException("No generated file");
      }

      response.setContentType("text/html;charset=UTF-8");
      response.setHeader("Content-disposition", "inline" + "; filename=" + fileName);
      printPagePopUpDownload(response.getOutputStream(), fileName + "-" + reportId + extension);
    } catch (Exception e) {
      this.throwOBError(response, vars, e);
    }
  }

  private void throwOBError(HttpServletResponse response, VariablesSecureApp vars, Exception e)
      throws IOException, ServletException {

    OBError obError = new OBError();
    obError.setType("Error");

    if (e instanceof OBTL_Exception) {
      obError.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(),
          ((OBTL_Exception) e).getTitle()));
      obError.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), e.getMessage()));
    } else if (e instanceof ClassNotFoundException) {
      obError.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(),
          "@OBTL_ClassNotFound@"));
      obError.setMessage(e.getMessage());
    } else if (e instanceof StringIndexOutOfBoundsException) {
      obError.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(),
          "@OBTL_StringIndexOutOfBoundsTitle@"));
      String eMsg = e.getMessage();
      Integer position = new Integer(StringUtils.deleteWhitespace(eMsg.substring(
          eMsg.lastIndexOf(":") + 1, eMsg.length()))) + 1;
      obError.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
          "@OBTL_StringIndexOutOfBoundsMessage@" + position));
    } else {
      e.printStackTrace();
      throw new ServletException(e);
    }

    vars.setMessage("TaxReportLauncher", obError);
    this.printPagePopUp(response, vars);
  }
}
