package org.openbravo.module.taxreportlauncher.erpCommon.ad_reports;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.exception.OBException;

public interface OBTL_TaxReport_I {

  /**
   * Generates the electronic file (plain text) of the model.
   * 
   * @param strOrgId
   *          organization's ID to generate the report
   * @param strReportId
   *          report's ID of the model to generate
   * @param strAcctSchemaId
   *          Accounting Schema used to calculate the report
   * @param strYearId
   *          year's ID to calculate the report
   * @param strPeriodId
   *          period's ID. In case of non-monthly basis report, this string must contain a
   *          comma-separated list of the period's ID
   * @param inputParams
   *          Map containing all the input parameters manually introduced by the user when launching
   *          the report. The key String must be equals to the search key of the input parameter
   *          defined in the report
   * @return a Map which contains:
   *         <ul>
   *         <li><b>fileName</b>: (String) File name that will have the created report. It must be
   *         introduced by the user as an input parameter</li>
   *         <li><b>file</b>: StringBuffer which contains the generated report</li>
   *         <li><b>files</b>: Set<File> containing the set of files to be included in the
   *         compressed ZIP file
   *         </ul>
   *         Important: if the Map contains a <b>file</b> key, the tax report launcher will return a
   *         txt file containing the StringBuffer. It it doesn't contain a <b>file</b> key, the tax
   *         report launcher will search for the <b>files</b> key and it will generate a ZIP file
   *         with its set of files. This feature is available @since 1.0.14
   * @throws OBException
   *           in case of something wrong in the structure of the report. Example, length validation
   *           for a field, error while converting an input parameter to a number...
   * @throws Exception
   *           in case of other Exceptions
   */
  public HashMap<String, Object> generateElectronicFile(String strOrgId, String strReportId,
      String strAcctSchemaId, String strYearId, String strPeriodId, Map<String, String> inputParams)
      throws OBException, Exception;
}