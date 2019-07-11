#!/usr/bin/env bash
set -e

echo "{{ salt['pillar.get']('postgres:remote_admin_pw') }}" > ~/.pgpass

chmod 600 ~/.pgpass

{% for service, values in pillar.get('postgres', {}).items()  %}

{% if values['user'] is defined %}

echo "Create remote database and user for service {{ service }}"
PGPASSWORD={{ values['remote_admin_pw'] }} createdb --host={{ values['remote_db_url'] }} --port={{ values['remote_db_port'] }} --username={{ values['remote_admin'] }} {{ values['database'] }}
echo "CREATE USER {{ values['user'] }} WITH PASSWORD '{{ values['password'] }}';" | PGPASSWORD={{ values['remote_admin_pw'] }} psql --host={{ values['remote_db_url'] }} --port={{ values['remote_db_port'] }} --username={{ values['remote_admin'] }} -v "ON_ERROR_STOP=1" {{ values['database'] }}
echo "GRANT ALL PRIVILEGES ON DATABASE {{ values['user'] }} TO {{ values['database'] }};" | PGPASSWORD={{ values['remote_admin_pw'] }} psql --host={{ values['remote_db_url'] }} --port={{ values['remote_db_port'] }} --username={{ values['remote_admin'] }} -v "ON_ERROR_STOP=1" {{ values['database'] }}
echo "ALTER SCHEMA public OWNER TO {{ values['user'] }};" | PGPASSWORD={{ values['remote_admin_pw'] }} psql --host={{ values['remote_db_url'] }} --port={{ values['remote_db_port'] }} --username={{ values['remote_admin'] }} -v "ON_ERROR_STOP=1" {{ values['database'] }}

{% endif %}

{% endfor %}

set +e
