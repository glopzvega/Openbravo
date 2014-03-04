package com.desiteg.ob.howtos.diot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDao;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.module.taxreportlauncher.TaxReportGroup;
import org.openbravo.module.taxreportlauncher.TaxReportParameter;
import org.openbravo.module.taxreportlauncher.Dao.TaxReportLauncherDao;
import org.openbravo.module.taxreportlauncher.erpCommon.ad_reports.OBTL_TaxReport_I;

public class DiotFiscalReport implements OBTL_TaxReport_I {

	private List<Period> periods;
	private AcctSchema acctSchema;
	private Organization org;

	private final TaxReportLauncherDao dao = new TaxReportLauncherDao();
	private final DiotFiscalReportDao reportDao = new DiotFiscalReportDao();

	private void initializeAttributes(final String strYearId,
			final String strAcctSchemaId, final String strReportId,
			final String strOrgId, final String strPeriodIds) {

		this.acctSchema = dao.getAcctSchema(strAcctSchemaId);
		this.org = dao.getOrg(strOrgId);
		this.periods = dao.getPeriods(strPeriodIds);
	}

	@Override
	public HashMap<String, Object> generateElectronicFile(String strOrgId,
			String strReportId, String strAcctSchemaId, String strYearId,
			String strPeriodId, Map<String, String> inputParams)
			throws OBException, Exception {

		initializeAttributes(strYearId, strAcctSchemaId, strReportId, strOrgId,
				strPeriodId);

		StringBuffer sb = new StringBuffer();
		List<FacturasTMP> facturasList1 = null, facturasList2 = null, facturasList3 = null, facturasTmpList = null, 
				listaAgrupada1 = null, listaAgrupada2 = null, listaAgrupada3 = null;
		
		boolean IVA_NORMAL = false, IVA_PENDIENTE = false, IVA_OTROS = false;
		int b = 0;

		if (inputParams.get("Facturas") != null && inputParams.get("Facturas").equals("Y"))
			b = 1;

		if (inputParams.get("IVA_NORMAL").equals("Y")) {
			
			facturasList1 = obtenerLineasFacturasDiot("IVA_NORMAL", b, 1);
			
			if (facturasList1 != null)
				IVA_NORMAL = true;			
		}

		if (inputParams.get("IVA_PENDIENTE").equals("Y")) {
			
			facturasList2 = obtenerLineasFacturasDiot("IVA_PENDIENTE", b, 2);
			
			if (facturasList2 != null)
				IVA_PENDIENTE = true;	
			else
				IVA_OTROS = true;
		}
		
		if (IVA_NORMAL && IVA_PENDIENTE){			
							
			listaAgrupada1 = agruparFacturasByNumero(facturasList1);
			listaAgrupada2 = agruparFacturasByNumero(facturasList2);
			
			facturasTmpList = juntarListasByNumero(listaAgrupada1, listaAgrupada2);
			facturasList3 = obtenerPagosNotInPeriod(facturasTmpList);
			
			if(b == 1) sb.append(mostrarFacturas(facturasList1, facturasList2, facturasList3));
			else
			{			
				if(facturasList3 != null)
				{							
					listaAgrupada1 = agruparFacturasByProveedor(facturasList1);
					listaAgrupada2 = agruparFacturasByProveedor(facturasList2);
					listaAgrupada3 = agruparFacturasByProveedor(facturasList3);
					
					facturasTmpList = juntarListasByProveedor(listaAgrupada2, listaAgrupada3);
					facturasTmpList = juntarListasByProveedor(listaAgrupada1, facturasTmpList);
					sb.append(imprimirListaFacturas(facturasTmpList));				
				}
				else
				{				
					listaAgrupada1 = agruparFacturasByProveedor(facturasList1);
					listaAgrupada2 = agruparFacturasByProveedor(facturasList2);
					facturasTmpList = juntarListasByProveedor(listaAgrupada1, listaAgrupada2);
					sb.append(imprimirListaFacturas(facturasTmpList));				
				}
			}			
		}
		
		else if (IVA_NORMAL)
		{
			if(b == 1) sb.append(mostrarFacturas(facturasList1, facturasList2, facturasList3));
			else
			{
				listaAgrupada1 = agruparFacturasByProveedor(facturasList1);
				sb.append(imprimirListaFacturas(listaAgrupada1));
			}
		}				

		else if (IVA_PENDIENTE){
		
			facturasList1 = obtenerLineasFacturasDiot("IVA_NORMAL", b, 1);
			
			if(facturasList1 != null)
			{
				listaAgrupada1 = agruparFacturasByNumero(facturasList1);
				listaAgrupada2 = agruparFacturasByNumero(facturasList2);
				facturasTmpList = juntarListasByNumero(listaAgrupada1, listaAgrupada2);				
			}
			else
			{
				facturasTmpList = agruparFacturasByNumero(facturasList2);
			}
			
			facturasList3 = obtenerPagosNotInPeriod(facturasTmpList);
			
			if(b == 1) sb.append(mostrarFacturas(null, facturasList2, facturasList3));
			else
			{						
				if(facturasList3 != null)
				{
					listaAgrupada2 = agruparFacturasByProveedor(facturasList2);
					listaAgrupada3 = agruparFacturasByProveedor(facturasList3);
					facturasTmpList = juntarListasByProveedor(listaAgrupada2, listaAgrupada3);
					sb.append(imprimirListaFacturas(facturasTmpList));
				}
				else
				{
					listaAgrupada2 = agruparFacturasByProveedor(facturasList2);
					sb.append(imprimirListaFacturas(listaAgrupada2));
				}				
			}
		}			
		
		else if(IVA_OTROS) {
			
			facturasList1 = obtenerLineasFacturasDiot("IVA_NORMAL", b, 1);
			
			if(facturasList1 != null)
			{
				facturasTmpList = agruparFacturasByNumero(facturasList1);				
			}
			
			facturasList3 = obtenerPagosNotInPeriod(facturasTmpList);
			
			if (b == 1)	sb.append(mostrarFacturas(null, null, facturasList3));
			else
			{
				if(facturasList3 != null && facturasList3.size() > 0)
				{
					sb.append(imprimirListaFacturas(facturasList3));
				}
				else
				{
					sb.append("No se encontraron Registros de Facturas asociados al periodo " + periods.get(0).getName());
				}
			}								
		}
		
		else
			sb.append("No se encontraron Registros de Facturas asociados al periodo " + periods.get(0).getName());			
		
		
		final HashMap<String, Object> returnedMap = new HashMap<String, Object>();

		returnedMap.put("fileName", "DiotFiscalReport");
		returnedMap.put("file", sb);

		return returnedMap;
	}

	private List<FacturasTMP> obtenerPagosNotInPeriod(List<FacturasTMP> facturasTmpList) {
		
		String facturas = generarCadenaFacturas(facturasTmpList);
		List<FIN_Payment> facturasNoTaxList = reportDao.getPaymentsInvoice(org, periods, facturas, 2);
		List<FacturasTMP> facturasNoTaxListTmp = null;
		
		if(facturasNoTaxList != null){			
			facturasNoTaxListTmp = procesarFacturasTaxPagos(facturasNoTaxList);	
		}							

		return facturasNoTaxListTmp;
	}	

	private String generarCadenaFacturas(List<FacturasTMP> listaTmp) {

		int contador = 1;
		String facturas = "";

		if(listaTmp != null){			
		
			for (FacturasTMP lista : listaTmp) {
	
				facturas = facturas + "'" + lista.getFactura() + "'";
	
				if (contador < listaTmp.size())
					facturas = facturas + ", ";
	
				contador++;
			}
		}
		return facturas;
	}

	private List<FacturasTMP> obtenerLineasFacturasDiot(String groupSearchKey, int facturas, int opcionIVA) {

		List<FacturasTMP> facturasTmpList = null;
		Set<TaxRate> taxRates = getTaxRates(groupSearchKey);
		List<InvoiceTax> invoiceTaxList = reportDao.getInvoiceTax(org, taxRates, periods, acctSchema);

		if (invoiceTaxList.size() > 0) {

			switch (opcionIVA) {

				case 1:
					facturasTmpList = procesarFacturasTax(invoiceTaxList);
					break;

				case 2:					
					facturasTmpList = procesarFacturasTax(invoiceTaxList);
					facturasTmpList = agruparFacturasByNumero(facturasTmpList);					
					String parametros = generarCadenaFacturas(facturasTmpList);
					
					List<FIN_Payment> paymentInvoiceTaxList = reportDao.getPaymentsInvoice(org, periods, parametros, 1);

					if (paymentInvoiceTaxList.size() > 0)
						facturasTmpList = procesarFacturasTaxPagos(paymentInvoiceTaxList);
					else
						facturasTmpList = null;
					break;
			}
		}

		return facturasTmpList;
	}	
	
	private Set<TaxRate> getTaxRates(String groupSearchKey) {

		Set<TaxRate> taxRates = null;

		TaxReportGroup group = (TaxReportGroup) OBDao.getFilteredCriteria(
				TaxReportGroup.class,
				Restrictions.eq(TaxReportGroup.PROPERTY_SEARCHKEY,
						groupSearchKey)).uniqueResult();

		for (TaxReportParameter taxReportParam : group
				.getOBTLTaxReportParameterList()) {
			if (taxReportParam.getType().equals("O")) {
				List<TaxReportParameter> paramList = new ArrayList<TaxReportParameter>();
				paramList.add(taxReportParam);
				taxRates = dao.getTaxRates(paramList);
				break;
			}
		}
		return taxRates;
	}

	private List<FacturasTMP> procesarFacturasTax(List<InvoiceTax> invoiceTaxList) {

		List<FacturasTMP> facturasTmpList = new ArrayList<FacturasTMP>();

		for (InvoiceTax invoiceTax : invoiceTaxList) {

			BusinessPartner bPartner = invoiceTax.getInvoice()
					.getBusinessPartner();

			String factura = invoiceTax.getInvoice().getDocumentNo();
			String nombre = bPartner.getTaxID();
			String claveTmp = bPartner.getReferenceNo();
			BigDecimal suma = invoiceTax.getTaxableAmount();

			if (nombre == null || nombre == "")
				nombre = bPartner.getName();

			if (claveTmp == null || claveTmp.equals(""))
				claveTmp = "04|03";

			FacturasTMP facturasTmp = new FacturasTMP();

			facturasTmp.setFactura(factura);
			facturasTmp.setProveedor(nombre);
			facturasTmp.setCompra(suma);
			facturasTmp.setClave(claveTmp);

			facturasTmpList.add(facturasTmp);
		}	

		return facturasTmpList;
	}

	private List<FacturasTMP> agruparFacturasByNumero(List<FacturasTMP> facturasList) {

		List<FacturasTMP> facturasListTmp = null;
		if (facturasList != null) {

			facturasListTmp = new ArrayList<FacturasTMP>();
			
			int contadorTmp = 0;

			for (FacturasTMP factura : facturasList) {

				if (facturasListTmp.size() > 0) {

					String fac = factura.getFactura();
					String facTmp = facturasListTmp.get(contadorTmp)
							.getFactura();

					if (!sonIguales(fac, facTmp)) {
						facturasListTmp.add(factura);
						contadorTmp++;
					}

				} else
					facturasListTmp.add(factura);
			}
		}
		return facturasListTmp;
	}

	private List<FacturasTMP> agruparFacturasByProveedor(List<FacturasTMP> facturasList) {

		List<FacturasTMP> facturasListTmp = new ArrayList<FacturasTMP>();
		int contadorTmp = 0;

		for (FacturasTMP factura : facturasList) {

			if (facturasListTmp.size() > 0) {

				String nombre = factura.getProveedor();
				String nombreTmp = facturasListTmp.get(contadorTmp)
						.getProveedor();

				if (sonIguales(nombre, nombreTmp)) {
					BigDecimal suma = factura.getCompra();
					BigDecimal sumaTmp = facturasListTmp.get(contadorTmp)
							.getCompra();
					suma = suma.add(sumaTmp);
					facturasListTmp.get(contadorTmp).setCompra(suma);
				} else {
					facturasListTmp.add(factura);
					contadorTmp++;
				}

			} else
				facturasListTmp.add(factura);
		}
		return facturasListTmp;
	}

	private boolean sonIguales(String nombre, String nombreTmp) {
		if (nombre.equals(nombreTmp))
			return true;
		else
			return false;
	}

	private List<FacturasTMP> procesarFacturasTaxPagos(List<FIN_Payment> paymentInvoiceTaxList) {

		List<FacturasTMP> facturasTmpList = new ArrayList<FacturasTMP>();

		for (FIN_Payment paymentInvoiceTax : paymentInvoiceTaxList) {

			BusinessPartner bPartner = paymentInvoiceTax.getBusinessPartner();

			String factura = paymentInvoiceTax.getFINPaymentDetailVList()
					.get(0).getInvoiceno();
			String nombre = bPartner.getTaxID();
			String claveTmp = bPartner.getReferenceNo();

			Double compraIVA = new Double(paymentInvoiceTax.getAmount()
					.doubleValue() / 1.16);
			BigDecimal suma = new BigDecimal(compraIVA);

			if (nombre == null || nombre == "")
				nombre = bPartner.getName();

			if (claveTmp == null || claveTmp.equals(""))
				claveTmp = "04|03";

			FacturasTMP facturasTmp = new FacturasTMP();

			facturasTmp.setFactura(factura);
			facturasTmp.setProveedor(nombre);
			facturasTmp.setCompra(suma);
			facturasTmp.setClave(claveTmp);

			facturasTmpList.add(facturasTmp);
		}		

		return facturasTmpList;
	}

	private List<FacturasTMP> juntarListasByNumero(List<FacturasTMP> lista1, List<FacturasTMP> lista2) {		
			
		int contador = 0;
		List<Integer> indexes = new ArrayList<Integer>();
		boolean iguales = false;

		for (FacturasTMP facturasTmp2 : lista2) {
			String factura = facturasTmp2.getFactura();
			iguales = false;

			for (FacturasTMP facturasTmp1 : lista1) {
				if (factura.equals(facturasTmp1.getFactura())) {

					BigDecimal compra2 = facturasTmp2.getCompra();
					BigDecimal compra1 = facturasTmp1.getCompra().add(compra2);
					facturasTmp1.setCompra(compra1);
					iguales = true;
					break;
				}
			}
			if (!iguales) {
				indexes.add(contador);
			}
			contador++;
		}

		for (Integer index : indexes) {
			lista1.add(lista2.get(index));
		}

		return lista1;
		
	}

	private List<FacturasTMP> juntarListasByProveedor(List<FacturasTMP> lista1, List<FacturasTMP> lista2) {		
		
		int contador = 0;
		List<Integer> indexes = new ArrayList<Integer>();
		boolean iguales = false;

		for (FacturasTMP facturasTmp2 : lista2) {
			String nombre = facturasTmp2.getProveedor();
			iguales = false;

			for (FacturasTMP facturasTmp1 : lista1) {
				if (nombre.equals(facturasTmp1.getProveedor())) {

					BigDecimal compra2 = facturasTmp2.getCompra();
					BigDecimal compra1 = facturasTmp1.getCompra().add(compra2);
					facturasTmp1.setCompra(compra1);
					iguales = true;
					break;
				}
			}
			if (!iguales) {
				indexes.add(contador);
			}
			contador++;
		}

		for (Integer index : indexes) {
			lista1.add(lista2.get(index));
		}

		return lista1;
		
	}
	
	private StringBuffer mostrarFacturas(List<FacturasTMP> lista1,
			List<FacturasTMP> lista2, List<FacturasTMP> lista3) {

		StringBuffer lines = new StringBuffer();
		
		if (lista1 != null && lista1.size() > 0){
			lines.append("IVA Normal \n");
			lines.append(imprimirListaFacturas(lista1));
			if(lista2 != null || lista3 != null) lines.append("\n");
		}
		if(lista2 != null && lista2.size() > 0){
			lines.append("IVA Pendiente \n");
			lines.append(imprimirListaFacturas(lista2));
			if(lista3 != null) lines.append("\n");
		}
		if(lista3 != null && lista3.size() > 0){			
			lines.append("IVA Pendiente Otros \n");
			lines.append(imprimirListaFacturas(lista3));			
		}		

		return lines;
	}
	
	private StringBuffer imprimirListaFacturas(List<FacturasTMP> lista) {
		StringBuffer lines = new StringBuffer();

		int contador = 1;
		boolean salto = true;
		for (FacturasTMP facturasTmp : lista) {

			if (contador == lista.size())
				salto = false;
			lines.append(crearLinea(facturasTmp, salto));
			contador++;
		}

		return lines;
	}

	private StringBuffer crearLinea(FacturasTMP factura, boolean salto) {

		StringBuffer line = new StringBuffer();

		BigDecimal compra = factura.getCompra().setScale(0,
				RoundingMode.HALF_EVEN);

		line.append(factura.getClave() + "|" + factura.getProveedor() + "|||||"
				+ compra + "|0|0|0|0|0||||||0|0||0|");

		if (salto)
			line.append("\n");

		return line;
	}
}
