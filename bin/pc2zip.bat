@echo off

rem Purpose: print pc2 version number
rem Author : pc2@ecs.csus.edu
rem $HeadURL$

call %0\..\pc2env.bat

java -Xms64M -Xmx768M -cp %libdir%\pc2.jar edu.csus.ecs.pc2.core.archive.ZipPC2

rem eof pc2ver.bat $Id$
