#!/bin/bash

if [ -f /opt/wildfly/.wildfly_admin_created ]; then
  echo "The WildFly 'admin' user has already been created."
  exit 0
fi

#generate password
PASS=${WILDFLY_PASS:-$(pwgen -s 12 1)}
_type=$( [ ${WILDFLY_PASS} ] && echo "preset" || echo "random" )
echo "=> Creating the WildFly user 'admin' with the ${_type} password '${PASS}'."
/opt/wildfly/bin/add-user.sh admin ${PASS} --silent
echo "=> Done!"
touch /opt/wildfly/.wildfly_admin_created
echo "========================================================================="
echo ""
echo "  You can now configure this WildFly server using:"
echo ""
echo "  admin:${PASS}"
echo ""
echo "========================================================================="