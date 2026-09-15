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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

/**
 * Metadata for the Pentaho Formula transform (libformula / OpenFormula).
 *
 * <p>Plugin id {@code PentahoFormula} is distinct from Hop's built-in POI {@code Formula}
 * transform. PDI Formula steps are remapped to this id by the Kettle import extension.
 */
@Getter
@Setter
@Transform(
    id = "PentahoFormula",
    image = "pentaho-formula.svg",
    name = "i18n::PentahoFormula.Name",
    description = "i18n::PentahoFormula.Description",
    categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Scripting",
    keywords = "i18n::PentahoFormula.keywords",
    classLoaderGroup = "pentaho-formula")
public class PentahoFormulaMeta extends BaseTransformMeta<PentahoFormula, PentahoFormulaData> {

  private static final Class<?> PKG = PentahoFormulaMeta.class;

  @HopMetadataProperty(
      groupKey = "formulas",
      key = "formula",
      injectionGroupKey = "FORMULAS",
      injectionGroupDescription = "PentahoFormulaMeta.Injection.Formulas")
  private List<PentahoFormulaMetaFunction> formulas;

  public PentahoFormulaMeta() {
    super();
    formulas = new ArrayList<>();
  }

  public PentahoFormulaMeta(PentahoFormulaMeta other) {
    this();
    for (PentahoFormulaMetaFunction formula : other.formulas) {
      this.formulas.add(new PentahoFormulaMetaFunction(formula));
    }
  }

  @Override
  public Object clone() {
    PentahoFormulaMeta copy = (PentahoFormulaMeta) super.clone();
    List<PentahoFormulaMetaFunction> cloned = new ArrayList<>();
    if (formulas != null) {
      for (PentahoFormulaMetaFunction formula : formulas) {
        cloned.add(new PentahoFormulaMetaFunction(formula));
      }
    }
    copy.formulas = cloned;
    return copy;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public void setDefault() {
    formulas = new ArrayList<>();
  }

  @Override
  public void getFields(
      IRowMeta row,
      String name,
      IRowMeta[] info,
      TransformMeta nextTransform,
      IVariables variables,
      IHopMetadataProvider metadataProvider)
      throws HopTransformException {
    for (PentahoFormulaMetaFunction fn : formulas) {
      if (Utils.isEmpty(fn.getReplaceField())) {
        if (!Utils.isEmpty(fn.getFieldName())) {
          try {
            IValueMeta v = ValueMetaFactory.createValueMeta(fn.getFieldName(), fn.getValueType());
            v.setLength(fn.getValueLength(), fn.getValuePrecision());
            v.setOrigin(name);
            row.addValueMeta(v);
          } catch (Exception e) {
            throw new HopTransformException(e);
          }
        }
      } else {
        int index = row.indexOfValue(fn.getReplaceField());
        if (index < 0) {
          throw new HopTransformException(
              BaseMessages.getString(
                  PKG, "PentahoFormulaMeta.Exception.UnknownReplaceField", fn.getReplaceField()));
        }
        IValueMeta v = row.getValueMeta(index).clone();
        v.setLength(fn.getValueLength(), fn.getValuePrecision());
        v.setOrigin(name);
        row.setValueMeta(index, v);
      }
    }
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta pipelineMeta,
      TransformMeta transformMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {
    if (prev == null || prev.size() == 0) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_WARNING,
              BaseMessages.getString(PKG, "PentahoFormulaMeta.CheckResult.ExpectedInputError"),
              transformMeta));
    } else {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_OK,
              BaseMessages.getString(
                  PKG, "PentahoFormulaMeta.CheckResult.FieldsReceived", "" + prev.size()),
              transformMeta));
    }

    if (input.length > 0) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_OK,
              BaseMessages.getString(PKG, "PentahoFormulaMeta.CheckResult.ExpectedInputOk"),
              transformMeta));
    } else {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              BaseMessages.getString(PKG, "PentahoFormulaMeta.CheckResult.ExpectedInputError"),
              transformMeta));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PentahoFormulaMeta that)) {
      return false;
    }
    return Objects.equals(formulas, that.formulas);
  }

  @Override
  public int hashCode() {
    return Objects.hash(formulas);
  }
}
