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

package org.projectdatahopper.hop.formula.operators;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.projectdatahopper.hop.formula.common.FormulaTestBase;

import java.math.BigDecimal;

/**
 * Creation-Date: 11.26.2007, 15:31:58
 *
 * @author David Kincade
 */
public class PercentageOperatorTest extends FormulaTestBase {
  @Override
  public Object[][] createDataTest() {
    return new Object[][]
      {
        { "0.25%", new BigDecimal( "0.0025" ) },
        { "25%", new BigDecimal( "0.25" ) },
        { "125%", new BigDecimal( "1.25" ) },
      };
  }

  @Test
  public void testDefault() throws Exception {
    runDefaultTest();
  }

}
