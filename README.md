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
| Marketplace | **[1.0.0](https://github.com/ProjectDataHopper/hop-pentaho-formula/releases/tag/v1.0.0)** (`org.projectdatahopper.hop:hop-pentaho-formula:1.0.0`) |
| `main` | `1.1.0-SNAPSHOT` |
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
assemblies/pentaho-formula/target/hop-pentaho-formula-1.1.0-SNAPSHOT.zip
```

## Install

### Manual zip

Download [hop-pentaho-formula-1.0.0.zip](https://github.com/ProjectDataHopper/hop-pentaho-formula/releases/download/v1.0.0/hop-pentaho-formula-1.0.0.zip) and unzip into the **Hop client root**:

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

Restart Hop. The transform appears as **Pentaho Formula** in the Scripting category
(plugin id `PentahoFormula`).

Hop's built-in POI Formula transform remains installed and is a different plugin.

### Hop 2.19 marketplace (Nexus)

You publish **one zip** that already includes the transform **and** libformula
jars under `lib/`. Users only install that zip.

```bash
# After libformula is on pentaho-reporting-lgpl (engine Jenkins job)
export NEXUS_USER=... NEXUS_PASSWORD=...
./scripts/publish-to-marketplace.sh
```

Details: [docs/marketplace-publish.md](docs/marketplace-publish.md).

Users (Hop **2.19+**):

```bash
# once: register the Data Hopper community repo
./hop marketplace repo import hop-marketplace-repo.yaml
# or:
# ./hop marketplace repo import \
#   https://raw.githubusercontent.com/ProjectDataHopper/hop-pentaho-formula/refs/heads/main/hop-marketplace-repo.yaml

./hop marketplace query | grep -i formula
./hop marketplace install hop-pentaho-formula
# Restart Hop so the plugin loads
```

Example install session:

```text
$ sh hop marketplace install hop-pentaho-formula
Resolved hop-pentaho-formula → org.projectdatahopper.hop:hop-pentaho-formula:1.0.0 (prefer repo 'data-hopper-community')
… Marketplace - Downloading org.projectdatahopper.hop:hop-pentaho-formula:1.0.0 from https://repository.data-hopper.com/repository/hop-community-plugins/…
… Marketplace - Installed org.projectdatahopper.hop:hop-pentaho-formula:1.0.0. Restart Hop to load the plugin.
Plugin … installed under <HOP_HOME> from repo 'data-hopper-community'. Restart Hop to load it.
```

After restart, the transform **Pentaho Formula** appears under **Scripting**.

Repository: `https://repository.data-hopper.com/repository/hop-community-plugins/`  
GAV: `org.projectdatahopper.hop:hop-pentaho-formula:{version}` (zip, including LGPL libformula)

If `data-hopper-community` is already imported (from hop-pentaho-reporting or
hopper-edw), re-import updates plugin metadata; with **browse** enabled the zip
is also listed live from Nexus.

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

## Modules

```text
hop-pentaho-formula/
  plugins/pentaho-formula/          # transform jar
  assemblies/pentaho-formula/       # installable zip
  samples/                          # example pipelines
  Jenkinsfile.marketplace           # zip → hop-community-plugins
  scripts/publish-to-marketplace.sh # local Nexus publish
  hop-marketplace-repo.yaml         # Hop 2.19+ marketplace import
```
