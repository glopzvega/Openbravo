package com.desiteg.ob.howtos.diot;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.tax.TaxRate;

public class DiotFiscalReportDao {

	/**
	 * Returns the list of Invoice Tax for the given Organization (and its
	 * children), list of tax rates, periods and accounting schema
	 * 
	 */

	public List<FIN_Payment> getPaymentsNoInvoice(Organization org,
			List<Period> periods, String facturas, int opcion) {

		try {
			OBContext.setAdminMode();
			final StringBuffer whereClause = new StringBuffer();

			whereClause.append(" as fp  ");
			whereClause.append(" inner join fetch fp.fINPaymentDetailVList as tx");
			whereClause.append(" inner join fetch tx.businessPartner as bp");					
				
			whereClause.append(" where fp.receipt = 'N' ");
			whereClause.append(" and fp.paymentDate >= :fromDate ");
			whereClause.append(" and fp.paymentDate < :toDate ");
			whereClause.append(" and fp.organization.id in (:orgs) ");
			
			if(facturas != null && facturas != ""){
				
				if(opcion == 1)
					whereClause.append(" and tx.invoiceno in ( " + facturas + " )");
				else
					whereClause.append(" and tx.invoiceno not in ( " + facturas + " )");
				
			}
											
			
			whereClause.append(" order by bp.id ");

			final OBQuery<FIN_Payment> obQuery = OBDal.getInstance()
					.createQuery(FIN_Payment.class, whereClause.toString());

			obQuery.setNamedParameter("fromDate", periods.get(0)
					.getStartingDate());
			obQuery.setNamedParameter("toDate", DateUtils.addDays(
					periods.get(periods.size() - 1).getEndingDate(), 1));

			OrganizationStructureProvider osp = OBContext.getOBContext()
					.getOrganizationStructureProvider(org.getClient().getId());
			obQuery.setNamedParameter("orgs",
					osp.getChildTree(org.getId(), true));

			return obQuery.list();
		}

		finally {
			OBContext.restorePreviousMode();
		}

	}

	public List<FIN_Payment> getPaymentsInvoice(Organization org,
			List<Period> periods, String facturas, int opcion) {

		try {
			OBContext.setAdminMode();
			final StringBuffer whereClause = new StringBuffer();

			whereClause.append(" as fp  ");
			whereClause.append(" inner join fetch fp.fINPaymentDetailVList as tx");
			whereClause.append(" inner join fetch tx.businessPartner as bp");						
			whereClause.append(" where fp.receipt = 'N' ");
			whereClause.append(" and fp.paymentDate >= :fromDate ");
			whereClause.append(" and fp.paymentDate < :toDate ");
			whereClause.append(" and fp.organization.id in (:orgs) ");
			
			if(facturas != null && facturas != ""){
				
				if(opcion == 1)
					whereClause.append(" and tx.invoiceno in ( " + facturas + " )");
				else
					whereClause.append(" and tx.invoiceno not in ( " + facturas + " )");
				
			}
			
			whereClause.append(" order by bp.id ");

			final OBQuery<FIN_Payment> obQuery = OBDal.getInstance()
					.createQuery(FIN_Payment.class, whereClause.toString());

			obQuery.setNamedParameter("fromDate", periods.get(0)
					.getStartingDate());
			obQuery.setNamedParameter("toDate", DateUtils.addDays(
					periods.get(periods.size() - 1).getEndingDate(), 1));

			OrganizationStructureProvider osp = OBContext.getOBContext()
					.getOrganizationStructureProvider(org.getClient().getId());
			obQuery.setNamedParameter("orgs",
					osp.getChildTree(org.getId(), true));

			return obQuery.list();
		}

		finally {
			OBContext.restorePreviousMode();
		}

	}

	public List<InvoiceTax> getInvoiceTax(Organization org,
			Collection<TaxRate> taxRates, List<Period> periods,
			AcctSchema acctSchema) {
		try {
			OBContext.setAdminMode();
			final StringBuffer whereClause = new StringBuffer();

			whereClause.append(" as it  ");
			whereClause.append(" inner join fetch it." + InvoiceTax.PROPERTY_INVOICE + " as i ");
			whereClause.append(" inner join fetch i.businessPartner");
			whereClause.append(" where exists (select 1 from FinancialMgmtAccountingFact as fa ");
			whereClause.append(" where i.id = fa."+ AccountingFact.PROPERTY_RECORDID);
			whereClause.append(" and fa."+ AccountingFact.PROPERTY_ACCOUNTINGSCHEMA	+ "= :acctSchema");
			whereClause.append(" and fa." + AccountingFact.PROPERTY_TABLE + ".id = :invoiceTableId )");
			whereClause.append(" and i." + Invoice.PROPERTY_SALESTRANSACTION + "='N' ");
			whereClause.append(" and i." + Invoice.PROPERTY_POSTED + "='Y' ");
			whereClause.append(" and i." + Invoice.PROPERTY_ACCOUNTINGDATE + ">= :fromDate ");
			whereClause.append(" and i." + Invoice.PROPERTY_ACCOUNTINGDATE + "< :toDate ");
			whereClause.append(" and i." + Invoice.PROPERTY_ORGANIZATION + ".id in (:orgs) ");
			whereClause.append(" and it." + InvoiceTax.PROPERTY_TAX + " in (:taxes) ");
			whereClause.append(" order by i.businessPartner.taxID");

			final OBQuery<InvoiceTax> obQuery = OBDal.getInstance()
					.createQuery(InvoiceTax.class, whereClause.toString());

			obQuery.setNamedParameter("acctSchema", acctSchema);
			obQuery.setNamedParameter("invoiceTableId", "318");

			obQuery.setNamedParameter("fromDate", periods.get(0)
					.getStartingDate());

			obQuery.setNamedParameter("toDate", DateUtils.addDays(
					periods.get(periods.size() - 1).getEndingDate(), 1));
			OrganizationStructureProvider osp = OBContext.getOBContext()
					.getOrganizationStructureProvider(org.getClient().getId());
			obQuery.setNamedParameter("orgs",
					osp.getChildTree(org.getId(), true));
			obQuery.setNamedParameter("taxes", taxRates);
			obQuery.setFilterOnActive(false);

			return obQuery.list();
		} finally {
			OBContext.restorePreviousMode();
		}
	}

}
