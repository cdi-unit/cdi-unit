#!/usr/bin/env bash

export workfile=`mktemp --tmpdir hide-logs.XXXXXXXXXX`

"$@" > "${workfile}" 2>&1

RESULT=$?

if [ $RESULT -ne 0 ] ; then
    echo "$(<${workfile})"
fi

exit $RESULT
