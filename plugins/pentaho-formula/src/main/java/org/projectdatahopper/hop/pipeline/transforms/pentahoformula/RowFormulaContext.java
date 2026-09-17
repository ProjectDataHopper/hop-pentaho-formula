/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.projectdatahopper.hop.formula.DefaultFormulaContext;
import org.projectdatahopper.hop.formula.EvaluationException;
import org.projectdatahopper.hop.formula.FormulaContext;
import org.projectdatahopper.hop.formula.LocalizationContext;
import org.projectdatahopper.hop.formula.error.FormulaErrorValue;
import org.projectdatahopper.hop.formula.function.FunctionRegistry;
import org.projectdatahopper.hop.formula.typing.DataType;
import org.projectdatahopper.hop.formula.typing.TypeRegistry;

/**
 * Formula context that resolves {@code [field]} references against the current Hop row.
 */
@Getter
@Setter
public class RowFormulaContext implements FormulaContext {
  private IRowMeta rowMeta;
  private final FormulaContext formulaContext;
  private final Map<String, Integer> valueIndexMap;
  private Object[] rowData;

  public RowFormulaContext(IRowMeta row) {
    this.formulaContext = new DefaultFormulaContext();
    this.rowMeta = row;
    this.rowData = null;
    this.valueIndexMap = new HashMap<>();
  }

  @Override
  public DataType resolveReferenceType(Object name) {
    if (name instanceof String fieldName) {
      IValueMeta valueMeta = this.rowMeta.searchValueMeta(fieldName);
      if (valueMeta != null) {
        return switch (valueMeta.getType()) {
          case IValueMeta.TYPE_STRING -> DataType.TEXT;
          case IValueMeta.TYPE_INTEGER, IValueMeta.TYPE_BIGNUMBER, IValueMeta.TYPE_NUMBER ->
              DataType.NUMBER;
          case IValueMeta.TYPE_BOOLEAN -> DataType.LOGICAL;
          case IValueMeta.TYPE_DATE, IValueMeta.TYPE_TIMESTAMP -> DataType.DATETIME;
          default -> DataType.ANY;
        };
      }
    }
    return DataType.ANY;
  }

  @Override
  public Object resolveReference(Object name) throws EvaluationException {
    if (name instanceof String fieldName) {
      IValueMeta valueMeta;
      Integer idx = valueIndexMap.get(fieldName);
      if (idx != null) {
        valueMeta = rowMeta.getValueMeta(idx);
      } else {
        int index = rowMeta.indexOfValue(fieldName);
        if (index < 0) {
          throw new EvaluationException(FormulaErrorValue.ERROR_INVALID_ARGUMENT_VALUE);
        }
        valueMeta = rowMeta.getValueMeta(index);
        valueIndexMap.put(fieldName, index);
        idx = index;
      }
      Object valueData = rowData[idx];
      try {
        return getPrimitive(valueMeta, valueData);
      } catch (HopValueException e) {
        throw new EvaluationException(FormulaErrorValue.ERROR_ARITHMETIC_VALUE, e);
      }
    }
    return null;
  }

  @Override
  public boolean isReferenceDirty(Object name) throws EvaluationException {
    return formulaContext.isReferenceDirty(name);
  }

  @Override
  public Locale getLocale() {
    return formulaContext.getLocale();
  }

  @Override
  public TimeZone getTimeZone() {
    return formulaContext.getTimeZone();
  }

  @Override
  public LocalizationContext getLocalizationContext() {
    return formulaContext.getLocalizationContext();
  }

  @Override
  public TypeRegistry getTypeRegistry() {
    return formulaContext.getTypeRegistry();
  }

  @Override
  public FunctionRegistry getFunctionRegistry() {
    return formulaContext.getFunctionRegistry();
  }

  @Override
  public Date getCurrentDate() {
    return new Date();
  }

  public static Object getPrimitive(IValueMeta valueMeta, Object valueData)
      throws HopValueException {
    return switch (valueMeta.getType()) {
      case IValueMeta.TYPE_BIGNUMBER -> valueMeta.getBigNumber(valueData);
      case IValueMeta.TYPE_BINARY -> valueMeta.getBinary(valueData);
      case IValueMeta.TYPE_BOOLEAN -> valueMeta.getBoolean(valueData);
      case IValueMeta.TYPE_DATE -> valueMeta.getDate(valueData);
      case IValueMeta.TYPE_INTEGER -> valueMeta.getInteger(valueData);
      case IValueMeta.TYPE_NUMBER -> valueMeta.getNumber(valueData);
      case IValueMeta.TYPE_STRING, IValueMeta.TYPE_TIMESTAMP -> valueMeta.getString(valueData);
      default -> null;
    };
  }

  public static Class<?> getPrimitiveClass(int valueType) {
    return switch (valueType) {
      case IValueMeta.TYPE_BIGNUMBER -> BigDecimal.class;
      case IValueMeta.TYPE_BINARY -> byte[].class;
      case IValueMeta.TYPE_BOOLEAN -> Boolean.class;
      case IValueMeta.TYPE_DATE -> Date.class;
      case IValueMeta.TYPE_INTEGER -> Long.class;
      case IValueMeta.TYPE_NUMBER -> Double.class;
      case IValueMeta.TYPE_STRING -> String.class;
      default -> null;
    };
  }
}
