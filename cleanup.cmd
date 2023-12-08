@REM
@REM Copyright © 2023 Deem
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM DEEM-MOD

REM @ECHO OFF
SET CurrentDir=%~dp0
ECHO %CurrentDir%
PUSHD %CurrentDir%
CLS

if not exist "cleanup.cmd" (
  ECHO You have to start this batch file in the plugin root folder
  pause:
  GOTO :END
)

pause:


del messages_ja_JP.properties /s
del messages_es_AR.properties /s
del messages_es_ES.properties /s
del messages_fr_FR.properties /s
del messages_it_IT.properties /s
del messages_ko_KR.properties /s
del messages_nl_NL.properties /s
del messages_no_NO.properties /s
del messages_pl_PL.properties /s
del messages_pt_BR.properties /s
del messages_pt_PT.properties /s
del messages_pt_PT.properties /s
del messages_zh_CN.properties /s


pause:

:END
