{%- from 'gateway/settings.sls' import gateway with context %}

# This file contains the necessary configs for CM to properly operate with Knox

add_knox_settings_to_cm:
  file.append:
    - name: /etc/cloudera-scm-server/cm.settings
    - makedirs: True
    - template: jinja
    - source: salt://gateway/config/cm/knox.settings.j2
    - unless: grep "PROXYUSER_KNOX_GROUPS" /etc/cloudera-scm-server/cm.settings
    - context:
        knox_address: {{ salt['pillar.get']('gateway:address') }}

cloudera_manager_setup_knox:
  file.replace:
    - name: /etc/default/cloudera-scm-server
    - pattern: "CMF_SERVER_ARGS=.*"
    - repl: CMF_SERVER_ARGS="-i /etc/cloudera-scm-server/cm.settings"
    - unless: grep "CMF_SERVER_ARGS=\"-i /etc/cloudera-scm-server/cm.settings\"" /etc/default/cloudera-scm-server

