# Hop Pentaho Formula

Third-party **Apache Hop** transform plugin that evaluates **OpenFormula** expressions
with Pentaho **libformula** (LGPL). This is a port of the PDI Formula step for Hop
2.19.0+.

| | |
|---|---|
| License | **LGPL-2.1** (see [LICENSE](LICENSE)) |
| Hop | 2.19.0+ (Java 21) |
| Engine | libformula **10.1.x** LGPL |
| Plugin id | `PentahoFormula` |
| ASF? | **No** — cannot ship inside Apache Hop (LGPL libformula) |

## Why this exists

Apache Hop already has a **Formula** transform. That one uses **Apache POI** (Excel
formulas, comma-separated arguments). PDI Formula uses **libformula** (OpenFormula,
`;` arguments, `[field]` references). The dialects are not the same.

PDI Formula was never ported into Apache Hop because libformula is LGPL. This
repository provides it as an **external** marketplace plugin so migrators keep
existing PDI Formula semantics.

Do **not** use libformula / Pentaho Reporting 10.2+ (Business Source License).

## Build

Requirements:

- JDK 21
- Maven 3.9+
- Apache Hop 2.19.0 artifacts (`~/.m2` or Maven Central)
- libformula 10.1 from [pentaho-reporting-lgpl](https://repository.data-hopper.com/#browse/browse:pentaho-reporting-lgpl:org%2Fpentaho%2Freporting%2Flibrary%2Flibformula)

```bash
mvn clean package
```

Plugin zip:

```text
assemblies/pentaho-formula/target/hop-pentaho-formula-1.0.0-SNAPSHOT.zip
```

## Install

Unzip into the **Hop client root**:

```bash
unzip hop-pentaho-formula-*.zip -d "$HOP_HOME"
```

Expected layout:

```text
$HOP_HOME/plugins/transforms/pentaho-formula/
  hop-transform-pentaho-formula-*.jar
  version.xml
  LICENSE
  NOTICE
  lib/          # libformula, libbase, …
```

Restart Hop. The transform appears as **Pentaho Formula** in the Scripting category.

Hop's built-in POI Formula transform remains installed and is a different plugin.

## Kettle / PDI import

When this plugin is loaded, Hop's Kettle importer is extended:

1. PDI `<type>Formula</type>` steps in imported `.ktr` files are remapped to
   `PentahoFormula`.
2. OpenFormula argument separators that the stock importer turned into commas
   (`IF([a],"Y","N")`) are restored to semicolons (`IF([a];"Y";"N")`).
3. Formula list wrapping (`<formulas>`) and `value_type` name→id conversion stay
   as the stock importer already does them.

Hop pipelines that already use the POI Formula transform (they persist `set_na`)
are left unchanged.

## Formula syntax (PDI-compatible)

- No leading `=`
- Field references: `[fieldName]`
- Function arguments separated by `;`
- Example: `IF([flag];"yes";"no")`

## Marketplace

See [hop-marketplace-repo.yaml](hop-marketplace-repo.yaml).
