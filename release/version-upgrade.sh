#!/bin/bash

OLD_VERSION=2.4.2-RELEASE
NEW_VERSION=2.4.3-RELEASE

cd ..

OLD_VERSION_NORMALIZED=$(echo "${OLD_VERSION}" | sed -e 's/[]$.*[\^]/\\&/g' )
NEW_VERSION_NORMALIZED=$(echo "${NEW_VERSION}" | sed -e 's/[]$.*[\^]/\\&/g' )
find . -type f ! -path "./release/version-upgrade.sh" -exec sed -i "s/${OLD_VERSION_NORMALIZED}/${NEW_VERSION_NORMALIZED}/g" {} +
