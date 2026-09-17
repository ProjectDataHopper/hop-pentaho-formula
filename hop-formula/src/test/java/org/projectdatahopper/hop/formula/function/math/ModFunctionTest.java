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

package org.projectdatahopper.hop.formula.function.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.projectdatahopper.hop.formula.common.FormulaTestBase;
import org.projectdatahopper.hop.formula.error.FormulaErrorValue;

import java.math.BigDecimal;

/**
 * @author Cedric Pronzato
 */
public class ModFunctionTest extends FormulaTestBase {
  @Test
  public void testDefault() throws Exception {
    runDefaultTest();
  }

  @Override
  public Object[][] createDataTest() {
    return new Object[][]
      {
        { "MOD(10;3)", new BigDecimal( 1 ) },
        { "MOD(2;8)", new BigDecimal( 2 ) },
        { "MOD(5.5;2.5)", new BigDecimal( 0.5 ) },
        { "MOD(-2;3)", new BigDecimal( 1 ) },
        { "MOD(2;-3)", new BigDecimal( -1 ) },
        { "MOD(-2;-3)", new BigDecimal( -2 ) },
        { "MOD(10;0)", FormulaErrorValue.ERROR_ARITHMETIC_VALUE },

        // custom tests
        { "MOD(40;50)", new BigDecimal( 40 ) },
        { "MOD(-40;50)", new BigDecimal( 10 ) },
        { "MOD(40;-50)", new BigDecimal( -10 ) },
        { "MOD(-40;-50)", new BigDecimal( -40 ) },
      };
  }


}
