set PGPASSWORD=4815162342
set port=9999
set user=postgres
 
psql -p %port% -U %user% -f 0-CreateDB-WhereIsTheTrolleybusOrTramBot.sql
forfiles /P . /M *r.sql /C "cmd /c psql -p %port% -U %user% -d WhereIsTheTrolleybusOrTramBot -f @file"