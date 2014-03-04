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

package org.openbravo.module.taxreportlauncher.Exception;

import org.openbravo.base.exception.OBException;

/**
 * This class is the base exception for all the exceptions related to the Tax Report Launcher. It
 * extends OBException, so it is an unchecked exception which also logs itself. The main difference
 * with OBException is the addition of a new attribute (title), useful for displaying error message
 * while creating the tax reports.
 * 
 * @author openbravo
 * 
 */
public class OBTL_Exception extends OBException {
  private static final long serialVersionUID = 1L;
  private String title;

  public OBTL_Exception() {
    super();
  }

  public OBTL_Exception(String title, String message) {
    super(message);
    this.setTitle(title);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

}
