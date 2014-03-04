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

package org.openbravo.module.taxreportlauncher.Dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationAcctSchema;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.calendar.Calendar;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.calendar.Year;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.module.taxreportlauncher.TaxParameter;
import org.openbravo.module.taxreportlauncher.TaxReport;
import org.openbravo.module.taxreportlauncher.TaxReportGroup;
import org.openbravo.module.taxreportlauncher.TaxReportParameter;
import org.openbravo.module.taxreportlauncher.TributaryKey;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * This class contains the specific code used by the Tax Report Launcher to access the database
 * using DAL (Hibernate)
 * 
 * @author openbravo
 * 
 */
public class TaxReportLauncherDao {

  public TaxReportLauncherDao() {
  }

  /**
   * Returns the Organization object which corresponds to the given ID
   * 
   * @param strOrgId
   *          Organization's ID
   * @return an Organization object, or null if no Organization is found
   */
  public Organization getOrg(String strOrgId) {
    try {
      OBContext.setAdminMode(true);
      return OBDal.getInstance().get(Organization.class, strOrgId);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the Calendar of the given Organization
   * 
   * @param org
   *          Organization object
   * @return the Calendar object of the given organization, or null if no Calendar is found
   */
  public Calendar getOrgCalendar(Organization organization) {
    try {
      OBContext.setAdminMode(true);
      return organization.getCalendar();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the Calendar of the given Organization
   * 
   * @param strOrgId
   *          Organization's ID
   * @return the Calendar object of the given organization, or null if no Calendar is found
   */
  public Calendar getOrgCalendar(String strOrgId) {
    try {
      OBContext.setAdminMode(true);
      return this.getOrg(strOrgId).getCalendar();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a List of Tax Reports configured for the given organization
   * 
   * @param organization
   *          Organization object
   * @return List of Tax Reports for the given organization
   */
  public List<TaxReport> getOrgTaxReportsOrderByName(Organization organization) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<TaxReport> obc = OBDal.getInstance().createCriteria(TaxReport.class);
      obc.add(Expression.eq(TaxReport.PROPERTY_ORGANIZATION, organization));
      obc.addOrderBy("name", true);
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a List of Tax Reports configured for the given organization
   * 
   * @param strOrgId
   *          Organization's ID
   * @return List of Tax Reports for the given organization
   */
  public List<TaxReport> getOrgTaxReportsOrderByName(String strOrgId) {
    return this.getOrgTaxReportsOrderByName(this.getOrg(strOrgId));
  }

  /**
   * Returns a List of relations between the given organization and its accounting schemas (and also
   * the accounting schemas of its ancestors)
   * 
   * @param organization
   *          Organization to know its accounting schemas
   * @return a List of AD_Org_AcctSchema rows
   */
  public List<OrganizationAcctSchema> getOrgAcctSchemas(Organization organization) {
    try {
      OBContext.setAdminMode(true);
      Set<Organization> parentOrgs = new HashSet<Organization>();

      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(organization.getClient().getId());
      for (final String org : osp.getParentTree(organization.getId(), true)) {
        parentOrgs.add(this.getOrg(org));
      }

      final OBCriteria<OrganizationAcctSchema> obc = OBDal.getInstance().createCriteria(
          OrganizationAcctSchema.class);
      obc.add(Expression.in(OrganizationAcctSchema.PROPERTY_ORGANIZATION, parentOrgs));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a List of relations between the given organization and its accounting schemas (and also
   * the accounting schemas of its ancestors)
   * 
   * @param strOrgId
   *          Organization's ID to know its accounting schemas
   * @return a List of AD_Org_AcctSchema rows
   */
  public List<OrganizationAcctSchema> getOrgAcctSchemas(String strOrgId) {
    return this.getOrgAcctSchemas(this.getOrg(strOrgId));
  }

  /**
   * Returns the List of the Years for the given Calendar
   * 
   * @param cal
   *          Calendar to get its years
   * @return List of the Years for the given Calendar
   */
  public List<Year> getYearsOrderByDateDesc(Calendar cal) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<Year> obc = OBDal.getInstance().createCriteria(Year.class);
      obc.add(Expression.eq(Year.PROPERTY_CALENDAR, cal));
      obc.addOrderBy(Year.PROPERTY_FISCALYEAR, false);
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the List of the Calendar's Years of the given Organization
   * 
   * @param organization
   *          which owns the calendar
   * @return List of the Years for the given Organization's Calendar
   */
  public List<Year> getYearsOrderByDateDesc(Organization organization) {
    return this.getYearsOrderByDateDesc(this.getOrgCalendar(organization));
  }

  /**
   * Returns the List of the Calendar's Years of the given Organization
   * 
   * @param strOrgId
   *          Organization's ID which owns the calendar
   * @return List of the Years for the given Organization's Calendar
   */
  public List<Year> getYearsOrderByDateDesc(String strOrgId) {
    return this.getYearsOrderByDateDesc(this.getOrgCalendar(this.getOrg(strOrgId)));
  }

  /**
   * Returns a List which contains String[2], where
   * <ul>
   * <li>string[0] is the Identifier for the Period</li>
   * <li>string[1] is the Name to be displayed</li>
   * </ul>
   * 
   * The structure of both values depends on the Report's Periodicity:
   * <ul>
   * <li><b>Anually</b>
   * <ul>
   * <li>Identifier: comma-separated list of Period ID ordered by starting date (whole year).
   * Example: "10000,10001,100002,100003"</li>
   * <li>Name: "Anually"</li>
   * </ul>
   * </li>
   * 
   * <li><b>Monthly</b>
   * <ul>
   * <li>Identifier: period ID of the month. Example: "10000"</li>
   * <li>Name: defined period's name in the database. Example: "January"</li>
   * </ul>
   * </li>
   * 
   * <li><b>Quarterly</b>
   * <ul>
   * <li>Identifier: comma-separated list of Period ID ordered by starting date (whole quarter).
   * Example: "10000,10001,100002"</li>
   * <li>Name: first period's name - last period's name of the quarter. Example: "January - March"</li>
   * </ul>
   * </li>
   * </ul>
   * 
   * @param strReportId
   *          ID of the Tax Report. It provides us the Periodicity
   * @param strYearId
   *          ID of the year. Periods are calculated for this year
   * @param periodSeparation
   *          in case of Quarterly reports, the String to use as separation
   * @param lang
   *          language used for displaying periodicity name
   * @return
   */
  public List<String[]> getReportPeriods(String strReportId, String strYearId,
      String periodSeparation, String lang) {
    try {
      OBContext.setAdminMode(true);
      if (strReportId == null || strReportId.equals("")) {
        return null;
      } else {
        List<String[]> returnedList = new ArrayList<String[]>();

        TaxReport report = OBDal.getInstance().get(TaxReport.class, strReportId);
        String reportPeriod = report.getPeriod();

        final OBCriteria<Period> obc = OBDal.getInstance().createCriteria(Period.class);
        obc.add(Expression.eq(Period.PROPERTY_YEAR, this.getYear(strYearId)));
        obc.addOrderBy(Period.PROPERTY_STARTINGDATE, true);
        final List<Period> periodList = obc.list();

        if (reportPeriod.equals("M")) {
          // Monthly report
          for (final Period period : periodList) {
            String[] periodArray = { period.getId(), period.getName() };
            returnedList.add(periodArray);
          }

        } else if (reportPeriod.equals("A")) {
          // Anually report
          StringBuffer id = new StringBuffer();
          String name = Utility.messageBD(new DalConnectionProvider(), "OBTL_Anually", lang);
          // list
          for (Iterator<Period> iterator = periodList.iterator(); iterator.hasNext();) {
            Period period = iterator.next();
            id.append(period.getId());
            if (iterator.hasNext())
              id.append(",");
          }
          String[] periodArray = { id.toString(), name };
          returnedList.add(periodArray);
        } else if (reportPeriod.equals("Q")) {
          // Quarterly report
          StringBuffer id = new StringBuffer();
          StringBuffer name = new StringBuffer();
          int i = 0;
          for (final Period period : periodList) {
            i++;
            id.append(period.getId());
            switch (i) {
            case 1:
              id.append(",");
              name.append(period.getName());
              break;
            case 2:
              id.append(",");
              name.append(periodSeparation);
              break;
            case 3:
              name.append(period.getName());
              String[] periodArray = { id.toString(), name.toString() };
              returnedList.add(periodArray);
              id = new StringBuffer();
              name = new StringBuffer();
              i = 0;
              break;
            }
          }

        }

        return returnedList;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the Year object which corresponds to the given ID
   * 
   * @param strYearId
   *          Year's ID
   * @return an Year object, or null if no Year is found
   */
  public Year getYear(String strYearId) {
    try {
      OBContext.setAdminMode(true);
      return OBDal.getInstance().get(Year.class, strYearId);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the List of Legal Entity Organizations
   * 
   * @param excludeWithoutAccounting
   *          if true, excludes Legal Entities without Accounting
   * @return List of Legal Entity Organizations
   */
  public List<Organization> getLegalEntityOrgs(boolean excludeWithoutAccounting) {
    try {
      OBContext.setAdminMode(true);
      String whereClause;
      if (excludeWithoutAccounting)
        whereClause = "organizationType.legalEntityWithAccounting='Y' and ready='Y'";
      else
        whereClause = "(organizationType.legalEntity='Y' or organizationType.legalEntityWithAccounting='Y') and ready='Y'";

      final OBQuery<Organization> obQuery = OBDal.getInstance().createQuery(Organization.class,
          whereClause);
      return obQuery.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the Java class name where the business logic for the given report has been developed
   * 
   * @param strReportId
   *          tax report's ID
   * @return Java class name string with the business logic for the given report
   */
  public String getTaxReportClass(String strReportId) {
    return this.getTaxReport(strReportId).getJavaClassName();
  }

  /**
   * Returns the Tax Report object which corresponds to the given ID
   * 
   * @param strOrgId
   *          Tax Report's ID
   * @return a Tax Report object, or null if no Tax Report is found
   */
  public TaxReport getTaxReport(String strReportId) {
    try {
      OBContext.setAdminMode(true);
      return OBDal.getInstance().get(TaxReport.class, strReportId);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the TributaryKey object which corresponds to the given TributaryKey's ID
   * 
   * @param strTributaryKeyId
   *          TributaryKey's ID
   * @return TributaryKey object which corresponds to the given TributaryKey's ID
   */
  public TributaryKey getTributaryKeyById(String strTributaryKeyId) {
    try {
      OBContext.setAdminMode(true);
      return OBDal.getInstance().get(TributaryKey.class, strTributaryKeyId);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the TributaryKey object which corresponds to the given TributaryKey's Value (Unique)
   * 
   * @param strValue
   *          TributaryKey's unique value
   * @return TributaryKey object which corresponds to the given TributaryKey's Value
   */
  public TributaryKey getTributaryKeyByValue(String strValue) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<TributaryKey> obc = OBDal.getInstance().createCriteria(TributaryKey.class);
      obc.add(Expression.eq(TributaryKey.PROPERTY_SEARCHKEY, strValue));
      return (TributaryKey) obc.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the List of Tax Report Groups for the given Tax Report
   * 
   * @param taxReport
   *          the Tax Report to calculate its Tax Report Groups
   * @return the List of Tax Report Groups for the given Tax Report
   */
  public List<TaxReportGroup> getTaxReportGroups(TaxReport taxReport) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<TaxReportGroup> obc = OBDal.getInstance().createCriteria(
          TaxReportGroup.class);
      obc.add(Expression.eq(TaxReportGroup.PROPERTY_TAXREPORT, taxReport));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the List of Tax Rates that have associated the given list of Tax Report Parameters
   * 
   * @param taxReportParameterList
   *          a List of Tax Report Parameters
   * @return a List of Tax Rates
   */
  public Set<TaxRate> getTaxRates(Collection<TaxReportParameter> taxReportParameterList) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<TaxParameter> obc = OBDal.getInstance().createCriteria(TaxParameter.class);
      obc.add(Expression.in(TaxParameter.PROPERTY_TAXREPORTPARAMETER, taxReportParameterList));
      List<TaxParameter> taxParameters = obc.list();

      Set<TaxRate> taxRates = new HashSet<TaxRate>();
      for (TaxParameter tP : taxParameters) {
        taxRates.add(tP.getTax());
      }

      return taxRates;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a List of the Parameters for the given Report which correspond to the given type
   * 
   * @param strReport
   *          Report's ID
   * @param type
   *          Parameter's type, which can be:
   *          <ul>
   *          <li><b>I (or INPUT)</b> for input parameters</li>
   *          <li><b>O (or OUTPUT)</b> for output parameters</li>
   *          <li><b>C (or CONSTANT)</b> for constant parameters</li>
   *          <li><b>NULL</b> for Input, Output and Constant parameters</li>
   *          </ul>
   * @return a List of Tax Report Parameters
   */
  public List<TaxReportParameter> getTaxReportParameters(String strReport, String type) {
    try {
      OBContext.setAdminMode(true);
      final StringBuilder whereClause = new StringBuilder();
      final List<Object> parameters = new ArrayList<Object>();
      whereClause.append(" as p");
      whereClause.append(" left join fetch p.taxReportGroup trg");
      whereClause.append(" left join fetch trg.taxReport trgtr");
      if (type == null || type.equals("")) {
        whereClause.append(" where p.type IN ('I', 'C', 'O') ");
      } else if (type.equalsIgnoreCase("I") || type.equalsIgnoreCase("INPUT")) {
        whereClause.append(" where p.type = 'I' ");
      } else if (type.equalsIgnoreCase("C") || type.equalsIgnoreCase("CONSTANT")) {
        whereClause.append(" where p.type = 'C' ");
      } else if (type.equalsIgnoreCase("O") || type.equalsIgnoreCase("OUTPUT")) {
        whereClause.append(" where p.type = 'O' ");
      }
      whereClause.append(" and trgtr.id = ?");
      whereClause.append(" order by trg.sequenceNumber, p.sequenceNumber");
      parameters.add(strReport);
      final OBQuery<TaxReportParameter> obqParameters = OBDal.getInstance().createQuery(
          TaxReportParameter.class, whereClause.toString());
      obqParameters.setParameters(parameters);
      return obqParameters.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a List of Tax Report Parameters for the given Tax Report and which have associated the
   * given Tributary Key
   * 
   * @param taxReport
   *          Tax Report to get its Parameters
   * @param tributaryKey
   *          Tributary Key that must contain the returned Tax Report Parameter
   * 
   * @return a List of Tax Report Parameters
   */
  public List<TaxReportParameter> getTaxReportParameters(TaxReport taxReport,
      TributaryKey tributaryKey) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<TaxReportParameter> obc = OBDal.getInstance().createCriteria(
          TaxReportParameter.class);
      obc.add(Expression.eq(TaxReportParameter.PROPERTY_TRIBUTARYKEY, tributaryKey));
      obc.add(Expression.in(TaxReportParameter.PROPERTY_TAXREPORTGROUP,
          this.getTaxReportGroups(taxReport)));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the TaxReportParameter object which corresponds to the given ID
   * 
   * @param strTaxReportParameterId
   *          strTaxReportParameter's ID
   * @return an TaxReportParameter object, or null if no TaxReportParameter is found
   */
  public TaxReportParameter getTaxReportParameter(String strTaxReportParameterId) {
    try {
      OBContext.setAdminMode(true);
      return OBDal.getInstance().get(TaxReportParameter.class, strTaxReportParameterId);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the TaxReportParameter object which correspond to the given report and searchkey
   * 
   * @param strReportId
   *          Tax Report's ID
   * @param value
   *          String with the searchkey of the parameter
   * @return a TaxReportParameter object
   */
  public TaxReportParameter getTaxReportParameter(String strReportId, String value) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<TaxReportParameter> obc = OBDal.getInstance().createCriteria(
          TaxReportParameter.class);
      obc.add(Expression.in(TaxReportParameter.PROPERTY_TAXREPORTGROUP,
          this.getTaxReportGroups(this.getTaxReport(strReportId))));
      obc.add(Expression.eq(TaxReportParameter.PROPERTY_SEARCHKEY, value));
      return (TaxReportParameter) obc.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the TaxReportParameter object which correspond to the given report and searchkey
   * 
   * @param taxReport
   *          Tax Report
   * @param value
   *          String with the searchkey of the parameter
   * @return a TaxReportParameter object
   */
  public TaxReportParameter getTaxReportParameter(TaxReport taxReport, String value) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<TaxReportParameter> obc = OBDal.getInstance().createCriteria(
          TaxReportParameter.class);
      obc.add(Expression.in(TaxReportParameter.PROPERTY_TAXREPORTGROUP,
          this.getTaxReportGroups(this.getTaxReport(taxReport.getId()))));
      obc.add(Expression.eq(TaxReportParameter.PROPERTY_SEARCHKEY, value));
      return (TaxReportParameter) obc.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a List of all the lines in the Fact Acct table which correspond to the given Acct
   * Schema and the List of Tax Rates
   * 
   * @param strOrgId
   *          Organization's ID
   * @param strAcctSchemaId
   *          Acct Schema's ID
   * @param taxRates
   *          Collection of Tax Rates
   * @param periods
   *          Collection of fiscal Periods
   * @return a List of Fact Acct lines
   */
  public List<AccountingFact> getAccountingFacts(String strOrgId, String strAcctSchemaId,
      Collection<TaxRate> taxRates, Collection<Period> periods) {
    try {
      OBContext.setAdminMode(true);
      final OBCriteria<AccountingFact> obc = OBDal.getInstance().createCriteria(
          AccountingFact.class);
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(OBContext.getOBContext().getCurrentClient().getId());
      obc.add(Expression.in(AccountingFact.PROPERTY_ORGANIZATION + ".id",
          osp.getChildTree(strOrgId, true)));
      obc.add(Expression.in(AccountingFact.PROPERTY_TAX, taxRates));
      obc.add(Expression.eq(AccountingFact.PROPERTY_ACCOUNTINGSCHEMA,
          this.getAcctSchema(strAcctSchemaId)));
      obc.add(Expression.in(AccountingFact.PROPERTY_PERIOD, periods));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a List of Period objects for the list of comma-separated Period's ID string
   * 
   * @param strPeriodIds
   *          comma-separated Period's ID. Example: "1000,1001,1002"
   * @return a List of Period objects
   */
  public List<Period> getPeriods(String strPeriodIds) {
    List<Period> periods = new ArrayList<Period>();
    StringTokenizer st = new StringTokenizer(StringUtils.deleteWhitespace(strPeriodIds), ",");
    while (st.hasMoreTokens()) {
      periods.add(this.getPeriod(st.nextToken()));
    }
    return periods;
  }

  /**
   * Returns the Period object which corresponds to the given ID
   * 
   * @param strPeriodId
   *          Period's ID
   * @return an Period object, or null if no Period is found
   */
  public Period getPeriod(String strPeriodId) {
    try {
      OBContext.setAdminMode(true);
      return OBDal.getInstance().get(Period.class, strPeriodId);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the Acct Schema object which corresponds to the given ID
   * 
   * @param strAcctSchemaId
   *          Acct Schema's ID
   * @return Acct Schema object which corresponds to the given ID
   */
  public AcctSchema getAcctSchema(String strAcctSchemaId) {
    try {
      OBContext.setAdminMode(true);
      return OBDal.getInstance().get(AcctSchema.class, strAcctSchemaId);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
