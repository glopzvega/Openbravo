package com.desiteg.ob.howtos.diot;

import java.math.BigDecimal;

public class FacturasTMP {

	private String factura;
	private String proveedor;
	private BigDecimal compra;
	private String clave;

	public FacturasTMP() {
	}

	public FacturasTMP(String proveedor, BigDecimal compra, String clave) {
		this.proveedor = proveedor;
		this.compra = compra;
		this.clave = clave;
	}
	
	public String getFactura() {
		return factura;
	}

	public void setFactura(String factura) {
		this.factura = factura;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public BigDecimal getCompra() {
		return compra;
	}

	public void setCompra(BigDecimal compra) {
		this.compra = compra;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	@Override
	public String toString() {
		return "FacturasTMP [proveedor=" + proveedor + ", compra=" + compra
				+ ", clave=" + clave + "]";
	}

}
