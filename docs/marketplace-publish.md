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
  lib/   # hop-formula
config/projects/samples/transforms/
  pentaho-formula-*.hpl
```

## One-time Nexus

Hosted Maven 2 repo **`hop-community-plugins`** (already used by hop-pentaho-reporting and hopper-edw):

- Anonymous **read** for marketplace install
- Deploy user with **write** (Jenkins credential id `nexus-hop-community`)

## What gets published

**One zip** on `hop-community-plugins`. That zip already **contains** the Hop
transform **and** `hop-formula` under `lib/`. End users only install that zip
via marketplace.

Maven builds the zip by packaging the reactor modules `hop-formula` and `hop-transform-pentaho-formula`.

Do **not** run `mvn deploy` on the reactor: that would publish the parent POM,
transform JAR, and assembly POM. Marketplace install uses `deploy-file` of the
assembly zip under GAV `org.projectdatahopper.hop:hop-pentaho-formula`.

## Package & publish (local)

Credentials: gitignored [`nexus-vars.sh`](../nexus-vars.sh) in the repo root (`NEXUS_USER` / `NEXUS_PASSWORD`). The publish script sources it automatically. Jenkins uses credential `nexus-hop-community` instead.

```bash
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
Resolved hop-pentaho-formula → org.projectdatahopper.hop:hop-pentaho-formula:1.1.0-SNAPSHOT (prefer repo 'data-hopper-community')
… Marketplace - Downloading org.projectdatahopper.hop:hop-pentaho-formula:1.1.0-SNAPSHOT from https://repository.data-hopper.com/repository/hop-community-plugins/…
… Marketplace - Installed org.projectdatahopper.hop:hop-pentaho-formula:1.1.0-SNAPSHOT. Restart Hop to load the plugin.
Plugin org.projectdatahopper.hop:hop-pentaho-formula:1.1.0-SNAPSHOT installed under <HOP_HOME> from repo 'data-hopper-community'. Restart Hop to load it.
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

## License

This project is licensed under **Apache-2.0**.
