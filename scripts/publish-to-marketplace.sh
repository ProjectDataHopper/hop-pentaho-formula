#!/usr/bin/env bash
# Package the marketplace zip and deploy to hop-community-plugins on Nexus.
#
# Prerequisites:
#   - libformula 10.1 resolvable from pentaho-reporting-lgpl (engine Jenkins job)
#   - Hop APIs in local m2 (hop.version)
#   - Nexus deploy credentials in gitignored nexus-vars.sh (NEXUS_USER / NEXUS_PASSWORD)
#     or already exported in the environment
#
# Usage:
#   ./scripts/publish-to-marketplace.sh
#   ./scripts/publish-to-marketplace.sh --hop-version 2.19.0
#   ./scripts/publish-to-marketplace.sh --dry-run
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "${ROOT}"

GROUP_ID="${GROUP_ID:-org.projectdatahopper.hop}"
ARTIFACT_ID="${ARTIFACT_ID:-hop-pentaho-formula}"
NEXUS_URL="https://repository.data-hopper.com/repository/hop-community-plugins/"
NEXUS_REPO_ID="hop-community-plugins"
HOP_VERSION="${HOP_VERSION:-}"
DRY_RUN=0
MVN="${MVN:-mvn}"

usage() {
  sed -n '2,14p' "$0" | sed 's/^# \{0,1\}//'
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --hop-version) HOP_VERSION="$2"; shift 2 ;;
    --nexus-url) NEXUS_URL="$2"; shift 2 ;;
    --repo-id) NEXUS_REPO_ID="$2"; shift 2 ;;
    --group-id) GROUP_ID="$2"; shift 2 ;;
    --artifact-id) ARTIFACT_ID="$2"; shift 2 ;;
    --dry-run) DRY_RUN=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; usage >&2; exit 2 ;;
  esac
done

NEXUS_URL="${NEXUS_URL%/}/"
HOP_PROPS=()
if [[ -n "${HOP_VERSION}" ]]; then
  HOP_PROPS+=(-Dhop.version="${HOP_VERSION}")
fi

PLUGIN_VERSION="$(${MVN} -q -DforceStdout help:evaluate -Dexpression=project.version ${HOP_PROPS[@]+"${HOP_PROPS[@]}"} 2>/dev/null | tail -1)"
if [[ -z "${PLUGIN_VERSION}" || "${PLUGIN_VERSION}" == *\$\{* ]]; then
  PLUGIN_VERSION="$(grep -m1 '<version>' "${ROOT}/pom.xml" | sed -E 's/.*<version>([^<]+)<.*/\1/')"
fi

ZIP_PATH="${ROOT}/assemblies/pentaho-formula/target/${ARTIFACT_ID}-${PLUGIN_VERSION}.zip"

echo "==> Plugin ${GROUP_ID}:${ARTIFACT_ID}:${PLUGIN_VERSION}"
echo "    Hop property: ${HOP_VERSION:- (pom default) }"
echo "    Nexus: ${NEXUS_URL} (server id ${NEXUS_REPO_ID})"
echo "    Zip: ${ZIP_PATH}"

run() {
  if [[ "${DRY_RUN}" -eq 1 ]]; then
    echo "DRY-RUN: $*"
  else
    "$@"
  fi
}

echo "==> mvn package"
run ${MVN} -B clean package -DskipTests ${HOP_PROPS[@]+"${HOP_PROPS[@]}"}

if [[ "${DRY_RUN}" -eq 1 ]]; then
  echo "DRY-RUN: would deploy ${ZIP_PATH}"
  exit 0
fi

if [[ ! -f "${ZIP_PATH}" ]]; then
  echo "ERROR: expected zip not found: ${ZIP_PATH}" >&2
  ls -la "${ROOT}/assemblies/pentaho-formula/target/" >&2 || true
  exit 1
fi

# Workstation env often has NEXUS_* for a different Artifactory. nexus-vars.sh is
# the auth for repository.data-hopper.com. Keep URL/repo id (CLI flags win).
if [[ -f "${ROOT}/nexus-vars.sh" ]]; then
  echo "==> Loading Nexus credentials from nexus-vars.sh"
  _saved_url="${NEXUS_URL}"
  _saved_id="${NEXUS_REPO_ID}"
  # shellcheck disable=SC1091
  set +u
  # shellcheck source=/dev/null
  source "${ROOT}/nexus-vars.sh"
  set -u
  NEXUS_URL="${_saved_url}"
  NEXUS_REPO_ID="${_saved_id}"
  unset _saved_url _saved_id
fi

if [[ -z "${NEXUS_USER:-}" || -z "${NEXUS_PASSWORD:-}" ]]; then
  echo "NOTE: NEXUS_USER/NEXUS_PASSWORD not set (no nexus-vars.sh); using Maven settings.xml server '${NEXUS_REPO_ID}'"
else
  echo "    deploy user: ${NEXUS_USER}"
fi

SETTINGS=""
if [[ -n "${NEXUS_USER:-}" && -n "${NEXUS_PASSWORD:-}" ]]; then
  SETTINGS="$(mktemp)"
  # XML-escape password specials for settings.xml
  esc() { printf '%s' "$1" | sed -e 's/&/\&amp;/g' -e 's/</\&lt;/g' -e 's/>/\&gt;/g' -e "s/'/\&apos;/g" -e 's/"/\&quot;/g'; }
  cat >"${SETTINGS}" <<EOF
<settings>
  <servers>
    <server>
      <id>${NEXUS_REPO_ID}</id>
      <username>$(esc "${NEXUS_USER}")</username>
      <password>$(esc "${NEXUS_PASSWORD}")</password>
    </server>
  </servers>
</settings>
EOF
  trap 'rm -f "${SETTINGS}"' EXIT
  MVN_S=(-s "${SETTINGS}")
else
  MVN_S=()
fi

echo "==> deploy-file zip to ${NEXUS_URL}"
run ${MVN} -B "${MVN_S[@]+"${MVN_S[@]}"}" \
  org.apache.maven.plugins:maven-deploy-plugin:3.1.3:deploy-file \
  -DgroupId="${GROUP_ID}" \
  -DartifactId="${ARTIFACT_ID}" \
  -Dversion="${PLUGIN_VERSION}" \
  -Dpackaging=zip \
  -Dfile="${ZIP_PATH}" \
  -DgeneratePom=true \
  -DrepositoryId="${NEXUS_REPO_ID}" \
  -Durl="${NEXUS_URL}"

echo
echo "Published ${GROUP_ID}:${ARTIFACT_ID}:${PLUGIN_VERSION}"
echo "Marketplace path:"
echo "  ${NEXUS_URL}${GROUP_ID//.//}/${ARTIFACT_ID}/${PLUGIN_VERSION}/${ARTIFACT_ID}-${PLUGIN_VERSION}.zip"
echo
echo "Users (Hop 2.19+):"
echo "  ./hop marketplace repo import hop-marketplace-repo.yaml"
echo "  ./hop marketplace install ${ARTIFACT_ID}"
echo "  # restart Hop"
