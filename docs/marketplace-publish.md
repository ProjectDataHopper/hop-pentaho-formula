# Publish Pentaho Formula to Nexus + Hop marketplace

## Coordinates

| | |
|--|--|
| groupId | `org.projectdatahopper.hop` |
| artifactId | `hop-pentaho-formula` |
| version | Marketplace release **1.0.0**; `main` is `1.1.0-SNAPSHOT` |
| packaging | **zip** (marketplace installable) |
| Nexus repo | `https://repository.data-hopper.com/repository/hop-community-plugins/` |
| Jenkins | https://jenkins.data-hopper.com/ |
| Install path | `plugins/transforms/pentaho-formula/` |

Zip layout (unzip into **Hop client root**):

```text
plugins/transforms/pentaho-formula/
  hop-transform-pentaho-formula-*.jar
  version.xml
  LICENSE
  NOTICE
  lib/   # LGPL libformula (+ transitives)
config/projects/samples/transforms/
  pentaho-formula-*.hpl
```

## One-time Nexus

Hosted Maven 2 repo **`hop-community-plugins`** (already used by hop-pentaho-reporting and hopper-edw):

- Anonymous **read** for marketplace install
- Deploy user with **write** (Jenkins credential id `nexus-hop-community`)

## What gets published

**One zip** on `hop-community-plugins`. That zip already **contains** the Hop
transform **and** LGPL libformula under `lib/`. End users only install that zip
via marketplace.

Maven builds the zip by resolving `libformula` (and dependencies) from
`pentaho-reporting-lgpl` (the engine Jenkins job). There is no vendor-folder
fallback in this repository.

Do **not** run `mvn deploy` on the reactor: that would publish the parent POM,
transform JAR, and assembly POM. Marketplace install uses `deploy-file` of the
assembly zip under GAV `org.projectdatahopper.hop:hop-pentaho-formula`.

## Package & publish (local)

Publish the **release** zip from `releases/1.0.0` (Maven version `1.0.0`). Do not
publish `main` (`1.1.0-SNAPSHOT`) as the marketplace pin.

```bash
git checkout releases/1.0.0
# libformula must be resolvable (successful pentaho-reporting-lgpl-engine job)
export NEXUS_USER=hop_community_build NEXUS_PASSWORD=...
# hop_build is 403 on hop-community-plugins; use hop_community_build
./scripts/publish-to-marketplace.sh
# optional: --hop-version 2.19.0
# optional: --dry-run
```

## Jenkins job

1. Open https://jenkins.data-hopper.com/
2. **New Item** → Pipeline → **Pipeline script from SCM** → this repo
3. **Script Path:** `Jenkinsfile.marketplace`
4. Suggested job name: `hop-pentaho-formula-marketplace`
5. Credential ID matching parameter (default `nexus-hop-community`)
6. Tools: `jdk-21`, `Maven 3.9.9` (adjust names in the file if needed)
7. Prefer: engine job has already published `libformula` to `pentaho-reporting-lgpl`

## Appear in Hop 2.19 marketplace

Marketplace **UI/CLI requires Hop 2.19+**.

### CLI

```bash
# Once: register the Data Hopper community repo
./hop marketplace repo import hop-marketplace-repo.yaml
# or after push:
# ./hop marketplace repo import \
#   https://raw.githubusercontent.com/ProjectDataHopper/hop-pentaho-formula/refs/heads/main/hop-marketplace-repo.yaml

./hop marketplace query | grep -i formula
./hop marketplace install hop-pentaho-formula
# Restart Hop GUI / hop-server
```

Example install session:

```text
$ sh hop marketplace install hop-pentaho-formula
Resolved hop-pentaho-formula → org.projectdatahopper.hop:hop-pentaho-formula:1.0.0 (prefer repo 'data-hopper-community')
… Marketplace - Downloading org.projectdatahopper.hop:hop-pentaho-formula:1.0.0 from https://repository.data-hopper.com/repository/hop-community-plugins/…
… Marketplace - Installed org.projectdatahopper.hop:hop-pentaho-formula:1.0.0. Restart Hop to load the plugin.
Plugin org.projectdatahopper.hop:hop-pentaho-formula:1.0.0 installed under <HOP_HOME> from repo 'data-hopper-community'. Restart Hop to load it.
```

If the repo `data-hopper-community` already exists (from hop-pentaho-reporting or
hopper-edw), re-import updates metadata; with **`browse: true`**, the new zip is
also discovered live from Nexus.

### Hop GUI

1. Marketplace toolbar → **Repositories**
2. Import `hop-marketplace-repo.yaml` or add URL
   `https://repository.data-hopper.com/repository/hop-community-plugins/`
   with browse enabled
3. Find **Pentaho Formula** → Install → restart

After restart: pipeline palette **Scripting** → **Pentaho Formula**
(plugin id `PentahoFormula`). Hop's built-in POI Formula transform remains a
different plugin.

## Note on licenses

This plugin is **LGPL-2.1** and ships libformula. It is **not** an Apache
Software Foundation release. Keep that clear in marketplace description
(already in the YAML). Do not use libformula / Pentaho Reporting 10.2+ (BSL).
