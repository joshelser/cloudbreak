#!/bin/bash

## logging
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

set -x

export CLOUD_PLATFORM="${cloudPlatform}"
export START_LABEL=${platformDiskStartLabel}
export PLATFORM_DISK_PREFIX=${platformDiskPrefix}
export LAZY_FORMAT_DISK_LIMIT=12
export IS_GATEWAY=${gateway?c}
export TMP_SSH_KEY="${tmpSshKey}"
export SIGN_KEY="${signaturePublicKey}"
export PUBLIC_SSH_KEY="${publicSshKey}"
export RELOCATE_DOCKER=${relocateDocker?c}
export SSH_USER=${sshUser}

${customUserData}

/usr/bin/user-data-helper.sh "$@" &> /var/log/user-data.log

mkdir -p /etc/systemd/system/salt-bootstrap.service.d
cat > /etc/systemd/system/salt-bootstrap.service.d/envs.conf << EOF
[Service]
Environment="SALTBOOT_USERNAME=cbadmin"
Environment="SALTBOOT_PASSWORD=${saltBootPassword}"
EOF
systemctl daemon-reload