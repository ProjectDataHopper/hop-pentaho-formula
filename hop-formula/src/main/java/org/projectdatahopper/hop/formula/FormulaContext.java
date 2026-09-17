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

package org.projectdatahopper.hop.formula;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.projectdatahopper.hop.formula.function.FunctionRegistry;
import org.projectdatahopper.hop.formula.typing.DataType;
import org.projectdatahopper.hop.formula.typing.TypeRegistry;
import org.projectdatahopper.hop.formula.typing.TypedValue;

/**
 * Execution context providing reference resolution, localization, type handling, and functions.
 */
public interface FormulaContext {
  Object resolveReference(Object name) throws EvaluationException;

  DataType resolveReferenceType(Object name);

  default boolean isReferenceDirty(Object name) throws EvaluationException {
    return false;
  }

  Locale getLocale();

  TimeZone getTimeZone();

  LocalizationContext getLocalizationContext();

  TypeRegistry getTypeRegistry();

  FunctionRegistry getFunctionRegistry();

  default Date getCurrentDate() {
    return new Date();
  }
}
