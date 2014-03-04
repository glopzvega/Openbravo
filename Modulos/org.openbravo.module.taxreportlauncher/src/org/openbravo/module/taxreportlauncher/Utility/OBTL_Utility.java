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

package org.openbravo.module.taxreportlauncher.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.module.taxreportlauncher.TaxReport;
import org.openbravo.module.taxreportlauncher.TaxReportGroup;
import org.openbravo.module.taxreportlauncher.TaxReportParameter;
import org.openbravo.module.taxreportlauncher.Dao.TaxReportLauncherDao;
import org.openbravo.module.taxreportlauncher.Exception.OBTL_Exception;

/**
 * Utility class for the Tax Report Launcher
 * 
 * @author openbravo
 * 
 */
public class OBTL_Utility {

  /**
   * Formats the input string
   * 
   * @param str
   *          String to be formated. Null means repeat fillChar length-1 times
   * @param length
   *          final length of the returned string
   * @param fillChar
   *          character used to fill the returned string. (Null is equivalent to ' ')
   * @param toRight
   *          if true, it aligns to the right ("  81"); else to the left ("81  ")
   * @param paramName
   *          parameter's name. Useful only for giving context in case of error
   * @return a formated string
   * @throws OBException
   */
  private static String internalFormat(String str, int length, Character fillChar, boolean toRight,
      String paramName) throws OBException {
    if (length <= 0) {
      // Impossible to format the string to a length less or equal to 0
      throw new OBTL_Exception("@OBTL_InvalidLengthTitle@" + paramName, "@OBTL_Length0@");
    } else {
      if (fillChar == null || fillChar.equals(""))
        fillChar = ' ';
      if (str == null) {
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++)
          sb.append(fillChar);
        return sb.toString();
      } else {
        if (str.length() <= length) {
          StringBuffer sb = new StringBuffer(length);
          if (toRight) {
            for (int i = 0; i < length - str.length(); i++) {
              sb.append(fillChar);
            }
            sb.append(str);
          } else {
            sb.append(str);
            for (int i = 0; i < length - str.length(); i++) {
              sb.append(fillChar);
            }
          }
          return sb.toString();

        } else {
          // Impossible to format string. str length is greater than the expected final length
          throw new OBTL_Exception("@OBTL_InvalidLengthTitle@" + paramName, str
              + "@OBTL_GreaterFinalLengthMessage@" + length);

        }
      }
    }
  }

  /**
   * Formats and validates the input string
   * 
   * @param str
   *          String to be formated. Null means repeat fillChar length-1 times
   * @param length
   *          final length of the returned string
   * @param fillChar
   *          character used to fill the returned string. (Null is equivalent to ' ')
   * @param toRight
   *          if true, it aligns to the right ("  81"); else to the left ("81  ")
   * @param paramName
   *          parameter's name. Useful only for giving context in case of error
   * @return a formated string
   * @throws OBException
   */
  public static String format(String str, int length, Character fillChar, boolean toRight,
      String paramName) throws OBException {
    String formatStr = OBTL_Utility.internalFormat(str, length, fillChar, toRight, paramName);
    boolean valid = OBTL_Utility.validateLength(formatStr, length);
    if (valid)
      return formatStr;
    else
      // The length of the formatted string (" + formatStr + ") is not equal to the expected final
      // length " + length)
      throw new OBTL_Exception("@OBTL_InvalidLengthTitle@ " + paramName, length
          + "@OBTL_NotEqualLengthMessage@ " + formatStr);
  }

  /**
   * Formats and validates the input string
   * 
   * @param str
   *          int to be formated. (It's transformed into String automatically)
   * @param length
   *          final length of the returned string
   * @param fillChar
   *          character used to fill the returned string. (Null is equivalent to ' ')
   * @param toRight
   *          if true, it aligns to the right ("  81"); else to the left ("81  ")
   * @param paramName
   *          parameter's name. Useful only for giving context in case of error
   * @return a formated string
   * @throws OBException
   */
  public static String format(int str, int length, char fillChar, boolean toRight, String paramName)
      throws OBException {
    return OBTL_Utility.format(new Integer(str).toString(), length, fillChar, toRight, paramName);
  }

  /**
   * Validates whether the given length is equal to the given String Length
   * 
   * @param str
   *          input String to validate
   * @param length
   *          length to compare to
   * @return true in case of same length, else false
   */
  public static boolean validateLength(String str, int length) {
    if (str.length() == length)
      return true;
    else
      return false;
  }

  /**
   * Returns a Map with the values of all the constant parameters for the given Tax Report, where:
   * <ul>
   * <li>The key is the Search Key defined in the report</li>
   * <li>The value is the constant value for this parameter</li>
   * </ul>
   * 
   * @param taxReport
   *          the Tax Report to get its constant parameters
   * @return a Map with all the constant parameters for the given Tax Report
   */
  public static Map<String, String> getValueConstantParameters(TaxReport taxReport) {
    Map<String, String> constantMap = new HashMap<String, String>();

    for (TaxReportGroup taxReportGroup : taxReport.getOBTLTaxReportGroupList())
      for (TaxReportParameter taxReportParameter : taxReportGroup.getOBTLTaxReportParameterList())
        constantMap.put(taxReportParameter.getSearchKey(), taxReportParameter.getConstantValue());

    return constantMap;
  }

  /**
   * Creates a compressed ZIP file
   * 
   * @param outputFilePath
   *          global path to the directory where the ZIP file will be created
   * @param fileName
   *          name for the ZIP file
   * @param inputFiles
   *          Set of files to be compressed into the ZIP file
   * @throws IOException
   */
  public static void createZIPfile(final String outputFilePath, final String fileName,
      final Set<File> inputFiles) throws IOException {
    final FileOutputStream file = new FileOutputStream(new File(outputFilePath, fileName));

    final ZipOutputStream zip = new ZipOutputStream(file);
    for (final File inputFile : inputFiles) {
      if (inputFile.isFile()) {
        final ZipEntry inZIP = new ZipEntry(inputFile.getName());
        zip.putNextEntry(inZIP);

        final FileInputStream in = new FileInputStream(inputFile);
        int len;
        final byte[] buf = new byte[1024];
        while ((len = in.read(buf)) > 0) {
          zip.write(buf, 0, len);
        }
        zip.closeEntry();
        in.close();
      }
    }
    zip.close();
  }

  /**
   * Returns a valid file name removing all the strange characters that shouldn't be used inside the
   * report's file name
   * 
   * @param name
   * @param reportId
   * @return
   */
  public static String getReportValidFileName(final String name, final String reportId) {
    String fileName = name;

    if (fileName == null || fileName.equals("")) {
      fileName = StringUtils.deleteWhitespace(new TaxReportLauncherDao().getTaxReport(reportId)
          .getName());
    } else {
      fileName = StringUtils.deleteWhitespace(fileName);
    }

    return removeStrangeCharacters(fileName);
  }

  /**
   * Returns a string removing all the strange characters
   * 
   * @param originalStr
   * @return
   */
  public static String removeStrangeCharacters(final String originalStr) {
    String fileName = originalStr;
    fileName = fileName.replaceAll("[áàäâ]", "a");
    fileName = fileName.replaceAll("[ÁÀÄÂ]", "A");
    fileName = fileName.replaceAll("[éèëê]", "e");
    fileName = fileName.replaceAll("[ÉÈËÊ]", "E");
    fileName = fileName.replaceAll("[íìïî]", "i");
    fileName = fileName.replaceAll("[ÍÌÏÎ]", "I");
    fileName = fileName.replaceAll("[óòöô]", "o");
    fileName = fileName.replaceAll("[ÓÒÖÔ]", "O");
    fileName = fileName.replaceAll("[úùüû]", "u");
    fileName = fileName.replaceAll("[ÚÙÜÛ]", "U");
    fileName = fileName.replaceAll("[ç]", "c");
    fileName = fileName.replaceAll("[Ç]", "C");
    fileName = fileName.replaceAll("[ñ]", "n");
    fileName = fileName.replaceAll("[Ñ]", "N");
    fileName = fileName.replaceAll("[^a-zA-Z0-9.]", "");
    return fileName;
  }

  /**
   * Returns the constant parameter (if any) or the input parameter value in case no constant
   * parameter is found
   * 
   * @param parameterSearchKey
   *          Search key for the parameter
   * @param constantParameters
   *          Map with the constant parameters
   * @param inputParams
   *          Map with the input parameter
   * @return
   */
  public static String getConstantOrInputParameter(final String parameterSearchKey,
      final Map<String, String> constantParameters, final Map<String, String> inputParams) {
    String value;
    value = constantParameters.get(parameterSearchKey);
    if (value == null) {
      value = inputParams.get(parameterSearchKey);
    }
    return value;
  }
}
