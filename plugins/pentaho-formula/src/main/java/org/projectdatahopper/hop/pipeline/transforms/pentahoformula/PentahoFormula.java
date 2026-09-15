/*
 * Copyright (C) 2026 Project Data Hopper
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.Utils;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;

/**
 * Evaluate OpenFormula expressions with Pentaho libformula.
 *
 * <p>Port of the PDI Formula step (Apache-2.0, 9.4.0.0-343) to the Hop 2.19 transform API.
 */
public class PentahoFormula extends BaseTransform<PentahoFormulaMeta, PentahoFormulaData> {
  private static final Class<?> PKG = PentahoFormula.class;

  public PentahoFormula(
      TransformMeta transformMeta,
      PentahoFormulaMeta meta,
      PentahoFormulaData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }

  @Override
  public boolean init() {
    if (!super.init()) {
      return false;
    }
    LibFormulaRuntime.ensureBooted();
    data.returnType = new int[meta.getFormulas().size()];
    for (int i = 0; i < meta.getFormulas().size(); i++) {
      data.returnType[i] = -1;
    }
    return true;
  }

  @Override
  public boolean processRow() throws HopException {
    Object[] r = getRow();
    if (r == null) {
      setOutputDone();
      return false;
    }

    if (first) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);

      data.context = new RowFormulaContext(data.outputRowMeta);

      data.replaceIndex = new int[meta.getFormulas().size()];
      for (int i = 0; i < meta.getFormulas().size(); i++) {
        PentahoFormulaMetaFunction fn = meta.getFormulas().get(i);
        if (!Utils.isEmpty(fn.getReplaceField())) {
          data.replaceIndex[i] = getInputRowMeta().indexOfValue(fn.getReplaceField());
          if (data.replaceIndex[i] < 0) {
            throw new HopException(
                BaseMessages.getString(
                    PKG,
                    "PentahoFormulaMeta.Exception.UnknownReplaceField",
                    fn.getReplaceField()));
          }
        } else {
          data.replaceIndex[i] = -1;
        }
      }
    }

    if (isRowLevel()) {
      logRowlevel("Read row #" + getLinesRead() + " : " + getInputRowMeta().getString(r));
    }

    Object[] outputRowData;
    try {
      outputRowData = calcFields(getInputRowMeta(), r);
    } catch (Exception e) {
      return handleFormulaError(r, e);
    }

    putRow(data.outputRowMeta, outputRowData);

    if (isRowLevel()) {
      logRowlevel(
          "Wrote row #" + getLinesWritten() + " : " + data.outputRowMeta.getString(outputRowData));
    }
    if (checkFeedback(getLinesRead()) && isBasic()) {
      logBasic("Linenr " + getLinesRead());
    }
    return true;
  }

  Object[] calcFields(IRowMeta rowMeta, Object[] r) throws HopException {
    try {
      Object[] outputRowData = RowDataUtil.createResizedCopy(r, data.outputRowMeta.size());
      int tempIndex = rowMeta.size();

      data.context.setRowData(outputRowData);

      if (data.formulas == null) {
        data.formulas = new org.pentaho.reporting.libraries.formula.Formula[meta.getFormulas().size()];
        for (int i = 0; i < meta.getFormulas().size(); i++) {
          PentahoFormulaMetaFunction fn = meta.getFormulas().get(i);
          if (!Utils.isEmpty(fn.getFieldName())) {
            data.formulas[i] = data.createFormula(fn.getFormula());
          } else {
            throw new HopException(
                BaseMessages.getString(
                    PKG,
                    "PentahoFormula.Exception.MissingFieldName",
                    Const.NVL(fn.getFormula(), "")));
          }
        }
      }

      for (int i = 0; i < meta.getFormulas().size(); i++) {
        PentahoFormulaMetaFunction fn = meta.getFormulas().get(i);
        if (Utils.isEmpty(fn.getFieldName())) {
          continue;
        }
        if (data.formulas[i] == null) {
          data.formulas[i] = data.createFormula(fn.getFormula());
        }

        Object formulaResult = data.formulas[i].evaluate();
        if (formulaResult instanceof LibFormulaErrorValue errorValue) {
          throw new HopException(
              BaseMessages.getString(
                  PKG,
                  "PentahoFormula.Exception.FormulaError",
                  fn.getFormula(),
                  fn.getFieldName(),
                  errorValue.toString()));
        }

        if (data.returnType[i] < 0) {
          discoverReturnType(fn, formulaResult, i);
        }

        int realIndex = (data.replaceIndex[i] < 0) ? tempIndex++ : data.replaceIndex[i];
        outputRowData[realIndex] = getReturnValue(formulaResult, data.returnType[i], realIndex, fn);
      }

      return outputRowData;
    } catch (HopException e) {
      throw e;
    } catch (Throwable e) {
      throw new HopValueException(e);
    }
  }

  private void discoverReturnType(PentahoFormulaMetaFunction fn, Object formulaResult, int i)
      throws HopValueException {
    if (formulaResult instanceof String) {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_STRING;
      fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_STRING);
    } else if (formulaResult instanceof Integer) {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_INTEGER;
      fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_INTEGER);
    } else if (formulaResult instanceof Long) {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_LONG;
      fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_INTEGER);
    } else if (formulaResult instanceof Date) {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_DATE;
      fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_DATE);
    } else if (formulaResult instanceof BigDecimal) {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_BIGDECIMAL;
      fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_BIGNUMBER);
    } else if (formulaResult instanceof Number) {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_NUMBER;
      fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_NUMBER);
    } else if (formulaResult instanceof byte[]) {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_BYTE_ARRAY;
      if (fn.getValueType() != IValueMeta.TYPE_BINARY) {
        throw new HopValueException(
            BaseMessages.getString(
                PKG, "PentahoFormula.Exception.ExpectBinary", fn.getFieldName(), fn.getFormula()));
      }
    } else if (formulaResult instanceof Boolean) {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_BOOLEAN;
      if (fn.getValueType() != IValueMeta.TYPE_BOOLEAN) {
        throw new HopValueException(
            BaseMessages.getString(
                PKG,
                "PentahoFormula.Exception.ExpectBoolean",
                fn.getFieldName(),
                fn.getFormula()));
      }
    } else {
      data.returnType[i] = PentahoFormulaData.RETURN_TYPE_STRING;
      fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_STRING);
    }
  }

  protected Object getReturnValue(
      Object formulaResult, int returnType, int realIndex, PentahoFormulaMetaFunction fn)
      throws HopException {
    if (formulaResult == null) {
      return null;
    }
    return switch (returnType) {
      case PentahoFormulaData.RETURN_TYPE_STRING ->
          fn.isNeedDataConversion()
              ? convertDataToTargetValueMeta(realIndex, formulaResult)
              : formulaResult.toString();
      case PentahoFormulaData.RETURN_TYPE_NUMBER ->
          fn.isNeedDataConversion()
              ? convertDataToTargetValueMeta(realIndex, formulaResult)
              : ((Number) formulaResult).doubleValue();
      case PentahoFormulaData.RETURN_TYPE_INTEGER ->
          fn.isNeedDataConversion()
              ? convertDataToTargetValueMeta(realIndex, formulaResult)
              : Long.valueOf(((Integer) formulaResult).intValue());
      case PentahoFormulaData.RETURN_TYPE_LONG,
              PentahoFormulaData.RETURN_TYPE_DATE,
              PentahoFormulaData.RETURN_TYPE_BIGDECIMAL,
              PentahoFormulaData.RETURN_TYPE_TIMESTAMP ->
          fn.isNeedDataConversion()
              ? convertDataToTargetValueMeta(realIndex, formulaResult)
              : formulaResult;
      case PentahoFormulaData.RETURN_TYPE_BYTE_ARRAY, PentahoFormulaData.RETURN_TYPE_BOOLEAN ->
          formulaResult;
      default -> null;
    };
  }

  private Object convertDataToTargetValueMeta(int i, Object formulaResult) throws HopException {
    if (formulaResult == null) {
      return null;
    }
    IValueMeta target = data.outputRowMeta.getValueMeta(i);
    IValueMeta actual = ValueMetaFactory.guessValueMetaInterface(formulaResult);
    return target.convertData(actual, formulaResult);
  }

  private boolean handleFormulaError(Object[] row, Exception e) throws HopTransformException {
    String message =
        BaseMessages.getString(
            PKG, "PentahoFormula.Exception.CouldNotBeEvaluated", e.getMessage());
    if (getTransformMeta().isDoingErrorHandling()) {
      putError(getInputRowMeta(), row, 1, message, null, "PentahoFormula001");
      return true;
    }
    logError(message, e);
    setErrors(1);
    stopAll();
    setOutputDone();
    return false;
  }
}
