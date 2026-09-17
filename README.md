# Hop Pentaho Formula

**Apache Hop** transform plugin that evaluates **OpenFormula** expressions
using **`hop-formula`**, a clean-room Apache-2.0 calculation engine. This is a port
of the PDI Formula step for Hop 2.19.0+.

| | |
|---|---|
| License | **Apache-2.0** (see [LICENSE](LICENSE)) |
| Hop | 2.19.0+ (Java 21) |
| Engine | **`hop-formula`** (clean-room OpenFormula engine, zero external runtime dependencies) |
| Plugin id | `PentahoFormula` |
| Marketplace | **[1.0.0](https://github.com/ProjectDataHopper/hop-pentaho-formula/releases/tag/v1.0.0)** (`org.projectdatahopper.hop:hop-pentaho-formula:1.0.0`) |
| `main` | `1.1.0-SNAPSHOT` |
| ASF-compatible? | **Yes** — pure Apache-2.0 license, zero LGPL/BSL code or dependencies |

## Why this exists

Apache Hop already has a **Formula** transform. That one uses **Apache POI** (Excel
formulas, comma-separated arguments). PDI Formula uses **OpenFormula** (ODF 1.2,
`;` arguments, `[field]` references). The dialects are not the same.

PDI Formula was originally not included in Apache Hop because the historical Pentaho
engine (`libformula`) was LGPL (and later relicensed to BSL). This repository provides:
1. **`hop-formula`**: a clean-room, zero-dependency Apache-2.0 OpenFormula calculation engine.
2. **`PentahoFormula`**: a Hop transform preserving 100% backward compatibility with PDI Formula pipelines.

## Build

Requirements:

- JDK 21
- Maven 3.9+
- Apache Hop 2.19.0 artifacts (`~/.m2` or Maven Central)

```bash
mvn clean package
```

Plugin zip:

```text
assemblies/pentaho-formula/target/hop-pentaho-formula-1.1.0-SNAPSHOT.zip
```

## Install

### Manual zip

Download `hop-pentaho-formula-*.zip` and unzip into the **Hop client root**:

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
  lib/          # hop-formula
```

Restart Hop. The transform appears as **Pentaho Formula** in the Scripting category
(plugin id `PentahoFormula`).

Hop's built-in POI Formula transform remains installed and is a different plugin.

### Hop 2.19 marketplace (Nexus)

You publish **one zip** that already includes the transform **and** `hop-formula`
jar under `lib/`. Users only install that zip.

```bash
# Credentials: gitignored nexus-vars.sh (NEXUS_USER / NEXUS_PASSWORD)
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
Resolved hop-pentaho-formula → org.projectdatahopper.hop:hop-pentaho-formula:1.1.0-SNAPSHOT (prefer repo 'data-hopper-community')
… Marketplace - Downloading org.projectdatahopper.hop:hop-pentaho-formula:1.1.0-SNAPSHOT from https://repository.data-hopper.com/repository/hop-community-plugins/…
… Marketplace - Installed org.projectdatahopper.hop:hop-pentaho-formula:1.1.0-SNAPSHOT. Restart Hop to load the plugin.
Plugin … installed under <HOP_HOME> from repo 'data-hopper-community'. Restart Hop to load it.
```

After restart, the transform **Pentaho Formula** appears under **Scripting**.

Repository: `https://repository.data-hopper.com/repository/hop-community-plugins/`  
GAV: `org.projectdatahopper.hop:hop-pentaho-formula:{version}` (zip)

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
  hop-formula/                      # clean-room OpenFormula calculation engine
  plugins/pentaho-formula/          # Hop transform jar & editor
  assemblies/pentaho-formula/       # installable plugin zip
  samples/                          # example pipelines
  Jenkinsfile.marketplace           # zip → hop-community-plugins
  scripts/publish-to-marketplace.sh # local Nexus publish
  hop-marketplace-repo.yaml         # Hop 2.19+ marketplace import
```
