#!/usr/bin/env bash
# Regression check for Java4Shell (CVE-2026-114938): every service Dockerfile must
# pin its base image by exact tag and digest, and the JDK inside must be a fixed build.
set -euo pipefail

cd "$(dirname "$0")/.."

# Fixed builds per affected line; 8 and 11 are not affected.
fixed_for_line() {
  case "$1" in
    8|11) echo "0" ;;
    17) echo "17.0.21.1" ;;
    21) echo "21.0.13.1" ;;
    25) echo "25.0.5.1" ;;
    27) echo "27.0.1.1" ;;
    *) echo "" ;;
  esac
}

version_ge() {
  [ "$(printf '%s\n%s\n' "$2" "$1" | sort -V | head -n1)" = "$2" ]
}

status=0
for dockerfile in */Dockerfile; do
  image=$(awk 'toupper($1) == "FROM" { print $2; exit }' "$dockerfile")
  if [[ "$image" != *@sha256:* ]]; then
    echo "FAIL $dockerfile: base image '$image' is not pinned by digest"
    status=1
    continue
  fi

  version=$( (docker run --rm --entrypoint java "$image" -version 2>&1 || true) \
    | sed -n 's/^openjdk version "\([0-9.]*\)".*/\1/p' | head -n1)
  line=${version%%.*}
  fixed=$(fixed_for_line "$line")

  if [ -z "$version" ] || [ -z "$fixed" ] || ! version_ge "$version" "$fixed"; then
    echo "FAIL $dockerfile: $image runs JDK '${version:-unknown}', needs >= ${fixed:-a supported line}"
    status=1
  else
    echo "OK   $dockerfile: JDK $version (>= $fixed)"
  fi
done

exit $status
